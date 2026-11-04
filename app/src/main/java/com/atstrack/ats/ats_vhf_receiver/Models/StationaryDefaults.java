package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class StationaryDefaults {
    public int firstTableNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public int secondTableNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public int thirdTableNumber = Byte.toUnsignedInt(ValueCodes.NULL);
    public int scanTime = Byte.toUnsignedInt(ValueCodes.NULL);
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
        firstTableNumber = Byte.toUnsignedInt(data[9]) > 0 && Byte.toUnsignedInt(data[9]) <= 12 ? Byte.toUnsignedInt(data[9]) : 0;
        secondTableNumber = Byte.toUnsignedInt(data[10]) > 0 && Byte.toUnsignedInt(data[10]) <= 12 ? Byte.toUnsignedInt(data[10]) : 0;
        thirdTableNumber = Byte.toUnsignedInt(data[11]) > 0 && Byte.toUnsignedInt(data[11]) <= 12 ? Byte.toUnsignedInt(data[11]) : 0;
        scanTime = Byte.toUnsignedInt(data[3]);
        scanTimeout = Byte.toUnsignedInt(data[4]);
        antennaNumber = Byte.toUnsignedInt(data[1]);
        dataTransferOn = data[2] != 0;
        storeRate = Byte.toUnsignedInt(data[5]);
        referenceFrequencyOn = data[6] != ValueCodes.NULL || data[7] != ValueCodes.NULL;
        referenceFrequency = referenceFrequencyOn ? (Byte.toUnsignedInt(data[6]) * 256) + Byte.toUnsignedInt(data[7]) + baseFrequency : Byte.toUnsignedInt(ValueCodes.NULL);
        referenceStoreRate = referenceFrequencyOn ? Byte.toUnsignedInt(data[8]) : Byte.toUnsignedInt(ValueCodes.NULL);
        originalBytes = data;
    }

    public String getTablesText() {
        String tables = "";
        if (firstTableNumber != ValueCodes.NONE && firstTableNumber != Byte.toUnsignedInt(ValueCodes.NULL))
            tables += firstTableNumber;
        if (secondTableNumber != ValueCodes.NONE && secondTableNumber != Byte.toUnsignedInt(ValueCodes.NULL))
            tables += ", " + secondTableNumber;
        if (thirdTableNumber != ValueCodes.NONE && thirdTableNumber != Byte.toUnsignedInt(ValueCodes.NULL))
            tables += ", " + thirdTableNumber;
        return tables;
    }

    public boolean existNotSet() {
        if (firstTableNumber == Byte.toUnsignedInt(ValueCodes.NULL) && secondTableNumber == Byte.toUnsignedInt(ValueCodes.NULL) && thirdTableNumber == Byte.toUnsignedInt(ValueCodes.NULL)) return true;
        if (antennaNumber == Byte.toUnsignedInt(ValueCodes.NULL)) return true;
        if (scanTime == Byte.toUnsignedInt(ValueCodes.NULL)) return true;
        if (scanTimeout == Byte.toUnsignedInt(ValueCodes.NULL)) return true;
        if (storeRate == Byte.toUnsignedInt(ValueCodes.NULL)) return true;
        if (referenceFrequencyOn) {
            if (referenceFrequency == Byte.toUnsignedInt(ValueCodes.NULL)) return true;
            return referenceStoreRate == Byte.toUnsignedInt(ValueCodes.NULL);
        }
        return false;
    }

    public boolean isDataCorrect() {
        boolean scanTimeoutCorrect = scanTimeout >= 4 && scanTimeout <= 200;
        boolean scanTimeCorrect = scanTime >= 10 && scanTime <= 255;
        boolean scanDifferenceCorrect = scanTimeout < scanTime;
        boolean antennaNumberCorrect = antennaNumber >= 1 && antennaNumber <= 4;
        boolean storeRateCorrect = storeRate == ValueCodes.NONE || storeRate == 5 || storeRate == 10 || storeRate == 15 || storeRate == 30 || storeRate == 60 || storeRate == 120 || storeRate == 240;
        boolean referenceCorrect;
        if (referenceFrequencyOn)
            referenceCorrect = referenceFrequency != Byte.toUnsignedInt(ValueCodes.NULL) && referenceStoreRate >= 1 && referenceStoreRate <= 24;
        else
            referenceCorrect = referenceFrequency == Byte.toUnsignedInt(ValueCodes.NULL) && referenceStoreRate == Byte.toUnsignedInt(ValueCodes.NULL);
        return scanTimeoutCorrect && scanTimeCorrect && scanDifferenceCorrect && antennaNumberCorrect && storeRateCorrect && referenceCorrect;
    }
}
