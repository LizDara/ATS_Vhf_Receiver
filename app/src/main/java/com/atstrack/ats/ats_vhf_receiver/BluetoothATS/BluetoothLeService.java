package com.atstrack.ats.ats_vhf_receiver.BluetoothATS;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String action;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_GATT_SERVICES_DISCOVERED_SECOND = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED_1";
    public final static String ACTION_DATA_AVAILABLE_SECOND = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_1";

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Implements callback methods for GATT events that the app cares about.  For example, connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG,"Attempting to start service discovery: " + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG,"onServicesDiscovered received: " +  status);
            }

        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "SUCCESS WRITE: " + (status == BluetoothGatt.GATT_SUCCESS) + " " + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"
                    + Calendar.getInstance().get(Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND) + "." + Calendar.getInstance().get(Calendar.MILLISECOND));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(action, characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.i(TAG, "Rssi: " + rssi + " Status: " + status);
            super.onReadRemoteRssi(gatt, rssi, status);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called such that resources are cleaned up properly.
        // In this particular example, close() is invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    @SuppressLint("MissingPermission")
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG,"BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device. Try to reconnect.
        if (mBluetoothGatt != null) {
            Log.d(TAG,"Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.i(TAG,"Attempting to start service discovery: " + mBluetoothGatt.discoverServices());
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG,"BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothAdapter = null;
        mBluetoothGatt = null;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    @SuppressLint("MissingPermission")
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void discovering() {
        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
    }

    public void discoveringSecond() {
        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED_SECOND);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     * @param service UUID to act on.
     * @param Characteristics  UUID to act on.
     */
    public void readCharacteristicDiagnostic(UUID service, UUID Characteristics) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w( TAG,"BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService myGatService = mBluetoothGatt.getService(service);
        BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(Characteristics);
        @SuppressLint("MissingPermission") boolean result = mBluetoothGatt.readCharacteristic(myGatChar);
        if (result)
            action = ACTION_DATA_AVAILABLE;
    }

    public void readCharacteristicDiagnosticSecond(UUID service, UUID Characteristics) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w( TAG,"BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService myGatService = mBluetoothGatt.getService(service);
        BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(Characteristics);
        @SuppressLint("MissingPermission") boolean result = mBluetoothGatt.readCharacteristic(myGatChar);
        if (result)
            action = ACTION_DATA_AVAILABLE_SECOND;
    }

    /**
     * To write to the value of a characteristic value or a descriptor.
     * @param service UUID to act on.
     * @param Characteristics UUID to act on.
     * @param data value to write.
     */
    public boolean writeCharacteristic(UUID service, UUID Characteristics, byte[] data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w( TAG,"BluetoothAdapter not initialized");
            return false;
        }
        if (data != null && data.length > 0) {
            BluetoothGattService myGatService = mBluetoothGatt.getService(service);
            BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(Characteristics);
            myGatChar.setValue(data);
            @SuppressLint("MissingPermission") boolean result = mBluetoothGatt.writeCharacteristic(myGatChar);
            return result;
        }
        return false;
    }

    /**
     * Enables or disables notification on a give characteristic.
     * @param service UUID to act on.
     * @param Characteristics  UUID to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    @SuppressLint("MissingPermission")
    public void setCharacteristicNotificationRead(UUID service, UUID Characteristics, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService myGatService = mBluetoothGatt.getService(service);
        BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(Characteristics);
        BluetoothGattDescriptor desc = myGatChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(desc);
        mBluetoothGatt.setCharacteristicNotification(myGatChar, enabled);
    }

    @SuppressLint("MissingPermission")
    public void readRssi() {
        mBluetoothGatt.readRemoteRssi();
    }
}