package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

public class MobileDefaults {
    public byte[] originalBytes;
    public int tableNumber = 255;
    public double scanRate = 255;
    public boolean gpsOn = true;
    public boolean autoRecordOn = true;

    public MobileDefaults() {}

    public MobileDefaults(byte[] data) {
        tableNumber = Integer.parseInt(Converters.getDecimalValue(data[1]));
        scanRate = Integer.parseInt(Converters.getDecimalValue(data[3])) * 0.1;
        gpsOn = (Integer.parseInt(Converters.getDecimalValue(data[2])) >> 7 & 1) == 1;
        autoRecordOn = (Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1) == 1;
        originalBytes = data;
    }
}
