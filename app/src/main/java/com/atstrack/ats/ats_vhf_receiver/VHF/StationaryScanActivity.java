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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
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
    @BindView(R.id.stationary_external_data_transfer_switch)
    SwitchCompat stationary_external_data_transfer_switch;
    @BindView(R.id.number_of_antennas_stationary_textView)
    TextView number_of_antennas_stationary_textView;
    @BindView(R.id.scan_timeout_seconds_stationary_textView)
    TextView scan_timeout_seconds_stationary_textView;
    @BindView(R.id.stationary_reference_frequency_switch)
    SwitchCompat stationary_reference_frequency_switch;
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

    private boolean previousScanning;
    private int firstTable;
    private int secondTable;
    private int thirdTable;
    private int antennas;

    /*ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                if (ValueCodes.TABLES_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                    int[] value = result.getData().getExtras().getIntArray(ValueCodes.VALUE);
                    String numbers = "";
                    for (int number : value)
                        numbers += number + ", ";
                    frequency_table_number_stationary_textView.setText(numbers.substring(0, numbers.length() - 2));
                    parameter = ValueCodes.TABLES;
                } else {
                    int value = result.getData().getExtras().getInt(ValueCodes.VALUE);
                    switch (result.getResultCode()) {
                        case ValueCodes.SCAN_RATE_SECONDS_CODE: // Gets the modified scan rate
                            scan_rate_seconds_stationary_textView.setText(String.valueOf(value));
                            parameter = ValueCodes.SCAN_RATE;
                            break;
                        case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Gets the modified scan timeout
                            scan_timeout_seconds_stationary_textView.setText(String.valueOf(value));
                            parameter = ValueCodes.SCAN_TIMEOUT;
                            break;
                        case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Gets the modified number of antennas
                            number_of_antennas_stationary_textView.setText(String.valueOf(value));
                            parameter = ValueCodes.ANTENNA_NUMBER;
                            break;
                        case ValueCodes.STORE_RATE_CODE:
                            store_rate_minutes_stationary_textView.setText((value == 100) ? "Continuous Store" : String.valueOf(value));
                            parameter = ValueCodes.STORE_RATE;
                            break;
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE:
                            reference_frequency_store_rate_stationary_textView.setText(String.valueOf(value));
                            parameter = ValueCodes.REFERENCE_FREQUENCY_STORE_RATE;
                            break;
                        case ValueCodes.RESULT_OK:
                            stationary_reference_frequency_switch.setEnabled(false);
                            frequency_reference_stationary_textView.setText(Converters.getFrequency(value));
                            parameter = ValueCodes.REFERENCE_FREQUENCY;
                            break;
                    }
                }
                setTemporary();
            });

    private void onClickTemporary() {
        byte[] b = new byte[]{(byte) 0x6F, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        switch (parameter) {
            case ValueCodes.TABLES:
                String[] tables = frequency_table_number_stationary_textView.getText().toString().split(", ");
                int firstTableNumber = (tables.length > 0) ? Integer.parseInt(tables[0]) : 0;
                int secondTableNumber = (tables.length > 1) ? Integer.parseInt(tables[1]) : 0;
                int thirdTableNumber = (tables.length > 2) ? Integer.parseInt(tables[2]) : 0;
                b[11] = (byte) firstTableNumber;
                b[12] = (byte) secondTableNumber;
                b[13] = (byte) thirdTableNumber;
                break;
            case ValueCodes.SCAN_RATE:
                b[2] = (byte) (Float.parseFloat(scan_rate_seconds_stationary_textView.getText().toString()) * 10);
                break;
            case ValueCodes.SCAN_TIMEOUT:
                b[3] = (byte) Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString());
                break;
            case ValueCodes.ANTENNA_NUMBER:
                b[1] = (byte) Integer.parseInt(number_of_antennas_stationary_textView.getText().toString());
                break;
            case ValueCodes.STORE_RATE:
                b[7] = (byte) Integer.parseInt(store_rate_minutes_stationary_textView.getText().toString());
                break;
            case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE:
                b[6] = (byte) Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());
                break;
            case ValueCodes.REFERENCE_FREQUENCY:
                int frequency = Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());
                b[4] = (byte) (frequency % 256);
                b[5] = (byte) (frequency / 256);
                break;

        }
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = leServiceConnection.getBluetoothLeService().writeCharacteristic(service, characteristic, b);

        if (result) parameter = "";
        stationary_reference_frequency_switch.setEnabled(true);
    }*/

    private void setStartScan() {
        byte[] b = setCalendar();
        b[0] = (byte) 0x83;
        b[7] = (byte) firstTable;
        b[8] = (byte) secondTable;
        b[9] = (byte) thirdTable;
        isScanning = TransferBleData.writeStartScan(ValueCodes.STATIONARY_DEFAULTS, b);
        if (isScanning)
            setVisibility("scanning");
    }

    private void setStopScan() {
        boolean result = TransferBleData.writeStopScan(ValueCodes.STATIONARY_DEFAULTS);
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

    /*@OnClick(R.id.frequency_table_number_stationary_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLES_NUMBER_CODE);
        intent.putExtra(ValueCodes.FIRST_TABLE_NUMBER, (int) originalData.get(ValueCodes.FIRST_TABLE_NUMBER));
        intent.putExtra(ValueCodes.SECOND_TABLE_NUMBER, (int) originalData.get(ValueCodes.SECOND_TABLE_NUMBER));
        intent.putExtra(ValueCodes.THIRD_TABLE_NUMBER, (int) originalData.get(ValueCodes.THIRD_TABLE_NUMBER));
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_stationary_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_timeout_seconds_stationary_linearLayout)
    public void onClickScanTimeoutSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_TIMEOUT_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.number_of_antennas_stationary_linearLayout)
    public void onClickNumberOfAntennas(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.NUMBER_OF_ANTENNAS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.store_rate_stationary_linearLayout)
    public void onClickStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.STORE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.stationary_reference_frequency_switch)
    public void onCheckedChangedReferenceFrequency(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            reference_frequency_stationary_linearLayout.setEnabled(true);
            int frequency = (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY);
            frequency_reference_stationary_textView.setText(frequency != 0 ? Converters.getFrequency(frequency) : "0");
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(true);
            int storeRate = (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE);
            reference_frequency_store_rate_stationary_textView.setText(String.valueOf(storeRate));
        } else {
            reference_frequency_stationary_linearLayout.setEnabled(false);
            frequency_reference_stationary_textView.setText(R.string.lb_no_reference_frequency);
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(false);
            reference_frequency_store_rate_stationary_textView.setText("0");
        }
    }

    @OnClick(R.id.reference_frequency_stationary_linearLayout)
    public void onClickReferenceFrequency(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, "Reference Frequency");
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.reference_frequency_store_rate_stationary_linearLayout)
    public void onClickReferenceFrequencyStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE);
        launcher.launch(intent);
    }*/

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

        initializeCallback();
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

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                switch (parameter) {
                    case ValueCodes.STATIONARY_DEFAULTS: // Gets stationary defaults data
                        TransferBleData.readDefaults(false);
                        break;
                    case ValueCodes.CONTINUE_LOG:
                        setNotificationLogScanning();
                        break;
                }
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                switch (Converters.getHexValue(packet[0])) {
                    case "88": // Battery
                        setBatteryPercent(packet);
                        break;
                    case "56": // Sd Card
                        setSdCardStatus(packet);
                        break;
                    case "6C": // Gets stationary defaults data
                        downloadData(packet);
                    default: // Receives the scan data
                        setCurrentLog(packet);
                        break;
                }
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
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

    @Override
    protected void updateVisibility(int visibility) {
        super.updateVisibility(visibility);
        view_detection_stationary_textView.setVisibility(visibility);
    }

    /**
     * With the received packet, gets stationary defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        firstTable = Integer.parseInt(Converters.getDecimalValue(data[9]));
        secondTable = Integer.parseInt(Converters.getDecimalValue(data[10]));
        thirdTable = Integer.parseInt(Converters.getDecimalValue(data[11]));
        String tables = "";
        for (int i = 9; i < data.length; i++) {
            if (data[i] != 0 && !Converters.getDecimalValue(data[i]).equals("FF "))
                tables += Converters.getDecimalValue(data[i]) + ", ";
        }
        if (tables.isEmpty()) { // There are no tables with frequencies to scan
            frequency_table_number_stationary_textView.setText(R.string.lb_none);
            start_stationary_button.setEnabled(false);
            start_stationary_button.setAlpha((float) 0.6);
        } else { // Shows the table to be scanned
            frequency_table_number_stationary_textView.setText(tables.substring(0, tables.length() - 2));
            start_stationary_button.setEnabled(true);
            start_stationary_button.setAlpha((float) 1);
        }
        antennas = Integer.parseInt(Converters.getDecimalValue(data[1]));
        number_of_antennas_stationary_textView.setText((antennas == 0) ? "None" : String.valueOf(antennas));
        stationary_external_data_transfer_switch.setChecked(data[2] != 0);
        stationary_external_data_transfer_switch.setEnabled(false);
        scan_rate_seconds_stationary_textView.setText(Converters.getDecimalValue(data[3]));
        scan_timeout_seconds_stationary_textView.setText(Converters.getDecimalValue(data[4]));
        if (Converters.getHexValue(data[5]).equals("00"))
            store_rate_minutes_stationary_textView.setText(R.string.lb_continuous_store);
        else
            store_rate_minutes_stationary_textView.setText(Converters.getDecimalValue(data[5]));
        int frequency = 0;
        if ((!Converters.getHexValue(data[6]).equals("FF") || !Converters.getHexValue(data[7]).equals("FF"))
                && (!Converters.getHexValue(data[6]).equals("00") || !Converters.getHexValue(data[7]).equals("00")))
            frequency = (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                    Integer.parseInt(Converters.getDecimalValue(data[7])) + baseFrequency;
        stationary_reference_frequency_switch.setChecked(frequency != 0);
        stationary_reference_frequency_switch.setEnabled(false);
        frequency_reference_stationary_textView.setText((frequency == 0) ? "No Reference Frequency" : Converters.getFrequency(frequency));
        reference_frequency_store_rate_stationary_textView.setText((frequency == 0) ? "No Reference Frequency" : Converters.getDecimalValue(data[8]));
    }

    /**
     * With the received packet, gets the data of scanning.
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
        antennas = Integer.parseInt(Converters.getDecimalValue(data[1])) >> 7;
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