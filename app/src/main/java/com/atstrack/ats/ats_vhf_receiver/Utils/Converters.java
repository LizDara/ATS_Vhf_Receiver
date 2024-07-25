package com.atstrack.ats.ats_vhf_receiver.Utils;

import java.io.UnsupportedEncodingException;

// Converters - converts value between different numeral system
public class Converters {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    // Gets value in hexadecimal system
    public static String getHexValue(byte value[]) {
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
    public static String getDecimalValue(byte value[]) {
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
        try {
            returnVal = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            returnVal = input.getBytes();
            e.printStackTrace();
        }
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
}
