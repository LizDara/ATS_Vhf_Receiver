package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ghost;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.slate_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.tall_poppy;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.border;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease_light;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase_light;
import static com.atstrack.ats.ats_vhf_receiver.R.style.body_regular;

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
    @BindView(R.id.frequency_manual_textView)
    TextView frequency_manual_textView;
    @BindView(R.id.manual_gps_switch)
    SwitchCompat manual_gps_switch;
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
    @BindView(R.id.audio_manual_linearLayout)
    LinearLayout audio_manual_linearLayout;
    @BindView(R.id.minus_imageView)
    ImageView minus_imageView;
    @BindView(R.id.plus_imageView)
    ImageView plus_imageView;
    @BindView(R.id.saving_targets_textView)
    TextView saving_targets_textView;

    private final static String TAG = ManualScanActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private static final int WAITING_PERIOD = 1000;

    private boolean isScanning;
    private boolean isEditFrequency;

    private int baseFrequency;
    private int frequencyRange;
    private int range;
    private byte detectionType;
    private int newFrequency;
    private int code;
    private int detections;
    private int mort;

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
                    if ("sendLog".equals(parameter)) { // Receives the data
                        onClickLog();
                    } else if ("sendLogScanning".equals(parameter)) {
                        onClickLogScanning();
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("sendLog")) // Receives the data
                        setCurrentLog(packet);
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
                        case "startManual": // Starts to scan
                            onClickStart();
                            break;
                        case "stopManual": // Stops scan
                            onClickStop();
                            break;
                        case "recordData": // Records a code
                            onClickRecord();
                            break;
                        case "decrease":
                            onClickDecrease();
                            break;
                        case "increase":
                            onClickIncrease();
                            break;
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode()) {
                    setVisibility("overview");
                }
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    newFrequency = result.getData().getExtras().getInt("frequency");
                    frequency_manual_textView.setText(getFrequency(newFrequency));
                    parameter = "sendLog";
                    mBluetoothLeService.discovering();
                }
            });

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
     * Writes the aerial scan data for start to scan.
     * Service name: Scan.
     * Characteristic name: Manual.
     */
    private void onClickStart() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);

        byte[] b = new byte[] {(byte) 0x86, (byte) ((newFrequency - baseFrequency) / 256),
                (byte) ((newFrequency - baseFrequency) % 256), (byte) (manual_gps_switch.isChecked() ? 0x80 : 0x0),
                (byte) (YY % 100), (byte) MM, (byte) DD, (byte) hh, (byte) mm, (byte) ss};

        Log.i(TAG, "On Click Start Manual: " + Converters.getHexValue(b));
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        isScanning = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (isScanning) {
            parameterWrite = "";
            setVisibility("scanning");

            int visibility = (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) ? View.GONE : View.VISIBLE;
            code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            period_textView.setVisibility(visibility);
            pulse_rate_textView.setVisibility(visibility);
            frequency_scan_manual_textView.setText(getFrequency(newFrequency));
        }
    }

    /**
     * Enables notification for receive the data.
     * Service name: Screen.
     * Characteristic name: SendLog.
     */
    private void onClickLog() {
        parameterWrite = "startManual";

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
     * Characteristic name: Manual.
     */
    private void onClickStop() {
        byte[] b = new byte[] {(byte) 0x87};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            clear();
            isScanning = false;

            if (isEditFrequency) {
                isEditFrequency = false;
                Intent intent = new Intent(this, EnterFrequencyActivity.class);
                intent.putExtra("title", "Change Frequency");
                intent.putExtra("baseFrequency", baseFrequency);
                intent.putExtra("range", range);
                launcher.launch(intent);
            } else {
                animationDrawable.stop();
                setVisibility("overview");
            }
            parameter = "";
        }
    }

    /**
     * Records the specific code information, the code received.
     */
    private void onClickRecord() {
        byte[] b = new byte[] {(byte) 0x8C, (byte) detections, (byte) mort, (byte) code};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);
        Log.i(TAG, "RECORD: " + Converters.getDecimalValue(b));

        if (result) {
            saving_targets_textView.setVisibility(View.GONE);
            record_data_manual_button.setAlpha(1);
            record_data_manual_button.setEnabled(true);
        }
    }

    private void onClickLogScanning() {
        parameter = "sendLog";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);
    }

    private void onClickDecrease() {
        byte[] b = new byte[] {(byte) 0x5E, (byte) ((newFrequency - baseFrequency) / 256), (byte) ((newFrequency - baseFrequency) % 256)};
        Log.i(TAG, "DECREASE FREQUENCY AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            if (newFrequency == baseFrequency) {
                minus_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease_light));
                minus_imageView.setEnabled(false);
            } else if (newFrequency == frequencyRange - 1) {
                plus_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase));
                plus_imageView.setEnabled(true);
            }
        }
    }

    private void onClickIncrease() {
        byte[] b = new byte[] {(byte) 0x5F, (byte) ((newFrequency - baseFrequency) / 256), (byte) ((newFrequency - baseFrequency) % 256)};
        Log.i(TAG, "INCREASE FREQUENCY AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            if (newFrequency == baseFrequency + 1) {
                minus_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease));
                minus_imageView.setEnabled(true);
            } else if (newFrequency == frequencyRange) {
                plus_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase_light));
                plus_imageView.setEnabled(false);
            }
        }
    }

    @OnClick(R.id.enter_new_frequency_button)
    public void onClickEnterNewFrequency(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra("title", "Change Frequency");
        intent.putExtra("baseFrequency", baseFrequency);
        intent.putExtra("range", range);
        launcher.launch(intent);
    }

    @OnClick(R.id.start_manual_button)
    public void onClickStartManual(View v) {
        parameter = "sendLog";
        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.edit_frequency_button)
    public void onClickEditFrequency(View v) {
        isEditFrequency = true;
        parameterWrite = "stopManual";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.record_data_manual_button)
    public void onClickRecordData(View v) {
        parameterWrite = "recordData";
        mBluetoothLeService.discoveringSecond();

        saving_targets_textView.setVisibility(View.VISIBLE);
        record_data_manual_button.setAlpha((float) 0.6);
        record_data_manual_button.setEnabled(false);
    }

    @OnClick(R.id.minus_imageView)
    public void onClickMinus(View v) {
        newFrequency = getFrequencyNumber(frequency_scan_manual_textView.getText().toString()) - 1;
        parameterWrite = "decrease";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.plus_imageView)
    public void onClickPlus(View v) {
        newFrequency = getFrequencyNumber(frequency_scan_manual_textView.getText().toString()) + 1;
        parameterWrite = "increase";
        mBluetoothLeService.discoveringSecond();
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

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        isScanning = getIntent().getExtras().getBoolean("scanning");
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        baseFrequency = sharedPreferences.getInt("BaseFrequency", 0) * 1000;
        range = sharedPreferences.getInt("Range", 0);
        detectionType = (byte) sharedPreferences.getInt("DetectionType", 0);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        isEditFrequency = false;

        int visibility = (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        audio_manual_linearLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);

        frequency_manual_textView.setText(getFrequency(baseFrequency));

        if (isScanning) { // The device is already scanning
            parameter = "sendLogScanning";
            setVisibility("scanning");
        } else { // Gets manual defaults data
            setVisibility("overview");
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                Intent intent = new Intent(this, StartScanningActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                parameterWrite = "stopManual";
                mBluetoothLeService.discoveringSecond();
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

    private void setVisibility(String value) {
        switch (value) {
            case "overview":
                ready_manual_scan_LinearLayout.setVisibility(View.VISIBLE);
                manual_scan_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.manual_scanning);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
                device_name_textView.setText(receiverInformation.getDeviceName());
                break;
            case "scanning":
                ready_manual_scan_LinearLayout.setVisibility(View.GONE);
                manual_scan_linearLayout.setVisibility(View.VISIBLE);
                title_toolbar.setText(R.string.lb_manual_scanning);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
                state_view.setBackgroundResource(R.drawable.scanning_animation);
                animationDrawable = (AnimationDrawable) state_view.getBackground();
                animationDrawable.start();
                device_name_textView.setText(receiverInformation.getDeviceName().replace("Not scanning", "Scanning, manual"));
                break;
        }
    }

    private String getFrequency(int frequency) {
        return String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3);
    }

    private int getFrequencyNumber(String frequency) {
        return Integer.parseInt(frequency.replace(".", ""));
    }

    /**
     * With the received packet, gets the data of scanning.
     *
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        Log.i(TAG, Converters.getHexValue(data));
        if (Converters.getHexValue(data[0]).equals("A1"))
            return;
        int frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                (Integer.parseInt(Converters.getDecimalValue(data[2]))));
        frequency_scan_manual_textView.setText(getFrequency(frequency));
        frequency_manual_textView.setText(getFrequency(frequency));

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
        codeTextView.setTextAppearance(body_regular);
        codeTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        codeTextView.setLayoutParams(params);

        TextView detectionsTextView = new TextView(this);
        detectionsTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        detectionsTextView.setTextAppearance(body_regular);
        detectionsTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        detectionsTextView.setLayoutParams(params);

        TextView mortalityTextView = new TextView(this);
        mortalityTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        mortalityTextView.setTextAppearance(body_regular);
        mortalityTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        mortalityTextView.setLayoutParams(params);

        TextView signalStrengthTextView = new TextView(this);
        signalStrengthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        signalStrengthTextView.setTextAppearance(body_regular);
        signalStrengthTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        signalStrengthTextView.setLayoutParams(params);

        TextView mortTextView = new TextView(this);
        mortTextView.setVisibility(View.GONE);

        newCode.addView(codeTextView);
        newCode.addView(detectionsTextView);
        newCode.addView(mortalityTextView);
        newCode.addView(signalStrengthTextView);
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

        return Integer.parseInt(codeTextView.getText().toString()) == code;
    }

    /**
     * Updates data in the first row in the table.
     *
     * @param signalStrength Number of signal strength to update.
     */
    private void refreshFirstCode(int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        mortalityTextView.setText(isMort ? "M" : "-");
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
            TextView codeTextView = (TextView) linearLayout.getChildAt(0);

            if (Integer.parseInt(codeTextView.getText().toString()) == code)
                position = i;
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
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        int code = Integer.parseInt(codeTextView.getText().toString());
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

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastMortalityTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimateMortalityTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastMortalityTextView.setText(penultimateMortalityTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());

            TextView lastMortTextView = (TextView) lastLinearLayout.getChildAt(4);
            TextView penultimateMortTextView = (TextView) penultimateLinearLayout.getChildAt(4);
            lastMortTextView.setText(penultimateMortTextView.getText());
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newCodeTextView = (TextView) linearLayout.getChildAt(0);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView newMortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView newMortTextView = (TextView) linearLayout.getChildAt(4);

        newCodeTextView.setText(String.valueOf(code));
        newDetectionsTextView.setText(String.valueOf(detections));
        newMortalityTextView.setText(isMort ? "M" : "-");
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
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
    private void refreshNonCoded(int period, double pulseRate, int signalStrength, int detections) {
        LinearLayout newNonCoded = new LinearLayout(this);
        newNonCoded.setOrientation(LinearLayout.HORIZONTAL);
        newNonCoded.setPadding(0, 8, 0, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;

        TextView periodTextView = new TextView(this);
        periodTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        periodTextView.setTextAppearance(body_regular);
        periodTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        periodTextView.setLayoutParams(params);

        TextView detectionsTextView = new TextView(this);
        detectionsTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        detectionsTextView.setTextAppearance(body_regular);
        detectionsTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        detectionsTextView.setLayoutParams(params);

        TextView pulseRateTextView = new TextView(this);
        pulseRateTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        pulseRateTextView.setTextAppearance(body_regular);
        pulseRateTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        pulseRateTextView.setLayoutParams(params);

        TextView signalStrengthTextView = new TextView(this);
        signalStrengthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        signalStrengthTextView.setTextAppearance(body_regular);
        signalStrengthTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        signalStrengthTextView.setLayoutParams(params);

        newNonCoded.addView(periodTextView);
        newNonCoded.addView(detectionsTextView);
        newNonCoded.addView(pulseRateTextView);
        newNonCoded.addView(signalStrengthTextView);

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

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastPulseRateTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimatePulseRateTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastPulseRateTextView.setText(penultimatePulseRateTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newPeriodTextView = (TextView) linearLayout.getChildAt(0);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView newPulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(3);

        newPeriodTextView.setText(String.valueOf(period));
        newDetectionsTextView.setText(String.valueOf(detections));
        newPulseRateTextView.setText(String.valueOf(pulseRate));
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
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
