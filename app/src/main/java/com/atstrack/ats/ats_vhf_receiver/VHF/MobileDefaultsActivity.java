package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

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
import com.atstrack.ats.ats_vhf_receiver.Utils.Messages;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class MobileDefaultsActivity extends BaseActivity {

    @BindView(R.id.frequency_table_number_aerial_textView)
    TextView frequency_table_number_aerial_textView;
    @BindView(R.id.scan_rate_seconds_aerial_textView)
    TextView scan_rate_seconds_aerial_textView;
    @BindView(R.id.gps_switch)
    SwitchCompat gps_switch;
    @BindView(R.id.aerial_auto_record_switch)
    SwitchCompat aerial_auto_record_switch;
    @BindView(R.id.save_changes_aerial_button)
    Button save_changes_aerial_button;

    private MobileDefaults mobileDefaults;
    private boolean firstTime = true;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) // Get the modified frequency table number
                    frequency_table_number_aerial_textView.setText(String.valueOf(value));
                else if (ValueCodes.SCAN_RATE_MOBILE_CODE == result.getResultCode()) // Get the modified scan rate
                    scan_rate_seconds_aerial_textView.setText(String.valueOf(value * 0.1));
                boolean changed = existChanges();
                save_changes_aerial_button.setEnabled(changed);
                save_changes_aerial_button.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified aerial defaults data by the user.
     */
    private void setMobileDefaults() {
        int info = (gps_switch.isChecked() ? 1 : 0) << 7;
        info = info | ((aerial_auto_record_switch.isChecked() ? 1 : 0) << 6);
        float scanRate = Float.parseFloat(scan_rate_seconds_aerial_textView.getText().toString());
        int frequencyTableNumber = (frequency_table_number_aerial_textView.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
        byte[] b = new byte[] {(byte) 0x4D, (byte) frequencyTableNumber, (byte) info, (byte) (scanRate * 10), 0, 0, 0, 0};
        boolean result = TransferBleData.writeDefaults(true, b);
        if (result)
            finish();
    }

    @OnClick(R.id.frequency_table_number_aerial_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.tableNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_aerial_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_MOBILE_CODE);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.scanRate);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.gps_switch)
    public void onCheckedChangedGps(CompoundButton button, boolean isChecked) {
        if (!firstTime) {
            boolean changed = existChanges();
            save_changes_aerial_button.setEnabled(changed);
            save_changes_aerial_button.setAlpha(changed ? (float) 1 : (float) 0.6);
        }
    }

    @OnCheckedChanged(R.id.aerial_auto_record_switch)
    public void onCheckedChangedAutoRecord(CompoundButton button, boolean isChecked) {
        if (!firstTime) {
            boolean changed = existChanges();
            save_changes_aerial_button.setEnabled(changed);
            save_changes_aerial_button.setAlpha(changed ? (float) 1 : (float) 0.6);
        }
    }

    @OnClick(R.id.save_changes_aerial_button)
    public void onClickSaveChanges(View v) {
        if (!existNotSet()) {
            if (existChanges())
                setMobileDefaults();
        } else {
            Messages.showMessage(this, "Complete all fields.");
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
                frequency_table_number_aerial_textView.setText(
                        (mobileDefaults.tableNumber == 0) ? "None" : Converters.getDecimalValue(data[1]));
                scan_rate_seconds_aerial_textView.setText(String.valueOf(mobileDefaults.scanRate));
                gps_switch.setChecked(mobileDefaults.gpsOn);
                aerial_auto_record_switch.setChecked(mobileDefaults.autoRecordOn);
            } else {
                mobileDefaults = new MobileDefaults();
                frequency_table_number_aerial_textView.setText(R.string.lb_not_set);
                scan_rate_seconds_aerial_textView.setText(R.string.lb_not_set);
                gps_switch.setChecked(true);
                aerial_auto_record_switch.setChecked(true);
            }
            save_changes_aerial_button.setEnabled(false);
            save_changes_aerial_button.setAlpha((float) 0.6);
            firstTime = false;
        }
    }

    private boolean existNotSet() {
        if (frequency_table_number_aerial_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (scan_rate_seconds_aerial_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        return false;
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        int tableNumber = (frequency_table_number_aerial_textView.getText().toString().equals("Not Set")) ? 0 :
                Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
        double scanRate = scan_rate_seconds_aerial_textView.getText().toString().equals("Not Set") ? 0 :
                Double.parseDouble(scan_rate_seconds_aerial_textView.getText().toString());

        return mobileDefaults.tableNumber != tableNumber || mobileDefaults.gpsOn != gps_switch.isChecked()
                || mobileDefaults.autoRecordOn != aerial_auto_record_switch.isChecked() || mobileDefaults.scanRate != scanRate;
    }
}