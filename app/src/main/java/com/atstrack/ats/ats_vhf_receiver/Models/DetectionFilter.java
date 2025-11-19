package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

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

    public DetectionFilter(byte[] data) {
        detectionType = data[1];
        matches = Integer.parseInt(Converters.getDecimalValue(data[2]));
        switch (Converters.getHexValue(data[1])) {
            case "09":
                break;
            case "08":
                pulseRate1 = Integer.parseInt(Converters.getDecimalValue(data[3]));
                pulseRateTolerance1 = Integer.parseInt(Converters.getDecimalValue(data[4]));
                pulseRate2 = Integer.parseInt(Converters.getDecimalValue(data[5]));
                pulseRateTolerance2 = Integer.parseInt(Converters.getDecimalValue(data[6]));
                pulseRate3 = Integer.parseInt(Converters.getDecimalValue(data[7]));
                pulseRateTolerance3 = Integer.parseInt(Converters.getDecimalValue(data[8]));
                pulseRate4 = Integer.parseInt(Converters.getDecimalValue(data[9]));
                pulseRateTolerance4 = Integer.parseInt(Converters.getDecimalValue(data[10]));
                break;
            case "07":
                maxPulseRate = Integer.parseInt(Converters.getDecimalValue(data[3]));
                minPulseRate = Integer.parseInt(Converters.getDecimalValue(data[5]));
                optionalData = Integer.parseInt(Converters.getDecimalValue(data[11]));
                break;
        }
    }
}
