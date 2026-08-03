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
    private boolean firstTime = true;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) // Get the modified frequency table number
                    ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.setText(String.valueOf(value));
                else if (ValueCodes.SCAN_RATE_MOBILE_CODE == result.getResultCode()) // Get the modified scan rate
                    ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.setText(String.valueOf(value * 0.1));
                boolean changed = existChanges();
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setEnabled(changed);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified aerial defaults data by the user.
     */
    private void setMobileDefaults() {
        int info = (((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.includeGpsOption.swGps.isChecked() ? 1 : 0) << 7;
        info = info | ((((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swMobileAutoRecord.isChecked() ? 1 : 0) << 6);
        float scanRate = Float.parseFloat(((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.getText().toString());
        int frequencyTableNumber = (((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.getText().toString());
        byte[] b = new byte[] {(byte) 0x4D, (byte) frequencyTableNumber, (byte) info, (byte) (scanRate * 10), 0, 0, 0, 0};
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
            intent.putExtra(ValueCodes.VALUE, (int)(mobileDefaults.scanRate * 10));
            launcher.launch(intent);
        });
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.includeGpsOption.swGps.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!firstTime) {
                boolean changed = existChanges();
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setEnabled(changed);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setAlpha(changed ? (float) 1 : (float) 0.6);
            }
        });
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swMobileAutoRecord.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!firstTime) {
                boolean changed = existChanges();
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setEnabled(changed);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setAlpha(changed ? (float) 1 : (float) 0.6);
            }
        });
        ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setOnClickListener(v -> {
            if (!existNotSet()) {
                if (existChanges())
                    setMobileDefaults();
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
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.setText(
                        (mobileDefaults.tableNumber == 0) ? "None" : String.valueOf(Byte.toUnsignedInt(data[1])));
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.setText(String.valueOf(mobileDefaults.scanRate));
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.includeGpsOption.swGps.setChecked(mobileDefaults.gpsOn);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swMobileAutoRecord.setChecked(mobileDefaults.autoRecordOn);
            } else {
                mobileDefaults = new MobileDefaults();
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.includeGpsOption.swGps.setChecked(true);
                ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swMobileAutoRecord.setChecked(true);
            }
            ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setEnabled(false);
            ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.btnStartMobile.setAlpha((float) 0.6);
            firstTime = false;
        }
    }

    private boolean existNotSet() {
        if (((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set)))
            return true;
        return ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set));
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        int tableNumber = (((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.getText().toString().equals("Not Set")) ? 0 :
                Integer.parseInt(((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvTableNumberMobile.getText().toString());
        double scanRate = ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.getText().toString().equals("Not Set") ? 0 :
                Double.parseDouble(((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.tvScanRateSecondsMobile.getText().toString());

        return mobileDefaults.tableNumber != tableNumber || mobileDefaults.gpsOn != ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.includeGpsOption.swGps.isChecked()
                || mobileDefaults.autoRecordOn != ((ActivityVhfMobileDefaultsBinding) binding).fragmentMobileSettings.includeMobileSettings.swMobileAutoRecord.isChecked() || mobileDefaults.scanRate != scanRate;
    }
}