package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

public class Coefficients {
    public int position;
    public int frequency = 255;
    public int coefficientA = 255;
    public int coefficientB = 255;
    public int constant = 255;
    public boolean isCoefficientANegative = true;
    public boolean isCoefficientBNegative = true;
    public boolean isConstantNegative = true;

    public Coefficients() {}

    public Coefficients(int position, int frequency) {
        this.position = position;
        this.frequency = frequency;
    }

    public void setData(byte[] data) {
        coefficientA = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        coefficientB = (Integer.parseInt(Converters.getDecimalValue(data[8])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[9]));
        constant = (Integer.parseInt(Converters.getDecimalValue(data[11])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[12]));
        isCoefficientANegative = Converters.getHexValue(data[4]).equals("80");
        isCoefficientBNegative = Converters.getHexValue(data[7]).equals("80");
        isConstantNegative = Converters.getHexValue(data[10]).equals("80");
    }
}
