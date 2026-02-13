package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.StationaryDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Objects;

public class StationaryScanActivity extends ScanBaseActivity {

    @BindView(R.id.ready_stationary_scan_LinearLayout)
    LinearLayout ready_stationary_scan_LinearLayout;
    @BindView(R.id.scan_rate_seconds_stationary_textView)
    TextView scan_rate_seconds_stationary_textView;
    @BindView(R.id.frequency_table_number_stationary_textView)
    TextView frequency_table_number_stationary_textView;
    @BindView(R.id.store_rate_minutes_stationary_textView)
    TextView store_rate_minutes_stationary_textView;
    @BindView(R.id.stationary_external_data_transfer_textView)
    TextView stationary_external_data_transfer_textView;
    @BindView(R.id.number_of_antennas_stationary_textView)
    TextView number_of_antennas_stationary_textView;
    @BindView(R.id.scan_timeout_seconds_stationary_textView)
    TextView scan_timeout_seconds_stationary_textView;
    @BindView(R.id.stationary_reference_frequency_textView)
    TextView stationary_reference_frequency_textView;
    @BindView(R.id.frequency_reference_stationary_textView)
    TextView frequency_reference_stationary_textView;
    @BindView(R.id.reference_frequency_store_rate_stationary_textView)
    TextView reference_frequency_store_rate_stationary_textView;
    @BindView(R.id.reference_frequency_stationary_linearLayout)
    LinearLayout reference_frequency_stationary_linearLayout;
    @BindView(R.id.reference_frequency_store_rate_stationary_linearLayout)
    LinearLayout reference_frequency_store_rate_stationary_linearLayout;
    @BindView(R.id.start_stationary_button)
    Button start_stationary_button;
    @BindView(R.id.stationary_result_linearLayout)
    LinearLayout stationary_result_linearLayout;
    @BindView(R.id.max_index_stationary_textView)
    TextView max_index_stationary_textView;
    @BindView(R.id.index_stationary_textView)
    TextView index_stationary_textView;
    @BindView(R.id.frequency_stationary_textView)
    TextView frequency_stationary_textView;
    @BindView(R.id.current_antenna_stationary_textView)
    TextView current_antenna_stationary_textView;
    @BindView(R.id.view_detection_stationary_textView)
    TextView view_detection_stationary_textView;
    @BindView(R.id.external_reference_default_linearLayout)
    LinearLayout external_reference_default_linearLayout;
    @BindView(R.id.external_reference_scan_linearLayout)
    LinearLayout external_reference_scan_linearLayout;

    private StationaryDefaults stationaryDefaults;
    private boolean previousScanning;
    private boolean goEditDefault;

    private void setStartScan() {
        byte[] b = setCalendar();
        b[0] = (byte) 0x83;
        b[7] = (byte) stationaryDefaults.firstTableNumber;
        b[8] = (byte) stationaryDefaults.secondTableNumber;
        b[9] = (byte) stationaryDefaults.thirdTableNumber;
        isScanning = TransferBleData.writeStartScan(ValueCodes.STATIONARY, b);
        if (isScanning)
            setVisibility("scanning");
    }

    private void setStopScan() {
        boolean result = TransferBleData.writeStopScan(ValueCodes.STATIONARY);
        if (result) {
            clear();
            isScanning = false;
            setVisibility("overview");
            animationDrawable.stop();
            if (previousScanning) {
                new Handler().postDelayed(() -> {
                    TransferBleData.readDefaults(false);
                }, ValueCodes.WAITING_PERIOD);
                previousScanning = false;
            }
        }
    }

    @OnClick(R.id.edit_stationary_default_textView)
    public void onClickStationaryDefault(View v) {
        goEditDefault = true;
        Intent intent = new Intent(this, StationaryDefaultsActivity.class);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.originalBytes);
        startActivity(intent);
    }

    @OnClick(R.id.start_stationary_button)
    public void onClickStartStationary(View v) {
        setNotificationLog();
        setStartScan();
    }

    @OnClick(R.id.view_detection_stationary_textView)
    public void onClickViewDetection(View v) {
        viewDetectionFilter.show(getSupportFragmentManager(), ViewDetectionFilter.TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_stationary_scan;
        title = getString(R.string.stationary_scanning);
        super.onCreate(savedInstanceState);

        goEditDefault = false;
        byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
        if (isScanning) { // The device is already scanning
            previousScanning = true;
            parameter = ValueCodes.CONTINUE_LOG;

            int currentFrequency = (Integer.parseInt(Converters.getDecimalValue(data[16])) * 256)
                    + Integer.parseInt(Converters.getDecimalValue(data[17])) + baseFrequency;
            int currentIndex = (Integer.parseInt(Converters.getDecimalValue(data[7])) * 256)
                    + Integer.parseInt(Converters.getDecimalValue(data[8]));
            int currentAntenna = Integer.parseInt(Converters.getDecimalValue(data[9]));
            detectionType = getIntent().getByteExtra(ValueCodes.DETECTION_TYPE, (byte) 0);
            frequency_stationary_textView.setText(Converters.getFrequency(currentFrequency));
            index_stationary_textView.setText(String.valueOf(currentIndex));
            current_antenna_stationary_textView.setText((currentAntenna == 0) ? "All" : String.valueOf(currentAntenna));
            scanState(data);
            setVisibility("scanning");
        } else { // Gets aerial defaults data
            downloadData(data);
            previousScanning = false;
            setVisibility("overview");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                Intent intent = new Intent(this, ScanningActivity.class);
                intent.putExtra(ValueCodes.PARAMETER, "");
                startActivity(intent);
                finish();
            } else {
                setStopScan();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isScanning && goEditDefault) {
            goEditDefault = false;
            TransferBleData.readDefaults(false);
        }
    }

    @Override
    protected void updateVisibility(int visibility) {
        super.updateVisibility(visibility);
        view_detection_stationary_textView.setVisibility(visibility);
    }

    @Override
    protected void discoverCharacteristic() {
        switch (parameter) {
            case ValueCodes.STATIONARY: // Gets stationary defaults data
                TransferBleData.readDefaults(false);
                break;
            case ValueCodes.CONTINUE_LOG:
                setNotificationLogScanning();
                break;
        }
    }

    @Override
    protected void downloadData(byte[] data) {
        super.downloadData(data);
        switch (Converters.getHexValue(data[0])) {
            case "6C": // Get stationary defaults data
                downloadStationaryDefault(data);
                break;
            case "44": // Fatal Scan Error
                break;
            default: // Get log scan
                setCurrentLog(data);
                break;
        }
    }

    private void downloadStationaryDefault(byte[] data) {
        external_reference_default_linearLayout.setVisibility(View.GONE);
        external_reference_scan_linearLayout.setVisibility(View.VISIBLE);
        stationaryDefaults = new StationaryDefaults(baseFrequency, data);
        String tables = "";
        if (stationaryDefaults.firstTableNumber != 0)
            tables += stationaryDefaults.firstTableNumber;
        if (stationaryDefaults.secondTableNumber != 0)
            tables += ", " + stationaryDefaults.secondTableNumber;
        if (stationaryDefaults.thirdTableNumber != 0)
            tables += ", " + stationaryDefaults.thirdTableNumber;
        frequency_table_number_stationary_textView.setText(tables.isEmpty() ? "None" : tables);
        number_of_antennas_stationary_textView.setText((stationaryDefaults.antennaNumber == 0) ? "None" : String.valueOf(stationaryDefaults.antennaNumber));
        stationary_external_data_transfer_textView.setText(stationaryDefaults.dataTransferOn ? "On" : "Off");
        scan_rate_seconds_stationary_textView.setText(String.valueOf(stationaryDefaults.scanRate));
        scan_timeout_seconds_stationary_textView.setText(String.valueOf(stationaryDefaults.scanTimeout));
        store_rate_minutes_stationary_textView.setText(stationaryDefaults.storeRate == 0 ? getString(R.string.lb_continuous_store) : String.valueOf(stationaryDefaults.storeRate));
        frequency_reference_stationary_textView.setText((stationaryDefaults.referenceFrequencyOn) ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : "No Reference Frequency");
        reference_frequency_store_rate_stationary_textView.setText((stationaryDefaults.referenceFrequencyOn) ? String.valueOf(stationaryDefaults.referenceStoreRate) : "No Reference Frequency");
        stationary_reference_frequency_textView.setText(stationaryDefaults.referenceFrequencyOn ? "On" : "Off");
    }

    private void setVisibility(String value) {
        switch (value) {
            case "overview":
                ready_stationary_scan_LinearLayout.setVisibility(View.VISIBLE);
                stationary_result_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.stationary_scanning);
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
                state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
                break;
            case "scanning":
                ready_stationary_scan_LinearLayout.setVisibility(View.GONE);
                stationary_result_linearLayout.setVisibility(View.VISIBLE);
                title_toolbar.setText(R.string.lb_stationary_scanning);
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close);
                state_view.setBackgroundResource(R.drawable.scanning_animation);
                animationDrawable = (AnimationDrawable) state_view.getBackground();
                animationDrawable.start();
                break;
        }
    }

    /**
     * With the received packet, get the data of scanning.
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        switch (Converters.getHexValue(data[0])) {
            case "50":
                scanState(data);
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "F1": //Coded
            case "F2": //Consolidated
                logScanCoded(data);
                break;
            case "E1":
            case "E2":
            case "EA": //Non Coded
                int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
                int period = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
                if (Converters.getHexValue(detectionType).equals("08")) // Non Coded Fixed
                    logScanNonCodedFixed(data[0], period, signalStrength);
                else if (Converters.getHexValue(detectionType).equals("07")) // Non Coded Variable
                    scanNonCodedVariable(period, signalStrength);
                break;
        }
    }

    private void scanState(byte[] data) {
        int maxIndex = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        max_index_stationary_textView.setText("Table Index (" + maxIndex + " Total)");
        detectionType = data[18];
        scanDetailListAdapter = new ScanDetailListAdapter(this, Converters.getHexValue(detectionType).equals("09"));
        item_recyclerView.setAdapter(scanDetailListAdapter);
        item_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        int visibility = Converters.getHexValue(detectionType).equals("09") ? View.GONE : View.VISIBLE;
        updateVisibility(visibility);

        if (!Converters.getHexValue(detectionType).equals("09")) {
            initializeDetectionFilter(data);
        }
    }

    /**
     * With the received packet, processes the data of scan header to display.
     * @param data The received packet.
     */
    private void logScanHeader(byte[] data) {
        clear();
        int frequency = ((Integer.parseInt(Converters.getDecimalValue(data[1])) & 63) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + baseFrequency;
        int index = (((Integer.parseInt(Converters.getDecimalValue(data[1])) >> 6) & 1) * 256) + Integer.parseInt(Converters.getDecimalValue(data[3]));
        int antennas = Integer.parseInt(Converters.getDecimalValue(data[1])) >> 7;
        if (antennas == 0) {
            antennas = (Integer.parseInt(Converters.getDecimalValue(data[7])) >> 6) + 1;
            current_antenna_stationary_textView.setText(String.valueOf(antennas));
        } else {
            current_antenna_stationary_textView.setText(R.string.lb_all);
        }
        index_stationary_textView.setText(String.valueOf(index));
        frequency_stationary_textView.setText(Converters.getFrequency(frequency));
    }

    private void logScanCoded(byte[] data) {
        int code = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int mortality = Integer.parseInt(Converters.getDecimalValue(data[5]));
        scanCoded(code, signalStrength, mortality);
    }

    private void logScanNonCodedFixed(byte format, int period, int signalStrength) {
        int type = Integer.parseInt(Converters.getHexValue(format).replace("E", ""));
        scanNonCodedFixed(period, signalStrength, type);
    }

    @Override
    protected void clear() {
        frequency_stationary_textView.setText("");
        index_stationary_textView.setText("");
        super.clear();
    }
}