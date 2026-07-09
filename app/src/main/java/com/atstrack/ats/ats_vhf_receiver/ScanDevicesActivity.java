package com.atstrack.ats.ats_vhf_receiver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.DeviceAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Models.DeviceData;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ManualScanActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.MenuActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.MobileScanActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.StartScanningActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.StationaryScanActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

public class ScanDevicesActivity extends BluetoothScannerActivity {

    @BindView(R.id.pb_searching)
    ProgressBar pb_searching;
    @BindView(R.id.tv_devices_subtitle)
    TextView tv_devices_subtitle;
    @BindView(R.id.tv_searching_message)
    TextView tv_searching_message;
    @BindView(R.id.rv_item)
    RecyclerView rv_item;
    @BindView(R.id.img_connected)
    ImageView img_connected;
    @BindView(R.id.btn_retry)
    Button btn_retry;
    @BindView(R.id.btn_cancel)
    Button btn_cancel;
    @BindView(R.id.btn_connect)
    Button btn_connect;

    private DeviceAdapter deviceAdapter;
    private LeServiceConnection leServiceConnection;
    private Timer connectionTimeout;
    private boolean mConnected, cancel, readBoardStatus, readScanStatus = false;
    private byte parameter = ValueCodes.NONE;
    private AlertDialog disconnectionDialog;
    private Handler messageHandler;

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
                    showDisconnectionAlertDialog();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter == ValueCodes.SCAN_STATE_COMMAND)
                        getDeviceStatus();
                    setVisibility(ValueCodes.CONNECTED);
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    if (packet[0] == ValueCodes.BOARD_STATUS_COMMAND)
                        downloadBoardState(packet);
                    else if (packet[0] == ValueCodes.SCAN_STATE_COMMAND)
                        downloadScanStatus(packet);
                }
            } catch (Exception e) {
                if (!cancel && leServiceConnection.existConnection() && mConnected && deviceAdapter.deviceType.contains(ValueCodes.VHF)) {
                    parameter = ValueCodes.NONE;
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
        parameter = ValueCodes.NONE;
        TransferBleData.notificationLog(true);
        if (!cancel && mConnected) {
            messageHandler.postDelayed(() -> {
                TransferBleData.readBoardStatus();
                Log.i(TAG, "SEND READ BOARD " + Calendar.getInstance().get(Calendar.MINUTE) + "." + Calendar.getInstance().get(Calendar.SECOND));
            }, ValueCodes.WAITING_PERIOD);
        }
    }

    @OnClick(R.id.btn_retry)
    public void onClickRetry(View v) {
        scanLeDevice(true);
    }

    @SuppressLint("MissingPermission")
    @OnClick(R.id.btn_connect)
    public void onClickConnect(View v) {
        initializeParameters();
        mBluetoothLeScanner.stopScan(mLeScanCallback);
        setVisibility(ValueCodes.CONNECTING);
        DeviceData device = deviceAdapter.selectedDevice.get(0);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.setInformation(device.bluetoothDevice.getName(), device.bluetoothDevice.getAddress(), device.batteryPercent);

        leServiceConnection = LeServiceConnection.getInstance();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
        mRegisterReceiver();

        if (deviceAdapter.deviceType.contains(ValueCodes.VHF))
            parameter = ValueCodes.SCAN_STATE_COMMAND;

        connectionTimeout.schedule(new TimerTask() { //create timer for connection timeout
            @Override
            public void run() {
                Log.i(TAG, "SCAN TIMEOUT " + Calendar.getInstance().get(Calendar.MINUTE) + "." + Calendar.getInstance().get(Calendar.SECOND));
                if (!cancel && mConnected && !deviceAdapter.deviceType.contains(ValueCodes.VHF)) {
                    connectionTimeout.cancel();
                    connectionTimeout.purge();
                    if (deviceAdapter.deviceType.contains(ValueCodes.ACOUSTIC)) {
                        showAcousticReceiverMenu();
                    } else if (deviceAdapter.deviceType.contains(ValueCodes.BLUETOOTH_RECEIVER)) {
                        showBluetoothReceiverMenu();
                    }
                } else {
                    showDisconnectionAlertDialog(); //Connection timeout, make sure you write mac address correct and ble device is discoverable
                }
            }
        }, ValueCodes.CONNECT_TIMEOUT);
    }

    @SuppressLint("MissingPermission")
    @OnClick(R.id.btn_cancel)
    public void onClickCancel(View v) {
        cancelConnection();
        setVisibility(ValueCodes.FOUNDED);
        mBluetoothLeScanner.startScan(mLeScanCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_scan_devices;
        String type = getIntent().getStringExtra(ValueCodes.TYPE);
        setToolbarTitle(type);
        super.onCreate(savedInstanceState);

        deviceAdapter = new DeviceAdapter(this, type, btn_connect, tv_devices_subtitle, tv_searching_message); // Initializes list view adapter.
        messageHandler = new Handler();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (disconnectionDialog != null && disconnectionDialog.isShowing())
            disconnectionDialog.dismiss();
        if (messageHandler != null)
            messageHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void initScanCallback() {
        super.initScanCallback();
        mLeScanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result.getDevice().getName() != null)
                    deviceAdapter.addDevice(result.getDevice(), result.getScanRecord().getBytes());
            }
        };
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.SEARCHING) {
            btn_retry.setVisibility(View.GONE);
            tv_devices_subtitle.setText(R.string.lb_searching_devices);
            tv_searching_message.setText(R.string.lb_message_searching);
            pb_searching.setVisibility(View.VISIBLE);
            img_connected.setVisibility(View.GONE);
            rv_item.setVisibility(View.INVISIBLE);
            btn_connect.setEnabled(false);
            btn_connect.setAlpha((float) 0.6);
            btn_cancel.setVisibility(View.GONE);
        } else if (view == ValueCodes.FOUNDED) {
            tv_devices_subtitle.setText("Found " + deviceAdapter.getItemCount() + " Devices");
            tv_searching_message.setText(R.string.lb_select_device);
            pb_searching.setVisibility(View.GONE);
            rv_item.setVisibility(View.VISIBLE);
            pb_searching.setVisibility(View.GONE);
            btn_cancel.setVisibility(View.GONE);
        } else if (view == ValueCodes.NO_FOUNDED) {
            tv_devices_subtitle.setText(R.string.lb_no_devices_found);
            tv_searching_message.setText(R.string.lb_unable_find_device);
            btn_retry.setVisibility(View.VISIBLE);
            pb_searching.setVisibility(View.GONE);
        } else if (view == ValueCodes.CONNECTING) {
            pb_searching.setVisibility(View.VISIBLE);
            deviceAdapter.setSelectedDevice();
            tv_devices_subtitle.setText(R.string.lb_connecting_device);
            tv_searching_message.setText(R.string.lb_connecting_message);
            btn_cancel.setVisibility(View.VISIBLE);
            btn_connect.setEnabled(false);
            btn_connect.setAlpha((float) 0.6);
        } else if (view == ValueCodes.CONNECTED) {
            pb_searching.setVisibility(View.GONE);
            img_connected.setVisibility(View.VISIBLE);
            tv_devices_subtitle.setText(R.string.lb_success);
            tv_searching_message.setText(R.string.lb_device_connected);
            btn_cancel.setVisibility(View.GONE);
        }
    }

    private void setToolbarTitle(String type) {
        switch (type) {
            case ValueCodes.VHF:
                title = getString(R.string.select_vhf_receiver);
                break;
            case ValueCodes.ACOUSTIC:
                title = getString(R.string.select_acoustic_receiver);
                break;
            case ValueCodes.WILDLINK:
                title = getString(R.string.select_wildlink);
                break;
            case ValueCodes.BLUETOOTH_RECEIVER:
                title = getString(R.string.select_bluetooth_beacon);
                break;
            default:
                title = "SELECT DEVICE";
                break;
        }
    }

    private void initializeParameters() {
        mConnected = cancel = false;
        parameter = ValueCodes.NONE;
        connectionTimeout = new Timer();
    }

    private void cancelConnection() {
        cancel = true;
        mConnected = false;
        readBoardStatus = false;
        parameter = ValueCodes.NONE;
        unbindService(leServiceConnection.getServiceConnection());
        if (leServiceConnection.existConnection()) leServiceConnection.close();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    @SuppressLint("MissingPermission")
    protected void scanLeDevice(final boolean enable) {
        super.scanLeDevice(enable);
        if (enable) {
            mBluetoothLeScanner.startScan(mLeScanCallback);
            deviceAdapter.clear();
            setVisibility(ValueCodes.SEARCHING);

            messageHandler.postDelayed(() -> {
                if (deviceAdapter.getItemCount() > 0) { // Available devices were found to display
                    rv_item.setAdapter(deviceAdapter);
                    rv_item.setLayoutManager(new LinearLayoutManager(this));
                    setVisibility(ValueCodes.FOUNDED);
                } else { // Unable to find any devices within range
                    mBluetoothLeScanner.stopScan(mLeScanCallback);
                    setVisibility(ValueCodes.NO_FOUNDED);
                }
                invalidateOptionsMenu();
            }, ValueCodes.SCAN_PERIOD);
        }
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionAlertDialog() {
        Context context = this;
        scanLeDevice(false);
        messageHandler.post(new Runnable() {
            public void run() {
                disconnectionDialog = Dialogs.createDisconnectionDialog(context, getString(R.string.lb_failed_connect));
                disconnectionDialog.show(); // a veces cuando desconecto quiere mostrar este mensaje como si siguiera corriendo el message handler

                byte[] data = Converters.convertToUTF8(leServiceConnection.getBluetoothLeService().downloadLogs);
                Data logData = new Data(ValueCodes.LOG_FILE);
                logData.packets.add(data);
                ArrayList<Data> dataList = new ArrayList<>();
                dataList.add(logData);
                File root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
                Converters.printDataFiles(root, dataList);

                messageHandler.postDelayed(() -> {
                    disconnectionDialog.dismiss();
                    if (leServiceConnection.existConnection())
                        leServiceConnection.close();
                    finish();
                }, ValueCodes.BRANDING_PERIOD); // The message disappears after a pre-defined period and will search for other available BLE devices again
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
        int firmwareVersion = Byte.toUnsignedInt(data[5]);
        sharedPreferencesEdit.putInt(ValueCodes.FIRMWARE_VERSION, firmwareVersion);
        int baseFrequency = Byte.toUnsignedInt(data[2]);
        int range = Byte.toUnsignedInt(data[3]);
        sharedPreferencesEdit.putInt(ValueCodes.BASE_FREQUENCY, baseFrequency);
        sharedPreferencesEdit.putInt(ValueCodes.RANGE, range);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        sharedPreferencesEdit.putInt(ValueCodes.WIDTH, metrics.widthPixels);
        sharedPreferencesEdit.putInt(ValueCodes.HEIGHT, metrics.heightPixels);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.changeSDCard(data[7] == 0x01);
        sharedPreferencesEdit.apply();
        readBoardStatus = true;
    }

    /**
     * With the received packet, get state of scan.
     * @param data The received packet.
     */
    private void downloadScanStatus(byte[] data) {
        if (readBoardStatus && !readScanStatus) {
            readScanStatus = true;
            connectionTimeout.cancel();
            connectionTimeout.purge();
            showVhfReceiverMenu(data);
        }
    }

    private void showVhfReceiverMenu(byte[] data) {
        Log.i(TAG, Converters.getHexValue(data));
        Intent intent = new Intent(this, MenuActivity.class);
        switch (data[1]) {
            case ValueCodes.MOBILE_SCAN_COMMAND:
            case ValueCodes.MOBILE_HOLD_COMMAND:
            case ValueCodes.MOBILE_PAUSE_COMMAND:
                intent = new Intent(this, MobileScanActivity.class);
                intent.putExtra(ValueCodes.IS_SCANNING, true);
                intent.putExtra(ValueCodes.VALUE, data);
                startActivities(new Intent[]{new Intent(this, MenuActivity.class), new Intent(this, StartScanningActivity.class), intent});
                break;
            case ValueCodes.STATIONARY_SCAN_COMMAND:
                intent = new Intent(this, StationaryScanActivity.class);
                intent.putExtra(ValueCodes.IS_SCANNING, true);
                intent.putExtra(ValueCodes.VALUE, data);
                startActivities(new Intent[]{new Intent(this, MenuActivity.class), new Intent(this, StartScanningActivity.class), intent});
                break;
            case ValueCodes.MANUAL_SCAN_COMMAND:
                intent = new Intent(this, ManualScanActivity.class);
                intent.putExtra(ValueCodes.IS_SCANNING, true);
                intent.putExtra(ValueCodes.VALUE, data);
                startActivities(new Intent[]{new Intent(this, MenuActivity.class), new Intent(this, StartScanningActivity.class), intent});
                break;
            default:
                startActivity(intent);
        }
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