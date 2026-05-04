package com.atstrack.ats.ats_vhf_receiver.Utils;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.pow;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.Snapshots;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetail;
import com.atstrack.ats.ats_vhf_receiver.R;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Converters {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String getHexValue(byte[] value) { // Gets value in hexadecimal system
        if (value == null)
            return "";

        char[] hexChars = new char[value.length * 3];
        int v;
        for (int j = 0; j < value.length; j++) {
            v = value[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static String getHexValue(byte b) { // Gets value in hexadecimal system for single byte
        char[] hexChars = new char[2];
        int v;
        v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    public static String getDecimalValue(byte b) { // Gets value in decimal system for single byte
        String result = "";
        result += ((int) b & 0xff);

        return result;
    }

    public static byte[] convertToUTF8(String input) {
        byte[] returnVal;
        returnVal = input.getBytes(StandardCharsets.UTF_8);
        return returnVal;
    }

    public static String getAsciiValue(int start, int end, byte[] data) {
        String value = "";
        for (int i = start; i < end; i++)
            value += (char) data[i];
        return value;
    }

    public static String getFrequency(int frequency) {
        return String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3);
    }

    public static int getFrequencyNumber(String frequency) {
        return Integer.parseInt(frequency.replace(".", ""));
    }

    /**
     * Gets the status of the devices found.
     * @param deviceName The content of the advertisement record offered by the remote device.
     * @return Return the device status.
     */
    public static String getStatusVhfReceiver(String deviceName) {
        String status = deviceName.substring(16, 17);
        switch (status) {
            case "0":
                status = " Not scanning";
                break;
            case "2":
                status = " Scanning, mobile";
                break;
            case "3":
                status = " Scanning, stationary";
                break;
            case "6":
                status = " Scanning, manual";
                break;
            default:
                status = " None";
                break;
        }
        return status;
    }

    public static String getDetectionFilter(String type) {
        switch (type) {
            case "C":
                return " Coded,";
            case "F":
                return " Fixed PR,";
            case "V":
                return " Variable PR,";
        }
        return "None,";
    }

    public static String getDetectionFilter(byte type) {
        switch (type) {
            case DetectionFilter.CODED:
                return "Coded";
            case DetectionFilter.FIXED:
                return "Fixed Pulse Rate";
            case DetectionFilter.VARIABLE:
            case DetectionFilter.VARIABLE_TEMPERATURE:
                return "Variable Pulse Rate";
        }
        return "";
    }

    /**
     * Gets the percentage of the device's battery.
     * @param scanRecord The content of the advertisement record offered by the remote device.
     * @return Return the battery percentage.
     */
    public static int getPercentBatteryVhfReceiver(byte[] scanRecord) {
        int firstElement = Byte.toUnsignedInt(scanRecord[0]);
        return Byte.toUnsignedInt(scanRecord[firstElement + 5]);
    }

    public static int getDeviceType(String name, boolean isLogo) {
        if (name.contains(ValueCodes.VHF) || name.contains("VHF")) {
            return isLogo ? R.drawable.ic_reptile_icon : R.drawable.ic_vhf_receiver;
        } else if (name.contains(ValueCodes.ACOUSTIC) || name.contains("Acoustic"))
            return isLogo ? R.drawable.ic_salmon_icon : R.drawable.ic_acoustic_receiver;
        else if (name.contains(ValueCodes.WILDLINK) || name.contains("Wildlink"))
            return isLogo ? R.drawable.ic_deer_icon : R.drawable.ic_wildlink_receiver;
        else if (name.contains("Bluetooth Tags"))
            return isLogo ? R.drawable.ic_bird_icon : R.drawable.ic_bluetooth_tag;
        else if (name.contains(ValueCodes.BLUETOOTH_RECEIVER) || name.contains("Bluetooth Receiver"))
            return isLogo ? R.drawable.bluetooth_receiver : R.drawable.ic_bluetooth_tag;
        else if (name.contains(ValueCodes.BEACON) || name.contains("Beacon"))
            return isLogo ? R.drawable.ic_transmitter_icon : R.drawable.ic_bluetooth_tag;
        return 0;
    }

    public static boolean isDefaultEmpty(byte[] data) {
        boolean isEmpty = true;
        for (int i = 1; i < data.length; i++) {
            if (data[i] != (byte) 0xFF) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    public static boolean areCoefficientsEmpty(byte[] data) {
        boolean isEmpty = true;
        for (int i = 4; i < data.length; i++) {
            if (data[i] != (byte) 0xFF) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    public static String[] getGpsData(byte[] data) {
        String[] coordinates = new String[2];
        float A, B1, B2, C, D;
        byte sign;
        int degrees, minutes;
        float latitude, longitude;

        //Latitude, byte 4 to 7
        A = (float) (data[4] & 0x7F);
        sign = (byte) (data[4] & 0x80);
        B1 = (float) (data[5] & 0x80);
        B2 = (float) (data[5] & 0x7F);
        C = (float) Byte.toUnsignedInt(data[6]);
        D = (float) Byte.toUnsignedInt(data[7]);
        if (data[4] == ValueCodes.NULL && data[5] == ValueCodes.NULL && data[6] == ValueCodes.NULL && data[7] == ValueCodes.NULL)
            latitude = 0;
        else
            latitude = (float)((1 + ((B2 + ((C + (D / 256)) / 256)) / 128)) * (pow(2, (A * 2) + (B1 / 128) - 127)));
        latitude = latitude / 1000000;
        degrees = (int) floor(abs(latitude));
        latitude = (latitude - degrees) * 100 / 60;
        latitude = latitude + degrees;
        if ((latitude * 1000000) == 0)
            coordinates[0] = "0";
        else
            coordinates[0] = sign == (byte) 0x80 ? "-" : "+";
        minutes = (int) ((latitude - degrees) * 1000000);
        /*if (minutes > 99999) {
            minutes -= 1000000;
            degrees++;
        }*/
        if (degrees < 10)
            coordinates[0] += "0";
        coordinates[0] += degrees + ".";
        if (minutes < 100000)
            coordinates[0] += "0";
        if (minutes < 10000)
            coordinates[0] += "0";
        if (minutes < 1000)
            coordinates[0] += "0";
        if (minutes < 100)
            coordinates[0] += "0";
        if (minutes < 10)
            coordinates[0] += "0";
        coordinates[0] += String.valueOf(minutes);

        //Longitude, byte 12 to 15
        A = (float) (data[12] & 0x7F);
        sign = (byte) (data[12] & 0x80);
        B1 = (float) (data[13] & 0x80);
        B2 = (float) (data[13] & 0x7F);
        C = (float) Byte.toUnsignedInt(data[14]);
        D = (float) Byte.toUnsignedInt(data[15]);
        if (data[12] != ValueCodes.NULL && data[13] != ValueCodes.NULL && data[14] != ValueCodes.NULL && data[15] != ValueCodes.NULL)
            longitude = 0;
        else
            longitude = (float)((1 + ((B2 + ((C + (D / 256)) / 256)) / 128)) * (pow(2, (A * 2) + (B1 / 128) - 127)));
        longitude = longitude / 1000000;
        degrees = (int) floor(abs(longitude));
        longitude = (longitude - degrees) * 100 / 60;
        longitude = longitude + degrees;
        if ((longitude * 1000000) == 0)
            coordinates[1] = "0";
        else
            coordinates[1] = sign == (byte) 0x80 ? "-" : "+";
        minutes = (int) ((longitude - degrees) * 1000000);
        /*if (minutes > 99999) {
            minutes -= 1000000;
            degrees++;
        }*/
        if (degrees < 100)
            coordinates[1] += "0";
        if (degrees < 10)
            coordinates[1] += "0";
        coordinates[1] += degrees + ".";
        if (minutes < 100000)
            coordinates[1] += "0";
        if (minutes < 10000)
            coordinates[1] += "0";
        if (minutes < 1000)
            coordinates[1] += "0";
        if (minutes < 100)
            coordinates[1] += "0";
        if (minutes < 10)
            coordinates[1] += "0";
        coordinates[1] += String.valueOf(minutes);

        return coordinates;
    }

    /**
     * Finds the page number of a 4-byte packet.
     * @param packet The received packet.
     * @return Returns the page number.
     */
    public static int findPageNumber(byte[] packet) {
        int pageNumber = Byte.toUnsignedInt(packet[0]);
        pageNumber = (Byte.toUnsignedInt(packet[1]) << 8) | pageNumber;
        pageNumber = (Byte.toUnsignedInt(packet[2]) << 16) | pageNumber;
        pageNumber = (Byte.toUnsignedInt(packet[3]) << 24) | pageNumber;
        return pageNumber;
    }

    /**
     * Processes the data when the download is complete.
     *
     * @param data The raw data.
     * @param process_percent Percent view.
     * @param baseActivity Base activity
     * @return Returns the processed data.
     */
    public static synchronized String getPackageProcessed(ArrayList<byte[]> data, View process_percent, BaseActivity baseActivity, boolean isRawFile) {
        String text = "";
        byte scanType = 0;
        int baseFrequency = 0;
        int frequency = 0;
        int frequencyTableIndex = 0;
        int YY = 0;
        int MM, DD, hh, mm, ss;
        int antenna = 0;
        int sessionNumber = 1;
        int date, secondsOffset, signalStrength, code, mort, numberDetection, periodHi, periodLo;
        Calendar baseDateTime = Calendar.getInstance();
        Calendar currentDateTime = Calendar.getInstance();
        int byteIndex = 0;

        for (byte[] packet : data) {
            int index = 0;
            while (index < packet.length) {
                byte format = packet[index];
                String[] coordinates = new String[]{"0", "0"};
                String gpsTimeStamp = "0";
                switch (format) {
                    case ValueCodes.STATIONARY_SCAN_COMMAND:
                    case ValueCodes.MOBILE_SCAN_COMMAND:
                    case ValueCodes.MANUAL_SCAN_COMMAND:
                        int matches;
                        byte detectionType = 0;
                        scanType = packet[index];
                        YY = Byte.toUnsignedInt(packet[index + 6]);
                        text += "[Header]" + ValueCodes.CR + ValueCodes.LF;
                        switch (format) {
                            case ValueCodes.STATIONARY_SCAN_COMMAND: {
                                baseFrequency = Byte.toUnsignedInt(packet[index + 20]) * 1000;
                                text += "Scan Type: Stationary" + ValueCodes.CR + ValueCodes.LF;
                                text += "Scan Interval (seconds): " + Byte.toUnsignedInt(packet[index + 3]) + ValueCodes.CR + ValueCodes.LF;
                                text += "Scan Timeout (seconds): " + Byte.toUnsignedInt(packet[index + 4]) + ValueCodes.CR + ValueCodes.LF;
                                text += "Num of Antennas: " + (Byte.toUnsignedInt(packet[index + 1]) + 1) + ValueCodes.CR + ValueCodes.LF;
                                text += "Store Interval (minutes): " + (packet[index + 5] == ValueCodes.NONE ? "Continuous" : Converters.getDecimalValue(packet[index + 5])) + ValueCodes.CR + ValueCodes.LF;
                                int referenceFrequency = (Byte.toUnsignedInt(packet[index + 9]) * 256) + Byte.toUnsignedInt(packet[index + 10]) + baseFrequency;
                                text += "Reference Frequency: " + (referenceFrequency == baseFrequency ? "No" : Converters.getFrequency(referenceFrequency)) + ValueCodes.CR + ValueCodes.LF;
                                text += "Reference Frequency Store Interval (minutes): " + (referenceFrequency == baseFrequency ? "No" : Converters.getDecimalValue(packet[index + 11])) + ValueCodes.CR + ValueCodes.LF;
                                detectionType = (byte) (packet[index + 2] & (byte) 0x0F);
                                matches = Byte.toUnsignedInt(packet[index + 2]) / 16;
                                String detection = "Coded";
                                String details = "";
                                if (detectionType == DetectionFilter.FIXED) {
                                    detection = "Non Coded Fixed Pulse Rate";
                                    details = matches + " matches required";
                                } else if (detectionType == DetectionFilter.VARIABLE) {
                                    detection = "Non Coded Variable Pulse Rate";
                                    details = matches + " matches required, " + Byte.toUnsignedInt(packet[index + 7]) + " to " + Byte.toUnsignedInt(packet[index + 10]) + " pulse rate range";
                                } else if (detectionType == DetectionFilter.VARIABLE_TEMPERATURE)
                                    detection = "Non Coded Variable Pulse Rate";
                                text += "Transmitter Detection Type: " + detection + ValueCodes.CR + ValueCodes.LF;
                                text += "Transmitter Detection Details: " + details + ValueCodes.CR + ValueCodes.LF;
                                index += 16;
                                byteIndex += 16;
                                break;
                            }
                            case ValueCodes.MOBILE_SCAN_COMMAND: {
                                baseFrequency = Byte.toUnsignedInt(packet[index + 5]) * 1000;
                                text += "Scan Type: Mobile" + ValueCodes.CR + ValueCodes.LF;
                                text += "Scan Interval (seconds): " + (Byte.toUnsignedInt(packet[index + 3]) * 0.1) + ValueCodes.CR + ValueCodes.LF;
                                detectionType = (byte) (packet[index + 4] & (byte) 0x0F);
                                matches = Byte.toUnsignedInt(packet[index + 4]) / 16;
                                String detection = "Coded";
                                String details = "";
                                if (detectionType == DetectionFilter.FIXED) {
                                    detection = "Non Coded Fixed Pulse Rate";
                                    details = matches + " matches required";
                                } else if (detectionType == DetectionFilter.VARIABLE) {
                                    detection = "Non Coded Variable Pulse Rate";
                                    details = matches + " matches required, " + Byte.toUnsignedInt(packet[index + 7]) + " to " + Byte.toUnsignedInt(packet[index + 10]) + " pulse rate range";
                                } else if (detectionType == DetectionFilter.VARIABLE_TEMPERATURE)
                                    detection = "Non Coded Variable Pulse Rate";
                                text += "Transmitter Detection Type: " + detection + ValueCodes.CR + ValueCodes.LF;
                                text += "Transmitter Detection Details: " + details + ValueCodes.CR + ValueCodes.LF;
                                int gps = Byte.toUnsignedInt(packet[index + 2]) >> 7 & 1;
                                text += "Gps: " + (gps == 1 ? "On" : "Off") + ValueCodes.CR + ValueCodes.LF;
                                index += 8;
                                byteIndex += 8;
                                break;
                            }
                            case ValueCodes.MANUAL_SCAN_COMMAND: {
                                if (process_percent != null) {
                                    int percent = (int) (((float) byteIndex / (float) (isRawFile ? packet.length : data.size() * Snapshots.BYTES_PER_PAGE)) * 100);
                                    baseActivity.runOnUiThread(() -> {
                                        if (isRawFile)
                                            ((ProgressBar) process_percent).setProgress(percent);
                                        else
                                            ((TextView) process_percent).setText(" - " + percent + "%");
                                    });
                                }
                                baseFrequency = Byte.toUnsignedInt(packet[index + 15]) * 1000;
                                text += "Scan Type: Manual" + ValueCodes.CR + ValueCodes.LF;
                                detectionType = (byte) (packet[index + 1] & (byte) 0x0F);
                                String detection = "Coded";
                                if (detectionType == DetectionFilter.FIXED)
                                    detection = "Non Coded Fixed Pulse Rate";
                                else if (detectionType == DetectionFilter.VARIABLE)
                                    detection = "Non Coded Variable Pulse Rate";
                                text += "Transmitter Detection Type: " + detection + ValueCodes.CR + ValueCodes.LF;
                                text += "Transmitter Detection Details: " + ValueCodes.CR + ValueCodes.LF;
                                YY = Byte.toUnsignedInt(packet[index + 2]);
                                MM = Byte.toUnsignedInt(packet[index + 3]);
                                DD = Byte.toUnsignedInt(packet[index + 4]);
                                hh = Byte.toUnsignedInt(packet[index + 5]);
                                mm = Byte.toUnsignedInt(packet[index + 6]);
                                ss = Byte.toUnsignedInt(packet[index + 7]);
                                baseDateTime.set(YY + 2000, MM - 1, DD, hh, mm, ss);
                                break;
                            }
                        }
                        text += "[Data]" + ValueCodes.CR + ValueCodes.LF;
                        text += (detectionType == DetectionFilter.CODED ?
                                "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" :
                                "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, PeriodHi, PeriodLo, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum") + ValueCodes.CR + ValueCodes.LF;
                        break;
                    case ValueCodes.SCAN_HEADER_COMMAND:
                        if (process_percent != null) {
                            int percent = (int) (((float) byteIndex / (float) (isRawFile ? packet.length : data.size() * Snapshots.BYTES_PER_PAGE)) * 100);
                            baseActivity.runOnUiThread(() -> {
                                if (isRawFile)
                                    ((ProgressBar) process_percent).setProgress(percent);
                                else
                                    ((TextView) process_percent).setText(" - " + percent + "%");
                            });
                        }

                        frequency = baseFrequency + ((Byte.toUnsignedInt(packet[index + 1]) * 256) + Byte.toUnsignedInt(packet[index + 2]));
                        frequencyTableIndex = (((Byte.toUnsignedInt(packet[index + 1]) >> 6) & 1) * 256) + Byte.toUnsignedInt(packet[index + 3]);
                        if (scanType == ValueCodes.STATIONARY_SCAN_COMMAND) {
                            antenna = Byte.toUnsignedInt(packet[index + 1]) >> 7;
                            if (antenna == 0)
                                antenna = (Byte.toUnsignedInt(packet[index + 7]) >> 6) + 1;
                        }
                        date = Byte.toUnsignedInt(packet[index + 4]) << 16 | Byte.toUnsignedInt(packet[index + 5]) << 8 | Byte.toUnsignedInt(packet[index + 6]);
                        mm = date % 100;
                        hh = (date / 100) % 100;
                        DD = (date / 10000) % 100;
                        MM = date / 1000000;
                        ss = Byte.toUnsignedInt((byte) (packet[index + 7] & (byte) 0x3F));
                        baseDateTime.set(YY + 2000, MM - 1, DD, hh, mm, ss);
                        break;
                    case ValueCodes.SCAN_FIX_CODED_COMMAND:
                        secondsOffset = Byte.toUnsignedInt(packet[index + 1]);
                        signalStrength = Byte.toUnsignedInt(packet[index + 4]);
                        code = Byte.toUnsignedInt(packet[index + 3]);
                        mort = Byte.toUnsignedInt(packet[index + 5]);
                        numberDetection = Byte.toUnsignedInt(packet[index + 7]);
                        currentDateTime.setTime(baseDateTime.getTime());
                        currentDateTime.add(Calendar.SECOND, secondsOffset);

                        if (index + 8 < packet.length && packet[index + 8] == ValueCodes.SCAN_GPS_COMMAND) {
                            byte[] gpsData = new byte[16];
                            System.arraycopy(packet, index + 8, gpsData, 0, 16);
                            coordinates = Converters.getGpsData(gpsData);

                            int year = Byte.toUnsignedInt(gpsData[1]);
                            MM = Byte.toUnsignedInt(gpsData[2]);
                            DD = Byte.toUnsignedInt(gpsData[3]);
                            hh = Byte.toUnsignedInt(gpsData[9]);
                            mm = Byte.toUnsignedInt(gpsData[10]);
                            ss = Byte.toUnsignedInt(gpsData[11]);
                            gpsTimeStamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                            index += 16;
                            byteIndex += 16;
                        }

                        text += (currentDateTime.get(Calendar.YEAR) - 2000) + ", " + currentDateTime.get(Calendar.DAY_OF_YEAR) + ", " + currentDateTime.get(Calendar.HOUR_OF_DAY) + ", " + currentDateTime.get(Calendar.MINUTE) +
                                ", " + currentDateTime.get(Calendar.SECOND) + ", " + (antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna) + ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) +
                                ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection + ", " + coordinates[0] + ", " + coordinates[1] + ", " + gpsTimeStamp + ", " +
                                ((currentDateTime.get(Calendar.MONTH) + 1) + "/" + currentDateTime.get(Calendar.DAY_OF_MONTH) + "/" + (currentDateTime.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                        break;
                    case ValueCodes.SCAN_FIX_CONSOLIDATED_CODED_COMMAND:
                        signalStrength = Byte.toUnsignedInt(packet[index + 4]);
                        code = Byte.toUnsignedInt(packet[index + 3]);
                        mort = (Byte.toUnsignedInt(packet[index + 6]) * 256) + Byte.toUnsignedInt(packet[index + 5]);
                        numberDetection = (Byte.toUnsignedInt(packet[index + 1]) * 256) + Byte.toUnsignedInt(packet[index + 7]);

                        text += (baseDateTime.get(Calendar.YEAR) - 2000) + ", " + baseDateTime.get(Calendar.DAY_OF_YEAR) + ", " + baseDateTime.get(Calendar.HOUR_OF_DAY) + ", " +
                                baseDateTime.get(Calendar.MINUTE) + ", " + baseDateTime.get(Calendar.SECOND) + ", " + (antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna) +
                                ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection +
                                ", 0, 0, 0, " + ((baseDateTime.get(Calendar.MONTH) + 1) + "/" + baseDateTime.get(Calendar.DAY_OF_MONTH) + "/" + (baseDateTime.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                        break;
                    case ValueCodes.SCAN_DATA_FIXED_NON_CODED_COMMAND:
                    case ValueCodes.SCAN_DATA_VARIABLE_NON_CODED_COMMAND:
                        secondsOffset = Byte.toUnsignedInt(packet[index + 1]);
                        signalStrength = Byte.toUnsignedInt(packet[index + 4]);
                        periodHi = Byte.toUnsignedInt(packet[index + 5]);
                        periodLo = Byte.toUnsignedInt(packet[index + 6]);
                        numberDetection = Byte.toUnsignedInt(packet[index + 7]);
                        currentDateTime.setTime(baseDateTime.getTime());
                        currentDateTime.add(Calendar.SECOND, secondsOffset);

                        if (index + 8 < packet.length && packet[index + 8] == ValueCodes.SCAN_GPS_COMMAND) {
                            byte[] gpsData = new byte[16];
                            System.arraycopy(packet, index + 8, gpsData, 0, 16);
                            coordinates = Converters.getGpsData(gpsData);

                            int year = Byte.toUnsignedInt(gpsData[1]);
                            MM = Byte.toUnsignedInt(gpsData[2]);
                            DD = Byte.toUnsignedInt(gpsData[3]);
                            hh = Byte.toUnsignedInt(gpsData[9]);
                            mm = Byte.toUnsignedInt(gpsData[10]);
                            ss = Byte.toUnsignedInt(gpsData[11]);
                            gpsTimeStamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                            index += 16;
                            byteIndex += 16;
                        }

                        text += (currentDateTime.get(Calendar.YEAR) - 2000) + ", " + currentDateTime.get(Calendar.DAY_OF_YEAR) + ", " + currentDateTime.get(Calendar.HOUR_OF_DAY) + ", " + currentDateTime.get(Calendar.MINUTE) +
                                ", " + currentDateTime.get(Calendar.SECOND) + ", " + (antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna) + ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) +
                                ", " + signalStrength + ", " + periodHi + ", " + periodLo + ", " + numberDetection + ", " + coordinates[0] + ", " + coordinates[1] + ", " + gpsTimeStamp + ", " +
                                ((currentDateTime.get(Calendar.MONTH) + 1) + "/" + currentDateTime.get(Calendar.DAY_OF_MONTH) + "/" + (currentDateTime.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                        break;
                    case ValueCodes.SCAN_FIXED_CONSOLIDATED_NON_CODED_COMMAND:
                        signalStrength = Byte.toUnsignedInt(packet[index + 4]);
                        periodHi = Byte.toUnsignedInt(packet[index + 5]);
                        periodLo = Byte.toUnsignedInt(packet[index + 6]);
                        numberDetection = (Byte.toUnsignedInt(packet[index + 1]) * 256) + Byte.toUnsignedInt(packet[index + 7]);

                        text += (baseDateTime.get(Calendar.YEAR) - 2000) + ", " + baseDateTime.get(Calendar.DAY_OF_YEAR) + ", " + baseDateTime.get(Calendar.HOUR_OF_DAY) + ", " + baseDateTime.get(Calendar.MINUTE) +
                                ", " + baseDateTime.get(Calendar.SECOND) + ", " + (antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna) + ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) +
                                ", " + signalStrength + ", " + periodHi + ", " + periodLo + ", " + numberDetection + ", 0, 0, 0, " +
                                ((baseDateTime.get(Calendar.MONTH) + 1) + "/" + baseDateTime.get(Calendar.DAY_OF_MONTH) + "/" + (baseDateTime.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                        break;
                    case ValueCodes.SCAN_MANUAL_CODED_COMMAND:
                        frequency = baseFrequency + ((Byte.toUnsignedInt(packet[index + 1]) * 256) + Byte.toUnsignedInt(packet[index + 2]));
                        signalStrength = Byte.toUnsignedInt(packet[index + 3]);
                        code = Byte.toUnsignedInt(packet[index + 4]);
                        mort = Byte.toUnsignedInt(packet[index + 5]);

                        if (index + 8 < packet.length && packet[index + 8] == ValueCodes.SCAN_GPS_COMMAND) { // Gps
                            byte[] gpsData = new byte[16];
                            System.arraycopy(packet, index + 8, gpsData, 0, 16);
                            coordinates = Converters.getGpsData(gpsData);

                            int year = Byte.toUnsignedInt(gpsData[1]);
                            MM = Byte.toUnsignedInt(gpsData[2]);
                            DD = Byte.toUnsignedInt(gpsData[3]);
                            hh = Byte.toUnsignedInt(gpsData[9]);
                            mm = Byte.toUnsignedInt(gpsData[10]);
                            ss = Byte.toUnsignedInt(gpsData[11]);
                            gpsTimeStamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                            index += 16;
                            byteIndex += 16;
                        }

                        text += (baseDateTime.get(Calendar.YEAR) - 2000) + ", " + baseDateTime.get(Calendar.DAY_OF_YEAR) + ", " + baseDateTime.get(Calendar.HOUR_OF_DAY) +
                                ", " + baseDateTime.get(Calendar.MINUTE) + ", " + baseDateTime.get(Calendar.SECOND) + ", 0, 0, " + Converters.getFrequency(frequency) +
                                ", " + signalStrength + ", " + code + ", " + mort + ", 0, " + coordinates[0] + ", " + coordinates[1] + ", " + gpsTimeStamp + ", " +
                                ((baseDateTime.get(Calendar.MONTH) + 1) + "/" + baseDateTime.get(Calendar.DAY_OF_MONTH) + "/" + (baseDateTime.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                        break;
                    case ValueCodes.SCAN_MANUAL_NON_CODED_COMMAND:
                        frequency = baseFrequency + ((Byte.toUnsignedInt(packet[index + 1]) * 256) +
                                Byte.toUnsignedInt(packet[index + 2]));
                        signalStrength = Byte.toUnsignedInt(packet[index + 3]);
                        periodHi = Byte.toUnsignedInt(packet[index + 4]);
                        periodLo = Byte.toUnsignedInt(packet[index + 5]);

                        if (index + 8 < packet.length && packet[index + 8] == ValueCodes.SCAN_GPS_COMMAND) {
                            byte[] gpsData = new byte[16];
                            System.arraycopy(packet, index + 8, gpsData, 0, 16);
                            coordinates = Converters.getGpsData(gpsData);

                            int year = Byte.toUnsignedInt(gpsData[1]);
                            MM = Byte.toUnsignedInt(gpsData[2]);
                            DD = Byte.toUnsignedInt(gpsData[3]);
                            hh = Byte.toUnsignedInt(gpsData[9]);
                            mm = Byte.toUnsignedInt(gpsData[10]);
                            ss = Byte.toUnsignedInt(gpsData[11]);
                            gpsTimeStamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                            index += 16;
                            byteIndex += 16;
                        }

                        text += (baseDateTime.get(Calendar.YEAR) - 2000) + ", " + baseDateTime.get(Calendar.DAY_OF_YEAR) + ", " + baseDateTime.get(Calendar.HOUR_OF_DAY) +
                                ", " + baseDateTime.get(Calendar.MINUTE) + ", " + baseDateTime.get(Calendar.SECOND) + ", 0, 0, " + Converters.getFrequency(frequency) +
                                ", " + signalStrength + ", " + periodHi + ", " + periodLo + ", 0, " + coordinates[0] + ", " + coordinates[1] + ", " + gpsTimeStamp + ", " +
                                ((baseDateTime.get(Calendar.MONTH) + 1) + "/" + baseDateTime.get(Calendar.DAY_OF_MONTH) + "/" + (baseDateTime.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                        break;
                    case ValueCodes.SCAN_STOP_COMMAND:
                        int scanSession = (Byte.toUnsignedInt(packet[index + 1]) * 65536) + (Byte.toUnsignedInt(packet[index + 2]) * 256) + Byte.toUnsignedInt(packet[index + 3]);
                        text += "[Footer]" + ValueCodes.CR + ValueCodes.LF;
                        text += "Session Num: " + scanSession + ValueCodes.CR + ValueCodes.LF;
                        date = Byte.toUnsignedInt(packet[index + 12]) << 16 | Byte.toUnsignedInt(packet[index + 13]) << 8 | Byte.toUnsignedInt(packet[index + 14]);
                        mm = date % 100;
                        hh = (date / 100) % 100;
                        DD = (date / 10000) % 100;
                        MM = date / 1000000;
                        ss = Byte.toUnsignedInt(packet[index + 15]);
                        text += "Time Stamp: " + MM + "/" + DD + "/" + YY + " " + hh + ":" + mm + ":" + ss + ValueCodes.CR + ValueCodes.LF;
                        if (scanSession == sessionNumber)
                            sessionNumber++;
                        index += 8;
                        byteIndex += 8;
                        break;
                }
                index += 8;
                byteIndex += 8;
            }
        }
        return text;
    }

    public static String getTagsData(ArrayList<TagDetail> tags) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String text = "Tag Type, Tag ID, Date/Time, RSSI, Latitude, Longitude, Tag Version, Vcc (mv), Temp (C)" + ValueCodes.CR + ValueCodes.LF;
        for (TagDetail tag : tags)
            text += "Bluetooth, " + tag.code + ", " + simpleDateFormat.format(new Date(tag.lastTimestamp)) + ", " + tag.rssi + ", " + tag.latitude + ", " + tag.longitude + ", 1.0, " + tag.voltage + ", " + tag.temperature + ValueCodes.CR + ValueCodes.LF;
        return text;
    }

    /**
     * Creates a file with the downloaded data.
     */
    public static boolean printDataFiles(File root, ArrayList<Data> dataList) {
        int i = 0;
        boolean outcome;
        FileOutputStream stream;
        File newFile;
        try {
            if (!root.exists()) {
                outcome = root.mkdirs();
                if (!outcome)
                    throw new Exception("Folder 'atstrack' can't be created on root: " + root.getPath());
                root.setReadable(true);
                root.setWritable(true);
            }
            while(i < dataList.size()) {
                String fileName = dataList.get(i).fileName;
                newFile = new File(root.getAbsolutePath(), fileName);
                int copy = 1; //see if there's a possible copy
                while (!(newFile.createNewFile())) {
                    newFile = new File(root.getAbsolutePath(), fileName.substring(0, fileName.length() - 4) + " (" + copy + ").txt");
                    copy++;
                }
                newFile.setReadable(true);
                newFile.setWritable(true);
                stream = new FileOutputStream(newFile); //write in the file created
                for (byte[] data : dataList.get(i).packets)
                    stream.write(data);
                stream.flush(); //save the file
                stream.close();
                i++;
                Log.i("CONVERTERS", "FINISH CREATE FILE " + newFile.getAbsolutePath() + " " + i);
            }
            return i == dataList.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
