package com.atstrack.ats.ats_vhf_receiver;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.atstrack.ats.ats_vhf_receiver.Adapters.CategoryAdapter;
import com.atstrack.ats.ats_vhf_receiver.BeaconTag.BeaconTagDetectionActivity;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnAdapterClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding = null;
    private CategoryAdapter categoryListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private String deniedPermissions = "";
    private boolean isNightModeOn;

    ActivityResultLauncher<Intent> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == -1) {
                    if (!this.deniedPermissions.isEmpty())
                        showAlertDialog();
                } else {
                    this.deniedPermissions += "\n- Nearby devices";
                    showAlertDialog();
                }
            });

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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.includeToolbar.tbMain);
        binding.includeToolbar.vState.setVisibility(View.GONE);
        binding.includeToolbar.tvTitleToolbar.setText(R.string.title_bridge_app);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
    }

    /**
     * Initializes the app theme and checks permissions to use bluetooth and storage.
     */
    private void init() {
        checkPermissions();
        checkStatusBLE();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.includeRecyclerView.rvItem.setLayoutManager(manager);
        binding.includeRecyclerView.rvItem.setHasFixedSize(true);
        categoryListAdapter = new CategoryAdapter(this, this);
        binding.includeRecyclerView.rvItem.setAdapter(categoryListAdapter);
        setVisibility(ValueCodes.CATEGORIES);
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
                    case "android.permission.BLUETOOTH_SCAN":
                        deniedPermissions += "BLUETOOTH";
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
        checkStatusBLE(); // Ensures Bluetooth is enabled on the device. If Bluetooth is not currently enabled, fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            deniedPermissions = deniedPermissions.replace("BLUETOOTH", "");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestPermissionLauncher.launch(enableBtIntent);
            return;
        } else if (deniedPermissions.isEmpty()) {
            return;
        } else if (deniedPermissions.contains("BLUETOOTH")) {
            deniedPermissions = deniedPermissions.replace("BLUETOOTH", "");
            deniedPermissions += "\n- Nearby devices";
        }
        showAlertDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!binding.includeToolbar.tvTitleToolbar.getText().toString().equals(getResources().getString(R.string.title_bridge_app)))
                setVisibility(ValueCodes.CATEGORIES);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAdapterItemClickListener(int position) {
        if (categoryListAdapter.types[position].contains("BT2400")) {
            setVisibility(ValueCodes.BLUETOOTH_TAGS);
            return;
        }
        if (categoryListAdapter.types[position].contains("Beacon")) {
            Intent intent = new Intent(this, BeaconTagDetectionActivity.class);
            startActivity(intent);
            return;
        }
        String type = "";
        if (categoryListAdapter.types[position].contains("VHF"))
            type = ValueCodes.VHF;
        else if (categoryListAdapter.types[position].contains("Wildlink"))
            type = ValueCodes.WILDLINK;
        else if (categoryListAdapter.types[position].contains("BluTrack"))
            type = ValueCodes.BLUETOOTH_RECEIVER;
        Intent intent = new Intent(this, ScanDevicesActivity.class);
        intent.putExtra(ValueCodes.TYPE, type);
        startActivity(intent);
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.CATEGORIES) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            binding.includeToolbar.tvTitleToolbar.setText(R.string.title_bridge_app);
            binding.tvBridgeSubtitle.setText(R.string.lbl_bridge_app_device_selection);
            binding.tvBridgeMessage.setText(R.string.lbl_bridge_app_device_type);
            binding.tvTypesSubtitle.setText(R.string.lbl_bridge_app_device_categories);
            categoryListAdapter.types = getResources().getStringArray(R.array.array_bridge_app_categories);
        } else if (view == ValueCodes.BLUETOOTH_TAGS) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            binding.includeToolbar.tvTitleToolbar.setText(R.string.title_bluetooth_tags);
            binding.tvBridgeSubtitle.setText(R.string.lbl_bluetooth_tags_receive_data);
            binding.tvBridgeMessage.setText(R.string.lbl_bluetooth_tags_message);
            binding.tvTypesSubtitle.setText(R.string.lbl_bluetooth_tags_connection_modes);
            categoryListAdapter.types = getResources().getStringArray(R.array.array_bridge_app_connection_modes);
        }
        categoryListAdapter.notifyDataSetChanged();
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

    private void showAlertDialog() {
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
}