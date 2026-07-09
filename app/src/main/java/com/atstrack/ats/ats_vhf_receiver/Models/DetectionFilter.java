package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class DetectionFilter {
    public byte detectionType;
    public int matches;
    public int pulseRate1 = 0;
    public int pulseRate2 = 0;
    public int pulseRate3 = 0;
    public int pulseRate4 = 0;
    public int pulseRateTolerance1 = 0;
    public int pulseRateTolerance2 = 0;
    public int pulseRateTolerance3 = 0;
    public int pulseRateTolerance4 = 0;
    public int maxPulseRate = 0;
    public int minPulseRate = 0;
    public int optionalData = 0;

    public DetectionFilter() {}

    public DetectionFilter(byte[] data) {
        detectionType = data[1];
        matches = Byte.toUnsignedInt(data[2]);
        switch (data[1]) {
            case ValueCodes.CODED:
                break;
            case ValueCodes.FIXED:
                pulseRate1 = Byte.toUnsignedInt(data[3]);
                pulseRateTolerance1 = Byte.toUnsignedInt(data[4]);
                pulseRate2 = Byte.toUnsignedInt(data[5]);
                pulseRateTolerance2 = Byte.toUnsignedInt(data[6]);
                pulseRate3 = Byte.toUnsignedInt(data[7]);
                pulseRateTolerance3 = Byte.toUnsignedInt(data[8]);
                pulseRate4 = Byte.toUnsignedInt(data[9]);
                pulseRateTolerance4 = Byte.toUnsignedInt(data[10]);
                break;
            case ValueCodes.VARIABLE:
                maxPulseRate = Byte.toUnsignedInt(data[3]);
                minPulseRate = Byte.toUnsignedInt(data[5]);
                optionalData = Byte.toUnsignedInt(data[11]);
                break;
        }
    }
}
