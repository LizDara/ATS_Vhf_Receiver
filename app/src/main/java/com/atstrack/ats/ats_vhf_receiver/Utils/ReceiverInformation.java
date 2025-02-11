package com.atstrack.ats.ats_vhf_receiver.Utils;

public class ReceiverInformation {

    private static ReceiverInformation receiverInformation = null;
    private String deviceAddress;
    private int deviceBattery;
    private String serialNumber;
    private String mSDCard;
    private String deviceStatus;
    private byte[] statusData;

    private ReceiverInformation() {
        serialNumber = "Unknown";
        deviceStatus = serialNumber;
        deviceAddress = "Unknown";
        deviceBattery = 0;
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
        deviceStatus = serialNumber;
    }

    public void changeSDCard(byte state) {
        mSDCard = Converters.getHexValue(state).equals("01") ? "Inserted" : "None";
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

    public String getSDCard() {
        return mSDCard;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String mDeviceStatus) {
        this.deviceStatus = mDeviceStatus;
    }

    public byte[] getStatusData() {
        return statusData;
    }

    public void setStatusData(byte[] statusData) {
        this.statusData = statusData;
    }
}
