package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfMobileDefaultsBinding;

public class MobileDefaultsActivity extends BaseActivity {
    private MobileDefaults mobileDefaults;
    private MobileDefaults currentMobileDefaults;
    private boolean firstTime = true;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) { // Get the modified frequency table number
                    currentMobileDefaults.tableNumber = value;
                    ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.setText(String.valueOf(currentMobileDefaults.tableNumber));
                } else if (ValueCodes.SCAN_RATE_MOBILE_CODE == result.getResultCode()) { // Get the modified scan rate
                    currentMobileDefaults.scanTime = value * 0.1;
                    ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.setText(String.format("%.1f", currentMobileDefaults.scanTime));
                }
                boolean changed = existChanges();
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setEnabled(changed);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified aerial defaults data by the user.
     */
    private void setMobileDefaults() {
        int info = (currentMobileDefaults.gpsOn ? 1 : 0) << 7;
        info = info | ((currentMobileDefaults.autoRecordOn ? 1 : 0) << 6);
        byte[] b = new byte[] {(byte) 0x4D, (byte) currentMobileDefaults.tableNumber, (byte) info, (byte) (currentMobileDefaults.scanTime * 10), 0, 0, 0, 0};
        boolean result = TransferBleData.writeDefaults(true, b);
        if (result)
            finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.title_vhf_defaults_mobile);
        binding = ActivityVhfMobileDefaultsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.layoutTableNumberMobile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
            intent.putExtra(ValueCodes.VALUE, mobileDefaults.tableNumber);
            launcher.launch(intent);
        });
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.layoutScanRateSecondsMobile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_MOBILE_CODE);
            intent.putExtra(ValueCodes.VALUE, (int)(mobileDefaults.scanTime * 10));
            launcher.launch(intent);
        });
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swGps.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!firstTime) {
                currentMobileDefaults.gpsOn = b;
                boolean changed = existChanges();
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setEnabled(changed);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setAlpha(changed ? (float) 1 : (float) 0.6);
            }
        });
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swMobileAutoRecord.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!firstTime) {
                currentMobileDefaults.autoRecordOn = b;
                boolean changed = existChanges();
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setEnabled(changed);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setAlpha(changed ? (float) 1 : (float) 0.6);
            }
        });
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setOnClickListener(v -> {
            if (!currentMobileDefaults.existNotSet()) {
                if (existChanges()) {
                    if (currentMobileDefaults.isDataCorrect())
                        setMobileDefaults();
                }
            } else {
                AlertDialog dialog = Dialogs.createAlertDialog(this, "Error", "Complete all fields.", false);
                dialogList.add(dialog);
                dialog.setOnDismissListener(d -> dialogList.remove(dialog));
            }
        });

        parameter = getIntent().getByteExtra(ValueCodes.PARAMETER, ValueCodes.NONE);
        if (parameter == ValueCodes.NONE) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.tvMobileTitle.setText(R.string.lbl_vhf_defaults_mobile_settings);
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.tvEditMobileDefault.setVisibility(View.GONE);
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setText(R.string.btn_vhf_tables_save_changes);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void discoverCharacteristic() {
        if (parameter == ValueCodes.MOBILE_DEFAULTS_COMMAND)
            TransferBleData.readDefaults(true);
    }

    @Override
    protected void downloadData(byte[] data) {
        if (data[0] == ValueCodes.MOBILE_DEFAULTS_COMMAND) {
            if (!Converters.isDefaultEmpty(data)) {
                mobileDefaults = new MobileDefaults(data);
                currentMobileDefaults = new MobileDefaults(data);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.setText(
                        (mobileDefaults.tableNumber == 0) ? getString(R.string.lbl_vhf_manual_option_none) : String.valueOf(Byte.toUnsignedInt(data[1])));
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.setText(String.format("%.1f", mobileDefaults.scanTime));
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swGps.setChecked(mobileDefaults.gpsOn);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swMobileAutoRecord.setChecked(mobileDefaults.autoRecordOn);
            } else {
                mobileDefaults = new MobileDefaults();
                currentMobileDefaults = new MobileDefaults();
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swGps.setChecked(true);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swMobileAutoRecord.setChecked(true);
            }
            ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setEnabled(false);
            ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setAlpha((float) 0.6);
            firstTime = false;
        }
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        return mobileDefaults.tableNumber != currentMobileDefaults.tableNumber || mobileDefaults.gpsOn != currentMobileDefaults.gpsOn
                || mobileDefaults.autoRecordOn != currentMobileDefaults.autoRecordOn || mobileDefaults.scanTime != currentMobileDefaults.scanTime;
    }
}