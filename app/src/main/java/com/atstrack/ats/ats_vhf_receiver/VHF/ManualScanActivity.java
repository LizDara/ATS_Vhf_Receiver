package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.AudioOptions;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Objects;

public class ManualScanActivity extends ScanBaseActivity {

    @BindView(R.id.ready_manual_scan_linearLayout)
    LinearLayout ready_manual_scan_LinearLayout;
    @BindView(R.id.frequency_manual_textView)
    TextView frequency_manual_textView;
    @BindView(R.id.gps_switch)
    SwitchCompat gps_switch;
    @BindView(R.id.gps_scanning_switch)
    SwitchCompat gps_scanning_switch;
    @BindView(R.id.manual_scan_linearLayout)
    LinearLayout manual_scan_linearLayout;
    @BindView(R.id.frequency_scan_manual_textView)
    TextView frequency_scan_manual_textView;
    @BindView(R.id.record_data_button)
    Button record_data_button;
    @BindView(R.id.audio_linearLayout)
    LinearLayout audio_linearLayout;
    @BindView(R.id.id_audio_textView)
    TextView id_audio_textView;
    @BindView(R.id.minus_imageView)
    ImageView minus_imageView;
    @BindView(R.id.plus_imageView)
    ImageView plus_imageView;
    @BindView(R.id.gps_state_textView)
    TextView gps_state_textView;
    @BindView(R.id.view_detection_manual_textView)
    TextView view_detection_manual_textView;
    @BindView(R.id.coordinates_linearLayout)
    LinearLayout coordinates_linearLayout;
    @BindView(R.id.latitude_textView)
    TextView latitude_textView;
    @BindView(R.id.longitude_textView)
    TextView longitude_textView;

    private int frequencyRange;
    private int newFrequency;
    private boolean enableGpsScanning;
    private byte[] audioOption = {ValueCodes.AUDIO_ALL_COMMAND, 0, 0};
    private DialogFragment audioOptions;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    setVisibility("overview");
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    newFrequency = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    frequency_manual_textView.setText(Converters.getFrequency(newFrequency));
                    if(manual_scan_linearLayout.getVisibility() == View.VISIBLE) {
                        setStartScan();
                    }
                }
            });

    private void setStartScan() {
        byte[] b = setCalendar(10);
        b[0] = ValueCodes.MANUAL_SCAN_COMMAND;
        b[7] = (byte) ((newFrequency - baseFrequency) / 256);
        b[8] = (byte) ((newFrequency - baseFrequency) % 256);
        b[9] = (byte) (gps_switch.isChecked() ? 0x80 : 0x0);
        isScanning = TransferBleData.writeStartScan(ValueCodes.MANUAL_SCAN_COMMAND, b);
        if (isScanning) {
            frequency_scan_manual_textView.setText(Converters.getFrequency(newFrequency));
            if (gps_switch.isChecked()) setGpsSearching(); else setGpsOff();
            gps_scanning_switch.setChecked(gps_switch.isChecked());
            setVisibility("scanning");
            enableGpsScanning = true;
        }
    }

    private void setStopScan() {
        boolean result = TransferBleData.writeStopScan(ValueCodes.MANUAL_SCAN_COMMAND);
        if (result) {
            clear();
            isScanning = false;
            animationDrawable.stop();
            setVisibility("overview");
        }
    }

    private void setRecord() {
        boolean result = TransferBleData.writeRecord(true, true);
        if (result) {
            record_data_button.setText(R.string.lb_record_data);
            record_data_button.setAlpha(1);
            record_data_button.setEnabled(true);
            clear();
        }
    }

    private void setDecreaseOrIncrease(boolean isDecrease) {
        boolean result = TransferBleData.writeDecreaseIncrease(isDecrease);
        if (result) {
            if (isDecrease) {
                if (newFrequency == baseFrequency) {
                    minus_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_decrease_light));
                    minus_imageView.setEnabled(false);
                } else if (newFrequency == frequencyRange - 1) {
                    plus_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_increase));
                    plus_imageView.setEnabled(true);
                }
            } else {
                if (newFrequency == baseFrequency + 1) {
                    minus_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_decrease));
                    minus_imageView.setEnabled(true);
                } else if (newFrequency == frequencyRange) {
                    plus_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_increase_light));
                    plus_imageView.setEnabled(false);
                }
            }
        }
    }

    private void setAudio() {
        byte[] b;
        if (audioOption[0] == ValueCodes.AUDIO_ONE_COMMAND)
            b = new byte[] {audioOption[0], audioOption[1], audioOption[2]};
        else
            b = new byte[] {audioOption[0], audioOption[2]};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
            String audioDescription = "All";
            if (audioOption[0] == ValueCodes.AUDIO_ONE_COMMAND)
                audioDescription = "Single (" + Byte.toUnsignedInt(audioOption[1]) + ")";
            else if (audioOption[0] == ValueCodes.AUDIO_NONE_COMMAND)
                audioDescription = "None";
            id_audio_textView.setText(audioDescription);
        }
    }

    private void setGps() {
        boolean result = TransferBleData.writeGps(gps_scanning_switch.isChecked());
        if (result) {
            if (gps_scanning_switch.isChecked()) setGpsSearching();
            else setGpsOff();
            gps_switch.setChecked(gps_scanning_switch.isChecked());
        } else {
            enableGpsScanning = false;
            gps_scanning_switch.setChecked(!gps_scanning_switch.isChecked());
            enableGpsScanning = true;
        }
    }

    @OnClick({R.id.enter_new_frequency_button, R.id.edit_frequency_button})
    public void onClickEnterNewFrequency(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, getString(R.string.lb_change_frequency));
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.start_manual_button)
    public void onClickStartManual(View v) {
        setNotificationLog();
        setStartScan();
    }

    @OnClick(R.id.record_data_button)
    public void onClickRecordData(View v) {
        record_data_button.setText(R.string.lb_saving_targets);
        record_data_button.setAlpha((float) 0.6);
        record_data_button.setEnabled(false);
        setRecord();
    }

    @OnClick(R.id.minus_imageView)
    public void onClickMinus(View v) {
        newFrequency = Converters.getFrequencyNumber(frequency_scan_manual_textView.getText().toString()) - 1;
        setDecreaseOrIncrease(true);
    }

    @OnClick(R.id.plus_imageView)
    public void onClickPlus(View v) {
        newFrequency = Converters.getFrequencyNumber(frequency_scan_manual_textView.getText().toString()) + 1;
        setDecreaseOrIncrease(false);
    }

    @OnClick(R.id.edit_audio_textView)
    public void onClickEditAudio(View v) {
        getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                audioOption = bundle.getByteArray(ValueCodes.VALUE);
                if (audioOption != null)
                    setAudio();
            }
        });
        audioOptions.show(getSupportFragmentManager(), AudioOptions.TAG);
    }

    @OnClick(R.id.view_detection_manual_textView)
    public void onClickViewDetection(View v) {
        viewDetectionFilter.show(getSupportFragmentManager(), ViewDetectionFilter.TAG);
    }

    @OnCheckedChanged(R.id.gps_scanning_switch)
    public void onCheckedChangedGps(CompoundButton button, boolean isChecked) {
        if (enableGpsScanning)
            setGps();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_manual_scan;
        title = getString(R.string.lb_start_scanning);
        super.onCreate(savedInstanceState);

        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        enableGpsScanning = false;

        if (isScanning) { // The device is already scanning
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            if (data != null) {
                parameter = ValueCodes.MANUAL_SCAN_COMMAND;
                gps_switch.setChecked((Byte.toUnsignedInt(data[15]) >> 7 & 1) == 1);
                gps_scanning_switch.setChecked(gps_switch.isChecked());
                if (gps_switch.isChecked()) setGpsSearching(); else setGpsOff();
                scanState(data);
                setVisibility("scanning");
                enableGpsScanning = true;
            }
        } else {
            newFrequency = baseFrequency;
            frequency_manual_textView.setText(Converters.getFrequency(newFrequency));
            setVisibility("overview");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                Intent intent = new Intent(this, ScanningActivity.class);
                intent.putExtra(ValueCodes.PARAMETER, ValueCodes.NONE);
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
    protected void updateVisibility() {
        super.updateVisibility();
        int visibility = detectionType == DetectionFilter.CODED ? View.GONE : View.VISIBLE;
        audio_linearLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        view_detection_manual_textView.setVisibility(visibility);
    }

    @Override
    protected void discoverCharacteristic() {
        if (parameter == ValueCodes.MANUAL_SCAN_COMMAND)
            setNotificationLogScanning();
    }

    @Override
    protected void downloadData(byte[] data) {
        if (data[0] == ValueCodes.FATAL_SCAN_ERROR_COMMAND) {
        } else {
            setCurrentLog(data);
        }
    }

    private void setVisibility(String value) {
        switch (value) {
            case "overview":
                ready_manual_scan_LinearLayout.setVisibility(View.VISIBLE);
                manual_scan_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.manual_scanning);
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
                state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
                break;
            case "scanning":
                ready_manual_scan_LinearLayout.setVisibility(View.GONE);
                manual_scan_linearLayout.setVisibility(View.VISIBLE);
                title_toolbar.setText(R.string.lb_manual_scanning);
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close);
                state_view.setBackgroundResource(R.drawable.scanning_animation);
                animationDrawable = (AnimationDrawable) state_view.getBackground();
                animationDrawable.start();
                break;
        }
    }

    private void setGpsOff() {
        gps_state_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_off, 0, 0, 0);
        gps_state_textView.setText(R.string.lb_off_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
    }

    private void setGpsSearching() {
        gps_state_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_searching, 0, 0, 0);
        gps_state_textView.setText(R.string.lb_searching_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
        latitude_textView.setText("");
        longitude_textView.setText("");
    }

    private void setGpsFailed() {
        gps_state_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_failed, 0, 0, 0);
        gps_state_textView.setText(R.string.lb_failed_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
    }

    private void setGpsValid() {
        gps_state_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_valid, 0, 0, 0);
        gps_state_textView.setText(R.string.lb_valid_gps);
        coordinates_linearLayout.setVisibility(View.VISIBLE);
    }

    /**
     * With the received packet, get the data of scanning.
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        switch (data[0]) {
            case ValueCodes.SCAN_STATE_COMMAND:
                scanState(data);
                break;
            case ValueCodes.SCAN_GPS_STATE_COMMAND:
                gpsState(data);
                break;
            case ValueCodes.SCAN_GPS_COMMAND:
                logGps(data);
                break;
            case ValueCodes.SCAN_HEADER_COMMAND:
                logScanHeader(data);
                break;
            case ValueCodes.SCAN_MANUAL_CODED_COMMAND:
                logScanCoded(data);
                break;
            case ValueCodes.SCAN_MANUAL_NON_CODED_COMMAND:
                int signalStrength = Byte.toUnsignedInt(data[3]);
                int period = (Byte.toUnsignedInt(data[4]) * 256) + Byte.toUnsignedInt(data[5]);
                if (detectionType == DetectionFilter.FIXED)
                    logScanNonCodedFixed(data[0], period, signalStrength);
                else if (detectionType == DetectionFilter.VARIABLE)
                    scanNonCodedVariable(period, signalStrength);
                break;
        }
    }

    private void scanState(byte[] data) {
        int frequency = baseFrequency + ((Byte.toUnsignedInt(data[10]) * 256) + Byte.toUnsignedInt(data[11]));
        frequency_scan_manual_textView.setText(Converters.getFrequency(frequency));
        frequency_manual_textView.setText(Converters.getFrequency(frequency));
        detectionType = data[18];
        scanDetailListAdapter = new ScanDetailListAdapter(this, detectionType == DetectionFilter.CODED);
        item_recyclerView.setAdapter(scanDetailListAdapter);
        item_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateVisibility();

        if (detectionType != DetectionFilter.CODED)
            initializeDetectionFilter(data);
        else
            audioOptions = AudioOptions.newInstance();
    }

    private void gpsState(byte[] data) {
        if (data[1] == ValueCodes.GPS_VALID)
            setGpsValid();
        else if (data[1] == ValueCodes.GPS_FAILED)
            setGpsFailed();
        else if (data[1] == ValueCodes.GPS_SEARCHING)
            setGpsSearching();
    }

    private void logScanHeader(byte[] data) {
        clear();
        int frequency = baseFrequency + ((Byte.toUnsignedInt(data[1]) * 256) + (Byte.toUnsignedInt(data[2])));
        frequency_scan_manual_textView.setText(Converters.getFrequency(frequency));
        frequency_manual_textView.setText(Converters.getFrequency(frequency));
    }

    private void logScanCoded(byte[] data) {
        int signalStrength = Byte.toUnsignedInt(data[3]);
        int code = Byte.toUnsignedInt(data[4]);
        int mortality = Byte.toUnsignedInt(data[5]);
        scanCoded(code, signalStrength, mortality);
    }

    private void logScanNonCodedFixed(byte format, int period, int signalStrength) {
        int type = Integer.parseInt(Converters.getHexValue(format).replace("E", ""));
        scanNonCodedFixed(period, signalStrength, type);
    }

    private void logGps(byte[] data) {
        String[] coordinates = Converters.getGpsData(data);
        latitude_textView.setText(coordinates[0]);
        longitude_textView.setText(coordinates[1]);
    }
}