package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.DialogsFragment.AudioOptionsDialogFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class ManualScanningFragment extends ScanBaseFragment implements ReceiverCallback {
    @BindView(R.id.sw_gps_scanning)
    SwitchCompat sw_gps_scanning;
    @BindView(R.id.tv_frequency_scan_manual)
    TextView tv_frequency_scan_manual;
    @BindView(R.id.btn_record_data)
    Button btn_record_data;
    @BindView(R.id.layout_audio)
    LinearLayout layout_audio;
    @BindView(R.id.tv_id_audio)
    TextView tv_id_audio;
    @BindView(R.id.img_minus)
    ImageView img_minus;
    @BindView(R.id.img_plus)
    ImageView img_plus;
    @BindView(R.id.tv_gps_state)
    TextView tv_gps_state;
    @BindView(R.id.tv_view_detection_manual)
    TextView tv_view_detection_manual;
    @BindView(R.id.layout_coordinates)
    LinearLayout layout_coordinates;
    @BindView(R.id.tv_latitude)
    TextView tv_latitude;
    @BindView(R.id.tv_longitude)
    TextView tv_longitude;

    private boolean gpsOn;
    private boolean enableGpsScanning;
    private byte[] audioOption = {ValueCodes.AUDIO_ALL_COMMAND, 0, 0};
    private DialogFragment audioOptions;
    private ActivityResultLauncher<Intent> launcher;
    private byte[] scanStateScanning;

    public ManualScanningFragment(int baseFrequency, int range, int currentFrequency, boolean gpsOn) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.currentFrequency = currentFrequency;
        this.gpsOn = gpsOn;
        isScanning = false;
        enableGpsScanning = true;
        initializeLauncher();
    }

    public ManualScanningFragment(int baseFrequency, int range, byte[] data) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.scanStateScanning = data;
        isScanning = true;
        enableGpsScanning = true;
        initializeLauncher();

    }

    @OnClick(R.id.btn_record_data)
    public void onClickRecordData(View v) {
        setVisibility(ValueCodes.START_RECORD);
        setRecord();
    }

    @OnClick(R.id.btn_edit_frequency)
    public void onClickEnterNewFrequency(View v) {
        Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, getString(R.string.lb_change_frequency));
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.img_minus)
    public void onClickMinus(View v) {
        currentFrequency = Converters.getFrequencyNumber(tv_frequency_scan_manual.getText().toString()) - 1;
        setDecreaseOrIncrease(true);
    }

    @OnClick(R.id.img_plus)
    public void onClickPlus(View v) {
        currentFrequency = Converters.getFrequencyNumber(tv_frequency_scan_manual.getText().toString()) + 1;
        setDecreaseOrIncrease(false);
    }

    @OnClick(R.id.tv_edit_audio)
    public void onClickEditAudio(View v) {
        getParentFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                audioOption = bundle.getByteArray(ValueCodes.VALUE);
                if (audioOption != null)
                    setAudio();
            }
        });
        audioOptions.show(getParentFragmentManager(), AudioOptionsDialogFragment.TAG);
    }

    @OnClick(R.id.tv_view_detection_manual)
    public void onClickViewDetection(View v) {
        showDetectionAlertDialog();
    }

    @OnCheckedChanged(R.id.sw_gps_scanning)
    public void onCheckedChangedGps(CompoundButton button, boolean isChecked) {
        if (enableGpsScanning)
            setGps();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        scanType = ValueCodes.MANUAL_SCAN_COMMAND;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isScanning) {
            initializeScanning();
            setNotificationLogScanning();
        } else {
            setNotificationLog();
            setStartScan();
        }
    }

    @Override
    protected void updateVisibility() {
        super.updateVisibility();
        int visibility = detectionType == ValueCodes.CODED ? View.GONE : View.VISIBLE;
        layout_audio.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        tv_view_detection_manual.setVisibility(visibility);
    }

    private void initializeScanning() {
        sw_gps_scanning.setChecked((Byte.toUnsignedInt(scanStateScanning[15]) >> 7 & 1) == 1);
        setVisibility(sw_gps_scanning.isChecked() ? ValueCodes.GPS_SEARCHING : ValueCodes.GPS_OFF);
        scanState(scanStateScanning);
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (ValueCodes.CANCELLED == result.getResultCode())
                        return;
                    if (ValueCodes.RESULT_OK == result.getResultCode()) {
                        currentFrequency = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                        setStartScan();
                    }
                });
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.GPS_OFF) {
            tv_gps_state.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_off, 0, 0, 0);
            tv_gps_state.setText(R.string.lb_off_gps);
            layout_coordinates.setVisibility(View.GONE);
        } else if (view == ValueCodes.GPS_SEARCHING) {
            tv_gps_state.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_searching, 0, 0, 0);
            tv_gps_state.setText(R.string.lb_searching_gps);
            layout_coordinates.setVisibility(View.GONE);
            tv_latitude.setText("");
            tv_longitude.setText("");
        } else if (view == ValueCodes.GPS_FAILED) {
            tv_gps_state.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_failed, 0, 0, 0);
            tv_gps_state.setText(R.string.lb_failed_gps);
            layout_coordinates.setVisibility(View.GONE);
        } else if (view == ValueCodes.GPS_VALID) {
            tv_gps_state.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_valid, 0, 0, 0);
            tv_gps_state.setText(R.string.lb_valid_gps);
            layout_coordinates.setVisibility(View.VISIBLE);
        } else if (view == ValueCodes.START_RECORD) {
            btn_record_data.setText(R.string.lb_saving_targets);
            btn_record_data.setAlpha((float) 0.6);
            btn_record_data.setEnabled(false);
        } else if (view == ValueCodes.STOP_RECORD) {
            btn_record_data.setText(R.string.lb_record_data);
            btn_record_data.setAlpha(1);
            btn_record_data.setEnabled(true);
        }
    }

    private void setStartScan() {
        byte[] b = Converters.setCalendar(10);
        b[0] = ValueCodes.MANUAL_SCAN_COMMAND;
        b[7] = (byte) ((currentFrequency - baseFrequency) / 256);
        b[8] = (byte) ((currentFrequency - baseFrequency) % 256);
        b[9] = (byte) (gpsOn ? 0x80 : 0x0);
        isScanning = TransferBleData.writeStartScan(ValueCodes.MANUAL_SCAN_COMMAND, b);
        if (isScanning) {
            tv_frequency_scan_manual.setText(Converters.getFrequency(currentFrequency));
            sw_gps_scanning.setChecked(gpsOn);
            setVisibility(sw_gps_scanning.isChecked() ? ValueCodes.GPS_SEARCHING : ValueCodes.GPS_OFF);
            setVisibility(ValueCodes.SCANNING);
            enableGpsScanning = true;
        }
    }

    private void setRecord() {
        boolean result = TransferBleData.writeRecord(true, true);
        if (result) {
            setVisibility(ValueCodes.STOP_RECORD);
            clear();
        }
    }

    private void setDecreaseOrIncrease(boolean isDecrease) {
        boolean result = TransferBleData.writeDecreaseIncrease(isDecrease);
        if (result) {
            if (isDecrease) {
                if (currentFrequency == baseFrequency) {
                    img_minus.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_decrease_light));
                    img_minus.setEnabled(false);
                } else if (currentFrequency == frequencyRange - 1) {
                    img_plus.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_increase));
                    img_plus.setEnabled(true);
                }
            } else {
                if (currentFrequency == baseFrequency + 1) {
                    img_minus.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_decrease));
                    img_minus.setEnabled(true);
                } else if (currentFrequency == frequencyRange) {
                    img_plus.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_increase_light));
                    img_plus.setEnabled(false);
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
            else if (audioOption[0] == ValueCodes.AUDIO_BACKGROUND_COMMAND)
                audioDescription = "None";
            tv_id_audio.setText(audioDescription);
        }
    }

    private void setGps() {
        boolean result = TransferBleData.writeGps(sw_gps_scanning.isChecked());
        if (result) {
            setVisibility(sw_gps_scanning.isChecked() ? ValueCodes.GPS_SEARCHING : ValueCodes.GPS_OFF);
        } else {
            enableGpsScanning = false;
            sw_gps_scanning.setChecked(!sw_gps_scanning.isChecked());
            enableGpsScanning = true;
        }
    }

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
                if (detectionType == ValueCodes.FIXED)
                    logScanNonCodedFixed(data[0], period, signalStrength);
                else if (detectionType == ValueCodes.VARIABLE)
                    scanNonCodedVariable(period, signalStrength);
                break;
        }
    }

    private void scanState(byte[] data) {
        int frequency = baseFrequency + ((Byte.toUnsignedInt(data[10]) * 256) + Byte.toUnsignedInt(data[11]));
        tv_frequency_scan_manual.setText(Converters.getFrequency(frequency));
        detectionType = data[18];
        scanDetailAdapter = new ScanDetailAdapter(requireContext(), detectionType == ValueCodes.CODED);
        rv_item.setAdapter(scanDetailAdapter);
        rv_item.setLayoutManager(new LinearLayoutManager(requireContext()));
        updateVisibility();

        if (detectionType != ValueCodes.CODED)
            initializeDetectionFilter(data);
        else
            audioOptions = AudioOptionsDialogFragment.newInstance();
    }

    private void gpsState(byte[] data) {
        setVisibility(data[1]);
    }

    private void logScanHeader(byte[] data) {
        clear();
        int frequency = baseFrequency + ((Byte.toUnsignedInt(data[1]) * 256) + (Byte.toUnsignedInt(data[2])));
        tv_frequency_scan_manual.setText(Converters.getFrequency(frequency));
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
        tv_latitude.setText(coordinates[0]);
        tv_longitude.setText(coordinates[1]);
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                if (packet[0] == ValueCodes.FATAL_SCAN_ERROR_COMMAND && !errorScan)
                    showAlertDialog();
                else
                    setCurrentLog(packet);
            }
        });
    }
}
