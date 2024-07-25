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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import static com.atstrack.ats.ats_vhf_receiver.R.color.catskill_white;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.limed_spruce;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_tertiary;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease_light;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase_light;
import static com.atstrack.ats.ats_vhf_receiver.R.style.body_regular;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_audio;

public class ManualScanActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
    @BindView(R.id.device_range_textView)
    TextView device_range_textView;
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
    @BindView(R.id.mortality_textView)
    TextView mortality_textView;
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
    @BindView(R.id.gps_manual_imageView)
    ImageView gps_manual_imageView;
    @BindView(R.id.state_manual_textView)
    TextView state_manual_textView;

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
    private byte[] audioOption = {(byte) 0x5A, 0, 0};

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
                        case "audio":
                            onClickAudio();
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

        byte[] b = new byte[] {(byte) 0x86, (byte) (YY % 100), (byte) MM, (byte) DD, (byte) hh, (byte) mm, (byte) ss,
                (byte) ((newFrequency - baseFrequency) / 256), (byte) ((newFrequency - baseFrequency) % 256),
                (byte) (manual_gps_switch.isChecked() ? 0x80 : 0x0)};

        Log.i(TAG, "On Click Start Manual " + newFrequency + ": " + Converters.getHexValue(b));
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        isScanning = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (isScanning) {
            parameterWrite = "";
            setVisibility("scanning");
            frequency_scan_manual_textView.setText(getFrequency(newFrequency));
            setGpsOff(R.string.lb_searching_gps);
            state_manual_textView.setVisibility(manual_gps_switch.isChecked() ? View.VISIBLE : View.GONE);
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
        byte[] b = new byte[] {(byte) 0x8C};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_MANUAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            state_manual_textView.setVisibility(View.GONE);
            record_data_manual_button.setAlpha(1);
            record_data_manual_button.setEnabled(true);
            clear();
        }
    }

    private void onClickLogScanning() {
        parameter = "sendLog";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);
    }

    private void onClickDecrease() {
        byte[] b = new byte[] {(byte) 0x5E};

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
        byte[] b = new byte[] {(byte) 0x5F};

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

    private void onClickAudio() {
        byte[] b;
        if (Converters.getHexValue(audioOption[0]).equals("59"))
            b = new byte[] {audioOption[0], audioOption[1], audioOption[2]};
        else
            b = new byte[] {audioOption[0], audioOption[2]};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);
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

        state_manual_textView.setVisibility(View.VISIBLE);
        state_manual_textView.setText(R.string.lb_saving_targets);
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

    @OnClick(R.id.edit_audio_manual_textView)
    public void onClickEditAudio(View v) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.audio_options, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        ImageButton close = view.findViewById(R.id.close_imageButton);
        Button single = view.findViewById(R.id.single_button);
        Button all = view.findViewById(R.id.all_button);
        Button none = view.findViewById(R.id.none_button);
        LinearLayout enterDigit = view.findViewById(R.id.enter_digit_linearLayout);
        TextView number = view.findViewById(R.id.digit_textView);
        Button one = view.findViewById(R.id.one_button);
        Button two = view.findViewById(R.id.two_button);
        Button three = view.findViewById(R.id.three_button);
        Button four = view.findViewById(R.id.four_button);
        Button five = view.findViewById(R.id.five_button);
        Button six = view.findViewById(R.id.six_button);
        Button seven = view.findViewById(R.id.seven_button);
        Button eight = view.findViewById(R.id.eight_button);
        Button nine = view.findViewById(R.id.nine_button);
        Button zero = view.findViewById(R.id.zero_button);
        ImageView delete = view.findViewById(R.id.delete_imageView);
        SwitchCompat background = view.findViewById(R.id.play_background_signals_switch);
        Button saveChanges = view.findViewById(R.id.save_digit_button);
        close.setOnClickListener(v1 -> dialog.dismiss());
        single.setOnClickListener(v14 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            single.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            all.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            none.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            enterDigit.setVisibility(View.VISIBLE);
            number.setText("");
            audioOption[0] = (byte) 0x59;
        });
        all.setOnClickListener(v15 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            single.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            all.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            none.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            enterDigit.setVisibility(View.GONE);
            audioOption[0] = (byte) 0x5A;
        });
        none.setOnClickListener(v16 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            single.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            all.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            none.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            enterDigit.setVisibility(View.GONE);
            audioOption[0] = (byte) 0x5B;
        });
        View.OnClickListener clickListener = v17 -> {
            String text = number.getText().toString();
            Button buttonNumber = (Button) v17;
            number.setText(text + buttonNumber.getText());
        };
        one.setOnClickListener(clickListener);
        two.setOnClickListener(clickListener);
        three.setOnClickListener(clickListener);
        four.setOnClickListener(clickListener);
        five.setOnClickListener(clickListener);
        six.setOnClickListener(clickListener);
        seven.setOnClickListener(clickListener);
        eight.setOnClickListener(clickListener);
        nine.setOnClickListener(clickListener);
        zero.setOnClickListener(clickListener);
        delete.setOnClickListener(v13 -> {
            if (!number.getText().toString().isEmpty()) {
                String text = number.getText().toString();
                number.setText(text.substring(0, text.length() - 1));
            }
        });
        saveChanges.setOnClickListener(v12 -> {
            audioOption[1] = Converters.getHexValue(audioOption[0]).equals("59") ? (byte) Integer.parseInt(number.getText().toString()) : 0;
            audioOption[2] = background.isChecked() ? (byte) 1 : 0;
            parameterWrite = "audio";
            mBluetoothLeService.discoveringSecond();

            dialog.dismiss();
        });
        dialog.setView(view);
        dialog.show();
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

        device_status_textView.setText(receiverInformation.getDeviceStatus());
        device_range_textView.setText(receiverInformation.getDeviceRange());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        baseFrequency = sharedPreferences.getInt("BaseFrequency", 0) * 1000;
        range = sharedPreferences.getInt("Range", 0);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        isEditFrequency = false;

        frequency_manual_textView.setText(getFrequency(baseFrequency));
        newFrequency = baseFrequency;
        if (isScanning) { // The device is already scanning
            parameter = "sendLogScanning";
            detectionType = getIntent().getExtras().getByte("detectionType");
            frequency_scan_manual_textView.setText(getFrequency(newFrequency));

            updateVisibility();
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
                device_status_textView.setText(receiverInformation.getDeviceStatus());
                break;
            case "scanning":
                ready_manual_scan_LinearLayout.setVisibility(View.GONE);
                manual_scan_linearLayout.setVisibility(View.VISIBLE);
                title_toolbar.setText(R.string.lb_manual_scanning);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
                state_view.setBackgroundResource(R.drawable.scanning_animation);
                animationDrawable = (AnimationDrawable) state_view.getBackground();
                animationDrawable.start();
                device_status_textView.setText(receiverInformation.getDeviceStatus().replace("Not scanning", "Scanning, manual"));
                break;
        }
    }

    private void updateVisibility() {
        int visibility = Converters.getHexValue(detectionType).equals("09") ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        mortality_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        audio_manual_linearLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);
    }

    private String getFrequency(int frequency) {
        return String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3);
    }

    private int getFrequencyNumber(String frequency) {
        return Integer.parseInt(frequency.replace(".", ""));
    }

    private void setGpsOff(int idString) {
        gps_manual_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_off));
        state_manual_textView.setText(idString);
    }

    private void setGpsOn() {
        gps_manual_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_on));
        state_manual_textView.setText(R.string.lb_valid_gps);
    }

    /**
     * With the received packet, gets the data of scanning.
     *
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        Log.i(TAG, Converters.getHexValue(data));
        if (Converters.getHexValue(data[0]).equals("50")) {
            scanState(data);
        } else if (Converters.getHexValue(data[0]).equals("51")) {
            gpsState(data);
        } else if (Converters.getHexValue(data[0]).equals("F0")) {
            clear();
            int frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                    (Integer.parseInt(Converters.getDecimalValue(data[2]))));
            frequency_scan_manual_textView.setText(getFrequency(frequency));
            frequency_manual_textView.setText(getFrequency(frequency));
        } else if (Converters.getHexValue(data[0]).equals("D0") || Converters.getHexValue(data[0]).equals("E0")) {
            int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[3]));

            if (Converters.getHexValue(detectionType).equals("09")) { // Coded
                code = Integer.parseInt(Converters.getDecimalValue(data[4]));
                int mortality = Integer.parseInt(Converters.getDecimalValue(data[5]));
                int position = getPositionNumber(code, 0);
                if (position > 0) {
                    refreshCodedPosition(position, signalStrength, mortality > 0);
                } else if (position < 0) {
                    detections = 1;
                    createDetail();
                    addNewCodedDetailInPosition(-position, code, signalStrength, detections, mortality > 0);
                } else {
                    detections = 1;
                    createDetail();
                    addNewCodedDetail(scan_details_linearLayout.getChildCount() - 2, code, signalStrength, detections, mortality > 0);
                }
            } else if (Converters.getHexValue(detectionType).equals("08")) { // Non Coded Fixed
                int period = (Integer.parseInt(Converters.getDecimalValue(data[4])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[5]));
                int pulseRate =  60000 / period;
                int type = Integer.parseInt(Converters.getHexValue(data[0]).replace("E", ""));
                int position = getPositionNumber(type, 4);
                if (position > 0) {
                    refreshNonCodedPosition(position, signalStrength, period, pulseRate);
                } else if (position < 0) {
                    detections = 1;
                    createDetail();
                    addNewNonCodedDetailInPosition(-position, pulseRate, signalStrength, detections, period, type);
                } else {
                    detections = 1;
                    createDetail();
                    addNewNonCodedFixedDetail(scan_details_linearLayout.getChildCount() - 2, pulseRate, signalStrength, detections, period, type);
                }
            } else if (Converters.getHexValue(detectionType).equals("07")) { // Non Coded Variable
                int period = (Integer.parseInt(Converters.getDecimalValue(data[4])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[5]));
                int pulseRate =  60000 / period;
                refreshNonCodedVariable(period, pulseRate, signalStrength);
            }
        }
    }

    private void scanState(byte[] data) {
        detectionType = data[18];
        int frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(data[10])) * 256) +
                (Integer.parseInt(Converters.getDecimalValue(data[11]))));
        frequency_scan_manual_textView.setText(getFrequency(frequency));
        frequency_manual_textView.setText(getFrequency(frequency));
        updateVisibility();
    }

    private void gpsState(byte[] data) {
        int state = Integer.parseInt(Converters.getDecimalValue(data[1]));
        if (state == 3)
            setGpsOn();
        else if (state == 2)
            setGpsOff(R.string.lb_failed_gps);
        else if (state == 1)
            setGpsOff(R.string.lb_searching_gps);
    }

    /**
     * Looks for the position in the table of code received.
     *
     * @param number Number of code or period to look.
     *
     * @return Returns the position of code in the table.
     */
    private int getPositionNumber(int number, int position) {
        for (int i = 2; i < scan_details_linearLayout.getChildCount() - 1; i += 2) {
            LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            TextView numberTextView = (TextView) linearLayout.getChildAt(position);

            if (Integer.parseInt(numberTextView.getText().toString()) == number)
                return i;
            else if (number < Integer.parseInt(numberTextView.getText().toString()))
                return -i;
        }
        return 0;
    }

    private void refreshCodedPosition(int position, int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        detections = Integer.parseInt(detectionsTextView.getText().toString()) + 1;
        mort = isMort ? Integer.parseInt(mortTextView.getText().toString()) + 1 : Integer.parseInt(mortTextView.getText().toString());
        detectionsTextView.setText(String.valueOf(detections));
        mortalityTextView.setText(isMort ? "M" : "-");
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        mortTextView.setText(String.valueOf(mort));
    }

    private void refreshNonCodedPosition(int position, int signalStrength, int period, int pulseRate) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);

        detections = Integer.parseInt(detectionsTextView.getText().toString()) + 1;
        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText(String.valueOf(detections));
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
    }

    private void addNewCodedDetail(int position, int code, int signalStrength, int detections, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        codeTextView.setText(String.valueOf(code));
        detectionsTextView.setText(String.valueOf(detections));
        mortalityTextView.setText(isMort ? "M" : "-");
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        mortTextView.setText(isMort ? "1" : "0");
        mort = isMort ? 1 : 0;
    }

    private void addNewNonCodedFixedDetail(int position, int pulseRate, int signalStrength, int detections, int period, int type) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView typeTextView = (TextView) linearLayout.getChildAt(4);

        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText(String.valueOf(detections));
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        typeTextView.setText(String.valueOf(type));
    }

    private void addNewNonCodedVariableDetail(int position, int pulseRate, int signalStrength, int period) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);

        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText("-");
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
    }

    private void addNewCodedDetailInPosition(int position, int code, int signalStrength, int detections, boolean isMort) {
        for (int i = scan_details_linearLayout.getChildCount() - 2; i > position ; i -= 2) {
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
        addNewCodedDetail(position, code, signalStrength, detections, isMort);
    }

    private void addNewNonCodedDetailInPosition(int position, int pulseRate, int signalStrength, int detections, int period, int type) {
        for (int i = scan_details_linearLayout.getChildCount() - 2; i > position ; i -= 2) {
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

            TextView lastTypeTextView = (TextView) lastLinearLayout.getChildAt(4);
            TextView penultimateTypeTextView = (TextView) penultimateLinearLayout.getChildAt(4);
            lastTypeTextView.setText(penultimateTypeTextView.getText());
        }
        addNewNonCodedFixedDetail(position, pulseRate, signalStrength, detections, period, type);
    }

    /**
     * Creates a row of non code data and display it.
     *
     * @param period Number of period received.
     * @param pulseRate Number of pulse rate received.
     * @param signalStrength Number of signal strength received.
     */
    private void refreshNonCodedVariable(int period, int pulseRate, int signalStrength) {
        createDetail();
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
        addNewNonCodedVariableDetail(2, pulseRate, signalStrength, period);
    }

    private void createDetail() {
        LinearLayout detail = new LinearLayout(this);
        detail.setOrientation(LinearLayout.HORIZONTAL);
        detail.setPadding(0, 8, 0, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;

        TextView firstTextView = createTextView(params);
        TextView detectionsTextView = createTextView(params);
        TextView secondTextView = createTextView(params);
        TextView signalStrengthTextView = createTextView(params);
        TextView extraTextView = new TextView(this);
        extraTextView.setVisibility(View.GONE);

        detail.addView(firstTextView);
        detail.addView(detectionsTextView);
        detail.addView(secondTextView);
        detail.addView(signalStrengthTextView);
        detail.addView(extraTextView);

        LinearLayout line = new LinearLayout(this);
        line.setBackgroundColor(ContextCompat.getColor(this, light_gray));
        line.setLayoutParams(line_view.getLayoutParams());

        scan_details_linearLayout.addView(detail);
        scan_details_linearLayout.addView(line);
    }

    private TextView createTextView(TableRow.LayoutParams params) {
        TextView textView = new TextView(this);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setTextAppearance(body_regular);
        textView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        textView.setLayoutParams(params);
        return textView;
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
}
