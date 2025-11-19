package com.atstrack.ats.ats_vhf_receiver.Models;

public class ScanDetail {
    public int code, detection, period, pulseRate, signalStrength;
    public boolean mortality;
    public int type; // For non coded types

    public ScanDetail(int code, int detection, boolean mortality, int signalStrength) {
        this.code = code;
        this.detection = detection;
        this.mortality = mortality;
        this.signalStrength = signalStrength;
    }

    public ScanDetail(int period, int detection, int pulseRate, int signalStrength, int type) {
        this.period = period;
        this.detection = detection;
        this.pulseRate = pulseRate;
        this.signalStrength = signalStrength;
        this.type = type;
    }
}
