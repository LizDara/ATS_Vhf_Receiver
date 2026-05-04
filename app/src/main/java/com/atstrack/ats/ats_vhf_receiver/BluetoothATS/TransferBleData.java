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

    public static boolean readBoardStatus() {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_DIAGNOSTIC, AtsUuids.UUID_CHARACTERISTIC_BOARD_STATE, new byte[] { 0 });
    }

    /**
     * Enable notification for receive the data.
     */
    public static void notificationLog(boolean enabled) {
        LeServiceConnection.getInstance().getBluetoothLeService().setCharacteristicNotification(
                AtsUuids.UUID_SERVICE_SCREEN, AtsUuids.UUID_CHARACTERISTIC_SEND_LOG, enabled);
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
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(service, characteristic, new byte[] { 0 });
    }

    public static boolean writeStartScan(byte type, byte[] data) {
        UUID characteristic = AtsUuids.UUID_CHARACTERISTIC_MANUAL;
        switch (type) {
            case ValueCodes.MOBILE_SCAN_COMMAND:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_AERIAL;
                break;
            case ValueCodes.STATIONARY_SCAN_COMMAND:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_STATIONARY;
                break;
        }
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, characteristic, data);
    }

    public static boolean writeStopScan(byte type) {
        byte[] data = new byte[] {ValueCodes.SCAN_STOP_COMMAND};
        UUID characteristic = AtsUuids.UUID_CHARACTERISTIC_MANUAL;
        switch (type) {
            case ValueCodes.MOBILE_SCAN_COMMAND:
                characteristic = AtsUuids.UUID_CHARACTERISTIC_AERIAL;
                break;
            case ValueCodes.STATIONARY_SCAN_COMMAND:
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
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, characteristic, new byte[] { isMobile ? (byte) 0x6D : (byte) 0x6C });
    }

    /**
     * Read the table number to get its frequencies.
     */
    public static void readFrequencies(int number) {
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_STORED_DATA, AtsUuids.UUID_CHARACTERISTIC_FREQ_TABLE, new byte[] { (byte) (0xD0 | number) });
    }

    /**
     * Writes the modified frequencies by the user.
     */
    public static boolean writeFrequencies(byte[] data) {
        return LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_STORED_DATA, AtsUuids.UUID_CHARACTERISTIC_FREQ_TABLE, data);
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
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_SCAN, AtsUuids.UUID_CHARACTERISTIC_TX_TYPE, new byte[] { 0x67 });
    }

    /**
     * Requests a read for get BLE device data.
     */
    public static void readDiagnostic() {
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_DIAGNOSTIC, AtsUuids.UUID_CHARACTERISTIC_DIAG_INFO, new byte[]{ 0 });
    }

    public static void readDataInfo() {
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_DIAGNOSTIC, AtsUuids.UUID_CHARACTERISTIC_DATA_INFO, new byte[] { 0 });
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
        LeServiceConnection.getInstance().getBluetoothLeService().writeCharacteristic(
                AtsUuids.UUID_SERVICE_STORED_DATA, AtsUuids.UUID_CHARACTERISTIC_STUDY_DATA, new byte[] { (byte) 0x97 });
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

    public static boolean requestConnectionPriority() {
        return LeServiceConnection.getInstance().getBluetoothLeService().requestConnectionPriority();
    }
}