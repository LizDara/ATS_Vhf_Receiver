package com.atstrack.ats.ats_vhf_receiver;

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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InputValueActivity extends AppCompatActivity {

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
    @BindView(R.id.value_spinner)
    Spinner value_spinner;
    @BindView(R.id.set_value_linearLayout)
    LinearLayout set_value_linearLayout;
    @BindView(R.id.store_rate_linearLayout)
    LinearLayout store_rate_linearLayout;
    @BindView(R.id.no_store_rate_imageView)
    ImageView no_store_rate_imageView;
    @BindView(R.id.five_minutes_imageView)
    ImageView five_minutes_imageView;
    @BindView(R.id.ten_minutes_imageView)
    ImageView ten_minutes_imageView;
    @BindView(R.id.twenty_minutes_imageView)
    ImageView twenty_minutes_imageView;
    @BindView(R.id.thirty_minutes_imageView)
    ImageView thirty_minutes_imageView;
    @BindView(R.id.sixty_minutes_imageView)
    ImageView sixty_minutes_imageView;

    private final static String TAG = InputValueActivity.class.getSimpleName();

    public static final int FREQUENCY_TABLE_NUMBER = 1001;
    public static final int SCAN_RATE_SECONDS = 1002;
    public static final int NUMBER_OF_ANTENNAS = 1003;
    public static final int SCAN_TIMEOUT_SECONDS = 1004;
    public static final int STORE_RATE = 1005;
    public static final int REFERENCE_FREQUENCY = 1006;
    public static final int REFERENCE_FREQUENCY_STORE_RATE = 1007;

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    private int value;
    private int storeRate = 0;

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
                    if (parameter.equals("aerial")) { // Gets aerial defaults data
                        onClickAerialDefaults();
                    } else if (parameter.equals("stationary")) { // Gets stationary defaults data
                        onClickStationaryDefaults();
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    switch (value) {
                        case FREQUENCY_TABLE_NUMBER: // Gets the frequency table number
                            downloadTable(packet);
                            break;
                        case SCAN_RATE_SECONDS: // Gets the scan rate seconds
                            downloadScanRate(packet);
                            break;
                        case NUMBER_OF_ANTENNAS: // Gets number of antennas
                            downloadAntennas(packet);
                            break;
                        case SCAN_TIMEOUT_SECONDS: // Gets the scan timeout seconds
                            downloadTimeout(packet);
                            break;
                        case STORE_RATE: // Gets the store rate
                            downloadStoreRate(packet);
                            break;
                        case REFERENCE_FREQUENCY_STORE_RATE: // Gets the reference frequency store rate
                            downloadReferenceFrequencyStoreRate(packet);
                            break;
                    }
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
     * Requests a read for get stationary defaults data.
     * Service name: Scan.
     * Characteristic name: Stationary.
     */
    private void onClickStationaryDefaults() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    @OnClick(R.id.no_store_rate_linearLayout)
    public void onClickNoStoreRate(View v) {
        no_store_rate_imageView.setVisibility(View.VISIBLE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        twenty_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 100;
    }

    @OnClick(R.id.five_minutes_linearLayout)
    public void onClickFiveMinutes(View v) {
        no_store_rate_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.VISIBLE);
        ten_minutes_imageView.setVisibility(View.GONE);
        twenty_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 5;
    }

    @OnClick(R.id.ten_minutes_linearLayout)
    public void onClickTenMinutes(View v) {
        no_store_rate_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.VISIBLE);
        twenty_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 10;
    }

    @OnClick(R.id.twenty_minutes_linearLayout)
    public void onClickTwentyMinutes(View v) {
        no_store_rate_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        twenty_minutes_imageView.setVisibility(View.VISIBLE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 20;
    }

    @OnClick(R.id.thirty_minutes_linearLayout)
    public void onClickThirtyMinutes(View v) {
        no_store_rate_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        twenty_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.VISIBLE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 30;
    }

    @OnClick(R.id.sixty_minutes_linearLayout)
    public void onClickSixtyMinutes(View v) {
        no_store_rate_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        twenty_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.VISIBLE);
        storeRate = 60;
    }

    @OnClick(R.id.save_changes_input_value_button)
    public void onClickSaveChanges(View v) {
        if (parameter.equals("aerial") && value == SCAN_RATE_SECONDS) { // Sends the scan rate value for aerial
            float scanRate = Float.parseFloat(value_spinner.getSelectedItem().toString());
            setResult((int) (scanRate * 10));
        }
        if (parameter.equals("stationary") && value == SCAN_RATE_SECONDS) { // Sends the scan rate value for stationary
            int scanRate = Integer.parseInt(value_spinner.getSelectedItem().toString());
            setResult(scanRate);
        }
        if (value == FREQUENCY_TABLE_NUMBER) { // Sends the frequency table number
            int frequencyTableNumber = (value_spinner.getSelectedItem().toString().equals("None")) ? 0 :
                    Integer.parseInt(value_spinner.getSelectedItem().toString().replace("Table ", ""));
            setResult(frequencyTableNumber);
        }
        if (value == NUMBER_OF_ANTENNAS) { // Sends the number of antennas
            int numberAntennas = value_spinner.getSelectedItemPosition();
            setResult(numberAntennas);
        }
        if (value == SCAN_TIMEOUT_SECONDS) { // Sends scan timeout value
            int timeout = Integer.parseInt(value_spinner.getSelectedItem().toString());
            setResult(timeout);
        }
        if (value == REFERENCE_FREQUENCY_STORE_RATE) {
            int storeRate = value_spinner.getSelectedItemPosition();
            setResult(storeRate);
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_value);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.edit_receiver_defaults);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        parameter = getIntent().getStringExtra("type");
        value = getIntent().getIntExtra("value", 0);

        if (value == STORE_RATE) {
            store_rate_linearLayout.setVisibility(View.VISIBLE);
        } else {
            set_value_linearLayout.setVisibility(View.VISIBLE);
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (value == STORE_RATE)
                setResult(storeRate);
            finish();
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
        if (!mConnected) {
            showDisconnectionMessage();
        }
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
     * With the received packet, gets frequency table number and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadTable(byte[] data) {
        List<String> tables = new ArrayList<>();
        int positionFrequencyTableNumber = 0;
        byte b = (parameter.equals("aerial")) ? data[6] : data[9];
        for (int i = 1; i <= 8; i++) {
            if ((b & 1) == 1) {
                tables.add("Table " + i);
                positionFrequencyTableNumber = (i == Integer.parseInt(Converters.getDecimalValue(data[1]))) ? tables.size() - 1 : 0;
            }
            b = (byte) (b >> 1);
        }
        b = (parameter.equals("aerial")) ? data[7] : data[10];
        for (int i = 9; i <= 12; i++) {
            if ((b & 1) == 1) {
                tables.add("Table " + i);
                positionFrequencyTableNumber = (i == Integer.parseInt(Converters.getDecimalValue(data[1]))) ? tables.size() - 1 : 0;
            }
            b = (byte) (b >> 1);
        }
        if (tables.isEmpty()) {
            tables.add("None");
        }
        ArrayAdapter<String> tablesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tables);
        tablesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        value_spinner.setAdapter(tablesAdapter);
        value_spinner.setSelection(positionFrequencyTableNumber);
    }

    /**
     * With the received packet, gets scan rate value and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadScanRate(byte[] data) {
        if (parameter.equals("aerial")) {
            ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(this, R.array.scanRateAerial, android.R.layout.simple_spinner_item);
            scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            value_spinner.setAdapter(scanRateAdapter);

            int index = 0;
            for (int i = 0; i < 49; i++) {
                String item = value_spinner.getItemAtPosition(i).toString().replace(".", "");
                if (item.equals(Converters.getDecimalValue(data[3]))) {
                    index = i;
                    break;
                }
            }
            value_spinner.setSelection(index);
        } else {
            ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(this, R.array.scanRateStationary, android.R.layout.simple_spinner_item);
            scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            value_spinner.setAdapter(scanRateAdapter);

            int scanRate = Integer.parseInt(Converters.getDecimalValue(data[3]));
            if (scanRate <= 255) {
                value_spinner.setSelection(scanRate - 3);
            } else {
                value_spinner.setSelection(0);
            }
        }
    }

    /**
     * With the received packet, gets number of antennas and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadAntennas(byte[] data) {
        ArrayAdapter<CharSequence> antennasAdapter = ArrayAdapter.createFromResource(this, R.array.antennas, android.R.layout.simple_spinner_item);
        antennasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        value_spinner.setAdapter(antennasAdapter);

        int antennaNumber = Integer.parseInt(Converters.getDecimalValue(data[2])) & 15;
        if (antennaNumber <= 4 && antennaNumber > 0) {
            value_spinner.setSelection(antennaNumber - 1);
        } else {
            value_spinner.setSelection(0);
        }
    }

    /**
     * With the received packet, gets scan timeout value and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadTimeout(byte[] data) {
        ArrayAdapter<CharSequence> timeoutAdapter = ArrayAdapter.createFromResource(this, R.array.timeout, android.R.layout.simple_spinner_item);
        timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        value_spinner.setAdapter(timeoutAdapter);

        int timeout = Integer.parseInt(Converters.getDecimalValue(data[4]));
        if (timeout <= 200) {
            value_spinner.setSelection(timeout - 2);
        } else {
            value_spinner.setSelection(0);
        }
    }

    /**
     * With the received packet, gets store rate value and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadStoreRate(byte[] data) {
        switch (Converters.getDecimalValue(data[5])) {
            case "0":
                no_store_rate_imageView.setVisibility(View.VISIBLE);
                break;
            case "5":
                five_minutes_imageView.setVisibility(View.VISIBLE);
                break;
            case "10":
                ten_minutes_imageView.setVisibility(View.VISIBLE);
                break;
            case "20":
                twenty_minutes_imageView.setVisibility(View.VISIBLE);
                break;
            case "30":
                thirty_minutes_imageView.setVisibility(View.VISIBLE);
                break;
            case "60":
                sixty_minutes_imageView.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * With the received packet, gets reference frequency store rate value and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadReferenceFrequencyStoreRate(byte[] data) {
        ArrayAdapter<CharSequence> storeRateAdapter = ArrayAdapter.createFromResource(this, R.array.referenceFrequencyStoreRate, android.R.layout.simple_spinner_item);
        storeRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        value_spinner.setAdapter(storeRateAdapter);

        int referenceFrequencyStoreRate = Integer.parseInt(Converters.getDecimalValue(data[8]));
        if (referenceFrequencyStoreRate <= 24) {
            value_spinner.setSelection(referenceFrequencyStoreRate);
        } else {
            value_spinner.setSelection(0);
        }
    }
}