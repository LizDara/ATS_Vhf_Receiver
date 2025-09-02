package com.atstrack.ats.ats_vhf_receiver.Utils;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;

import android.util.Log;

import androidx.annotation.NonNull;

import com.atstrack.ats.ats_vhf_receiver.R;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;

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

    public static String getDecimalValue(byte[] value) { // Gets value in decimal system
        if (value == null)
            return "";

        String result = "";
        for (byte b : value)
            result += ((int) b & 0xff) + " ";
        return result;
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

    public static int hexToDecimal(String input) {
        int total = 0;
        int pot;
        int multiple = 0;
        int z;

        String[] hexadecimal = input.split("");

        for (int x = hexadecimal.length - 1; x >= 0; x--) {
            z = (hexadecimal.length - x - 1);
            pot = 1;
            for (int y = 0; y < z; y++) {
                pot *= 16;
            }

            String letter = hexadecimal[x];
            switch (letter) {
                case "0":
                    multiple = 0;
                    break;
                case "1":
                    multiple = 1;
                    break;
                case "2":
                    multiple = 2;
                    break;
                case "3":
                    multiple = 3;
                    break;
                case "4":
                    multiple = 4;
                    break;
                case "5":
                    multiple = 5;
                    break;
                case "6":
                    multiple = 6;
                    break;
                case "7":
                    multiple = 7;
                    break;
                case "8":
                    multiple = 8;
                    break;
                case "9":
                    multiple = 9;
                    break;
                case "A":
                    multiple = 10;
                    break;
                case "B":
                    multiple = 11;
                    break;
                case "C":
                    multiple = 12;
                    break;
                case "D":
                    multiple = 13;
                    break;
                case "E":
                    multiple = 14;
                    break;
                case "F":
                    multiple = 15;
                    break;
            }
            total += (pot * multiple);
        }
        return total;
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
        if (type.equals("C"))
            return " Coded,";
        if (type.equals("F"))
            return " Fixed PR,";
        if (type.equals("V"))
            return " Variable PR,";
        return "None";
    }

    /**
     * Gets the percentage of the device's battery.
     * @param scanRecord The content of the advertisement record offered by the remote device.
     * @return Return the battery percentage.
     */
    public static int getPercentBatteryVhfReceiver(byte[] scanRecord) {
        int firstElement = Integer.parseInt(Converters.getDecimalValue(scanRecord[0]));
        return Integer.parseInt(Converters.getDecimalValue(scanRecord[firstElement + 5]));
    }

    public static int getDeviceType(String name, boolean isLogo) {
        if (name.contains("vr") || name.contains("VHF")) {
            return isLogo ? R.drawable.vhf_receiver : R.drawable.ic_vhf_receiver;
        } else if (name.contains("ar") || name.contains("Acoustic"))
            return isLogo ? R.drawable.acoustic_receiver : R.drawable.ic_acoustic_receiver;
        else if (name.contains("wl") || name.contains("Wildlink"))
            return isLogo ? R.drawable.wildlink_receiver : R.drawable.ic_wildlink_receiver;
        else if (name.contains("Tags"))
            return isLogo ? R.drawable.bluetooth_tag : R.drawable.ic_bluetooth_tag;
        else if (name.contains("br") || name.contains("Bluetooth Receiver"))
            return isLogo ? R.drawable.bluetooth_receiver : R.drawable.ic_bluetooth_tag;
        else if (name.contains("bt") || name.contains("Beacon"))
            return isLogo ? R.drawable.beacon_tags : R.drawable.ic_bluetooth_tag;
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
        float A;
        float B1;
        float B2;
        float C;
        float D;
        byte sign;
        int degrees;
        int minutes;
        float latitude;
        float longitude;

        //Latitude, byte 4 to 7
        A = (float) (data[4] & 0x7F);
        sign = (byte) (data[4] & 0x80);
        B1 = (float) (data[5] & 0x80);
        B2 = (float) (data[5] & 0x7F);
        C = (float) Integer.parseInt(Converters.getDecimalValue(data[6]));
        D = (float) Integer.parseInt(Converters.getDecimalValue(data[7]));
        if (Converters.getHexValue(data[4]).equals("FF") && Converters.getHexValue(data[5]).equals("FF")
                && Converters.getHexValue(data[6]).equals("FF") && Converters.getHexValue(data[7]).equals("FF"))
            latitude = 0;
        else
            latitude = (float)((1 + ((B2 + ((C + (D / 256)) / 256)) / 128)) * (pow(2, (A * 2) + (B1 / 128) - 127)));
        latitude = latitude / 1000000;
        degrees = (int) floor(abs(latitude));
        latitude = (latitude - degrees) * 100 / 60;
        latitude = latitude + degrees;
        if ((latitude * 1000000) == 0) {
            coordinates[0] = "0";
        } else {
            if (sign == (byte) 0x80)
                coordinates[0] = "-";
            else
                coordinates[0] = "+";
        }
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
        C = (float) Integer.parseInt(Converters.getDecimalValue(data[14]));
        D = (float) Integer.parseInt(Converters.getDecimalValue(data[15]));
        if (Converters.getHexValue(data[12]).equals("FF") && Converters.getHexValue(data[13]).equals("FF")
                && Converters.getHexValue(data[14]).equals("FF") && Converters.getHexValue(data[15]).equals("FF"))
            longitude = 0;
        else
            longitude = (float)((1 + ((B2 + ((C + (D / 256)) / 256)) / 128)) * (pow(2, (A * 2) + (B1 / 128) - 127)));
        longitude = longitude / 1000000;
        degrees = (int) floor(abs(longitude));
        longitude = (longitude - degrees) * 100 / 60;
        longitude = longitude + degrees;
        if ((longitude * 1000000) == 0) {
            coordinates[1] = "0";
        } else {
            if (sign == (byte) 0x80)
                coordinates[1] = "-";
            else
                coordinates[1] = "+";
        }
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
     * Processes the data when the download is complete.
     * @param packet The raw data.
     * @return Returns the processed data.
     */
    public static synchronized String getPackageProcessed(@NonNull byte[] packet, int baseFrequency) {
        String data = "";
        int index = 0;
        int frequency = 0;
        int frequencyTableIndex = 0;
        int YY;
        int MM;
        int DD;
        int hh;
        int mm;
        int ss;
        int antenna = 0;
        int sessionNumber = 1;
        Calendar calendar = Calendar.getInstance();

        while (index < packet.length) {
            String format = Converters.getHexValue(packet[index]);
            if (format.equals("83") || format.equals("82")) { //Mobile and Stationary Scan
                byte detectionType;
                int matches;
                YY = Integer.parseInt(Converters.getDecimalValue(packet[index + 6]));
                data += "[Header]" + ValueCodes.CR + ValueCodes.LF;
                if (format.equals("83")) { // Stationary
                    data += "Scan Type: Stationary" + ValueCodes.CR + ValueCodes.LF;
                    data += "Scan Interval (seconds): " + Converters.getDecimalValue(packet[index + 3]) + ValueCodes.CR + ValueCodes.LF;
                    data += "Scan Timeout (seconds): " + Converters.getDecimalValue(packet[index + 4]) + ValueCodes.CR + ValueCodes.LF;
                    data += "Num of Antennas: " + (Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) + 1) + ValueCodes.CR + ValueCodes.LF;
                    data += "Store Interval (minutes): " + (Converters.getDecimalValue(packet[index + 5]).equals("0") ? "Continuous" : Converters.getDecimalValue(packet[index + 5])) + ValueCodes.CR + ValueCodes.LF;
                    int referenceFrequency = (Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])) + baseFrequency;
                    data += "Reference Frequency: " + (referenceFrequency == baseFrequency ? "No" : Converters.getFrequency(referenceFrequency)) + ValueCodes.CR + ValueCodes.LF;
                    data += "Reference Frequency Store Interval (minutes): " + Converters.getDecimalValue(packet[index + 11]) + ValueCodes.CR + ValueCodes.LF;
                    detectionType = (byte) (packet[index + 2] & (byte) 0x0F);
                    String detection = "Coded";
                    if (Converters.getHexValue(detectionType).equals("08"))
                        detection = "Non Coded Fixed Pulse Rate";
                    else if (Converters.getHexValue(detectionType).equals("07"))
                        detection = "Non Coded Variable Pulse Rate";
                    else if (Converters.getHexValue(detectionType).equals("06"))
                        detection = "Non Coded Variable Pulse Rate";
                    matches = Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) / 16;
                    data += "Transmitter Detection Type: " + detection + ValueCodes.CR + ValueCodes.LF;
                    String details = "";
                    if (Converters.getHexValue(detectionType).equals("08"))
                        details = matches + " matches required";
                    else if (Converters.getHexValue(detectionType).equals("07"))
                        details = matches + " matches required, " + Converters.getDecimalValue(packet[index + 7]) + " to " + Converters.getDecimalValue(packet[index + 10]) + " pulse rate range";
                    data += "Transmitter Detection Details: " + details + ValueCodes.CR + ValueCodes.LF;
                    index += 24;
                } else { // Mobile
                    data += "Scan Type: Mobile" + ValueCodes.CR + ValueCodes.LF;
                    data += "Scan Interval (seconds): " + (Integer.parseInt(Converters.getDecimalValue(packet[index + 3])) * 0.1) + ValueCodes.CR + ValueCodes.LF;
                    detectionType = (byte) (packet[index + 4] & (byte) 0x0F);
                    String detection = "Coded";
                    if (Converters.getHexValue(detectionType).equals("08"))
                        detection = "Non Coded Fixed Pulse Rate";
                    else if (Converters.getHexValue(detectionType).equals("07"))
                        detection = "Non Coded Variable Pulse Rate";
                    else if (Converters.getHexValue(detectionType).equals("06"))
                        detection = "Non Coded Variable Pulse Rate";
                    matches = Integer.parseInt(Converters.getDecimalValue(packet[index + 4])) / 16;
                    data += "Transmitter Detection Type: " + detection + ValueCodes.CR + ValueCodes.LF;
                    String details = "";
                    if (Converters.getHexValue(detectionType).equals("08"))
                        details = matches + " matches required";
                    else if (Converters.getHexValue(detectionType).equals("07"))
                        details = matches + " matches required, " + Converters.getDecimalValue(packet[index + 7]) + " to " + Converters.getDecimalValue(packet[index + 10]) + " pulse rate range";
                    data += "Transmitter Detection Details: " + details + ValueCodes.CR + ValueCodes.LF;
                    int gps = Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) >> 7 & 1;
                    data += "Gps: " + (gps == 1 ? "On" : "Off") + ValueCodes.CR + ValueCodes.LF;
                    index += 16;
                }
                data += "[Data]" + ValueCodes.CR + ValueCodes.LF;
                if (Converters.getHexValue(detectionType).equals("09"))
                    data += "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" + ValueCodes.CR + ValueCodes.LF;
                else
                    data += "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, PeriodHi, PeriodLo, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" + ValueCodes.CR + ValueCodes.LF;
                while (index < packet.length && !Converters.getHexValue(packet[index]).equals("83") && !Converters.getHexValue(packet[index]).equals("82") && !Converters.getHexValue(packet[index]).equals("86")) {
                    if (Converters.getHexValue(packet[index]).equals("F0")) { //Header
                        frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 256) +
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 2])));
                        frequencyTableIndex = (((Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) >> 6) & 1) * 256) + Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                        if (format.equals("83")) {
                            antenna = Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) >> 7;
                            if (antenna == 0)
                                antenna = (Integer.parseInt(Converters.getDecimalValue(packet[index + 7])) >> 6) + 1;
                        }
                        int date = Converters.hexToDecimal(
                                Converters.getHexValue(packet[index + 4]) + Converters.getHexValue(packet[index + 5]) + Converters.getHexValue(packet[index + 6]));
                        MM = date / 1000000;
                        date = date % 1000000;
                        DD = date / 10000;
                        date = date % 10000;
                        hh = date / 100;
                        mm = date % 100;
                        ss = Integer.parseInt(Converters.getDecimalValue((byte) (packet[index + 7] & (byte) 0x3F)));
                        calendar.set(YY + 2000, MM - 1, DD, hh, mm, ss);
                    } else if (Converters.getHexValue(packet[index]).equals("F1")) {
                        int secondsOffset = Integer.parseInt(Converters.getDecimalValue(packet[index + 1]));
                        int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                        int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                        int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                        int numberDetection = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                        calendar.add(Calendar.SECOND, secondsOffset);
                        String[] coordinates = new String[] {"0", "0"};
                        String gpsTimeStamp = "0";

                        if (Converters.getHexValue(packet[index + 8]).equals("A1")) {
                            byte[] gpsData = new byte[16];
                            System.arraycopy(packet, index + 8, gpsData, 0, 16);
                            coordinates = Converters.getGpsData(gpsData);

                            int year = Integer.parseInt(Converters.getDecimalValue(gpsData[1]));
                            MM = Integer.parseInt(Converters.getDecimalValue(gpsData[2]));
                            DD = Integer.parseInt(Converters.getDecimalValue(gpsData[3]));
                            hh = Integer.parseInt(Converters.getDecimalValue(gpsData[9]));
                            mm = Integer.parseInt(Converters.getDecimalValue(gpsData[10]));
                            ss = Integer.parseInt(Converters.getDecimalValue(gpsData[11]));
                            gpsTimeStamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss + ValueCodes.CR + ValueCodes.LF;
                            index+=16;
                        }

                        data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) + ", " + calendar.get(Calendar.MINUTE) +
                                ", " + calendar.get(Calendar.SECOND) + ", " + (antenna == 0 && format.equals("83") ? "All" : antenna) + ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) +
                                ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection + ", " + coordinates[0] + ", " + coordinates[1] + ", " + gpsTimeStamp + ", " +
                                ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;

                    } else if (Converters.getHexValue(packet[index]).equals("F2")) {
                        int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                        int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                        int mort = (Integer.parseInt(Converters.getDecimalValue(packet[index + 6])) * 256) +
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                        int numberDetection = (Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 256) +
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));

                        data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) + ", " +
                                calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + (antenna == 0 && format.equals("83") ? "All" : antenna) +
                                ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection +
                                ", 0, 0, 0, " + ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;

                    } else if (Converters.getHexValue(packet[index]).equals("E1") || Converters.getHexValue(packet[index]).equals("E2")) {
                        int secondsOffset = Integer.parseInt(Converters.getDecimalValue(packet[index + 1]));
                        int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                        calendar.add(Calendar.SECOND, secondsOffset);
                        String[] coordinates = new String[] {"0", "0"};
                        String gpsTimeStamp = "0";

                        if (Converters.getHexValue(packet[index + 8]).equals("A1")) {
                            byte[] gpsData = new byte[16];
                            System.arraycopy(packet, index + 8, gpsData, 0, 16);
                            coordinates = Converters.getGpsData(gpsData);

                            int year = Integer.parseInt(Converters.getDecimalValue(gpsData[1]));
                            MM = Integer.parseInt(Converters.getDecimalValue(gpsData[2]));
                            DD = Integer.parseInt(Converters.getDecimalValue(gpsData[3]));
                            hh = Integer.parseInt(Converters.getDecimalValue(gpsData[9]));
                            mm = Integer.parseInt(Converters.getDecimalValue(gpsData[10]));
                            ss = Integer.parseInt(Converters.getDecimalValue(gpsData[11]));
                            gpsTimeStamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss + ValueCodes.CR + ValueCodes.LF;
                            index+=16;
                        }

                        data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) + ", " + calendar.get(Calendar.MINUTE) +
                                ", " + calendar.get(Calendar.SECOND) + ", " + (antenna == 0 && format.equals("83") ? "All" : antenna) + ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) +
                                ", " + signalStrength + ", 0, 0, 0, " + coordinates[0] + ", " + coordinates[1] + ", " + gpsTimeStamp + ", " +
                                ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                    } else if (Converters.getHexValue(packet[index]).equals("87")) { // End Scan
                        int scanSession = (Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 65536) + (Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) * 256) +
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                        data += "[Footer]" + ValueCodes.CR + ValueCodes.LF;
                        data += "Session Num: " + scanSession + ValueCodes.CR + ValueCodes.LF;
                        int date = Converters.hexToDecimal(
                                Converters.getHexValue(packet[index + 12]) + Converters.getHexValue(packet[index + 13]) + Converters.getHexValue(packet[index + 14]));
                        MM = date / 1000000;
                        date = date % 1000000;
                        DD = date / 10000;
                        date = date % 10000;
                        hh = date / 100;
                        mm = date % 100;
                        ss = Integer.parseInt(Converters.getDecimalValue(packet[index + 15]));
                        data += "Time Stamp: " + MM + "/" + DD + "/" + YY + " " + hh + ":" + mm + ":" + ss + ValueCodes.CR + ValueCodes.LF;
                        if (scanSession == sessionNumber)
                            sessionNumber++;
                        index += 8;
                    }
                    index += 8;
                }
            } else if (format.equals("86")) { //Manual Scan
                data += "[Header]" + ValueCodes.CR + ValueCodes.LF;
                data += "Scan Type: Manual" + ValueCodes.CR + ValueCodes.LF;
                byte detectionType = (packet[index + 1] > (byte) 0x80) ? (byte) (packet[index + 1] - (byte) 0x80) : packet[index + 1];
                String detection = "Coded";
                if (Converters.getHexValue(detectionType).equals("08"))
                    detection = "Non Coded Fixed Pulse Rate";
                else if (Converters.getHexValue(detectionType).equals("07"))
                    detection = "Non Coded Variable Pulse Rate";
                data += "Transmitter Detection Type: " + detection + ValueCodes.CR + ValueCodes.LF;
                data += "Transmitter Detection Details: " + ValueCodes.CR + ValueCodes.LF;
                data += "[Data]" + ValueCodes.CR + ValueCodes.LF;
                YY = Integer.parseInt(Converters.getDecimalValue(packet[index + 2]));
                MM = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                DD = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                hh = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                mm = Integer.parseInt(Converters.getDecimalValue(packet[index + 6]));
                ss = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                calendar.set(YY + 2000, MM - 1, DD, hh, mm, ss);

                if (Converters.getHexValue(packet[index + 8]).equals("D0")) { //Coded
                    data += "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" + ValueCodes.CR + ValueCodes.LF;
                    frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 11]));
                    int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 12]));
                    int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 13]));
                    String[] coordinates = new String[] {"0", "0"};
                    String gpsTimeStamp = "0";

                    if (Converters.getHexValue(packet[index + 8]).equals("A1")) {
                        byte[] gpsData = new byte[16];
                        System.arraycopy(packet, index + 8, gpsData, 0, 16);
                        coordinates = Converters.getGpsData(gpsData);

                        int year = Integer.parseInt(Converters.getDecimalValue(gpsData[1]));
                        MM = Integer.parseInt(Converters.getDecimalValue(gpsData[2]));
                        DD = Integer.parseInt(Converters.getDecimalValue(gpsData[3]));
                        hh = Integer.parseInt(Converters.getDecimalValue(gpsData[9]));
                        mm = Integer.parseInt(Converters.getDecimalValue(gpsData[10]));
                        ss = Integer.parseInt(Converters.getDecimalValue(gpsData[11]));
                        gpsTimeStamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss + ValueCodes.CR + ValueCodes.LF;
                        index+=16;
                    }

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", 0, " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) +
                            ", " + signalStrength + ", " + code + ", " + mort + ", 0, " + coordinates[0] + ", " + coordinates[1] + ", " + gpsTimeStamp + ", " +
                            ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                } else if (Converters.getHexValue(packet[index + 8]).equals("E0")) { //Non Coded
                    data += "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, PeriodHi, PeriodLo, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" + ValueCodes.CR + ValueCodes.LF;
                    frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 11]));
                    String[] coordinates = new String[] {"0", "0"};
                    String gpsTimeStamp = "0";

                    if (Converters.getHexValue(packet[index + 8]).equals("A1")) {
                        byte[] gpsData = new byte[16];
                        System.arraycopy(packet, index + 8, gpsData, 0, 16);
                        coordinates = Converters.getGpsData(gpsData);

                        int year = Integer.parseInt(Converters.getDecimalValue(gpsData[1]));
                        MM = Integer.parseInt(Converters.getDecimalValue(gpsData[2]));
                        DD = Integer.parseInt(Converters.getDecimalValue(gpsData[3]));
                        hh = Integer.parseInt(Converters.getDecimalValue(gpsData[9]));
                        mm = Integer.parseInt(Converters.getDecimalValue(gpsData[10]));
                        ss = Integer.parseInt(Converters.getDecimalValue(gpsData[11]));
                        gpsTimeStamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss + ValueCodes.CR + ValueCodes.LF;
                        index+=16;
                    }

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", 0, " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) +
                            ", " + signalStrength + ", 0, 0, 0, " + coordinates[0] + ", " + coordinates[1] + ", " + gpsTimeStamp + ", " +
                            ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                }
                index += 16;
                if (Converters.getHexValue(packet[index]).equals("87")) {
                    int scanSession = (Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 65536) + (Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                    data += "[Footer]" + ValueCodes.CR + ValueCodes.LF;
                    data += "Session Num: " + scanSession + ValueCodes.CR + ValueCodes.LF;
                    int date = Converters.hexToDecimal(
                            Converters.getHexValue(packet[index + 12]) + Converters.getHexValue(packet[index + 13]) + Converters.getHexValue(packet[index + 14]));
                    MM = date / 1000000;
                    date = date % 1000000;
                    DD = date / 10000;
                    date = date % 10000;
                    hh = date / 100;
                    mm = date % 100;
                    ss = Integer.parseInt(Converters.getDecimalValue(packet[index + 15]));
                    data += "Time Stamp: " + MM + "/" + DD + " " + hh + ":" + mm + ":" + ss + ValueCodes.CR + ValueCodes.LF;
                    if (scanSession == sessionNumber)
                        sessionNumber++;
                    index += 16;
                }
            } else {
                index += 8;
            }
        }
        return data;
    }

    /**
     * Creates a file with the downloaded data.
     */
    public static boolean printSnapshotFiles(File root, ArrayList<Snapshots> snapshotArray) {
        int i = 0;
        boolean outcome;
        FileOutputStream stream;
        File newFile;
        try {
            if (!root.exists()) {
                outcome = root.mkdirs();
                if (!outcome)
                    throw new Exception("Folder 'atstrack' can't be created.");
                root.setReadable(true);
                root.setWritable(true);
            }
            while(i < snapshotArray.size()) {
                String fileName = snapshotArray.get(i).getFileName();
                newFile = new File(root.getAbsolutePath(), fileName);
                Log.i("CONVERTERS", "New File Root: " + newFile.getAbsolutePath() + ", i: " + i);
                int copy = 1; //see if there's a possible copy
                while (!(newFile.createNewFile())) {
                    newFile = new File(root.getAbsolutePath(), fileName.substring(0, fileName.length() - 4) + " (" + copy + ").txt");
                    copy++;
                }
                newFile.setReadable(true);
                newFile.setWritable(true);
                stream = new FileOutputStream(newFile); //write in the file created
                stream.write(snapshotArray.get(i).getSnapshot());
                stream.flush(); //save the file
                stream.close();
                i++;
            }
            return i == snapshotArray.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
