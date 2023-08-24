package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.catskill_white;

public class SelectValueActivity extends AppCompatActivity {

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
    @BindView(R.id.non_coded_linearLayout)
    LinearLayout non_coded_linearLayout;
    @BindView(R.id.select_pulse_rate_linearLayout)
    LinearLayout select_pulse_rate_linearLayout;
    @BindView(R.id.number_of_matches_scrollView)
    ScrollView number_of_matches_scrollView;
    @BindView(R.id.fixed_pulse_rate_imageView)
    ImageView fixed_pulse_rate_imageView;
    @BindView(R.id.variable_pulse_rate_imageView)
    ImageView variable_pulse_rate_imageView;
    @BindView(R.id.max_min_pulse_rate_linearLayout)
    LinearLayout max_min_pulse_rate_linearLayout;
    @BindView(R.id.max_min_pulse_rate_textView)
    TextView max_min_pulse_rate_textView;
    @BindView(R.id.max_min_pulse_rate_editText)
    EditText max_min_pulse_rate_editText;
    @BindView(R.id.period_pulse_rate_textView)
    TextView period_pulse_rate_textView;
    @BindView(R.id.data_calculation_types_linearLayout)
    LinearLayout data_calculation_types_linearLayout;
    @BindView(R.id.none_imageView)
    ImageView none_imageView;
    @BindView(R.id.temperature_imageView)
    ImageView temperature_imageView;
    @BindView(R.id.period_imageView)
    ImageView period_imageView;
    @BindView(R.id.two_imageView)
    ImageView two_imageView;
    @BindView(R.id.three_imageView)
    ImageView three_imageView;
    @BindView(R.id.four_imageView)
    ImageView four_imageView;
    @BindView(R.id.five_imageView)
    ImageView five_imageView;
    @BindView(R.id.six_imageView)
    ImageView six_imageView;
    @BindView(R.id.seven_imageView)
    ImageView seven_imageView;
    @BindView(R.id.eight_imageView)
    ImageView eight_imageView;
    @BindView(R.id.pulse_rate_linearLayout)
    LinearLayout pulse_rate_linearLayout;
    @BindView(R.id.pulse_rate_textView)
    TextView pulse_rate_textView;
    @BindView(R.id.pulse_rate_editText)
    EditText pulse_rate_editText;
    @BindView(R.id.pulse_rate_tolerance_textView)
    TextView pulse_rate_tolerance_textView;
    @BindView(R.id.pulse_rate_tolerance_editText)
    EditText pulse_rate_tolerance_editText;
    @BindView(R.id.save_changes_select_value_button)
    Button save_changes_select_value_button;

    private final static String TAG = SelectValueActivity.class.getSimpleName();

    public static final int PULSE_RATE_TYPE = 1001;
    public static final int MATCHES_FOR_VALID_PATTERN = 1002;
    public static final int FIXED_PULSE_RATE = 1003;
    public static final int VARIABLE_PULSE_RATE = 1004;
    public static final int PULSE_RATE_1 = 1005;
    public static final int PULSE_RATE_2 = 1006;
    public static final int PULSE_RATE_3 = 1007;
    public static final int PULSE_RATE_4 = 1008;
    public static final int MAX_PULSE_RATE = 1009;
    public static final int MIN_PULSE_RATE = 1010;
    public static final int DATA_CALCULATION_TYPES = 1011;
    public static final int NONE = 1012;
    public static final int TEMPERATURE = 1013;
    public static final int PERIOD = 1014;

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    private int type;
    private int value;

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
                    if (parameter.equals("txType"))
                        onClickTxType();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    switch (type) {
                        case PULSE_RATE_TYPE: // Gets the pulse rate type
                            downloadPulseRateType(packet);
                            break;
                        case MATCHES_FOR_VALID_PATTERN: // Gets the matches for valid pattern
                            downloadMatchesForValidPattern(packet);
                            break;
                        case MAX_PULSE_RATE: // Gets the max pulse rate
                            downloadMaxPulseRate(packet);
                            break;
                        case MIN_PULSE_RATE: // Gets the min pulse rate
                            downloadMinPulseRate(packet);
                            break;
                        case DATA_CALCULATION_TYPES: // Gets data calculation types

                        case PULSE_RATE_1: // Gets the pulse rate 1
                            downloadPulseRate1(packet);
                            break;
                        case PULSE_RATE_2: // Gets the pulse rate 2
                            downloadPulseRate2(packet);
                            break;
                        case PULSE_RATE_3: // Gets the pulse rate 3
                            downloadPulseRate3(packet);
                            break;
                        case PULSE_RATE_4: // Gets the pulse rate 4
                            downloadPulseRate4(packet);
                            break;
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
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
            int pulseRate = (max_min_pulse_rate_editText.getText().toString().isEmpty()) ? 0 : Integer.parseInt(max_min_pulse_rate_editText.getText().toString());
            double period = (max_min_pulse_rate_editText.getText().toString().isEmpty() ||
                    Integer.parseInt(max_min_pulse_rate_editText.getText().toString()) == 0) ? 0 : (double) 60000 / pulseRate;
            period_pulse_rate_textView.setText(String.format("%.2f ms (period)", period));
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
     * Requests a read for tx type data.
     * Service name: Scan.
     * Characteristic name: Tx type.
     */
    private void onClickTxType() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    @OnClick(R.id.select_pulse_rate_button)
    public void onClickSelectPulseRate(View v) {
        non_coded_linearLayout.setVisibility(View.GONE);
        select_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
        save_changes_select_value_button.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.fixed_pulse_rate_linearLayout)
    public void onClickFixedPulseRate(View v) {
        fixed_pulse_rate_imageView.setVisibility(View.VISIBLE);
        variable_pulse_rate_imageView.setVisibility(View.GONE);
        value = FIXED_PULSE_RATE;
    }

    @OnClick(R.id.variable_pulse_rate_linearLayout)
    public void onClickVariablePulseRate(View v) {
        variable_pulse_rate_imageView.setVisibility(View.VISIBLE);
        fixed_pulse_rate_imageView.setVisibility(View.GONE);
        value = VARIABLE_PULSE_RATE;
    }

    @OnClick(R.id.none_linearLayout)
    public void onClickNone(View v) {
        none_imageView.setVisibility(View.VISIBLE);
        temperature_imageView.setVisibility(View.GONE);
        period_imageView.setVisibility(View.GONE);
        value = NONE;
    }

    @OnClick(R.id.temperature_linearLayout)
    public void onClickTemperature(View v) {
        temperature_imageView.setVisibility(View.VISIBLE);
        none_imageView.setVisibility(View.GONE);
        period_imageView.setVisibility(View.GONE);
        value = TEMPERATURE;
    }

    @OnClick(R.id.period_linearLayout)
    public void onClickPeriod(View v) {
        period_imageView.setVisibility(View.VISIBLE);
        none_imageView.setVisibility(View.GONE);
        temperature_imageView.setVisibility(View.GONE);
        value = PERIOD;
    }

    @OnClick(R.id.two_linearLayout)
    public void onClickTwo(View v) {
        two_imageView.setVisibility(View.VISIBLE);
        three_imageView.setVisibility(View.GONE);
        four_imageView.setVisibility(View.GONE);
        five_imageView.setVisibility(View.GONE);
        six_imageView.setVisibility(View.GONE);
        seven_imageView.setVisibility(View.GONE);
        eight_imageView.setVisibility(View.GONE);
        value = 2;
    }

    @OnClick(R.id.three_linearLayout)
    public void onClickThree(View v) {
        three_imageView.setVisibility(View.VISIBLE);
        two_imageView.setVisibility(View.GONE);
        four_imageView.setVisibility(View.GONE);
        five_imageView.setVisibility(View.GONE);
        six_imageView.setVisibility(View.GONE);
        seven_imageView.setVisibility(View.GONE);
        eight_imageView.setVisibility(View.GONE);
        value = 3;
    }

    @OnClick(R.id.four_linearLayout)
    public void onClickFour(View v) {
        four_imageView.setVisibility(View.VISIBLE);
        two_imageView.setVisibility(View.GONE);
        three_imageView.setVisibility(View.GONE);
        five_imageView.setVisibility(View.GONE);
        six_imageView.setVisibility(View.GONE);
        seven_imageView.setVisibility(View.GONE);
        eight_imageView.setVisibility(View.GONE);
        value = 4;
    }

    @OnClick(R.id.five_linearLayout)
    public void onClickFive(View v) {
        five_imageView.setVisibility(View.VISIBLE);
        two_imageView.setVisibility(View.GONE);
        three_imageView.setVisibility(View.GONE);
        four_imageView.setVisibility(View.GONE);
        six_imageView.setVisibility(View.GONE);
        seven_imageView.setVisibility(View.GONE);
        eight_imageView.setVisibility(View.GONE);
        value = 5;
    }

    @OnClick(R.id.six_linearLayout)
    public void onClickSix(View v) {
        six_imageView.setVisibility(View.VISIBLE);
        two_imageView.setVisibility(View.GONE);
        three_imageView.setVisibility(View.GONE);
        four_imageView.setVisibility(View.GONE);
        five_imageView.setVisibility(View.GONE);
        seven_imageView.setVisibility(View.GONE);
        eight_imageView.setVisibility(View.GONE);
        value = 6;
    }

    @OnClick(R.id.seven_linearLayout)
    public void onClickSeven(View v) {
        seven_imageView.setVisibility(View.VISIBLE);
        two_imageView.setVisibility(View.GONE);
        three_imageView.setVisibility(View.GONE);
        four_imageView.setVisibility(View.GONE);
        five_imageView.setVisibility(View.GONE);
        six_imageView.setVisibility(View.GONE);
        eight_imageView.setVisibility(View.GONE);
        value = 7;
    }

    @OnClick(R.id.eight_linearLayout)
    public void onClickEight(View v) {
        eight_imageView.setVisibility(View.VISIBLE);
        two_imageView.setVisibility(View.GONE);
        three_imageView.setVisibility(View.GONE);
        four_imageView.setVisibility(View.GONE);
        five_imageView.setVisibility(View.GONE);
        six_imageView.setVisibility(View.GONE);
        seven_imageView.setVisibility(View.GONE);
        value = 8;
    }

    @OnClick(R.id.save_changes_select_value_button)
    public void onClickSaveChanges(View v) {
        if (type == PULSE_RATE_1 || type == PULSE_RATE_2 || type == PULSE_RATE_3 || type == PULSE_RATE_4) {
            int pulseRate = Integer.parseInt(pulse_rate_editText.getText().toString());
            int tolerance = Integer.parseInt(pulse_rate_tolerance_editText.getText().toString());
            if (pulseRate > 0 && pulseRate <= 150 && tolerance > 0 && tolerance <= 10) {
                value = (pulseRate * 100) + tolerance;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Invalid Format or Values");
                builder.setMessage("Please enter valid pulse rate or tolerance values.");
                builder.setPositiveButton("Ok", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(catskill_white)));
                return;
            }
        } else if (type == MAX_PULSE_RATE || type == MIN_PULSE_RATE) {
            value = Integer.parseInt(max_min_pulse_rate_editText.getText().toString());
        }
        setResult(value);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_value);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();
        parameter = "txType";

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        type = getIntent().getIntExtra("type", 0);
        switch (type) {
            case PULSE_RATE_TYPE:
                title_toolbar.setText(R.string.pulse_rate_type_options);
                save_changes_select_value_button.setVisibility(View.GONE);
                break;
            case MATCHES_FOR_VALID_PATTERN:
                title_toolbar.setText(R.string.matches_for_valid_pattern);
                number_of_matches_scrollView.setVisibility(View.VISIBLE);
                break;
            case MAX_PULSE_RATE:
                title_toolbar.setText(R.string.max_pulse_rate);
                max_min_pulse_rate_textView.setText(R.string.lb_max_pulse_rate);
                max_min_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                max_min_pulse_rate_editText.addTextChangedListener(textChangedListener);
                break;
            case MIN_PULSE_RATE:
                title_toolbar.setText(R.string.min_pulse_rate);
                max_min_pulse_rate_textView.setText(R.string.lb_min_pulse_rate);
                max_min_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                max_min_pulse_rate_editText.addTextChangedListener(textChangedListener);
                break;
            case DATA_CALCULATION_TYPES:
                title_toolbar.setText(R.string.optional_data_calculations);
                data_calculation_types_linearLayout.setVisibility(View.VISIBLE);
                break;
            case PULSE_RATE_1:
                title_toolbar.setText(R.string.target_pulse_rate_1);
                pulse_rate_textView.setText(R.string.lb_pr1);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr1_tolerance);
                pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                break;
            case PULSE_RATE_2:
                title_toolbar.setText(R.string.target_pulse_rate_2);
                pulse_rate_textView.setText(R.string.lb_pr2);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr2_tolerance);
                pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                break;
            case PULSE_RATE_3:
                title_toolbar.setText(R.string.target_pulse_rate_3);
                pulse_rate_textView.setText(R.string.lb_pr3);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr3_tolerance);
                pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                break;
            case PULSE_RATE_4:
                title_toolbar.setText(R.string.target_pulse_rate_4);
                pulse_rate_textView.setText(R.string.lb_pr4);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr4_tolerance);
                pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                break;
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected) {
            showDisconnectionMessage();
        }
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
     * With the received packet, gets pulse rate type and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadPulseRateType(byte[] data) {
        Log.i(TAG, "Type: " + Converters.getHexValue(data));
        if (Converters.getHexValue(data[1]).equals("20") || // this is the correct
                Converters.getHexValue(data[1]).equals("04") ||
                Converters.getHexValue(data[1]).equals("00") ||
                Converters.getHexValue(data[1]).equals("E2") ||
                Converters.getHexValue(data[1]).equals("64")) {
            non_coded_linearLayout.setVisibility(View.VISIBLE);
            value = 0;
        } else if (Converters.getHexValue(data[1]).equals("21")) {
            select_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
            fixed_pulse_rate_imageView.setVisibility(View.VISIBLE);
            save_changes_select_value_button.setVisibility(View.VISIBLE);
            value = FIXED_PULSE_RATE;
        } else if (Converters.getHexValue(data[1]).equals("22")) {
            select_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
            variable_pulse_rate_imageView.setVisibility(View.VISIBLE);
            save_changes_select_value_button.setVisibility(View.VISIBLE);
            value = FIXED_PULSE_RATE;
        }
    }

    /**
     * With the received packet, gets matches for valid pattern and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadMatchesForValidPattern(byte[] data) {
        switch (Converters.getDecimalValue(data[2])) {
            case "2":
                two_imageView.setVisibility(View.VISIBLE);
                break;
            case "3":
                three_imageView.setVisibility(View.VISIBLE);
                break;
            case "4":
                four_imageView.setVisibility(View.VISIBLE);
                break;
            case "5":
                five_imageView.setVisibility(View.VISIBLE);
                break;
            case "6":
                six_imageView.setVisibility(View.VISIBLE);
                break;
            case "7":
                seven_imageView.setVisibility(View.VISIBLE);
                break;
            case "8":
                eight_imageView.setVisibility(View.VISIBLE);
                break;
        }
        value = Integer.parseInt(Converters.getDecimalValue(data[2]));
    }

    /**
     * With the received packet, gets max pulse rate and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadMaxPulseRate(byte[] data) {
        int maxPulse = (Integer.parseInt(Converters.getDecimalValue(data[3])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[4]));
        max_min_pulse_rate_editText.setText(String.valueOf(maxPulse));
        double period = (maxPulse == 0) ? 0 : (double) 60000 / maxPulse;
        period_pulse_rate_textView.setText(String.format("%.2f ms (period)", period));
        value = maxPulse;
    }

    /**
     * With the received packet, gets min pulse rate and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadMinPulseRate(byte[] data) {
        int minPulse = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        max_min_pulse_rate_editText.setText(String.valueOf(minPulse));
        double period = (minPulse == 0) ? 0 : (double) 60000 / minPulse;
        period_pulse_rate_textView.setText(String.format("%.2f ms (period)", period));
        value = minPulse;
    }

    /**
     * With the received packet, gets data calculation types and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadDataCalculation(byte[] data) {
        switch (Converters.getHexValue(data[11])) {
            case "00":
                none_imageView.setVisibility(View.VISIBLE);
                value = NONE;
                break;
            case "08":
                period_imageView.setVisibility(View.VISIBLE);
                value = PERIOD;
                break;
            case "04":
                temperature_imageView.setVisibility(View.VISIBLE);
                value = TEMPERATURE;
                break;
        }
    }

    /**
     * With the received packet, gets pulse rate 1 and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadPulseRate1(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[3]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[4]));
        value = (Integer.parseInt(Converters.getDecimalValue(data[3])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[4]));
    }

    /**
     * With the received packet, gets pulse rate 2 and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadPulseRate2(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[5]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[6]));
        value = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[6]));
    }

    /**
     * With the received packet, gets pulse rate 3 and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadPulseRate3(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[7]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[8]));
        value = (Integer.parseInt(Converters.getDecimalValue(data[7])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[8]));
    }

    /**
     * With the received packet, gets pulse rate 4 and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadPulseRate4(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[9]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[10]));
        value = (Integer.parseInt(Converters.getDecimalValue(data[9])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[10]));
    }
}