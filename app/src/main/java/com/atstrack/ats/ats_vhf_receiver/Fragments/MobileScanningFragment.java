package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentMobileScanningBinding;

public class MobileScanningFragment extends ScanBaseFragment implements ReceiverCallback {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        scanType = ValueCodes.MOBILE_SCAN_COMMAND;
        binding = FragmentMobileScanningBinding.inflate(inflater, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((FragmentMobileScanningBinding) binding).btnHold.setOnClickListener(v -> setHoldScan());
        ((FragmentMobileScanningBinding) binding).imgDecrease.setOnClickListener(v -> {
            if (currentFrequency > baseFrequency)
                setDecreaseOrIncrease(true);
        });
        ((FragmentMobileScanningBinding) binding).imgIncrease.setOnClickListener(v -> {
            if (currentFrequency < frequencyRange)
                setDecreaseOrIncrease(false);
        });
        ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setOnClickListener(v -> setRecordScan());
        ((FragmentMobileScanningBinding) binding).imgLeft.setOnClickListener(v -> TransferBleData.writeLeftRight(true));
        ((FragmentMobileScanningBinding) binding).imgRight.setOnClickListener(v -> TransferBleData.writeLeftRight(false));
        ((FragmentMobileScanningBinding) binding).includeAudioOption.tvEditAudio.setOnClickListener(v -> {
            getParentFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    audioOption = bundle.getByteArray(ValueCodes.VALUE);
                    if (audioOption != null)
                        setAudio();
                }
            });
            audioOptions.show(getParentFragmentManager(), AudioOptionsDialogFragment.TAG);
        });
        ((FragmentMobileScanningBinding) binding).tvEditTable.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment, new EditTableScanFragment(baseFrequency, range, currentIndex, currentFrequency, totalFrequencies), String.valueOf(ValueCodes.THIRD_STEP))
                        .addToBackStack(String.valueOf(ValueCodes.SECOND_STEP))
                        .commit();
            }
        });
        ((FragmentMobileScanningBinding) binding).tvViewDetectionMobile.setOnClickListener(v -> showDetectionAlertDialog());
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
                showAlertDialog(getString(R.string.lbl_vhf_mobile_tables_merged));
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
    protected void updateVisibility(TextView tv_code_period, TextView tv_mortality_pulse_rate) {
        super.updateVisibility(tv_code_period, tv_mortality_pulse_rate);
        int visibility = detectionType == ValueCodes.CODED ? View.GONE : View.VISIBLE;
        ((FragmentMobileScanningBinding) binding).includeAudioOption.layoutAudio.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE); // se muestra solo cuando es CODED
        ((FragmentMobileScanningBinding) binding).tvViewDetectionMobile.setVisibility(visibility);
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.START_HOLD) {
            ((FragmentMobileScanningBinding) binding).btnHold.setText(R.string.btn_vhf_mobile_release);
            ((FragmentMobileScanningBinding) binding).tvFrequencyHold.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_lock, 0);
            ((FragmentMobileScanningBinding) binding).tvFrequencyMobile.setTextColor(ContextCompat.getColor(requireContext(), R.color.mountain_meadow));
            ((FragmentMobileScanningBinding) binding).tvEditTable.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            ((FragmentMobileScanningBinding) binding).tvEditTable.setEnabled(true);
        } else if (view == ValueCodes.STOP_HOLD) {
            ((FragmentMobileScanningBinding) binding).btnHold.setText(R.string.btn_vhf_mobile_hold);
            ((FragmentMobileScanningBinding) binding).tvFrequencyHold.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_unlock, 0);
            ((FragmentMobileScanningBinding) binding).tvFrequencyMobile.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            ((FragmentMobileScanningBinding) binding).tvEditTable.setTextColor(ContextCompat.getColor(requireContext(), R.color.ghost));
            ((FragmentMobileScanningBinding) binding).tvEditTable.setEnabled(false);
        } else if (view == ValueCodes.START_RECORD) {
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setText(R.string.btn_vhf_mobile_stop_recording);
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_stop));
        } else if (view == ValueCodes.STOP_RECORD) {
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setText(R.string.btn_vhf_manual_record_data);
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_primary));
        } else if (view == ValueCodes.GPS_OFF) {
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_off, 0, 0, 0);
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_off);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
        } else if (view == ValueCodes.GPS_SEARCHING) {
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_searching, 0, 0, 0);
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_searching);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.tvLatitude.setText("");
            ((FragmentMobileScanningBinding) binding).includeCoordinates.tvLongitude.setText("");
        } else if (view == ValueCodes.GPS_FAILED) {
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_failed, 0, 0, 0);
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_failed);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
        } else if (view == ValueCodes.GPS_VALID) {
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_valid, 0, 0, 0);
            ((FragmentMobileScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_valid);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.VISIBLE);
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
        ((FragmentMobileScanningBinding) binding).tvFrequencyMobile.setText(Converters.getFrequency(currentFrequency));
        ((FragmentMobileScanningBinding) binding).tvTableIndexMobile.setText(String.valueOf(currentIndex));
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
            ((FragmentMobileScanningBinding) binding).includeAudioOption.tvIdAudio.setText(audioDescription);
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
                else if (detectionType == ValueCodes.VARIABLE) {
                    if (period > 0)
                        scanNonCodedVariable(period, signalStrength);
                }
                break;
        }
    }

    private void scanState(byte[] data) {
        totalFrequencies = (Byte.toUnsignedInt(data[5]) * 256) + Byte.toUnsignedInt(data[6]);
        ((FragmentMobileScanningBinding) binding).tvMaxIndexMobile.setText("Table Index (" + totalFrequencies + " Total)");
        detectionType = data[18];
        scanDetailAdapter = new ScanDetailAdapter(requireContext(), detectionType == ValueCodes.CODED);
        ((FragmentMobileScanningBinding) binding).includeScanDetails.includeRecyclerView.rvItem.setAdapter(scanDetailAdapter);
        ((FragmentMobileScanningBinding) binding).includeScanDetails.includeRecyclerView.rvItem.setLayoutManager(new LinearLayoutManager(requireContext()));
        updateVisibility(((FragmentMobileScanningBinding) binding).includeScanDetails.tvCodePeriod, ((FragmentMobileScanningBinding) binding).includeScanDetails.tvMortalityPulseRate);
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
        ((FragmentMobileScanningBinding) binding).tvMaxIndexMobile.setText("Table Index (" + totalFrequencies + " Total)");
    }

    private void logScanHeader(byte[] data) {
        clear();
        currentFrequency = (Byte.toUnsignedInt(data[1]) * 256) + Byte.toUnsignedInt(data[2]) + baseFrequency;
        currentIndex = (((Byte.toUnsignedInt(data[1]) >> 6) & 1) * 256) + Byte.toUnsignedInt(data[3]);
        ((FragmentMobileScanningBinding) binding).tvTableIndexMobile.setText(String.valueOf(currentIndex));
        ((FragmentMobileScanningBinding) binding).tvFrequencyMobile.setText(Converters.getFrequency(currentFrequency));
    }

    private void logScanCoded(byte[] data) {
        int code = Byte.toUnsignedInt(data[3]);
        int signalStrength = Byte.toUnsignedInt(data[4]);
        int mortality = Byte.toUnsignedInt(data[5]);
        scanCoded(code, signalStrength, mortality);
    }

    private void logScanNonCodedFixed(byte format, int period, int signalStrength) {
        int type = Integer.parseInt(Converters.getHexValue(format).replace("E", ""));
        if (period > 0)
            scanNonCodedFixed(period, signalStrength, type);
    }

    private void logGps(byte[] data) {
        String[] coordinates = Converters.getGpsData(data);
        ((FragmentMobileScanningBinding) binding).includeCoordinates.tvLatitude.setText(coordinates[0]);
        ((FragmentMobileScanningBinding) binding).includeCoordinates.tvLongitude.setText(coordinates[1]);
    }

    @Override
    protected void clear() {
        ((FragmentMobileScanningBinding) binding).tvTableIndexMobile.setText("");
        ((FragmentMobileScanningBinding) binding).tvFrequencyMobile.setText("");
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
