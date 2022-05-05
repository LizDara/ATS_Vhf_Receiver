package com.atstrack.ats.ats_vhf_receiver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.util.UUID;

public class StationaryDefaultsActivity extends AppCompatActivity {

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
    @BindView(R.id.frequency_table_number_stationary_textView)
    TextView frequency_table_number_stationary_textView;
    @BindView(R.id.scan_rate_seconds_stationary_textView)
    TextView scan_rate_seconds_stationary_textView;
    @BindView(R.id.scan_timeout_seconds_stationary_textView)
    TextView scan_timeout_seconds_stationary_textView;
    @BindView(R.id.number_of_antennas_stationary_textView)
    TextView number_of_antennas_stationary_textView;
    @BindView(R.id.store_rate_stationary_textView)
    TextView store_rate_stationary_textView;
    @BindView(R.id.frequency_reference_stationary_textView)
    TextView frequency_reference_stationary_textView;
    @BindView(R.id.store_rate_stationary_imageView)
    ImageView store_rate_stationary_imageView;
    @BindView(R.id.reference_frequency_store_rate_stationary_textView)
    TextView reference_frequency_store_rate_stationary_textView;
    @BindView(R.id.store_rate_stationary_linearLayout)
    LinearLayout store_rate_stationary_linearLayout;

    private final static String TAG = StationaryDefaultsActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    private int[] data;

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
                    else if (parameter.equals("save")) // Save stationary defaults data
                        showMessage(packet);
                }
            }
            catch (Exception e) {
                Timber.tag("DCA:BR 198").e(e, "Unexpected error.");
            }
        }
    };

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
        int info = (Integer.parseInt(frequency_table_number_stationary_textView.getText().toString()) * 16)
                + ((number_of_antennas_stationary_textView.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(number_of_antennas_stationary_textView.getText().toString()));
        int scanRate = Integer.parseInt(scan_rate_seconds_stationary_textView.getText().toString());
        int scanTimeout = Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString());
        int storeRate;
        switch (store_rate_stationary_textView.getText().toString()) {
            case "No Store Rate":
                storeRate = 0;
                break;
            case "Continuous Store":
                storeRate = 255;
                break;
            default:
                storeRate = Integer.parseInt(store_rate_stationary_textView.getText().toString());
                break;
        }
        int frequency = (frequency_reference_stationary_textView.getText().toString().equals("No Reference Frequency")) ?
                0 : (Integer.parseInt(frequency_reference_stationary_textView.getText().toString()) % 150000);
        int referenceFrequencyStoreRate = Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());
        byte[] b = new byte[]{(byte) 0x4C, (byte) info, (byte) 0x0, (byte) scanRate, (byte) scanTimeout, (byte) storeRate,
                (byte) (frequency / 256), (byte) (frequency % 256), (byte) referenceFrequencyStoreRate, (byte) 0x0, (byte) 0x0};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, false);

        finish();
    }

    @OnClick(R.id.frequency_table_number_stationary_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("type", "stationary");
        intent.putExtra("value", InputValueActivity.FREQUENCY_TABLE_NUMBER);
        startActivityForResult(intent, InputValueActivity.FREQUENCY_TABLE_NUMBER);
    }

    @OnClick(R.id.scan_rate_seconds_stationary_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("type", "stationary");
        intent.putExtra("value", InputValueActivity.SCAN_RATE_SECONDS);
        startActivityForResult(intent, InputValueActivity.SCAN_RATE_SECONDS);
    }

    @OnClick(R.id.scan_timeout_seconds_stationary_linearLayout)
    public void onClickScanTimeoutSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("type", "stationary");
        intent.putExtra("value", InputValueActivity.SCAN_TIMEOUT_SECONDS);
        startActivityForResult(intent, InputValueActivity.SCAN_TIMEOUT_SECONDS);
    }

    @OnClick(R.id.number_of_antennas_stationary_linearLayout)
    public void onClickNumberOfAntennas(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("type", "stationary");
        intent.putExtra("value", InputValueActivity.NUMBER_OF_ANTENNAS);
        startActivityForResult(intent, InputValueActivity.NUMBER_OF_ANTENNAS);
    }

    @OnClick(R.id.store_rate_stationary_linearLayout)
    public void onClickStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("type", "stationary");
        intent.putExtra("value", InputValueActivity.STORE_RATE);
        startActivityForResult(intent, InputValueActivity.STORE_RATE);
    }

    /*@OnClick(R.id.reference_frequency_stationary_linearLayout)
    public void onClickReferenceFrequency(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(InputValueActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        intent.putExtra(InputValueActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        intent.putExtra(InputValueActivity.EXTRAS_DEVICE_STATUS, mDeviceStatus);
        intent.putExtra(InputValueActivity.EXTRAS_BATTERY, mPercentBattery);
        intent.putExtra("type", "stationary");
        intent.putExtra("value", InputValueActivity.REFERENCE_FREQUENCY);
        startActivityForResult(intent, InputValueActivity.REFERENCE_FREQUENCY);
    }*/

    @OnClick(R.id.reference_frequency_store_rate_stationary_linearLayout)
    public void onClickReferenceFrequencyStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra("type", "stationary");
        intent.putExtra("value", InputValueActivity.REFERENCE_FREQUENCY_STORE_RATE);
        startActivityForResult(intent, InputValueActivity.REFERENCE_FREQUENCY_STORE_RATE);
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

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0)
            return;
        switch (requestCode) {
            case InputValueActivity.FREQUENCY_TABLE_NUMBER:  // Gets the modified frequency table number
                frequency_table_number_stationary_textView.setText(String.valueOf(resultCode));
                break;
            case InputValueActivity.SCAN_RATE_SECONDS:  // Gets the modified scan rate
                scan_rate_seconds_stationary_textView.setText(String.valueOf(resultCode));
                break;
            case InputValueActivity.SCAN_TIMEOUT_SECONDS:  // Gets the modified scan timeout
                scan_timeout_seconds_stationary_textView.setText(String.valueOf(resultCode));
                break;
            case InputValueActivity.NUMBER_OF_ANTENNAS:  // Gets the modified number of antennas
                number_of_antennas_stationary_textView.setText(String.valueOf(resultCode));
                break;
            case InputValueActivity.STORE_RATE:
                store_rate_stationary_textView.setText((resultCode == 100) ? "No Store Rate" : String.valueOf(resultCode));
                break;
            case InputValueActivity.REFERENCE_FREQUENCY_STORE_RATE:
                reference_frequency_store_rate_stationary_textView.setText(String.valueOf(resultCode));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (checkChanges()) {
                if (isDataCorrect()) {
                    parameter = "save";
                    mBluetoothLeService.discovering();
                } else {
                    showMessage(new byte[]{(byte) 1});
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
            parameter = "";
            int frequencyTable = Integer.parseInt(Converters.getDecimalValue(data[1])) / 16;
            frequency_table_number_stationary_textView.setText(
                    (frequencyTable == 0) ? "None" : String.valueOf(frequencyTable));
            int antennaNumber = Integer.parseInt(Converters.getDecimalValue(data[1])) % 16;
            number_of_antennas_stationary_textView.setText((antennaNumber == 0) ? "None" : String.valueOf(antennaNumber));
            scan_rate_seconds_stationary_textView.setText(Converters.getDecimalValue(data[3]));
            scan_timeout_seconds_stationary_textView.setText(Converters.getDecimalValue(data[4]));
            if (Converters.getHexValue(data[5]).equals("FF")) {
                store_rate_stationary_textView.setText("Continuous Store");
                store_rate_stationary_imageView.setVisibility(View.GONE);
                store_rate_stationary_linearLayout.setEnabled(false);
            } else {
                store_rate_stationary_textView.setText((Converters.getDecimalValue(data[5]).equals("0")) ? "No Store Rate" :
                        Converters.getDecimalValue(data[5]));
                store_rate_stationary_imageView.setVisibility(View.VISIBLE);
                store_rate_stationary_linearLayout.setEnabled(true);
            }
            int frequency;
            if (Converters.getDecimalValue(data[6]).equals("0") && Converters.getDecimalValue(data[7]).equals("0")) {
                frequency = 0;
            } else {
                frequency = (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                        Integer.parseInt(Converters.getDecimalValue(data[7])) + 150000;
            }
            frequency_reference_stationary_textView.setText((frequency == 0) ? "No Reference Frequency" : String.valueOf(frequency));
            reference_frequency_store_rate_stationary_textView.setText(Converters.getDecimalValue(data[8]));
            this.data = new int[]{frequencyTable, antennaNumber, Integer.parseInt(Converters.getDecimalValue(data[3])),
                    Integer.parseInt(Converters.getDecimalValue(data[4])), Integer.parseInt(Converters.getDecimalValue(data[5])),
                    frequency, Integer.parseInt(Converters.getDecimalValue(data[8]))};
        }
    }

    /**
     * Displays a message indicating whether the writing was successful.
     *
     * @param data This packet indicates the writing status.
     */
    private void showMessage(byte[] data) {
        int status = Integer.parseInt(Converters.getDecimalValue(data[0]));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message!");
        if (status == 0) {
            builder.setMessage("Completed.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                finish();
            });
        }
        if (status == 1) {
            builder.setMessage("Data incorrect.");
            builder.setPositiveButton("OK", null);
        }
        builder.show();
    }

    /**
     * Checks for changes to the default data.
     *
     * @return Returns true, if there are changes.
     */
    private boolean checkChanges() {
        int frequencyTable = (frequency_table_number_stationary_textView.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(frequency_table_number_stationary_textView.getText().toString());
        int antennaNumber = (number_of_antennas_stationary_textView.getText().toString().equals("None") ? 0 :
                Integer.parseInt(number_of_antennas_stationary_textView.getText().toString()));
        int scanRate = Integer.parseInt(scan_rate_seconds_stationary_textView.getText().toString());
        int timeout = Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString());
        int storeRate;
        switch (store_rate_stationary_textView.getText().toString()) {
            case "No Store Rate":
                storeRate = 0;
                break;
            case "Continuous Store":
                storeRate = 255;
                break;
            default:
                storeRate = Integer.parseInt(store_rate_stationary_textView.getText().toString());
                break;
        }
        int frequency = (frequency_reference_stationary_textView.getText().toString().equals("No Reference Frequency"))
                ? 0 : Integer.parseInt(frequency_reference_stationary_textView.getText().toString());
        int referenceFrequencyStoreRate = Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());
        return frequencyTable != data[0] || antennaNumber != data[1] || scanRate != data[2] || timeout != data[3]
                || storeRate != data[4] || frequency != data[5] || referenceFrequencyStoreRate != data[6];
    }

    /**
     * Checks that the data is a valid and correct format.
     *
     * @return Returns true, if the data is correct.
     */
    private boolean isDataCorrect() {
        return Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString())
                < Integer.parseInt(scan_rate_seconds_stationary_textView.getText().toString());
    }
}
