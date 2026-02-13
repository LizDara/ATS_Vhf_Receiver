package com.atstrack.ats.ats_vhf_receiver;

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
import android.os.Build;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.LeDeviceListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ManualScanActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.MenuActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.MobileScanActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.StationaryScanActivity;

import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanDevicesActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.searching_progressBar)
    ProgressBar searching_progressBar;
    @BindView(R.id.searching_devices_linearLayout)
    LinearLayout searching_devices_linearLayout;
    @BindView(R.id.devices_subtitle_textView)
    TextView devices_subtitle_textView;
    @BindView(R.id.searching_message_textView)
    TextView searching_message_textView;
    @BindView(R.id.items_scrollView)
    ScrollView items_scrollView;
    @BindView(R.id.item_recyclerView)
    RecyclerView item_recyclerView;
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

    private final static String TAG = ScanDevicesActivity.class.getSimpleName();

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private LeServiceConnection leServiceConnection;
    private Timer connectionTimeout;
    private boolean mConnected, cancel, readBoardStatus = false;
    private String parameter = "";

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

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
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
                        getDeviceStatus();
                    setDeviceConnected();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    Log.i(TAG, Converters.getHexValue(packet) + Calendar.getInstance().get(Calendar.MINUTE) + "." + Calendar.getInstance().get(Calendar.SECOND));
                    if (Converters.getHexValue(packet[0]).equals("41")) // Get board state
                        downloadBoardState(packet);
                    else if (Converters.getHexValue(packet[0]).equals("50")) // Check if the BLE device is scanning
                        downloadScanStatus(packet);
                }
            } catch (Exception e) {
                if (!cancel && leServiceConnection.existConnection() && mConnected && mLeDeviceListAdapter.getSelectedDevice().getName().contains(ValueCodes.VHF)) {
                    parameter = "";
                    getDeviceStatus();
                    Log.w(TAG, e.getLocalizedMessage());
                }
            }
        }
    };

    /**
     * Requests a read for scan state.
     */
    private void getDeviceStatus() {
        parameter = "";
        TransferBleData.notificationLog();
        if (!cancel && mConnected) {
            new Handler().postDelayed(() -> {
                TransferBleData.readBoardState();
                Log.i(TAG, "SEND READ BOARD " + Calendar.getInstance().get(Calendar.MINUTE) + "." + Calendar.getInstance().get(Calendar.SECOND));
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
        initializeParameters();
        mBluetoothLeScanner.stopScan(mLeScanCallback);
        BluetoothDevice device = mLeDeviceListAdapter.getSelectedDevice();
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.setInformation(device.getName(), device.getAddress(), "0%");
        Log.i(TAG, "CONNECT DEVICE " + Calendar.getInstance().get(Calendar.MINUTE) + "." + Calendar.getInstance().get(Calendar.SECOND));

        setConnectingDevice();
        leServiceConnection = LeServiceConnection.getInstance();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
        mRegisterReceiver();

        if (device.getName().contains(ValueCodes.VHF))
            parameter = ValueCodes.SCAN_STATUS;

        connectionTimeout.schedule(new TimerTask() { //create timer for connection timeout
            @Override
            public void run() {
                Log.i(TAG, "SCAN TIMEOUT " + Calendar.getInstance().get(Calendar.MINUTE) + "." + Calendar.getInstance().get(Calendar.SECOND));
                if (!cancel && mConnected && !device.getName().contains(ValueCodes.VHF)) {
                    connectionTimeout.cancel();
                    connectionTimeout.purge();
                    if (device.getName().contains(ValueCodes.ACOUSTIC)) {
                        showAcousticReceiverMenu();
                    } else if (device.getName().contains(ValueCodes.BLUETOOTH_RECEIVER)) {
                        showBluetoothReceiverMenu();
                    }
                } else {
                    showDisconnectionMessage(); //Connection timeout, make sure you write mac address correct and ble device is discoverable
                }
            }
        }, ValueCodes.CONNECT_TIMEOUT);
    }

    @SuppressLint("MissingPermission")
    @OnClick(R.id.cancel_button)
    public void onClickCancel(View v) {
        cancelConnection();
        setDevicesFound();
        mBluetoothLeScanner.startScan(mLeScanCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_devices);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String type = getIntent().getStringExtra(ValueCodes.TYPE);
        state_view.setVisibility(View.GONE);
        setToolbarTitle(type);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this, connect_button, devices_subtitle_textView, searching_message_textView); // Initializes list view adapter.
        mLeDeviceListAdapter.setDeviceType(type);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE); // Initializes a Bluetooth adapter.
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mConnected)
            mRegisterReceiver();
        else if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
            scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mConnected)
                unregisterReceiver(mGattUpdateReceiver);
            else if (leServiceConnection != null && !leServiceConnection.existConnection())
                scanLeDevice(false);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception ex) {
            Log.w(TAG, "Failed to unregister receiver");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setToolbarTitle(String type) {
        switch (type) {
            case "ATSvr":
                title_toolbar.setText(R.string.select_vhf_receiver);
                break;
            case "ATSar":
                title_toolbar.setText(R.string.select_acoustic_receiver);
                break;
            case "ATSwl":
                title_toolbar.setText(R.string.select_wildlink);
                break;
            case "ATSbr":
                title_toolbar.setText(R.string.select_bluetooth_beacon);
                break;
            default:
                title_toolbar.setText("SELECT DEVICE");
                break;
        }
    }

    private void initializeParameters() {
        mConnected = cancel = false;
        parameter = "";
        connectionTimeout = new Timer();
    }

    private void cancelConnection() {
        cancel = true;
        mConnected = false;
        readBoardStatus = false;
        parameter = "";
        unbindService(leServiceConnection.getServiceConnection());
        if (leServiceConnection.existConnection()) leServiceConnection.close();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private void setSearchingDevices() {
        no_device_found_linearLayout.setVisibility(View.GONE);
        searching_devices_linearLayout.setVisibility(View.VISIBLE);
        connecting_device_linearLayout.setVisibility(View.GONE);
        devices_subtitle_textView.setText(R.string.lb_searching_devices);
        searching_message_textView.setText(R.string.lb_message_searching);
        searching_progressBar.setVisibility(View.VISIBLE);
        connected_imageView.setVisibility(View.GONE);
        items_scrollView.setVisibility(View.INVISIBLE);
        connect_button.setEnabled(false);
        connect_button.setAlpha((float) 0.6);
    }

    private void setDevicesFound() {
        devices_subtitle_textView.setText("Found " + mLeDeviceListAdapter.getItemCount() + " Devices");
        searching_message_textView.setText(R.string.lb_select_device);
        searching_progressBar.setVisibility(View.GONE);
        items_scrollView.setVisibility(View.VISIBLE);
        selected_device_scrollView.setVisibility(View.GONE);
        searching_progressBar.setVisibility(View.GONE);
        cancel_button.setVisibility(View.GONE);
    }

    private void setNoDevicesFound() {
        devices_subtitle_textView.setText(R.string.lb_no_devices_found);
        searching_message_textView.setText(R.string.lb_unable_find_device);
        no_device_found_linearLayout.setVisibility(View.VISIBLE);
        searching_progressBar.setVisibility(View.GONE);
    }

    private void setConnectingDevice() {
        searching_progressBar.setVisibility(View.VISIBLE);
        selected_device_scrollView.setVisibility(View.VISIBLE);
        selected_device_scrollView.removeAllViews();
        LinearLayout device = (LinearLayout) mLeDeviceListAdapter.getSelectedView();
        ((ViewGroup)device.getParent()).removeView(device);
        selected_device_scrollView.addView(device);
        devices_subtitle_textView.setText(R.string.lb_connecting_device);
        searching_message_textView.setText(R.string.lb_connecting_message);
        cancel_button.setVisibility(View.VISIBLE);
        connect_button.setEnabled(false);
        connect_button.setAlpha((float) 0.6);
        items_scrollView.setVisibility(View.GONE);
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

            new Handler().postDelayed(() -> {
                if (mLeDeviceListAdapter.getItemCount() > 0) { // Available devices were found to display
                    item_recyclerView.setAdapter(mLeDeviceListAdapter);
                    item_recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    setDevicesFound();
                } else { // Unable to find any devices within range
                    mBluetoothLeScanner.stopScan(mLeScanCallback);
                    setNoDevicesFound();
                }
                invalidateOptionsMenu();
            }, ValueCodes.SCAN_PERIOD);
        } else {
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        Context activity = this;
        runOnUiThread(new Runnable() {
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(activity);
                View view = inflater.inflate(R.layout.disconnect_message, null);
                final AlertDialog dialog = new AlertDialog.Builder(activity).create();
                TextView disconnect_message = view.findViewById(R.id.disconnect_message);
                disconnect_message.setText(R.string.lb_failed_connect);
                dialog.setView(view);
                dialog.show();

                new Handler().postDelayed(() -> {
                    dialog.dismiss();
                    if (leServiceConnection.existConnection())
                        leServiceConnection.close();
                    finish();
                }, ValueCodes.MESSAGE_PERIOD); // The message disappears after a pre-defined period and will search for other available BLE devices again
            }
        });
    }

    /**
     * With the received packet, get state of board.
     * @param data The received packet.
     */
    private void downloadBoardState(byte[] data) {
        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
        sharedPreferencesEdit.putString(ValueCodes.VERSION, "1.0.0");
        int baseFrequency = Integer.parseInt(Converters.getDecimalValue(data[2]));
        int range = Integer.parseInt(Converters.getDecimalValue(data[3]));
        sharedPreferencesEdit.putInt(ValueCodes.BASE_FREQUENCY, baseFrequency);
        sharedPreferencesEdit.putInt(ValueCodes.RANGE, range);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        sharedPreferencesEdit.putInt(ValueCodes.WIDTH, metrics.widthPixels);
        sharedPreferencesEdit.putInt(ValueCodes.HEIGHT, metrics.heightPixels);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.changeSDCard(Converters.getHexValue(data[7]).equals("01"));
        sharedPreferencesEdit.apply();
        readBoardStatus = true;
    }

    /**
     * With the received packet, get state of scan.
     * @param data The received packet.
     */
    private void downloadScanStatus(byte[] data) {
        if (readBoardStatus) {
            connectionTimeout.cancel();
            connectionTimeout.purge();
            showVhfReceiverMenu(data);
        }
    }

    private void showVhfReceiverMenu(byte[] data) {
        Intent intent = new Intent(this, MenuActivity.class);
        switch (Converters.getHexValue(data[1])) {
            case "82": // The BLE device is in aerial scanning
            case "81":
            case "80":
                intent = new Intent(this, MobileScanActivity.class);
                intent.putExtra(ValueCodes.SCANNING, true);
                break;
            case "83": // The BLE device is in stationary scanning
                intent = new Intent(this, StationaryScanActivity.class);
                intent.putExtra(ValueCodes.SCANNING, true);
                break;
            case "86": // The BLE device is in manual scanning
                intent = new Intent(this, ManualScanActivity.class);
                intent.putExtra(ValueCodes.SCANNING, true);
                break;
            default:
                intent.putExtra(ValueCodes.FIRST_TIME, true);
        }
        intent.putExtra(ValueCodes.VALUE, data);
        startActivity(intent);
        finish();
    }

    private void showAcousticReceiverMenu() {
        Intent intent = new Intent(getBaseContext(), com.atstrack.ats.ats_vhf_receiver.Acoustic.MenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void showBluetoothReceiverMenu() {
        Intent intent = new Intent(this, com.atstrack.ats.ats_vhf_receiver.BluetoothReceiver.MenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void mRegisterReceiver() {
        if (Build.VERSION.SDK_INT >= 33)
            registerReceiver(mGattUpdateReceiver, TransferBleData.makeGattUpdateIntentFilter(), 2);
        else
            registerReceiver(mGattUpdateReceiver, TransferBleData.makeGattUpdateIntentFilter());
    }
}