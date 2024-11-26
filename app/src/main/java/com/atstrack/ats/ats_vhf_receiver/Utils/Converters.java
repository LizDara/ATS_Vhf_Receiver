package com.atstrack.ats.ats_vhf_receiver.Utils;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;

// Converters - converts value between different numeral system
public class Converters {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    // Gets value in hexadecimal system
    public static String getHexValue(byte[] value) {
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

    // Gets value in hexadecimal system for single byte
    public static String getHexValue(byte b) {
        char[] hexChars = new char[2];
        int v;
        v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    // Gets value in decimal system
    public static String getDecimalValue(byte[] value) {
        if (value == null)
            return "";

        String result = "";
        for (byte b : value)
            result += ((int) b & 0xff) + " ";
        return result;
    }

    // Gets value in decimal system for single byte
    public static String getDecimalValue(byte b) {
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
     * Processes the data when the download is complete.
     * @param packet The raw data.
     * @return Returns the processed data.
     */
    public static synchronized String readPacket(byte[] packet, int baseFrequency) {
        String data = "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsAge, Date, SessionNum" + ValueCodes.CR + ValueCodes.LF;
        int index = 0;
        int frequency = 0;
        int frequencyTableIndex = 0;
        int year;
        int antenna = 0;
        int sessionNumber = 1;
        Calendar calendar = Calendar.getInstance();

        while (index < packet.length) {
            String format = Converters.getHexValue(packet[index]);
            if (format.equals("83") || format.equals("82")) {
                year = Integer.parseInt(Converters.getDecimalValue(packet[index + 6]));
                index += 8;
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
                        int seconds = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
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
                        if (scanSession == sessionNumber)
                            sessionNumber++;
                        index += 8;
                    }
                    index += 8;
                }
            } else if (format.equals("86")) {
                year = Integer.parseInt(Converters.getDecimalValue(packet[index + 2]));
                int month = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                int day = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                int hour = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                int minute = Integer.parseInt(Converters.getDecimalValue(packet[index + 6]));
                int seconds = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                calendar.set(year + 2000, month - 1, day, hour, minute, seconds);

                if (Converters.getHexValue(packet[index + 8]).equals("D0")) {
                    frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 11]));
                    int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 12]));
                    int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 13]));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", 0, " + frequencyTableIndex +
                            ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", " + code + ", " + mort + ", 0, 0, 0, 0, " +
                            ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                } else if (Converters.getHexValue(packet[index + 8]).equals("E0")) {
                    frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 11]));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", 0, " + frequencyTableIndex +
                            ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", 0, 0, 0, 0, 0, 0, " + ((calendar.get(Calendar.MONTH) + 1) +
                            "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", " + sessionNumber + ValueCodes.CR + ValueCodes.LF;
                } else if (Converters.getHexValue(packet[index]).equals("87")) {
                    int scanSession = (Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 65536) + (Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                    if (scanSession == sessionNumber)
                        sessionNumber++;
                }
                index += 16;
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
                Log.i("CONVERTERS", "New File Root: " + newFile.getAbsolutePath());
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
            Log.i("CONVERTERS", "Number: " + i);
            return i == snapshotArray.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
