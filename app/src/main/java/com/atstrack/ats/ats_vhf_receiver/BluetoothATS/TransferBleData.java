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
        Log.i("TRANSFER-BLE", "Read Board State");
        return LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(
                AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_BOARD_STATE);
    }

    /**
     * Enables notification for receive the data.
     */
    public static void notificationLog() {
        Log.i("TRANSFER-BLE", "Notification Log");
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotificationRead(
                AtsVhfReceiverUuids.UUID_SERVICE_SCREEN, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG, true);
    }

    public static void disableNotificationLog() {
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotificationRead(
                AtsVhfReceiverUuids.UUID_SERVICE_SCREEN, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG, false);
    }

    public static boolean writeDetectionFilter(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE, data);
    }

    /**
     * Requests a read for get the number of frequencies from each table and display it.
     */
    public static void readTables(boolean isScanning) {
        Log.i("TRANSFER-BLE", "Read Tables");
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        if (isScanning) LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnosticSecond(service, characteristic);
        else LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(service, characteristic);
    }

    public static boolean writeStartScan(String type, byte[] data) {
        Log.i("TRANSFER-BLE", "Write Start Scan " + type);
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        switch (type) {
            case ValueCodes.MOBILE_DEFAULTS:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
                break;
            case ValueCodes.STATIONARY_DEFAULTS:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
                break;
        }
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    public static boolean writeStopScan(String type) {
        Log.i("TRANSFER-BLE", "Write Stop Scan " + type);
        byte[] data = new byte[] {(byte) 0x87};
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        switch (type) {
            case ValueCodes.MOBILE_DEFAULTS:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
                break;
            case ValueCodes.STATIONARY_DEFAULTS:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
                break;
        }
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    /**
     * Records the specific code information, the code received.
     */
    public static boolean writeRecord(boolean start, boolean isManual) {
        byte[] data = new byte[] {start ? (byte) 0x8C : (byte) 0x8E};
        UUID characteristic = isManual ? AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL : AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(AtsVhfReceiverUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    /**
     * Writes a value for add one to the current frequency.
     */
    public static boolean writeDecreaseIncrease(boolean isDecrease) {
        byte[] data = new byte[] {isDecrease ? (byte) 0x5E : (byte) 0x5F};
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE, data);
    }

    public static boolean writeScanning(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE, data);
    }

    public static boolean setHold(boolean isHold) {
        byte[] b = new byte[] {isHold ? (byte) 0x80 : (byte) 0x81};
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL, b);
    }

    /**
     * Writes a value to go to the previous or next index.
     */
    public static void writeLeftRight(boolean isLeft) {
        byte[] b = new byte[] {isLeft ? (byte) 0x57 : (byte) 0x58};
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE, b);
    }

    /**
     * Requests a read for get defaults data.
     */
    public static void readDefaults(boolean isMobile) {
        Log.i("TRANSFER-BLE", "Read Defaults " + isMobile);
        UUID characteristic = isMobile ? AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL : AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, characteristic);
    }

    /**
     * Read the table number to get its frequencies.
     */
    public static void readFrequencies(int number) {
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(
                AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA, getTableCharacteristic(number));
    }

    /**
     * Writes the modified frequencies by the user.
     */
    public static boolean writeFrequencies(int number, byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA, getTableCharacteristic(number), data);
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
        UUID characteristic = isMobile ? AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL : AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    /**
     * Requests a read for detection filter data.
     */
    public static void readDetectionFilter() {
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(
                AtsVhfReceiverUuids.UUID_SERVICE_SCAN, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE);
    }

    /**
     * Requests a read for get BLE device data.
     */
    public static void readDiagnostic() {
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(
                AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_DIAG_INFO);
    }

    public static void readDataInfo() {
        boolean result = LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(
                AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_DATA_INFO);
        Log.i("TRANFER-BLE", "RESULT READ: " + result);
    }

    /**
     * Requests a download data for the user.
     */
    public static void downloadResponse() {
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotificationRead(
                AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA, true);
    }

    /**
     * Requests a read for get BLE device data before download data.
     */
    public static void readPageNumber() {
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristicDiagnostic(
                AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA);
    }

    public static boolean writeResponse(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA, AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA, data);
    }

    public static boolean writeOTA(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeOTA(data);
    }

    public static boolean requestMtu(int mtu) {
        return LeServiceConnection.getInstance().getBluetoothLeService().requestMtu(mtu);
    }
}