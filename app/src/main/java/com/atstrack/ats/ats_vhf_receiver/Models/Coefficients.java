package com.atstrack.ats.ats_vhf_receiver.Models;

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
        coefficientA = (Byte.toUnsignedInt(data[5]) * 256) + Byte.toUnsignedInt(data[6]);
        coefficientB = (Byte.toUnsignedInt(data[8]) * 256) + Byte.toUnsignedInt(data[9]);
        constant = (Byte.toUnsignedInt(data[11]) * 256) + Byte.toUnsignedInt(data[12]);
        isCoefficientANegative = data[4] == (byte) 0x80;
        isCoefficientBNegative = data[7] == (byte) 0x80;
        isConstantNegative = data[10] == (byte) 0x80;
    }
}
