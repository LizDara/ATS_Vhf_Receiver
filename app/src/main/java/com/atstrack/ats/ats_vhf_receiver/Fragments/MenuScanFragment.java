package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ManualScanActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.MobileScanActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.StationaryScanActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentMenuScanBinding;

public class MenuScanFragment extends Fragment implements ReceiverCallback {
    private FragmentMenuScanBinding binding = null;
    private boolean isDetectionFilterEmpty;
    private boolean areTablesEmpty;
    private boolean isDefaultEmpty;
    private byte[] detectionData;
    private byte[] tablesData;
    private byte[] defaultData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnStartManualScan.setOnClickListener(v -> {
            if (isDetectionFilterEmpty) {
                showWarningMessage(ValueCodes.DETECTION_FILTER_COMMAND, detectionData);
            } else {
                Intent intent = new Intent(requireContext(), ManualScanActivity.class);
                startActivity(intent);
            }
        });
        binding.btnStartMobileScan.setOnClickListener(v -> TransferBleData.readDefaults(true));
        binding.btnStartStationaryScan.setOnClickListener(v -> TransferBleData.readDefaults(false));
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && getView() != null) {
                if (isDetectionFilterEmpty) {
                    TransferBleData.readDetectionFilter();
                } else if (areTablesEmpty) {
                    TransferBleData.readTables();
                }
            }
        }, ValueCodes.WAITING_PERIOD);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                switch (packet[0]) {
                    case ValueCodes.DETECTION_FILTER_COMMAND:
                        downloadDetectionType(packet);
                        break;
                    case ValueCodes.TABLES_COMMAND:
                        downloadTables(packet);
                        break;
                    case ValueCodes.MOBILE_DEFAULTS_COMMAND:
                        downloadMobileDefaults(packet);
                        break;
                    case ValueCodes.STATIONARY_DEFAULTS_COMMAND:
                        downloadStationaryDefaults(packet);
                        break;
                }
            }
        });
    }

    private void downloadDetectionType(byte[] data) {
        detectionData = data;
        isDetectionFilterEmpty = false;
        if (data[1] != ValueCodes.CODED) {
            isDetectionFilterEmpty = data[2] == 0 && data[3] == 0 && data[4] == 0 && data[5] == 0 && data[6] == 0 && data[7] == 0
                    && data[8] == 0 && data[9] == 0 && data[10] == 0 && data[11] == 0;
        }
        TransferBleData.readTables();
    }

    /**
     * With the received packet, gets the number of frequencies from each table and display on the screen.
     * @param data The received packet.
     */
    private void downloadTables(byte[] data) {
        tablesData = data;
        areTablesEmpty = data[1] == 0 && data[2] == 0 && data[3] == 0 && data[4] == 0 && data[5] == 0 && data[6] == 0
                && data[7] == 0 && data[8] == 0 && data[9] == 0 && data[10] == 0 && data[11] == 0 && data[12] == 0;
    }

    private void downloadMobileDefaults(byte[] data) {
        defaultData = data;
        isDefaultEmpty = Converters.isDefaultEmpty(data);
        if (isDetectionFilterEmpty) {
            showWarningMessage(ValueCodes.DETECTION_FILTER_COMMAND, detectionData);
        } else if (areTablesEmpty) {
            showWarningMessage(ValueCodes.TABLES_COMMAND, tablesData);
        } else if (isDefaultEmpty) {
            showWarningMessage(ValueCodes.MOBILE_DEFAULTS_COMMAND, defaultData);
        } else {
            Intent intent = new Intent(requireContext(), MobileScanActivity.class);
            intent.putExtra(ValueCodes.VALUE, data);
            startActivity(intent);
        }
    }

    private void downloadStationaryDefaults(byte[] data) {
        defaultData = data;
        isDefaultEmpty = Converters.isDefaultEmpty(data);
        if (isDetectionFilterEmpty) {
            showWarningMessage(ValueCodes.DETECTION_FILTER_COMMAND, detectionData);
        } else if (areTablesEmpty) {
            showWarningMessage(ValueCodes.TABLES_COMMAND, tablesData);
        } else if (isDefaultEmpty) {
            showWarningMessage(ValueCodes.STATIONARY_DEFAULTS_COMMAND, defaultData);
        } else {
            Intent intent = new Intent(requireContext(), StationaryScanActivity.class);
            intent.putExtra(ValueCodes.VALUE, data);
            startActivity(intent);
        }
    }

    private void showWarningMessage(int parameter, byte[] data) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new WarningMessageFragment(parameter, data))
                    .addToBackStack(null)
                    .commit();
        }
    }
}
