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
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.atstrack.ats.ats_vhf_receiver.Utils.AtsUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Calendar;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean otaUpdate = false;

    public String downloadLogs = "";

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    // Implements callback methods for GATT events that the app cares about.  For example, connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - onConnectionStateChange: new state = " + newState + ", status = " + status + ValueCodes.CR + ValueCodes.LF;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.d(TAG,"Attempting to start service discovery: " + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "DEVICE DISCONNECTED, STATUS = " + status);
                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction);
                if (status == 8) // TIMEOUT
                    close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - onServicesDiscovered: status = " + status + ValueCodes.CR + ValueCodes.LF;
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            else
                Log.w(TAG,"onServicesDiscovered received: " +  status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - onCharacteristicWrite: status = " + status + ", char = " + characteristic.getUuid().toString() + ValueCodes.CR + ValueCodes.LF;
            if (status != BluetoothGatt.GATT_SUCCESS)
                return;
            if (characteristic.getUuid().equals(AtsUuids.UUID_CHARACTERISTIC_SILICON_LABS_OTA_CONTROL) && characteristic.getValue().length == 1 && characteristic.getValue()[0] == 0x00) {
                broadcastUpdate(ACTION_GATT_CONNECTED); //OTA Begin written
                Log.i(TAG, "OTA BEGIN 0x00");
            } else if (characteristic.getUuid().equals(AtsUuids.UUID_CHARACTERISTIC_SILICON_LABS_OTA_CONTROL) && characteristic.getValue().length == 1 && characteristic.getValue()[0] == 0x03) {
                broadcastUpdate(ACTION_GATT_CONNECTED); //OTA End written
                Log.i(TAG, "OTA END 0x03");
            } else if (characteristic.getUuid().equals(AtsUuids.UUID_CHARACTERISTIC_SILICON_LABS_OTA_CONTROL) && characteristic.getValue().length == 1 && characteristic.getValue()[0] == 0x04) {
                broadcastUpdate(ACTION_GATT_CONNECTED); //OTA End written
                Log.i(TAG, "OTA END 0x04");
            } else if (characteristic.getUuid().equals(AtsUuids.UUID_CHARACTERISTIC_SILICON_LABS_OTA_DATA)) {
                broadcastUpdate(ACTION_GATT_CONNECTED);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - onCharacteristicRead: status = " + status + ", char = " + characteristic.getUuid().toString() + ValueCodes.CR + ValueCodes.LF;
            downloadLogs += Converters.getHexValue(characteristic.getValue()) + ValueCodes.CR + ValueCodes.LF;
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - onCharacteristicChanged: char = " + characteristic.getUuid().toString() + ValueCodes.CR + ValueCodes.LF;
            downloadLogs += Converters.getHexValue(characteristic.getValue()) + ValueCodes.CR + ValueCodes.LF;
            broadcastUpdate(characteristic);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - onMtuChanged: mtu = " + mtu + ", status = " + status + ValueCodes.CR + ValueCodes.LF;
            super.onMtuChanged(gatt, mtu, status);
            if (otaUpdate && status == BluetoothGatt.GATT_SUCCESS) {
                otaUpdate = false;
                broadcastUpdate(ACTION_GATT_CONNECTED);
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(BluetoothLeService.ACTION_DATA_AVAILABLE);
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
        if (mBluetoothGatt != null) {
            Log.d(TAG, "Checking existing mBluetoothGatt to keep connection alive across activities.");
            BluetoothDevice connectedDevice = mBluetoothGatt.getDevice();
            if (connectedDevice != null && connectedDevice.getAddress().equals(address)) {
                Log.d(TAG, "Reusing functional connection for the same device address.");
                broadcastUpdate(ACTION_GATT_CONNECTED);
                return true;
            } else {
                Log.w(TAG, "Existing GATT is corrupted or belongs to another device. Cleaning up.");
                close();
                return false;
            }
        }
        try {
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found. Unable to connect.");
                return false;
            }
            // We want to directly connect to the device, so we are setting the autoConnect parameter to false.
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
            downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - connect: Trying to create a new connection" + ValueCodes.CR + ValueCodes.LF;
            Log.d(TAG, "Trying to create a new connection.");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
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
        downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - disconnect: The app disconnected an existing connection or cancel a pending connection" + ValueCodes.CR + ValueCodes.LF;
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
        Log.i(TAG, "CLOSE BLUETOOTH GATT");
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * To write to the value of a characteristic value or a descriptor.
     * @param service UUID to act on.
     * @param characteristics UUID to act on.
     * @param data value to write.
     */
    public boolean writeCharacteristic(UUID service, UUID characteristics, byte[] data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w( TAG,"BluetoothAdapter not initialized");
            return false;
        }
        if (data != null && data.length > 0) {
            BluetoothGattService myGatService = mBluetoothGatt.getService(service);
            BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(characteristics);
            myGatChar.setValue(data);
            @SuppressLint("MissingPermission") boolean result = mBluetoothGatt.writeCharacteristic(myGatChar);
            downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - writeCharacteristic: The app writes a value { " + Converters.getHexValue(data) + "} to char " + characteristics.toString() + ", result = " + result + ValueCodes.CR + ValueCodes.LF;
            return result;
        }
        return false;
    }

    /**
     * Enables or disables notification on a give characteristic.
     * @param service UUID to act on.
     * @param characteristics  UUID to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    @SuppressLint("MissingPermission")
    public void setCharacteristicNotification(UUID service, UUID characteristics, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService myGatService = mBluetoothGatt.getService(service);
        BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(characteristics);
        BluetoothGattDescriptor desc = myGatChar.getDescriptor(AtsUuids.CLIENT_CHARACTERISTIC_CONFIG);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(desc);
        boolean result = mBluetoothGatt.setCharacteristicNotification(myGatChar, enabled);
        downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - setCharacteristicNotification: The app enables or disables notification on a give char " + characteristics.toString() + ", enabled = " + enabled + ", result = " + result + ValueCodes.CR + ValueCodes.LF;
    }

    public boolean writeOTA(byte[] data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        BluetoothGattService myGatService = mBluetoothGatt.getService(AtsUuids.UUID_SERVICE_SILICON_LABS_OTA);
        BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(data.length == 1 ? AtsUuids.UUID_CHARACTERISTIC_SILICON_LABS_OTA_CONTROL : AtsUuids.UUID_CHARACTERISTIC_SILICON_LABS_OTA_DATA);
        myGatChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        myGatChar.setValue(data);
        @SuppressLint("MissingPermission") boolean result = mBluetoothGatt.writeCharacteristic(myGatChar);
        downloadLogs += String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + ":" + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)) + "." + String.format("%03d", Calendar.getInstance().get(Calendar.MILLISECOND)) +  " - writeCharacteristic: The app writes a value { " + Converters.getHexValue(data) + "} to char " + myGatChar.getUuid().toString() + ", result = " + result + ValueCodes.CR + ValueCodes.LF;
        return result;
    }

    @SuppressLint("MissingPermission")
    public boolean requestConnectionPriority() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        return mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
    }

    public boolean requestMtu(int mtu, boolean isOta) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        otaUpdate = isOta;
        @SuppressLint("MissingPermission") boolean result = mBluetoothGatt.requestMtu(mtu);
        return result;
    }
}