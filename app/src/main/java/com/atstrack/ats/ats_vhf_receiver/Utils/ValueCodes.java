package com.atstrack.ats.ats_vhf_receiver.Utils;

public class ValueCodes {
    //Device Category
    public static final String ACOUSTIC = "ATSar";
    public static final String VHF = "ATSvr";
    public static final String BLUETOOTH_RECEIVER = "UART"; // ATSbr
    public static final String WILDLINK = "ATSwl";
    public static final String BEACON = "CTT"; // ATSbt

    //Values
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String VALUE = "value";
    public static final String FIRMWARE_VERSION = "version";
    public static final String PARAMETER = "parameter";

    //Codes
    public static final int CANCELLED = 1000;
    public static final int RESULT_OK = 2000;
    public final static char CR = (char) 0x0D;
    public final static char LF = (char) 0x0A;
    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int REQUEST_CODE_OPEN_STORAGE = 2;
    public static final int RAW_FILE = 3;
    public static final int PROCESSED_FILE = 4;
    public static final int BLUETOOTH_FILE = 5;
    public static final int LOG_FILE = 6;
    public static final byte NONE = (byte) 0x00;
    public static final byte NULL = (byte) 0xFF;

    //Periods
    public static final int WAITING_PERIOD = 180;
    public static final int MESSAGE_PERIOD = 1000;
    public static final int DOWNLOAD_PERIOD = 220; //280
    public static final int SCAN_PERIOD = 2000;
    public static final int BRANDING_PERIOD = 2000;
    public static final int CONNECT_TIMEOUT = 3200;

    //Firmware Update
    public static final byte MTU = (byte) 0xF7;
    public static final byte UPDATE = (byte) 0xF8;
    public static final byte FINISH = (byte) 0xF9;

    //Views
    public static final int CATEGORIES = 301;
    public static final int BLUETOOTH_TAGS = 302;
    public static final int SEARCHING = 303;
    public static final int FOUNDED = 304;
    public static final int NO_FOUNDED = 305;
    public static final int CONNECTING = 306;
    public static final int CONNECTED = 307;
    public static final int OVERVIEW = 308;
    public static final int PROCESSING = 309;
    public static final int FIRST_STEP = 311;
    public static final int SECOND_STEP = 312;
    public static final int THIRD_STEP = 313;
    public static final int FOURTH_STEP = 314;
    public static final int DELETE = 315;
    public static final int DOWNLOAD = 317;
    public static final int DOWNLOADING = 318;
    public static final int DOWNLOADED = 319;
    public static final int SCANNING = 322;
    public static final int START_RECORD = 323;
    public static final int STOP_RECORD = 324;
    public static final int START_HOLD = 327;
    public static final int STOP_HOLD = 328;

    /* ----------------- VHF DEVICE ---------------- */
    //Detection Filter
    public static final byte CODED = 0x09;
    public static final byte FIXED = 0x08;
    public static final byte VARIABLE = 0x07;
    public static final byte VARIABLE_TEMPERATURE = 0x06;

    //Defaults
    public static final int TABLE_NUMBER_CODE = 1001;
    public static final int TABLES_NUMBER_CODE = 1002;
    public static final int SCAN_RATE_MOBILE_CODE = 1003;
    public static final int SCAN_RATE_STATIONARY_CODE = 1004;
    public static final int NUMBER_OF_ANTENNAS_CODE = 1005;
    public static final int SCAN_TIMEOUT_SECONDS_CODE = 1006;
    public static final int STORE_RATE_CODE = 1007;
    public static final int REFERENCE_FREQUENCY_STORE_RATE_CODE = 1008;
    public static final int PULSE_RATE_TYPE_CODE = 1009;
    public static final int MATCHES_FOR_VALID_PATTERN_CODE = 1010;
    public static final int PULSE_RATE_1_CODE = 1014;
    public static final int PULSE_RATE_2_CODE = 1015;
    public static final int PULSE_RATE_3_CODE = 1016;
    public static final int PULSE_RATE_4_CODE = 1017;
    public static final int MAX_PULSE_RATE_CODE = 1018;
    public static final int MIN_PULSE_RATE_CODE = 1019;
    public static final int DATA_CALCULATION_TYPE_CODE = 1020;
    public static final int GPS_CODE = 1021;
    public static final int AUTO_RECORD_CODE = 1022;

    //Values
    public static final String IS_SCANNING = "isScanning";
    public static final String DEFAULT_SETTING = "defaults";
    public static final String BASE_FREQUENCY = "baseFrequency";
    public static final String RANGE = "range";
    public static final String TITLE = "title";
    public static final String POSITION = "position";
    public static final String TYPE = "type";
    public static final String TOTAL = "total";
    public static final String IS_TEMPERATURE = "isTemperature";
    public static final String TABLE = "table";

    //Data
    public static final String FIRST_TABLE_NUMBER = "FirstTableNumber";
    public static final String SECOND_TABLE_NUMBER = "SecondTableNumber";
    public static final String THIRD_TABLE_NUMBER = "ThirdTableNumber";

    //COMMANDS
    public static final byte MOBILE_PAUSE_COMMAND = (byte) 0x80;
    public static final byte MOBILE_HOLD_COMMAND = (byte) 0x81;
    public static final byte MOBILE_SCAN_COMMAND = (byte) 0x82;
    public static final byte STATIONARY_SCAN_COMMAND = (byte) 0x83;
    public static final byte MANUAL_SCAN_COMMAND = (byte) 0x86;
    public static final byte SCAN_STATE_COMMAND = (byte) 0x50;
    public static final byte SD_CARD_COMMAND = (byte) 0x56;
    public static final byte BATTERY_COMMAND = (byte) 0x88;
    public static final byte BOARD_STATUS_COMMAND = (byte) 0x41;
    public static final byte COEFFICIENTS_COMMAND = (byte) 0x7D;
    public static final byte STORAGE_COMMAND = (byte) 0x52;
    public static final byte STORAGE_RESPONSE_COMMAND = (byte) 0xDD;
    public static final byte STORAGE_ERROR_COMMAND = (byte) 0xAA;
    public static final byte DETECTION_FILTER_COMMAND = (byte) 0x67;
    public static final byte TABLES_COMMAND = (byte) 0x7A;
    public static final byte DIAGNOSTIC_COMMAND = (byte) 0x89;
    public static final byte MOBILE_DEFAULTS_COMMAND = (byte) 0x6D;
    public static final byte STATIONARY_DEFAULTS_COMMAND = (byte) 0x6C;

    public static final byte SCAN_HEADER_COMMAND = (byte) 0xF0;
    public static final byte SCAN_FIX_CODED_COMMAND = (byte) 0xF1;
    public static final byte SCAN_FIX_CONSOLIDATED_CODED_COMMAND = (byte) 0xF2;
    public static final byte SCAN_DATA_FIXED_NON_CODED_COMMAND = (byte) 0xE1;
    public static final byte SCAN_DATA_VARIABLE_NON_CODED_COMMAND = (byte) 0xEA;
    public static final byte SCAN_FIXED_CONSOLIDATED_NON_CODED_COMMAND = (byte) 0xE2;
    public static final byte SCAN_VARIABLE_CONSOLIDATED_NON_CODED_COMMAND = (byte) 0xEB;
    public static final byte SCAN_MANUAL_CODED_COMMAND = (byte) 0xD0;
    public static final byte SCAN_MANUAL_NON_CODED_COMMAND = (byte) 0xE0;
    public static final byte SCAN_GPS_STATE_COMMAND = (byte) 0x51;
    public static final byte SCAN_GPS_COMMAND = (byte) 0xA1;
    public static final byte SCAN_FREQUENCIES_NUMBER_COMMAND = (byte) 0x8A;
    public static final byte SCAN_STOP_COMMAND = (byte) 0x87;
    public static final byte AUDIO_ONE_COMMAND = (byte) 0x59;
    public static final byte AUDIO_ALL_COMMAND = (byte) 0x5A;
    public static final byte AUDIO_BACKGROUND_COMMAND = (byte) 0x5B;
    public static final byte FATAL_SCAN_ERROR_COMMAND = (byte) 0x44;

    public static final byte GPS_VALID = 3;
    public static final byte GPS_FAILED = 2;
    public static final byte GPS_SEARCHING = 1;
    public static final byte GPS_OFF = 0;

    /* ----------------- ACOUSTIC DEVICE ---------------- */

    //COMMANDS
    public static final byte ACOUSTIC_STATUS_COMMAND = (byte) 0x78;

    /* ----------------- BLUETOOTH RECEIVER DEVICE ---------------- */
    public static final int RSSI = 10;
    public static final int VOLTAGE = 11;
    public static final int TEMPERATURE = 12;

    public static final int MIN_RSSI = -90;
    public static final int MAX_BLUETOOTH_RSSI = -20;
    public static final int MAX_TAG_RSSI = -44;
    public static final int MIN_VOLTAGE = 0;
    public static final int MAX_VOLTAGE = 1800;
    public static final int MIN_TEMPERATURE = 0;
    public static final int MAX_TEMPERATURE = 60;
}