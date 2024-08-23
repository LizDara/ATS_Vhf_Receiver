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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverStatus;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Objects;
import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.catskill_white;

public class SelectValueActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.select_pulse_rate_linearLayout)
    LinearLayout select_pulse_rate_linearLayout;
    @BindView(R.id.number_of_matches_scrollView)
    ScrollView number_of_matches_scrollView;
    @BindView(R.id.coded_imageView)
    ImageView coded_imageView;
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

    private final static String TAG = SelectValueActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    private int type;
    private int value;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize())
                finish();
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    showDisconnectionMessage(status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals(ValueCodes.DETECTION_TYPE))
                        onClickTxType();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    switch (type) {
                        case ValueCodes.PULSE_RATE_TYPE_CODE: // Gets the pulse rate type
                            downloadPulseRateType(packet);
                            break;
                        case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE: // Gets the matches for valid pattern
                            downloadMatchesForValidPattern(packet);
                            break;
                        case ValueCodes.MAX_PULSE_RATE_CODE: // Gets the max pulse rate
                            downloadMaxPulseRate(packet);
                            break;
                        case ValueCodes.MIN_PULSE_RATE_CODE: // Gets the min pulse rate
                            downloadMinPulseRate(packet);
                            break;
                        case ValueCodes.DATA_CALCULATION_TYPE_CODE: // Gets data calculation types
                            downloadDataCalculation(packet);
                        case ValueCodes.PULSE_RATE_1_CODE: // Gets the pulse rate 1
                            downloadPulseRate1(packet);
                            break;
                        case ValueCodes.PULSE_RATE_2_CODE: // Gets the pulse rate 2
                            downloadPulseRate2(packet);
                            break;
                        case ValueCodes.PULSE_RATE_3_CODE: // Gets the pulse rate 3
                            downloadPulseRate3(packet);
                            break;
                        case ValueCodes.PULSE_RATE_4_CODE: // Gets the pulse rate 4
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

    private final TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

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
     */
    private void onClickTxType() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    @OnClick(R.id.coded_linearLayout)
    public void onClickCoded(View v) {
        coded_imageView.setVisibility(View.VISIBLE);
        fixed_pulse_rate_imageView.setVisibility(View.GONE);
        variable_pulse_rate_imageView.setVisibility(View.GONE);
        value = ValueCodes.CODED_CODE;
    }

    @OnClick(R.id.fixed_pulse_rate_linearLayout)
    public void onClickFixedPulseRate(View v) {
        coded_imageView.setVisibility(View.GONE);
        fixed_pulse_rate_imageView.setVisibility(View.VISIBLE);
        variable_pulse_rate_imageView.setVisibility(View.GONE);
        value = ValueCodes.FIXED_PULSE_RATE_CODE;
    }

    @OnClick(R.id.variable_pulse_rate_linearLayout)
    public void onClickVariablePulseRate(View v) {
        coded_imageView.setVisibility(View.GONE);
        variable_pulse_rate_imageView.setVisibility(View.VISIBLE);
        fixed_pulse_rate_imageView.setVisibility(View.GONE);
        value = ValueCodes.VARIABLE_PULSE_RATE_CODE;
    }

    @OnClick(R.id.none_linearLayout)
    public void onClickNone(View v) {
        none_imageView.setVisibility(View.VISIBLE);
        temperature_imageView.setVisibility(View.GONE);
        period_imageView.setVisibility(View.GONE);
        value = ValueCodes.NONE_CODE;
    }

    @OnClick(R.id.temperature_linearLayout)
    public void onClickTemperature(View v) {
        temperature_imageView.setVisibility(View.VISIBLE);
        none_imageView.setVisibility(View.GONE);
        period_imageView.setVisibility(View.GONE);
        value = ValueCodes.TEMPERATURE_CODE;
    }

    @OnClick(R.id.period_linearLayout)
    public void onClickPeriod(View v) {
        period_imageView.setVisibility(View.VISIBLE);
        none_imageView.setVisibility(View.GONE);
        temperature_imageView.setVisibility(View.GONE);
        value = ValueCodes.PERIOD_CODE;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_value);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        receiverInformation = ReceiverInformation.getReceiverInformation();
        ReceiverStatus.setReceiverStatus(this);

        type = getIntent().getIntExtra(ValueCodes.TYPE, 0);
        switch (type) {
            case ValueCodes.PULSE_RATE_TYPE_CODE:
                setVisibility("pulseRateTypes");
                break;
            case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE:
                setVisibility("matches");
                break;
            case ValueCodes.MAX_PULSE_RATE_CODE:
                setVisibility("maxMin");
                title_toolbar.setText(R.string.max_pulse_rate);
                max_min_pulse_rate_textView.setText(R.string.lb_max_pulse_rate);
                break;
            case ValueCodes.MIN_PULSE_RATE_CODE:
                setVisibility("maxMin");
                title_toolbar.setText(R.string.min_pulse_rate);
                max_min_pulse_rate_textView.setText(R.string.lb_min_pulse_rate);
                break;
            case ValueCodes.DATA_CALCULATION_TYPE_CODE:
                setVisibility("calculation");
                break;
            case ValueCodes.PULSE_RATE_1_CODE:
                setVisibility("pulseRateValues");
                title_toolbar.setText(R.string.target_pulse_rate_1);
                pulse_rate_textView.setText(R.string.lb_pr1);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr1_tolerance);
                break;
            case ValueCodes.PULSE_RATE_2_CODE:
                setVisibility("pulseRateValues");
                title_toolbar.setText(R.string.target_pulse_rate_2);
                pulse_rate_textView.setText(R.string.lb_pr2);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr2_tolerance);
                break;
            case ValueCodes.PULSE_RATE_3_CODE:
                setVisibility("pulseRateValues");
                title_toolbar.setText(R.string.target_pulse_rate_3);
                pulse_rate_textView.setText(R.string.lb_pr3);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr3_tolerance);
                break;
            case ValueCodes.PULSE_RATE_4_CODE:
                setVisibility("pulseRateValues");
                title_toolbar.setText(R.string.target_pulse_rate_4);
                pulse_rate_textView.setText(R.string.lb_pr4);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr4_tolerance);
                break;
        }
        parameter = ValueCodes.DETECTION_TYPE;

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            Intent intent = new Intent();
            if (type == ValueCodes.PULSE_RATE_1_CODE || type == ValueCodes.PULSE_RATE_2_CODE || type == ValueCodes.PULSE_RATE_3_CODE || type == ValueCodes.PULSE_RATE_4_CODE) {
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
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(catskill_white)));
                    return true;
                }
            } else if (type == ValueCodes.MAX_PULSE_RATE_CODE || type == ValueCodes.MIN_PULSE_RATE_CODE) {
                value = Integer.parseInt(max_min_pulse_rate_editText.getText().toString());
            }
            intent.putExtra(ValueCodes.VALUE, value);
            setResult(type, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
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
    public void onBackPressed() {
        Log.i(TAG, "Back Button Pressed");
    }

    private void showDisconnectionMessage(int status) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(view);
        dialog.show();
        Toast.makeText(this, "Connection failed, status: " + status, Toast.LENGTH_LONG).show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD);
    }

    private void setVisibility(String value) {
        switch (value) {
            case "pulseRateTypes":
                select_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                number_of_matches_scrollView.setVisibility(View.GONE);
                pulse_rate_linearLayout.setVisibility(View.GONE);
                max_min_pulse_rate_linearLayout.setVisibility(View.GONE);
                data_calculation_types_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.pulse_rate_type_options);
                break;
            case "matches":
                select_pulse_rate_linearLayout.setVisibility(View.GONE);
                number_of_matches_scrollView.setVisibility(View.VISIBLE);
                pulse_rate_linearLayout.setVisibility(View.GONE);
                max_min_pulse_rate_linearLayout.setVisibility(View.GONE);
                data_calculation_types_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.matches_for_valid_pattern);
                break;
            case "pulseRateValues":
                select_pulse_rate_linearLayout.setVisibility(View.GONE);
                number_of_matches_scrollView.setVisibility(View.GONE);
                pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                max_min_pulse_rate_linearLayout.setVisibility(View.GONE);
                data_calculation_types_linearLayout.setVisibility(View.GONE);
                break;
            case "maxMin":
                select_pulse_rate_linearLayout.setVisibility(View.GONE);
                number_of_matches_scrollView.setVisibility(View.GONE);
                pulse_rate_linearLayout.setVisibility(View.GONE);
                max_min_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                data_calculation_types_linearLayout.setVisibility(View.GONE);
                max_min_pulse_rate_editText.addTextChangedListener(textChangedListener);
                break;
            case "calculation":
                select_pulse_rate_linearLayout.setVisibility(View.GONE);
                number_of_matches_scrollView.setVisibility(View.GONE);
                pulse_rate_linearLayout.setVisibility(View.GONE);
                max_min_pulse_rate_linearLayout.setVisibility(View.GONE);
                data_calculation_types_linearLayout.setVisibility(View.VISIBLE);
                title_toolbar.setText(R.string.optional_data_calculations);
                break;
        }
    }

    /**
     * With the received packet, gets pulse rate type and display on the screen.
     * @param data The received packet.
     */
    private void downloadPulseRateType(byte[] data) {
        setVisibility("pulseRateTypes");
        if (Converters.getHexValue(data[1]).equals("09")) {
            coded_imageView.setVisibility(View.VISIBLE);
            value = ValueCodes.CODED_CODE;
        } else if (Converters.getHexValue(data[1]).equals("08")) {
            fixed_pulse_rate_imageView.setVisibility(View.VISIBLE);
            value = ValueCodes.FIXED_PULSE_RATE_CODE;
        } else if (Converters.getHexValue(data[1]).equals("07")) {
            variable_pulse_rate_imageView.setVisibility(View.VISIBLE);
            value = ValueCodes.VARIABLE_PULSE_RATE_CODE;
        }
    }

    /**
     * With the received packet, gets matches for valid pattern and display on the screen.
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
     * @param data The received packet.
     */
    private void downloadDataCalculation(byte[] data) {
        switch (Converters.getHexValue(data[11])) {
            case "00":
                none_imageView.setVisibility(View.VISIBLE);
                value = ValueCodes.NONE_CODE;
                break;
            case "08":
                period_imageView.setVisibility(View.VISIBLE);
                value = ValueCodes.PERIOD_CODE;
                break;
            case "04":
                temperature_imageView.setVisibility(View.VISIBLE);
                value = ValueCodes.TEMPERATURE_CODE;
                break;
        }
    }

    /**
     * With the received packet, gets pulse rate 1 and display on the screen.
     * @param data The received packet.
     */
    private void downloadPulseRate1(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[3]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[4]));
        value = (Integer.parseInt(Converters.getDecimalValue(data[3])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[4]));
    }

    /**
     * With the received packet, gets pulse rate 2 and display on the screen.
     * @param data The received packet.
     */
    private void downloadPulseRate2(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[5]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[6]));
        value = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[6]));
    }

    /**
     * With the received packet, gets pulse rate 3 and display on the screen.
     * @param data The received packet.
     */
    private void downloadPulseRate3(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[7]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[8]));
        value = (Integer.parseInt(Converters.getDecimalValue(data[7])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[8]));
    }

    /**
     * With the received packet, gets pulse rate 4 and display on the screen.
     * @param data The received packet.
     */
    private void downloadPulseRate4(byte[] data) {
        pulse_rate_editText.setText(Converters.getDecimalValue(data[9]));
        pulse_rate_tolerance_editText.setText(Converters.getDecimalValue(data[10]));
        value = (Integer.parseInt(Converters.getDecimalValue(data[9])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[10]));
    }
}