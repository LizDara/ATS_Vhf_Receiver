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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StationaryDefaultsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
    @BindView(R.id.device_range_textView)
    TextView device_range_textView;
    @BindView(R.id.percent_battery_textView)
    TextView percent_battery_textView;
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

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = true;
    private String parameter = "";

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals("save")) { // Save stationary defaults data
                        onClickSave();
                    } else if (parameter.equals("stationary")) { // Gets stationary defaults data
                        onClickStationaryDefaults();
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("stationary")) // Gets stationary defaults data
                        downloadData(packet);
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                else if (ValueCodes.TABLES_NUMBER == result.getResultCode()) { // Gets the modified frequency table number
                    int[] value = result.getData().getExtras().getIntArray(ValueCodes.VALUE);
                    String numbers = "";
                    for (int number : value)
                        numbers += number + ", ";
                    frequency_table_number_stationary_textView.setText(numbers.substring(0, numbers.length() - 2));
                } else {
                    int value = result.getData().getExtras().getInt(ValueCodes.VALUE);
                    switch (result.getResultCode()) {
                        case ValueCodes.SCAN_RATE_SECONDS: // Gets the modified scan rate
                            scan_rate_seconds_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.SCAN_TIMEOUT_SECONDS: // Gets the modified scan timeout
                            scan_timeout_seconds_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.NUMBER_OF_ANTENNAS: // Gets the modified number of antennas
                            number_of_antennas_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.STORE_RATE:
                            store_rate_minutes_stationary_textView.setText((value == 100) ? "Continuous Store" : String.valueOf(value));
                            break;
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE:
                            reference_frequency_store_rate_stationary_textView.setText(String.valueOf(value));
                            break;
                        case ValueCodes.RESULT_OK:
                            frequency_reference_stationary_textView.setText(getFrequency(value));
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
     * Service name: Scan.
     * Characteristic name: Stationary.
     */
    private void onClickStationaryDefaults() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Writes the modified stationary defaults data by the user.
     * Service name: Scan.
     * Characteristic name: Stationary.
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
        if ("Continuous Store".equals(store_rate_minutes_stationary_textView.getText().toString())) {
            storeRate = 0;
        } else {
            storeRate = Integer.parseInt(store_rate_minutes_stationary_textView.getText().toString());
        }
        int frequency = (stationary_reference_frequency_switch.isChecked()) ?
                (getFrequencyNumber(frequency_reference_stationary_textView.getText().toString()) - baseFrequency) : 0;
        int referenceFrequencyStoreRate = Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());
        byte[] b = new byte[] {(byte) 0x4C, (byte) antennasNumber, (byte) externalDataPush, (byte) scanRate, (byte) scanTimeout,
                (byte) storeRate, (byte) (frequency / 256), (byte) (frequency % 256), (byte) referenceFrequencyStoreRate,
                (byte) firstTableNumber, (byte) secondTableNumber, (byte) thirdTableNumber};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);
        Log.i(TAG, "Saved: " + Converters.getHexValue(b));

        if (result)
            showMessage(0);
        else
            showMessage(2);
    }

    @OnClick(R.id.frequency_table_number_stationary_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("parameter", "tables");
        intent.putExtra("type", ValueCodes.TABLES_NUMBER);
        intent.putExtra("firstTable", (int) originalData.get("FirstTableNumber"));
        intent.putExtra("secondTable", (int) originalData.get("SecondTableNumber"));
        intent.putExtra("thirdTable", (int) originalData.get("ThirdTableNumber"));
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_stationary_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("parameter", "stationary");
        intent.putExtra("type", ValueCodes.SCAN_RATE_SECONDS);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_timeout_seconds_stationary_linearLayout)
    public void onClickScanTimeoutSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("parameter", "stationary");
        intent.putExtra("type", ValueCodes.SCAN_TIMEOUT_SECONDS);
        launcher.launch(intent);
    }

    @OnClick(R.id.number_of_antennas_stationary_linearLayout)
    public void onClickNumberOfAntennas(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("parameter", "stationary");
        intent.putExtra("type", ValueCodes.NUMBER_OF_ANTENNAS);
        launcher.launch(intent);
    }

    @OnClick(R.id.store_rate_stationary_linearLayout)
    public void onClickStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("parameter", "stationary");
        intent.putExtra("type", ValueCodes.STORE_RATE);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.stationary_reference_frequency_switch)
    public void onCheckedChangedReferenceFrequency(CompoundButton button, boolean isChecked) {
        Log.i(TAG, "Change State Reference");
        if (isChecked) {
            reference_frequency_stationary_linearLayout.setEnabled(true);
            int frequency = (int) originalData.get("ReferenceFrequency");
            frequency_reference_stationary_textView.setText(frequency != 0 ? getFrequency(frequency) : "0");
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(true);
        } else {
            Log.i(TAG, "No Reference");
            reference_frequency_stationary_linearLayout.setEnabled(false);
            frequency_reference_stationary_textView.setText("No Reference Frequency");
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(false);
            reference_frequency_store_rate_stationary_textView.setText("0");
        }
    }

    @OnClick(R.id.reference_frequency_stationary_linearLayout)
    public void onClickReferenceFrequency(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra("title", "Reference Frequency");
        intent.putExtra("baseFrequency", baseFrequency);
        intent.putExtra("range", range);
        launcher.launch(intent);
    }

    @OnClick(R.id.reference_frequency_store_rate_stationary_linearLayout)
    public void onClickReferenceFrequencyStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("parameter", "stationary");
        intent.putExtra("type", ValueCodes.REFERENCE_FREQUENCY_STORE_RATE);
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary_defaults);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.stationary_defaults);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();
        parameter = "stationary";

        device_status_textView.setText(receiverInformation.getDeviceStatus());
        device_range_textView.setText(receiverInformation.getDeviceRange());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        baseFrequency = sharedPreferences.getInt("BaseFrequency", 0) * 1000;
        range = sharedPreferences.getInt("Range", 0);

        originalData = new HashMap();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (checkChanges()) {
                if (isDataCorrect()) {
                    parameter = "save";
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
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
            Log.i(TAG,"Connect request result= " + result);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected)
            showDisconnectionMessage();
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "Back Button Pressed");
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        dialog.setView(view);
        dialog.show();

        // The message disappears after a pre-defined period and will search for other available BLE devices again
        int MESSAGE_PERIOD = 3000;
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, MESSAGE_PERIOD);
    }

    /**
     * With the received packet, gets stationary defaults data.
     *
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            Log.i(TAG, Converters.getHexValue(data));
            parameter = "";
            String tables = "";
            for (int i = 9; i < data.length; i++) {
                if (data[i] != 0)
                    tables += Converters.getDecimalValue(data[i]) + ", ";
            }
            frequency_table_number_stationary_textView.setText(tables.equals("") ? "None" : tables.substring(0, tables.length() - 2));
            int antennaNumber = Integer.parseInt(Converters.getDecimalValue(data[1]));
            number_of_antennas_stationary_textView.setText((antennaNumber == 0) ? "None" : String.valueOf(antennaNumber));
            stationary_external_data_transfer_switch.setChecked(data[2] != 0);
            scan_rate_seconds_stationary_textView.setText(Converters.getDecimalValue(data[3]));
            scan_timeout_seconds_stationary_textView.setText(Converters.getDecimalValue(data[4]));
            if (Converters.getHexValue(data[5]).equals("00")) {
                store_rate_minutes_stationary_textView.setText("Continuous Store");
            } else {
                store_rate_minutes_stationary_textView.setText(Converters.getDecimalValue(data[5]));
            }
            int frequency = 0;
            if (!Converters.getDecimalValue(data[6]).equals("0") && !Converters.getDecimalValue(data[7]).equals("0")) {
                frequency = (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                        Integer.parseInt(Converters.getDecimalValue(data[7])) + baseFrequency;
            }
            stationary_reference_frequency_switch.setChecked(frequency != 0);
            frequency_reference_stationary_textView.setText((frequency == 0) ? "No Reference Frequency" : getFrequency(frequency));
            reference_frequency_store_rate_stationary_textView.setText(Converters.getDecimalValue(data[8]));

            originalData.put("FirstTableNumber", Integer.parseInt(Converters.getDecimalValue(data[9])));
            originalData.put("SecondTableNumber", Integer.parseInt(Converters.getDecimalValue(data[10])));
            originalData.put("ThirdTableNumber", Integer.parseInt(Converters.getDecimalValue(data[11])));
            originalData.put("AntennaNumber", antennaNumber);
            originalData.put("ScanTime", Integer.parseInt(Converters.getDecimalValue(data[3])));
            originalData.put("ScanTimeout", Integer.parseInt(Converters.getDecimalValue(data[4])));
            originalData.put("StoreRate", Integer.parseInt(Converters.getDecimalValue(data[5])));
            originalData.put("ExternalDataTransfer", Integer.parseInt(Converters.getDecimalValue(data[2])));
            originalData.put("ReferenceFrequency", frequency);
            originalData.put("ReferenceFrequencyStoreRate", Integer.parseInt(Converters.getDecimalValue(data[8])));
        }
    }

    private String getFrequency(int frequency) {
        return String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3);
    }

    private int getFrequencyNumber(String frequency) {
        return Integer.parseInt(frequency.replace(".", ""));
    }

    /**
     * Displays a message indicating whether the writing was successful.
     *
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
     *
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
        if ("Continuous Store".equals(store_rate_minutes_stationary_textView.getText().toString())) {
            storeRate = 0;
        } else {
            storeRate = Integer.parseInt(store_rate_minutes_stationary_textView.getText().toString());
        }
        int referenceFrequency = stationary_reference_frequency_switch.isChecked() ?
                getFrequencyNumber(frequency_reference_stationary_textView.getText().toString()) : 0;
        int referenceFrequencyStoreRate = Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());

        return (int) originalData.get("FirstTableNumber") != firstTableNumber || (int) originalData.get("SecondTableNumber") != secondTableNumber
                || (int) originalData.get("ThirdTableNumber") != thirdTableNumber || (int) originalData.get("AntennaNumber") != antennaNumber
                || (int) originalData.get("ScanTime") != scanRate || (int) originalData.get("ScanTimeout") != timeout
                || (int) originalData.get("StoreRate") != storeRate || (int) originalData.get("ReferenceFrequency") != referenceFrequency
                || (int) originalData.get("ReferenceFrequencyStoreRate") != referenceFrequencyStoreRate
                || (int) originalData.get("ExternalDataTransfer") != externalData;
    }

    /**
     * Checks that the data is a valid and correct format.
     *
     * @return Returns true, if the data is correct.
     */
    private boolean isDataCorrect() {
        boolean scanTimeCorrect = Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString())
                < Integer.parseInt(scan_rate_seconds_stationary_textView.getText().toString());
        boolean referenceFrequencyCorrect = !stationary_reference_frequency_switch.isChecked() || !frequency_reference_stationary_textView.getText().equals("0");
        return scanTimeCorrect && referenceFrequencyCorrect;
    }
}
