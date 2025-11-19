package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.StationaryDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class StationaryDefaultsActivity extends BaseActivity {

    @BindView(R.id.frequency_table_number_stationary_textView)
    TextView frequency_table_number_stationary_textView;
    @BindView(R.id.scan_rate_seconds_stationary_textView)
    TextView scan_rate_seconds_stationary_textView;
    @BindView(R.id.scan_timeout_seconds_stationary_textView)
    TextView scan_timeout_seconds_stationary_textView;
    @BindView(R.id.number_of_antennas_stationary_textView)
    TextView number_of_antennas_stationary_textView;
    @BindView(R.id.store_rate_minutes_stationary_textView)
    TextView store_rate_minutes_stationary_textView;
    @BindView(R.id.frequency_reference_stationary_textView)
    TextView frequency_reference_stationary_textView;
    @BindView(R.id.store_rate_stationary_imageView)
    ImageView store_rate_stationary_imageView;
    @BindView(R.id.reference_frequency_store_rate_stationary_textView)
    TextView reference_frequency_store_rate_stationary_textView;
    @BindView(R.id.store_rate_stationary_linearLayout)
    LinearLayout store_rate_stationary_linearLayout;
    @BindView(R.id.stationary_external_data_transfer_switch)
    SwitchCompat stationary_external_data_transfer_switch;
    @BindView(R.id.stationary_reference_frequency_switch)
    SwitchCompat stationary_reference_frequency_switch;
    @BindView(R.id.reference_frequency_stationary_linearLayout)
    LinearLayout reference_frequency_stationary_linearLayout;
    @BindView(R.id.reference_frequency_store_rate_stationary_linearLayout)
    LinearLayout reference_frequency_store_rate_stationary_linearLayout;

    private final static String TAG = StationaryDefaultsActivity.class.getSimpleName();

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
                    frequency_table_number_stationary_textView.setText(numbers.substring(0, numbers.length() - 2));
                } else {
                    int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                    switch (result.getResultCode()) {
                        case ValueCodes.SCAN_RATE_STATIONARY_CODE: // Get the modified scan rate
                            scan_rate_seconds_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Get the modified scan timeout
                            scan_timeout_seconds_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Get the modified number of antennas
                            number_of_antennas_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.STORE_RATE_CODE: // Get store rate
                            store_rate_minutes_stationary_textView.setText((value == 100) ? "Continuous Store" : String.valueOf(value));
                            break;
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE: // Get reference store rate
                            reference_frequency_store_rate_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.RESULT_OK: // Get reference frequency
                            frequency_reference_stationary_textView.setText(Converters.getFrequency(value));
                            break;
                    }
                }
            });

    /**
     * Writes the modified stationary defaults data by the user.
     */
    private void setStationaryDefaults() {
        String[] tables = frequency_table_number_stationary_textView.getText().toString().split(", ");
        int firstTableNumber = (tables.length > 0) ? Integer.parseInt(tables[0]) : 0;
        int secondTableNumber = (tables.length > 1) ? Integer.parseInt(tables[1]) : 0;
        int thirdTableNumber = (tables.length > 2) ? Integer.parseInt(tables[2]) : 0;
        int antennasNumber = number_of_antennas_stationary_textView.getText().toString().equals("None") ? 0 :
                Integer.parseInt(number_of_antennas_stationary_textView.getText().toString());
        int scanRate = Integer.parseInt(scan_rate_seconds_stationary_textView.getText().toString());
        int scanTimeout = Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString());
        int externalDataPush = stationary_external_data_transfer_switch.isChecked() ? 1 : 0;
        int storeRate;
        if ("Continuous Store".equals(store_rate_minutes_stationary_textView.getText().toString()))
            storeRate = 0;
        else
            storeRate = Integer.parseInt(store_rate_minutes_stationary_textView.getText().toString());
        int frequency = (stationary_reference_frequency_switch.isChecked()) ?
                (Converters.getFrequencyNumber(frequency_reference_stationary_textView.getText().toString()) - baseFrequency) : 0;
        int referenceFrequencyStoreRate = stationary_reference_frequency_switch.isChecked()
                ? Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString()) : 255;
        byte[] b = new byte[] {(byte) 0x4C, (byte) antennasNumber, (byte) externalDataPush, (byte) scanRate, (byte) scanTimeout,
                (byte) storeRate, (byte) (frequency / 256), (byte) (frequency % 256), (byte) referenceFrequencyStoreRate,
                (byte) firstTableNumber, (byte) secondTableNumber, (byte) thirdTableNumber};
        boolean result = TransferBleData.writeDefaults(false, b);
        if (result)
            Message.showMessage(this, 0);
        else
            Message.showMessage(this, 2);
    }

    @OnClick(R.id.frequency_table_number_stationary_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLES_NUMBER_CODE);
        intent.putExtra(ValueCodes.FIRST_TABLE_NUMBER, stationaryDefaults.firstTableNumber);
        intent.putExtra(ValueCodes.SECOND_TABLE_NUMBER, stationaryDefaults.secondTableNumber);
        intent.putExtra(ValueCodes.THIRD_TABLE_NUMBER, stationaryDefaults.thirdTableNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_stationary_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_STATIONARY_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.scanRate);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_timeout_seconds_stationary_linearLayout)
    public void onClickScanTimeoutSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_TIMEOUT_SECONDS_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.scanTimeout);
        launcher.launch(intent);
    }

    @OnClick(R.id.number_of_antennas_stationary_linearLayout)
    public void onClickNumberOfAntennas(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.NUMBER_OF_ANTENNAS_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.antennaNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.store_rate_stationary_linearLayout)
    public void onClickStoreRate(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.STORE_RATE_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.storeRate);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.stationary_reference_frequency_switch)
    public void onCheckedChangedReferenceFrequency(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            reference_frequency_stationary_linearLayout.setEnabled(true);
            frequency_reference_stationary_textView.setText(stationaryDefaults.referenceFrequency != 0 && stationaryDefaults.referenceFrequency != 255 ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : getString(R.string.lb_not_set));
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(true);
            reference_frequency_store_rate_stationary_textView.setText(stationaryDefaults.referenceStoreRate == 255 ? getString(R.string.lb_not_set) : String.valueOf(stationaryDefaults.referenceStoreRate));
        } else {
            reference_frequency_stationary_linearLayout.setEnabled(false);
            frequency_reference_stationary_textView.setText(R.string.lb_no_reference_frequency);
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(false);
            reference_frequency_store_rate_stationary_textView.setText(R.string.lb_no_reference_frequency);
        }
    }

    @OnClick(R.id.reference_frequency_stationary_linearLayout)
    public void onClickReferenceFrequency(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, "Reference Frequency");
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.reference_frequency_store_rate_stationary_linearLayout)
    public void onClickReferenceFrequencyStoreRate(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE);
        intent.putExtra(ValueCodes.VALUE, stationaryDefaults.referenceStoreRate);
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_stationary_defaults;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.stationary_defaults);
        super.onCreate(savedInstanceState);

        initializeCallback();
        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
        parameter = getIntent().getExtras().getString(ValueCodes.PARAMETER, "");
        if (parameter.isEmpty()) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.STATIONARY_DEFAULTS)) // Gets stationary defaults data
                    TransferBleData.readDefaults(false);
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                switch (Converters.getHexValue(packet[0])) {
                    case "88": // Battery
                        setBatteryPercent(packet);
                        break;
                    case "56": // Sd Card
                        setSdCardStatus(packet);
                        break;
                    case "6C": // Get stationary defaults data
                        downloadData(packet);
                        break;
                }
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!existNotSet()) {
                if (existChanges()) {
                    if (isDataCorrect())
                        setStationaryDefaults();
                    else
                        Message.showMessage(this, 1);
                } else {
                    finish();
                }
            } else {
                Message.showMessage(this, "Complete all fields.");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * With the received packet, gets stationary defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        parameter = "";
        if (!Converters.isDefaultEmpty(data)) {
            stationaryDefaults = new StationaryDefaults(baseFrequency, data);
            String tables = "";
            if (stationaryDefaults.firstTableNumber != 0)
                tables += stationaryDefaults.firstTableNumber;
            if (stationaryDefaults.secondTableNumber != 0)
                tables += ", " + stationaryDefaults.secondTableNumber;
            if (stationaryDefaults.thirdTableNumber != 0)
                tables += ", " + stationaryDefaults.thirdTableNumber;
            frequency_table_number_stationary_textView.setText(tables.isEmpty() ? "None" : tables);
            number_of_antennas_stationary_textView.setText((stationaryDefaults.antennaNumber == 0) ? "None" : String.valueOf(stationaryDefaults.antennaNumber));
            stationary_external_data_transfer_switch.setChecked(stationaryDefaults.dataTransferOn);
            scan_rate_seconds_stationary_textView.setText(stationaryDefaults.scanRate);
            scan_timeout_seconds_stationary_textView.setText(stationaryDefaults.scanTimeout);
            store_rate_minutes_stationary_textView.setText(stationaryDefaults.storeRate == 0 ? getString(R.string.lb_continuous_store) : String.valueOf(stationaryDefaults.storeRate));
            frequency_reference_stationary_textView.setText((stationaryDefaults.referenceFrequencyOn) ? Converters.getFrequency(stationaryDefaults.referenceFrequency) : "No Reference Frequency");
            reference_frequency_store_rate_stationary_textView.setText((stationaryDefaults.referenceFrequencyOn) ? String.valueOf(stationaryDefaults.referenceStoreRate) : "No Reference Frequency");
            stationary_reference_frequency_switch.setChecked(stationaryDefaults.referenceFrequencyOn);
        } else {
            frequency_table_number_stationary_textView.setText(R.string.lb_not_set);
            number_of_antennas_stationary_textView.setText(R.string.lb_not_set);
            stationary_external_data_transfer_switch.setChecked(true);
            scan_rate_seconds_stationary_textView.setText(R.string.lb_not_set);
            scan_timeout_seconds_stationary_textView.setText(R.string.lb_not_set);
            store_rate_minutes_stationary_textView.setText(R.string.lb_not_set);
            frequency_reference_stationary_textView.setText(R.string.lb_not_set);
        }
    }

    private boolean existNotSet() {
        if (frequency_table_number_stationary_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (number_of_antennas_stationary_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (scan_rate_seconds_stationary_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (scan_timeout_seconds_stationary_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (store_rate_minutes_stationary_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (frequency_reference_stationary_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (reference_frequency_store_rate_stationary_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        return false;
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        String[] tables = frequency_table_number_stationary_textView.getText().toString().split(", ");
        int firstTableNumber = (tables.length > 0) ? Integer.parseInt(tables[0]) : 0;
        int secondTableNumber = (tables.length > 1) ? Integer.parseInt(tables[1]) : 0;
        int thirdTableNumber = (tables.length > 2) ? Integer.parseInt(tables[2]) : 0;
        int antennaNumber = (number_of_antennas_stationary_textView.getText().toString().equals("None") ? 0 :
                Integer.parseInt(number_of_antennas_stationary_textView.getText().toString()));
        int scanRate = Integer.parseInt(scan_rate_seconds_stationary_textView.getText().toString());
        int timeout = Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString());
        int storeRate;
        if ("Continuous Store".equals(store_rate_minutes_stationary_textView.getText().toString()))
            storeRate = 0;
        else
            storeRate = Integer.parseInt(store_rate_minutes_stationary_textView.getText().toString());
        int referenceFrequency = stationary_reference_frequency_switch.isChecked() ?
                Converters.getFrequencyNumber(frequency_reference_stationary_textView.getText().toString()) : 0;
        int referenceFrequencyStoreRate = stationary_reference_frequency_switch.isChecked() ? Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString()) : 255;

        return stationaryDefaults.firstTableNumber != firstTableNumber || stationaryDefaults.secondTableNumber != secondTableNumber
                || stationaryDefaults.thirdTableNumber != thirdTableNumber || stationaryDefaults.antennaNumber != antennaNumber
                || stationaryDefaults.scanRate != scanRate || stationaryDefaults.scanTimeout != timeout
                || stationaryDefaults.storeRate != storeRate || stationaryDefaults.referenceFrequency != referenceFrequency
                || stationaryDefaults.referenceStoreRate != referenceFrequencyStoreRate
                || stationaryDefaults.dataTransferOn != stationary_external_data_transfer_switch.isChecked();
    }

    /**
     * Checks that the data is a valid and correct format.
     * @return Returns true, if the data is correct.
     */
    private boolean isDataCorrect() {
        boolean scanTimeCorrect = Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString())
                < Integer.parseInt(scan_rate_seconds_stationary_textView.getText().toString());
        boolean referenceFrequencyCorrect = !stationary_reference_frequency_switch.isChecked() || !frequency_reference_stationary_textView.getText().equals("0");
        return scanTimeCorrect && referenceFrequencyCorrect;
    }
}