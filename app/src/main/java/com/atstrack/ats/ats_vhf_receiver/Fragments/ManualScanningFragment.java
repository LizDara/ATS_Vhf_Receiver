package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentManualScanningBinding;

public class ManualScanningFragment extends ScanBaseFragment implements ReceiverCallback {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        scanType = ValueCodes.MANUAL_SCAN_COMMAND;
        binding = FragmentManualScanningBinding.inflate(inflater, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setOnClickListener(v -> {
            setVisibility(ValueCodes.START_RECORD);
            setRecord();
        });
        ((FragmentManualScanningBinding) binding).btnEditFrequency.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
            intent.putExtra(ValueCodes.TITLE, getString(R.string.title_vhf_manual_change_frequency));
            intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
            intent.putExtra(ValueCodes.RANGE, range);
            launcher.launch(intent);
        });
        ((FragmentManualScanningBinding) binding).imgMinus.setOnClickListener(v -> {
            currentFrequency = Converters.getFrequencyNumber(((FragmentManualScanningBinding) binding).tvFrequencyScanManual.getText().toString()) - 1;
            setDecreaseOrIncrease(true);
        });
        ((FragmentManualScanningBinding) binding).imgPlus.setOnClickListener(v -> {
            currentFrequency = Converters.getFrequencyNumber(((FragmentManualScanningBinding) binding).tvFrequencyScanManual.getText().toString()) + 1;
            setDecreaseOrIncrease(false);
        });
        ((FragmentManualScanningBinding) binding).includeAudioOption.tvEditAudio.setOnClickListener(v -> {
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
        ((FragmentManualScanningBinding) binding).tvViewDetectionManual.setOnClickListener(v -> showDetectionAlertDialog());
        ((FragmentManualScanningBinding) binding).swGpsScanning.setOnCheckedChangeListener((compoundButton, b) -> {
            if (enableGpsScanning)
                setGps();
        });
        if (isScanning) {
            initializeScanning();
            setNotificationLogScanning();
        } else {
            setNotificationLog();
            setStartScan();
        }
    }

    @Override
    protected void updateVisibility(TextView tv_code_period, TextView tv_mortality_pulse_rate) {
        super.updateVisibility(tv_code_period, tv_mortality_pulse_rate);
        int visibility = detectionType == ValueCodes.CODED ? View.GONE : View.VISIBLE;
        ((FragmentManualScanningBinding) binding).includeAudioOption.layoutAudio.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        ((FragmentManualScanningBinding) binding).tvViewDetectionManual.setVisibility(visibility);
    }

    private void initializeScanning() {
        ((FragmentManualScanningBinding) binding).swGpsScanning.setChecked((Byte.toUnsignedInt(scanStateScanning[15]) >> 7 & 1) == 1);
        setVisibility(((FragmentManualScanningBinding) binding).swGpsScanning.isChecked() ? ValueCodes.GPS_SEARCHING : ValueCodes.GPS_OFF);
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
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_off, 0, 0, 0);
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_off);
            ((FragmentManualScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
        } else if (view == ValueCodes.GPS_SEARCHING) {
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_searching, 0, 0, 0);
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_searching);
            ((FragmentManualScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
            ((FragmentManualScanningBinding) binding).includeCoordinates.tvLatitude.setText("");
            ((FragmentManualScanningBinding) binding).includeCoordinates.tvLongitude.setText("");
        } else if (view == ValueCodes.GPS_FAILED) {
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_failed, 0, 0, 0);
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_failed);
            ((FragmentManualScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
        } else if (view == ValueCodes.GPS_VALID) {
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_valid, 0, 0, 0);
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_valid);
            ((FragmentManualScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.VISIBLE);
        } else if (view == ValueCodes.START_RECORD) {
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setText(R.string.lbl_vhf_manual_saving_targets);
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setAlpha((float) 0.6);
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setEnabled(false);
        } else if (view == ValueCodes.STOP_RECORD) {
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setText(R.string.btn_vhf_manual_record_data);
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setAlpha(1);
            ((FragmentManualScanningBinding) binding).includeGpsRecordOptions.btnRecordData.setEnabled(true);
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
            ((FragmentManualScanningBinding) binding).tvFrequencyScanManual.setText(Converters.getFrequency(currentFrequency));
            ((FragmentManualScanningBinding) binding).swGpsScanning.setChecked(gpsOn);
            setVisibility(((FragmentManualScanningBinding) binding).swGpsScanning.isChecked() ? ValueCodes.GPS_SEARCHING : ValueCodes.GPS_OFF);
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
                    ((FragmentManualScanningBinding) binding).imgMinus.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_decrease_light));
                    ((FragmentManualScanningBinding) binding).imgMinus.setEnabled(false);
                } else if (currentFrequency == frequencyRange - 1) {
                    ((FragmentManualScanningBinding) binding).imgPlus.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_increase));
                    ((FragmentManualScanningBinding) binding).imgPlus.setEnabled(true);
                }
            } else {
                if (currentFrequency == baseFrequency + 1) {
                    ((FragmentManualScanningBinding) binding).imgMinus.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_decrease));
                    ((FragmentManualScanningBinding) binding).imgMinus.setEnabled(true);
                } else if (currentFrequency == frequencyRange) {
                    ((FragmentManualScanningBinding) binding).imgPlus.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_increase_light));
                    ((FragmentManualScanningBinding) binding).imgPlus.setEnabled(false);
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
            ((FragmentManualScanningBinding) binding).includeAudioOption.tvIdAudio.setText(audioDescription);
        }
    }

    private void setGps() {
        boolean result = TransferBleData.writeGps(((FragmentManualScanningBinding) binding).swGpsScanning.isChecked());
        if (result) {
            setVisibility(((FragmentManualScanningBinding) binding).swGpsScanning.isChecked() ? ValueCodes.GPS_SEARCHING : ValueCodes.GPS_OFF);
        } else {
            enableGpsScanning = false;
            ((FragmentManualScanningBinding) binding).swGpsScanning.setChecked(!((FragmentManualScanningBinding) binding).swGpsScanning.isChecked());
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
                else if (detectionType == ValueCodes.VARIABLE) {
                    if (period > 0)
                        scanNonCodedVariable(period, signalStrength);
                }
                break;
        }
    }

    private void scanState(byte[] data) {
        int frequency = baseFrequency + ((Byte.toUnsignedInt(data[10]) * 256) + Byte.toUnsignedInt(data[11]));
        ((FragmentManualScanningBinding) binding).tvFrequencyScanManual.setText(Converters.getFrequency(frequency));
        detectionType = data[18];
        scanDetailAdapter = new ScanDetailAdapter(requireContext(), detectionType == ValueCodes.CODED);
        ((FragmentManualScanningBinding) binding).includeScanDetails.includeRecyclerView.rvItem.setAdapter(scanDetailAdapter);
        ((FragmentManualScanningBinding) binding).includeScanDetails.includeRecyclerView.rvItem.setLayoutManager(new LinearLayoutManager(requireContext()));
        updateVisibility(((FragmentManualScanningBinding) binding).includeScanDetails.tvCodePeriod, ((FragmentManualScanningBinding) binding).includeScanDetails.tvMortalityPulseRate);

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
        ((FragmentManualScanningBinding) binding).tvFrequencyScanManual.setText(Converters.getFrequency(frequency));
    }

    private void logScanCoded(byte[] data) {
        int signalStrength = Byte.toUnsignedInt(data[3]);
        int code = Byte.toUnsignedInt(data[4]);
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
        ((FragmentManualScanningBinding) binding).includeCoordinates.tvLatitude.setText(coordinates[0]);
        ((FragmentManualScanningBinding) binding).includeCoordinates.tvLongitude.setText(coordinates[1]);
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
