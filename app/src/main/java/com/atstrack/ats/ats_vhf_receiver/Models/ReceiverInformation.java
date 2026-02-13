package com.atstrack.ats.ats_vhf_receiver.Models;

public class ReceiverInformation {

    private static ReceiverInformation receiverInformation = null;
    private String deviceAddress;
    private String deviceName;
    private int deviceBattery;
    private String serialNumber;
    private boolean mSDCardInserted;

    private ReceiverInformation() {
        serialNumber = "Unknown";
        deviceAddress = "Unknown";
        deviceBattery = 0;
        mSDCardInserted = false;
    }

    public static ReceiverInformation getReceiverInformation() {
        if (receiverInformation == null)
            receiverInformation = new ReceiverInformation();
        return receiverInformation;
    }

    public void setInformation(String deviceName, String deviceAddress, String deviceBattery) {
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.deviceBattery = Integer.parseInt(deviceBattery.replace("%", ""));
        this.serialNumber = deviceName.substring(0, 7);
    }

    public void changeSDCard(boolean inserted) {
        mSDCardInserted = inserted;
    }

    public void changeDeviceBattery(int deviceBattery) {
        this.deviceBattery = deviceBattery;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getPercentBattery() {
        return deviceBattery;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public boolean isSDCardInserted() {
        return mSDCardInserted;
    }

    public void initialize() {
        receiverInformation = new ReceiverInformation();
    }
}
