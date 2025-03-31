package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.AudioOptions;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease_light;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase_light;

public class ManualScanActivity extends ScanBaseActivity {

    @BindView(R.id.ready_manual_scan_linearLayout)
    LinearLayout ready_manual_scan_LinearLayout;
    @BindView(R.id.frequency_manual_textView)
    TextView frequency_manual_textView;
    @BindView(R.id.manual_gps_switch)
    SwitchCompat manual_gps_switch;
    @BindView(R.id.manual_scan_linearLayout)
    LinearLayout manual_scan_linearLayout;
    @BindView(R.id.frequency_scan_manual_textView)
    TextView frequency_scan_manual_textView;
    @BindView(R.id.record_data_manual_button)
    Button record_data_manual_button;
    @BindView(R.id.audio_manual_linearLayout)
    LinearLayout audio_manual_linearLayout;
    @BindView(R.id.id_audio_manual_textView)
    TextView id_audio_manual_textView;
    @BindView(R.id.minus_imageView)
    ImageView minus_imageView;
    @BindView(R.id.plus_imageView)
    ImageView plus_imageView;
    @BindView(R.id.gps_manual_imageView)
    ImageView gps_manual_imageView;
    @BindView(R.id.gps_state_manual_textView)
    TextView gps_state_manual_textView;
    @BindView(R.id.view_detection_manual_textView)
    TextView view_detection_manual_textView;

    private boolean isEditFrequency;
    private int frequencyRange;
    private int newFrequency;
    private final byte[] audioOption = {(byte) 0x5A, 0, 0};
    private DialogFragment audioOptions;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    setVisibility("overview");
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    newFrequency = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    frequency_manual_textView.setText(Converters.getFrequency(newFrequency));
                    parameter = ValueCodes.START_LOG;
                    leServiceConnection.getBluetoothLeService().discovering();
                }
            });

    private void setStartScan() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);

        byte[] b = new byte[] {(byte) 0x86, (byte) (YY % 100), (byte) (MM + 1), (byte) DD, (byte) hh, (byte) mm, (byte) ss,
                (byte) ((newFrequency - baseFrequency) / 256), (byte) ((newFrequency - baseFrequency) % 256),
                (byte) (manual_gps_switch.isChecked() ? 0x80 : 0x0)};
        isScanning = TransferBleData.writeStartScan("MANUAL", b);
        if (isScanning) {
            secondParameter = "";
            setVisibility("scanning");
            frequency_scan_manual_textView.setText(Converters.getFrequency(newFrequency));
            if (manual_gps_switch.isChecked()) setGpsSearching(); else setGpsOff();
        }
    }

    private void setStopScan() {
        boolean result = TransferBleData.writeStopScan("MANUAL");
        if (result) {
            clear();
            isScanning = false;
            if (isEditFrequency) {
                isEditFrequency = false;
                Intent intent = new Intent(this, EnterFrequencyActivity.class);
                intent.putExtra(ValueCodes.TITLE, "Change Frequency");
                intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
                intent.putExtra(ValueCodes.RANGE, range);
                launcher.launch(intent);
            } else {
                animationDrawable.stop();
                setVisibility("overview");
            }
            parameter = "";
        }
    }

    private void setRecord() {
        boolean result = TransferBleData.writeRecord(true, true);
        if (result) {
            record_data_manual_button.setText(R.string.lb_record_data);
            record_data_manual_button.setAlpha(1);
            record_data_manual_button.setEnabled(true);
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
            id_audio_manual_textView.setText(audioDescription);
        }
    }

    @OnClick(R.id.enter_new_frequency_button)
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
        leServiceConnection.getBluetoothLeService().discovering();
    }

    @OnClick(R.id.edit_frequency_button)
    public void onClickEditFrequency(View v) {
        isEditFrequency = true;
        secondParameter = ValueCodes.STOP_SCAN;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.record_data_manual_button)
    public void onClickRecordData(View v) {
        secondParameter = ValueCodes.RECORD;
        leServiceConnection.getBluetoothLeService().discoveringSecond();

        record_data_manual_button.setText(R.string.lb_saving_targets);
        record_data_manual_button.setAlpha((float) 0.6);
        record_data_manual_button.setEnabled(false);
    }

    @OnClick(R.id.minus_imageView)
    public void onClickMinus(View v) {
        newFrequency = Converters.getFrequencyNumber(frequency_scan_manual_textView.getText().toString()) - 1;
        secondParameter = ValueCodes.DECREASE;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.plus_imageView)
    public void onClickPlus(View v) {
        newFrequency = Converters.getFrequencyNumber(frequency_scan_manual_textView.getText().toString()) + 1;
        secondParameter = ValueCodes.INCREASE;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.edit_audio_manual_textView)
    public void onClickEditAudio(View v) {
        getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                secondParameter = bundle.getString(ValueCodes.PARAMETER);
                if (secondParameter != null && secondParameter.equals(ValueCodes.AUDIO)) {
                    audioOption[0] = bundle.getByte(ValueCodes.AUDIO);
                    audioOption[1] = (byte) bundle.getInt(ValueCodes.VALUE);
                    audioOption[2] = bundle.getByte(ValueCodes.BACKGROUND);

                    leServiceConnection.getBluetoothLeService().discoveringSecond();
                }
            }
        });
        audioOptions.show(getSupportFragmentManager(), AudioOptions.TAG);
    }

    @OnClick(R.id.view_detection_manual_textView)
    public void onClickViewDetection(View v) {
        viewDetectionFilter.show(getSupportFragmentManager(), ViewDetectionFilter.TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_manual_scan;
        title = getString(R.string.lb_start_scanning);
        super.onCreate(savedInstanceState);

        initializeCallback();
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        isEditFrequency = false;

        if (isScanning) { // The device is already scanning
            parameter = ValueCodes.CONTINUE_LOG;
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            scanState(data);
            setVisibility("scanning");
        } else { // Gets manual defaults data
            newFrequency = baseFrequency;
            frequency_manual_textView.setText(Converters.getFrequency(newFrequency));
            setVisibility("overview");
        }
    }

    @Override
    protected void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                parameter = secondParameter = "";
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.START_LOG)) // Receives the data
                    setNotificationLog();
                else if (parameter.equals(ValueCodes.CONTINUE_LOG))
                    setNotificationLogScanning();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (parameter.equals(ValueCodes.START_LOG)) // Receives the data
                    setCurrentLog(packet);
            }
        };
        secondReceiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {}

            @Override
            public void onGattDiscovered() {
                switch (secondParameter) {
                    case ValueCodes.START_SCAN: // Starts to scan
                        setStartScan();
                        break;
                    case ValueCodes.STOP_SCAN: // Stops scan
                        setStopScan();
                        break;
                    case ValueCodes.RECORD: // Records a code
                        setRecord();
                        break;
                    case ValueCodes.DECREASE:
                        setDecreaseOrIncrease(true);
                        break;
                    case ValueCodes.INCREASE:
                        setDecreaseOrIncrease(false);
                        break;
                    case ValueCodes.AUDIO:
                        setAudio();
                        break;
                }
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {}
        };
        super.initializeCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
        registerReceiver(secondGattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeSecondGattUpdateIntentFilter());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                Intent intent = new Intent(this, ScanningActivity.class);
                startActivity(intent);
                finish();
            } else {
                secondParameter = ValueCodes.STOP_SCAN;
                leServiceConnection.getBluetoothLeService().discoveringSecond();
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
        audio_manual_linearLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        view_detection_manual_textView.setVisibility(visibility);
    }

    private void setGpsOff() {
        gps_manual_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_off));
        gps_state_manual_textView.setText(R.string.lb_off_gps);
    }

    private void setGpsSearching() {
        gps_manual_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_searching));
        gps_state_manual_textView.setText(R.string.lb_searching_gps);
    }

    private void setGpsFailed() {
        gps_manual_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_failed));
        gps_state_manual_textView.setText(R.string.lb_failed_gps);
    }

    private void setGpsValid() {
        gps_manual_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_valid));
        gps_state_manual_textView.setText(R.string.lb_valid_gps);
    }

    /**
     * With the received packet, gets the data of scanning.
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        Log.i(TAG, Converters.getHexValue(data));
        switch (Converters.getHexValue(data[0])) {
            case "50":
                scanState(data);
                break;
            case "51":
                gpsState(data);
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
}