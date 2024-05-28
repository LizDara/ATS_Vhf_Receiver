package com.atstrack.ats.ats_vhf_receiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.util.UUID;

public class MainMenuActivity extends AppCompatActivity {

    @BindView(R.id.menu_linearLayout)
    LinearLayout menu_linearLayout;
    @BindView(R.id.vhf_constraintLayout)
    ConstraintLayout vhf_constraintLayout;
    @BindView(R.id.state_textView)
    TextView state_textView;
    @BindView(R.id.status_device_menu_textView)
    TextView status_textView;
    @BindView(R.id.disconnect_button)
    TextView disconnect_button;
    @BindView(R.id.connecting_device_linearLayout)
    LinearLayout connecting_device_linearLayout;
    @BindView(R.id.percent_battery_menu_textView)
    TextView percent_battery_menu;

    private final static String TAG = MainMenuActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_STATUS = "DEVICE_STATUS";
    public static final String EXTRAS_BATTERY = "DEVICE_BATTERY";
    private static final long MESSAGE_PERIOD = 1000;
    private static final long CONNECT_PERIOD = 1500;

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private Handler mHandler;
    private Handler mHandlerMenu;
    private boolean scanning;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (mBluetoothLeService != null) {
                if (!mBluetoothLeService.initialize()) {
                    Log.d(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
                // Automatically connects to the device upon successful start-up initialization.
                boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
                if (result)
                    mConnected = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = false;
    private String parameter;
    private String secondParameter;

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
                Log.i(TAG, action);
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals("scanning"))
                        onClickScanning();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (Converters.getHexValue(packet[0]).equals("50")) // Checks if the BLE device is scanning
                        downloadScanning(packet);
                    else if (packet.length == 22) //Get scan data
                        downloadBoardState(packet);
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    private final BroadcastReceiver mSecondGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND.equals(action)) {
                    if (secondParameter.equals("boardState")) { // Checks if the BLE device is scanning
                        onClickBoardState();
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
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

    private static IntentFilter makeSecondGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND);
        return intentFilter;
    }

    /**
     * Requests a read for check if the BLE device is scanning.
     * Service name: Diagnostic.
     * Characteristic name: BoardState.
     */
    private void onClickBoardState() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_BOARD_STATE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
        secondParameter = "";
        parameter = "boardState";
    }

    private void onClickScanning() {
        secondParameter = "boardState";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, MESSAGE_PERIOD);
    }

    @OnClick(R.id.disconnect_button)
    public void onClickDisconnect(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        mBluetoothLeService.disconnect();
    }

    @OnClick(R.id.view_receiver_options_button)
    public void onClickViewReceiverOptions(View v) {
        Intent intent = new Intent(this, ReceiverOptionsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.start_scanning_button)
    public void onClickStartScanning(View v) {
        Intent intent = new Intent(this, StartScanningActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();
        scanning = false;

        boolean isMenu = getIntent().getBooleanExtra("menu", false);

        if (!isMenu) { // Connecting to the selected BLE device
            parameter = "scanning"; // Checks if the BLE device is scanning
            receiverInformation.changeInformation(
                    getIntent().getStringExtra(EXTRAS_DEVICE_NAME),
                    getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS),
                    getIntent().getStringExtra(EXTRAS_DEVICE_STATUS),
                    getIntent().getStringExtra(EXTRAS_BATTERY));

            mHandlerMenu = new Handler();
            mHandler = new Handler();
            menu_linearLayout.setVisibility(View.GONE);
            connectingToDevice();
        } else { // Only displays the main menu
            vhf_constraintLayout.setVisibility(View.GONE);
            menu_linearLayout.setVisibility(View.VISIBLE);
            connecting_device_linearLayout.setVisibility(View.GONE);
        }

        status_textView.setText(receiverInformation.getDeviceName());
        percent_battery_menu.setText(receiverInformation.getPercentBattery());

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        mBluetoothLeService.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(mSecondGattUpdateReceiver, makeSecondGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(mSecondGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // Go back to the previous activity
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mConnected) {
            connecting_device_linearLayout.setVisibility(View.VISIBLE);
        } else if (menu_linearLayout.getVisibility() == View.VISIBLE) {
            showDisconnectionMessage("Receiver Disconnected");
        }
        return true;
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     *
     * @param message The message that will be displayed on the screen.
     */
    private void showDisconnectionMessage(String message) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        TextView disconnect_message = view.findViewById(R.id.disconnect_message);
        disconnect_message.setText(message);

        dialog.setView(view);
        dialog.show();

        // The message disappears after a pre-defined period and will search for other available BLE devices again
        mHandlerMenu.postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, CONNECT_PERIOD);
    }

    /**
     * Tries to connect to the selected BLE device or shows a connection error.
     */
    private void connectingToDevice() {
        mHandler.postDelayed(() -> {
            if (mConnected) {
                state_textView.setText(R.string.lb_connected);

                mHandlerMenu.postDelayed(() -> {
                    if (!scanning) { // After connecting displays the main menu
                        menu_linearLayout.setVisibility(View.VISIBLE);
                        vhf_constraintLayout.setVisibility(View.GONE);
                        connecting_device_linearLayout.setVisibility(View.GONE);
                    }
                }, MESSAGE_PERIOD);
            } else {
                showDisconnectionMessage("Failed to connect to receiver");
            }
        }, CONNECT_PERIOD);
    }

    /**
     * With the received packet, check if the BLE device is in scanning.
     *
     * @param data The received packet.
     */
    private void downloadBoardState(byte[] data) {
        parameter = "scanning";
        Log.i(TAG, "BoardState: " + Converters.getHexValue(data));
        int baseFrequency = Integer.parseInt(Converters.getDecimalValue(data[1]));
        int range = Integer.parseInt(Converters.getDecimalValue(data[2]));
        int detectionType = Integer.parseInt(Converters.getDecimalValue(data[3]));

        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
        sharedPreferencesEdit.putInt("BaseFrequency", baseFrequency);
        sharedPreferencesEdit.putInt("Range", range);
        sharedPreferencesEdit.putInt("DetectionType", detectionType);
        sharedPreferencesEdit.apply();
    }

    private void downloadScanning(byte[] data) {
        parameter = "";
        Log.i(TAG, "Scanning: " + Converters.getHexValue(data));
        if (data[1] == 0)
            return;
        Intent intent = new Intent();
        scanning = true;
        switch (Converters.getHexValue(data[1])) {
            case "82": // The BLE device is in aerial scanning
            case "81":
                int autoRecord = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1;
                int currentFrequency = (Integer.parseInt(Converters.getDecimalValue(data[16])) * 256)
                        + Integer.parseInt(Converters.getDecimalValue(data[17]));
                int currentIndex = (Integer.parseInt(Converters.getDecimalValue(data[7])) * 256)
                        + Integer.parseInt(Converters.getDecimalValue(data[8]));
                intent = new Intent(this, AerialScanActivity.class);
                intent.putExtra("isHold", Converters.getHexValue(data[1]).equals("81"));
                intent.putExtra("autoRecord", autoRecord == 1);
                intent.putExtra("frequency", currentFrequency);
                intent.putExtra("index", currentIndex);
                break;
            case "83": // The BLE device is in stationary scanning
                intent = new Intent(this, StationaryScanActivity.class);
                break;
            case "86": // The BLE device is in manual scanning
                intent = new Intent(this, ManualScanActivity.class);
                break;
        }
        intent.putExtra("scanning", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}