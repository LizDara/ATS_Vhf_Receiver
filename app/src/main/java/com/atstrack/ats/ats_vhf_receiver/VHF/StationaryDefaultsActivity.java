package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.StationaryDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class StationaryDefaultsActivity extends BaseActivity {

    @BindView(R.id.tv_frequency_table_number_stationary)
    TextView tv_frequency_table_number_stationary;
    @BindView(R.id.tv_scan_rate_seconds_stationary)
    TextView tv_scan_rate_seconds_stationary;
    @BindView(R.id.tv_scan_timeout_seconds_stationary)
    TextView tv_scan_timeout_seconds_stationary;
    @BindView(R.id.tv_number_of_antennas_stationary)
    TextView tv_number_of_antennas_stationary;
    @BindView(R.id.tv_store_rate_minutes_stationary)
    TextView tv_store_rate_minutes_stationary;
    @BindView(R.id.tv_frequency_reference_stationary)
    TextView tv_frequency_reference_stationary;
    @BindView(R.id.tv_reference_frequency_store_rate_stationary)
    TextView tv_reference_frequency_store_rate_stationary;
    @BindView(R.id.layout_store_rate_stationary)
    LinearLayout layout_store_rate_stationary;
    @BindView(R.id.sw_stationary_external_data_transfer)
    SwitchCompat sw_stationary_external_data_transfer;
    @BindView(R.id.sw_stationary_reference_frequency)
    SwitchCompat sw_stationary_reference_frequency;
    @BindView(R.id.layout_reference_frequency_stationary)
    LinearLayout layout_reference_frequency_stationary;
    @BindView(R.id.layout_reference_frequency_store_rate_stationary)
    LinearLayout layout_reference_frequency_store_rate_stationary;
    @BindView(R.id.layout_external_reference_default)
    LinearLayout layout_external_reference_default;
    @BindView(R.id.layout_external_reference_scan)
    LinearLayout layout_external_reference_scan;
    @BindView(R.id.btn_start_stationary)
    Button btn_start_stationary;
    @BindView(R.id.tv_stationary_title)
    TextView tv_stationary_title;
    @BindView(R.id.tv_edit_stationary_default)
    TextView tv_edit_stationary_default;

    private StationaryDefaults stationaryDefaults;
    private int baseFrequency;
    private int range;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                if (ValueCodes.TABLES_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                    int[] value = result.getData().getIntArrayExtra(ValueCodes.VALUE);
                    String numbers = "";
                    for (int number : value)
                        numbers += number + ", ";
                    tv_frequency_table_number_stationary.setText(numbers.substring(0, numbers.length() - 2));
                } else {
                    int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    switch (result.getResultCode()) {
                        case ValueCodes.SCAN_RATE_STATIONARY_CODE: // Get the modified scan rate
                            tv_scan_rate_seconds_stationary.setText(String.valueOf(value));
                            break;
                        case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Get the modified scan timeout
                            tv_scan_timeout_seconds_stationary.setText(String.valueOf(value));
                            break;
                        case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Get the modified number of antennas
                            tv_number_of_antennas_stationary.setText(String.valueOf(value));
                            break;
                        case ValueCodes.STORE_RATE_CODE: // Get store rate
                            tv_store_rate_minutes_stationary.setText((value == 0) ? "Continuous Store" : String.valueOf(value));
                            break;
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE: // Get reference store rate
                            tv_reference_frequency_store_rate_stationary.setText(String.valueOf(value));
                            break;
                        case ValueCodes.RESULT_OK: // Get reference frequency
                            tv_frequency_reference_stationary.setText(Converters.getFrequency(value));
                            break;
                    }
                }
                boolean changed = existChanges();
                btn_start_stationary.setEnabled(changed);
                btn_start_stationary.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified stationary defaults data by the user.
     */
    private void setStationaryDefaults() {
        String[] tables = tv_frequency_table_number_stationary.getText().toString().split(", ");
        int firstTableNumber = (tables.length > 0) ? Integer.parseInt(tables[0]) : 0;
        int secondTableNumber = (tables.length > 1) ? Integer.parseInt(tables[1]) : 0;
        int thirdTableNumber = (tables.length > 2) ? Integer.parseInt(tables[2]) : 0;
        int antennasNumber = tv_number_of_antennas_stationary.getText().toString().equals("None") ? 0 :
                Integer.parseInt(tv_number_of_antennas_stationary.getText().toString());
        int scanRate = Integer.parseInt(tv_scan_rate_seconds_stationary.getText().toString());
        int scanTimeout = Integer.parseInt(tv_scan_timeout_seconds_stationary.getText().toString());
        int externalDataPush = sw_stationary_external_data_transfer.isChecked() ? 1 : 0;
        int storeRate;
        if ("Continuous Store".equals(tv_store_rate_minutes_stationary.getText().toString()))
            storeRate = 0;
        else
            storeRate = Integer.parseInt(tv_store_rate_minutes_stationary.getText().toString());
        int frequency = (sw_stationary_reference_frequency.isChecked()) ?
                (Converters.getFrequencyNumber(tv_frequency_reference_stationary.getText().toString()) - baseFrequency) : 0;
        int referenceFrequencyStoreRate = sw_stationary_reference_frequency.isChecked()
                ? Integer.parseInt(tv_reference_frequency_store_rate_stationary.getText().toString()) : 255;
        byte[] b = new byte[] {(byte) 0x4C, (byte) antennasNumber, (byte) externalDataPush, (byte) scanRate, (byte) scanTimeout,
                (byte) storeRate, (byte) (frequency / 256), (byte) (frequency % 256), (byte) referenceFrequencyStoreRate,
                (byte) firstTableNumber, (byte) secondTableNumber, (byte) thirdTableNumber};
        boolean result = TransferBleData.writeDefaults(false, b);
        if (result)
            finish();
    }

    @OnClick(R.id.layout_frequency_table_number_stationary)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLES_NUMBER_CODE);
        intent.putExtra(ValueCodes.FIRST_TABLE_NUMBER, stationaryDefaults.firstTableNumber);
        intent.putExtra(ValueCodes.SECOND_TABLE_NUMBER, stationaryDefaults.secondTableNumber);
        intent.putExtra(ValueCodes.THIRD_TABLE_NUMBER, stationaryDefaults.thirdTableNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_scan_rate_seconds_stationary)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_STATIONARY_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.scanRate);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_scan_timeout_seconds_stationary)
    public void onClickScanTimeoutSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_TIMEOUT_SECONDS_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.scanTimeout);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_number_of_antennas_stationary)
    public void onClickNumberOfAntennas(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.NUMBER_OF_ANTENNAS_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.antennaNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_store_rate_stationary)
    public void onClickStoreRate(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.STORE_RATE_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.storeRate);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.sw_stationary_reference_frequency)
    public void onCheckedChangedReferenceFrequency(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            layout_reference_frequency_stationary.setEnabled(true);
            tv_frequency_reference_stationary.setText(stationaryDefaults.referenceFrequency != 0 && stationaryDefaults.referenceFrequency != 255 ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : getString(R.string.lb_not_set));
            layout_reference_frequency_store_rate_stationary.setEnabled(true);
            tv_reference_frequency_store_rate_stationary.setText(stationaryDefaults.referenceStoreRate == 255 ? getString(R.string.lb_not_set) : String.valueOf(stationaryDefaults.referenceStoreRate));
        } else {
            layout_reference_frequency_stationary.setEnabled(false);
            tv_frequency_reference_stationary.setText(R.string.lb_no_reference_frequency);
            layout_reference_frequency_store_rate_stationary.setEnabled(false);
            tv_reference_frequency_store_rate_stationary.setText(R.string.lb_no_reference_frequency);
        }
    }

    @OnClick(R.id.layout_reference_frequency_stationary)
    public void onClickReferenceFrequency(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, "Reference Frequency");
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_reference_frequency_store_rate_stationary)
    public void onClickReferenceFrequencyStoreRate(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.referenceStoreRate);
        launcher.launch(intent);
    }

    @OnClick(R.id.btn_start_stationary)
    public void onClickSaveChanges(View v) {
        if (!existNotSet()) {
            if (existChanges()) {
                if (isDataCorrect())
                    setStationaryDefaults();
                else {
                    AlertDialog dialog = Dialogs.createAlertDialog(this, "Error", "Data incorrect.", false);
                    dialogList.add(dialog);
                    dialog.setOnDismissListener(d -> dialogList.remove(dialog));
                }
            }
        } else {
            AlertDialog dialog = Dialogs.createAlertDialog(this, "Error", "Complete all fields.", false);
            dialogList.add(dialog);
            dialog.setOnDismissListener(d -> dialogList.remove(dialog));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_stationary_defaults;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.stationary_defaults);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
        parameter = getIntent().getByteExtra(ValueCodes.PARAMETER, ValueCodes.NONE);
        if (parameter == ValueCodes.NONE) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
        tv_stationary_title.setText(R.string.lb_aerial_settings);
        tv_edit_stationary_default.setVisibility(View.GONE);
        btn_start_stationary.setText(R.string.lb_save_changes);
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
        if (parameter == ValueCodes.STATIONARY_DEFAULTS_COMMAND)
            TransferBleData.readDefaults(false);
    }

    @Override
    protected void downloadData(byte[] data) {
        if (data[0] == ValueCodes.STATIONARY_DEFAULTS_COMMAND) {
            parameter = ValueCodes.NONE;
            layout_external_reference_default.setVisibility(View.VISIBLE);
            layout_external_reference_scan.setVisibility(View.GONE);
            if (!Converters.isDefaultEmpty(data)) {
                stationaryDefaults = new StationaryDefaults(baseFrequency, data);
                String tables = "";
                if (stationaryDefaults.firstTableNumber != 0 && stationaryDefaults.firstTableNumber != 255)
                    tables += stationaryDefaults.firstTableNumber;
                if (stationaryDefaults.secondTableNumber != 0 && stationaryDefaults.secondTableNumber != 255)
                    tables += ", " + stationaryDefaults.secondTableNumber;
                if (stationaryDefaults.thirdTableNumber != 0 && stationaryDefaults.thirdTableNumber != 255)
                    tables += ", " + stationaryDefaults.thirdTableNumber;
                tv_frequency_table_number_stationary.setText(tables.isEmpty() ? "None" : tables);
                tv_number_of_antennas_stationary.setText((stationaryDefaults.antennaNumber == 0) ? "None" : String.valueOf(stationaryDefaults.antennaNumber));
                sw_stationary_external_data_transfer.setChecked(stationaryDefaults.dataTransferOn);
                tv_scan_rate_seconds_stationary.setText(String.valueOf(stationaryDefaults.scanRate));
                tv_scan_timeout_seconds_stationary.setText(String.valueOf(stationaryDefaults.scanTimeout));
                tv_store_rate_minutes_stationary.setText(stationaryDefaults.storeRate == 0 ? getString(R.string.lb_continuous_store) : String.valueOf(stationaryDefaults.storeRate));
                tv_frequency_reference_stationary.setText((stationaryDefaults.referenceFrequencyOn) ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : "No Reference Frequency");
                tv_reference_frequency_store_rate_stationary.setText((stationaryDefaults.referenceFrequencyOn) ? String.valueOf(stationaryDefaults.referenceStoreRate) : "No Reference Frequency");
                sw_stationary_reference_frequency.setChecked(stationaryDefaults.referenceFrequencyOn);
            } else {
                stationaryDefaults = new StationaryDefaults();
                tv_frequency_table_number_stationary.setText(R.string.lb_not_set);
                tv_number_of_antennas_stationary.setText(R.string.lb_not_set);
                sw_stationary_external_data_transfer.setChecked(true);
                tv_scan_rate_seconds_stationary.setText(R.string.lb_not_set);
                tv_scan_timeout_seconds_stationary.setText(R.string.lb_not_set);
                tv_store_rate_minutes_stationary.setText(R.string.lb_not_set);
                tv_frequency_reference_stationary.setText(R.string.lb_not_set);
                tv_reference_frequency_store_rate_stationary.setText(R.string.lb_not_set);
            }
            btn_start_stationary.setEnabled(false);
            btn_start_stationary.setAlpha((float) 0.6);
        }
    }

    private boolean existNotSet() {
        if (tv_frequency_table_number_stationary.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (tv_number_of_antennas_stationary.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (tv_scan_rate_seconds_stationary.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (tv_scan_timeout_seconds_stationary.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (tv_store_rate_minutes_stationary.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (sw_stationary_reference_frequency.isChecked()) {
            if (tv_frequency_reference_stationary.getText().toString().equals(getString(R.string.lb_not_set)))
                return true;
            if (tv_reference_frequency_store_rate_stationary.getText().toString().equals(getString(R.string.lb_not_set)))
                return true;
        }
        return false;
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        int firstTableNumber = 0, secondTableNumber = 0, thirdTableNumber = 0;
        if (!tv_frequency_table_number_stationary.getText().toString().equals("Not Set")) {
            String[] tables = tv_frequency_table_number_stationary.getText().toString().split(", ");
            firstTableNumber = (tables.length > 0) ? Integer.parseInt(tables[0]) : 0;
            secondTableNumber = (tables.length > 1) ? Integer.parseInt(tables[1]) : 0;
            thirdTableNumber = (tables.length > 2) ? Integer.parseInt(tables[2]) : 0;
        }
        int antennaNumber = (tv_number_of_antennas_stationary.getText().toString().equals("Not Set") ? 0 :
                Integer.parseInt(tv_number_of_antennas_stationary.getText().toString()));
        int scanRate = tv_scan_rate_seconds_stationary.getText().toString().equals("Not Set") ? 0 :
                Integer.parseInt(tv_scan_rate_seconds_stationary.getText().toString());
        int timeout = tv_scan_timeout_seconds_stationary.getText().toString().equals("Not Set") ? 0 :
                Integer.parseInt(tv_scan_timeout_seconds_stationary.getText().toString());
        int storeRate;
        if ("Continuous Store".equals(tv_store_rate_minutes_stationary.getText().toString()))
            storeRate = 0;
        else
            storeRate = tv_store_rate_minutes_stationary.getText().toString().equals("Not Set") ? 0 :
                    Integer.parseInt(tv_store_rate_minutes_stationary.getText().toString());
        int referenceFrequency = sw_stationary_reference_frequency.isChecked() && !tv_frequency_reference_stationary.getText().toString().equals("Not Set") ?
                Converters.getFrequencyNumber(tv_frequency_reference_stationary.getText().toString()) : 0;
        int referenceFrequencyStoreRate = sw_stationary_reference_frequency.isChecked() && !tv_reference_frequency_store_rate_stationary.getText().toString().equals("Not Set") ?
                Integer.parseInt(tv_reference_frequency_store_rate_stationary.getText().toString()) : 255;

        return stationaryDefaults.firstTableNumber != firstTableNumber || stationaryDefaults.secondTableNumber != secondTableNumber
                || stationaryDefaults.thirdTableNumber != thirdTableNumber || stationaryDefaults.antennaNumber != antennaNumber
                || stationaryDefaults.scanRate != scanRate || stationaryDefaults.scanTimeout != timeout
                || stationaryDefaults.storeRate != storeRate || stationaryDefaults.referenceFrequency != referenceFrequency
                || stationaryDefaults.referenceStoreRate != referenceFrequencyStoreRate
                || stationaryDefaults.dataTransferOn != sw_stationary_external_data_transfer.isChecked();
    }

    /**
     * Checks that the data is a valid and correct format.
     * @return Returns true, if the data is correct.
     */
    private boolean isDataCorrect() {
        boolean scanTimeCorrect = Integer.parseInt(tv_scan_timeout_seconds_stationary.getText().toString())
                < Integer.parseInt(tv_scan_rate_seconds_stationary.getText().toString());
        boolean referenceFrequencyCorrect = !sw_stationary_reference_frequency.isChecked() || !tv_frequency_reference_stationary.getText().equals("0");
        return scanTimeCorrect && referenceFrequencyCorrect;
    }
}