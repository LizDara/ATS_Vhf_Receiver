package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.widget.TextView;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.light_blue;
import static com.atstrack.ats.ats_vhf_receiver.R.color.tall_poppy;

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
    @BindView(R.id.scan_rateD_stationary_textView)
    TextView scan_rateD_stationary_textView;
    @BindView(R.id.timeoutD_stationary_textView)
    TextView timeoutD_stationary_textView;
    @BindView(R.id.current_antenna_stationary_textView)
    TextView current_antenna_stationary_textView;
    @BindView(R.id.first_result_stationary_textView)
    TextView firstResultTextView;
    @BindView(R.id.second_result_stationary_textView)
    TextView secondResultTextView;
    @BindView(R.id.third_result_stationary_textView)
    TextView thirdResultTextView;
    @BindView(R.id.forth_result_stationary_textView)
    TextView forthResultTextView;

    private final static String TAG = StationaryScanActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private boolean state = true;
    private boolean response = true;

    private boolean scanning;
    private String currentData;

    private AnimationDrawable animationDrawable;

    private int selectedTable;
    private int numberAntennas;
    private int matches;
    private int txType;
    private int scanTime;
    private int timeout;
    private int storeTime;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int seconds;
    private int referenceFrequency;
    private int referenceFrequencyStoreRate;
    private int externalDataPush;
    private int pr1;
    private int pr1_tolerance;
    private int pr2;
    private int pr2_tolerance;
    private int pr3;
    private int pr3_tolerance;
    private int pr4;
    private int pr4_tolerance;
    private boolean mortality;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG,"Unable to initialize Bluetooth");
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

    private boolean mConnected = false;
    private String parameter = "";

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
                    state = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    switch (parameter) {
                        case "stationary": // Gets stationary defaults data
                            onClickStationary();
                            break;
                        case "startStationary": // Starts to scan
                            onClickStart();
                            break;
                        case "sendLog": // Receives the data
                            onClickLog();
                            break;
                        case "stopStationary": // Stops scan
                            onClickStop();
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
                        case "stopStationary": // Stops scan
                            showMessage(packet);
                            break;
                    }
                }
            }
            catch (Exception e) {
                Timber.tag("DCA:BR 198").e(e, "Unexpected error.");
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
        parameter = "sendLog";

        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);
        year = YY % 100;

        byte[] b = new byte[]{
                (byte) 0x83, (byte) (YY % 100), (byte) MM, (byte) DD, (byte) hh, (byte) mm, (byte) ss, (byte) selectedTable};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, true);

        scanning = true;
    }

    /**
     * Enables notification for receive the data.
     * Service name: Screen.
     * Characteristic name: SendLog.
     */
    private void onClickLog() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);
    }

    /**
     * Writes a value for stop scan.
     * Service name: Scan.
     * Characteristic name: Stationary.
     */
    private void onClickStop() {
        parameter = "stationary";
        byte[] b = new byte[]{(byte) 0x87};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, false);

        scanning = false;
        animationDrawable.stop();
        state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
        clear();
        stationary_result_linearLayout.setVisibility(View.GONE);
        ready_stationary_scan_LinearLayout.setVisibility(View.VISIBLE);
        title_toolbar.setText(R.string.stationary_scanning);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.edit_stationary_settings_button)
    public void onClickEditStationarySettings(View v) {
        Intent intent = new Intent(this, StationaryDefaultsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.start_stationary_button)
    public void onClickStartStationary(View v) {
        parameter = "startStationary";
        mBluetoothLeService.discovering();
        title_toolbar.setText(R.string.lb_stationary_scanning);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        ready_stationary_scan_LinearLayout.setVisibility(View.GONE);
        stationary_result_linearLayout.setVisibility(View.VISIBLE);
        state_view.setBackgroundResource(R.drawable.scanning_animation);
        animationDrawable = (AnimationDrawable) state_view.getBackground();
        animationDrawable.start();
    }

    @OnClick(R.id.stop_scanning_stationary_button)
    public void onClickStopScanning(View v) {
        parameter = "stopStationary";
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
        final Intent intent = getIntent();
        scanning = intent.getExtras().getBoolean("scanning");
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        mortality = false;

        if (scanning) { // The device is already scanning
            parameter = "sendLog";
            year = intent.getExtras().getInt("year");
            month = intent.getExtras().getInt("month");
            day = intent.getExtras().getInt("day");
            hour = intent.getExtras().getInt("hour");
            minute = intent.getExtras().getInt("minute");
            seconds = intent.getExtras().getInt("seconds");

            ready_stationary_scan_LinearLayout.setVisibility(View.GONE);
            stationary_result_linearLayout.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.lb_stationary_scanning);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

            state_view.setBackgroundResource(R.drawable.scanning_animation);
            animationDrawable = (AnimationDrawable) state_view.getBackground();
            animationDrawable.start();
        } else { // Gets aerial defaults data
            parameter = "stationary";
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!scanning) {
                Intent intent = new Intent(this, StationaryScanningActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                parameter = "stopStationary";
                mBluetoothLeService.discovering();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (scanning) { // Asks if you want to stop the scan
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stop Stationary");
            builder.setMessage("Are you sure you want to stop scanning?");
            builder.setPositiveButton("OK", (dialog, which) -> {
                parameter = "stopStationary";
                mBluetoothLeService.discovering();
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
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
            Log.d(TAG,"Connect request result= " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected && !state)
            showDisconnectionMessage();
        return true;
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view =inflater.inflate(R.layout.disconnect_message, null);
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
        if (!response) {
            showMessage(new byte[]{0});
            response = true;
        }
        if (data.length == 1) {
            parameter = "stationary";
            mBluetoothLeService.discovering();
        }
        if (Converters.getHexValue(data[0]).equals("6C")) {
            int frequencyTable = Integer.parseInt(Converters.getDecimalValue(data[1])) / 16;
            selectedTable = frequencyTable;
            if (frequencyTable == 0) { // There are no tables with frequencies to scan
                selected_frequency_stationary_textView.setText(R.string.lb_none);
                frequency_empty_textView.setVisibility(View.VISIBLE);
                start_stationary_button.setEnabled(false);
                start_stationary_button.setAlpha((float) 0.6);
            } else { // Shows the table to be scanned
                selected_frequency_stationary_textView.setText(String.valueOf(frequencyTable));
                frequency_empty_textView.setVisibility(View.GONE);
                start_stationary_button.setEnabled(true);
                start_stationary_button.setAlpha((float) 1);
            }
            number_antennas_stationary_textView.setText((numberAntennas == 0) ? "None" : String.valueOf(numberAntennas));
            scan_rate_stationary_textView.setText(Converters.getDecimalValue(data[3]));
            timeout_stationary_textView.setText(Converters.getDecimalValue(data[4]));
            if (Converters.getHexValue(data[5]).equals("FF")) {
                store_rateC_stationary_textView.setText("Continuous Store");
            } else {
                store_rateC_stationary_textView.setText((Converters.getDecimalValue(data[5]).equals("0")) ? "No store rate" :
                        Converters.getDecimalValue(data[5]));
            }
        }
    }

    /**
     * With the received packet, gets the data of scanning.
     *
     * @param data The received packet.
     */
    public void setCurrentLog(byte[] data) {
        currentData = "";
        String format = Converters.getHexValue(data[0]);
        switch (format) {
            case "83":
                Log.i(TAG, "83: " + Converters.getHexValue(data));
                startScanStationaryFirstPart(data);
                //logFreq(new byte[]{data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15]});//byte 0 = F0
                break;
            case "84":
                Log.i(TAG, "84: " + Converters.getHexValue(data));
                startScanStationarySecondPart(data);
                break;
            case "85":
                Log.i(TAG, "85: " + Converters.getHexValue(data));
                startScanStationaryThirdPart(data);
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "F1":
                logScanFix(data);
                break;
            case "F2":
                logCoded(data);
                break;
            case "E3":
                logFixedPulseRate(data);
                break;
            default: //E1 and E2
                logScanData(data);
                break;
        }
        refresh();
    }

    public void startScanStationaryFirstPart(byte[] data) {
        selectedTable = Integer.parseInt(Converters.getDecimalValue(data[1])) / 16;
        numberAntennas = Integer.parseInt(Converters.getDecimalValue(data[1])) % 16;
        matches = Integer.parseInt(Converters.getDecimalValue(data[2])) / 16;
        txType = Integer.parseInt(Converters.getDecimalValue(data[2])) % 16; // 1 = fixed pulse, 2 = variable pulse, 0 = eiler
        scanTime = Integer.parseInt(Converters.getDecimalValue(data[3]));
        timeout = Integer.parseInt(Converters.getDecimalValue(data[4]));
        storeTime = Integer.parseInt(Converters.getDecimalValue(data[5]));
        year = Integer.parseInt(Converters.getDecimalValue(data[6]));
        pr1 = Integer.parseInt(Converters.getDecimalValue(data[7]));
    }

    public void startScanStationarySecondPart(byte[] data) {
        referenceFrequency = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + 150000;
        referenceFrequencyStoreRate = Integer.parseInt(Converters.getDecimalValue(data[3]));
        externalDataPush = Integer.parseInt(Converters.getDecimalValue(data[4]));
    }

    public void startScanStationaryThirdPart(byte[] data) {
        pr1_tolerance = Integer.parseInt(Converters.getDecimalValue(data[1]));
        pr2 = Integer.parseInt(Converters.getDecimalValue(data[2]));
        pr2_tolerance = Integer.parseInt(Converters.getDecimalValue(data[3]));
        pr3 = Integer.parseInt(Converters.getDecimalValue(data[4]));
        pr3_tolerance = Integer.parseInt(Converters.getDecimalValue(data[5]));
        pr4 = Integer.parseInt(Converters.getDecimalValue(data[6]));
        pr4_tolerance = Integer.parseInt(Converters.getDecimalValue(data[7]));
    }

    /**
     * With the received packet, processes the data to display.
     *
     * @param data The received packet.
     */
    public void logScanHeader(byte[] data) {
        mortality = false;
        int freqOffset = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + 150000;
        int date = Converters.strToDecimal(Converters.getHexValue(data[4]) + Converters.getHexValue(data[5]) + Converters.getHexValue(data[6]));
        month = date / 1000000;
        date = date % 1000000;
        day = date / 10000;
        date = date % 10000;
        hour = date / 100;
        minute = date % 100;
        seconds = Integer.parseInt(Converters.getDecimalValue(data[7]));
        currentData += month + "/" + day + "/" + year + "       " + hour + ":" + minute + ":" + seconds;

        table_stationary_textView.setText(selectedTable + " (" + Converters.getDecimalValue(data[3]) + ")");
        frequency_stationary_textView.setText(String.valueOf(freqOffset).substring(0, 3) + "." + String.valueOf(freqOffset).substring(3));
        scan_rateD_stationary_textView.setText(String.valueOf(scanTime));
        timeoutD_stationary_textView.setText(String.valueOf(timeout));
        current_antenna_stationary_textView.setText((numberAntennas == 0) ? "All" : String.valueOf(numberAntennas));
    }

    /**
     * With the received packet, processes the data to display.
     *
     * @param data The received packet.
     */
    public void logScanFix(byte[] data) {
        mortality = (Integer.parseInt(Converters.getDecimalValue(data[5])) > 0);
        for (int i = 1; i < data.length; i++) {
            byte b = data[i];
            switch (i) {
                case 1:
                    currentData += "Sec:" + Converters.getDecimalValue(b) + " ";
                    break;
                case 2:
                    currentData += "A:" + (Integer.parseInt(Converters.getDecimalValue(b)) > 128 ?
                            Integer.parseInt(Converters.getDecimalValue(b)) - 128 : Converters.getDecimalValue(b)) + " ";//Si es mayor que x80 se resta x80
                    break;
                case 3:
                    currentData += "C:" +
                            ((Integer.parseInt(Converters.getDecimalValue(b)) < 10) ? "0" + Converters.getDecimalValue(b): Converters.getDecimalValue(b))
                            + (mortality ? "M " : " ");
                    break;
                case 4:
                    currentData += "SS:" + (Integer.parseInt(Converters.getDecimalValue(b)) + 200) + " ";
                    break;
                /*case 5:
                    currentData += "#:" + (mortality ? Integer.parseInt(Converters.getDecimalValue(b)) - 100 : Converters.getDecimalValue(b));
                    break;*/
            }
        }
    }

    public void logScanData(byte[] data) {
        int number = Integer.parseInt(Converters.getDecimalValue(data[5])) * 256;
        int match = 0;
        for (int i = 1; i < data.length; i++) {
            byte b = data[i];
            switch (i) {
                case 1:
                    currentData += "Sec:" + Converters.getDecimalValue(b) + " ";
                    break;
                case 2:
                    currentData += "A:" + (Integer.parseInt(Converters.getDecimalValue(b)) % 10) + " ";
                    match = Integer.parseInt(Converters.getDecimalValue(b)) / 10;
                    break;
                case 4:
                    currentData += "SS:" + (Integer.parseInt(Converters.getDecimalValue(b)) + 200) + " ";
                    break;
                case 6:
                    currentData += "Per:" + (number + Integer.parseInt(Converters.getDecimalValue(b))) + " ";
                    currentData += "Match:" + match + " ";
                    break;
                case 7:
                    currentData += "#:" + Converters.getDecimalValue(b);
                    break;
            }
        }
    }

    public void logCoded(byte[] data) {
        mortality = (Integer.parseInt(Converters.getDecimalValue(data[5])) > 0);
        for (int i = 1; i < data.length; i++) {
            byte b = data[i];
            switch (i) {
                case 2:
                    currentData += "A:" + (Integer.parseInt(Converters.getDecimalValue(b)) > 128 ?
                            Integer.parseInt(Converters.getDecimalValue(b)) - 128 : Converters.getDecimalValue(b)) + " ";
                    break;
                case 3:
                    currentData += "C:" +
                            ((Integer.parseInt(Converters.getDecimalValue(b)) < 10) ? "0" + Converters.getDecimalValue(b): Converters.getDecimalValue(b))
                            + (mortality ? "M " : " ");
                    break;
                case 4:
                    currentData += "SS:" + (Integer.parseInt(Converters.getDecimalValue(b)) + 200) + " ";
                    break;
                /*case 5:
                    currentData += "#:" + (mortality ? Integer.parseInt(Converters.getDecimalValue(b)) - 100 : Converters.getDecimalValue(b));
                    break;*/
            }
        }
    }

    public void logFixedPulseRate(byte[] data) {
        int number = Integer.parseInt(Converters.getDecimalValue(data[5])) * 256;
        int match = 0;
        for (int i = 1; i < data.length; i++) {
            byte b = data[i];
            switch (i) {
                case 2:
                    currentData += "A:" + (Integer.parseInt(Converters.getDecimalValue(b)) % 10) + " ";
                    match = Integer.parseInt(Converters.getDecimalValue(b)) / 10;
                    break;
                case 4:
                    currentData += "SS:" + (Integer.parseInt(Converters.getDecimalValue(b)) + 200) + " ";
                    break;
                case 6:
                    currentData += "Per:" + (number + Integer.parseInt(Converters.getDecimalValue(b))) + " ";
                    currentData += "Match:" + match + " ";
                    break;
                case 7:
                    currentData += "#:" + Converters.getDecimalValue(b);
                    break;
            }
        }
    }

    /**
     * Updates the data displayed on the screen.
     */
    public void refresh() {
        forthResultTextView.setText(thirdResultTextView.getText());
        forthResultTextView.setTextColor(thirdResultTextView.getText().toString().contains("M") ?
                ContextCompat.getColor(this, tall_poppy) : ContextCompat.getColor(this, light_blue));

        thirdResultTextView.setText(secondResultTextView.getText());
        thirdResultTextView.setTextColor(secondResultTextView.getText().toString().contains("M") ?
                ContextCompat.getColor(this, tall_poppy) : ContextCompat.getColor(this, light_blue));

        secondResultTextView.setText(firstResultTextView.getText());
        secondResultTextView.setTextColor(firstResultTextView.getText().toString().contains("M") ?
                ContextCompat.getColor(this, tall_poppy) : ContextCompat.getColor(this, light_blue));

        firstResultTextView.setText(currentData);
        firstResultTextView.setTextColor(mortality ?
                ContextCompat.getColor(this, tall_poppy) : ContextCompat.getColor(this, light_blue));
    }

    /**
     * Clears the screen to start displaying the data.
     */
    public void clear() {
        firstResultTextView.setText("");
        secondResultTextView.setText("");
        thirdResultTextView.setText("");
        forthResultTextView.setText("");
    }

    /**
     * Displays a message indicating whether the writing was successful.
     *
     * @param data This packet indicates the writing status.
     */
    private void showMessage(byte[] data) {
        int status = Integer.parseInt(Converters.getDecimalValue(data[0]));

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Success!");
        if (status == 0)
            builder.setMessage("Completed.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}