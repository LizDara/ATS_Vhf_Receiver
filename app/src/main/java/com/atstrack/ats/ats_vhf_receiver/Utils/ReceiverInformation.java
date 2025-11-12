package com.atstrack.ats.ats_vhf_receiver.Utils;

public class ReceiverInformation {

    private static ReceiverInformation receiverInformation = null;
    private String deviceAddress;
    private int deviceBattery;
    private String serialNumber;
    private boolean mSDCardInserted;
    private byte[] statusData;

    private ReceiverInformation() {
        serialNumber = "Unknown";
        deviceAddress = "Unknown";
        deviceBattery = 0;
        statusData = null;
        mSDCardInserted = false;
    }

    public static ReceiverInformation getReceiverInformation() {
        if (receiverInformation == null)
            receiverInformation = new ReceiverInformation();
        return receiverInformation;
    }

    public void changeInformation(String serialNumber, String deviceAddress, String deviceBattery) {
        this.deviceAddress = deviceAddress;
        this.deviceBattery = Integer.parseInt(deviceBattery.replace("%", ""));
        this.serialNumber = serialNumber;
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

    public int getPercentBattery() {
        return deviceBattery;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public boolean isSDCardInserted() {
        return mSDCardInserted;
    }

    public byte[] getStatusData() {
        return statusData;
    }

    public void setStatusData(byte[] statusData) {
        this.statusData = statusData;
    }

    public void initialize() {
        receiverInformation = new ReceiverInformation();
    }
}
