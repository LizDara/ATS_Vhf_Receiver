package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class MobileDefaultsActivity extends BaseActivity {

    @BindView(R.id.tv_table_number_mobile)
    TextView tv_table_number_mobile;
    @BindView(R.id.tv_scan_rate_seconds_mobile)
    TextView tv_scan_rate_seconds_mobile;
    @BindView(R.id.sw_gps)
    SwitchCompat sw_gps;
    @BindView(R.id.sw_mobile_auto_record)
    SwitchCompat sw_mobile_auto_record;
    @BindView(R.id.btn_start_mobile)
    Button btn_start_mobile;
    @BindView(R.id.tv_mobile_title)
    TextView tv_mobile_title;
    @BindView(R.id.tv_edit_mobile_default)
    TextView tv_edit_mobile_default;

    private MobileDefaults mobileDefaults;
    private boolean firstTime = true;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) // Get the modified frequency table number
                    tv_table_number_mobile.setText(String.valueOf(value));
                else if (ValueCodes.SCAN_RATE_MOBILE_CODE == result.getResultCode()) // Get the modified scan rate
                    tv_scan_rate_seconds_mobile.setText(String.valueOf(value * 0.1));
                boolean changed = existChanges();
                btn_start_mobile.setEnabled(changed);
                btn_start_mobile.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified aerial defaults data by the user.
     */
    private void setMobileDefaults() {
        int info = (sw_gps.isChecked() ? 1 : 0) << 7;
        info = info | ((sw_mobile_auto_record.isChecked() ? 1 : 0) << 6);
        float scanRate = Float.parseFloat(tv_scan_rate_seconds_mobile.getText().toString());
        int frequencyTableNumber = (tv_table_number_mobile.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(tv_table_number_mobile.getText().toString());
        byte[] b = new byte[] {(byte) 0x4D, (byte) frequencyTableNumber, (byte) info, (byte) (scanRate * 10), 0, 0, 0, 0};
        boolean result = TransferBleData.writeDefaults(true, b);
        if (result)
            finish();
    }

    @OnClick(R.id.layout_table_number_mobile)
    public void onClickTableNumber(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.tableNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_scan_rate_seconds_mobile)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_MOBILE_CODE);
        intent.putExtra(ValueCodes.VALUE, (int)(mobileDefaults.scanRate * 10));
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.sw_gps)
    public void onCheckedChangedGps(CompoundButton button, boolean isChecked) {
        if (!firstTime) {
            boolean changed = existChanges();
            btn_start_mobile.setEnabled(changed);
            btn_start_mobile.setAlpha(changed ? (float) 1 : (float) 0.6);
        }
    }

    @OnCheckedChanged(R.id.sw_mobile_auto_record)
    public void onCheckedChangedAutoRecord(CompoundButton button, boolean isChecked) {
        if (!firstTime) {
            boolean changed = existChanges();
            btn_start_mobile.setEnabled(changed);
            btn_start_mobile.setAlpha(changed ? (float) 1 : (float) 0.6);
        }
    }

    @OnClick(R.id.btn_start_mobile)
    public void onClickSaveChanges(View v) {
        if (!existNotSet()) {
            if (existChanges())
                setMobileDefaults();
        } else {
            AlertDialog dialog = Dialogs.createAlertDialog(this, "Error", "Complete all fields.", false);
            dialogList.add(dialog);
            dialog.setOnDismissListener(d -> dialogList.remove(dialog));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_mobile_defaults;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.aerial_defaults);
        super.onCreate(savedInstanceState);

        parameter = getIntent().getByteExtra(ValueCodes.PARAMETER, ValueCodes.NONE);
        if (parameter == ValueCodes.NONE) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
        tv_mobile_title.setText(R.string.lb_aerial_settings);
        tv_edit_mobile_default.setVisibility(View.GONE);
        btn_start_mobile.setText(R.string.lb_save_changes);
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
                tv_table_number_mobile.setText(
                        (mobileDefaults.tableNumber == 0) ? "None" : Converters.getDecimalValue(data[1]));
                tv_scan_rate_seconds_mobile.setText(String.valueOf(mobileDefaults.scanRate));
                sw_gps.setChecked(mobileDefaults.gpsOn);
                sw_mobile_auto_record.setChecked(mobileDefaults.autoRecordOn);
            } else {
                mobileDefaults = new MobileDefaults();
                tv_table_number_mobile.setText(R.string.lb_not_set);
                tv_scan_rate_seconds_mobile.setText(R.string.lb_not_set);
                sw_gps.setChecked(true);
                sw_mobile_auto_record.setChecked(true);
            }
            btn_start_mobile.setEnabled(false);
            btn_start_mobile.setAlpha((float) 0.6);
            firstTime = false;
        }
    }

    private boolean existNotSet() {
        if (tv_table_number_mobile.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (tv_scan_rate_seconds_mobile.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        return false;
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        int tableNumber = (tv_table_number_mobile.getText().toString().equals("Not Set")) ? 0 :
                Integer.parseInt(tv_table_number_mobile.getText().toString());
        double scanRate = tv_scan_rate_seconds_mobile.getText().toString().equals("Not Set") ? 0 :
                Double.parseDouble(tv_scan_rate_seconds_mobile.getText().toString());

        return mobileDefaults.tableNumber != tableNumber || mobileDefaults.gpsOn != sw_gps.isChecked()
                || mobileDefaults.autoRecordOn != sw_mobile_auto_record.isChecked() || mobileDefaults.scanRate != scanRate;
    }
}