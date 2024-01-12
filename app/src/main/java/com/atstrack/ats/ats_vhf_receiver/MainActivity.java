package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeDeviceListAdapter;

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
    @BindView(R.id.anim_spinner)
    ImageView anim_spinner;
    @BindView(R.id.branding_constraintLayout)
    ConstraintLayout branding_constraintLayout;
    @BindView(R.id.searching_receivers_constraintLayout)
    ConstraintLayout searching_receivers_constraintLayout;
    @BindView(R.id.refresh_button)
    ImageButton refresh_button;

    private boolean isNightModeOn;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 8 seconds.
    private static final long SCAN_PERIOD = 8000;
    private static long MESSAGE_PERIOD = 2000;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;

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

    @OnClick(R.id.location_button)
    public void enableLocation(View v) {
        location_enable_linearLayout.setVisibility(View.GONE);
        Intent enableLocIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        int REQUEST_ENABLE_LOC = 1;
        startActivityForResult(enableLocIntent, REQUEST_ENABLE_LOC);
    }

    @OnClick (R.id.bluetooth_button)
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
            sharedPreferencesEditor.putBoolean("NightMode", false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sharedPreferencesEditor.putBoolean("NightMode", true);
        }
        sharedPreferencesEditor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String version = "version: " + BuildConfig.VERSION_NAME;
        version_textView.setText(version);

        init();

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initializes the app theme and checks permissions to use bluetooth and storage.
     */
    private void init() {
        searching_receivers_constraintLayout.setVisibility(View.GONE);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        SharedPreferences appSettingPrefs = getSharedPreferences("AppSettingPrefs", 0);
        sharedPreferencesEditor = appSettingPrefs.edit();
        isNightModeOn = hour > 25;
        sharedPreferencesEditor.putBoolean("NightMode", isNightModeOn);
        sharedPreferencesEditor.apply();
        //isNightModeOn = appSettingPrefs.getBoolean("NightMode", false);

        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            switch_dark_mode.setChecked(true);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            switch_dark_mode.setChecked(false);
        }

        checkPermissions();
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            bluetooth_enable_linearLayout.setVisibility(View.VISIBLE);
        }
        if (!isLocationEnable()) {
            location_enable_linearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        new Handler().postDelayed(() -> {
            branding_constraintLayout.setVisibility(View.GONE);
            searching_receivers_constraintLayout.setVisibility(View.VISIBLE);
            scanLeDevice(true);
        }, MESSAGE_PERIOD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    /**
     * Method for scanning and displaying available BLE devices.
     *
     * @param enable If true, enable to scan available devices.
     */
    private void scanLeDevice(final boolean enable) {
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (enable) {
            // Initializes the spinner to search for available devices
            anim_spinner.setImageDrawable((AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_anim_spinner_48));
            Drawable drawable = anim_spinner.getDrawable();
            Animatable animatable = (Animatable) drawable;
            AnimatedVectorDrawableCompat.registerAnimationCallback(drawable, new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    new Handler().postDelayed(animatable::start, SCAN_PERIOD);
                }
            });
            animatable.start();

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
                Log.i("MainActivity", "COUNT: " + mLeDeviceListAdapter.getItemCount());

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
            }, SCAN_PERIOD);
        } else {
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i("MainActivity", "SDK S: " + Build.VERSION.SDK_INT);
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_CONNECT");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_SCAN");
            permissionCheck += this.checkSelfPermission("Manifest.permission.MANAGE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");

            if (permissionCheck != 0) {
                MESSAGE_PERIOD = 9000;
                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN}, 1001); //Any number
            }
        } else {
            Log.i("MainActivity", "NO SDK S: " + Build.VERSION.SDK_INT);
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                MESSAGE_PERIOD = 5000;
                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
    }

    /**
     * Checks if location mode is enabled to use.
     *
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
