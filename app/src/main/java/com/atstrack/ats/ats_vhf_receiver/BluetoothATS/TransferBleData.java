package com.atstrack.ats.ats_vhf_receiver.BluetoothATS;

import android.content.IntentFilter;

import com.atstrack.ats.ats_vhf_receiver.Utils.AtsUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.UUID;

public class TransferBleData {

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public static boolean readBoardState() {
        return LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristic(
                AtsUuids.UUID_SERVICE_DIAGNOSTIC, AtsUuids.UUID_CHARACTERISTIC_BOARD_STATE);
    }

    /**
     * Enable notification for receive the data.
     */
    public static void notificationLog() {
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotification(
                AtsUuids.UUID_SERVICE_SCREEN, AtsUuids.UUID_CHARACTERISTIC_SEND_LOG, true);
    }

    public static boolean writeDetectionFilter(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, AtsUuids.UUID_CHARACTERISTIC_TX_TYPE, data);
    }

    /**
     * Requests a read for get the number of frequencies from each table and display it.
     */
    public static void readTables() {
        UUID service = AtsUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristic(service, characteristic);
    }

    public static boolean writeStartScan(String type, byte[] data) {
        UUID characteristic = AtsUuids.UUID_CHARACTERISTIC_MANUAL;
        switch (type) {
            case ValueCodes.MOBILE:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_AERIAL;
                break;
            case ValueCodes.STATIONARY:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_STATIONARY;
                break;
        }
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    public static boolean writeStopScan(String type) {
        byte[] data = new byte[] {(byte) 0x87};
        UUID characteristic = AtsUuids.UUID_CHARACTERISTIC_MANUAL;
        switch (type) {
            case ValueCodes.MOBILE:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_AERIAL;
                break;
            case ValueCodes.STATIONARY:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_STATIONARY;
                break;
        }
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    /**
     * Records the specific code information, the code received.
     */
    public static boolean writeRecord(boolean start, boolean isManual) {
        byte[] data = new byte[] {start ? (byte) 0x8C : (byte) 0x8E};
        UUID characteristic = isManual ? AtsUuids.UUID_CHARACTERISTIC_MANUAL : AtsUuids.UUID_CHARACTERISTIC_AERIAL;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(AtsUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    public static boolean writeGps(boolean gpsOn) {
        byte[] data = new byte[] {gpsOn ? (byte) 0x92 : (byte) 0x91};
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, AtsUuids.UUID_CHARACTERISTIC_GPS, data);
    }

    /**
     * Writes a value for add one to the current frequency.
     */
    public static boolean writeDecreaseIncrease(boolean isDecrease) {
        byte[] data = new byte[] {isDecrease ? (byte) 0x5E : (byte) 0x5F};
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, AtsUuids.UUID_CHARACTERISTIC_SCAN_TABLE, data);
    }

    public static boolean writeScanning(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, AtsUuids.UUID_CHARACTERISTIC_SCAN_TABLE, data);
    }

    public static boolean setHold(boolean isHold) {
        byte[] b = new byte[] {isHold ? (byte) 0x80 : (byte) 0x81};
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, AtsUuids.UUID_CHARACTERISTIC_AERIAL, b);
    }

    /**
     * Writes a value to go to the previous or next index.
     */
    public static void writeLeftRight(boolean isLeft) {
        byte[] b = new byte[] {isLeft ? (byte) 0x57 : (byte) 0x58};
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, AtsUuids.UUID_CHARACTERISTIC_SCAN_TABLE, b);
    }

    /**
     * Requests a read for get defaults data.
     */
    public static void readDefaults(boolean isMobile) {
        UUID characteristic = isMobile ? AtsUuids.UUID_CHARACTERISTIC_AERIAL : AtsUuids.UUID_CHARACTERISTIC_STATIONARY;
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, characteristic);
    }

    /**
     * Read the table number to get its frequencies.
     */
    public static void readFrequencies(int number) {
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristic(
                AtsUuids.UUID_SERVICE_STORED_DATA, getTableCharacteristic(number));
    }

    /**
     * Writes the modified frequencies by the user.
     */
    public static boolean writeFrequencies(int number, byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_STORED_DATA, getTableCharacteristic(number), data);
    }

    private static UUID getTableCharacteristic(int number) {
        UUID characteristic = AtsUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        switch (number)     {
            case 1:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_1;
                break;
            case 2:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_2;
                break;
            case 3:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_3;
                break;
            case 4:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_4;
                break;
            case 5:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_5;
                break;
            case 6:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_6;
                break;
            case 7:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_7;
                break;
            case 8:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_8;
                break;
            case 9:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_9;
                break;
            case 10:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_10;
                break;
            case 11:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_11;
                break;
            case 12:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_TABLE_12;
                break;
        }
        return characteristic;
    }

    public static boolean writeDefaults(boolean isMobile, byte[] data) {
        UUID characteristic = isMobile ? AtsUuids.UUID_CHARACTERISTIC_AERIAL : AtsUuids.UUID_CHARACTERISTIC_STATIONARY;
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    /**
     * Requests a read for detection filter data.
     */
    public static void readDetectionFilter() {
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, AtsUuids.UUID_CHARACTERISTIC_TX_TYPE);
    }

    /**
     * Requests a read for get BLE device data.
     */
    public static void readDiagnostic() {
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristic(
                AtsUuids.UUID_SERVICE_DIAGNOSTIC, AtsUuids.UUID_CHARACTERISTIC_DIAG_INFO);
    }

    public static void readDataInfo() {
        boolean result = LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristic(
                AtsUuids.UUID_SERVICE_DIAGNOSTIC, AtsUuids.UUID_CHARACTERISTIC_DATA_INFO);
    }

    /**
     * Requests a download data for the user.
     */
    public static void downloadResponse(boolean enabled) {
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotification(
                AtsUuids.UUID_SERVICE_STORED_DATA, AtsUuids.UUID_CHARACTERISTIC_STUDY_DATA, enabled);
    }

    /**
     * Requests a read for get BLE device data before download data.
     */
    public static void readPageNumber() {
        LeServiceConnection.getInstance().getBluetoothLeService().readCharacteristic(
                AtsUuids.UUID_SERVICE_STORED_DATA, AtsUuids.UUID_CHARACTERISTIC_STUDY_DATA);
    }

    public static boolean writeResponse(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_STORED_DATA, AtsUuids.UUID_CHARACTERISTIC_STUDY_DATA, data);
    }

    /**
     * Enable notification for receive bluetooth tags.
     */
    public static void receiveTags(boolean enabled) {
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotification(
                AtsUuids.UUID_SERVICE_TAG, AtsUuids.UUID_CHARACTERISTIC_TAG, enabled);
    }

    public static boolean writeOTA(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeOTA(data);
    }

    public static boolean requestMtu(int mtu, boolean isOta) {
        return LeServiceConnection.getInstance().getBluetoothLeService().requestMtu(mtu, isOta);
    }
}