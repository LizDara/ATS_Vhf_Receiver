package com.atstrack.ats.ats_vhf_receiver.Models;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.Calendar;

public class Data {
    public String fileName;
    public ArrayList<byte[]> packets;

    public Data(int type) {
        packets = new ArrayList<>();
        setFileName(type);
    }

    private void setFileName(int type) {
        Calendar time = Calendar.getInstance();
        String month = ((time.get(Calendar.MONTH) + 1) < 10) ? "0" + (time.get(Calendar.MONTH) + 1) : String.valueOf(time.get(Calendar.MONTH) + 1);
        String day = (time.get(Calendar.DAY_OF_MONTH) < 10) ? "0" + time.get(Calendar.DAY_OF_MONTH) : String.valueOf(time.get(Calendar.DAY_OF_MONTH));
        String hour = (time.get(Calendar.HOUR_OF_DAY) < 10) ? "0" + time.get(Calendar.HOUR_OF_DAY) : String.valueOf(time.get(Calendar.HOUR_OF_DAY));
        String minute = (time.get(Calendar.MINUTE) < 10) ? "0" + time.get(Calendar.MINUTE) : String.valueOf(time.get(Calendar.MINUTE));
        String second = (time.get(Calendar.SECOND) < 10) ? "0" + time.get(Calendar.SECOND) : String.valueOf(time.get(Calendar.SECOND));
        if (type == ValueCodes.RAW_FILE) {
            fileName = "D" + ReceiverInformation.getReceiverInformation().getSerialNumber() + "_" + month + day + (time.get(Calendar.YEAR) - 2000)
                    + hour + minute + second + "Raw.txt";
        } else if (type == ValueCodes.PROCESSED_FILE) {
            fileName = "D" + ReceiverInformation.getReceiverInformation().getSerialNumber() + "_" + month + day + (time.get(Calendar.YEAR) - 2000)
                    + hour + minute + second + ".txt";
        } else if (type == ValueCodes.METRICS_FILE) {
            fileName = "D" + ReceiverInformation.getReceiverInformation().getSerialNumber() + "_" + month + day + (time.get(Calendar.YEAR) - 2000)
                    + hour + minute + second + "Met.txt";
        } else if (type == ValueCodes.BLUETOOTH_FILE) {
            fileName = "ATS_BTBT_" + time.get(Calendar.YEAR) + month + day + "_" + hour + minute + second + ".txt";
        } else if (type == ValueCodes.LOG_FILE) {
            fileName = "L" + ReceiverInformation.getReceiverInformation().getSerialNumber() + "_" + month + day + (time.get(Calendar.YEAR) - 2000)
                    + hour + minute + second + ".txt";
        }
    }
}
