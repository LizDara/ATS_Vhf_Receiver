package com.atstrack.ats.ats_vhf_receiver.Utils;

public class ReceiverInformation {

    private static ReceiverInformation receiverInformation = null;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceStatus;
    private String mDeviceBattery;

    private ReceiverInformation() {
        mDeviceName = "Unknown";
        mDeviceAddress = "Unknown";
        mDeviceStatus = "None";
        mDeviceBattery = "%";
    }

    public static ReceiverInformation getReceiverInformation() {
        if (receiverInformation == null) {
            receiverInformation = new ReceiverInformation();
        }
        return receiverInformation;
    }

    public void changeInformation(String deviceName, String deviceAddress, String deviceStatus, String deviceBattery) {
        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;
        mDeviceStatus = deviceStatus;
        mDeviceBattery = deviceBattery;
    }

    public void changeDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public void changeDeviceBattery(String deviceBattery) {
        mDeviceBattery = deviceBattery;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public String getDeviceStatus() {
        return mDeviceStatus;
    }

    public String getPercentBattery() {
        return mDeviceBattery;
    }
}
