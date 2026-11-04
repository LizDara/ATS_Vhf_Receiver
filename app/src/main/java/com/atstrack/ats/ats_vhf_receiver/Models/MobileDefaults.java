package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class MobileDefaults {
    public byte[] originalBytes;
    public int tableNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public double scanTime = Byte.toUnsignedInt(ValueCodes.NULL);
    public boolean gpsOn = true;
    public boolean autoRecordOn = true;

    public MobileDefaults() {}

    public MobileDefaults(byte[] data) {
        tableNumber = Byte.toUnsignedInt(data[1]);
        scanTime = Byte.toUnsignedInt(data[3]) / 10.0;
        gpsOn = (Byte.toUnsignedInt(data[2]) >> 7 & 1) == 1;
        autoRecordOn = (Byte.toUnsignedInt(data[2]) >> 6 & 1) == 1;
        originalBytes = data;
    }

    public boolean existNotSet() {
        if (tableNumber == Byte.toUnsignedInt(ValueCodes.NULL)) return true;
        return scanTime == Byte.toUnsignedInt(ValueCodes.NULL);
    }

    public boolean isDataCorrect() {
        boolean tableCorrect = tableNumber != ValueCodes.NONE;
        boolean scanTimeCorrect = scanTime >= 1.5 && scanTime <= 25.5;
        return tableCorrect && scanTimeCorrect;
    }
}
