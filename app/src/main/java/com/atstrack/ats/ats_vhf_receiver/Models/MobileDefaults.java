package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class MobileDefaults {
    public byte[] originalBytes;
    public int tableNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public double scanRate = Byte.toUnsignedInt(ValueCodes.NULL);
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
