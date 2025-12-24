package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

public class TagDetail {
    public String code;
    public String rssi;
    public String temperature;
    public int detections;

    public TagDetail(byte[] data) {
        setValues(data);
        detections = 1;
    }

    public void updateData(byte[] data) {
        setValues(data);
        detections++;
    }

    private void setValues(byte[] data) {
        code = Converters.getAsciiValue(6, 14, data);
        rssi = Converters.getAsciiValue(24, 28, data);
        temperature = Converters.getAsciiValue(40, 44, data);
    }
}
