package com.atstrack.ats.ats_vhf_receiver.Utils;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.pow;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.Models.Coordinates;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
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
import java.util.TimeZone;

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
        StringBuilder value = new StringBuilder();
        for (int i = start; i < end; i++)
            value.append((char) data[i]);
        return value.toString();
    }

    @SuppressLint("DefaultLocale")
    public static String getTimeString() {
        Calendar cal = Calendar.getInstance();
        return String.format("%02d:%02d:%02d.%03d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
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
            case ValueCodes.CODED:
                return "Coded";
            case ValueCodes.FIXED:
                return "Fixed Pulse Rate";
            case ValueCodes.VARIABLE:
            case ValueCodes.VARIABLE_TEMPERATURE:
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
        else if (name.contains("BT2400"))
            return isLogo ? R.drawable.ic_insect_icon : R.drawable.ic_bluetooth_tag;
        else if (name.contains(ValueCodes.BLUETOOTH_RECEIVER) || name.contains("BluTrack"))
            return isLogo ? R.drawable.ic_bluetooth_receiver : R.drawable.ic_bluetooth_tag;
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

    public static Coordinates getGpsData(byte[] data) {
        Coordinates coordinates = new Coordinates();
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
            coordinates.latitude = "0";
        else
            coordinates.latitude = sign == (byte) 0x80 ? "-" : "+";
        minutes = (int) ((latitude - degrees) * 1000000);
        /*if (minutes > 99999) {
            minutes -= 1000000;
            degrees++;
        }*/
        if (degrees < 10)
            coordinates.latitude += "0";
        coordinates.latitude += degrees + ".";
        if (minutes < 100000)
            coordinates.latitude += "0";
        if (minutes < 10000)
            coordinates.latitude += "0";
        if (minutes < 1000)
            coordinates.latitude += "0";
        if (minutes < 100)
            coordinates.latitude += "0";
        if (minutes < 10)
            coordinates.latitude += "0";
        coordinates.latitude += String.valueOf(minutes);

        //Longitude, byte 12 to 15
        A = (float) (data[12] & 0x7F);
        sign = (byte) (data[12] & 0x80);
        B1 = (float) (data[13] & 0x80);
        B2 = (float) (data[13] & 0x7F);
        C = (float) Byte.toUnsignedInt(data[14]);
        D = (float) Byte.toUnsignedInt(data[15]);
        if (data[12] == ValueCodes.NULL && data[13] == ValueCodes.NULL && data[14] == ValueCodes.NULL && data[15] == ValueCodes.NULL)
            longitude = 0;
        else
            longitude = (float)((1 + ((B2 + ((C + (D / 256)) / 256)) / 128)) * (pow(2, (A * 2) + (B1 / 128) - 127)));
        longitude = longitude / 1000000;
        degrees = (int) floor(abs(longitude));
        longitude = (longitude - degrees) * 100 / 60;
        longitude = longitude + degrees;
        if ((longitude * 1000000) == 0)
            coordinates.longitude = "0";
        else
            coordinates.longitude = sign == (byte) 0x80 ? "-" : "+";
        minutes = (int) ((longitude - degrees) * 1000000);
        /*if (minutes > 99999) {
            minutes -= 1000000;
            degrees++;
        }*/
        if (degrees < 100)
            coordinates.longitude += "0";
        if (degrees < 10)
            coordinates.longitude += "0";
        coordinates.longitude += degrees + ".";
        if (minutes < 100000)
            coordinates.longitude += "0";
        if (minutes < 10000)
            coordinates.longitude += "0";
        if (minutes < 1000)
            coordinates.longitude += "0";
        if (minutes < 100)
            coordinates.longitude += "0";
        if (minutes < 10)
            coordinates.longitude += "0";
        coordinates.longitude += String.valueOf(minutes);

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

    public static byte[] setCalendar(int length) {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        byte[] b = new byte[length];
        b[1] = (byte) (currentDate.get(Calendar.YEAR) % 100);
        b[2] = (byte) (currentDate.get(Calendar.MONTH) + 1);
        b[3] = (byte) currentDate.get(Calendar.DAY_OF_MONTH);
        b[4] = (byte) currentDate.get(Calendar.HOUR_OF_DAY);
        b[5] = (byte) currentDate.get(Calendar.MINUTE);
        b[6] = (byte) currentDate.get(Calendar.SECOND);
        return b;
    }

    /**
     * Processes the data when the download is complete.
     *
     * @param data The raw data.
     * @param process_percent Percent view.
     * @param baseActivity Base activity
     * @return Returns the processed data.
     */
    public static synchronized String[] getPackageProcessed(ArrayList<byte[]> data, View process_percent, BaseActivity baseActivity, boolean isRawFile) {
        StringBuilder text = new StringBuilder();
        StringBuilder metrics = new StringBuilder();
        byte scanType = 0;
        int baseFrequency = 0;
        int frequency = 0;
//        int frequencyTableIndex = 0;
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
                Coordinates coordinates = new Coordinates();
                switch (format) {
                    case ValueCodes.STATIONARY_SCAN_COMMAND:
                    case ValueCodes.MOBILE_SCAN_COMMAND:
                    case ValueCodes.MANUAL_SCAN_COMMAND:
                        int matches;
                        byte detectionType = 0;
                        YY = Byte.toUnsignedInt(packet[index + 6]);
                        switch (format) {
                            case ValueCodes.STATIONARY_SCAN_COMMAND: {
                                text.append("[Header]" + ValueCodes.CR + ValueCodes.LF);
                                metrics.append("[Header]" + ValueCodes.CR + ValueCodes.LF);
                                if (packet[index + 24] == ValueCodes.SERIAL_COMMAND) {
                                    int serial = Byte.toUnsignedInt(packet[index + 28]) << 16 | Byte.toUnsignedInt(packet[index + 29]) << 8 | Byte.toUnsignedInt(packet[index + 30]);
                                    text.append("Serial: ").append(serial).append(ValueCodes.CR).append(ValueCodes.LF);
                                    metrics.append("Serial: ").append(serial).append(ValueCodes.CR).append(ValueCodes.LF);
                                }
                                baseFrequency = Byte.toUnsignedInt(packet[index + 20]) * 1000;
                                text.append("Scan Type: Stationary" + ValueCodes.CR + ValueCodes.LF);
                                metrics.append("Scan Type: Stationary" + ValueCodes.CR + ValueCodes.LF);
                                text.append("Scan Interval (seconds): ").append(Byte.toUnsignedInt(packet[index + 3])).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Scan Interval (seconds): ").append(Byte.toUnsignedInt(packet[index + 3])).append(ValueCodes.CR).append(ValueCodes.LF);
                                text.append("Scan Timeout (seconds): ").append(Byte.toUnsignedInt(packet[index + 4])).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Scan Timeout (seconds): ").append(Byte.toUnsignedInt(packet[index + 4])).append(ValueCodes.CR).append(ValueCodes.LF);
                                text.append("Num of Antennas: ").append(Byte.toUnsignedInt(packet[index + 1]) + 1).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Num of Antennas: ").append(Byte.toUnsignedInt(packet[index + 1]) + 1).append(ValueCodes.CR).append(ValueCodes.LF);
                                text.append("Store Interval (minutes): ").append(packet[index + 5] == ValueCodes.NONE ? "Continuous" : String.valueOf(Byte.toUnsignedInt(packet[index + 5]))).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Store Interval (minutes): ").append(packet[index + 5] == ValueCodes.NONE ? "Continuous" : String.valueOf(Byte.toUnsignedInt(packet[index + 5]))).append(ValueCodes.CR).append(ValueCodes.LF);
                                int referenceFrequency = (Byte.toUnsignedInt(packet[index + 9]) * 256) + Byte.toUnsignedInt(packet[index + 10]) + baseFrequency;
                                text.append("Reference Frequency: ").append(referenceFrequency == baseFrequency ? "No" : Converters.getFrequency(referenceFrequency)).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Reference Frequency: ").append(referenceFrequency == baseFrequency ? "No" : Converters.getFrequency(referenceFrequency)).append(ValueCodes.CR).append(ValueCodes.LF);
                                text.append("Reference Frequency Store Interval (hours): ").append(referenceFrequency == baseFrequency ? "No" : String.valueOf(Byte.toUnsignedInt(packet[index + 11]))).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Reference Frequency Store Interval (hours): ").append(referenceFrequency == baseFrequency ? "No" : String.valueOf(Byte.toUnsignedInt(packet[index + 11]))).append(ValueCodes.CR).append(ValueCodes.LF);
                                detectionType = (byte) (packet[index + 2] & (byte) 0x0F);
                                matches = Byte.toUnsignedInt(packet[index + 2]) / 16;
                                String detection = "Coded";
                                String details = "";
                                if (detectionType == ValueCodes.FIXED) {
                                    detection = "Non Coded Fixed Pulse Rate";
                                    details = matches + " matches required";
                                } else if (detectionType == ValueCodes.VARIABLE) {
                                    detection = "Non Coded Variable Pulse Rate";
                                    details = matches + " matches required, " + Byte.toUnsignedInt(packet[index + 7]) + " to " + Byte.toUnsignedInt(packet[index + 10]) + " pulse rate range";
                                } else if (detectionType == ValueCodes.VARIABLE_TEMPERATURE)
                                    detection = "Non Coded Variable Pulse Rate";
                                text.append("Transmitter Detection Type: ").append(detection).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Transmitter Detection Type: ").append(detection).append(ValueCodes.CR).append(ValueCodes.LF);
                                text.append("Transmitter Detection Details: ").append(details).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Transmitter Detection Details: ").append(details).append(ValueCodes.CR).append(ValueCodes.LF);
                                index += 16;
                                byteIndex += 16;
                                break;
                            }
                            case ValueCodes.MOBILE_SCAN_COMMAND: {
                                text.append("[Header]" + ValueCodes.CR + ValueCodes.LF);
                                metrics.append("[Header]" + ValueCodes.CR + ValueCodes.LF);
                                if (packet[index + 16] == ValueCodes.SERIAL_COMMAND) {
                                    int serial = Byte.toUnsignedInt(packet[index + 20]) << 16 | Byte.toUnsignedInt(packet[index + 21]) << 8 | Byte.toUnsignedInt(packet[index + 22]);
                                    text.append("Serial: ").append(serial).append(ValueCodes.CR).append(ValueCodes.LF);
                                    metrics.append("Serial: ").append(serial).append(ValueCodes.CR).append(ValueCodes.LF);
                                }
                                baseFrequency = Byte.toUnsignedInt(packet[index + 5]) * 1000;
                                text.append("Scan Type: Mobile" + ValueCodes.CR + ValueCodes.LF);
                                metrics.append("Scan Type: Mobile" + ValueCodes.CR + ValueCodes.LF);
                                text.append("Scan Interval (seconds): ").append(Byte.toUnsignedInt(packet[index + 3]) * 0.1).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Scan Interval (seconds): ").append(Byte.toUnsignedInt(packet[index + 3]) * 0.1).append(ValueCodes.CR).append(ValueCodes.LF);
                                detectionType = (byte) (packet[index + 4] & (byte) 0x0F);
                                matches = Byte.toUnsignedInt(packet[index + 4]) / 16;
                                String detection = "Coded";
                                String details = "";
                                if (detectionType == ValueCodes.FIXED) {
                                    detection = "Non Coded Fixed Pulse Rate";
                                    details = matches + " matches required";
                                } else if (detectionType == ValueCodes.VARIABLE) {
                                    detection = "Non Coded Variable Pulse Rate";
                                    details = matches + " matches required, " + Byte.toUnsignedInt(packet[index + 7]) + " to " + Byte.toUnsignedInt(packet[index + 10]) + " pulse rate range";
                                } else if (detectionType == ValueCodes.VARIABLE_TEMPERATURE)
                                    detection = "Non Coded Variable Pulse Rate";
                                text.append("Transmitter Detection Type: ").append(detection).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Transmitter Detection Type: ").append(detection).append(ValueCodes.CR).append(ValueCodes.LF);
                                text.append("Transmitter Detection Details: ").append(details).append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Transmitter Detection Details: ").append(details).append(ValueCodes.CR).append(ValueCodes.LF);
                                int gps = Byte.toUnsignedInt(packet[index + 2]) >> 7 & 1;
                                text.append("Gps: ").append(gps == 1 ? "On" : "Off").append(ValueCodes.CR).append(ValueCodes.LF);
                                metrics.append("Gps: ").append(gps == 1 ? "On" : "Off").append(ValueCodes.CR).append(ValueCodes.LF);
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
                                int serial = Byte.toUnsignedInt(packet[index + 4]) << 16 | Byte.toUnsignedInt(packet[index + 5]) << 8 | Byte.toUnsignedInt(packet[index + 6]);
                                baseFrequency = Byte.toUnsignedInt(packet[index + 1]) * 1000;
                                frequency = baseFrequency + ((Byte.toUnsignedInt(packet[index + 2]) * 256) + Byte.toUnsignedInt(packet[index + 3]));
                                YY = Byte.toUnsignedInt(packet[index + 7]);
                                detectionType = packet[index + 8] == ValueCodes.SCAN_MANUAL_CODED_COMMAND ? ValueCodes.CODED : ValueCodes.NONE;
                                String detection = detectionType == ValueCodes.CODED ? "Coded" : "Non Coded";
                                if (scanType != ValueCodes.MANUAL_SCAN_COMMAND) {
                                    text.append("[Header]" + ValueCodes.CR + ValueCodes.LF);
                                    metrics.append("[Header]" + ValueCodes.CR + ValueCodes.LF);
                                    text.append("Serial: ").append(serial).append(ValueCodes.CR).append(ValueCodes.LF);
                                    metrics.append("Serial: ").append(serial).append(ValueCodes.CR).append(ValueCodes.LF);
                                    text.append("Scan Type: Manual" + ValueCodes.CR + ValueCodes.LF);
                                    metrics.append("Scan Type: Manual" + ValueCodes.CR + ValueCodes.LF);
                                    text.append("Transmitter Detection Type: ").append(detection).append(ValueCodes.CR).append(ValueCodes.LF);
                                    metrics.append("Transmitter Detection Type: ").append(detection).append(ValueCodes.CR).append(ValueCodes.LF);
                                    text.append("Transmitter Detection Details: " + ValueCodes.CR + ValueCodes.LF);
                                    metrics.append("Transmitter Detection Details: " + ValueCodes.CR + ValueCodes.LF);
                                }
                                break;
                            }
                        }
                        scanType = format;
                        text.append("[Data]" + ValueCodes.CR + ValueCodes.LF);
                        metrics.append("[Data]" + ValueCodes.CR + ValueCodes.LF);
                        text.append(detectionType == ValueCodes.CODED ?
                                "Year, JulianDay, Hour, Min, Sec, Ant, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" :
                                "Year, JulianDay, Hour, Min, Sec, Ant, Freq, SS, PeriodHi, PeriodLo, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum").append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append(detectionType == ValueCodes.CODED ?
                                "Year, JulianDay, Hour, Min, Sec, Ant, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum" :
                                "Year, JulianDay, Hour, Min, Sec, Ant, Freq, SS, PeriodHi, PeriodLo, NumDet, Lat, Long, GpsTimestamp, Date, SessionNum").append(ValueCodes.CR).append(ValueCodes.LF);
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
//                        frequencyTableIndex = (((Byte.toUnsignedInt(packet[index + 1]) >> 6) & 1) * 256) + Byte.toUnsignedInt(packet[index + 3]);
                        antenna = 0;
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
                            coordinates.timestamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                            index += 16;
                            byteIndex += 16;
                        }

                        text.append((currentDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(currentDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(currentDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(currentDateTime.get(Calendar.MINUTE)).append(", ").append(currentDateTime.get(Calendar.SECOND)).append(", ").append(antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna).append(", ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(code).append(", ").append(mort).append(", ").append(numberDetection).append(", ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", ").append((currentDateTime.get(Calendar.MONTH) + 1)).append("/").append(currentDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(currentDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append((currentDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(currentDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(currentDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(currentDateTime.get(Calendar.MINUTE)).append(", ").append(currentDateTime.get(Calendar.SECOND)).append(", ").append(antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna).append(", ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(code).append(", ").append(mort).append(", ").append(numberDetection).append(", ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", ").append((currentDateTime.get(Calendar.MONTH) + 1)).append("/").append(currentDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(currentDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        break;
                    case ValueCodes.SCAN_FIX_CONSOLIDATED_CODED_COMMAND:
                        signalStrength = Byte.toUnsignedInt(packet[index + 4]);
                        code = Byte.toUnsignedInt(packet[index + 3]);
                        mort = (Byte.toUnsignedInt(packet[index + 6]) * 256) + Byte.toUnsignedInt(packet[index + 5]);
                        numberDetection = (Byte.toUnsignedInt(packet[index + 1]) * 256) + Byte.toUnsignedInt(packet[index + 7]);

                        text.append((baseDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(baseDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(baseDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(baseDateTime.get(Calendar.MINUTE)).append(", ").append(baseDateTime.get(Calendar.SECOND)).append(", ").append(antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna).append(", ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(code).append(", ").append(mort).append(", ").append(numberDetection).append(", 0, 0, 0, ").append((baseDateTime.get(Calendar.MONTH) + 1)).append("/").append(baseDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(baseDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append((baseDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(baseDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(baseDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(baseDateTime.get(Calendar.MINUTE)).append(", ").append(baseDateTime.get(Calendar.SECOND)).append(", ").append(antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna).append(", ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(code).append(", ").append(mort).append(", ").append(numberDetection).append(", 0, 0, 0, ").append((baseDateTime.get(Calendar.MONTH) + 1)).append("/").append(baseDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(baseDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        break;
                    case ValueCodes.SCAN_DATA_FIXED_NON_CODED_COMMAND:
                    case ValueCodes.SCAN_DATA_VARIABLE_NON_CODED_COMMAND:
                        secondsOffset = Byte.toUnsignedInt(packet[index + 1]);
                        signalStrength = Byte.toUnsignedInt(packet[index + 4]);
                        periodHi = (Byte.toUnsignedInt(packet[index + 5]) << 8) | Byte.toUnsignedInt(packet[index + 6]);
                        periodLo = 0;
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
                            coordinates.timestamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                            index += 16;
                            byteIndex += 16;
                        }

                        text.append((currentDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(currentDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(currentDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(currentDateTime.get(Calendar.MINUTE)).append(", ").append(currentDateTime.get(Calendar.SECOND)).append(", ").append(antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna).append(", ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(periodHi).append(", ").append(periodLo).append(", ").append(numberDetection).append(", ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", ").append((currentDateTime.get(Calendar.MONTH) + 1)).append("/").append(currentDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(currentDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append((currentDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(currentDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(currentDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(currentDateTime.get(Calendar.MINUTE)).append(", ").append(currentDateTime.get(Calendar.SECOND)).append(", ").append(antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna).append(", ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(periodHi).append(", ").append(periodLo).append(", ").append(numberDetection).append(", ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", ").append((currentDateTime.get(Calendar.MONTH) + 1)).append("/").append(currentDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(currentDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        break;
                    case ValueCodes.SCAN_FIXED_CONSOLIDATED_NON_CODED_COMMAND:
                        signalStrength = Byte.toUnsignedInt(packet[index + 4]);
                        periodHi = (Byte.toUnsignedInt(packet[index + 5]) << 8) | Byte.toUnsignedInt(packet[index + 6]);
                        periodLo = 0;
                        numberDetection = (Byte.toUnsignedInt(packet[index + 1]) * 256) + Byte.toUnsignedInt(packet[index + 7]);

                        text.append((baseDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(baseDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(baseDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(baseDateTime.get(Calendar.MINUTE)).append(", ").append(baseDateTime.get(Calendar.SECOND)).append(", ").append(antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna).append(", ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(periodHi).append(", ").append(periodLo).append(", ").append(numberDetection).append(", 0, 0, 0, ").append((baseDateTime.get(Calendar.MONTH) + 1)).append("/").append(baseDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(baseDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append((baseDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(baseDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(baseDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(baseDateTime.get(Calendar.MINUTE)).append(", ").append(baseDateTime.get(Calendar.SECOND)).append(", ").append(antenna == 0 && scanType == ValueCodes.STATIONARY_SCAN_COMMAND ? "All" : antenna).append(", ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(periodHi).append(", ").append(periodLo).append(", ").append(numberDetection).append(", 0, 0, 0, ").append((baseDateTime.get(Calendar.MONTH) + 1)).append("/").append(baseDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(baseDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        break;
                    case ValueCodes.SCAN_MANUAL_CODED_COMMAND:
                        date = Byte.toUnsignedInt(packet[index + 2]) << 16 | Byte.toUnsignedInt(packet[index + 3]) << 8 | Byte.toUnsignedInt(packet[index + 4]);
                        mm = date % 100;
                        hh = (date / 100) % 100;
                        DD = (date / 10000) % 100;
                        MM = date / 1000000;
                        ss = Byte.toUnsignedInt(packet[index + 1]);
                        baseDateTime.set(YY + 2000, MM - 1, DD, hh, mm, ss);
                        signalStrength = Byte.toUnsignedInt(packet[index + 5]);
                        code = Byte.toUnsignedInt(packet[index + 6]);
                        mort = Byte.toUnsignedInt(packet[index + 7]);

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
                            coordinates.timestamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                            index += 16;
                            byteIndex += 16;
                        }

                        text.append((baseDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(baseDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(baseDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(baseDateTime.get(Calendar.MINUTE)).append(", ").append(baseDateTime.get(Calendar.SECOND)).append(", 0, ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(code).append(", ").append(mort).append(", 0, ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", ").append((baseDateTime.get(Calendar.MONTH) + 1)).append("/").append(baseDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(baseDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append((baseDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(baseDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(baseDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(baseDateTime.get(Calendar.MINUTE)).append(", ").append(baseDateTime.get(Calendar.SECOND)).append(", 0, ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(code).append(", ").append(mort).append(", 0, ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", ").append((baseDateTime.get(Calendar.MONTH) + 1)).append("/").append(baseDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(baseDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        break;
                    case ValueCodes.SCAN_MANUAL_NON_CODED_COMMAND:
                        date = Byte.toUnsignedInt(packet[index + 2]) << 16 | Byte.toUnsignedInt(packet[index + 3]) << 8 | Byte.toUnsignedInt(packet[index + 4]);
                        mm = date % 100;
                        hh = (date / 100) % 100;
                        DD = (date / 10000) % 100;
                        MM = date / 1000000;
                        ss = Byte.toUnsignedInt(packet[index + 1]);
                        baseDateTime.set(YY + 2000, MM - 1, DD, hh, mm, ss);
                        signalStrength = Byte.toUnsignedInt(packet[index + 5]);
                        periodHi = (Byte.toUnsignedInt(packet[index + 6]) << 8) | Byte.toUnsignedInt(packet[index + 7]);
                        periodLo = 0;

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
                            coordinates.timestamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                            index += 16;
                            byteIndex += 16;
                        }

                        text.append((baseDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(baseDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(baseDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(baseDateTime.get(Calendar.MINUTE)).append(", ").append(baseDateTime.get(Calendar.SECOND)).append(", 0, ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(periodHi).append(", ").append(periodLo).append(", 0, ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", ").append((baseDateTime.get(Calendar.MONTH) + 1)).append("/").append(baseDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(baseDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append((baseDateTime.get(Calendar.YEAR) - 2000)).append(", ").append(baseDateTime.get(Calendar.DAY_OF_YEAR)).append(", ").append(baseDateTime.get(Calendar.HOUR_OF_DAY)).append(", ").append(baseDateTime.get(Calendar.MINUTE)).append(", ").append(baseDateTime.get(Calendar.SECOND)).append(", 0, ").append(Converters.getFrequency(frequency)).append(", ").append(signalStrength).append(", ").append(periodHi).append(", ").append(periodLo).append(", 0, ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", ").append((baseDateTime.get(Calendar.MONTH) + 1)).append("/").append(baseDateTime.get(Calendar.DAY_OF_MONTH)).append("/").append(baseDateTime.get(Calendar.YEAR) - 2000).append(", ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        break;
                    case ValueCodes.SCAN_GPS_COMMAND: // MANUAL SCAN, NO DATA ONLY GPS
                        byte[] gpsData = new byte[16];
                        System.arraycopy(packet, index, gpsData, 0, 16);
                        coordinates = Converters.getGpsData(gpsData);
                        int year = Byte.toUnsignedInt(gpsData[1]);
                        MM = Byte.toUnsignedInt(gpsData[2]);
                        DD = Byte.toUnsignedInt(gpsData[3]);
                        hh = Byte.toUnsignedInt(gpsData[9]);
                        mm = Byte.toUnsignedInt(gpsData[10]);
                        ss = Byte.toUnsignedInt(gpsData[11]);
                        coordinates.timestamp = MM + "/" + DD + "/" + year + " " + hh + ":" + mm + ":" + ss;
                        index += 8;
                        byteIndex += 8;

                        text.append(YY).append(", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", 0, ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append(YY).append(", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ").append(coordinates.latitude).append(", ").append(coordinates.longitude).append(", ").append(coordinates.timestamp).append(", 0, ").append(sessionNumber).append(ValueCodes.CR).append(ValueCodes.LF);
                        break;
                    case ValueCodes.SCAN_STOP_COMMAND:
                        int scanSession = (Byte.toUnsignedInt(packet[index + 1]) * 65536) + (Byte.toUnsignedInt(packet[index + 2]) * 256) + Byte.toUnsignedInt(packet[index + 3]);
                        text.append("[Footer]" + ValueCodes.CR + ValueCodes.LF);
                        metrics.append("[Footer]" + ValueCodes.CR + ValueCodes.LF);
                        text.append("Session Num: ").append(scanSession).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append("Session Num: ").append(scanSession).append(ValueCodes.CR).append(ValueCodes.LF);
                        date = Byte.toUnsignedInt(packet[index + 12]) << 16 | Byte.toUnsignedInt(packet[index + 13]) << 8 | Byte.toUnsignedInt(packet[index + 14]);
                        mm = date % 100;
                        hh = (date / 100) % 100;
                        DD = (date / 10000) % 100;
                        MM = date / 1000000;
                        ss = Byte.toUnsignedInt(packet[index + 15]);
                        text.append("Time Stamp: ").append(MM).append("/").append(DD).append("/").append(YY).append(" ").append(hh).append(":").append(mm).append(":").append(ss).append(ValueCodes.CR).append(ValueCodes.LF);
                        metrics.append("Time Stamp: ").append(MM).append("/").append(DD).append("/").append(YY).append(" ").append(hh).append(":").append(mm).append(":").append(ss).append(ValueCodes.CR).append(ValueCodes.LF);
                        if (scanSession == sessionNumber)
                            sessionNumber++;
                        index += 8;
                        byteIndex += 8;
                        break;
                    default: // METRICS
                        if (format == ValueCodes.TIMESTAMP_COMMAND) {
                            int sysTime = Byte.toUnsignedInt(packet[index + 1]) << 16 | Byte.toUnsignedInt(packet[index + 2]) << 8 | Byte.toUnsignedInt(packet[index + 3]);
                            date = Byte.toUnsignedInt(packet[index + 4]) << 16 | Byte.toUnsignedInt(packet[index + 5]) << 8 | Byte.toUnsignedInt(packet[index + 6]);
                            mm = date % 100;
                            hh = (date / 100) % 100;
                            DD = (date / 10000) % 100;
                            MM = date / 1000000;
                            ss = Byte.toUnsignedInt(packet[index + 7]);
                            metrics.append("SysTime: ").append(sysTime).append(", M: ").append(MM).append(", D: ").append(DD).append(", H: ").append(hh).append(", m: ").append(mm).append(", s: ").append(ss).append(ValueCodes.CR).append(ValueCodes.LF);
                        } else if (format == ValueCodes.VOLTAGE_COMMAND) {
                            int sysTime = Byte.toUnsignedInt(packet[index + 1]) << 16 | Byte.toUnsignedInt(packet[index + 2]) << 8 | Byte.toUnsignedInt(packet[index + 3]);
                            int extVolt = Byte.toUnsignedInt(packet[index + 4]) << 8 | Byte.toUnsignedInt(packet[index + 5]);
                            int intVolt = (Byte.toUnsignedInt(packet[index + 7]) * 10) + 6000;
                            metrics.append("SysTime: ").append(sysTime).append(", Battery: ").append(Byte.toUnsignedInt(packet[index + 6])).append(", Internal Voltage: ").append(intVolt).append(", External Voltage: ").append(extVolt).append(ValueCodes.CR).append(ValueCodes.LF);
                        } else if (format == ValueCodes.LOW_BATTERY_COMMAND) {
                            metrics.append("Low Battery Detected" + ValueCodes.CR + ValueCodes.LF);
                        }
                        break;
                }
                index += 8;
                byteIndex += 8;
            }
        }
        return new String[] {text.toString(), metrics.toString()};
    }

    public static String getTagsData(ArrayList<TagDetail> tags) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        StringBuilder text = new StringBuilder("Tag Type, Tag ID, Date/Time, RSSI, Latitude, Longitude, Tag Version, Vcc (mv), Temp (C)" + ValueCodes.CR + ValueCodes.LF);
        for (TagDetail tag : tags)
            text.append("Bluetooth, ").append(tag.code).append(", ").append(simpleDateFormat.format(new Date(tag.lastTimestamp))).append(", ").append(tag.rssi).append(", ").append(tag.latitude).append(", ").append(tag.longitude).append(", 1.0, ").append(tag.voltage).append(", ").append(tag.temperature).append(ValueCodes.CR).append(ValueCodes.LF);
        return text.toString();
    }

    public static boolean printDataFiles(File root, ArrayList<Data> dataList) {
        if (dataList == null) return false;

        try {
            if (!root.exists()) {
                if (!root.mkdirs())
                    throw new Exception("Folder 'atstrack' can't be created on root: " + root.getPath());
                root.setReadable(true);
                root.setWritable(true);
            }

            for (Data dataObj : dataList) {
                File newFile = new File(root, dataObj.fileName);
                int copy = 1;
                String baseName = dataObj.fileName.substring(0, dataObj.fileName.length() - 4);
                while (newFile.exists() && copy < 100) {
                    newFile = new File(root, baseName + " (" + copy + ").txt");
                    copy++;
                }
                if (newFile.createNewFile()) {
                    newFile.setReadable(true);
                    newFile.setWritable(true);
                    try (FileOutputStream stream = new FileOutputStream(newFile)) {
                        for (byte[] packet : dataObj.packets)
                            stream.write(packet);
                        stream.flush();
                    }
                } else {
                    throw new Exception("The file can't be created: " + newFile.getName());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
