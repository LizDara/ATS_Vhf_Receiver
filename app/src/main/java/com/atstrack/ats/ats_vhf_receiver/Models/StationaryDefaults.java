package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class StationaryDefaults {
    public int firstTableNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public int secondTableNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public int thirdTableNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public int scanRate = Byte.toUnsignedInt(ValueCodes.NULL);
    public int scanTimeout = Byte.toUnsignedInt(ValueCodes.NULL);
    public int antennaNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public boolean dataTransferOn = true;
    public int storeRate = Byte.toUnsignedInt(ValueCodes.NULL);
    public boolean referenceFrequencyOn = true;
    public int referenceFrequency = Byte.toUnsignedInt(ValueCodes.NULL);
    public int referenceStoreRate = Byte.toUnsignedInt(ValueCodes.NULL);
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
