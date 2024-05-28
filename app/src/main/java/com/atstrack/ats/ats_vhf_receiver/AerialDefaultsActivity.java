package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AerialDefaultsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.device_name_textView)
    TextView device_name_textView;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
    @BindView(R.id.percent_battery_textView)
    TextView percent_battery_textView;
    @BindView(R.id.frequency_table_number_aerial_textView)
    TextView frequency_table_number_aerial_textView;
    @BindView(R.id.scan_rate_seconds_aerial_textView)
    TextView scan_rate_seconds_aerial_textView;
    @BindView(R.id.aerial_gps_switch)
    SwitchCompat aerial_gps_switch;
    @BindView(R.id.aerial_auto_record_switch)
    SwitchCompat aerial_auto_record_switch;

    private final static String TAG = AerialDefaultsActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    Map<String, Object> originalData;

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
                Log.i(TAG, action + " " + mConnected);
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    Log.i(TAG,"Connect in broadcast: " + mConnected);
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals("save")) { // Save aerial defaults data
                        onClickSave();
                    } else if (parameter.equals("aerial")) { // Gets aerial defaults data
                        onClickAerialDefaults();
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("aerial")) // Gets aerial defaults data
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
                int value = result.getData().getExtras().getInt(ValueCodes.VALUE);
                if (ValueCodes.FREQUENCY_TABLE_NUMBER == result.getResultCode()) { // Gets the modified frequency table number
                    frequency_table_number_aerial_textView.setText(String.valueOf(value));
                } else if (ValueCodes.SCAN_RATE_SECONDS == result.getResultCode()) { // Gets the modified scan rate
                    scan_rate_seconds_aerial_textView.setText(String.valueOf(value * 0.1));
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
     * Requests a read for get aerial defaults data.
     * Service name: Scan.
     * Characteristic name: Aerial.
     */
    private void onClickAerialDefaults() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Writes the modified aerial defaults data by the user.
     * Service name: Scan.
     * Characteristic name: Aerial.
     */
    private void onClickSave() {
        int info = (aerial_gps_switch.isChecked() ? 1 : 0) << 7;
        info = info | ((aerial_auto_record_switch.isChecked() ? 1 : 0) << 6);
        float scanRate = Float.parseFloat(scan_rate_seconds_aerial_textView.getText().toString());
        int frequencyTableNumber = (frequency_table_number_aerial_textView.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
        byte[] b = new byte[] {(byte) 0x4D, (byte) frequencyTableNumber, (byte) info, (byte) (scanRate * 10), 0, 0, 0, 0};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result)
            showMessage(0);
        else
            showMessage(2);
    }

    @OnClick(R.id.frequency_table_number_aerial_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("parameter", "tables");
        intent.putExtra("type", ValueCodes.FREQUENCY_TABLE_NUMBER);
        intent.putExtra("table", (int) originalData.get("TableNumber"));
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_aerial_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("parameter", "aerial");
        intent.putExtra("type", ValueCodes.SCAN_RATE_SECONDS);
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerial_defaults);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.aerial_defaults);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();
        parameter = "aerial";

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

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
        if (!mConnected) {
            showDisconnectionMessage();
        }
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
        Log.i(TAG,"Connect in message: " + mConnected);
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
     * With the received packet, gets aerial defaults data.
     *
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6D")) {
            parameter = "";
            Log.i(TAG, "Parameter: " + parameter);
            frequency_table_number_aerial_textView.setText(
                    (Converters.getDecimalValue(data[1]).equals("0")) ? "None" : Converters.getDecimalValue(data[1]));
            int gps = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 7 & 1;
            aerial_gps_switch.setChecked(gps == 1);
            int autoRecord = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1;
            aerial_auto_record_switch.setChecked(autoRecord == 1);
            double scanRate = (double) (Integer.parseInt(Converters.getDecimalValue(data[3])) * 0.1);
            scan_rate_seconds_aerial_textView.setText(String.valueOf(scanRate));
            Log.i(TAG, "Showed Data");

            originalData.put("TableNumber", Integer.parseInt(Converters.getDecimalValue(data[1])));
            originalData.put("Gps", gps == 1);
            originalData.put("AutoRecord", autoRecord == 1);
            originalData.put("ScanTime", scanRate);
            Log.i(TAG, "Saved Data");
        }
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
        int tableNumber = (frequency_table_number_aerial_textView.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
        double scanRate = Double.parseDouble(scan_rate_seconds_aerial_textView.getText().toString());

        return (int) originalData.get("TableNumber") != tableNumber || (boolean) originalData.get("Gps") != aerial_gps_switch.isChecked()
                || (boolean) originalData.get("AutoRecord") != aerial_auto_record_switch.isChecked()
                || (double) originalData.get("ScanTime") != scanRate;
    }

    /**
     * Checks that the data is a valid and correct format.
     *
     * @return Returns true, if the data is correct.
     */
    private boolean isDataCorrect() {
        return true;
    }
}
