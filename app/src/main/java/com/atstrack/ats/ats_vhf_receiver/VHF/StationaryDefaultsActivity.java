package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.StationaryDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfStationaryDefaultsBinding;

public class StationaryDefaultsActivity extends BaseActivity {
    private StationaryDefaults stationaryDefaults;
    private int baseFrequency;
    private int range;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                if (ValueCodes.TABLES_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                    int[] value = result.getData().getIntArrayExtra(ValueCodes.VALUE);
                    String numbers = "";
                    for (int number : value)
                        numbers += number + ", ";
                    ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.setText(numbers.substring(0, numbers.length() - 2));
                } else {
                    int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    switch (result.getResultCode()) {
                        case ValueCodes.SCAN_RATE_STATIONARY_CODE: // Get the modified scan rate
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.setText(String.valueOf(value));
                            break;
                        case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Get the modified scan timeout
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.setText(String.valueOf(value));
                            break;
                        case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Get the modified number of antennas
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.setText(String.valueOf(value));
                            break;
                        case ValueCodes.STORE_RATE_CODE: // Get store rate
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.setText((value == 0) ? "Continuous Store" : String.valueOf(value));
                            break;
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE: // Get reference store rate
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText(String.valueOf(value));
                            break;
                        case ValueCodes.RESULT_OK: // Get reference frequency
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText(Converters.getFrequency(value));
                            break;
                    }
                }
                boolean changed = existChanges();
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setEnabled(changed);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified stationary defaults data by the user.
     */
    private void setStationaryDefaults() {
        String[] tables = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.getText().toString().split(", ");
        int firstTableNumber = (tables.length > 0) ? Integer.parseInt(tables[0]) : 0;
        int secondTableNumber = (tables.length > 1) ? Integer.parseInt(tables[1]) : 0;
        int thirdTableNumber = (tables.length > 2) ? Integer.parseInt(tables[2]) : 0;
        int antennasNumber = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.getText().toString().equals("None") ? 0 :
                Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.getText().toString());
        int scanRate = Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.getText().toString());
        int scanTimeout = Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.getText().toString());
        int externalDataPush = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryExternalDataTransfer.isChecked() ? 1 : 0;
        int storeRate;
        if ("Continuous Store".equals(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.getText().toString()))
            storeRate = 0;
        else
            storeRate = Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.getText().toString());
        int frequency = (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.isChecked()) ?
                (Converters.getFrequencyNumber(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.getText().toString()) - baseFrequency) : 0;
        int referenceFrequencyStoreRate = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.isChecked()
                ? Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.getText().toString()) : 255;
        byte[] b = new byte[] {(byte) 0x4C, (byte) antennasNumber, (byte) externalDataPush, (byte) scanRate, (byte) scanTimeout,
                (byte) storeRate, (byte) (frequency / 256), (byte) (frequency % 256), (byte) referenceFrequencyStoreRate,
                (byte) firstTableNumber, (byte) secondTableNumber, (byte) thirdTableNumber};
        boolean result = TransferBleData.writeDefaults(false, b);
        if (result)
            finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.title_vhf_defaults_stationary);
        binding = ActivityVhfStationaryDefaultsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutFrequencyTableNumberStationary.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLES_NUMBER_CODE);
            intent.putExtra(ValueCodes.FIRST_TABLE_NUMBER, stationaryDefaults.firstTableNumber);
            intent.putExtra(ValueCodes.SECOND_TABLE_NUMBER, stationaryDefaults.secondTableNumber);
            intent.putExtra(ValueCodes.THIRD_TABLE_NUMBER, stationaryDefaults.thirdTableNumber);
            launcher.launch(intent);
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutScanRateSecondsStationary.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_STATIONARY_CODE);
            intent.putExtra(ValueCodes.VALUE, stationaryDefaults.scanRate);
            launcher.launch(intent);
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutScanTimeoutSecondsStationary.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_TIMEOUT_SECONDS_CODE);
            intent.putExtra(ValueCodes.VALUE, stationaryDefaults.scanTimeout);
            launcher.launch(intent);
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutNumberOfAntennasStationary.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.NUMBER_OF_ANTENNAS_CODE);
            intent.putExtra(ValueCodes.VALUE, stationaryDefaults.antennaNumber);
            launcher.launch(intent);
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutStoreRateStationary.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.STORE_RATE_CODE);
            intent.putExtra(ValueCodes.VALUE, stationaryDefaults.storeRate);
            launcher.launch(intent);
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStationary.setEnabled(true);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText(stationaryDefaults.referenceFrequency != 0 && stationaryDefaults.referenceFrequency != 255 ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : getString(R.string.lbl_vhf_config_not_set));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStoreRateStationary.setEnabled(true);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText(stationaryDefaults.referenceStoreRate == 255 ? getString(R.string.lbl_vhf_config_not_set) : String.valueOf(stationaryDefaults.referenceStoreRate));
            } else {
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStationary.setEnabled(false);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText(R.string.lbl_vhf_defaults_stationary_no_reference);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStoreRateStationary.setEnabled(false);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText(R.string.lbl_vhf_defaults_stationary_no_reference);
            }
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStationary.setOnClickListener(v -> {
            Intent intent = new Intent(this, EnterFrequencyActivity.class);
            intent.putExtra(ValueCodes.TITLE, "Reference Frequency");
            intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
            intent.putExtra(ValueCodes.RANGE, range);
            launcher.launch(intent);
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStoreRateStationary.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDefaultsActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE);
            intent.putExtra(ValueCodes.VALUE, stationaryDefaults.referenceStoreRate);
            launcher.launch(intent);
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setOnClickListener(v -> {
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
        });

        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
        parameter = getIntent().getByteExtra(ValueCodes.PARAMETER, ValueCodes.NONE);
        if (parameter == ValueCodes.NONE) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.tvStationaryTitle.setText(R.string.lbl_vhf_defaults_stationary_settings);
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.tvEditStationaryDefault.setVisibility(View.GONE);
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setText(R.string.btn_vhf_tables_save_changes);
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
            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutExternalReferenceDefault.setVisibility(View.VISIBLE);
            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutExternalReferenceScan.setVisibility(View.GONE);
            if (!Converters.isDefaultEmpty(data)) {
                stationaryDefaults = new StationaryDefaults(baseFrequency, data);
                String tables = "";
                if (stationaryDefaults.firstTableNumber != 0 && stationaryDefaults.firstTableNumber != 255)
                    tables += stationaryDefaults.firstTableNumber;
                if (stationaryDefaults.secondTableNumber != 0 && stationaryDefaults.secondTableNumber != 255)
                    tables += ", " + stationaryDefaults.secondTableNumber;
                if (stationaryDefaults.thirdTableNumber != 0 && stationaryDefaults.thirdTableNumber != 255)
                    tables += ", " + stationaryDefaults.thirdTableNumber;
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.setText(tables.isEmpty() ? "None" : tables);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.setText((stationaryDefaults.antennaNumber == 0) ? "None" : String.valueOf(stationaryDefaults.antennaNumber));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryExternalDataTransfer.setChecked(stationaryDefaults.dataTransferOn);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.setText(String.valueOf(stationaryDefaults.scanRate));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.setText(String.valueOf(stationaryDefaults.scanTimeout));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.setText(stationaryDefaults.storeRate == 0 ? getString(R.string.lbl_vhf_defaults_store_rate_continuous) : String.valueOf(stationaryDefaults.storeRate));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText((stationaryDefaults.referenceFrequencyOn) ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : "No Reference Frequency");
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText((stationaryDefaults.referenceFrequencyOn) ? String.valueOf(stationaryDefaults.referenceStoreRate) : "No Reference Frequency");
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.setChecked(stationaryDefaults.referenceFrequencyOn);
            } else {
                stationaryDefaults = new StationaryDefaults();
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryExternalDataTransfer.setChecked(true);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText(R.string.lbl_vhf_config_not_set);
            }
            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setEnabled(false);
            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setAlpha((float) 0.6);
        }
    }

    private boolean existNotSet() {
        if (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set)))
            return true;
        if (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set)))
            return true;
        if (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set)))
            return true;
        if (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set)))
            return true;
        if (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set)))
            return true;
        if (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.isChecked()) {
            if (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set)))
                return true;
            if (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.getText().toString().equals(getString(R.string.lbl_vhf_config_not_set)))
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
        if (!((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.getText().toString().equals("Not Set")) {
            String[] tables = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.getText().toString().split(", ");
            firstTableNumber = (tables.length > 0) ? Integer.parseInt(tables[0]) : 0;
            secondTableNumber = (tables.length > 1) ? Integer.parseInt(tables[1]) : 0;
            thirdTableNumber = (tables.length > 2) ? Integer.parseInt(tables[2]) : 0;
        }
        int antennaNumber = (((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.getText().toString().equals("Not Set") ? 0 :
                Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.getText().toString()));
        int scanRate = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.getText().toString().equals("Not Set") ? 0 :
                Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.getText().toString());
        int timeout = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.getText().toString().equals("Not Set") ? 0 :
                Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.getText().toString());
        int storeRate;
        if ("Continuous Store".equals(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.getText().toString()))
            storeRate = 0;
        else
            storeRate = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.getText().toString().equals("Not Set") ? 0 :
                    Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.getText().toString());
        int referenceFrequency = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.isChecked() && !((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.getText().toString().equals("Not Set") ?
                Converters.getFrequencyNumber(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.getText().toString()) : 0;
        int referenceFrequencyStoreRate = ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.isChecked() && !((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.getText().toString().equals("Not Set") ?
                Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.getText().toString()) : 255;

        return stationaryDefaults.firstTableNumber != firstTableNumber || stationaryDefaults.secondTableNumber != secondTableNumber
                || stationaryDefaults.thirdTableNumber != thirdTableNumber || stationaryDefaults.antennaNumber != antennaNumber
                || stationaryDefaults.scanRate != scanRate || stationaryDefaults.scanTimeout != timeout
                || stationaryDefaults.storeRate != storeRate || stationaryDefaults.referenceFrequency != referenceFrequency
                || stationaryDefaults.referenceStoreRate != referenceFrequencyStoreRate
                || stationaryDefaults.dataTransferOn != ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryExternalDataTransfer.isChecked();
    }

    /**
     * Checks that the data is a valid and correct format.
     * @return Returns true, if the data is correct.
     */
    private boolean isDataCorrect() {
        boolean scanTimeCorrect = Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.getText().toString())
                < Integer.parseInt(((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.getText().toString());
        boolean referenceFrequencyCorrect = !((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.isChecked() || !((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.getText().equals("0");
        return scanTimeCorrect && referenceFrequencyCorrect;
    }
}