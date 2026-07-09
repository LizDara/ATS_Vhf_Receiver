package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

public class Detection {
    public String rssi;
    public String temperature;
    public String voltage;
    public double latitude;
    public double longitude;
    public long timestamp;

    public Detection(byte[] data, double latitude, double longitude, long timestamp) { // Bluetooth Receiver
        rssi = Converters.getAsciiValue(24, 28, data);
        temperature = Converters.getAsciiValue(40, 44, data);
        voltage = Converters.getAsciiValue(54, 58, data);
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public Detection(byte[] data, double latitude, double longitude, int rssi, long timestamp) { // Tag Directly
        temperature = String.format("%.1f", (double) ((Byte.toUnsignedInt(data[15]) << 8) | Byte.toUnsignedInt(data[14])) / 100);
        voltage = String.valueOf((Byte.toUnsignedInt(data[13]) << 8) | Byte.toUnsignedInt(data[12]));
        this.latitude = latitude;
        this.longitude = longitude;
        this.rssi = String.valueOf(rssi);
        this.timestamp = timestamp;
    }
}
