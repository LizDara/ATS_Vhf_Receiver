package com.atstrack.ats.ats_vhf_receiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Messages.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

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
    TextView percent_battery_menu_textView;
    @BindView(R.id.connecting_progressBar)
    ProgressBar connecting_progressBar;
    @BindView(R.id.connected_imageView)
    ImageView connected_imageView;
    @BindView(R.id.sd_card_menu_textView)
    TextView sd_card_menu_textView;
    @BindView(R.id.battery_menu_imageView)
    ImageView battery_menu_imageView;
    @BindView(R.id.sd_card_menu_imageView)
    ImageView sd_card_menu_imageView;

    private final static String TAG = MainMenuActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_STATUS = "DEVICE_STATUS";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RANGE = "DEVICE_RANGE";
    public static final String EXTRAS_BATTERY = "DEVICE_BATTERY";

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private Handler mHandler;
    private Handler mHandlerMenu;
    private byte detectionType;
    private DialogFragment detectionFilter;

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
                if (!result)
                    showDisconnectionMessage("Failed to connect to receiver");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = false;
    private String parameter = "";
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
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    connecting_device_linearLayout.setVisibility(View.VISIBLE);
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action) && mConnected) {
                    mConnected = false;
                    if (menu_linearLayout.getVisibility() == View.VISIBLE)
                        showDisconnectionMessage("Receiver Disconnected");
                    else
                        showDisconnectionMessage("Failed to connect to receiver");
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals(ValueCodes.SCAN_STATUS))
                        onClickScanning();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    Log.i(TAG, Converters.getHexValue(packet));
                    if (Converters.getHexValue(packet[0]).equals("50")) // Checks if the BLE device is scanning
                        downloadScanning(packet);
                    else if (Converters.getHexValue(packet[0]).equals("41")) //Get board state
                        downloadBoardState(packet);
                }
            } catch (Exception e) {
                mBluetoothLeService.disconnect();
            }
        }
    };

    private final BroadcastReceiver mSecondGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND.equals(action)) {
                    if (secondParameter.equals(ValueCodes.BOARD_STATUS)) // Checks if the BLE device is scanning
                        onClickBoardState();
                    else if (secondParameter.equals(ValueCodes.DETECTION_TYPE))
                        onClickDetectionFilter();
                }
            } catch (Exception e) {
                mBluetoothLeService.disconnect();
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
     * Requests a read for board state.
     */
    private void onClickBoardState() {
        secondParameter = "";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_BOARD_STATE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Requests a read for scan state.
     */
    private void onClickScanning() {
        secondParameter = ValueCodes.BOARD_STATUS;
        parameter = "";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, ValueCodes.WAITING_PERIOD);
    }

    private void onClickDetectionFilter() {
        byte[] b = new byte[11];
        b[0] = (byte) 0x47;
        b[1] = detectionType;

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        secondParameter = "";
        if (result) {
            receiverInformation.changeTxType(detectionType);
        } else {
            detectionType = 0;
            detectionFilter.show(getSupportFragmentManager(), DetectionFilter.TAG);
        }
    }

    @OnClick(R.id.disconnect_button)
    public void onClickDisconnect(View v) {
        mBluetoothLeService.disconnect();
    }

    @OnClick(R.id.start_scanning_button)
    public void onClickStartScanning(View v) {
        Intent intent = new Intent(this, StartScanningActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.receiver_configuration_button)
    public void onClickReceiverConfiguration(View v) {
        Intent intent = new Intent(this, ReceiverConfigurationActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.manage_receiver_data_button)
    public void onClickManageReceiverData(View v) {
        Intent intent = new Intent(this, GetDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.convert_raw_data_button)
    public void onClickConvertRaw(View v) {
        Intent intent = new Intent(this, ConvertRawDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.diagnostics_button)
    public void onClickDiagnostics(View v) {
        Intent intent = new Intent(this, TestReceiverActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on

        receiverInformation = ReceiverInformation.getReceiverInformation(); // Get device data from previous activity

        boolean isMenu = getIntent().getBooleanExtra(ValueCodes.MENU, false);
        if (!isMenu) { // Connecting to the selected BLE device
            parameter = ValueCodes.SCAN_STATUS; // Checks if the BLE device is scanning
            receiverInformation.changeInformation((byte) 0, (byte) 0,
                    getIntent().getStringExtra(EXTRAS_DEVICE_NAME),
                    getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS),
                    getIntent().getStringExtra(EXTRAS_DEVICE_RANGE),
                    getIntent().getStringExtra(EXTRAS_BATTERY));

            String status = getIntent().getStringExtra(EXTRAS_DEVICE_STATUS);
            if (status.contains("Fixed"))
                detectionType = 0x08;
            else if (status.contains("Variable"))
                detectionType = 0x07;
            else if (status.contains("Coded"))
                detectionType = 0x09;

            mHandlerMenu = new Handler();
            mHandler = new Handler();
            menu_linearLayout.setVisibility(View.GONE);
            connectingToDevice();
        } else { // Only displays the main menu
            vhf_constraintLayout.setVisibility(View.GONE);
            menu_linearLayout.setVisibility(View.VISIBLE);
            connecting_device_linearLayout.setVisibility(View.GONE);
            sd_card_menu_textView.setText(receiverInformation.getSDCard());
            sd_card_menu_imageView.setBackground(ContextCompat.getDrawable(this, receiverInformation.getSDCard().equals("Inserted") ? R.drawable.ic_sd_card : R.drawable.ic_no_sd_card));
        }
        status_textView.setText("Receiver " + receiverInformation.getDeviceName());
        percent_battery_menu_textView.setText(receiverInformation.getPercentBattery() + "%");
        battery_menu_imageView.setBackground(ContextCompat.getDrawable(this, receiverInformation.getPercentBattery() > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));

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

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
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
        }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD);
    }

    /**
     * Tries to connect to the selected BLE device or shows a connection error.
     */
    private void connectingToDevice() {
        mHandler.postDelayed(() -> {
            Log.i(TAG, "Connected: " + mConnected);
            if (mConnected) {
                connecting_progressBar.setVisibility(View.GONE);
                connected_imageView.setVisibility(View.VISIBLE);
                state_textView.setText(R.string.lb_connected);

                mHandlerMenu.postDelayed(() -> { // After connecting displays the main menu
                    menu_linearLayout.setVisibility(View.VISIBLE);
                    vhf_constraintLayout.setVisibility(View.GONE);
                    connecting_device_linearLayout.setVisibility(View.GONE);
                }, ValueCodes.MESSAGE_PERIOD);
            } else {
                showDisconnectionMessage("Failed to connect to receiver");
            }
        }, ValueCodes.CONNECT_PERIOD);
    }

    /**
     * With the received packet, get state of board.
     * @param data The received packet.
     */
    private void downloadBoardState(byte[] data) {
        checkSDCard(data[7]);
        int baseFrequency = Integer.parseInt(Converters.getDecimalValue(data[2]));
        int range = Integer.parseInt(Converters.getDecimalValue(data[3]));
        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
        sharedPreferencesEdit.putInt(ValueCodes.BASE_FREQUENCY, baseFrequency);
        sharedPreferencesEdit.putInt(ValueCodes.RANGE, range);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        sharedPreferencesEdit.putInt(ValueCodes.WIDTH, metrics.widthPixels);
        sharedPreferencesEdit.putInt(ValueCodes.HEIGHT, metrics.heightPixels);
        sharedPreferencesEdit.apply();

        if (detectionType == 0) {
            detectionFilter = DetectionFilter.newInstance();

            getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    secondParameter = bundle.getString(ValueCodes.PARAMETER);
                    if (secondParameter != null && secondParameter.equals(ValueCodes.DETECTION_TYPE)) {
                        detectionType = bundle.getByte(ValueCodes.VALUE);

                        mBluetoothLeService.discoveringSecond();
                    }
                }
            });
            detectionFilter.show(getSupportFragmentManager(), DetectionFilter.TAG);
        } else {
            receiverInformation.changeTxType(detectionType);
        }
    }

    /**
     * With the received packet, get state of scan.
     * @param data The received packet.
     */
    private void downloadScanning(byte[] data) {
        receiverInformation.changeScanState(data[1]);
        if (data[1] == 0)
            return;
        Intent intent = new Intent();
        switch (Converters.getHexValue(data[1])) {
            case "82": // The BLE device is in aerial scanning
            case "81":
            case "80":
                intent = new Intent(this, AerialScanActivity.class);
                break;
            case "83": // The BLE device is in stationary scanning
                intent = new Intent(this, StationaryScanActivity.class);
                break;
            case "86": // The BLE device is in manual scanning
                intent = new Intent(this, ManualScanActivity.class);
                break;
        }
        intent.putExtra(ValueCodes.VALUE, data);
        intent.putExtra(ValueCodes.SCANNING, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void checkSDCard(byte state) {
        receiverInformation.changeSDCard(state);
        sd_card_menu_textView.setText(receiverInformation.getSDCard());
        sd_card_menu_imageView.setBackground(ContextCompat.getDrawable(this, Converters.getHexValue(state).equals("01") ? R.drawable.ic_sd_card : R.drawable.ic_no_sd_card));
    }
}