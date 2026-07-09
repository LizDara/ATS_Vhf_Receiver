package com.atstrack.ats.ats_vhf_receiver.Models;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class DeviceData {
    public BluetoothDevice bluetoothDevice;
    public boolean selected;
    public String serialNumber;
    public String detectionFilter;
    public String status;
    public int batteryPercent;
    public String range;

    public DeviceData(BluetoothDevice device, byte[] scanRecord) {
        bluetoothDevice = device;
        selected = false;
        setInformation(scanRecord);
    }

    @SuppressLint("MissingPermission")
    private void setInformation(byte[] scanRecord) {
        String deviceName = bluetoothDevice.getName();
        serialNumber = deviceName.substring(0, 7);
        if (deviceName.contains(ValueCodes.VHF)) {
            detectionFilter = Converters.getDetectionFilter(deviceName.substring(15, 16));
            status = Converters.getStatusVhfReceiver(deviceName);
            batteryPercent = Converters.getPercentBatteryVhfReceiver(scanRecord);
            int baseFrequency = Integer.parseInt(deviceName.substring(12, 15)) * 1000;
            int frequencyRange = ((Integer.parseInt(deviceName.substring(17)) + (baseFrequency / 1000)) * 1000) - 1;
            range = Converters.getFrequency(baseFrequency) + "-" + Converters.getFrequency(frequencyRange);
        }
    }
}
