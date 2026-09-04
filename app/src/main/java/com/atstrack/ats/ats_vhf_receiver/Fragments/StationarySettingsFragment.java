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
import com.atstrack.ats.ats_vhf_receiver.Models.StationaryDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ScanBaseActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.StationaryDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentStationarySettingsBinding;

public class StationarySettingsFragment extends Fragment implements ReceiverCallback {
    private FragmentStationarySettingsBinding binding = null;
    private final int baseFrequency;
    private StationaryDefaults stationaryDefaults;
    private boolean goEditDefault;

    public StationarySettingsFragment(int baseFrequency) {
        this.baseFrequency = baseFrequency;
        this.goEditDefault = false;
    }

    public StationarySettingsFragment(int baseFrequency, byte[] data) {
        this.baseFrequency = baseFrequency;
        this.goEditDefault = false;
        stationaryDefaults = new StationaryDefaults(baseFrequency, data);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStationarySettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvEditStationaryDefault.setOnClickListener(v -> {
            goEditDefault = true;
            Intent intent = new Intent(requireContext(), StationaryDefaultsActivity.class);
            intent.putExtra(ValueCodes.VALUE, stationaryDefaults.originalBytes);
            startActivity(intent);
        });
        binding.btnStartStationary.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment, new StationaryScanningFragment(baseFrequency, stationaryDefaults), String.valueOf(ValueCodes.SECOND_STEP))
                        .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                        .commit();
            }
        });
        binding.includeStationarySettings.imgTimeoutNext.setVisibility(View.GONE);
        binding.includeStationarySettings.imgScanTimeNext.setVisibility(View.GONE);
        binding.includeStationarySettings.imgStoreRateNext.setVisibility(View.GONE);
        binding.includeStationarySettings.imgScanTablesNext.setVisibility(View.GONE);
        binding.includeStationarySettings.imgAntennasNext.setVisibility(View.GONE);
        binding.includeStationarySettings.imgReferenceFrequencyNext.setVisibility(View.GONE);
        binding.includeStationarySettings.imgReferenceStoreRateNext.setVisibility(View.GONE);
        initialize();
        if (stationaryDefaults != null)
            downloadStationaryDefault();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initialize();
            if (stationaryDefaults == null) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded() && getView() != null)
                        TransferBleData.readDefaults(false);
                }, ValueCodes.WAITING_PERIOD);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (goEditDefault && stationaryDefaults != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded() && getView() != null)
                    TransferBleData.readDefaults(false);
                }, ValueCodes.WAITING_PERIOD);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initialize() {
        if (getActivity() instanceof ScanBaseActivity) {
            ((ScanBaseActivity) getActivity()).setScanViews(false);
        }
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                if (packet[0] == ValueCodes.STATIONARY_DEFAULTS_COMMAND) {
                    goEditDefault = false;
                    stationaryDefaults = new StationaryDefaults(baseFrequency, packet);
                    downloadStationaryDefault();
                }
            }
        });
    }

    private void downloadStationaryDefault() {
        binding.includeStationarySettings.layoutExternalReferenceDefault.setVisibility(View.GONE);
        binding.includeStationarySettings.layoutExternalReferenceScan.setVisibility(View.VISIBLE);
        String tables = "";
        if (stationaryDefaults.firstTableNumber != 0 && stationaryDefaults.firstTableNumber != Byte.toUnsignedInt(ValueCodes.NULL))
            tables += stationaryDefaults.firstTableNumber;
        if (stationaryDefaults.secondTableNumber != 0 && stationaryDefaults.secondTableNumber != Byte.toUnsignedInt(ValueCodes.NULL))
            tables += ", " + stationaryDefaults.secondTableNumber;
        if (stationaryDefaults.thirdTableNumber != 0 && stationaryDefaults.thirdTableNumber != Byte.toUnsignedInt(ValueCodes.NULL))
            tables += ", " + stationaryDefaults.thirdTableNumber;
        binding.includeStationarySettings.tvFrequencyTableNumberStationary.setText(tables.isEmpty() ? getString(R.string.lbl_vhf_manual_option_none) : tables);
        binding.includeStationarySettings.tvNumberOfAntennasStationary.setText((stationaryDefaults.antennaNumber == 0) ? getString(R.string.lbl_vhf_manual_option_none) : String.valueOf(stationaryDefaults.antennaNumber));
        binding.includeStationarySettings.tvStationaryExternalDataTransfer.setText(stationaryDefaults.dataTransferOn ? "On" : "Off");
        binding.includeStationarySettings.tvScanRateSecondsStationary.setText(String.valueOf(stationaryDefaults.scanRate));
        binding.includeStationarySettings.tvScanTimeoutSecondsStationary.setText(String.valueOf(stationaryDefaults.scanTimeout));
        binding.includeStationarySettings.tvStoreRateMinutesStationary.setText(stationaryDefaults.storeRate == 0 ? getString(R.string.lbl_vhf_defaults_store_rate_continuous) : String.valueOf(stationaryDefaults.storeRate));
        binding.includeStationarySettings.tvFrequencyReferenceStationary.setText((stationaryDefaults.referenceFrequencyOn) ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : getString(R.string.lbl_vhf_defaults_stationary_no_reference));
        binding.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText((stationaryDefaults.referenceFrequencyOn) ? String.valueOf(stationaryDefaults.referenceStoreRate) : getString(R.string.lbl_vhf_defaults_stationary_no_reference));
        binding.includeStationarySettings.tvStationaryReferenceFrequency.setText(stationaryDefaults.referenceFrequencyOn ? "On" : "Off");
    }
}
