package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.MobileDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.ScanBaseActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentMobileSettingsBinding;

public class MobileSettingsFragment extends Fragment implements ReceiverCallback {
    private FragmentMobileSettingsBinding binding = null;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMobileSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.includeMobileSettings.layoutTableNumberMobile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
            intent.putExtra(ValueCodes.VALUE, mobileDefaults.tableNumber);
            launcher.launch(intent);
        });
        binding.includeMobileSettings.layoutScanRateSecondsMobile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_MOBILE_CODE);
            intent.putExtra(ValueCodes.VALUE, (int)(mobileDefaults.scanRate * 10));
            launcher.launch(intent);
        });
        binding.includeMobileSettings.includeGpsOption.swGps.setOnCheckedChangeListener((compoundButton, b) -> {
            if (isReadyToTemporary) {
                binding.includeMobileSettings.includeGpsOption.swGps.setEnabled(false);
                setTemporary(ValueCodes.GPS_CODE);
            }
        });
        binding.includeMobileSettings.swMobileAutoRecord.setOnCheckedChangeListener((compoundButton, b) -> {
            if (isReadyToTemporary) {
                binding.includeMobileSettings.swMobileAutoRecord.setEnabled(false);
                setTemporary(ValueCodes.AUTO_RECORD_CODE);
            }
        });
        binding.tvEditMobileDefault.setOnClickListener(v -> {
            goEditDefault = true;
            Intent intent = new Intent(requireContext(), MobileDefaultsActivity.class);
            intent.putExtra(ValueCodes.VALUE, mobileDefaults.originalBytes);
            startActivity(intent);
        });
        binding.btnStartMobile.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment, new MobileScanningFragment(baseFrequency, range, mobileDefaults), String.valueOf(ValueCodes.SECOND_STEP))
                        .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                        .commit();
            }
        });
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
            binding.includeMobileSettings.tvTableNumberMobile.setText(R.string.lbl_vhf_manual_option_none);
            binding.btnStartMobile.setEnabled(false);
            binding.btnStartMobile.setAlpha((float) 0.6);
        } else { // Shows the table to be scanned
            binding.includeMobileSettings.tvTableNumberMobile.setText(String.valueOf(mobileDefaults.tableNumber));
            binding.btnStartMobile.setEnabled(true);
            binding.btnStartMobile.setAlpha((float) 1);
        }
        binding.includeMobileSettings.tvScanRateSecondsMobile.setText(String.valueOf(mobileDefaults.scanRate));
        binding.includeMobileSettings.includeGpsOption.swGps.setChecked(mobileDefaults.gpsOn);
        binding.includeMobileSettings.swMobileAutoRecord.setChecked(mobileDefaults.autoRecordOn);
        isReadyToTemporary = true;
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (ValueCodes.CANCELLED == result.getResultCode())
                        return;
                    int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                        binding.includeMobileSettings.tvTableNumberMobile.setText(String.valueOf(value));
                        setTemporary(ValueCodes.TABLE_NUMBER_CODE);
                    } else if (ValueCodes.SCAN_RATE_MOBILE_CODE == result.getResultCode()) { // Gets the modified scan rate
                        binding.includeMobileSettings.tvScanRateSecondsMobile.setText(String.valueOf(value * 0.1));
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
                b[1] = (byte) Integer.parseInt(binding.includeMobileSettings.tvTableNumberMobile.getText().toString());
                mobileDefaults.tableNumber = Integer.parseInt(binding.includeMobileSettings.tvTableNumberMobile.getText().toString());
                break;
            case ValueCodes.SCAN_RATE_MOBILE_CODE:
                b[3] = (byte) (Float.parseFloat(binding.includeMobileSettings.tvScanRateSecondsMobile.getText().toString()) * 10);
                mobileDefaults.scanRate = Double.parseDouble(binding.includeMobileSettings.tvScanRateSecondsMobile.getText().toString());
                break;
            case ValueCodes.GPS_CODE:
                b[2] = binding.includeMobileSettings.includeGpsOption.swGps.isChecked() ? (byte) (Byte.toUnsignedInt(b[2]) | 0x80) : (byte) (Byte.toUnsignedInt(b[2]) & 0x7F);
                mobileDefaults.gpsOn = binding.includeMobileSettings.includeGpsOption.swGps.isChecked();
                break;
            case ValueCodes.AUTO_RECORD_CODE:
                b[2] = binding.includeMobileSettings.swMobileAutoRecord.isChecked() ? (byte) (Byte.toUnsignedInt(b[2]) | 0x40) : (byte) (Byte.toUnsignedInt(b[2]) & 0xBF);
                mobileDefaults.autoRecordOn = binding.includeMobileSettings.swMobileAutoRecord.isChecked();
                break;
        }
        boolean result = TransferBleData.writeDefaults(true, b);
        if (!result) {
            mobileDefaults = new MobileDefaults(mobileDefaults.originalBytes);
            downloadMobileDefault();
        }
        binding.includeMobileSettings.includeGpsOption.swGps.setEnabled(true);
        binding.includeMobileSettings.swMobileAutoRecord.setEnabled(true);
    }
}
