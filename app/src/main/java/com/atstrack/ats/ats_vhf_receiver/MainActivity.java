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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.CategoryListAdapter;
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

    private final static String TAG = MainActivity.class.getSimpleName();

    private CategoryListAdapter categoryListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private String deniedPermissions = "";
    private boolean isNightModeOn;

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
        init();
    }

    /**
     * Initializes the app theme and checks permissions to use bluetooth and storage.
     */
    private void init() {
        version_textView.setText("version: " + BuildConfig.VERSION_NAME);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        if (receiverInformation.getDeviceAddress().equals("Unknown")) {
            bridge_app_linearLayout.setVisibility(View.GONE);
            checkPermissions();
        } else {
            receiverInformation.initialize();
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
            initialize(ValueCodes.MESSAGE_PERIOD);
            return;
        } else if (deniedPermissions.contains("BLUETOOTH")) {
            deniedPermissions = deniedPermissions.replace("BLUETOOTH", "");
            deniedPermissions += "\n- Nearby devices";
        }
        showAlert();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            if (!main_title_toolbar.getText().toString().equals(getResources().getString(R.string.bridge_app)))
                showDeviceCategories();
            else
                finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onAdapterItemClickListener(int position) {
        if (categoryListAdapter.getTypes()[position].contains("Tags")) {
            showBluetoothTags();
            return;
        }
        String type = "";
        if (categoryListAdapter.getTypes()[position].contains("VHF"))
            type = "ATSvr";
        else if (categoryListAdapter.getTypes()[position].contains("Acoustic"))
            type = "ATSar";
        else if (categoryListAdapter.getTypes()[position].contains("Wildlink"))
            type = "ATSwl";
        else if (categoryListAdapter.getTypes()[position].contains("Bluetooth Receiver"))
            type = "ATSbr";
        else if (categoryListAdapter.getTypes()[position].contains("Beacon"))
            type = "ATSbt";
        Intent intent = new Intent(this, ScanDevicesActivity.class);
        intent.putExtra(ValueCodes.TYPE, type);
        startActivity(intent);
    }

    private void showDeviceCategories() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        main_title_toolbar.setText(R.string.bridge_app);
        splash_screen_constraintLayout.setVisibility(View.GONE);
        bridge_app_linearLayout.setVisibility(View.VISIBLE);
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
        new Handler().postDelayed(() -> showDeviceCategories(), TIME);
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
}