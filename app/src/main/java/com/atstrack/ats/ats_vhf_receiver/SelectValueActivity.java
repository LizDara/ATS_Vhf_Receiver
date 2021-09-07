package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    @BindView(R.id.fixed_filter_type_linearLayout)
    LinearLayout fixed_filter_type_linearLayout;
    @BindView(R.id.variable_filter_type_linearLayout)
    LinearLayout variable_filter_type_linearLayout;
    @BindView(R.id.number_of_matches_scrollView)
    ScrollView number_of_matches_scrollView;
    @BindView(R.id.fixed_pulse_rate_imageView)
    ImageView fixed_pulse_rate_imageView;
    @BindView(R.id.variable_pulse_rate_imageView)
    ImageView variable_pulse_rate_imageView;
    @BindView(R.id.pattern_matching_imageView)
    ImageView pattern_matching_imageView;
    @BindView(R.id.pulses_per_scan_time_imageView)
    ImageView pulses_per_scan_time_imageView;
    @BindView(R.id.temperature_imageView)
    ImageView temperature_imageView;
    @BindView(R.id.period_imageView)
    ImageView period_imageView;
    @BindView(R.id.altitude_imageView)
    ImageView altitude_imageView;
    @BindView(R.id.depth_imageView)
    ImageView depth_imageView;
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
    public static final int FILTER_TYPE = 1002;
    public static final int MATCHES_FOR_VALID_PATTERN = 1003;
    public static final int FIXED_PULSE_RATE = 1004;
    public static final int VARIABLE_PULSE_RATE = 1005;
    public static final int PATTERN_MATCHING = 1006;
    public static final int PULSES_PER_SCAN_TIME = 1007;
    public static final int TEMPERATURE = 1008;
    public static final int PERIOD = 1009;
    public static final int ALTITUDE = 1010;
    public static final int DEPTH = 1011;
    public static final int PULSE_RATE_1 = 1012;
    public static final int PULSE_RATE_2 = 1013;
    public static final int PULSE_RATE_3 = 1014;
    public static final int PULSE_RATE_4 = 1015;

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private boolean state = true;

    private int type;
    private int value;

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
                    if (parameter.equals("txType")) {
                        onClickTxType();
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (type == PULSE_RATE_TYPE)
                        downloadPulseRateType(packet);
                    else if (type == MATCHES_FOR_VALID_PATTERN)
                        downloadMatchesForValidPattern(packet);
                    else if (type == PULSE_RATE_1)
                        downloadPulseRate1(packet);
                    else if (type == PULSE_RATE_2)
                        downloadPulseRate2(packet);
                    else if (type == PULSE_RATE_3)
                        downloadPulseRate3(packet);
                    else if (type == PULSE_RATE_4)
                        downloadPulseRate4(packet);
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

    public void onClickTxType() {
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

    @OnClick(R.id.pattern_matching_linearLayout)
    public void onClickPatternMatching(View v) {
        pattern_matching_imageView.setVisibility(View.VISIBLE);
        pulses_per_scan_time_imageView.setVisibility(View.GONE);
        value = PATTERN_MATCHING;
    }

    @OnClick(R.id.pulses_per_scan_time_linearLayout)
    public void onClickPulsesPerScanTime(View v) {
        pulses_per_scan_time_imageView.setVisibility(View.VISIBLE);
        pattern_matching_imageView.setVisibility(View.GONE);
        value = PULSES_PER_SCAN_TIME;
    }

    @OnClick(R.id.temperature_linearLayout)
    public void onClickTemperature(View v) {
        temperature_imageView.setVisibility(View.VISIBLE);
        period_imageView.setVisibility(View.GONE);
        altitude_imageView.setVisibility(View.GONE);
        depth_imageView.setVisibility(View.GONE);
        value = TEMPERATURE;
    }

    @OnClick(R.id.period_linearLayout)
    public void onClickPeriod(View v) {
        period_imageView.setVisibility(View.VISIBLE);
        temperature_imageView.setVisibility(View.GONE);
        altitude_imageView.setVisibility(View.GONE);
        depth_imageView.setVisibility(View.GONE);
        value = PERIOD;
    }

    @OnClick(R.id.altitude_linearLayout)
    public void onClickAltitude(View v) {
        altitude_imageView.setVisibility(View.VISIBLE);
        temperature_imageView.setVisibility(View.GONE);
        period_imageView.setVisibility(View.GONE);
        depth_imageView.setVisibility(View.GONE);
        value = ALTITUDE;
    }

    @OnClick(R.id.depth_linearLayout)
    public void onClickDepth(View v) {
        depth_imageView.setVisibility(View.VISIBLE);
        temperature_imageView.setVisibility(View.GONE);
        period_imageView.setVisibility(View.GONE);
        altitude_imageView.setVisibility(View.GONE);
        value = DEPTH;
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
            value = Integer.parseInt(pulse_rate_editText.getText().toString());
            value = (value * 100) + Integer.parseInt(pulse_rate_tolerance_editText.getText().toString());
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
        final Intent intent = getIntent();
        receiverInformation = ReceiverInformation.getReceiverInformation();
        parameter = "txType";

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        type = intent.getIntExtra("type", 0);
        if (type == PULSE_RATE_TYPE) {
            title_toolbar.setText(R.string.pulse_rate_type_options);
            save_changes_select_value_button.setVisibility(View.GONE);
        }
        /*if (type == FIXED_PULSE_RATE) {
            fixed_filter_type_linearLayout.setVisibility(View.VISIBLE);
            value = PATTERN_MATCHING;
            title_toolbar.setText("Filter Type Options");
        }
        if (type == VARIABLE_PULSE_RATE) {
            variable_filter_type_linearLayout.setVisibility(View.VISIBLE);
            value = TEMPERATURE;
            title_toolbar.setText("Filter Type Options");
        }*/
        if (type == MATCHES_FOR_VALID_PATTERN) {
            number_of_matches_scrollView.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.matches_for_valid_pattern);
        }
        if (type == PULSE_RATE_1) {
            pulse_rate_textView.setText(R.string.lb_pr1);
            pulse_rate_tolerance_textView.setText(R.string.lb_pr1_tolerance);
            pulse_rate_linearLayout.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.target_pulse_rate_1);
        }
        if (type == PULSE_RATE_2) {
            pulse_rate_textView.setText(R.string.lb_pr2);
            pulse_rate_tolerance_textView.setText(R.string.lb_pr2_tolerance);
            pulse_rate_linearLayout.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.target_pulse_rate_2);
        }
        if (type == PULSE_RATE_3) {
            pulse_rate_textView.setText(R.string.lb_pr3);
            pulse_rate_tolerance_textView.setText(R.string.lb_pr3_tolerance);
            pulse_rate_linearLayout.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.target_pulse_rate_3);
        }
        if (type == PULSE_RATE_4) {
            pulse_rate_textView.setText(R.string.lb_pr4);
            pulse_rate_tolerance_textView.setText(R.string.lb_pr4_tolerance);
            pulse_rate_linearLayout.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.target_pulse_rate_4);
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //hago un case por si en un futuro agrego mas opciones
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
        if (!mConnected && !state) {
            showMessageDisconnect();
        }
        return true;
    }

    private void showMessageDisconnect() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view =inflater.inflate(R.layout.disconnect_message, null);
        final androidx.appcompat.app.AlertDialog dialog = new AlertDialog.Builder(this).create();

        dialog.setView(view);
        dialog.show();

        int MESSAGE_PERIOD = 3000;
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, MESSAGE_PERIOD);
    }

    public void downloadPulseRateType(byte[] data) {
        if (Converters.getHexValue(data[1]).equals("20") || Converters.getHexValue(data[1]).equals("04")) {
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

    public void downloadMatchesForValidPattern(byte[] data) {
        if (Converters.getDecimalValue(data[2]).equals("2")) {
            two_imageView.setVisibility(View.VISIBLE);
            value = 2;
        } else if (Converters.getDecimalValue(data[2]).equals("3")) {
            three_imageView.setVisibility(View.VISIBLE);
            value = 3;
        } else if (Converters.getDecimalValue(data[2]).equals("4")) {
            four_imageView.setVisibility(View.VISIBLE);
            value = 4;
        } else if (Converters.getDecimalValue(data[2]).equals("5")) {
            five_imageView.setVisibility(View.VISIBLE);
            value = 5;
        } else if (Converters.getDecimalValue(data[2]).equals("6")) {
            six_imageView.setVisibility(View.VISIBLE);
            value = 6;
        } else if (Converters.getDecimalValue(data[2]).equals("7")) {
            seven_imageView.setVisibility(View.VISIBLE);
            value = 7;
        } else if (Converters.getDecimalValue(data[2]).equals("8")) {
            eight_imageView.setVisibility(View.VISIBLE);
            value = 8;
        }
    }

    public void downloadPulseRate1(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[3]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[4]));
    }

    public void downloadPulseRate2(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[5]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[6]));
    }

    public void downloadPulseRate3(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[7]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[8]));
    }

    public void downloadPulseRate4(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[9]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[10]));
    }
}