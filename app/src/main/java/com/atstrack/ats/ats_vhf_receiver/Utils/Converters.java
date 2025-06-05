package com.atstrack.ats.ats_vhf_receiver.Utils;

import android.util.Log;

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
     * @param scanRecord The content of the advertisement record offered by the remote device.
     * @return Return the device status.
     */
    public static String getStatusVhfReceiver(byte[] scanRecord) {
        int firstElement = Integer.parseInt(Converters.getDecimalValue(scanRecord[0]));
        String status = Converters.getHexValue(scanRecord[firstElement + 6]);
        switch (status) {
            case "00":
                status = " Not scanning";
                break;
            case "82":
            case "81":
            case "80":
                status = " Scanning, mobile";
                break;
            case "83":
                status = " Scanning, stationary";
                break;
            case "86":
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

    /**
     * Processes the data when the download is complete.
     * @param packet The raw data.
     * @return Returns the processed data.
     */
    public static synchronized String readPacket(byte[] packet, int baseFrequency) {
        String data = "";
        int index = 0;
        int frequency = 0;
        int frequencyTableIndex = 0;
        int year;
        int antenna = 0;
        int sessionNumber = 1;
        Calendar calendar = Calendar.getInstance();

        while (index < packet.length) {
            String format = Converters.getHexValue(packet[index]);
            if (format.equals("83") || format.equals("82")) { //Mobile and Stationary Scan
                byte detectionType;
                int matches;
                year = Integer.parseInt(Converters.getDecimalValue(packet[index + 6]));
                data += "[Header]" + ValueCodes.CR + ValueCodes.LF;
                if (format.equals("83")) {
                    data += "Scan Type: Stationary" + ValueCodes.CR + ValueCodes.LF;
                    data += "Scan Interval (seconds): " + Converters.getDecimalValue(packet[index + 3]) + ValueCodes.CR + ValueCodes.LF;
                    data += "Scan Timeout (seconds): " + Converters.getDecimalValue(packet[index + 4]) + ValueCodes.CR + ValueCodes.LF;
                    data += "Num of Antennas: " + Converters.getDecimalValue(packet[index + 1]) + ValueCodes.CR + ValueCodes.LF;
                    data += "Store Interval (minutes): " + Converters.getDecimalValue(packet[index + 5]) + ValueCodes.CR + ValueCodes.LF;
                    int referenceFrequency = (Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])) + baseFrequency;
                    data += "Reference Frequency: " + Converters.getFrequency(referenceFrequency) + ValueCodes.CR + ValueCodes.LF;
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
                } else {
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
                        int month = date / 1000000;
                        date = date % 1000000;
                        int day = date / 10000;
                        date = date % 10000;
                        int hour = date / 100;
                        int minute = date % 100;
                        int seconds = Integer.parseInt(Converters.getDecimalValue((byte) (packet[index + 7] & (byte) 0x3F)));
                        calendar.set(year + 2000, month - 1, day, hour, minute, seconds);
                    } else if (Converters.getHexValue(packet[index]).equals("F1")) {
                        int secondsOffset = Integer.parseInt(Converters.getDecimalValue(packet[index + 1]));
                        int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                        int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                        int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                        int numberDetection = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                        calendar.add(Calendar.SECOND, secondsOffset);

                        data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                                ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + (antenna == 0 && format.equals("83") ? "All" : antenna) +
                                ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection + ", 0, 0, 0, " +
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

                        data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                                ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + (antenna == 0 && format.equals("83") ? "All" : antenna) +
                                ", " + frequencyTableIndex + ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", 0, 0, 0, 0, 0, 0, " +
                                ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                    } else if (Converters.getHexValue(packet[index]).equals("87")) {
                        int scanSession = (Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 65536) + (Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) * 256) +
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                        data += "[Footer]" + ValueCodes.CR + ValueCodes.LF;
                        data += "Session Num: " + scanSession + ValueCodes.CR + ValueCodes.LF;
                        int date = Converters.hexToDecimal(
                                Converters.getHexValue(packet[index + 12]) + Converters.getHexValue(packet[index + 13]) + Converters.getHexValue(packet[index + 14]));
                        int month = date / 1000000;
                        date = date % 1000000;
                        int day = date / 10000;
                        date = date % 10000;
                        int hour = date / 100;
                        int minute = date % 100;
                        int seconds = Integer.parseInt(Converters.getDecimalValue(packet[index + 15]));
                        data += "Time Stamp: " + month + "/" + day + " " + hour + ":" + minute + ":" + seconds + ValueCodes.CR + ValueCodes.LF;
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
                year = Integer.parseInt(Converters.getDecimalValue(packet[index + 2]));
                int month = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                int day = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                int hour = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                int minute = Integer.parseInt(Converters.getDecimalValue(packet[index + 6]));
                int seconds = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                calendar.set(year + 2000, month - 1, day, hour, minute, seconds);

                if (Converters.getHexValue(packet[index + 8]).equals("D0")) { //Coded
                    data += "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" + ValueCodes.CR + ValueCodes.LF;
                    frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 11]));
                    int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 12]));
                    int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 13]));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", 0, " + frequencyTableIndex +
                            ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", " + code + ", " + mort + ", 0, 0, 0, 0, " +
                            ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                } else if (Converters.getHexValue(packet[index + 8]).equals("E0")) { //Non Coded
                    data += "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, PeriodHi, PeriodLo, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" + ValueCodes.CR + ValueCodes.LF;
                    frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 11]));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", 0, " + frequencyTableIndex +
                            ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", 0, 0, 0, 0, 0, 0, " + ((calendar.get(Calendar.MONTH) + 1) +
                            "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                }
                index += 16;
                if (Converters.getHexValue(packet[index]).equals("87")) {
                    int scanSession = (Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 65536) + (Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                    data += "[Footer]" + ValueCodes.CR + ValueCodes.LF;
                    data += "Session Num: " + scanSession + ValueCodes.CR + ValueCodes.LF;
                    int date = Converters.hexToDecimal(
                            Converters.getHexValue(packet[index + 12]) + Converters.getHexValue(packet[index + 13]) + Converters.getHexValue(packet[index + 14]));
                    month = date / 1000000;
                    date = date % 1000000;
                    day = date / 10000;
                    date = date % 10000;
                    hour = date / 100;
                    minute = date % 100;
                    seconds = Integer.parseInt(Converters.getDecimalValue(packet[index + 15]));
                    data += "Time Stamp: " + month + "/" + day + " " + hour + ":" + minute + ":" + seconds + ValueCodes.CR + ValueCodes.LF;
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
