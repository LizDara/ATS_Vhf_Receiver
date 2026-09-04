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

import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityScanDevicesBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ScanDevicesActivity extends BluetoothScannerActivity {
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
                leServiceConnection.getBluetoothLeService().downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - onScanDevicesActivity: Broadcast Receiver (action): " + action + ValueCodes.CR + ValueCodes.LF;
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
                leServiceConnection.getBluetoothLeService().downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - onScanDevicesActivity: Broadcast Receiver catch: " + e.getLocalizedMessage() + ValueCodes.CR + ValueCodes.LF;
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
            messageHandler.postDelayed(() -> TransferBleData.readBoardStatus(), ValueCodes.WAITING_PERIOD);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_scan_devices;
        String type = getIntent().getStringExtra(ValueCodes.TYPE);
        setToolbarTitle(type);
        binding = ActivityScanDevicesBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnRetry.setOnClickListener(v -> scanLeDevice(true));
        ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnConnect.setOnClickListener(v -> {
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
                        if (deviceAdapter.deviceType.contains(ValueCodes.ACOUSTIC)) {
                            showAcousticReceiverMenu();
                        } else if (deviceAdapter.deviceType.contains(ValueCodes.BLUETOOTH_RECEIVER)) {
                            showBluetoothReceiverMenu();
                        }
                    } else {
                        if (!readBoardStatus || !readScanStatus)
                            showDisconnectionAlertDialog(); //Connection timeout, make sure you write mac address correct and ble device is discoverable
                    }
                    connectionTimeout.cancel();
                    connectionTimeout.purge();
                }
            }, ValueCodes.CONNECT_TIMEOUT);
        });
        ((ActivityScanDevicesBinding) binding).btnCancel.setOnClickListener(v -> {
            cancelConnection();
            setVisibility(ValueCodes.FOUNDED);
            mBluetoothLeScanner.startScan(mLeScanCallback);
        });

        deviceAdapter = new DeviceAdapter(this, type, ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnConnect, ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvDevicesSubtitle, ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvSearchingMessage); // Initializes list view adapter.
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
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnRetry.setVisibility(View.GONE);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvDevicesSubtitle.setText(R.string.lbl_device_selection_searching);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvSearchingMessage.setText(R.string.lbl_device_selection_searching_msg);
            ((ActivityScanDevicesBinding) binding).pbSearching.setVisibility(View.VISIBLE);
            ((ActivityScanDevicesBinding) binding).imgConnected.setVisibility(View.GONE);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.includeRecyclerView.rvItem.setVisibility(View.INVISIBLE);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnConnect.setEnabled(false);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnConnect.setAlpha((float) 0.6);
            ((ActivityScanDevicesBinding) binding).btnCancel.setVisibility(View.GONE);
        } else if (view == ValueCodes.FOUNDED) {
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvDevicesSubtitle.setText("Found " + deviceAdapter.getItemCount() + " Devices");
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvSearchingMessage.setText(R.string.lbl_device_selection_guide);
            ((ActivityScanDevicesBinding) binding).pbSearching.setVisibility(View.GONE);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.includeRecyclerView.rvItem.setVisibility(View.VISIBLE);
            ((ActivityScanDevicesBinding) binding).pbSearching.setVisibility(View.GONE);
            ((ActivityScanDevicesBinding) binding).btnCancel.setVisibility(View.GONE);
        } else if (view == ValueCodes.NO_FOUNDED) {
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvDevicesSubtitle.setText(R.string.lbl_device_selection_none_found);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvSearchingMessage.setText(R.string.lbl_device_selection_range_warning);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnRetry.setVisibility(View.VISIBLE);
            ((ActivityScanDevicesBinding) binding).pbSearching.setVisibility(View.GONE);
        } else if (view == ValueCodes.CONNECTING) {
            ((ActivityScanDevicesBinding) binding).pbSearching.setVisibility(View.VISIBLE);
            deviceAdapter.setSelectedDevice();
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvDevicesSubtitle.setText(R.string.lbl_receiver_connection_connecting_device);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvSearchingMessage.setText(R.string.lbl_receiver_connection_msg);
            ((ActivityScanDevicesBinding) binding).btnCancel.setVisibility(View.VISIBLE);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnConnect.setEnabled(false);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.btnConnect.setAlpha((float) 0.6);
        } else if (view == ValueCodes.CONNECTED) {
            ((ActivityScanDevicesBinding) binding).pbSearching.setVisibility(View.GONE);
            ((ActivityScanDevicesBinding) binding).imgConnected.setVisibility(View.VISIBLE);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvDevicesSubtitle.setText(R.string.lbl_receiver_connection_success);
            ((ActivityScanDevicesBinding) binding).includeSearchingDevices.tvSearchingMessage.setText(R.string.lbl_receiver_connection_success_msg);
            ((ActivityScanDevicesBinding) binding).btnCancel.setVisibility(View.GONE);
        }
    }

    private void setToolbarTitle(String type) {
        switch (type) {
            case ValueCodes.VHF:
                title = getString(R.string.title_device_selection_vhf);
                break;
            case ValueCodes.ACOUSTIC:
                title = getString(R.string.title_device_selection_acoustic);
                break;
            case ValueCodes.WILDLINK:
                title = getString(R.string.title_device_selection_wildlink);
                break;
            case ValueCodes.BLUETOOTH_RECEIVER:
                title = getString(R.string.title_device_selection_bluetooth_beacon);
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
        if (leServiceConnection.existConnection()) leServiceConnection.close(this);
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
                    ((ActivityScanDevicesBinding) binding).includeSearchingDevices.includeRecyclerView.rvItem.setAdapter(deviceAdapter);
                    ((ActivityScanDevicesBinding) binding).includeSearchingDevices.includeRecyclerView.rvItem.setLayoutManager(new LinearLayoutManager(this));
                    setVisibility(ValueCodes.FOUNDED);
                } else { // Unable to find any devices within range
                    mBluetoothLeScanner.stopScan(mLeScanCallback);
                    setVisibility(ValueCodes.NO_FOUNDED);
                }
                invalidateOptionsMenu();
            }, ValueCodes.BRANDING_PERIOD);
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
                if (!readBoardStatus || !readScanStatus) {
                    disconnectionDialog = Dialogs.createDisconnectionDialog(context, getString(R.string.lbl_receiver_connection_failed), deviceAdapter.deviceType);
                    disconnectionDialog.show();

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
                            leServiceConnection.close(getBaseContext());
                        finish();
                    }, ValueCodes.BRANDING_PERIOD); // The message disappears after a pre-defined period and will search for other available BLE devices again
                }
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
        Intent intent = new Intent(this, com.atstrack.ats.ats_vhf_receiver.BluetoothReceiver.BluetoothTagDetectionActivity.class);
        startActivity(intent);
        finish();
    }

    private void mRegisterReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) // API 33+
            registerReceiver(mGattUpdateReceiver, TransferBleData.makeGattUpdateIntentFilter(), Context.RECEIVER_EXPORTED);
        else
            registerReceiver(mGattUpdateReceiver, TransferBleData.makeGattUpdateIntentFilter());
    }
}