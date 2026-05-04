package com.atstrack.ats.ats_vhf_receiver.Models;

public class MobileDefaults {
    public byte[] originalBytes;
    public int tableNumber = 255;
    public double scanRate = 255;
    public boolean gpsOn = true;
    public boolean autoRecordOn = true;

    public MobileDefaults() {}

    public MobileDefaults(byte[] data) {
        tableNumber = Byte.toUnsignedInt(data[1]);
        scanRate = Byte.toUnsignedInt(data[3]) * 0.1;
        gpsOn = (Byte.toUnsignedInt(data[2]) >> 7 & 1) == 1;
        autoRecordOn = (Byte.toUnsignedInt(data[2]) >> 6 & 1) == 1;
        originalBytes = data;
    }
}
