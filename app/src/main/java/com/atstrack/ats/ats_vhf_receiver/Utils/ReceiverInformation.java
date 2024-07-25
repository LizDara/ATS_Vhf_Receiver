package com.atstrack.ats.ats_vhf_receiver.Utils;

public class ReceiverInformation {

    private static ReceiverInformation receiverInformation = null;
    private String mDeviceStatus;
    private String mDeviceAddress;
    private String mDeviceRange;
    private String mDeviceBattery;
    private String deviceName;
    private byte txType;
    private byte scanState;

    private ReceiverInformation() {
        deviceName = "Unknown";
        mDeviceStatus = deviceName;
        mDeviceAddress = "Unknown";
        mDeviceRange = "None";
        mDeviceBattery = "%";
    }

    public static ReceiverInformation getReceiverInformation() {
        if (receiverInformation == null) {
            receiverInformation = new ReceiverInformation();
        }
        return receiverInformation;
    }

    public void changeInformation(byte txType, byte scanState, String deviceName, String deviceAddress, String deviceRange, String deviceBattery) {
        mDeviceAddress = deviceAddress;
        mDeviceRange = deviceRange;
        mDeviceBattery = deviceBattery;
        this.deviceName = deviceName;
        this.txType = txType;
        this.scanState = scanState;
        mDeviceStatus = deviceName;
        setTxType();
        setScanState();
    }

    public void changeTxType(byte type) {
        mDeviceStatus = deviceName;
        txType = type;
        setTxType();
        setScanState();
    }

    public void changeScanState(byte state) {
        mDeviceStatus = deviceName;
        scanState = state;
        setTxType();
        setScanState();
    }

    public void changeDeviceBattery(String deviceBattery) {
        mDeviceBattery = deviceBattery;
    }

    public String getDeviceStatus() {
        return mDeviceStatus;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public String getDeviceRange() {
        return mDeviceRange;
    }

    public String getPercentBattery() {
        return mDeviceBattery;
    }

    private void setTxType() {
        switch (Converters.getHexValue(txType)) {
            case "09":
                mDeviceStatus += " Coded,";
                break;
            case "08":
                mDeviceStatus += " Fixed PR,";
                break;
            case "07":
                mDeviceStatus += " Variable PR,";
                break;
        }
    }

    private void setScanState() {
        switch (Converters.getHexValue(scanState)) {
            case "00":
                mDeviceStatus += " Not scanning";
                break;
            case "82":
            case "81":
            case "80":
                mDeviceStatus += " Scanning, mobile";
                break;
            case "83":
                mDeviceStatus += " Scanning, stationary";
                break;
            case "86":
                mDeviceStatus += " Scanning, manual";
                break;
            default:
                mDeviceStatus += " None";
                break;
        }
    }
}
