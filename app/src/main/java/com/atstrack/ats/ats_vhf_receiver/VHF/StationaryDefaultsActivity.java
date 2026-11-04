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
    private StationaryDefaults currentStationaryDefaults;
    private boolean firstTime = true;
    private int baseFrequency;
    private int range;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                if (ValueCodes.TABLES_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                    int[] value = result.getData().getIntArrayExtra(ValueCodes.VALUE);
                    if (value.length >= 1)
                        currentStationaryDefaults.firstTableNumber = value[0];
                    if (value.length >= 2)
                        currentStationaryDefaults.secondTableNumber = value[1];
                    if (value.length == 3)
                        currentStationaryDefaults.thirdTableNumber = value[2];
                    String tables = currentStationaryDefaults.getTablesText();
                    ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.setText((tables.isEmpty()) ? getString(R.string.lbl_vhf_manual_option_none) : tables);
                } else {
                    int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    switch (result.getResultCode()) {
                        case ValueCodes.SCAN_RATE_STATIONARY_CODE: // Get the modified scan rate
                            currentStationaryDefaults.scanTime = value;
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.setText(String.valueOf(currentStationaryDefaults.scanTime));
                            break;
                        case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Get the modified scan timeout
                            currentStationaryDefaults.scanTimeout = value;
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.setText(String.valueOf(currentStationaryDefaults.scanTimeout));
                            break;
                        case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Get the modified number of antennas
                            currentStationaryDefaults.antennaNumber = value;
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.setText(String.valueOf(currentStationaryDefaults.antennaNumber));
                            break;
                        case ValueCodes.STORE_RATE_CODE: // Get store rate
                            currentStationaryDefaults.storeRate = value;
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.setText((currentStationaryDefaults.storeRate == 0) ? getString(R.string.lbl_vhf_defaults_store_rate_continuous) : String.valueOf(currentStationaryDefaults.storeRate));
                            break;
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE: // Get reference store rate
                            currentStationaryDefaults.referenceStoreRate = value;
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText(String.valueOf(currentStationaryDefaults.referenceStoreRate));
                            break;
                        case ValueCodes.RESULT_OK: // Get reference frequency
                            currentStationaryDefaults.referenceFrequency = value;
                            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText(Converters.getFrequency(currentStationaryDefaults.referenceFrequency));
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
        byte[] b = new byte[] {(byte) 0x4C, (byte) currentStationaryDefaults.antennaNumber, (byte) (currentStationaryDefaults.dataTransferOn ? 1 : 0), (byte) currentStationaryDefaults.scanTime, (byte) currentStationaryDefaults.scanTimeout,
                (byte) currentStationaryDefaults.storeRate, (byte) (currentStationaryDefaults.referenceFrequency != Byte.toUnsignedInt(ValueCodes.NULL) ? (currentStationaryDefaults.referenceFrequency / 256) : ValueCodes.NULL), (byte) (currentStationaryDefaults.referenceFrequency != Byte.toUnsignedInt(ValueCodes.NULL) ? (currentStationaryDefaults.referenceFrequency % 256) : ValueCodes.NULL),
                (byte) currentStationaryDefaults.referenceStoreRate, (byte) currentStationaryDefaults.firstTableNumber, (byte) currentStationaryDefaults.secondTableNumber, (byte) currentStationaryDefaults.thirdTableNumber};
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
            intent.putExtra(ValueCodes.VALUE, stationaryDefaults.scanTime);
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
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText(stationaryDefaults.referenceFrequency != Byte.toUnsignedInt(ValueCodes.NULL) ?
                        Converters.getFrequency(stationaryDefaults.referenceFrequency) : getString(R.string.lbl_vhf_config_not_set));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStoreRateStationary.setEnabled(true);
                String referenceStoreRate = stationaryDefaults.referenceStoreRate != Byte.toUnsignedInt(ValueCodes.NULL) ? String.valueOf(stationaryDefaults.referenceStoreRate) : getString(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText(referenceStoreRate);
                currentStationaryDefaults.referenceFrequency = stationaryDefaults.referenceFrequency;
                currentStationaryDefaults.referenceStoreRate = stationaryDefaults.referenceStoreRate;
            } else {
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStationary.setEnabled(false);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText(R.string.lbl_vhf_defaults_stationary_no_reference);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStoreRateStationary.setEnabled(false);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText(R.string.lbl_vhf_defaults_stationary_no_reference);
                currentStationaryDefaults.referenceFrequency = Byte.toUnsignedInt(ValueCodes.NULL);
                currentStationaryDefaults.referenceStoreRate = Byte.toUnsignedInt(ValueCodes.NULL);
            }
            if (!firstTime) {
                currentStationaryDefaults.referenceFrequencyOn = b;
                boolean changed = existChanges();
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setEnabled(changed);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setAlpha(changed ? (float) 1 : (float) 0.6);
            }
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryExternalDataTransfer.setOnCheckedChangeListener((compoundButton, b) -> {
            currentStationaryDefaults.dataTransferOn = b;
            if (!firstTime) {
                boolean changed = existChanges();
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setEnabled(changed);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setAlpha(changed ? (float) 1 : (float) 0.6);
            }
        });
        ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.layoutReferenceFrequencyStationary.setOnClickListener(v -> {
            Intent intent = new Intent(this, EnterFrequencyActivity.class);
            intent.putExtra(ValueCodes.TITLE, getString(R.string.lbl_vhf_stationary_reference_freq));
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
            if (!currentStationaryDefaults.existNotSet()) {
                if (existChanges()) {
                    if (currentStationaryDefaults.isDataCorrect())
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
                currentStationaryDefaults = new StationaryDefaults(baseFrequency, data);
                String tables = stationaryDefaults.getTablesText();
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.setText(tables.isEmpty() ? getString(R.string.lbl_vhf_manual_option_none) : tables);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.setText((stationaryDefaults.antennaNumber == ValueCodes.NONE) ? getString(R.string.lbl_vhf_manual_option_none) : String.valueOf(stationaryDefaults.antennaNumber));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryExternalDataTransfer.setChecked(stationaryDefaults.dataTransferOn);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.setText(String.valueOf(stationaryDefaults.scanTime));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.setText(String.valueOf(stationaryDefaults.scanTimeout));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.setText(stationaryDefaults.storeRate == ValueCodes.NONE ? getString(R.string.lbl_vhf_defaults_store_rate_continuous) : String.valueOf(stationaryDefaults.storeRate));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText((stationaryDefaults.referenceFrequencyOn) ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : getString(R.string.lbl_vhf_defaults_stationary_no_reference));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText((stationaryDefaults.referenceFrequencyOn) ? String.valueOf(stationaryDefaults.referenceStoreRate) : getString(R.string.lbl_vhf_defaults_stationary_no_reference));
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.setChecked(stationaryDefaults.referenceFrequencyOn);
            } else {
                stationaryDefaults = new StationaryDefaults();
                currentStationaryDefaults = new StationaryDefaults();
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyTableNumberStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvNumberOfAntennasStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryExternalDataTransfer.setChecked(true);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.swStationaryReferenceFrequency.setChecked(true);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanRateSecondsStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvScanTimeoutSecondsStationary.setText(R.string.lbl_vhf_config_not_set);
                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvStoreRateMinutesStationary.setText(R.string.lbl_vhf_config_not_set);
//                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvFrequencyReferenceStationary.setText(R.string.lbl_vhf_config_not_set);
//                ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.includeStationarySettings.tvReferenceFrequencyStoreRateStationary.setText(R.string.lbl_vhf_config_not_set);
            }
            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setEnabled(false);
            ((ActivityVhfStationaryDefaultsBinding) binding).fragmentStationarySettings.btnStartStationary.setAlpha((float) 0.6);
            firstTime = false;
        }
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        return stationaryDefaults.firstTableNumber != currentStationaryDefaults.firstTableNumber || stationaryDefaults.secondTableNumber != currentStationaryDefaults.secondTableNumber
                || stationaryDefaults.thirdTableNumber != currentStationaryDefaults.thirdTableNumber || stationaryDefaults.antennaNumber != currentStationaryDefaults.antennaNumber
                || stationaryDefaults.scanTime != currentStationaryDefaults.scanTime || stationaryDefaults.scanTimeout != currentStationaryDefaults.scanTimeout
                || stationaryDefaults.storeRate != currentStationaryDefaults.storeRate || stationaryDefaults.referenceFrequency != currentStationaryDefaults.referenceFrequency
                || stationaryDefaults.referenceStoreRate != currentStationaryDefaults.referenceStoreRate || stationaryDefaults.dataTransferOn != currentStationaryDefaults.dataTransferOn;
    }
}