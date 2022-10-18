package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

import static com.atstrack.ats.ats_vhf_receiver.R.color.catskill_white;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;

public class ManualScanActivity extends AppCompatActivity {

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
    @BindView(R.id.ready_manual_scan_linearLayout)
    LinearLayout ready_manual_scan_LinearLayout;
    @BindView(R.id.change_frequency_linearLayout)
    LinearLayout change_frequency_linearLayout;
    @BindView(R.id.frequency_manual_textView)
    TextView frequency_manual_textView;
    @BindView(R.id.manual_gps_switch)
    SwitchCompat manual_gps_switch;
    @BindView(R.id.start_manual_button)
    Button start_manual_button;
    @BindView(R.id.enter_frequency_editText)
    EditText enter_frequency_editText;
    @BindView(R.id.ready_to_scan_manual_button)
    Button ready_to_scan_manual_button;
    @BindView(R.id.manual_scan_linearLayout)
    LinearLayout manual_scan_linearLayout;
    @BindView(R.id.frequency_scan_manual_textView)
    TextView frequency_scan_manual_textView;
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
    @BindView(R.id.record_data_manual_button)
    Button record_data_manual_button;

    private final static String TAG = ManualScanActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    private boolean isScanning;
    private boolean isEditFrequency;

    private int baseFrequency;
    private int range;
    private byte detectionType;
    private int newFrequency;
    private int code;
    private int detections;
    private int mort;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int seconds;

    private AnimationDrawable animationDrawable;

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
                        case "startManual": // Starts to scan
                            onClickStart();
                            break;
                        case "sendLog": // Receives the data
                            onClickLog();
                            break;
                        case "stopManual": // Stops scan
                            onClickStop();
                            break;
                        case "recordData": // Records a code
                            onClickRecord();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("sendLog")) // Receives the data
                        setCurrentLog(packet);
                }
            }
            catch (Exception e) {
                Timber.tag("DCA:BR 198").e(e, "Unexpected error.");
            }
        }
    };

    /**
     * Change the period while editing the pulse rate.
     */
    private TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (enter_frequency_editText.getText().toString().isEmpty()) {
                ready_to_scan_manual_button.setEnabled(false);
                ready_to_scan_manual_button.setAlpha((float) 0.6);
            } else {
                ready_to_scan_manual_button.setEnabled(true);
                ready_to_scan_manual_button.setAlpha(1);
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
     * Writes the aerial scan data for start to scan.
     * Service name: Scan.
     * Characteristic name: Manual.
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

        byte[] b = new byte[] {(byte) 0x86, (byte) ((newFrequency - (baseFrequency * 1000)) / 256),
                (byte) ((newFrequency - (baseFrequency * 1000)) % 256), (byte) (manual_gps_switch.isChecked() ? 0x80 : 0x0),
                (byte) year, (byte) MM, (byte) DD, (byte) hh, (byte) mm, (byte) ss};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, true);
        Log.i(TAG, "Start Data "+Converters.getDecimalValue(b));

        isScanning = true;
        title_toolbar.setText(R.string.lb_manual_scanning);
        ready_manual_scan_LinearLayout.setVisibility(View.GONE);
        manual_scan_linearLayout.setVisibility(View.VISIBLE);
        record_data_manual_button.setVisibility(View.VISIBLE);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        state_view.setBackgroundResource(R.drawable.scanning_animation);
        animationDrawable = (AnimationDrawable) state_view.getBackground();
        animationDrawable.start();

        int visibility = (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);
        frequency_scan_manual_textView.setText((String.valueOf(newFrequency).substring(0, 3) + "." + String.valueOf(newFrequency).substring(3)));
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
        Log.i(TAG, "Log Data");
    }

    /**
     * Writes a value for stop scan.
     * Service name: Scan.
     * Characteristic name: Manual.
     */
    private void onClickStop() {
        parameter = "";
        byte[] b = new byte[] {(byte) 0x87};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, false);
        Log.i(TAG, "Stop Data");

        clear();
        isScanning = false;
        animationDrawable.stop();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        manual_scan_linearLayout.setVisibility(View.GONE);
        record_data_manual_button.setAlpha((float) 1);
        record_data_manual_button.setEnabled(true);
        record_data_manual_button.setVisibility(View.GONE);
        state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));

        if (isEditFrequency) {
            isEditFrequency = false;
            title_toolbar.setText(R.string.lb_change_frequency);
            change_frequency_linearLayout.setVisibility(View.VISIBLE);
        } else {
            title_toolbar.setText(R.string.manual_scanning);
            ready_manual_scan_LinearLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Records the specific code information, the code received.
     */
    public void onClickRecord() {
        parameter = "sendLog";
        byte[] b = new byte[] {(byte) 0x8C, (byte) detections, (byte) mort, (byte) code};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, true);
        Log.i(TAG, "RECORD: " + Converters.getDecimalValue(b));

        record_data_manual_button.setAlpha((float) 0.6);
        record_data_manual_button.setEnabled(false);
    }

    @OnClick(R.id.enter_new_frequency_button)
    public void onClickEnterNewFrequency(View v) {
        title_toolbar.setText(R.string.lb_change_frequency);
        change_frequency_linearLayout.setVisibility(View.VISIBLE);
        ready_manual_scan_LinearLayout.setVisibility(View.GONE);
        enter_frequency_editText.setText("");
        if (enter_frequency_editText.getText().toString().isEmpty()) {
            ready_to_scan_manual_button.setEnabled(false);
            ready_to_scan_manual_button.setAlpha((float) 0.6);
        } else {
            ready_to_scan_manual_button.setEnabled(true);
            ready_to_scan_manual_button.setAlpha((float) 1);
        }
    }

    @OnClick(R.id.ready_to_scan_manual_button)
    public void onClickReadyToScan(View v) {
        newFrequency = (enter_frequency_editText.getText().toString().isEmpty()) ? 0 : Integer.parseInt(enter_frequency_editText.getText().toString());
        if (newFrequency > (baseFrequency * 1000) && newFrequency < (baseFrequency + range) * 1000) {
            frequency_manual_textView.setText(String.valueOf(newFrequency).substring(0, 3) + "." + String.valueOf(newFrequency).substring(3));
            title_toolbar.setText(R.string.manual_scanning);
            change_frequency_linearLayout.setVisibility(View.GONE);
            ready_manual_scan_LinearLayout.setVisibility(View.VISIBLE);
            start_manual_button.setEnabled(true);
            start_manual_button.setAlpha(1);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Invalid Format or Values");
            builder.setMessage("Please enter valid frequency values.");
            builder.setPositiveButton("Ok", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(catskill_white)));
        }
    }

    @OnClick(R.id.start_manual_button)
    public void onClickStartManual(View v) {
        parameter = "startManual";
        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.edit_frequency_button)
    public void onClickEditFrequency(View v) {
        parameter = "stopManual";
        isEditFrequency = true;
        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.record_data_manual_button)
    public void onClickRecordData(View v) {
        parameter = "recordData";
        mBluetoothLeService.discovering();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_scan);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.manual_scanning);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        isScanning = false;
        isEditFrequency = false;

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        isScanning = getIntent().getExtras().getBoolean("scanning");
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        enter_frequency_editText.addTextChangedListener(textChangedListener);
        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        baseFrequency = sharedPreferences.getInt("BaseFrequency", 0);
        range = sharedPreferences.getInt("Range", 0);
        detectionType =(byte) sharedPreferences.getInt("DetectionType", 0);

        if (isScanning) { // The device is already scanning
            parameter = "sendLog";
            year = getIntent().getExtras().getInt("year");
            month = getIntent().getExtras().getInt("month");
            day = getIntent().getExtras().getInt("day");
            hour = getIntent().getExtras().getInt("hour");
            minute = getIntent().getExtras().getInt("minute");
            seconds = getIntent().getExtras().getInt("seconds");

            ready_manual_scan_LinearLayout.setVisibility(View.GONE);
            manual_scan_linearLayout.setVisibility(View.VISIBLE);
            record_data_manual_button.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.lb_aerial_scanning);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

            state_view.setBackgroundResource(R.drawable.scanning_animation);
            animationDrawable = (AnimationDrawable) state_view.getBackground();
            animationDrawable.start();

            int visibility = (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) ? View.GONE : View.VISIBLE;
            code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            period_textView.setVisibility(visibility);
            pulse_rate_textView.setVisibility(visibility);
        } else { // Gets manual defaults data
            start_manual_button.setEnabled(false);
            start_manual_button.setAlpha((float) 0.6);
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                if (change_frequency_linearLayout.getVisibility() == View.VISIBLE) {
                    change_frequency_linearLayout.setVisibility(View.GONE);
                    ready_manual_scan_LinearLayout.setVisibility(View.VISIBLE);
                } else {
                    Intent intent = new Intent(this, StartScanningActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            } else {
                parameter = "stopManual";
                mBluetoothLeService.discovering();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected )
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
     * With the received packet, gets the data of scanning.
     *
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        int frequency = (baseFrequency * 1000) +
                ((Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) + (Integer.parseInt(Converters.getDecimalValue(data[2]))));
        frequency_scan_manual_textView.setText((String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3)));

        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[3]));

        if (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) {
            code = Integer.parseInt(Converters.getDecimalValue(data[4]));
            int mort = Integer.parseInt(Converters.getDecimalValue(data[5]));
            int position;

            if (scan_details_linearLayout.getChildCount() > 2 && isEqualFirstCode(code)) {
                refreshFirstCode(signalStrength, mort > 0);
            } else if ((position = positionCode(code)) != 0) {
                refreshPosition(position, signalStrength, mort > 0);
            } else {
                detections = 1;
                createCodeDetail(code, signalStrength, detections, mort > 0);
            }
        } else {
            int period = (Integer.parseInt(Converters.getDecimalValue(data[4])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[5]));
            double pulseRate = (double) 60000 / period;

            refreshNonCoded(period, pulseRate, signalStrength, detections);
        }
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
        codeTextView.setTextAppearance(this, R.style.body_regular);
        codeTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        codeTextView.setLayoutParams(params);

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

            if (Integer.parseInt(textView.getText().toString().replace(" M", "")) == code) position = i;
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
        for (int i = finalPosition; i > 3; i -= 2) {
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
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newCodeTextView = (TextView) linearLayout.getChildAt(0);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(1);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(2);
        TextView newMortTextView = (TextView) linearLayout.getChildAt(3);

        newCodeTextView.setText(code + (isMort ? " M" : ""));
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
        newDetectionsTextView.setText(String.valueOf(detections));

        newMortTextView.setText(isMort ? mort + 1 : mort);
        this.detections = detections;
        this.mort = Integer.parseInt(newMortTextView.getText().toString());
    }

    /**
     * Creates a row of non code data and display it.
     *
     * @param period Number of period received.
     * @param pulseRate Number of pulse rate received.
     * @param signalStrength Number of signal strength received.
     * @param detections Number of detections received.
     */
    private void refreshNonCoded(int period, double pulseRate, int signalStrength, int detections) {
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
        newPulseRateTextView.setText(String.format("%.2f", pulseRate));
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
        newDetectionsTextView.setText(String.valueOf(detections));
    }

    /**
     * Clears the screen to start displaying the data.
     */
    private void clear() {
        frequency_scan_manual_textView.setText("");
        int count = scan_details_linearLayout.getChildCount();

        while (count > 2) {
            scan_details_linearLayout.removeViewAt(2);
            count--;
        }
    }
}
