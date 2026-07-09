package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

public class TagDetail {
    public String code;
    public String rssi;
    public String temperature;
    public String voltage;
    public double latitude;
    public double longitude;
    public long lastTimestamp;

    public TagDetail(byte[] data, double latitude, double longitude, long lastTimestamp) { // Bluetooth Receiver (exported data)
        code = Converters.getAsciiValue(6, 14, data);
        rssi = Converters.getAsciiValue(24, 28, data);
        temperature = Converters.getAsciiValue(40, 44, data);
        voltage = Converters.getAsciiValue(54, 58, data);
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastTimestamp = lastTimestamp;
    }

    public TagDetail(byte[] data, String rssi, double latitude, double longitude, long lastTimestamp) { // Tags (exported data)
        code = Converters.getHexValue(data[4]) + Converters.getHexValue(data[5]) + Converters.getHexValue(data[6]) + Converters.getHexValue(data[7]);
        temperature = String.format("%.1f", (double) ((Byte.toUnsignedInt(data[15]) << 8) | Byte.toUnsignedInt(data[14])) / 100);
        voltage = String.valueOf((Byte.toUnsignedInt(data[13]) << 8) | Byte.toUnsignedInt(data[12]));
        this.rssi = rssi;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastTimestamp = lastTimestamp;
    }
}