package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class StationarySettingsFragment extends Fragment implements ReceiverCallback {
    @BindView(R.id.tv_scan_rate_seconds_stationary)
    TextView tv_scan_rate_seconds_stationary;
    @BindView(R.id.tv_frequency_table_number_stationary)
    TextView tv_frequency_table_number_stationary;
    @BindView(R.id.tv_store_rate_minutes_stationary)
    TextView tv_store_rate_minutes_stationary;
    @BindView(R.id.tv_stationary_external_data_transfer)
    TextView tv_stationary_external_data_transfer;
    @BindView(R.id.tv_number_of_antennas_stationary)
    TextView tv_number_of_antennas_stationary;
    @BindView(R.id.tv_scan_timeout_seconds_stationary)
    TextView tv_scan_timeout_seconds_stationary;
    @BindView(R.id.tv_stationary_reference_frequency)
    TextView tv_stationary_reference_frequency;
    @BindView(R.id.tv_frequency_reference_stationary)
    TextView tv_frequency_reference_stationary;
    @BindView(R.id.tv_reference_frequency_store_rate_stationary)
    TextView tv_reference_frequency_store_rate_stationary;
    @BindView(R.id.layout_external_reference_default)
    LinearLayout layout_external_reference_default;
    @BindView(R.id.layout_external_reference_scan)
    LinearLayout layout_external_reference_scan;
    @BindView(R.id.img_timeout_next)
    ImageView img_timeout_next;
    @BindView(R.id.img_scan_time_next)
    ImageView img_scan_time_next;
    @BindView(R.id.img_store_rate_next)
    ImageView img_store_rate_next;
    @BindView(R.id.img_scan_tables_next)
    ImageView img_scan_tables_next;
    @BindView(R.id.img_antennas_next)
    ImageView img_antennas_next;
    @BindView(R.id.img_reference_frequency_next)
    ImageView img_reference_frequency_next;
    @BindView(R.id.img_reference_store_rate_next)
    ImageView img_reference_store_rate_next;

    private Unbinder unbinder;
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

    @OnClick(R.id.tv_edit_stationary_default)
    public void onClickStationaryDefault(View v) {
        goEditDefault = true;
        Intent intent = new Intent(requireContext(), StationaryDefaultsActivity.class);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.originalBytes);
        startActivity(intent);
    }

    @OnClick(R.id.btn_start_stationary)
    public void onClickStartStationary(View v) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new StationaryScanningFragment(baseFrequency, stationaryDefaults), String.valueOf(ValueCodes.SECOND_STEP))
                    .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stationary_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        img_timeout_next.setVisibility(View.GONE);
        img_scan_time_next.setVisibility(View.GONE);
        img_store_rate_next.setVisibility(View.GONE);
        img_scan_tables_next.setVisibility(View.GONE);
        img_antennas_next.setVisibility(View.GONE);
        img_reference_frequency_next.setVisibility(View.GONE);
        img_reference_store_rate_next.setVisibility(View.GONE);
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
        if (unbinder != null)
            unbinder.unbind();
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
        layout_external_reference_default.setVisibility(View.GONE);
        layout_external_reference_scan.setVisibility(View.VISIBLE);
        String tables = "";
        if (stationaryDefaults.firstTableNumber != 0 && stationaryDefaults.firstTableNumber != 255)
            tables += stationaryDefaults.firstTableNumber;
        if (stationaryDefaults.secondTableNumber != 0 && stationaryDefaults.secondTableNumber != 255)
            tables += ", " + stationaryDefaults.secondTableNumber;
        if (stationaryDefaults.thirdTableNumber != 0 && stationaryDefaults.thirdTableNumber != 255)
            tables += ", " + stationaryDefaults.thirdTableNumber;
        tv_frequency_table_number_stationary.setText(tables.isEmpty() ? "None" : tables);
        tv_number_of_antennas_stationary.setText((stationaryDefaults.antennaNumber == 0) ? "None" : String.valueOf(stationaryDefaults.antennaNumber));
        tv_stationary_external_data_transfer.setText(stationaryDefaults.dataTransferOn ? "On" : "Off");
        tv_scan_rate_seconds_stationary.setText(String.valueOf(stationaryDefaults.scanRate));
        tv_scan_timeout_seconds_stationary.setText(String.valueOf(stationaryDefaults.scanTimeout));
        tv_store_rate_minutes_stationary.setText(stationaryDefaults.storeRate == 0 ? getString(R.string.lb_continuous_store) : String.valueOf(stationaryDefaults.storeRate));
        tv_frequency_reference_stationary.setText((stationaryDefaults.referenceFrequencyOn) ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : "No Reference Frequency");
        tv_reference_frequency_store_rate_stationary.setText((stationaryDefaults.referenceFrequencyOn) ? String.valueOf(stationaryDefaults.referenceStoreRate) : "No Reference Frequency");
        tv_stationary_reference_frequency.setText(stationaryDefaults.referenceFrequencyOn ? "On" : "Off");
    }
}
