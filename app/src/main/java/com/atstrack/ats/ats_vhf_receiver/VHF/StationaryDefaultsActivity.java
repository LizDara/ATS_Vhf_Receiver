package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.HashMap;
import java.util.Map;

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

    private Map<String, Object> originalData;
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
                        case ValueCodes.SCAN_RATE_SECONDS_CODE: // Gets the modified scan rate
                            scan_rate_seconds_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Gets the modified scan timeout
                            scan_timeout_seconds_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Gets the modified number of antennas
                            number_of_antennas_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.STORE_RATE_CODE:
                            store_rate_minutes_stationary_textView.setText((value == 100) ? "Continuous Store" : String.valueOf(value));
                            break;
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE:
                            reference_frequency_store_rate_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.RESULT_OK:
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
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLES_NUMBER_CODE);
        intent.putExtra(ValueCodes.FIRST_TABLE_NUMBER, (int) originalData.get(ValueCodes.FIRST_TABLE_NUMBER));
        intent.putExtra(ValueCodes.SECOND_TABLE_NUMBER, (int) originalData.get(ValueCodes.SECOND_TABLE_NUMBER));
        intent.putExtra(ValueCodes.THIRD_TABLE_NUMBER, (int) originalData.get(ValueCodes.THIRD_TABLE_NUMBER));
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_stationary_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_timeout_seconds_stationary_linearLayout)
    public void onClickScanTimeoutSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_TIMEOUT_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.number_of_antennas_stationary_linearLayout)
    public void onClickNumberOfAntennas(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.NUMBER_OF_ANTENNAS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.store_rate_stationary_linearLayout)
    public void onClickStoreRate(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.STORE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.stationary_reference_frequency_switch)
    public void onCheckedChangedReferenceFrequency(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            reference_frequency_stationary_linearLayout.setEnabled(true);
            int frequency = (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY);
            frequency_reference_stationary_textView.setText(frequency != 0 ? Converters.getFrequency(frequency) : getString(R.string.lb_not_set));
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(true);
            int storeRate = (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE);
            reference_frequency_store_rate_stationary_textView.setText(storeRate == 255 ? getString(R.string.lb_not_set) : String.valueOf(storeRate));
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
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE);
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
        originalData = new HashMap<>();
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
                if (parameter.equals(ValueCodes.SAVE)) // Save stationary defaults data
                    setStationaryDefaults();
                else if (parameter.equals(ValueCodes.STATIONARY_DEFAULTS)) // Gets stationary defaults data
                    TransferBleData.readDefaults(false);
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                if (Converters.getHexValue(packet[0]).equals("88")) // Battery
                    setBatteryPercent(packet);
                else if (Converters.getHexValue(packet[0]).equals("56")) // Sd Card
                    setSdCardStatus(packet);
                else if (parameter.equals(ValueCodes.STATIONARY_DEFAULTS)) // Gets stationary defaults data
                    downloadData(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!existNotSet()) {
                if (existChanges()) {
                    if (isDataCorrect()) {
                        parameter = ValueCodes.SAVE;
                        leServiceConnection.getBluetoothLeService().discovering();
                    } else {
                        Message.showMessage(this, 1);
                    }
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

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 33)
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter(), 2);
        else
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
    }

    /**
     * With the received packet, gets stationary defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            parameter = "";
            int frequency = 0;
            if (!Converters.isDefaultEmpty(data)) {
                String tables = "";
                for (int i = 9; i < data.length; i++) {
                    if (data[i] != 0)
                        tables += Converters.getDecimalValue(data[i]) + ", ";
                }
                frequency_table_number_stationary_textView.setText(tables.isEmpty() ? "None" : tables.substring(0, tables.length() - 2));
                int antennaNumber = Integer.parseInt(Converters.getDecimalValue(data[1]));
                number_of_antennas_stationary_textView.setText((antennaNumber == 0) ? "None" : String.valueOf(antennaNumber));
                stationary_external_data_transfer_switch.setChecked(data[2] != 0);
                scan_rate_seconds_stationary_textView.setText(Converters.getDecimalValue(data[3]));
                scan_timeout_seconds_stationary_textView.setText(Converters.getDecimalValue(data[4]));
                if (Converters.getHexValue(data[5]).equals("00"))
                    store_rate_minutes_stationary_textView.setText(R.string.lb_continuous_store);
                else
                    store_rate_minutes_stationary_textView.setText(Converters.getDecimalValue(data[5]));
                if (!Converters.getHexValue(data[6]).equals("FF") && !Converters.getHexValue(data[7]).equals("FF")
                        && !Converters.getHexValue(data[6]).equals("00") && !Converters.getHexValue(data[7]).equals("00"))
                    frequency = (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(data[7])) + baseFrequency;
                //frequency_reference_stationary_textView.setText((frequency == 0) ? "No Reference Frequency" : Converters.getFrequency(frequency));
                //reference_frequency_store_rate_stationary_textView.setText(Converters.getDecimalValue(data[8]));
            } else {
                frequency_table_number_stationary_textView.setText(getString(R.string.lb_not_set));
                number_of_antennas_stationary_textView.setText(getString(R.string.lb_not_set));
                stationary_external_data_transfer_switch.setChecked(true);
                scan_rate_seconds_stationary_textView.setText(getString(R.string.lb_not_set));
                scan_timeout_seconds_stationary_textView.setText(getString(R.string.lb_not_set));
                store_rate_minutes_stationary_textView.setText(getString(R.string.lb_not_set));
            }
            originalData.put(ValueCodes.FIRST_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[9])));
            originalData.put(ValueCodes.SECOND_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[10])));
            originalData.put(ValueCodes.THIRD_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[11])));
            originalData.put(ValueCodes.ANTENNA_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[1])));
            originalData.put(ValueCodes.SCAN_RATE, Integer.parseInt(Converters.getDecimalValue(data[3])));
            originalData.put(ValueCodes.SCAN_TIMEOUT, Integer.parseInt(Converters.getDecimalValue(data[4])));
            originalData.put(ValueCodes.STORE_RATE, Integer.parseInt(Converters.getDecimalValue(data[5])));
            originalData.put(ValueCodes.EXTERNAL_DATA_TRANSFER, Integer.parseInt(Converters.getDecimalValue(data[2])));
            originalData.put(ValueCodes.REFERENCE_FREQUENCY, frequency);
            originalData.put(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE, Integer.parseInt(Converters.getDecimalValue(data[8])));
            stationary_reference_frequency_switch.setChecked(frequency != 0);
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6C ...");
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
        int externalData = stationary_external_data_transfer_switch.isChecked() ? 1 : 0;
        int storeRate;
        if ("Continuous Store".equals(store_rate_minutes_stationary_textView.getText().toString()))
            storeRate = 0;
        else
            storeRate = Integer.parseInt(store_rate_minutes_stationary_textView.getText().toString());
        int referenceFrequency = stationary_reference_frequency_switch.isChecked() ?
                Converters.getFrequencyNumber(frequency_reference_stationary_textView.getText().toString()) : 0;
        int referenceFrequencyStoreRate = stationary_reference_frequency_switch.isChecked() ? Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString()) : 0;

        return (int) originalData.get(ValueCodes.FIRST_TABLE_NUMBER) != firstTableNumber || (int) originalData.get(ValueCodes.SECOND_TABLE_NUMBER) != secondTableNumber
                || (int) originalData.get(ValueCodes.THIRD_TABLE_NUMBER) != thirdTableNumber || (int) originalData.get(ValueCodes.ANTENNA_NUMBER) != antennaNumber
                || (int) originalData.get(ValueCodes.SCAN_RATE) != scanRate || (int) originalData.get(ValueCodes.SCAN_TIMEOUT) != timeout
                || (int) originalData.get(ValueCodes.STORE_RATE) != storeRate || (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY) != referenceFrequency
                || (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE) != referenceFrequencyStoreRate
                || (int) originalData.get(ValueCodes.EXTERNAL_DATA_TRANSFER) != externalData;
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