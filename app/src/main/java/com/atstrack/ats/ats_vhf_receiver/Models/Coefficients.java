package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class Coefficients {
    public int position = -1;
    public int frequency = Byte.toUnsignedInt(ValueCodes.NULL);
    public int coefficientA = Byte.toUnsignedInt(ValueCodes.NULL);
    public int coefficientB = Byte.toUnsignedInt(ValueCodes.NULL);
    public int constant = Byte.toUnsignedInt(ValueCodes.NULL);
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
