package com.atstrack.ats.ats_vhf_receiver.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class DetectionFilter implements Parcelable {
    public static final byte CODED = 0x09;
    public static final byte FIXED = 0x08;
    public static final byte VARIABLE = 0x07;
    public static final byte VARIABLE_TEMPERATURE = 0x06;

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
            case CODED:
                break;
            case FIXED:
                pulseRate1 = Byte.toUnsignedInt(data[3]);
                pulseRateTolerance1 = Byte.toUnsignedInt(data[4]);
                pulseRate2 = Byte.toUnsignedInt(data[5]);
                pulseRateTolerance2 = Byte.toUnsignedInt(data[6]);
                pulseRate3 = Byte.toUnsignedInt(data[7]);
                pulseRateTolerance3 = Byte.toUnsignedInt(data[8]);
                pulseRate4 = Byte.toUnsignedInt(data[9]);
                pulseRateTolerance4 = Byte.toUnsignedInt(data[10]);
                break;
            case VARIABLE:
                maxPulseRate = Byte.toUnsignedInt(data[3]);
                minPulseRate = Byte.toUnsignedInt(data[5]);
                optionalData = Byte.toUnsignedInt(data[11]);
                break;
        }
    }

    protected DetectionFilter(Parcel in) {
        detectionType = in.readByte();
        matches = in.readInt();
        pulseRate1 = in.readInt();
        pulseRate2 = in.readInt();
        pulseRate3 = in.readInt();
        pulseRate4 = in.readInt();
        pulseRateTolerance1 = in.readInt();
        pulseRateTolerance2 = in.readInt();
        pulseRateTolerance3 = in.readInt();
        pulseRateTolerance4 = in.readInt();
        maxPulseRate = in.readInt();
        minPulseRate = in.readInt();
        optionalData = in.readInt();
    }

    public static final Creator<DetectionFilter> CREATOR = new Creator<DetectionFilter>() {
        @Override
        public DetectionFilter createFromParcel(Parcel in) {
            return new DetectionFilter(in);
        }

        @Override
        public DetectionFilter[] newArray(int size) {
            return new DetectionFilter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte(detectionType);
        parcel.writeInt(matches);
        parcel.writeInt(pulseRate1);
        parcel.writeInt(pulseRate2);
        parcel.writeInt(pulseRate3);
        parcel.writeInt(pulseRate4);
        parcel.writeInt(pulseRateTolerance1);
        parcel.writeInt(pulseRateTolerance2);
        parcel.writeInt(pulseRateTolerance3);
        parcel.writeInt(pulseRateTolerance4);
        parcel.writeInt(maxPulseRate);
        parcel.writeInt(minPulseRate);
        parcel.writeInt(optionalData);
    }
}
