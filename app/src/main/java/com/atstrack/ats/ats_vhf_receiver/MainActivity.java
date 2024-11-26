package com.atstrack.ats.ats_vhf_receiver;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atstrack.ats.ats_vhf_receiver.Adapters.LeDeviceListAdapter;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.location_enable_linearLayout)
    LinearLayout location_enable_linearLayout;
    @BindView(R.id.location_textView)
    TextView location_textView;
    @BindView(R.id.location_button)
    Button location_button;
    @BindView(R.id.bluetooth_enable_linearLayout)
    LinearLayout bluetooth_enable_linearLayout;
    @BindView(R.id.bluetooth_textView)
    TextView bluetooth_textView;
    @BindView(R.id.bluetooth_button)
    Button bluetooth_button;
    @BindView(R.id.version_textView)
    TextView version_textView;
    @BindView(R.id.switch_dark_mode)
    SwitchCompat switch_dark_mode;
    @BindView(R.id.searching_receivers_linearLayout)
    LinearLayout searching_receivers_linearLayout;
    @BindView(R.id.device_recyclerView)
    RecyclerView device_recyclerView;
    @BindView(R.id.retry_linearLayout)
    LinearLayout retry_linearLayout;
    @BindView(R.id.retry_button)
    Button retry_button;
    @BindView(R.id.update_linearLayout)
    LinearLayout update_linearLayout;
    @BindView(R.id.devices_linearLayout)
    LinearLayout devices_linearLayout;
    @BindView(R.id.branding_constraintLayout)
    ConstraintLayout branding_constraintLayout;
    @BindView(R.id.searching_receivers_constraintLayout)
    ConstraintLayout searching_receivers_constraintLayout;
    @BindView(R.id.refresh_button)
    ImageButton refresh_button;

    private final static String TAG = MainActivity.class.getSimpleName();

    private SharedPreferences.Editor sharedPreferencesEditor;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
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
                Log.i("MAIN ACTIVITY", "RESULT: " + result.getResultCode());
                    if (result.getResultCode() == -1)
                        initializeScan(ValueCodes.MESSAGE_PERIOD);
                    else {
                        Log.i(TAG, "THE APP CLOSED CAUSED BY A PROBLEM WITH LAUNCHER");
                        finish();
                    }
            });

    @OnClick(R.id.location_button)
    public void enableLocation(View v) {
        location_enable_linearLayout.setVisibility(View.GONE);
        Intent enableLocIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        int REQUEST_ENABLE_LOC = 1;
        startActivityForResult(enableLocIntent, REQUEST_ENABLE_LOC);
    }

    @SuppressLint("MissingPermission")
    @OnClick(R.id.bluetooth_button)
    public void enableBluetooth(View v) {
        bluetooth_enable_linearLayout.setVisibility(View.GONE);
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT = 1;
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @OnClick(R.id.refresh_button)
    public void onClickRefresh(View v) {
        devices_linearLayout.setVisibility(View.GONE);
        searching_receivers_linearLayout.setVisibility(View.VISIBLE);
        refresh_button.setAlpha((float) 0.6);
        refresh_button.setEnabled(false);
        scanLeDevice(true);
    }

    @OnClick(R.id.retry_button)
    public void onClickRetry(View v) {
        retry_linearLayout.setVisibility(View.GONE);
        retry_button.setVisibility(View.GONE);
        searching_receivers_linearLayout.setVisibility(View.VISIBLE);
        scanLeDevice(true);
    }

    @OnClick(R.id.switch_dark_mode)
    public void onDarkModeClick(View v) {
        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sharedPreferencesEditor.putBoolean(ValueCodes.NIGHT_MODE, false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sharedPreferencesEditor.putBoolean(ValueCodes.NIGHT_MODE, true);
        }
        sharedPreferencesEditor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String version = "version: " + BuildConfig.VERSION_NAME;
        version_textView.setText(version);
        mHandler = new Handler();
        init();
    }

    /**
     * Initializes the app theme and checks permissions to use bluetooth and storage.
     */
    private void init() {
        searching_receivers_constraintLayout.setVisibility(View.GONE);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        SharedPreferences appSettingPrefs = getSharedPreferences(ValueCodes.SETTING_PREFERENCES, 0);
        sharedPreferencesEditor = appSettingPrefs.edit();
        isNightModeOn = hour > 25;
        sharedPreferencesEditor.putBoolean(ValueCodes.NIGHT_MODE, isNightModeOn);
        sharedPreferencesEditor.apply();
        //isNightModeOn = appSettingPrefs.getBoolean("NightMode", false);

        if (isNightModeOn)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        switch_dark_mode.setChecked(isNightModeOn);

        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                finish();
                return;
            }
        }*/
        Log.i(TAG, "AFTER CHECK GRANT RESULTS " + requestCode);

        // Use this check to determine whether BLE is supported on the device. Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "THE APP CLOSED CAUSED BY A PROBLEM WITH BLUETOOTH LE");
            finish();
        }

        // Initializes a Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        Log.i(TAG, "AFTER GET ADAPTER BLUETOOTH " + requestCode);

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            //Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "THE APP CLOSED CAUSED BY A PROBLEM WITH BLUETOOTH NOT SUPPORTED");
            finish();
        }
        Log.i(TAG, "AFTER ADAPTER " + requestCode);

        // Ensures Bluetooth is enabled on the device. If Bluetooth is not currently enabled, fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestPermissionLauncher.launch(enableBtIntent);
            Log.i(TAG, "AFTER BLUETOOTH NO ENABLE");
        } else {
            initializeScan(ValueCodes.MESSAGE_PERIOD);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
            mLeDeviceListAdapter.clear();
        }
    }

    private void initializeScan(int TIME) {
        mLeDeviceListAdapter = new LeDeviceListAdapter(this); // Initializes list view adapter.
        new Handler().postDelayed(() -> {
            branding_constraintLayout.setVisibility(View.GONE);
            searching_receivers_constraintLayout.setVisibility(View.VISIBLE);
            scanLeDevice(true);
        }, TIME);
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
            refresh_button.setAlpha((float) 0.6);
            refresh_button.setEnabled(false);
            retry_linearLayout.setVisibility(View.GONE);
            retry_button.setVisibility(View.GONE);
            devices_linearLayout.setVisibility(View.GONE);
            searching_receivers_linearLayout.setVisibility(View.VISIBLE);
            mBluetoothLeScanner.startScan(mLeScanCallback);

            mHandler.postDelayed(() -> {
                mBluetoothLeScanner.stopScan(mLeScanCallback);
                searching_receivers_linearLayout.setVisibility(View.GONE);

                if (mLeDeviceListAdapter.getItemCount() > 0) { // Available devices were found to display
                    devices_linearLayout.setVisibility(View.VISIBLE);
                    device_recyclerView.setAdapter(mLeDeviceListAdapter);
                    device_recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    refresh_button.setVisibility(View.VISIBLE);
                    refresh_button.setAlpha((float) 1);
                    refresh_button.setEnabled(true);
                } else { // Unable to find any devices within range
                    devices_linearLayout.setVisibility(View.GONE);
                    retry_linearLayout.setVisibility(View.VISIBLE);
                    retry_button.setVisibility(View.VISIBLE);
                    refresh_button.setVisibility(View.GONE);
                }
                invalidateOptionsMenu();
            }, ValueCodes.SCAN_PERIOD);
        } else {
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Checks permissions to be able to use Bluetooth (meaning, Location Permissions if API 23+) and Storage.
     * If Location Permissions are needed, it's capable to ask the user for them.
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_CONNECT");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_SCAN");
            permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.MANAGE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(
                        new String[]{Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_ADVERTISE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001); //Any number
            } else {
                initializeScan(ValueCodes.BRANDING_PERIOD);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_CONNECT");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_SCAN");
            permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.MANAGE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(
                        new String[]{
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            } else {
                initializeScan(ValueCodes.BRANDING_PERIOD);
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
                initializeScan(ValueCodes.BRANDING_PERIOD);
            }
        }
        Log.i(TAG, "AFTER REQUEST " + Build.VERSION.SDK_INT);
    }

    /**
     * Checks if location mode is enabled to use.
     * @return Returns true, if the location mode is not off.
     */
    private boolean isLocationEnable() {
        int locationMode;
        try {
            locationMode = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }
}