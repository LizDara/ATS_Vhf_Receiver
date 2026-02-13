package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

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
        firstTableNumber = Integer.parseInt(Converters.getDecimalValue(data[9]));
        secondTableNumber = Integer.parseInt(Converters.getDecimalValue(data[10]));
        thirdTableNumber = Integer.parseInt(Converters.getDecimalValue(data[11]));
        scanRate = Integer.parseInt(Converters.getDecimalValue(data[3]));
        scanTimeout = Integer.parseInt(Converters.getDecimalValue(data[4]));
        antennaNumber = Integer.parseInt(Converters.getDecimalValue(data[1]));
        dataTransferOn = data[2] != 0;
        storeRate = Integer.parseInt(Converters.getDecimalValue(data[5]));
        referenceFrequencyOn = (!Converters.getHexValue(data[6]).equals("FF") || !Converters.getHexValue(data[7]).equals("FF"))
                && (!Converters.getHexValue(data[6]).equals("00") || !Converters.getHexValue(data[7]).equals("00"));
        referenceFrequency = referenceFrequencyOn ? (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[7])) + baseFrequency : 0;
        referenceStoreRate = referenceFrequencyOn ? Integer.parseInt(Converters.getDecimalValue(data[8])) : 0;
        originalBytes = data;
    }
}
