package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.AudioOptions;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Objects;

import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease_light;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase_light;

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
    @BindView(R.id.gps_imageView)
    ImageView gps_imageView;
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
    private final byte[] audioOption = {(byte) 0x5A, 0, 0};
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
        byte[] b = setCalendar();
        b[0] = (byte) 0x86;
        b[7] = (byte) ((newFrequency - baseFrequency) / 256);
        b[8] = (byte) ((newFrequency - baseFrequency) % 256);
        b[9] = (byte) (gps_switch.isChecked() ? 0x80 : 0x0);
        isScanning = TransferBleData.writeStartScan("MANUAL", b);
        if (isScanning) {
            clear();
            frequency_scan_manual_textView.setText(Converters.getFrequency(newFrequency));
            if (gps_switch.isChecked()) setGpsSearching(); else setGpsOff();
            gps_scanning_switch.setChecked(gps_switch.isChecked());
            setVisibility("scanning");
            enableGpsScanning = true;
        }
    }

    private void setStopScan() {
        boolean result = TransferBleData.writeStopScan("MANUAL");
        if (result) {
            clear();
            isScanning = false;
            animationDrawable.stop();
            setVisibility("overview");
            parameter = "";
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
                    minus_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease_light));
                    minus_imageView.setEnabled(false);
                } else if (newFrequency == frequencyRange - 1) {
                    plus_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase));
                    plus_imageView.setEnabled(true);
                }
            } else {
                if (newFrequency == baseFrequency + 1) {
                    minus_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease));
                    minus_imageView.setEnabled(true);
                } else if (newFrequency == frequencyRange) {
                    plus_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase_light));
                    plus_imageView.setEnabled(false);
                }
            }
        }
    }

    private void setAudio() {
        byte[] b;
        String audioDescription = "All";
        if (Converters.getHexValue(audioOption[0]).equals("59"))
            b = new byte[] {audioOption[0], audioOption[1], audioOption[2]};
        else
            b = new byte[] {audioOption[0], audioOption[2]};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
            if (Converters.getHexValue(audioOption[0]).equals("59"))
                audioDescription = "Single (" + Converters.getDecimalValue(audioOption[1]) + ")";
            else if (Converters.getHexValue(audioOption[0]).equals("5B"))
                audioDescription = "None";
            id_audio_textView.setText(audioDescription);
        }
    }

    private void setGps() {
        boolean result = TransferBleData.writeGps(gps_scanning_switch.isChecked());
        if (result) {
            setGpsSearching();
            gps_switch.setChecked(gps_scanning_switch.isChecked());
        } else {
            gps_scanning_switch.setChecked(!gps_scanning_switch.isChecked());
        }
    }

    @OnClick({R.id.enter_new_frequency_button, R.id.edit_frequency_button})
    public void onClickEnterNewFrequency(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, "Change Frequency");
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.start_manual_button)
    public void onClickStartManual(View v) {
        parameter = ValueCodes.START_LOG;
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
                String state = bundle.getString(ValueCodes.PARAMETER);
                if (state != null && state.equals(ValueCodes.AUDIO)) {
                    audioOption[0] = bundle.getByte(ValueCodes.AUDIO);
                    audioOption[1] = (byte) bundle.getInt(ValueCodes.VALUE);
                    audioOption[2] = bundle.getByte(ValueCodes.BACKGROUND);
                    setAudio();
                }
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

        initializeCallback();
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        enableGpsScanning = false;

        if (isScanning) { // The device is already scanning
            parameter = ValueCodes.CONTINUE_LOG;
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            gps_switch.setChecked((Integer.parseInt(Converters.getDecimalValue(data[15])) >> 7 & 1) == 1);
            gps_scanning_switch.setChecked(gps_switch.isChecked());
            if (gps_switch.isChecked()) setGpsSearching(); else setGpsOff();
            scanState(data);
            setVisibility("scanning");
            enableGpsScanning = true;
        } else { // Gets manual defaults data
            newFrequency = baseFrequency;
            frequency_manual_textView.setText(Converters.getFrequency(newFrequency));
            setVisibility("overview");
        }
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                parameter = "";
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.CONTINUE_LOG))
                    setNotificationLogScanning();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                if (Converters.getHexValue(packet[0]).equals("88")) // Battery
                    setBatteryPercent(packet);
                else if (Converters.getHexValue(packet[0]).equals("56")) // Sd Card
                    setSdCardStatus(packet);
                else if (parameter.equals(ValueCodes.START_LOG)) // Receives the data
                    setCurrentLog(packet);
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

    @Override
    protected void updateVisibility(int visibility) {
        super.updateVisibility(visibility);
        audio_linearLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        view_detection_manual_textView.setVisibility(visibility);
    }

    private void setGpsOff() {
        gps_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_off));
        gps_state_textView.setText(R.string.lb_off_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
    }

    private void setGpsSearching() {
        gps_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_searching));
        gps_state_textView.setText(R.string.lb_searching_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
        latitude_textView.setText("");
        longitude_textView.setText("");
    }

    private void setGpsFailed() {
        gps_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_failed));
        gps_state_textView.setText(R.string.lb_failed_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
    }

    private void setGpsValid() {
        gps_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_valid));
        gps_state_textView.setText(R.string.lb_valid_gps);
        coordinates_linearLayout.setVisibility(View.VISIBLE);
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
            case "51":
                gpsState(data);
                break;
            case "A1":
                logGps(data);
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "D0": // Coded
                logScanCoded(data);
                break;
            case "E0": // Non Coded
                int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[3]));
                if (Converters.getHexValue(detectionType).equals("08")) // Non Coded Fixed
                    logScanNonCodedFixed(data, signalStrength);
                else if (Converters.getHexValue(detectionType).equals("07")) // Non Coded Variable
                    logScanNonCodedVariable(data, signalStrength);
                break;
        }
    }

    private void scanState(byte[] data) {
        int frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(data[10])) * 256) +
                (Integer.parseInt(Converters.getDecimalValue(data[11]))));
        frequency_scan_manual_textView.setText(Converters.getFrequency(frequency));
        frequency_manual_textView.setText(Converters.getFrequency(frequency));
        detectionType = data[18];
        int visibility = Converters.getHexValue(detectionType).equals("09") ? View.GONE : View.VISIBLE;
        updateVisibility(visibility);

        if (!Converters.getHexValue(detectionType).equals("09")) {
            initializeDetectionFilter(data);
        } else {
            audioOptions = AudioOptions.newInstance();
        }
    }

    private void gpsState(byte[] data) {
        int state = Integer.parseInt(Converters.getDecimalValue(data[1]));
        if (state == 3) // Valid
            setGpsValid();
        else if (state == 2) // Failed
            setGpsFailed();
        else if (state == 1) // Searching
            setGpsSearching();
    }

    private void logScanHeader(byte[] data) {
        clear();
        int frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                (Integer.parseInt(Converters.getDecimalValue(data[2]))));
        frequency_scan_manual_textView.setText(Converters.getFrequency(frequency));
        frequency_manual_textView.setText(Converters.getFrequency(frequency));
    }

    private void logScanCoded(byte[] data) {
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int code = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int mortality = Integer.parseInt(Converters.getDecimalValue(data[5]));
        scanCoded(code, signalStrength, mortality);
    }

    private void logScanNonCodedFixed(byte[] data, int signalStrength) {
        int period = (Integer.parseInt(Converters.getDecimalValue(data[4])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[5]));
        int pulseRate = 60000 / period;
        int type = Integer.parseInt(Converters.getHexValue(data[0]).replace("E", ""));
        scanNonCodedFixed(period, pulseRate, signalStrength, type);
    }

    private void logScanNonCodedVariable(byte[] data, int signalStrength) {
        int period = (Integer.parseInt(Converters.getDecimalValue(data[4])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[5]));
        scanNonCodedVariable(period, signalStrength);
    }

    private void logGps(byte[] data) {
        String[] coordinates = Converters.getGpsData(data);
        latitude_textView.setText(coordinates[0]);
        longitude_textView.setText(coordinates[1]);
    }
}