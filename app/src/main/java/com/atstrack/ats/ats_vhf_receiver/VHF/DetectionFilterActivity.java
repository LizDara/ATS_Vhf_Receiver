package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Messages;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class DetectionFilterActivity extends BaseActivity {

    @BindView(R.id.pulse_rate_type_textView)
    TextView pulse_rate_type_textView;
    @BindView(R.id.matches_for_valid_pattern_textView)
    TextView matches_for_valid_pattern_textView;
    @BindView(R.id.matches_for_valid_pattern_linearLayout)
    LinearLayout matches_for_valid_pattern_linearLayout;
    @BindView(R.id.pulse_rates_linearLayout)
    LinearLayout pulse_rates_linearLayout;
    @BindView(R.id.max_pulse_rate_textView)
    TextView max_pulse_rate_textView;
    @BindView(R.id.min_pulse_rate_textView)
    TextView min_pulse_rate_textView;
    @BindView(R.id.optional_data_textView)
    TextView optional_data_textView;
    @BindView(R.id.pulse_rate_type_imageView)
    ImageView pulse_rate_type_imageView;
    @BindView(R.id.target_pulse_rate_linearLayout)
    LinearLayout target_pulse_rate_linearLayout;
    @BindView(R.id.pr1_textView)
    TextView pr1_textView;
    @BindView(R.id.pr1_tolerance_textView)
    TextView pr1_tolerance_textView;
    @BindView(R.id.pr2_textView)
    TextView pr2_textView;
    @BindView(R.id.pr2_tolerance_textView)
    TextView pr2_tolerance_textView;
    @BindView(R.id.save_changes_detection_button)
    Button save_changes_detection_button;

    private DetectionFilter detectionFilter;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                switch (result.getResultCode()) {
                    case ValueCodes.PULSE_RATE_TYPE_CODE:
                        if (value == DetectionFilter.FIXED)
                            setVisibility("Fixed");
                        else if (value == DetectionFilter.VARIABLE)
                            setVisibility("Variable");
                        else if (value == DetectionFilter.CODED)
                            setVisibility("Coded");
                        break;
                    case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE:
                        matches_for_valid_pattern_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MAX_PULSE_RATE_CODE:
                        max_pulse_rate_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MIN_PULSE_RATE_CODE:
                        min_pulse_rate_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.DATA_CALCULATION_TYPE_CODE:
                        if (value == 0)
                            optional_data_textView.setText(R.string.lb_none);
                        else if (value == DetectionFilter.VARIABLE_TEMPERATURE)
                            optional_data_textView.setText(R.string.lb_temperature);
                        break;
                    case ValueCodes.PULSE_RATE_1_CODE:
                        pr1_textView.setText(String.valueOf(value / 100));
                        pr1_tolerance_textView.setText(String.valueOf(value % 100));
                        break;
                    case ValueCodes.PULSE_RATE_2_CODE:
                        pr2_textView.setText(String.valueOf(value / 100));
                        pr2_tolerance_textView.setText(String.valueOf(value % 100));
                        break;
                }
                boolean changed = existChanges();
                save_changes_detection_button.setEnabled(changed);
                save_changes_detection_button.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified tx type data by the user.
     */
    private void setDetectionFilter() {
        byte[] b = new byte[12];
        b[0] = (byte) 0x47;
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                b = new byte[] {(byte) 0x47, DetectionFilter.FIXED, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) Integer.parseInt(pr1_textView.getText().toString()), (byte) Integer.parseInt(pr1_tolerance_textView.getText().toString()),
                        (byte) Integer.parseInt(pr2_textView.getText().toString()), (byte) Integer.parseInt(pr2_tolerance_textView.getText().toString()),
                        0, 0, 0, 0, 0};
                break;
            case "Non Coded (Variable Pulse Rate)":
                int optionalData = optional_data_textView.getText().toString().equals(getString(R.string.lb_none)) ? 0 : DetectionFilter.VARIABLE_TEMPERATURE;
                b = new byte[] {(byte) 0x47, DetectionFilter.VARIABLE, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) (Integer.parseInt(max_pulse_rate_textView.getText().toString())), 0,
                        (byte) (Integer.parseInt(min_pulse_rate_textView.getText().toString())), 0, 0, 0, 0, 0, (byte) optionalData};
                break;
            case "Coded":
                b[1] = DetectionFilter.CODED;
                break;
        }
        boolean result = TransferBleData.writeDetectionFilter(b);
        if (result)
            finish();
    }

    @OnClick(R.id.pulse_rate_type_linearLayout)
    public void onClickPulseRateType(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_TYPE_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.detectionType);
        launcher.launch(intent);
    }

    @OnClick(R.id.matches_for_valid_pattern_linearLayout)
    public void onClickMatchesValidPattern(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.matches);
        launcher.launch(intent);
    }

    @OnClick(R.id.max_pulse_rate_linearLayout)
    public void onClickMaxPulseRate(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MAX_PULSE_RATE_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.maxPulseRate);
        launcher.launch(intent);
    }

    @OnClick(R.id.min_pulse_rate_linearLayout)
    public void onClickMinPulseRate(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MIN_PULSE_RATE_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.minPulseRate);
        launcher.launch(intent);
    }

    @OnClick(R.id.optional_data_linearLayout)
    public void onClickOptionalDataCalculations(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.DATA_CALCULATION_TYPE_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.optionalData);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr1_linearLayout)
    public void onClickPR1(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_1_CODE);
        intent.putExtra(ValueCodes.VALUE, (detectionFilter.pulseRate1 * 100) + detectionFilter.pulseRateTolerance1);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr2_linearLayout)
    public void onClickPR2(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_2_CODE);
        intent.putExtra(ValueCodes.VALUE, (detectionFilter.pulseRate2 * 100) + detectionFilter.pulseRateTolerance2);
        launcher.launch(intent);
    }

    @OnClick(R.id.save_changes_detection_button)
    public void onClickSaveChanges(View v) {
        if (existChanges()) {
            if (isDataCorrect())
                setDetectionFilter();
            else
                Messages.showMessage(this, 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_detection_filter;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.set_transmitter_type);
        super.onCreate(savedInstanceState);

        parameter = getIntent().getByteExtra(ValueCodes.PARAMETER, ValueCodes.NONE);
        if (parameter == ValueCodes.NONE) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void discoverCharacteristic() {
        if (parameter == ValueCodes.DETECTION_FILTER_COMMAND)
            TransferBleData.readDetectionFilter();
    }

    @Override
    protected void downloadData(byte[] data) {
        super.downloadData(data);
        if (data[0] == ValueCodes.DETECTION_FILTER_COMMAND) {
            parameter = ValueCodes.NONE;
            detectionFilter = new DetectionFilter(data);
            switch (data[1]) {
                case DetectionFilter.CODED:
                    setVisibility("Coded");
                    break;
                case DetectionFilter.FIXED:
                    setVisibility("Fixed");

                    matches_for_valid_pattern_textView.setText(String.valueOf(detectionFilter.matches));
                    pr1_textView.setText(String.valueOf(detectionFilter.pulseRate1));
                    pr1_tolerance_textView.setText(String.valueOf(detectionFilter.pulseRateTolerance1));
                    pr2_textView.setText(String.valueOf(detectionFilter.pulseRate2));
                    pr2_tolerance_textView.setText(String.valueOf(detectionFilter.pulseRateTolerance2));
                    break;
                case DetectionFilter.VARIABLE:
                    setVisibility("Variable");

                    matches_for_valid_pattern_textView.setText(String.valueOf(detectionFilter.matches));
                    max_pulse_rate_textView.setText(String.valueOf(detectionFilter.maxPulseRate));
                    min_pulse_rate_textView.setText(String.valueOf(detectionFilter.minPulseRate));
                    optional_data_textView.setText(detectionFilter.optionalData == 6 ? R.string.lb_temperature : R.string.lb_none);
                    break;
            }
            save_changes_detection_button.setEnabled(false);
            save_changes_detection_button.setAlpha((float) 0.6);
        }
    }

    private void setVisibility(String value) {
        switch (value) {
            case "Coded":
                pulse_rate_type_textView.setText(R.string.lb_coded);
                matches_for_valid_pattern_linearLayout.setVisibility(View.GONE);
                target_pulse_rate_linearLayout.setVisibility(View.GONE);
                pulse_rates_linearLayout.setVisibility(View.GONE);
                break;
            case "Fixed":
                pulse_rate_type_textView.setText(R.string.lb_non_coded_fixed);
                matches_for_valid_pattern_linearLayout.setVisibility(View.VISIBLE);
                target_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                pulse_rates_linearLayout.setVisibility(View.GONE);
                matches_for_valid_pattern_textView.setText("3");
                pr1_textView.setText("0");
                pr1_tolerance_textView.setText("0");
                pr2_textView.setText("0");
                pr2_tolerance_textView.setText("0");
                break;
            case "Variable":
                pulse_rate_type_textView.setText(R.string.lb_non_coded_variable);
                matches_for_valid_pattern_linearLayout.setVisibility(View.VISIBLE);
                target_pulse_rate_linearLayout.setVisibility(View.GONE);
                pulse_rates_linearLayout.setVisibility(View.VISIBLE);
                matches_for_valid_pattern_textView.setText("3");
                max_pulse_rate_textView.setText("0");
                min_pulse_rate_textView.setText("0");
                optional_data_textView.setText(getString(R.string.lb_none));
                break;
        }
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        DetectionFilter currentDetectionFilter = new DetectionFilter();
        currentDetectionFilter.matches = (matches_for_valid_pattern_textView.getText().equals(""))
                ? 0 : Integer.parseInt(matches_for_valid_pattern_textView.getText().toString());
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                currentDetectionFilter.detectionType = DetectionFilter.FIXED;
                currentDetectionFilter.pulseRate1 = Integer.parseInt(pr1_textView.getText().toString());
                currentDetectionFilter.pulseRate2 = Integer.parseInt(pr2_textView.getText().toString());
                currentDetectionFilter.pulseRateTolerance1 = Integer.parseInt(pr1_tolerance_textView.getText().toString());
                currentDetectionFilter.pulseRateTolerance2 = Integer.parseInt(pr2_tolerance_textView.getText().toString());
                break;
            case "Non Coded (Variable Pulse Rate)":
                currentDetectionFilter.detectionType = DetectionFilter.VARIABLE;
                currentDetectionFilter.maxPulseRate = Integer.parseInt(max_pulse_rate_textView.getText().toString());
                currentDetectionFilter.minPulseRate = Integer.parseInt(min_pulse_rate_textView.getText().toString());
                currentDetectionFilter.optionalData = optional_data_textView.getText().toString().equals(getString(R.string.lb_temperature)) ? DetectionFilter.VARIABLE_TEMPERATURE : 0;
                break;
            case "Coded":
                currentDetectionFilter.detectionType = DetectionFilter.CODED;
                break;
        }
        return detectionFilter.detectionType != currentDetectionFilter.detectionType || detectionFilter.matches != currentDetectionFilter.matches
                || detectionFilter.pulseRate1 != currentDetectionFilter.pulseRate1 || detectionFilter.pulseRate2 != currentDetectionFilter.pulseRate2
                || detectionFilter.pulseRate3 != currentDetectionFilter.pulseRate3 || detectionFilter.pulseRate4 != currentDetectionFilter.pulseRate4
                || detectionFilter.pulseRateTolerance1 != currentDetectionFilter.pulseRateTolerance1
                || detectionFilter.pulseRateTolerance2 != currentDetectionFilter.pulseRateTolerance2 || detectionFilter.pulseRateTolerance3 != currentDetectionFilter.pulseRateTolerance3
                || detectionFilter.pulseRateTolerance4 != currentDetectionFilter.pulseRateTolerance4 || detectionFilter.maxPulseRate != currentDetectionFilter.maxPulseRate
                || detectionFilter.minPulseRate != currentDetectionFilter.minPulseRate || detectionFilter.optionalData != currentDetectionFilter.optionalData;
    }

    private boolean isDataCorrect() {
        if (pulse_rate_type_textView.getText().toString().equals(getString(R.string.lb_non_coded_fixed)))
            return !pr1_textView.getText().equals("0");
        else if (pulse_rate_type_textView.getText().toString().equals(getString(R.string.lb_non_coded_variable))) {
            int max = Integer.parseInt(max_pulse_rate_textView.getText().toString());
            int min = Integer.parseInt(min_pulse_rate_textView.getText().toString());
            return (max > 0 && max <= 240) && (min > 0 && min <= 240) && (max > min);
        }
        return true;
    }
}