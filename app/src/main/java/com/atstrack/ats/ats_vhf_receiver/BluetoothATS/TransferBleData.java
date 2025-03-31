package com.atstrack.ats.ats_vhf_receiver.BluetoothATS;

import android.content.IntentFilter;
import android.util.Log;

import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.UUID;

public class TransferBleData {

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    public static IntentFilter makeFirstGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public static IntentFilter makeSecondGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND);
        return intentFilter;
    }

    public static IntentFilter makeThirdGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_SECOND);
        return intentFilter;
    }

    public static boolean readBoardState() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_BOARD_STATE;
        return LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Enables notification for receive the data.
     */
    public static void notificationLog() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotificationRead(service, characteristic, true);
    }

    public static boolean writeDetectionFilter(byte[] data) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, data);
    }

    /**
     * Requests a read for get the number of frequencies from each table and display it.
     */
    public static void readTables(boolean isScanning) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        if (isScanning) LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnosticSecond(service, characteristic);
        else LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(service, characteristic);
    }

    public static boolean writeStartScan(String type, byte[] data) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        switch (type) {
            case ValueCodes.MOBILE_DEFAULTS:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
                break;
            case ValueCodes.STATIONARY_DEFAULTS:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
                break;
        }
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, data);
    }

    public static boolean writeStopScan(String type) {
        byte[] data = new byte[] {(byte) 0x87};
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        switch (type) {
            case ValueCodes.MOBILE_DEFAULTS:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
                break;
            case ValueCodes.STATIONARY_DEFAULTS:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
                break;
        }
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, data);
    }

    /**
     * Records the specific code information, the code received.
     */
    public static boolean writeRecord(boolean start, boolean isManual) {
        byte[] data = new byte[] {start ? (byte) 0x8C : (byte) 0x8E};
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = isManual ? AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL : AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, data);
    }

    /**
     * Writes a value for add one to the current frequency.
     */
    public static boolean writeDecreaseIncrease(boolean isDecrease) {
        byte[] data = new byte[] {isDecrease ? (byte) 0x5E : (byte) 0x5F};
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, data);
    }

    public static boolean writeScanning(byte[] data) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, data);
    }

    public static boolean setHold(boolean isHold) {
        byte[] b = new byte[] {isHold ? (byte) 0x80 : (byte) 0x81};
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, b);
    }

    /**
     * Writes a value to go to the previous or next index.
     */
    public static void writeLeftRight(boolean isLeft) {
        byte[] b = new byte[] {isLeft ? (byte) 0x57 : (byte) 0x58};
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, b);
    }

    /**
     * Requests a read for get defaults data.
     */
    public static void readDefaults(boolean isMobile) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = isMobile ? AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL : AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Read the table number to get its frequencies.
     */
    public static void readFrequencies(int number) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(service, getTableCharacteristic(number));
    }

    /**
     * Writes the modified frequencies by the user.
     */
    public static boolean writeFrequencies(int number, byte[] data) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, getTableCharacteristic(number), data);
    }

    private static UUID getTableCharacteristic(int number) {
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        switch (number)     {
            case 1:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_1;
                break;
            case 2:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_2;
                break;
            case 3:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_3;
                break;
            case 4:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_4;
                break;
            case 5:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_5;
                break;
            case 6:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_6;
                break;
            case 7:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_7;
                break;
            case 8:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_8;
                break;
            case 9:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_9;
                break;
            case 10:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_10;
                break;
            case 11:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_11;
                break;
            case 12:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_12;
                break;
        }
        return characteristic;
    }

    public static boolean writeDefaults(boolean isMobile, byte[] data) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = isMobile ? AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL : AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, data);
    }

    /**
     * Requests a read for detection filter data.
     */
    public static void readDetectionFilter() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Requests a read for get BLE device data.
     */
    public static void readDiagnostic() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_DIAG_INFO;
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Requests a download data for the user.
     */
    public static void downloadResponse() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotificationRead(service, characteristic, true);
    }

    /**
     * Requests a read for get BLE device data before download data.
     */
    public static void readPageNumber() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(service, characteristic);
    }

    public static boolean writeResponse(byte[] data) {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, data);
    }
}