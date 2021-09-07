package com.atstrack.ats.ats_vhf_receiver.Utils;

public class ReceiverInformation {

    private static ReceiverInformation receiverInformation = null;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceStatus;
    private String mPercentBattery;

    private ReceiverInformation() {}

    public static ReceiverInformation getReceiverInformation(
            ) {
        if (receiverInformation == null) {
            receiverInformation = new ReceiverInformation();
        }
        return receiverInformation;
    }

    public void setReceiverInformation(String deviceName, String deviceAddress, String deviceStatus, String deviceBattery) {
        this.mDeviceName = deviceName;
        this.mDeviceAddress = deviceAddress;
        this.mDeviceStatus = deviceStatus;
        this.mPercentBattery = deviceBattery;
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
        return mPercentBattery;
    }
}
