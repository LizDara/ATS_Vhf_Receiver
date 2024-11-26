package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverStatus;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StationaryDefaultsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
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

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    Map<String, Object> originalData;
    private int baseFrequency;
    private int range;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize())
                finish();
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    showDisconnectionMessage(status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals(ValueCodes.SAVE)) // Save stationary defaults data
                        onClickSave();
                    else if (parameter.equals(ValueCodes.STATIONARY_DEFAULTS)) // Gets stationary defaults data
                        onClickStationaryDefaults();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    Log.i(TAG, Converters.getHexValue(packet));
                    if (packet == null) return;
                    if (parameter.equals(ValueCodes.STATIONARY_DEFAULTS)) // Gets stationary defaults data
                        downloadData(packet);
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                if (ValueCodes.TABLES_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                    int[] value = result.getData().getExtras().getIntArray(ValueCodes.VALUE);
                    String numbers = "";
                    for (int number : value)
                        numbers += number + ", ";
                    frequency_table_number_stationary_textView.setText(numbers.substring(0, numbers.length() - 2));
                } else {
                    int value = result.getData().getExtras().getInt(ValueCodes.VALUE);
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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * Requests a read for get stationary defaults data.
     */
    private void onClickStationaryDefaults() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Writes the modified stationary defaults data by the user.
     */
    private void onClickSave() {
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
        int referenceFrequencyStoreRate = reference_frequency_store_rate_stationary_textView.getText().toString().isEmpty() ? 0 : Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());
        byte[] b = new byte[] {(byte) 0x4C, (byte) antennasNumber, (byte) externalDataPush, (byte) scanRate, (byte) scanTimeout,
                (byte) storeRate, (byte) (frequency / 256), (byte) (frequency % 256), (byte) referenceFrequencyStoreRate,
                (byte) firstTableNumber, (byte) secondTableNumber, (byte) thirdTableNumber};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result)
            showMessage(0);
        else
            showMessage(2);
    }

    @OnClick(R.id.frequency_table_number_stationary_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLES_NUMBER_CODE);
        intent.putExtra(ValueCodes.FIRST_TABLE_NUMBER, (int) originalData.get(ValueCodes.FIRST_TABLE_NUMBER));
        intent.putExtra(ValueCodes.SECOND_TABLE_NUMBER, (int) originalData.get(ValueCodes.SECOND_TABLE_NUMBER));
        intent.putExtra(ValueCodes.THIRD_TABLE_NUMBER, (int) originalData.get(ValueCodes.THIRD_TABLE_NUMBER));
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_stationary_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_timeout_seconds_stationary_linearLayout)
    public void onClickScanTimeoutSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_TIMEOUT_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.number_of_antennas_stationary_linearLayout)
    public void onClickNumberOfAntennas(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.NUMBER_OF_ANTENNAS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.store_rate_stationary_linearLayout)
    public void onClickStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.STORE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.stationary_reference_frequency_switch)
    public void onCheckedChangedReferenceFrequency(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            reference_frequency_stationary_linearLayout.setEnabled(true);
            int frequency = (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY);
            frequency_reference_stationary_textView.setText(frequency != 0 ? Converters.getFrequency(frequency) : "0");
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(true);
            int storeRate = (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE);
            reference_frequency_store_rate_stationary_textView.setText(String.valueOf(storeRate));
        } else {
            reference_frequency_stationary_linearLayout.setEnabled(false);
            frequency_reference_stationary_textView.setText(R.string.lb_no_reference_frequency);
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(false);
            reference_frequency_store_rate_stationary_textView.setText("0");
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
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE);
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary_defaults);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.stationary_defaults);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        receiverInformation = ReceiverInformation.getReceiverInformation();
        ReceiverStatus.setReceiverStatus(this);

        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
        parameter = ValueCodes.STATIONARY_DEFAULTS;
        originalData = new HashMap<>();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (checkChanges()) {
                if (isDataCorrect()) {
                    parameter = ValueCodes.SAVE;
                    mBluetoothLeService.discovering();
                } else {
                    showMessage(1);
                }
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "Back Button Pressed");
    }

    private void showDisconnectionMessage(int status) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(view);
        dialog.show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD);
    }

    /**
     * With the received packet, gets stationary defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            parameter = "";
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
            int frequency = 0;
            if (!Converters.getDecimalValue(data[6]).equals("0") && !Converters.getDecimalValue(data[7]).equals("0"))
                frequency = (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                        Integer.parseInt(Converters.getDecimalValue(data[7])) + baseFrequency;
            //frequency_reference_stationary_textView.setText((frequency == 0) ? "No Reference Frequency" : Converters.getFrequency(frequency));
            //reference_frequency_store_rate_stationary_textView.setText(Converters.getDecimalValue(data[8]));

            originalData.put(ValueCodes.FIRST_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[9])));
            originalData.put(ValueCodes.SECOND_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[10])));
            originalData.put(ValueCodes.THIRD_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[11])));
            originalData.put(ValueCodes.ANTENNA_NUMBER, antennaNumber);
            originalData.put(ValueCodes.SCAN_RATE, Integer.parseInt(Converters.getDecimalValue(data[3])));
            originalData.put(ValueCodes.SCAN_TIMEOUT, Integer.parseInt(Converters.getDecimalValue(data[4])));
            originalData.put(ValueCodes.STORE_RATE, Integer.parseInt(Converters.getDecimalValue(data[5])));
            originalData.put(ValueCodes.EXTERNAL_DATA_TRANSFER, Integer.parseInt(Converters.getDecimalValue(data[2])));
            originalData.put(ValueCodes.REFERENCE_FREQUENCY, frequency);
            originalData.put(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE, Integer.parseInt(Converters.getDecimalValue(data[8])));
            stationary_reference_frequency_switch.setChecked(frequency != 0);
        }
    }

    /**
     * Displays a message indicating whether the writing was successful.
     * @param status This number indicates the writing status.
     */
    private void showMessage(int status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message!");
        switch (status) {
            case 0:
                builder.setMessage("Completed.");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    finish();
                });
                break;
            case 1:
                builder.setMessage("Data incorrect.");
                builder.setPositiveButton("OK", null);
                break;
            case 2:
                builder.setMessage("Not completed.");
                builder.setPositiveButton("OK", null);
                break;
        }
        builder.show();
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean checkChanges() {
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
        int referenceFrequencyStoreRate = reference_frequency_store_rate_stationary_textView.getText().toString().isEmpty() ? 0 : Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());

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