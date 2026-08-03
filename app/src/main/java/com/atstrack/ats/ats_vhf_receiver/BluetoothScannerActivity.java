package com.atstrack.ats.ats_vhf_receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;

public class BluetoothScannerActivity extends AppCompatActivity {
    protected final static String TAG = ScanDevicesActivity.class.getSimpleName();
    protected ViewBinding binding = null;
    protected int contentViewId;
    protected String title;

    protected BluetoothAdapter mBluetoothAdapter;
    protected BluetoothLeScanner mBluetoothLeScanner;
    protected ScanCallback mLeScanCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// Keep screen on
        ActivitySetting.setToolbar(this, title, "");
        initScanCallback();
    }

    @Override
    protected void onDestroy() {
        scanLeDevice(false);
        super.onDestroy();
    }

    protected void initScanCallback() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE); // Initializes a Bluetooth adapter.
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }
    /**
     * Method for scanning and displaying available BLE devices.
     * @param enable If true, enable to scan available devices.
     */
    @SuppressLint("MissingPermission")
    protected void scanLeDevice(final boolean enable) {
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (enable) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            mBluetoothLeScanner.startScan(null, settings, mLeScanCallback);
        } else {
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }
}
