package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.style.body_regular;

public class StationaryScanActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.device_name_textView)
    TextView device_name_textView;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
    @BindView(R.id.percent_battery_textView)
    TextView percent_battery_textView;
    @BindView(R.id.ready_stationary_scan_LinearLayout)
    LinearLayout ready_stationary_scan_LinearLayout;
    @BindView(R.id.ready_stationary_textView)
    TextView ready_stationary_textView;
    @BindView(R.id.scan_rate_stationary_textView)
    TextView scan_rate_stationary_textView;
    @BindView(R.id.selected_frequency_stationary_textView)
    TextView selected_frequency_stationary_textView;
    @BindView(R.id.store_rateC_stationary_textView)
    TextView store_rateC_stationary_textView;
    @BindView(R.id.external_data_transfer_stationary_textView)
    TextView external_data_transfer_stationary_textView;
    @BindView(R.id.number_antennas_stationary_textView)
    TextView number_antennas_stationary_textView;
    @BindView(R.id.timeout_stationary_textView)
    TextView timeout_stationary_textView;
    @BindView(R.id.edit_stationary_settings_button)
    TextView edit_stationary_defaults_textView;
    @BindView(R.id.frequency_empty_textView)
    TextView frequency_empty_textView;
    @BindView(R.id.start_stationary_button)
    Button start_stationary_button;
    @BindView(R.id.stationary_result_linearLayout)
    LinearLayout stationary_result_linearLayout;
    @BindView(R.id.table_stationary_textView)
    TextView table_stationary_textView;
    @BindView(R.id.frequency_stationary_textView)
    TextView frequency_stationary_textView;
    @BindView(R.id.current_antenna_stationary_textView)
    TextView current_antenna_stationary_textView;
    @BindView(R.id.scan_details_linearLayout)
    LinearLayout scan_details_linearLayout;
    @BindView(R.id.code_textView)
    TextView code_textView;
    @BindView(R.id.period_textView)
    TextView period_textView;
    @BindView(R.id.pulse_rate_textView)
    TextView pulse_rate_textView;
    @BindView(R.id.line_view)
    View line_view;

    private final static String TAG = StationaryScanActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private static final int WAITING_PERIOD = 1000;

    private boolean isScanning;
    private boolean previousScanning;

    private AnimationDrawable animationDrawable;

    private int baseFrequency;
    private byte detectionType;
    private int selectedTable;
    private int numberAntennas;
    private int code;
    private int detections;
    private int mort;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int seconds;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = true;
    private String parameter = "";
    private String parameterWrite = "";

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    switch (parameter) {
                        case "stationary": // Gets stationary defaults data
                            onClickStationary();
                            break;
                        case "sendLog": // Receives the data
                            onClickLog();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    switch (parameter) {
                        case "stationary": // Gets stationary defaults data
                            downloadData(packet);
                            break;
                        case "sendLog": // Receives the data
                            setCurrentLog(packet);
                            break;
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    private final BroadcastReceiver mGattUpdateReceiverWrite = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                Log.i(TAG, "BROADCAST RECEIVER 1: " + action);
                if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND.equals(action)) {
                    switch (parameterWrite) {
                        case "startStationary": // Starts to scan
                            onClickStart();
                            break;
                        case "stopStationary": // Stops scan
                            onClickStop();
                            break;
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private static IntentFilter makeGattUpdateIntentFilterWrite() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND);
        return intentFilter;
    }

    /**
     * Requests a read for get stationary defaults data.
     * Service name: Scan.
     * Characteristic name: Stationary.
     */
    private void onClickStationary() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Writes the stationary scan data for start to scan.
     * Service name: Scan.
     * Characteristic name: Stationary.
     */
    private void onClickStart() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);
        year = YY % 100;

        byte[] b = new byte[] {(byte) 0x83, (byte) (YY % 100), (byte) MM, (byte) DD, (byte) hh, (byte) mm, (byte) ss, (byte) selectedTable};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        isScanning = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (isScanning) {
            parameterWrite = "";
            title_toolbar.setText(R.string.lb_stationary_scanning);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            ready_stationary_scan_LinearLayout.setVisibility(View.GONE);
            stationary_result_linearLayout.setVisibility(View.VISIBLE);

            state_view.setBackgroundResource(R.drawable.scanning_animation);
            animationDrawable = (AnimationDrawable) state_view.getBackground();
            animationDrawable.start();
            frequency_stationary_textView.setText("");

            device_name_textView.setText(receiverInformation.getDeviceName().replace("Not scanning", "Scanning, stationary"));
        }
    }

    /**
     * Enables notification for receive the data.
     * Service name: Screen.
     * Characteristic name: SendLog.
     */
    private void onClickLog() {
        parameterWrite = "startStationary";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, WAITING_PERIOD);
    }

    /**
     * Writes a value for stop scan.
     * Service name: Scan.
     * Characteristic name: Stationary.
     */
    private void onClickStop() {
        byte[] b = new byte[] {(byte) 0x87};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            clear();
            isScanning = false;
            animationDrawable.stop();
            state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
            stationary_result_linearLayout.setVisibility(View.GONE);
            ready_stationary_scan_LinearLayout.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.stationary_scanning);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

            device_name_textView.setText(receiverInformation.getDeviceName());

            if (previousScanning) {
                parameter = "stationary";
                new Handler().postDelayed(() -> {
                    mBluetoothLeService.discovering();
                }, WAITING_PERIOD);
            } else {
                parameter = "";
            }
        }
    }

    @OnClick(R.id.edit_stationary_settings_button)
    public void onClickEditStationarySettings(View v) {
        parameter = "stationary";
        Intent intent = new Intent(this, StationaryDefaultsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.start_stationary_button)
    public void onClickStartStationary(View v) {
        parameter = "sendLog";
        mBluetoothLeService.discovering();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary_scan);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.stationary_scanning);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        isScanning = getIntent().getExtras().getBoolean("scanning");
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        baseFrequency = sharedPreferences.getInt("BaseFrequency", 0);
        detectionType = (byte) sharedPreferences.getInt("DetectionType", 0);

        int visibility = (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);

        if (isScanning) { // The device is already scanning
            previousScanning = true;
            parameter = "sendLog";
            year = getIntent().getExtras().getInt("year");
            month = getIntent().getExtras().getInt("month");
            day = getIntent().getExtras().getInt("day");
            hour = getIntent().getExtras().getInt("hour");
            minute = getIntent().getExtras().getInt("minute");
            seconds = getIntent().getExtras().getInt("seconds");

            ready_stationary_scan_LinearLayout.setVisibility(View.GONE);
            stationary_result_linearLayout.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.lb_stationary_scanning);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

            state_view.setBackgroundResource(R.drawable.scanning_animation);
            animationDrawable = (AnimationDrawable) state_view.getBackground();
            animationDrawable.start();
        } else { // Gets aerial defaults data
            previousScanning = false;
            parameter = "stationary";
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                Intent intent = new Intent(this, StationaryScanningActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                parameterWrite = "stopStationary";
                mBluetoothLeService.discoveringSecond();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isScanning) { // Asks if you want to stop the scan
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stop Stationary");
            builder.setMessage("Are you sure you want to stop scanning?");
            builder.setPositiveButton("OK", (dialog, which) -> {
                parameterWrite = "stopStationary";
                mBluetoothLeService.discoveringSecond();
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.catskill_white)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(mGattUpdateReceiverWrite, makeGattUpdateIntentFilterWrite());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(mGattUpdateReceiverWrite);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected)
            showDisconnectionMessage();
        return true;
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        dialog.setView(view);
        dialog.show();

        // The message disappears after a pre-defined period and will search for other available BLE devices again
        int MESSAGE_PERIOD = 3000;
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, MESSAGE_PERIOD);
    }

    /**
     * With the received packet, gets stationary defaults data.
     *
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (data.length == 1) {
            parameter = "stationary";
            mBluetoothLeService.discovering();
        } else if (Converters.getHexValue(data[0]).equals("6C")) {
            parameter = "";
            selectedTable = Integer.parseInt(Converters.getDecimalValue(data[1])) / 16;
            if (selectedTable == 0) { // There are no tables with frequencies to scan
                selected_frequency_stationary_textView.setText(R.string.lb_none);
                //frequency_empty_textView.setVisibility(View.VISIBLE);
                start_stationary_button.setEnabled(false);
                start_stationary_button.setAlpha((float) 0.6);
            } else { // Shows the table to be scanned
                selected_frequency_stationary_textView.setText(String.valueOf(selectedTable));
                //frequency_empty_textView.setVisibility(View.GONE);
                start_stationary_button.setEnabled(true);
                start_stationary_button.setAlpha((float) 1);
            }
            numberAntennas = Integer.parseInt(Converters.getDecimalValue(data[1])) % 16;
            number_antennas_stationary_textView.setText((numberAntennas == 0) ? "None" : String.valueOf(numberAntennas));
            scan_rate_stationary_textView.setText(Converters.getDecimalValue(data[3]));
            timeout_stationary_textView.setText(Converters.getDecimalValue(data[4]));
            if (Converters.getHexValue(data[5]).equals("FF")) {
                store_rateC_stationary_textView.setText("Continuous Store");
            } else {
                store_rateC_stationary_textView.setText((Converters.getDecimalValue(data[5]).equals("0")) ? "No Store Rate" :
                        Converters.getDecimalValue(data[5]));
            }
        }
    }

    /**
     * With the received packet, gets the data of scanning.
     *
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        switch (Converters.getHexValue(data[0])) {
            case "83":
                startScanStationaryFirstPart(data);
                break;
            case "84":
                startScanStationarySecondPart(data);
                break;
            case "85":
                startScanStationaryThirdPart(data);
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "F1":
                logScanFix(data);
                break;
            case "F2":
                logScanFixConsolidated(data);
                break;
            default: //E1 and E2
                logScanData(data);
                break;
        }
    }

    /**
     * Process the stationary data, first part of packet.
     *
     * @param data The stationary data packet.
     */
    private void startScanStationaryFirstPart(byte[] data) {
        selectedTable = Integer.parseInt(Converters.getDecimalValue(data[1])) / 16;
        numberAntennas = Integer.parseInt(Converters.getDecimalValue(data[1])) % 16;
        year = Integer.parseInt(Converters.getDecimalValue(data[6]));

        byte detectionType = (byte) (Integer.parseInt(Converters.getDecimalValue(data[4])) % 16);
        int visibility = (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);
    }

    /**
     * Process the stationary data, second part of packet.
     *
     * @param data The stationary data packet.
     */
    private void startScanStationarySecondPart(byte[] data) {
    }

    /**
     * Process the stationary data, third part of packet.
     *
     * @param data The stationary data packet.
     */
    private void startScanStationaryThirdPart(byte[] data) {
    }

    /**
     * With the received packet, processes the data of scan header to display.
     *
     * @param data The received packet.
     */
    private void logScanHeader(byte[] data) {
        int frequency = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + 150000;
        /*int date = Converters.hexToDecimal(Converters.getHexValue(data[4]) + Converters.getHexValue(data[5]) + Converters.getHexValue(data[6]));
        month = date / 1000000;
        date = date % 1000000;
        day = date / 10000;
        date = date % 10000;
        hour = date / 100;
        minute = date % 100;
        seconds = Integer.parseInt(Converters.getDecimalValue(data[7]));
        currentData += month + "/" + day + "/" + year + "       " + hour + ":" + minute + ":" + seconds;*/

        if (!frequency_stationary_textView.getText().equals("") &&
                Integer.parseInt(frequency_stationary_textView.getText().toString().replace(".", "")) != frequency) {
            clear();
        }

        table_stationary_textView.setText(selectedTable + " (" + Converters.getDecimalValue(data[3]) + ")");
        frequency_stationary_textView.setText(String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3));
        current_antenna_stationary_textView.setText((numberAntennas == 0) ? "All" : String.valueOf(numberAntennas));
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is code.
     *
     * @param data The received packet.
     */
    private void logScanFix(byte[] data) {
        int position;
        int code = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = Integer.parseInt(Converters.getDecimalValue(data[7]));
        int mort = Integer.parseInt(Converters.getDecimalValue(data[5]));

        if (scan_details_linearLayout.getChildCount() > 2 && isEqualFirstCode(code)) {
            refreshFirstCode(signalStrength, mort > 0);
        } else if ((position = positionCode(code)) != 0) {
            refreshPosition(position, signalStrength, mort > 0);
        } else {
            createCodeDetail(code, signalStrength, detections, mort > 0);
        }
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is code.
     *
     * @param data The received packet.
     */
    private void logScanFixConsolidated(byte[] data) {
        int position;
        int code = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = Integer.parseInt(Converters.getDecimalValue(data[7]));
        int mort = Integer.parseInt(Converters.getDecimalValue(data[5]));

        if (scan_details_linearLayout.getChildCount() > 2 && isEqualFirstCode(code)) {
            refreshFirstCode(signalStrength, mort > 0);
        } else if ((position = positionCode(code)) != 0) {
            refreshPosition(position, signalStrength, mort > 0);
        } else {
            createCodeDetail(code, signalStrength, detections, mort > 0);
        }
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is non code.
     *
     * @param data The received packet.
     */
    private void logScanData(byte[] data) {
        int period = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        float pulseRate = (float) (60000 / period);
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = Integer.parseInt(Converters.getDecimalValue(data[2])) / 10;

        refreshNonCoded(period, pulseRate, signalStrength, detections);
    }

    /**
     * Creates a row of code data and display it.
     *
     * @param code Number of code.
     * @param signalStrength Number of signal strength.
     * @param detections Number of detections.
     * @param isMort True, if the code is mort.
     */
    private void createCodeDetail(int code, int signalStrength, int detections, boolean isMort) {
        LinearLayout newCode = new LinearLayout(this);
        newCode.setOrientation(LinearLayout.HORIZONTAL);
        newCode.setPadding(0, 8, 0, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;

        TextView codeTextView = new TextView(this);
        codeTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        codeTextView.setTextAppearance(body_regular);
        codeTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        codeTextView.setLayoutParams(params);

        TextView signalStrengthTextView = new TextView(this);
        signalStrengthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        signalStrengthTextView.setTextAppearance(body_regular);
        signalStrengthTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        signalStrengthTextView.setLayoutParams(params);

        TextView detectionsTextView = new TextView(this);
        detectionsTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        detectionsTextView.setTextAppearance(body_regular);
        detectionsTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        detectionsTextView.setLayoutParams(params);

        TextView mortTextView = new TextView(this);
        mortTextView.setVisibility(View.GONE);

        newCode.addView(codeTextView);
        newCode.addView(signalStrengthTextView);
        newCode.addView(detectionsTextView);
        newCode.addView(mortTextView);

        LinearLayout line = new LinearLayout(this);
        line.setBackgroundColor(ContextCompat.getColor(this, light_gray));
        line.setLayoutParams(line_view.getLayoutParams());

        scan_details_linearLayout.addView(newCode);
        scan_details_linearLayout.addView(line);

        refreshCode(scan_details_linearLayout.getChildCount() - 2, code, signalStrength, detections, isMort, 0);
    }

    /**
     * Checks that the first code in the table is equal to code received.
     *
     * @param code Number of code received.
     *
     * @return Returns true, if the first code is equal to code received.
     */
    private boolean isEqualFirstCode(int code) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);

        return Integer.parseInt(codeTextView.getText().toString().replace(" M", "")) == code;
    }

    /**
     * Updates data in the first row in the table.
     *
     * @param signalStrength Number of signal strength to update.
     */
    private void refreshFirstCode(int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(1);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(2);
        TextView mortTextView = (TextView) linearLayout.getChildAt(3);

        if (!codeTextView.getText().toString().contains(" M") && isMort) codeTextView.setText(codeTextView.getText().toString() + " M");
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        detectionsTextView.setText(String.valueOf(Integer.parseInt(detectionsTextView.getText().toString()) + 1));
        if (isMort) mortTextView.setText(String.valueOf(Integer.parseInt(mortTextView.getText().toString()) + 1));
        detections = Integer.parseInt(detectionsTextView.getText().toString());
        mort = Integer.parseInt(mortTextView.getText().toString());
    }

    /**
     * Looks for the position in the table of code received.
     *
     * @param code Number of code to look.
     *
     * @return Returns the position of code in the table.
     */
    private int positionCode(int code) {
        int position = 0;
        for (int i = 4; i < scan_details_linearLayout.getChildCount() - 1; i += 2) {
            LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            TextView textView = (TextView) linearLayout.getChildAt(0);

            if (Integer.parseInt(textView.getText().toString().replace(" M", "")) == code) {
                position = i;
            }
        }

        return position;
    }

    /**
     * Updates data at a specific position in the table.
     *
     * @param position Number of position in the table to update.
     * @param signalStrength Number of signal strength.
     */
    private void refreshPosition(int position, int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(2);
        TextView mortTextView = (TextView) linearLayout.getChildAt(3);

        int code = Integer.parseInt(codeTextView.getText().toString().replace(" M", ""));
        int detections = Integer.parseInt(detectionsTextView.getText().toString());
        int mort = Integer.parseInt(mortTextView.getText().toString());

        refreshCode(position, code, signalStrength, detections + 1, isMort, mort);
    }

    /**
     * Updates data from a specific position to the first position in the table.
     *
     * @param finalPosition Number of rows to update in the table.
     * @param code Number of code received.
     * @param signalStrength Number of signal strength received.
     * @param detections Number of signal strength calculated.
     * @param isMort True, if the code is mort.
     */
    private void refreshCode(int finalPosition, int code, int signalStrength, int detections, boolean isMort, int mort) {
        for (int i = finalPosition; i > 3 ; i -= 2) {
            LinearLayout lastLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            LinearLayout penultimateLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i - 2);

            TextView lastCodeTextView = (TextView) lastLinearLayout.getChildAt(0);
            TextView penultimateCodeTextView = (TextView) penultimateLinearLayout.getChildAt(0);
            lastCodeTextView.setText(penultimateCodeTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastMortTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateMortTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastMortTextView.setText(penultimateMortTextView.getText());
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newCodeTextView = (TextView) linearLayout.getChildAt(0);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(1);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(2);
        TextView newMortTextView = (TextView) linearLayout.getChildAt(3);

        newCodeTextView.setText(code + (isMort ? " M" : ""));
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
        newDetectionsTextView.setText(String.valueOf(detections));

        newMortTextView.setText(isMort ? String.valueOf(mort + 1) : String.valueOf(mort));
        this.detections =  detections;
        this.mort = isMort ? mort + 1 : mort;
        Log.i(TAG, "Code: " + newCodeTextView.getText() + " SS: " + newSignalStrengthTextView.getText() + " Det: " + newDetectionsTextView.getText() + " Mort: " + newMortTextView.getText() + " Size: " + scan_details_linearLayout.getChildCount());
    }

    /**
     * Creates a row of non code data and display it.
     *
     * @param period Number of period received.
     * @param pulseRate Number of pulse rate received.
     * @param signalStrength Number of signal strength received.
     * @param detections Number of detections received.
     */
    private void refreshNonCoded(int period, float pulseRate, int signalStrength, int detections) {
        LinearLayout newNonCoded = new LinearLayout(this);
        newNonCoded.setOrientation(LinearLayout.HORIZONTAL);
        newNonCoded.setPadding(0, 8, 0, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;

        TextView periodTextView = new TextView(this);
        periodTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        periodTextView.setTextAppearance(this, R.style.body_regular);
        periodTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        periodTextView.setLayoutParams(params);

        TextView pulseRateTextView = new TextView(this);
        pulseRateTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        pulseRateTextView.setTextAppearance(this, R.style.body_regular);
        pulseRateTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        pulseRateTextView.setLayoutParams(params);

        TextView signalStrengthTextView = new TextView(this);
        signalStrengthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        signalStrengthTextView.setTextAppearance(this, R.style.body_regular);
        signalStrengthTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        signalStrengthTextView.setLayoutParams(params);

        TextView detectionsTextView = new TextView(this);
        detectionsTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        detectionsTextView.setTextAppearance(this, R.style.body_regular);
        detectionsTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        detectionsTextView.setLayoutParams(params);

        newNonCoded.addView(periodTextView);
        newNonCoded.addView(pulseRateTextView);
        newNonCoded.addView(signalStrengthTextView);
        newNonCoded.addView(detectionsTextView);

        LinearLayout line = new LinearLayout(this);
        line.setBackgroundColor(ContextCompat.getColor(this, light_gray));
        line.setLayoutParams(line_view.getLayoutParams());

        scan_details_linearLayout.addView(newNonCoded);
        scan_details_linearLayout.addView(line);

        for (int i = scan_details_linearLayout.getChildCount() - 2; i > 3 ; i -= 2) {
            LinearLayout lastLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            LinearLayout penultimateLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i - 2);

            TextView lastPeriodTextView = (TextView) lastLinearLayout.getChildAt(0);
            TextView penultimatePeriodTextView = (TextView) penultimateLinearLayout.getChildAt(0);
            lastPeriodTextView.setText(penultimatePeriodTextView.getText());

            TextView lastPulseRateTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimatePulseRateTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastPulseRateTextView.setText(penultimatePulseRateTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newPeriodTextView = (TextView) linearLayout.getChildAt(0);
        TextView newPulseRateTextView = (TextView) linearLayout.getChildAt(1);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(2);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(3);

        newPeriodTextView.setText(String.valueOf(period));
        newPulseRateTextView.setText(String.valueOf(pulseRate));
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
        newDetectionsTextView.setText(String.valueOf(detections));
    }

    /**
     * Clears the screen to start displaying the data.
     */
    private void clear() {
        int count = scan_details_linearLayout.getChildCount();

        while (count > 2) {
            scan_details_linearLayout.removeViewAt(2);
            count--;
        }
    }

    /**
     * Displays a message indicating whether the writing was successful.
     *
     * @param data This packet indicates the writing status.
     */
    private void showMessage(byte[] data) {
        int status = Integer.parseInt(Converters.getDecimalValue(data[0]));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success!");
        if (status == 0)
            builder.setMessage("Completed.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}