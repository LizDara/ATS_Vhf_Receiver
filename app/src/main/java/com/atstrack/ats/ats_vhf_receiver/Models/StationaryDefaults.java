package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class StationaryDefaults {
    public int firstTableNumber = 255;
    public int secondTableNumber = 255;
    public int thirdTableNumber = 255;
    public int scanRate = 255;
    public int scanTimeout = 255;
    public int antennaNumber = 255;
    public boolean dataTransferOn = true;
    public int storeRate = 255;
    public boolean referenceFrequencyOn = true;
    public int referenceFrequency = 255;
    public int referenceStoreRate = 255;
    public byte[] originalBytes;

    public StationaryDefaults() {}

    public StationaryDefaults(int baseFrequency, byte[] data) {
        firstTableNumber = Byte.toUnsignedInt(data[9]);
        secondTableNumber = Byte.toUnsignedInt(data[10]);
        thirdTableNumber = Byte.toUnsignedInt(data[11]);
        scanRate = Byte.toUnsignedInt(data[3]);
        scanTimeout = Byte.toUnsignedInt(data[4]);
        antennaNumber = Byte.toUnsignedInt(data[1]);
        dataTransferOn = data[2] != 0;
        storeRate = Byte.toUnsignedInt(data[5]);
        referenceFrequencyOn = (data[6] != ValueCodes.NULL || data[7] != ValueCodes.NULL)
                && (data[6] != ValueCodes.NONE || data[7] != ValueCodes.NONE);
        referenceFrequency = referenceFrequencyOn ? (Byte.toUnsignedInt(data[6]) * 256) + Byte.toUnsignedInt(data[7]) + baseFrequency : 0;
        referenceStoreRate = referenceFrequencyOn ? Byte.toUnsignedInt(data[8]) : 0;
        originalBytes = data;
    }
}
