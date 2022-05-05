package com.atstrack.ats.ats_vhf_receiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
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
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
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

public class MainMenuActivity extends AppCompatActivity {

    @BindView(R.id.menu_linearLayout)
    LinearLayout menu_linearLayout;
    @BindView(R.id.vhf_constraintLayout)
    ConstraintLayout vhf_linearLayout;
    @BindView(R.id.state_textView)
    TextView state_textView;
    @BindView(R.id.status_device_menu_textView)
    TextView status_textView;
    @BindView(R.id.disconnect_button)
    TextView disconnect_button;
    @BindView(R.id.connecting_device_linearLayout)
    LinearLayout connecting_device_linearLayout;
    @BindView(R.id.check_avd_anim)
    ImageView check_avd_anim;
    @BindView(R.id.percent_battery_menu_textView)
    TextView percent_battery_menu;

    private final static String TAG = MainMenuActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_STATUS = "DEVICE_STATUS";
    public static final String EXTRAS_BATTERY = "DEVICE_BATTERY";
    private static final long MESSAGE_PERIOD = 1000;
    private static final long CONNECT_PERIOD = 3000;

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
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
            if (result)
                mConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = false;
    private String parameter;

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
                    if (parameter.equals("scanning")) // Checks if the BLE device is scanning
                        onClickScanning();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("scanning")) // Checks if the BLE device is scanning
                        download(packet);
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
     * Requests a read for check if the BLE device is scanning.
     * Service name: Diagnostic.
     * Characteristic name: BoardStatus.
     */
    private void onClickScanning() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_BOARD_STATUS;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
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

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();
        scanning = false;

        boolean isMenu = getIntent().getBooleanExtra("menu", false);

        if (!isMenu) { // Connecting to the selected BLE device
            // Checks if the BLE device is scanning
            parameter = "scanning";
            receiverInformation.setReceiverInformation(
                    getIntent().getStringExtra(EXTRAS_DEVICE_NAME),
                    getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS),
                    getIntent().getStringExtra(EXTRAS_DEVICE_STATUS),
                    getIntent().getStringExtra(EXTRAS_BATTERY));

            // Initializes the spinner to connect to BLE device
            check_avd_anim.setImageDrawable((AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_anim_spinner_48));
            Drawable drawable = check_avd_anim.getDrawable();
            Animatable animatable = (Animatable) drawable;
            AnimatedVectorDrawableCompat.registerAnimationCallback(drawable, new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    new Handler().postDelayed(() -> animatable.start(), CONNECT_PERIOD);
                }
            });
            animatable.start();

            mHandlerMenu = new Handler();
            mHandler = new Handler();
            connectingToDevice();
        } else { // Only displays the main menu
            vhf_linearLayout.setVisibility(View.GONE);
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

                // Initializes the check animation when connected successfully
                check_avd_anim.setImageDrawable((AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.check_avd_anim));
                Drawable drawable = check_avd_anim.getDrawable();
                Animatable animatable = (Animatable) drawable;
                AnimatedVectorDrawableCompat.registerAnimationCallback(drawable, new Animatable2Compat.AnimationCallback() {
                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        new Handler().postDelayed(() -> animatable.start(), MESSAGE_PERIOD);
                    }
                });
                animatable.start();

                mHandlerMenu.postDelayed(() -> {
                    if (!scanning) { // After connecting displays the main menu
                        menu_linearLayout.setVisibility(View.VISIBLE);
                        vhf_linearLayout.setVisibility(View.GONE);
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
    private void download(byte[] data) {
        switch (Converters.getHexValue(data[0])) {
            case "00": // The BLE device is not in scanning
                scanning = false;
                int baseFrequency = Integer.parseInt(Converters.getDecimalValue(data[1]));
                int range = Integer.parseInt(Converters.getDecimalValue(data[2]));
                int detectionType = Integer.parseInt(Converters.getDecimalValue(data[3]));
                int statusBytesDefault = Integer.parseInt(Converters.getDecimalValue(data[7]));

                SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
                SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
                sharedPreferencesEdit.putInt("BaseFrequency", baseFrequency);
                sharedPreferencesEdit.putInt("Range", range);
                sharedPreferencesEdit.putInt("DetectionType", detectionType);
                sharedPreferencesEdit.apply();
                break;
            case "82": // The BLE device is in aerial scanning
                scanning = true;
                int txTypeA = Integer.parseInt(Converters.getDecimalValue(data[3]));
                int antennaA = Integer.parseInt(Converters.getDecimalValue(data[4])) / 16;
                int tableA = Integer.parseInt(Converters.getDecimalValue(data[4])) % 16;
                int scanTimeA = Integer.parseInt(Converters.getDecimalValue(data[5]));
                int timeoutA = Integer.parseInt(Converters.getDecimalValue(data[6]));

                Intent intentA = new Intent(this, AerialScanActivity.class);
                intentA.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentA.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentA.putExtra("scanning", true);
                intentA.putExtra("year", Integer.parseInt(Converters.getDecimalValue(data[16])));
                intentA.putExtra("month", Integer.parseInt(Converters.getDecimalValue(data[17])));
                intentA.putExtra("day", Integer.parseInt(Converters.getDecimalValue(data[18])));
                intentA.putExtra("hour", Integer.parseInt(Converters.getDecimalValue(data[19])));
                intentA.putExtra("minute", Integer.parseInt(Converters.getDecimalValue(data[20])));
                intentA.putExtra("seconds", Integer.parseInt(Converters.getDecimalValue(data[21])));
                startActivity(intentA);
                break;
            case "83": // The BLE device is in stationary scanning
                scanning = true;
                int txTypeS = Integer.parseInt(Converters.getDecimalValue(data[3]));
                int antennaS = Integer.parseInt(Converters.getDecimalValue(data[4])) / 16;
                int tableS = Integer.parseInt(Converters.getDecimalValue(data[4])) % 16;
                int scanTimeS = Integer.parseInt(Converters.getDecimalValue(data[5]));
                int timeoutS = Integer.parseInt(Converters.getDecimalValue(data[6]));

                Intent intentS = new Intent(this, StationaryScanActivity.class);
                intentS.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentS.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentS.putExtra("scanning", true);
                intentS.putExtra("year", Integer.parseInt(Converters.getDecimalValue(data[16])));
                intentS.putExtra("month", Integer.parseInt(Converters.getDecimalValue(data[17])));
                intentS.putExtra("day", Integer.parseInt(Converters.getDecimalValue(data[18])));
                intentS.putExtra("hour", Integer.parseInt(Converters.getDecimalValue(data[19])));
                intentS.putExtra("minute", Integer.parseInt(Converters.getDecimalValue(data[20])));
                intentS.putExtra("seconds", Integer.parseInt(Converters.getDecimalValue(data[21])));
                startActivity(intentS);
                break;
            case "86": // The BLE device is in manual scanning
                scanning = true;
                int txTypeM = Integer.parseInt(Converters.getDecimalValue(data[3]));
                int antennaM = Integer.parseInt(Converters.getDecimalValue(data[4])) / 16;
                int tableM = Integer.parseInt(Converters.getDecimalValue(data[4])) % 16;
                int scanTimeM = Integer.parseInt(Converters.getDecimalValue(data[5]));
                int timeoutM = Integer.parseInt(Converters.getDecimalValue(data[6]));

                Intent intentM = new Intent(this, ManualScanActivity.class);
                intentM.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentM.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentM.putExtra("scanning", true);
                intentM.putExtra("year", Integer.parseInt(Converters.getDecimalValue(data[16])));
                intentM.putExtra("month", Integer.parseInt(Converters.getDecimalValue(data[17])));
                intentM.putExtra("day", Integer.parseInt(Converters.getDecimalValue(data[18])));
                intentM.putExtra("hour", Integer.parseInt(Converters.getDecimalValue(data[19])));
                intentM.putExtra("minute", Integer.parseInt(Converters.getDecimalValue(data[20])));
                intentM.putExtra("seconds", Integer.parseInt(Converters.getDecimalValue(data[21])));
                startActivity(intentM);
        }
    }
}