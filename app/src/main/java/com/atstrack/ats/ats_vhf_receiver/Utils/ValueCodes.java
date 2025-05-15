package com.atstrack.ats.ats_vhf_receiver.Utils;

public class ValueCodes {
    //Device Category
    public static final String ACOUSTIC = "ACOUSTIC";
    public static final String VHF = "VHF";

    //Codes
    public static final int CANCELLED = 1000;
    public static final int RESULT_OK = 2000;
    public final static char CR = (char) 0x0D;
    public final static char LF = (char) 0x0A;
    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int REQUEST_CODE_OPEN_STORAGE = 3;

    //Defaults
    public static final int TABLE_NUMBER_CODE = 1001;
    public static final int TABLES_NUMBER_CODE = 1002;
    public static final int SCAN_RATE_SECONDS_CODE = 1003;
    public static final int NUMBER_OF_ANTENNAS_CODE = 1004;
    public static final int SCAN_TIMEOUT_SECONDS_CODE = 1005;
    public static final int STORE_RATE_CODE = 1006;
    public static final int REFERENCE_FREQUENCY_STORE_RATE_CODE = 1007;
    public static final int PULSE_RATE_TYPE_CODE = 1008;
    public static final int MATCHES_FOR_VALID_PATTERN_CODE = 1009;
    public static final int CODED_CODE = 1010;
    public static final int FIXED_PULSE_RATE_CODE = 1011;
    public static final int VARIABLE_PULSE_RATE_CODE = 1012;
    public static final int PULSE_RATE_1_CODE = 1013;
    public static final int PULSE_RATE_2_CODE = 1014;
    public static final int PULSE_RATE_3_CODE = 1015;
    public static final int PULSE_RATE_4_CODE = 1016;
    public static final int MAX_PULSE_RATE_CODE = 1017;
    public static final int MIN_PULSE_RATE_CODE = 1018;
    public static final int DATA_CALCULATION_TYPE_CODE = 1019;

    //Values
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String VALUE = "value";
    public static final String VERSION = "version";
    public static final String STATUS = "status";
    public static final String SCANNING = "scanning";
    public static final String DEFAULT_SETTING = "defaults";
    public static final String BASE_FREQUENCY = "baseFrequency";
    public static final String RANGE = "range";
    public static final String FIRST_TIME = "firstTime";
    public static final String TITLE = "title";
    public static final String POSITION = "position";
    public static final String PARAMETER = "parameter";
    public static final String TYPE = "type";
    public static final String TOTAL = "total";
    public static final String IS_FILE = "isFile";
    public static final String FREQUENCIES = "frequencies";
    public static final String IS_TEMPERATURE = "isTemperature";

    //Parameters
    public static final String OTA_BEGIN = "begin";
    public static final String MTU = "mtu";
    public static final String UPDATE = "update";
    public static final String OTA_END_WRITTEN = "endWritten";
    public static final String FINISH = "finish";
    public static final String OTA_END = "end";
    public static final String SAVE = "save";
    public static final String MOBILE_DEFAULTS = "mobile";
    public static final String TABLES = "tables";
    public static final String START_LOG = "startLog";
    public static final String CONTINUE_LOG = "continueLog";
    public static final String START_SCAN = "startScan";
    public static final String STOP_SCAN = "stopScan";
    public static final String HOLD = "hold";
    public static final String ADD_FREQUENCY = "addFrequency";
    public static final String DELETE_FREQUENCY = "deleteFrequency";
    public static final String DECREASE = "decrease";
    public static final String INCREASE = "increase";
    public static final String MERGE = "merge";
    public static final String RECORD = "record";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String TABLE = "table";
    public static final String TEST = "test";
    public static final String DOWNLOAD = "download";
    public static final String DELETE_RESPONSE = "response";
    public static final String DELETE = "delete";
    public static final String PAGES_NUMBER = "pagesNumber";
    public static final String READY_DOWNLOAD = "ready";
    public static final String PAGE_OK = "pageOk";
    public static final String PAGE_BAD = "pageBad";
    public static final String STATIONARY_DEFAULTS = "stationary";
    public static final String SCAN_STATUS = "scanStatus";
    public static final String BOARD_STATUS = "boardStatus";
    public static final String AUDIO = "audio";
    public static final String BACKGROUND = "background";
    public static final String DETECTION_TYPE = "detectionType";

    //Original Data
    public static final String TABLE_NUMBER = "TableNumber";
    public static final String GPS = "Gps";
    public static final String AUTO_RECORD = "AutoRecord";
    public static final String SCAN_RATE = "ScanRate";
    public static final String FIRST_TABLE_NUMBER = "FirstTableNumber";
    public static final String SECOND_TABLE_NUMBER = "SecondTableNumber";
    public static final String THIRD_TABLE_NUMBER = "ThirdTableNumber";
    public static final String ANTENNA_NUMBER = "AntennaNumber";
    public static final String SCAN_TIMEOUT = "ScanTimeout";
    public static final String STORE_RATE = "StoreRate";
    public static final String EXTERNAL_DATA_TRANSFER = "ExternalDataTransfer";
    public static final String REFERENCE_FREQUENCY = "ReferenceFrequency";
    public static final String REFERENCE_FREQUENCY_STORE_RATE = "ReferenceFrequencyStoreRate";
    public static final String PULSE_RATE_TYPE = "PulseRateType";
    public static final String MATCHES = "Matches";
    public static final String PULSE_RATE_1 = "PulseRate1";
    public static final String PULSE_RATE_2 = "PulseRate2";
    public static final String PULSE_RATE_3 = "PulseRate3";
    public static final String PULSE_RATE_4 = "PulseRate4";
    public static final String PULSE_RATE_TOLERANCE_1 = "PulseRateTolerance1";
    public static final String PULSE_RATE_TOLERANCE_2 = "PulseRateTolerance2";
    public static final String PULSE_RATE_TOLERANCE_3 = "PulseRateTolerance3";
    public static final String PULSE_RATE_TOLERANCE_4 = "PulseRateTolerance4";
    public static final String MAX_PULSE_RATE = "MaxPulseRate";
    public static final String MIN_PULSE_RATE = "MinPulseRate";
    public static final String DATA_CALCULATION = "DataCalculation";

    //Periods
    public static final int DISCONNECTION_MESSAGE_PERIOD = 1000;
    public static final int WAITING_PERIOD = 180;
    public static final int MESSAGE_PERIOD = 1300;
    public static final int DOWNLOAD_PERIOD = 280;
    public static final int SCAN_PERIOD = 2000;
    public static final int BRANDING_PERIOD = 2000;
    public static final int CONNECT_TIMEOUT = 3000;
}