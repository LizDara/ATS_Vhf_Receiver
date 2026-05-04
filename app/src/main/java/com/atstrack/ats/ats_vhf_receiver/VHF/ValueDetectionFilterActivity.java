package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;
import butterknife.OnClick;

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
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Messages;
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
        value = DetectionFilter.CODED;
    }

    @OnClick(R.id.fixed_pulse_rate_linearLayout)
    public void onClickFixedPulseRate(View v) {
        coded_imageView.setVisibility(View.GONE);
        fixed_pulse_rate_imageView.setVisibility(View.VISIBLE);
        variable_pulse_rate_imageView.setVisibility(View.GONE);
        value = DetectionFilter.FIXED;
    }

    @OnClick(R.id.variable_pulse_rate_linearLayout)
    public void onClickVariablePulseRate(View v) {
        coded_imageView.setVisibility(View.GONE);
        variable_pulse_rate_imageView.setVisibility(View.VISIBLE);
        fixed_pulse_rate_imageView.setVisibility(View.GONE);
        value = DetectionFilter.VARIABLE;
    }

    @OnClick(R.id.none_linearLayout)
    public void onClickNone(View v) {
        none_imageView.setVisibility(View.VISIBLE);
        temperature_imageView.setVisibility(View.GONE);
        value = 0;
    }

    @OnClick(R.id.temperature_linearLayout)
    public void onClickTemperature(View v) {
        temperature_imageView.setVisibility(View.VISIBLE);
        none_imageView.setVisibility(View.GONE);
        value = DetectionFilter.VARIABLE_TEMPERATURE;
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
        title = getString(R.string.lb_select_value);
        super.onCreate(savedInstanceState);

        type = getIntent().getIntExtra(ValueCodes.TYPE, 0);
        switch (type) {
            case ValueCodes.PULSE_RATE_TYPE_CODE: // Get the pulse rate type
                setVisibility("pulseRateTypes");
                byte detectionType = getIntent().getByteExtra(ValueCodes.VALUE, (byte) 0);
                setPulseRateType(detectionType);
                break;
            case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE: // Get the matches for valid pattern
                setVisibility("matches");
                int matches = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setMatchesForValidPattern(matches);
                break;
            case ValueCodes.MAX_PULSE_RATE_CODE: // Get the max pulse rate
                setVisibility("maxMin");
                title_toolbar.setText(R.string.max_pulse_rate);
                max_min_pulse_rate_textView.setText(R.string.lb_max_pulse_rate);
                int max = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setMaxMinPulseRate(max);
                break;
            case ValueCodes.MIN_PULSE_RATE_CODE: // Get the min pulse rate
                setVisibility("maxMin");
                title_toolbar.setText(R.string.min_pulse_rate);
                max_min_pulse_rate_textView.setText(R.string.lb_min_pulse_rate);
                int min = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setMaxMinPulseRate(min);
                break;
            case ValueCodes.DATA_CALCULATION_TYPE_CODE: // Get data calculation types
                setVisibility("calculation");
                int calculation = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setDataCalculation(calculation);
                break;
            case ValueCodes.PULSE_RATE_1_CODE: // Get the pulse rate 1
                setVisibility("pulseRateValues");
                title_toolbar.setText(R.string.target_pulse_rate_1);
                pulse_rate_textView.setText(R.string.lb_pr1);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr1_tolerance);
                int value1 = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setPulseRate(value1 / 100, value1 % 100);
                break;
            case ValueCodes.PULSE_RATE_2_CODE: // Get the pulse rate 2
                setVisibility("pulseRateValues");
                title_toolbar.setText(R.string.target_pulse_rate_2);
                pulse_rate_textView.setText(R.string.lb_pr2);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr2_tolerance);
                int value2 = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setPulseRate(value2 / 100, value2 % 100);
                break;
            case ValueCodes.PULSE_RATE_3_CODE: // Get the pulse rate 3
                setVisibility("pulseRateValues");
                title_toolbar.setText(R.string.target_pulse_rate_3);
                pulse_rate_textView.setText(R.string.lb_pr3);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr3_tolerance);
                int value3 = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setPulseRate(value3 / 100, value3 % 100);
                break;
            case ValueCodes.PULSE_RATE_4_CODE: // Get the pulse rate 4
                setVisibility("pulseRateValues");
                title_toolbar.setText(R.string.target_pulse_rate_4);
                pulse_rate_textView.setText(R.string.lb_pr4);
                pulse_rate_tolerance_textView.setText(R.string.lb_pr4_tolerance);
                int value4 = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setPulseRate(value4 / 100, value4 % 100);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            Intent intent = new Intent();
            if (type == ValueCodes.PULSE_RATE_1_CODE || type == ValueCodes.PULSE_RATE_2_CODE || type == ValueCodes.PULSE_RATE_3_CODE || type == ValueCodes.PULSE_RATE_4_CODE) {
                int pulseRate = Integer.parseInt(pulse_rate_editText.getText().toString());
                int tolerance = pulse_rate_tolerance_spinner.getSelectedItemPosition() + 4;
                if (pulseRate > 0 && pulseRate <= 240) {
                    value = (pulseRate * 100) + tolerance;
                } else {
                    Messages.showMessage(this, "Invalid Format or Values", "Please enter valid pulse rate or tolerance values.");
                    return true;
                }
            } else if (type == ValueCodes.MAX_PULSE_RATE_CODE || type == ValueCodes.MIN_PULSE_RATE_CODE) {
                value = Integer.parseInt(max_min_pulse_rate_editText.getText().toString());
                if (value < 1 || value > 240) {
                    Messages.showMessage(this, "Invalid Format or Values", "Please enter valid pulse rate.");
                    return true;
                }
            }
            intent.putExtra(ValueCodes.VALUE, value);
            setResult(type, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void setPulseRateType(byte detectionType) {
        setVisibility("pulseRateTypes");
        if (detectionType == DetectionFilter.CODED) {
            coded_imageView.setVisibility(View.VISIBLE);
            value = DetectionFilter.CODED;
        } else if (detectionType == DetectionFilter.FIXED) {
            fixed_pulse_rate_imageView.setVisibility(View.VISIBLE);
            value = DetectionFilter.FIXED;
        } else if (detectionType == DetectionFilter.VARIABLE) {
            variable_pulse_rate_imageView.setVisibility(View.VISIBLE);
            value = DetectionFilter.VARIABLE;
        }
    }

    private void setMatchesForValidPattern(int matches) {
        switch (matches) {
            case 2:
                two_imageView.setVisibility(View.VISIBLE);
                break;
            case 3:
                three_imageView.setVisibility(View.VISIBLE);
                break;
            case 4:
                four_imageView.setVisibility(View.VISIBLE);
                break;
            case 5:
                five_imageView.setVisibility(View.VISIBLE);
                break;
            case 6:
                six_imageView.setVisibility(View.VISIBLE);
                break;
            case 7:
                seven_imageView.setVisibility(View.VISIBLE);
                break;
            case 8:
                eight_imageView.setVisibility(View.VISIBLE);
                break;
        }
        value = matches;
    }

    private void setMaxMinPulseRate(int maxMin) {
        max_min_pulse_rate_editText.setText(String.valueOf(maxMin));
        double period = (maxMin == 0) ? 0 : (double) 60000 / maxMin;
        period_pulse_rate_textView.setText(String.format("%.2f ms (period)", period));
        value = maxMin;
    }

    private void setDataCalculation(int calculation) {
        if (calculation == DetectionFilter.VARIABLE_TEMPERATURE)
            temperature_imageView.setVisibility(View.VISIBLE);
        else
            none_imageView.setVisibility(View.VISIBLE);
        value = calculation;
    }

    private void setPulseRate(int pr, int prTolerance) {
        pulse_rate_editText.setText(String.valueOf(pr));
        pulse_rate_tolerance_spinner.setSelection(prTolerance - 4);
        value = (pr * 100) + prTolerance;
    }
}