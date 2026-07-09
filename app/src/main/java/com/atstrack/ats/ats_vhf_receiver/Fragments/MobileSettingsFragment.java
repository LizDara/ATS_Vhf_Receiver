package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.MobileDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.ScanBaseActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDefaultsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MobileSettingsFragment extends Fragment implements ReceiverCallback {
    @BindView(R.id.tv_scan_rate_seconds_mobile)
    TextView tv_scan_rate_seconds_mobile;
    @BindView(R.id.tv_table_number_mobile)
    TextView tv_table_number_mobile;
    @BindView(R.id.sw_gps)
    SwitchCompat sw_gps;
    @BindView(R.id.sw_mobile_auto_record)
    SwitchCompat sw_mobile_auto_record;
    @BindView(R.id.btn_start_mobile)
    Button btn_start_mobile;

    private Unbinder unbinder;
    private final int baseFrequency;
    private final int range;
    private MobileDefaults mobileDefaults;
    private boolean goEditDefault;
    private boolean isReadyToTemporary;
    private ActivityResultLauncher<Intent> launcher;

    public MobileSettingsFragment(int baseFrequency, int range) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.goEditDefault = this.isReadyToTemporary = false;
        initializeLauncher();
    }

    public MobileSettingsFragment(int baseFrequency, int range, byte[] data) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.goEditDefault = this.isReadyToTemporary = false;
        mobileDefaults = new MobileDefaults(data);
        initializeLauncher();
    }

    @OnClick(R.id.layout_table_number_mobile)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(requireContext(), ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.tableNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_scan_rate_seconds_mobile)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(requireContext(), ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_MOBILE_CODE);
        intent.putExtra(ValueCodes.VALUE, (int)(mobileDefaults.scanRate * 10));
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.sw_gps)
    public void onCheckedChangedGps(CompoundButton button, boolean isChecked) {
        if (isReadyToTemporary) {
            sw_gps.setEnabled(false);
            setTemporary(ValueCodes.GPS_CODE);
        }
    }

    @OnCheckedChanged(R.id.sw_mobile_auto_record)
    public void onCheckedChangedAutoRecord(CompoundButton button, boolean isChecked) {
        if (isReadyToTemporary) {
            sw_mobile_auto_record.setEnabled(false);
            setTemporary(ValueCodes.AUTO_RECORD_CODE);
        }
    }

    @OnClick(R.id.tv_edit_mobile_default)
    public void onClickMobileDefault(View v) {
        goEditDefault = true;
        Intent intent = new Intent(requireContext(), MobileDefaultsActivity.class);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.originalBytes);
        startActivity(intent);
    }

    @OnClick(R.id.btn_start_mobile)
    public void onClickStartMobile(View v) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new MobileScanningFragment(baseFrequency, range, mobileDefaults), String.valueOf(ValueCodes.SECOND_STEP))
                    .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        if (mobileDefaults != null)
            downloadMobileDefault();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initialize();
            Log.i("MOBILE SETTINGS", "NO HIDDEN MOBILE DEFAULTS");
            if (mobileDefaults == null) {
                Log.i("MOBILE SETTINGS", "BEFORE READ DEFAULTS");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded() && getView() != null)
                        TransferBleData.readDefaults(true);
                    Log.i("MOBILE SETTINGS", "AFTER READ DEFAULTS");
                }, ValueCodes.WAITING_PERIOD);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (goEditDefault && mobileDefaults != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded() && getView() != null)
                    TransferBleData.readDefaults(true);
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
                if (packet[0] == ValueCodes.MOBILE_DEFAULTS_COMMAND) {
                    goEditDefault = false;
                    mobileDefaults = new MobileDefaults(packet);
                    downloadMobileDefault();
                }
            }
        });
    }

    private void downloadMobileDefault() {
        if (mobileDefaults.tableNumber == 0) { // There are no tables with frequencies to scan
            tv_table_number_mobile.setText(R.string.lb_none);
            btn_start_mobile.setEnabled(false);
            btn_start_mobile.setAlpha((float) 0.6);
        } else { // Shows the table to be scanned
            tv_table_number_mobile.setText(String.valueOf(mobileDefaults.tableNumber));
            btn_start_mobile.setEnabled(true);
            btn_start_mobile.setAlpha((float) 1);
        }
        tv_scan_rate_seconds_mobile.setText(String.valueOf(mobileDefaults.scanRate));
        sw_gps.setChecked(mobileDefaults.gpsOn);
        sw_mobile_auto_record.setChecked(mobileDefaults.autoRecordOn);
        isReadyToTemporary = true;
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (ValueCodes.CANCELLED == result.getResultCode())
                        return;
                    int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                        tv_table_number_mobile.setText(String.valueOf(value));
                        setTemporary(ValueCodes.TABLE_NUMBER_CODE);
                    } else if (ValueCodes.SCAN_RATE_MOBILE_CODE == result.getResultCode()) { // Gets the modified scan rate
                        tv_scan_rate_seconds_mobile.setText(String.valueOf(value * 0.1));
                        setTemporary(ValueCodes.SCAN_RATE_MOBILE_CODE);
                    }
                });
    }

    private void setTemporary(int type) {
        int info = (mobileDefaults.gpsOn ? 1 : 0) << 7;
        info = info | ((mobileDefaults.autoRecordOn ? 1 : 0) << 6);
        byte[] b = new byte[]{(byte) 0x6F, (byte) mobileDefaults.tableNumber, (byte) info, (byte) ((int) (mobileDefaults.scanRate * 10))};
        switch (type) {
            case ValueCodes.TABLE_NUMBER_CODE:
                b[1] = (byte) Integer.parseInt(tv_table_number_mobile.getText().toString());
                mobileDefaults.tableNumber = Integer.parseInt(tv_table_number_mobile.getText().toString());
                break;
            case ValueCodes.SCAN_RATE_MOBILE_CODE:
                b[3] = (byte) (Float.parseFloat(tv_scan_rate_seconds_mobile.getText().toString()) * 10);
                mobileDefaults.scanRate = Double.parseDouble(tv_scan_rate_seconds_mobile.getText().toString());
                break;
            case ValueCodes.GPS_CODE:
                b[2] = sw_gps.isChecked() ? (byte) (Byte.toUnsignedInt(b[2]) | 0x80) : (byte) (Byte.toUnsignedInt(b[2]) & 0x7F);
                mobileDefaults.gpsOn = sw_gps.isChecked();
                break;
            case ValueCodes.AUTO_RECORD_CODE:
                b[2] = sw_mobile_auto_record.isChecked() ? (byte) (Byte.toUnsignedInt(b[2]) | 0x40) : (byte) (Byte.toUnsignedInt(b[2]) & 0xBF);
                mobileDefaults.autoRecordOn = sw_mobile_auto_record.isChecked();
                break;
        }
        boolean result = TransferBleData.writeDefaults(true, b);
        if (!result) {
            mobileDefaults = new MobileDefaults(mobileDefaults.originalBytes);
            downloadMobileDefault();
        }
        sw_gps.setEnabled(true);
        sw_mobile_auto_record.setEnabled(true);
    }
}
