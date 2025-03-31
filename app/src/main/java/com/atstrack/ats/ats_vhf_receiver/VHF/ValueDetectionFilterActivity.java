package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;
import butterknife.OnClick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class ValueDetectionFilterActivity extends BaseActivity {

    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
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
    @BindView(R.id.pulse_rate_tolerance_spinner)
    Spinner pulse_rate_tolerance_spinner;

    private final static String TAG = ValueDetectionFilterActivity.class.getSimpleName();

    private int type;
    private int value;
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
        contentViewId = R.layout.activity_vhf_value_detection_filter;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.lb_set_value);
        super.onCreate(savedInstanceState);

        initializeCallback();
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
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.DETECTION_TYPE))
                    TransferBleData.readDetectionFilter();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (Converters.getHexValue(packet[0]).equals("67")) {
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
                } else {
                    Message.showMessage((Activity) mContext, "Package found: " + Converters.getHexValue(packet) + ". Package expected: 0x67 ...");
                }
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            Intent intent = new Intent();
            if (type == ValueCodes.PULSE_RATE_1_CODE || type == ValueCodes.PULSE_RATE_2_CODE || type == ValueCodes.PULSE_RATE_3_CODE || type == ValueCodes.PULSE_RATE_4_CODE) {
                int pulseRate = Integer.parseInt(pulse_rate_editText.getText().toString());
                int tolerance = pulse_rate_tolerance_spinner.getSelectedItemPosition() + 4;
                if (pulseRate >= 8 && pulseRate <= 150) {
                    value = (pulseRate * 100) + tolerance;
                } else {
                    Message.showMessage(this, "Invalid Format or Values", "Please enter valid pulse rate or tolerance values.");
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
        registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
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
                ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(this, R.array.pulseRateTolerance, android.R.layout.simple_spinner_item);
                scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                pulse_rate_tolerance_spinner.setAdapter(scanRateAdapter);
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
        int pulseRateTolerance = Integer.parseInt(Converters.getDecimalValue(data[4]));
        pulse_rate_editText.setText(Converters.getDecimalValue(data[3]));
        pulse_rate_tolerance_spinner.setSelection(pulseRateTolerance - 4);
        value = (Integer.parseInt(Converters.getDecimalValue(data[3])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[4]));
    }

    /**
     * With the received packet, gets pulse rate 2 and display on the screen.
     * @param data The received packet.
     */
    private void downloadPulseRate2(byte[] data) {
        int pulseRateTolerance = Integer.parseInt(Converters.getDecimalValue(data[6]));
        pulse_rate_editText.setText(Converters.getDecimalValue(data[5]));
        pulse_rate_tolerance_spinner.setSelection(pulseRateTolerance - 4);
        value = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[6]));
    }

    /**
     * With the received packet, gets pulse rate 3 and display on the screen.
     * @param data The received packet.
     */
    private void downloadPulseRate3(byte[] data) {
        int pulseRateTolerance = Integer.parseInt(Converters.getDecimalValue(data[8]));
        pulse_rate_editText.setText(Converters.getDecimalValue(data[7]));
        pulse_rate_tolerance_spinner.setSelection(pulseRateTolerance - 4);
        value = (Integer.parseInt(Converters.getDecimalValue(data[7])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[8]));
    }

    /**
     * With the received packet, gets pulse rate 4 and display on the screen.
     * @param data The received packet.
     */
    private void downloadPulseRate4(byte[] data) {
        int pulseRateTolerance = Integer.parseInt(Converters.getDecimalValue(data[10]));
        pulse_rate_editText.setText(Converters.getDecimalValue(data[9]));
        pulse_rate_tolerance_spinner.setSelection(pulseRateTolerance - 4);
        value = (Integer.parseInt(Converters.getDecimalValue(data[9])) * 100) + Integer.parseInt(Converters.getDecimalValue(data[10]));
    }
}