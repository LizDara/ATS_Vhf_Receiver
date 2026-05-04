package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

public class TagDetail {
    public String code;
    public String rssi;
    public String temperature;
    public String voltage;
    public int detections;
    public double latitude;
    public double longitude;
    public int frequencyTone;
    public long timeSince;
    public long lastTimestamp = 0;

    public TagDetail(byte[] data, double latitude, double longitude, long lastTimestamp) { // Bluetooth Receiver (exported data)
        setBluetoothValues(data);
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastTimestamp = lastTimestamp;
        detections = 1;
    }

    public TagDetail(byte[] data, String rssi, double latitude, double longitude, long lastTimestamp) { // Tags (exported data)
        setTagValues(data);
        this.rssi = rssi;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastTimestamp = lastTimestamp;
        detections = 1;
    }

    public TagDetail(byte[] data, int frequency, long timeSince, long lastTimestamp) { // Bluetooth Receiver
        setBluetoothValues(data);
        detections = 1;
        frequencyTone = frequency;
        this.timeSince = timeSince;
        this.lastTimestamp = lastTimestamp;
    }

    public TagDetail(byte[] data, String rssi, int frequency, long timeSince, long lastTimestamp) { // Tags
        setTagValues(data);
        this.rssi = rssi;
        detections = 1;
        frequencyTone = frequency;
        this.timeSince = timeSince;
        this.lastTimestamp = lastTimestamp;
    }

    public void updateData(byte[] data, long timeSince, long lastTimestamp) { // Bluetooth Receiver
        setBluetoothValues(data);
        detections++;
        this.timeSince = timeSince;
        this.lastTimestamp = lastTimestamp;
    }

    public void updateData(byte[] data, String rssi, long timeSince, long lastTimestamp) { // Tags
        setTagValues(data);
        this.rssi = rssi;
        detections++;
        this.timeSince = timeSince;
        this.lastTimestamp = lastTimestamp;
    }

    private void setBluetoothValues(byte[] data) { // Bluetooth Receiver
        code = Converters.getAsciiValue(6, 14, data);
        rssi = Converters.getAsciiValue(24, 28, data);
        temperature = Converters.getAsciiValue(40, 44, data);
        voltage = Converters.getAsciiValue(54, 58, data);
    }

    private void setTagValues(byte[] data) { // Tags
        code = Converters.getHexValue(data[4]) + Converters.getHexValue(data[5]) + Converters.getHexValue(data[6]) + Converters.getHexValue(data[7]);
        temperature = String.format("%.1f", (double) ((Byte.toUnsignedInt(data[15]) << 8) | Byte.toUnsignedInt(data[14])) / 100);
        voltage = String.valueOf((Byte.toUnsignedInt(data[13]) << 8) | Byte.toUnsignedInt(data[12]));
    }
}
