package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.DialogsFragment.AudioOptionsDialogFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.Coordinates;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.ScanBaseActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentMobileScanningBinding;

public class MobileScanningFragment extends ScanBaseFragment implements ReceiverCallback {
    private MobileDefaults mobileDefaults;
    private byte[] scanState;
    private boolean isHold; // This can change during scanning
    private boolean isRecord; // This can change during scanning
    private byte[] audioOption = {ValueCodes.AUDIO_ALL_COMMAND, 0, 0};
    private DialogFragment audioOptions;
    private ActivityResultLauncher<Intent> launcher;
    private byte[] tables;
    private boolean deleting = false;

    public MobileScanningFragment(int baseFrequency, int range, MobileDefaults mobileDefaults) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.mobileDefaults = mobileDefaults;
        isHold = isScanning = false;
        initializeLauncher();
    }

    public MobileScanningFragment(int baseFrequency, int range, byte[] data) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.scanState = data;
        isScanning = true;
        initializeLauncher();
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
        ((FragmentMobileScanningBinding) binding).layoutMobileScanning.setOnClickListener(v -> {
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).includeScanOptions.layoutHold.setOnClickListener(v -> {
            setHoldScan();
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).includeScanOptions.layoutRecordData.setOnClickListener(v -> {
            setRecordScan();
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).includeScanOptions.tvStopScan.setOnClickListener(v -> {
            boolean result = TransferBleData.writeStopScan(scanType);
            if (result) {
                Fragment fragment1 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.FIRST_STEP));
                Fragment fragment2 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.SECOND_STEP));
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                if (fragment2 != null) transaction.remove(fragment2);
                if (fragment1 != null)
                    transaction.show(fragment1);
                transaction.commit();
            }
        });
        ((FragmentMobileScanningBinding) binding).includeScanOptions.tvExitReceiver.setOnClickListener(v -> {
            LeServiceConnection.getInstance().getBluetoothLeService().disconnect();
        });
        ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setOnClickListener(v -> {
            if (deleting) {
                byte[] b = new byte[] {ValueCodes.DELETE_FREQUENCY_COMMAND, (byte) (currentIndex / 256), (byte) (currentIndex % 256)};
                boolean result = TransferBleData.writeScanning(b);
                if (result) {
                    setVisibility(ValueCodes.CANCELLED);
                    showAlertDialog(getString(R.string.lbl_vhf_mobile_frequency_deleted));
                }
            } else {
                deleting = true;
                setVisibility(ValueCodes.DELETE);
            }
        });
        ((FragmentMobileScanningBinding) binding).tvAddFrequency.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
            intent.putExtra(ValueCodes.TITLE, getString(R.string.btn_vhf_mobile_add_frequency_scan));
            intent.putExtra(ValueCodes.POSITION, -1);
            intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
            intent.putExtra(ValueCodes.RANGE, range);
            launcher.launch(intent);
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).tvMergeTable.setOnClickListener(v -> {
            if (tables == null) {
                TransferBleData.readTables();
            } else {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(this)
                            .add(R.id.fcv_activity_fragment, new TablesScanFragment(tables, ValueCodes.MOBILE_SCAN_COMMAND), String.valueOf(ValueCodes.THIRD_STEP))
                            .addToBackStack(String.valueOf(ValueCodes.SECOND_STEP))
                            .commit();
                }
            }
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).imgDecrease.setOnClickListener(v -> {
            if (currentFrequency > baseFrequency)
                TransferBleData.writeScanning(new byte[] {ValueCodes.FREQUENCY_DOWN_COMMAND});
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).imgIncrease.setOnClickListener(v -> {
            if (currentFrequency < frequencyRange)
                TransferBleData.writeScanning(new byte[] {ValueCodes.FREQUENCY_UP_COMMAND});
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).imgLeft.setOnClickListener(v -> {
            TransferBleData.writeScanning(new byte[]{ValueCodes.INDEX_PREVIOUS_COMMAND});
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).imgRight.setOnClickListener(v -> {
            TransferBleData.writeScanning(new byte[] {ValueCodes.INDEX_NEXT_COMMAND});
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
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
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        ((FragmentMobileScanningBinding) binding).tvViewDetectionMobile.setOnClickListener(v -> {
            showDetectionAlertDialog();
            if (deleting) {
                deleting = false;
                setVisibility(ValueCodes.CANCELLED);
            }
        });
        if (isScanning) {
            setNotificationLogScanning();
            initializeScanning();
        } else {
            setNotificationLog();
            setStartScan();
        }

        getParentFragmentManager().setFragmentResultListener(ValueCodes.TABLE, this, (requestKey, result) -> {
            if (!isAdded() || getView() == null) return;
            boolean tableMerged = result.getBoolean(ValueCodes.VALUE, false);
            if (tableMerged) {
                isHold = false;
                setVisibility(ValueCodes.STOP_HOLD);
                showAlertDialog(getString(R.string.lbl_vhf_mobile_tables_merged));
            }
        });
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (ValueCodes.CANCELLED == result.getResultCode())
                        return;
                    int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    if (ValueCodes.RESULT_OK == result.getResultCode()) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isAdded() && getView() != null) {
                                setNewFrequency(value);
                            }
                        }, ValueCodes.WAITING_PERIOD);
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
                } else if (packet[0] == ValueCodes.TABLES_COMMAND) {
                    downloadTables(packet);
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
            ((FragmentMobileScanningBinding) binding).includeScanOptions.tvHold.setText(R.string.btn_vhf_mobile_release);
            ((FragmentMobileScanningBinding) binding).includeScanOptions.tvHold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_unlock, 0, 0);
            ((FragmentMobileScanningBinding) binding).includeScanOptions.imgHold.setVisibility(View.VISIBLE);
            ((FragmentMobileScanningBinding) binding).includeAudioOption.tvEditAudio.setTextColor(ContextCompat.getColor(requireContext(), R.color.limed_spruce));
            ((FragmentMobileScanningBinding) binding).includeAudioOption.tvEditAudio.setEnabled(true);
            ((FragmentMobileScanningBinding) binding).imgDecrease.setAlpha(1.0f);
            ((FragmentMobileScanningBinding) binding).imgIncrease.setAlpha(1.0f);
            ((FragmentMobileScanningBinding) binding).imgLeft.setAlpha(1.0f);
            ((FragmentMobileScanningBinding) binding).imgRight.setAlpha(1.0f);
            ((FragmentMobileScanningBinding) binding).imgDecrease.setEnabled(true);
            ((FragmentMobileScanningBinding) binding).imgIncrease.setEnabled(true);
            ((FragmentMobileScanningBinding) binding).imgLeft.setEnabled(true);
            ((FragmentMobileScanningBinding) binding).imgRight.setEnabled(true);
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setAlpha(1.0f);
            ((FragmentMobileScanningBinding) binding).tvAddFrequency.setAlpha(1.0f);
            ((FragmentMobileScanningBinding) binding).tvMergeTable.setAlpha(1.0f);
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setEnabled(true);
            ((FragmentMobileScanningBinding) binding).tvAddFrequency.setEnabled(true);
            ((FragmentMobileScanningBinding) binding).tvMergeTable.setEnabled(true);
        } else if (view == ValueCodes.STOP_HOLD) {
            ((FragmentMobileScanningBinding) binding).includeScanOptions.tvHold.setText(R.string.btn_vhf_mobile_hold);
            ((FragmentMobileScanningBinding) binding).includeScanOptions.tvHold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_lock, 0, 0);
            ((FragmentMobileScanningBinding) binding).includeScanOptions.imgHold.setVisibility(View.GONE);
            ((FragmentMobileScanningBinding) binding).includeAudioOption.tvEditAudio.setTextColor(ContextCompat.getColor(requireContext(), R.color.ghost));
            ((FragmentMobileScanningBinding) binding).includeAudioOption.tvEditAudio.setEnabled(false);
            ((FragmentMobileScanningBinding) binding).imgDecrease.setAlpha(0.6f);
            ((FragmentMobileScanningBinding) binding).imgIncrease.setAlpha(0.6f);
            ((FragmentMobileScanningBinding) binding).imgLeft.setAlpha(0.6f);
            ((FragmentMobileScanningBinding) binding).imgRight.setAlpha(0.6f);
            ((FragmentMobileScanningBinding) binding).imgDecrease.setEnabled(false);
            ((FragmentMobileScanningBinding) binding).imgIncrease.setEnabled(false);
            ((FragmentMobileScanningBinding) binding).imgLeft.setEnabled(false);
            ((FragmentMobileScanningBinding) binding).imgRight.setEnabled(false);
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setAlpha(0.6f);
            ((FragmentMobileScanningBinding) binding).tvAddFrequency.setAlpha(0.6f);
            ((FragmentMobileScanningBinding) binding).tvMergeTable.setAlpha(0.6f);
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setEnabled(false);
            ((FragmentMobileScanningBinding) binding).tvAddFrequency.setEnabled(false);
            ((FragmentMobileScanningBinding) binding).tvMergeTable.setEnabled(false);
        } else if (view == ValueCodes.START_RECORD) {
            ((FragmentMobileScanningBinding) binding).includeScanOptions.imgRecordData.setVisibility(View.VISIBLE);
            ((FragmentMobileScanningBinding) binding).includeScanOptions.tvRecordData.setText(R.string.btn_vhf_mobile_stop_recording);
        } else if (view == ValueCodes.STOP_RECORD) {
            ((FragmentMobileScanningBinding) binding).includeScanOptions.imgRecordData.setVisibility(View.GONE);
            ((FragmentMobileScanningBinding) binding).includeScanOptions.tvRecordData.setText(R.string.btn_vhf_manual_record_data);
        } else if (view == ValueCodes.GPS_OFF) {
            ((FragmentMobileScanningBinding) binding).includeCoordinates.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_off);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
        } else if (view == ValueCodes.GPS_SEARCHING) {
            ((FragmentMobileScanningBinding) binding).includeCoordinates.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_searching);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.tvLatitude.setText("");
            ((FragmentMobileScanningBinding) binding).includeCoordinates.tvLongitude.setText("");
        } else if (view == ValueCodes.GPS_FAILED) {
            ((FragmentMobileScanningBinding) binding).includeCoordinates.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_failed);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.GONE);
        } else if (view == ValueCodes.GPS_VALID) {
            ((FragmentMobileScanningBinding) binding).includeCoordinates.tvGpsState.setText(R.string.lbl_vhf_mobile_gps_valid);
            ((FragmentMobileScanningBinding) binding).includeCoordinates.layoutCoordinates.setVisibility(View.VISIBLE);
        } else if (view == ValueCodes.DELETE) {
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setText(R.string.btn_vhf_mobile_confirm_delete_frequency_scan);
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_50));
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_cancel));
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_delete_frequency_light, 0, 0);
        } else if (view == ValueCodes.CANCELLED) {
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setText(R.string.btn_vhf_mobile_delete_frequency_scan);
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.scan_option));
            ((FragmentMobileScanningBinding) binding).tvDeleteFrequency.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_delete_frequency, 0, 0);
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
        setVisibility(mobileDefaults.gpsOn ? (scanState[14] == ValueCodes.GPS_OFF ? ValueCodes.GPS_SEARCHING : scanState[14]) : ValueCodes.GPS_OFF);
        scanState(scanState);
    }

    private void setHoldScan() {
        boolean result = TransferBleData.setHold(isHold);
        if (result) {
            isHold = !isHold;
            setVisibility(isHold ? ValueCodes.START_HOLD : ValueCodes.STOP_HOLD);
            if (getActivity() instanceof ScanBaseActivity) {
                ((ScanBaseActivity) getActivity()).setTitle(isHold ? getString(R.string.title_vhf_mobile_holding) : getString(R.string.lbl_vhf_start_scan_scanning));
            }
        }
    }

    private void setRecordScan() {
        boolean result = TransferBleData.writeRecord(false, new byte[] {isRecord ? ValueCodes.STOP_RECORD_COMMAND : ValueCodes.RECORD_COMMAND});
        if (result) {
            isRecord = !isRecord;
            setVisibility(isRecord ? ValueCodes.START_RECORD : ValueCodes.STOP_RECORD);
        }
    }

    private void setNewFrequency(int newFrequency) {
        byte[] b = new byte[] {ValueCodes.ADD_FREQUENCY_COMMAND, (byte) ((newFrequency - baseFrequency) / 256), (byte) ((newFrequency - baseFrequency) % 256)};
        boolean result = TransferBleData.writeScanning(b);
        if (result)
            showAlertDialog(getString(R.string.lbl_vhf_tables_added));
    }

    private void setAudio() {
        byte[] b;
        if (audioOption[0] == ValueCodes.AUDIO_SINGLE_COMMAND)
            b = new byte[] {audioOption[0], audioOption[1], audioOption[2]};
        else
            b = new byte[] {audioOption[0], audioOption[2]};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
            String audioDescription = getString(R.string.lbl_vhf_manual_option_all);
            if (audioOption[0] == ValueCodes.AUDIO_SINGLE_COMMAND)
                audioDescription = "Single (" + Byte.toUnsignedInt(audioOption[1]) + ")";
            else if (audioOption[0] == ValueCodes.AUDIO_BACKGROUND_COMMAND)
                audioDescription = getString(R.string.lbl_vhf_manual_option_background);
            ((FragmentMobileScanningBinding) binding).includeAudioOption.tvIdAudio.setText(audioDescription);
        }
    }

    private void downloadTables(byte[] data) {
        tables = data;
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new TablesScanFragment(tables, ValueCodes.MOBILE_SCAN_COMMAND), String.valueOf(ValueCodes.FOURTH_STEP))
                    .addToBackStack(String.valueOf(ValueCodes.THIRD_STEP))
                    .commit();
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
                ((FragmentMobileScanningBinding) binding).includeScanDetails.includeNoDetections.getRoot().setVisibility(View.GONE);
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
                    if (period > 0) {
                        scanNonCodedVariable(period, signalStrength);
                        ((FragmentMobileScanningBinding) binding).includeScanDetails.includeNoDetections.getRoot().setVisibility(View.GONE);
                    }
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
        if (period > 0) {
            scanNonCodedFixed(period, signalStrength, type);
            ((FragmentMobileScanningBinding) binding).includeScanDetails.includeNoDetections.getRoot().setVisibility(View.GONE);
        }
    }

    private void logGps(byte[] data) {
        Coordinates coordinates = Converters.getGpsData(data);
        ((FragmentMobileScanningBinding) binding).includeCoordinates.tvLatitude.setText(coordinates.latitude);
        ((FragmentMobileScanningBinding) binding).includeCoordinates.tvLongitude.setText(coordinates.longitude);
    }

    @Override
    protected void clear() {
        ((FragmentMobileScanningBinding) binding).tvTableIndexMobile.setText("");
        ((FragmentMobileScanningBinding) binding).tvFrequencyMobile.setText("");
        super.clear();
        ((FragmentMobileScanningBinding) binding).includeScanDetails.includeNoDetections.getRoot().setVisibility(View.VISIBLE);
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
