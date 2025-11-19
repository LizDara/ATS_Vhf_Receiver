package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

public class StationaryDefaults {
    public int firstTableNumber;
    public int secondTableNumber;
    public int thirdTableNumber;
    public int scanRate;
    public int scanTimeout;
    public int antennaNumber;
    public boolean dataTransferOn;
    public int storeRate;
    public boolean referenceFrequencyOn;
    public int referenceFrequency;
    public int referenceStoreRate;

    public StationaryDefaults(int baseFrequency, byte[] data) {
        firstTableNumber = Integer.parseInt(Converters.getHexValue(data[9]));
        secondTableNumber = Integer.parseInt(Converters.getHexValue(data[10]));
        thirdTableNumber = Integer.parseInt(Converters.getHexValue(data[11]));
        scanRate = Integer.parseInt(Converters.getHexValue(data[3]));
        scanTimeout = Integer.parseInt(Converters.getHexValue(data[4]));
        antennaNumber = Integer.parseInt(Converters.getHexValue(data[1]));
        dataTransferOn = data[2] != 0;
        storeRate = Integer.parseInt(Converters.getHexValue(data[5]));
        referenceFrequencyOn = (!Converters.getHexValue(data[6]).equals("FF") || !Converters.getHexValue(data[7]).equals("FF"))
                && (!Converters.getHexValue(data[6]).equals("00") || !Converters.getHexValue(data[7]).equals("00"));
        referenceFrequency = referenceFrequencyOn ? (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[7])) + baseFrequency : 0;
        referenceStoreRate = referenceFrequencyOn ? Integer.parseInt(Converters.getDecimalValue(data[8])) : 0;
    }
}
