package com.atstrack.ats.ats_vhf_receiver;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.CategoryListAdapter;
import com.atstrack.ats.ats_vhf_receiver.Adapters.LeDeviceListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnAdapterClickListener {

    @BindView(R.id.main_toolbar)
    Toolbar main_toolbar;
    @BindView(R.id.main_title_toolbar)
    TextView main_title_toolbar;
    @BindView(R.id.version_textView)
    TextView version_textView;
    @BindView(R.id.splash_screen_constraintLayout)
    ConstraintLayout splash_screen_constraintLayout;
    @BindView(R.id.bridge_app_linearLayout)
    LinearLayout bridge_app_linearLayout;
    @BindView(R.id.bridge_subtitle_textView)
    TextView bridge_subtitle_textView;
    @BindView(R.id.bridge_message_textView)
    TextView bridge_message_textView;
    @BindView(R.id.types_subtitle_textView)
    TextView types_subtitle_textView;
    @BindView(R.id.category_recyclerView)
    RecyclerView category_recyclerView;
    @BindView(R.id.select_device_constraintLayout)
    ConstraintLayout select_device_constraintLayout;
    @BindView(R.id.searching_progressBar)
    ProgressBar searching_progressBar;
    @BindView(R.id.searching_devices_linearLayout)
    LinearLayout searching_devices_linearLayout;
    @BindView(R.id.devices_subtitle_textView)
    TextView devices_subtitle_textView;
    @BindView(R.id.searching_message_textView)
    TextView searching_message_textView;
    @BindView(R.id.devices_scrollView)
    ScrollView devices_scrollView;
    @BindView(R.id.device_recyclerView)
    RecyclerView device_recyclerView;
    @BindView(R.id.connecting_device_linearLayout)
    LinearLayout connecting_device_linearLayout;
    @BindView(R.id.selected_device_scrollView)
    ScrollView selected_device_scrollView;
    @BindView(R.id.connected_imageView)
    ImageView connected_imageView;
    @BindView(R.id.cancel_button)
    Button cancel_button;
    @BindView(R.id.connect_button)
    Button connect_button;
    @BindView(R.id.no_device_found_linearLayout)
    LinearLayout no_device_found_linearLayout;

    private final static String TAG = MainActivity.class.getSimpleName();

    private CategoryListAdapter categoryListAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    private boolean cancel;
    private boolean isNightModeOn;

    // Device scan callback.
    private final ScanCallback mLeScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    mLeDeviceListAdapter.addDevice(result.getDevice(), result.getScanRecord().getBytes());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            };

    ActivityResultLauncher<Intent> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == -1) {
                        if (this.deniedPermissions.isEmpty())
                            initialize(ValueCodes.MESSAGE_PERIOD);
                        else
                            showAlert();
                    } else {
                        this.deniedPermissions += "\n- Nearby devices";
                        showAlert();
                    }
            });

    private LeServiceConnection leServiceConnection;
    private boolean mConnected = false;
    private String parameter = "";
    private String secondParameter;
    private String deniedPermissions = "";

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
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action) && mConnected) {
                    mConnected = false;
                    showDisconnectionMessage();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals(ValueCodes.SCAN_STATUS))
                        getScanStatus();
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
                leServiceConnection.getBluetoothLeService().disconnect();
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
                        getBoardState();
                }
            } catch (Exception e) {
                leServiceConnection.getBluetoothLeService().disconnect();
            }
        }
    };

    /**
     * Requests a read for board state.
     */
    private void getBoardState() {
        secondParameter = "";
        TransferBleData.readBoardState();
    }

    /**
     * Requests a read for scan state.
     */
    private void getScanStatus() {
        secondParameter = ValueCodes.BOARD_STATUS;
        parameter = "";
        TransferBleData.notificationLog();
        if (mConnected) {
            new Handler().postDelayed(() -> {
                leServiceConnection.getBluetoothLeService().discoveringSecond();
            }, ValueCodes.WAITING_PERIOD);
        }
    }

    @OnClick(R.id.retry_button)
    public void onClickRetry(View v) {
        scanLeDevice(true);
    }

    @SuppressLint("MissingPermission")
    @OnClick(R.id.connect_button)
    public void onClickConnect(View v) {
        cancel = false;
        BluetoothDevice device = mLeDeviceListAdapter.getSelectedDevice();
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.changeInformation(device.getName().substring(0, 7), device.getAddress(), "0%");
        connectingToDevice();

        mHandler.postDelayed(() -> {
            if (!cancel) {
                if (mConnected) {
                    parameter = ValueCodes.SCAN_STATUS;
                    leServiceConnection.getBluetoothLeService().discovering();
                    setDeviceConnected();
                    new Handler().postDelayed(() -> { // After connecting display the main menu of device
                        if (!cancel && mConnected) {
                            if (device.getName().contains("vr")) {
                                connectVhfReceiver();
                            } else if (device.getName().contains("ar")) {
                                Intent intent = new Intent(this, AcousticMenuActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }
                    }, ValueCodes.MESSAGE_PERIOD);
                } else {
                    showDisconnectionMessage();
                }
            }
        }, ValueCodes.CONNECT_PERIOD);
    }

    @OnClick(R.id.cancel_button)
    public void onClickCancel(View v) {
        cancel = true;
        if (mConnected) leServiceConnection.getBluetoothLeService().disconnect();
        unbindService(leServiceConnection.getServiceConnection());
        leServiceConnection.close();
        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(mSecondGattUpdateReceiver);

        connecting_device_linearLayout.setVisibility(View.GONE);
        setDevicesFound();
    }

    /*@OnClick(R.id.switch_dark_mode)
    public void onDarkModeClick(View v) {
        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sharedPreferencesEditor.putBoolean(ValueCodes.NIGHT_MODE, false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sharedPreferencesEditor.putBoolean(ValueCodes.NIGHT_MODE, true);
        }
        sharedPreferencesEditor.apply();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(main_toolbar);
        main_title_toolbar.setText(R.string.bridge_app);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mHandler = new Handler();
        init();
    }

    /**
     * Initializes the app theme and checks permissions to use bluetooth and storage.
     */
    private void init() {
        version_textView.setText("version: " + BuildConfig.VERSION_NAME);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        if (receiverInformation.getDeviceAddress().equals("Unknown")) {
            select_device_constraintLayout.setVisibility(View.GONE);
            checkPermissions();
        } else {
            checkStatusBLE();
            initialize(0);
        }
        /*int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        SharedPreferences appSettingPrefs = getSharedPreferences(ValueCodes.SETTING_PREFERENCES, 0);
        SharedPreferences.Editor sharedPreferencesEditor = appSettingPrefs.edit();
        isNightModeOn = hour > 25;
        sharedPreferencesEditor.putBoolean(ValueCodes.NIGHT_MODE, isNightModeOn);
        sharedPreferencesEditor.apply();
        isNightModeOn = appSettingPrefs.getBoolean("NightMode", false);

        if (isNightModeOn)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        switch_dark_mode.setChecked(isNightModeOn);*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                switch (permissions[i]) {
                    case "android.permission.ACCESS_FINE_LOCATION":
                        deniedPermissions += "\n- Location";
                        break;
                    case "android.permission.WRITE_EXTERNAL_STORAGE":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (Environment.isExternalStorageManager())
                                break;
                        }
                        deniedPermissions += "\n- Files and media";
                        break;
                }
            }
        }
        checkStatusBLE();
        // Ensures Bluetooth is enabled on the device. If Bluetooth is not currently enabled, fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestPermissionLauncher.launch(enableBtIntent);
        } else if (deniedPermissions.isEmpty()) {
            initialize(ValueCodes.MESSAGE_PERIOD);
        } else {
            showAlert();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!main_title_toolbar.getText().toString().equals(getResources().getString(R.string.bridge_app)))
                showDeviceCategories();
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mConnected) {
                unregisterReceiver(mGattUpdateReceiver);
                unregisterReceiver(mSecondGattUpdateReceiver);
            } else {
                scanLeDevice(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnected) {
            leServiceConnection.close();
        }
    }

    @Override
    public void onAdapterItemClickListener(int position) {
        if (categoryListAdapter.getTypes()[position].contains("Tags")) {
            showBluetoothTags();
            return;
        }
        String type = "";
        if (categoryListAdapter.getTypes()[position].contains("VHF")) {
            main_title_toolbar.setText(R.string.select_vhf_receiver);
            type = "ATSvr";
        } else if (categoryListAdapter.getTypes()[position].contains("Acoustic")) {
            main_title_toolbar.setText(R.string.select_acoustic_receiver);
            type = "ATSar";
        } else if (categoryListAdapter.getTypes()[position].contains("Wildlink")) {
            main_title_toolbar.setText(R.string.select_wildlink);
            type = "ATSwl";
        } else if (categoryListAdapter.getTypes()[position].contains("Bluetooth Receiver")) {
            type = "ATSbr";
        } else if (categoryListAdapter.getTypes()[position].contains("Beacon")) {
            type = "ATSbt";
        }
        mLeDeviceListAdapter.setDeviceType(type);
        scanLeDevice(true);
    }

    private void showDeviceCategories() {
        main_title_toolbar.setText(R.string.bridge_app);
        splash_screen_constraintLayout.setVisibility(View.GONE);
        select_device_constraintLayout.setVisibility(View.VISIBLE);
        searching_devices_linearLayout.setVisibility(View.GONE);
        bridge_app_linearLayout.setVisibility(View.VISIBLE);
        bridge_subtitle_textView.setText(R.string.lb_device_selection);
        bridge_message_textView.setText(R.string.lb_type_of_device);
        types_subtitle_textView.setText(R.string.lb_device_categories);
        categoryListAdapter = new CategoryListAdapter(this, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        category_recyclerView.setLayoutManager(manager);
        category_recyclerView.setHasFixedSize(true);
        category_recyclerView.setAdapter(categoryListAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showBluetoothTags() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        main_title_toolbar.setText(R.string.bluetooth_tags);
        bridge_subtitle_textView.setText(R.string.lb_bluetooth_receive_data);
        bridge_message_textView.setText(R.string.lb_bluetooth_tags_message);
        types_subtitle_textView.setText(R.string.lb_connection_modes);
        categoryListAdapter.setBluetoothTags();
        categoryListAdapter.notifyDataSetChanged();
    }

    private void initialize(int TIME) {
        mLeDeviceListAdapter = new LeDeviceListAdapter(this, connect_button); // Initializes list view adapter.
        new Handler().postDelayed(() -> showDeviceCategories(), TIME);
    }

    /**
     * Method for scanning and displaying available BLE devices.
     * @param enable If true, enable to scan available devices.
     */
    @SuppressLint("MissingPermission")
    private void scanLeDevice(final boolean enable) {
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (enable) {
            mLeDeviceListAdapter.clear();
            setSearchingDevices();
            mBluetoothLeScanner.startScan(mLeScanCallback);

            mHandler.postDelayed(() -> {
                mBluetoothLeScanner.stopScan(mLeScanCallback);

                if (mLeDeviceListAdapter.getItemCount() > 0) { // Available devices were found to display
                    device_recyclerView.setAdapter(mLeDeviceListAdapter);
                    device_recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    setDevicesFound();
                } else { // Unable to find any devices within range
                    setNoDevicesFound();
                }
                invalidateOptionsMenu();
            }, ValueCodes.SCAN_PERIOD);
        } else {
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    /**
     * Checks permissions to be able to use Bluetooth (meaning, Location Permissions if API 23+) and Storage.
     * If Location Permissions are needed, it's capable to ask the user for them.
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_CONNECT");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_SCAN");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(
                        new String[]{Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001); //Any number
            } else {
                initialize(ValueCodes.BRANDING_PERIOD);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_CONNECT");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_SCAN");
            permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(
                        new String[]{
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            } else {
                initialize(ValueCodes.BRANDING_PERIOD);
            }
        } else {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(
                        new String[]{Manifest.permission.BLUETOOTH,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            } else {
                initialize(ValueCodes.BRANDING_PERIOD);
            }
        }
    }

    private void checkStatusBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { // Use this check to determine whether BLE is supported on the device. Then you can selectively disable BLE-related features.
            Log.i(TAG, "THE APP CLOSED CAUSED BY A PROBLEM WITH BLUETOOTH LE");
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE); // Initializes a Bluetooth adapter.
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) { // Checks if Bluetooth is supported on the device.
            Log.i(TAG, "THE APP CLOSED CAUSED BY A PROBLEM WITH BLUETOOTH NOT SUPPORTED");
            finish();
        }
    }

    private void setSearchingDevices() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        select_device_constraintLayout.setVisibility(View.VISIBLE);
        no_device_found_linearLayout.setVisibility(View.GONE);
        bridge_app_linearLayout.setVisibility(View.GONE);
        searching_devices_linearLayout.setVisibility(View.VISIBLE);
        connecting_device_linearLayout.setVisibility(View.GONE);
        devices_subtitle_textView.setText(R.string.lb_searching_devices);
        searching_message_textView.setText(R.string.lb_message_searching);
        searching_progressBar.setVisibility(View.VISIBLE);
        connected_imageView.setVisibility(View.GONE);
        devices_scrollView.setVisibility(View.INVISIBLE);
        connect_button.setEnabled(false);
        connect_button.setAlpha((float) 0.6);
    }

    private void setDevicesFound() {
        devices_subtitle_textView.setText("Found " + mLeDeviceListAdapter.getItemCount() + " Devices");
        searching_message_textView.setText(R.string.lb_select_device);
        searching_progressBar.setVisibility(View.GONE);
        devices_scrollView.setVisibility(View.VISIBLE);
        searching_progressBar.setVisibility(View.GONE);
    }

    private void setNoDevicesFound() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        devices_subtitle_textView.setText(R.string.lb_no_devices_found);
        searching_message_textView.setText(R.string.lb_unable_find_device);
        no_device_found_linearLayout.setVisibility(View.VISIBLE);
        searching_progressBar.setVisibility(View.GONE);
    }

    private void setConnectingDevice() {
        searching_progressBar.setVisibility(View.VISIBLE);
        selected_device_scrollView.removeAllViews();
        LinearLayout device = (LinearLayout) mLeDeviceListAdapter.getSelectedView();
        ((ViewGroup)device.getParent()).removeView(device);
        selected_device_scrollView.addView(device);
        devices_subtitle_textView.setText(R.string.lb_connecting_device);
        searching_message_textView.setText(R.string.lb_connecting_message);
        cancel_button.setVisibility(View.VISIBLE);
        connect_button.setEnabled(false);
        connect_button.setAlpha((float) 0.6);
        devices_scrollView.setVisibility(View.GONE);
        connecting_device_linearLayout.setVisibility(View.VISIBLE);
    }

    private void setDeviceConnected() {
        searching_progressBar.setVisibility(View.GONE);
        connected_imageView.setVisibility(View.VISIBLE);
        devices_subtitle_textView.setText(R.string.lb_success);
        searching_message_textView.setText(R.string.lb_device_connected);
        cancel_button.setVisibility(View.GONE);
    }

    /**
     * Tries to connect to the selected BLE device or shows a connection error.
     */
    private void connectingToDevice() {
        setConnectingDevice();

        leServiceConnection = LeServiceConnection.getInstance();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
        registerReceiver(mSecondGattUpdateReceiver, TransferBleData.makeSecondGattUpdateIntentFilter());
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        TextView disconnect_message = view.findViewById(R.id.disconnect_message);
        disconnect_message.setText(R.string.lb_failed_connect);
        dialog.setView(view);
        dialog.show();

        new Handler().postDelayed(() -> {
                dialog.dismiss();
                scanLeDevice(true);
            }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD); // The message disappears after a pre-defined period and will search for other available BLE devices again
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("App Permissions Required");
        builder.setMessage("To ensure complete functioning of this app please select it your phone's settings and set \"Allow\" for the following permissions:" + deniedPermissions);
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            if (deniedPermissions.contains("Files")) {
                Intent enableBtIntent = new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                int REQUEST_STORAGE = 1;
                startActivityForResult(enableBtIntent, REQUEST_STORAGE);
            }
            finish();
        });
        builder.show();
    }

    /**
     * With the received packet, get state of board.
     * @param data The received packet.
     */
    @SuppressLint("MissingPermission")
    private void downloadBoardState(byte[] data) {
        if (mLeDeviceListAdapter.getSelectedDevice().getName().contains("vr")) {
            ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
            receiverInformation.changeSDCard(data[7]);
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
        }
    }

    /**
     * With the received packet, get state of scan.
     * @param data The received packet.
     */
    private void downloadScanning(byte[] data) {
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.setStatusData(data);
    }

    @SuppressLint("MissingPermission")
    private void connectVhfReceiver() {
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.changeDeviceBattery(Converters.getPercentBatteryVhfReceiver(mLeDeviceListAdapter.getScanRecord()));
        Intent intent = new Intent(this, VhfMenuActivity.class);
        switch (Converters.getHexValue(receiverInformation.getStatusData()[1])) {
            case "82": // The BLE device is in aerial scanning
            case "81":
            case "80":
                intent = new Intent(this, VhfMobileScanActivity.class);
                break;
            case "83": // The BLE device is in stationary scanning
                intent = new Intent(this, VhfStationaryScanActivity.class);
                break;
            case "86": // The BLE device is in manual scanning
                intent = new Intent(this, VhfManualScanActivity.class);
                break;
            default:
                intent.putExtra(VhfMenuActivity.EXTRAS_DEVICE_STATUS, mLeDeviceListAdapter.getSelectedDevice().getName().substring(15, 16));
                intent.putExtra("menu", false);
        }
        intent.putExtra(ValueCodes.VALUE, receiverInformation.getStatusData());
        intent.putExtra(ValueCodes.SCANNING, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}