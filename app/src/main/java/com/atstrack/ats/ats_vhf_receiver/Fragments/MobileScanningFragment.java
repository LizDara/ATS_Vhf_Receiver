package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.DialogsFragment.AudioOptionsDialogFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.OnClick;

public class MobileScanningFragment extends ScanBaseFragment implements ReceiverCallback {
    @BindView(R.id.tv_max_index_mobile)
    TextView tv_max_index_mobile;
    @BindView(R.id.tv_table_index_mobile)
    TextView tv_table_index_mobile;
    @BindView(R.id.tv_frequency_mobile)
    TextView tv_frequency_mobile;
    @BindView(R.id.tv_frequency_hold)
    TextView tv_frequency_hold;
    @BindView(R.id.btn_hold)
    TextView btn_hold;
    @BindView(R.id.tv_edit_table)
    TextView tv_edit_table;
    @BindView(R.id.tv_id_audio)
    TextView tv_id_audio;
    @BindView(R.id.btn_record_data)
    Button btn_record_data;
    @BindView(R.id.tv_gps_state)
    TextView tv_gps_state;
    @BindView(R.id.tv_view_detection_mobile)
    TextView tv_view_detection_mobile;
    @BindView(R.id.layout_coordinates)
    LinearLayout layout_coordinates;
    @BindView(R.id.tv_latitude)
    TextView tv_latitude;
    @BindView(R.id.tv_longitude)
    TextView tv_longitude;
    @BindView(R.id.layout_audio)
    LinearLayout layout_audio;

    private MobileDefaults mobileDefaults;
    private byte[] scanState;
    private boolean isHold; // This can change during scanning
    private boolean isRecord; // This can change during scanning
    private byte[] audioOption = {ValueCodes.AUDIO_ALL_COMMAND, 0, 0};
    private DialogFragment audioOptions;

    public MobileScanningFragment(int baseFrequency, int range, MobileDefaults mobileDefaults) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.mobileDefaults = mobileDefaults;
        isHold = isScanning = false;
    }

    public MobileScanningFragment(int baseFrequency, int range, byte[] data) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.scanState = data;
        isScanning = true;
    }

    @OnClick(R.id.btn_hold)
    public void onClickHold(View v) {
        setHoldScan();
    }

    @OnClick(R.id.img_decrease)
    public void onClickDecrease(View v) {
        if (currentFrequency > baseFrequency)
            setDecreaseOrIncrease(true);
    }

    @OnClick(R.id.img_increase)
    public void onClickIncrease(View v) {
        if (currentFrequency < frequencyRange)
            setDecreaseOrIncrease(false);
    }

    @OnClick(R.id.btn_record_data)
    public void onClickRecordData(View v) {
        setRecordScan();
    }

    @OnClick(R.id.img_left)
    public void onClickLeft(View v) {
        TransferBleData.writeLeftRight(true);
    }

    @OnClick(R.id.img_right)
    public void onClickRight(View v) {
        TransferBleData.writeLeftRight(false);
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

    @OnClick(R.id.tv_edit_table)
    public void onClickEditTable(View v) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new EditTableScanFragment(baseFrequency, range, currentIndex, currentFrequency, totalFrequencies), String.valueOf(ValueCodes.THIRD_STEP))
                    .addToBackStack(String.valueOf(ValueCodes.SECOND_STEP))
                    .commit();
        }
    }

    @OnClick(R.id.tv_view_detection_mobile)
    public void onClickViewDetection(View v) {
        showDetectionAlertDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        scanType = ValueCodes.MOBILE_SCAN_COMMAND;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isScanning) {
            setNotificationLogScanning();
            initializeScanning();
        } else {
            setNotificationLog();
            setStartScan();
        }

        getParentFragmentManager().setFragmentResultListener(ValueCodes.TOTAL, this, (requestKey, result) -> {
            if (!isAdded() || getView() == null) return;
            boolean tableMerged = result.getBoolean(ValueCodes.VALUE, false);
            if (tableMerged) {
                isHold = false;
                setVisibility(ValueCodes.STOP_HOLD);
                showAlertDialog(getString(R.string.lb_tables_merged));
            }
        });
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                if (packet[0] == ValueCodes.FATAL_SCAN_ERROR_COMMAND) {
                    if (!errorScan)
                        showAlertDialog();
                } else {
                    setCurrentLog(packet);
                }
            }
        });
    }

    @Override
    protected void updateVisibility() {
        super.updateVisibility();
        int visibility = detectionType == ValueCodes.CODED ? View.GONE : View.VISIBLE;
        layout_audio.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE); // se muestra solo cuando es CODED
        tv_view_detection_mobile.setVisibility(visibility);
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.START_HOLD) {
            btn_hold.setText(R.string.lb_release);
            tv_frequency_hold.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_lock, 0);
            tv_frequency_mobile.setTextColor(ContextCompat.getColor(requireContext(), R.color.mountain_meadow));
            tv_edit_table.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            tv_edit_table.setEnabled(true);
        } else if (view == ValueCodes.STOP_HOLD) {
            btn_hold.setText(R.string.lb_hold);
            tv_frequency_hold.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_unlock, 0);
            tv_frequency_mobile.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            tv_edit_table.setTextColor(ContextCompat.getColor(requireContext(), R.color.ghost));
            tv_edit_table.setEnabled(false);
        } else if (view == ValueCodes.START_RECORD) {
            btn_record_data.setText(R.string.lb_stop_recording);
            btn_record_data.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_stop));
        } else if (view == ValueCodes.STOP_RECORD) {
            btn_record_data.setText(R.string.lb_record_data);
            btn_record_data.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_primary));
        } else if (view == ValueCodes.GPS_OFF) {
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
        }
    }

    private void setStartScan() {
        byte[] b = Converters.setCalendar(10);
        b[0] = ValueCodes.MOBILE_SCAN_COMMAND;
        b[7] = (byte) mobileDefaults.tableNumber;
        isScanning = TransferBleData.writeStartScan(ValueCodes.MOBILE_SCAN_COMMAND, b);
        if (isScanning) {
            setVisibility(ValueCodes.STOP_HOLD);
            isRecord = mobileDefaults.autoRecordOn;
            setVisibility(isRecord ? ValueCodes.START_RECORD : ValueCodes.STOP_RECORD);
            setVisibility(mobileDefaults.gpsOn ? ValueCodes.GPS_SEARCHING : ValueCodes.GPS_OFF);
            setVisibility(ValueCodes.SCANNING);
        }
    }

    private void initializeScanning() {
        mobileDefaults = new MobileDefaults();
        currentFrequency = (Byte.toUnsignedInt(scanState[16]) * 256) + Byte.toUnsignedInt(scanState[17]) + baseFrequency;
        currentIndex = (Byte.toUnsignedInt(scanState[7]) * 256) + Byte.toUnsignedInt(scanState[8]);
        mobileDefaults.autoRecordOn = isRecord = (Byte.toUnsignedInt(scanState[15]) >> 6 & 1) == 1;
        mobileDefaults.gpsOn = (Byte.toUnsignedInt(scanState[15]) >> 7 & 1) == 1;
        isHold = scanState[1] == ValueCodes.MOBILE_HOLD_COMMAND;
        tv_frequency_mobile.setText(Converters.getFrequency(currentFrequency));
        tv_table_index_mobile.setText(String.valueOf(currentIndex));
        setVisibility(isHold ? ValueCodes.START_HOLD : ValueCodes.STOP_HOLD);
        setVisibility(isRecord ? ValueCodes.START_RECORD : ValueCodes.STOP_RECORD);
        setVisibility(mobileDefaults.gpsOn ? ValueCodes.GPS_SEARCHING : ValueCodes.GPS_OFF);
        scanState(scanState);
    }

    private void setHoldScan() {
        boolean result = TransferBleData.setHold(isHold);
        if (result) {
            isHold = !isHold;
            setVisibility(isHold ? ValueCodes.START_HOLD : ValueCodes.STOP_HOLD);
        }
    }

    private void setDecreaseOrIncrease(boolean isDecrease) {
        boolean result = TransferBleData.writeDecreaseIncrease(isDecrease);
    }

    private void setRecordScan() {
        boolean result = TransferBleData.writeRecord(!isRecord, false);
        if (result) {
            isRecord = !isRecord;
            setVisibility(isRecord ? ValueCodes.START_RECORD : ValueCodes.STOP_RECORD);
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
            case ValueCodes.SCAN_FREQUENCIES_NUMBER_COMMAND:
                frequenciesNumber(data);
                break;
            case ValueCodes.SCAN_HEADER_COMMAND:
                logScanHeader(data);
                break;
            case ValueCodes.SCAN_FIX_CODED_COMMAND:
                logScanCoded(data);
                break;
            case ValueCodes.SCAN_DATA_FIXED_NON_CODED_COMMAND:
            case ValueCodes.SCAN_FIXED_CONSOLIDATED_NON_CODED_COMMAND:
            case ValueCodes.SCAN_DATA_VARIABLE_NON_CODED_COMMAND:
            case ValueCodes.SCAN_VARIABLE_CONSOLIDATED_NON_CODED_COMMAND:
                int signalStrength = Byte.toUnsignedInt(data[4]);
                int period = (Byte.toUnsignedInt(data[5]) * 256) + Byte.toUnsignedInt(data[6]);
                if (detectionType == ValueCodes.FIXED)
                    logScanNonCodedFixed(data[0], period, signalStrength);
                else if (detectionType == ValueCodes.VARIABLE)
                    scanNonCodedVariable(period, signalStrength);
                break;
        }
    }

    private void scanState(byte[] data) {
        totalFrequencies = (Byte.toUnsignedInt(data[5]) * 256) + Byte.toUnsignedInt(data[6]);
        tv_max_index_mobile.setText("Table Index (" + totalFrequencies + " Total)");
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

    private void frequenciesNumber(byte[] data) {
        totalFrequencies = (Byte.toUnsignedInt(data[1]) * 256) + Byte.toUnsignedInt(data[2]);
        tv_max_index_mobile.setText("Table Index (" + totalFrequencies + " Total)");
    }

    private void logScanHeader(byte[] data) {
        clear();
        currentFrequency = (Byte.toUnsignedInt(data[1]) * 256) + Byte.toUnsignedInt(data[2]) + baseFrequency;
        currentIndex = (((Byte.toUnsignedInt(data[1]) >> 6) & 1) * 256) + Byte.toUnsignedInt(data[3]);
        tv_table_index_mobile.setText(String.valueOf(currentIndex));
        tv_frequency_mobile.setText(Converters.getFrequency(currentFrequency));
    }

    private void logScanCoded(byte[] data) {
        int code = Byte.toUnsignedInt(data[3]);
        int signalStrength = Byte.toUnsignedInt(data[4]);
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
    protected void clear() {
        tv_table_index_mobile.setText("");
        tv_frequency_mobile.setText("");
        super.clear();
    }

    private void showAlertDialog(String message) {
        AlertDialog dialog = Dialogs.createFrequenciesDialog(requireContext(), message);
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && getView() != null) {
                dialog.dismiss();
            }
        }, ValueCodes.MESSAGE_PERIOD);
    }
}
