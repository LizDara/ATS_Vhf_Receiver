package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import butterknife.BindView;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class DetectionFilterActivity extends BaseActivity {

    @BindView(R.id.tv_pulse_rate_type)
    TextView tv_pulse_rate_type;
    @BindView(R.id.tv_matches_for_valid_pattern)
    TextView tv_matches_for_valid_pattern;
    @BindView(R.id.layout_matches_for_valid_pattern)
    LinearLayout layout_matches_for_valid_pattern;
    @BindView(R.id.layout_pulse_rates)
    LinearLayout layout_pulse_rates;
    @BindView(R.id.tv_max_pulse_rate)
    TextView tv_max_pulse_rate;
    @BindView(R.id.tv_min_pulse_rate)
    TextView tv_min_pulse_rate;
    @BindView(R.id.tv_optional_data)
    TextView tv_optional_data;
    @BindView(R.id.layout_target_pulse_rate)
    LinearLayout layout_target_pulse_rate;
    @BindView(R.id.tv_pr1)
    TextView tv_pr1;
    @BindView(R.id.tv_pr1_tolerance)
    TextView tv_pr1_tolerance;
    @BindView(R.id.tv_pr2)
    TextView tv_pr2;
    @BindView(R.id.tv_pr2_tolerance)
    TextView tv_pr2_tolerance;
    @BindView(R.id.btn_save_changes_detection)
    Button btn_save_changes_detection;

    private DetectionFilter detectionFilter;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                switch (result.getResultCode()) {
                    case ValueCodes.PULSE_RATE_TYPE_CODE:
                        setVisibility(value);
                        break;
                    case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE:
                        tv_matches_for_valid_pattern.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MAX_PULSE_RATE_CODE:
                        tv_max_pulse_rate.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MIN_PULSE_RATE_CODE:
                        tv_min_pulse_rate.setText(String.valueOf(value));
                        break;
                    case ValueCodes.DATA_CALCULATION_TYPE_CODE:
                        if (value == 0)
                            tv_optional_data.setText(R.string.lb_none);
                        else if (value == ValueCodes.VARIABLE_TEMPERATURE)
                            tv_optional_data.setText(R.string.lb_temperature);
                        break;
                    case ValueCodes.PULSE_RATE_1_CODE:
                        tv_pr1.setText(String.valueOf(value / 100));
                        tv_pr1_tolerance.setText(String.valueOf(value % 100));
                        break;
                    case ValueCodes.PULSE_RATE_2_CODE:
                        tv_pr2.setText(String.valueOf(value / 100));
                        tv_pr2_tolerance.setText(String.valueOf(value % 100));
                        break;
                }
                boolean changed = existChanges();
                btn_save_changes_detection.setEnabled(changed);
                btn_save_changes_detection.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified tx type data by the user.
     */
    private void setDetectionFilter() {
        byte[] b = new byte[12];
        b[0] = (byte) 0x47;
        switch (tv_pulse_rate_type.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                b = new byte[] {(byte) 0x47, ValueCodes.FIXED, (byte) Integer.parseInt(tv_matches_for_valid_pattern.getText().toString()),
                        (byte) Integer.parseInt(tv_pr1.getText().toString()), (byte) Integer.parseInt(tv_pr1_tolerance.getText().toString()),
                        (byte) Integer.parseInt(tv_pr2.getText().toString()), (byte) Integer.parseInt(tv_pr2_tolerance.getText().toString()),
                        0, 0, 0, 0, 0};
                break;
            case "Non Coded (Variable Pulse Rate)":
                int optionalData = tv_optional_data.getText().toString().equals(getString(R.string.lb_none)) ? 0 : ValueCodes.VARIABLE_TEMPERATURE;
                b = new byte[] {(byte) 0x47, ValueCodes.VARIABLE, (byte) Integer.parseInt(tv_matches_for_valid_pattern.getText().toString()),
                        (byte) (Integer.parseInt(tv_max_pulse_rate.getText().toString())), 0,
                        (byte) (Integer.parseInt(tv_min_pulse_rate.getText().toString())), 0, 0, 0, 0, 0, (byte) optionalData};
                break;
            case "Coded":
                b[1] = ValueCodes.CODED;
                break;
        }
        boolean result = TransferBleData.writeDetectionFilter(b);
        if (result)
            finish();
    }

    @OnClick(R.id.layout_pulse_rate_type)
    public void onClickPulseRateType(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_TYPE_CODE);
        intent.putExtra(ValueCodes.VALUE, (int) detectionFilter.detectionType);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_matches_for_valid_pattern)
    public void onClickMatchesValidPattern(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.matches);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_max_pulse_rate)
    public void onClickMaxPulseRate(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MAX_PULSE_RATE_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.maxPulseRate);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_min_pulse_rate)
    public void onClickMinPulseRate(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MIN_PULSE_RATE_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.minPulseRate);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_optional_data)
    public void onClickOptionalDataCalculations(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.DATA_CALCULATION_TYPE_CODE);
        intent.putExtra(ValueCodes.VALUE, detectionFilter.optionalData);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_pr1)
    public void onClickPR1(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_1_CODE);
        intent.putExtra(ValueCodes.VALUE, (detectionFilter.pulseRate1 * 100) + detectionFilter.pulseRateTolerance1);
        launcher.launch(intent);
    }

    @OnClick(R.id.layout_pr2)
    public void onClickPR2(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_2_CODE);
        intent.putExtra(ValueCodes.VALUE, (detectionFilter.pulseRate2 * 100) + detectionFilter.pulseRateTolerance2);
        launcher.launch(intent);
    }

    @OnClick(R.id.btn_save_changes_detection)
    public void onClickSaveChanges(View v) {
        if (existChanges()) {
            if (isDataCorrect())
                setDetectionFilter();
            else {
                AlertDialog dialog = Dialogs.createAlertDialog(this, "Error", "Data Incorrect", false);
                dialogList.add(dialog);
                dialog.setOnDismissListener(d -> dialogList.remove(dialog));
            }
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
            setVisibility(data[1]);
            switch (data[1]) {
                case ValueCodes.CODED:
                    break;
                case ValueCodes.FIXED:
                    tv_matches_for_valid_pattern.setText(String.valueOf(detectionFilter.matches));
                    tv_pr1.setText(String.valueOf(detectionFilter.pulseRate1));
                    tv_pr1_tolerance.setText(String.valueOf(detectionFilter.pulseRateTolerance1));
                    tv_pr2.setText(String.valueOf(detectionFilter.pulseRate2));
                    tv_pr2_tolerance.setText(String.valueOf(detectionFilter.pulseRateTolerance2));
                    break;
                case ValueCodes.VARIABLE:
                    tv_matches_for_valid_pattern.setText(String.valueOf(detectionFilter.matches));
                    tv_max_pulse_rate.setText(String.valueOf(detectionFilter.maxPulseRate));
                    tv_min_pulse_rate.setText(String.valueOf(detectionFilter.minPulseRate));
                    tv_optional_data.setText(detectionFilter.optionalData == 6 ? R.string.lb_temperature : R.string.lb_none);
                    break;
            }
            btn_save_changes_detection.setEnabled(false);
            btn_save_changes_detection.setAlpha((float) 0.6);
        }
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.CODED) {
            tv_pulse_rate_type.setText(R.string.lb_coded);
            layout_matches_for_valid_pattern.setVisibility(View.GONE);
            layout_target_pulse_rate.setVisibility(View.GONE);
            layout_pulse_rates.setVisibility(View.GONE);
        } else if (view == ValueCodes.FIXED) {
            tv_pulse_rate_type.setText(R.string.lb_non_coded_fixed);
            layout_matches_for_valid_pattern.setVisibility(View.VISIBLE);
            layout_target_pulse_rate.setVisibility(View.VISIBLE);
            layout_pulse_rates.setVisibility(View.GONE);
            tv_matches_for_valid_pattern.setText("3");
            tv_pr1.setText("0");
            tv_pr1_tolerance.setText("0");
            tv_pr2.setText("0");
            tv_pr2_tolerance.setText("0");
        } else if (view == ValueCodes.VARIABLE) {
            tv_pulse_rate_type.setText(R.string.lb_non_coded_variable);
            layout_matches_for_valid_pattern.setVisibility(View.VISIBLE);
            layout_target_pulse_rate.setVisibility(View.GONE);
            layout_pulse_rates.setVisibility(View.VISIBLE);
            tv_matches_for_valid_pattern.setText("3");
            tv_max_pulse_rate.setText("0");
            tv_min_pulse_rate.setText("0");
            tv_optional_data.setText(getString(R.string.lb_none));
        }
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        DetectionFilter currentDetectionFilter = new DetectionFilter();
        currentDetectionFilter.matches = (tv_matches_for_valid_pattern.getText().equals(""))
                ? 0 : Integer.parseInt(tv_matches_for_valid_pattern.getText().toString());
        switch (tv_pulse_rate_type.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                currentDetectionFilter.detectionType = ValueCodes.FIXED;
                currentDetectionFilter.pulseRate1 = Integer.parseInt(tv_pr1.getText().toString());
                currentDetectionFilter.pulseRate2 = Integer.parseInt(tv_pr2.getText().toString());
                currentDetectionFilter.pulseRateTolerance1 = Integer.parseInt(tv_pr1_tolerance.getText().toString());
                currentDetectionFilter.pulseRateTolerance2 = Integer.parseInt(tv_pr2_tolerance.getText().toString());
                break;
            case "Non Coded (Variable Pulse Rate)":
                currentDetectionFilter.detectionType = ValueCodes.VARIABLE;
                currentDetectionFilter.maxPulseRate = Integer.parseInt(tv_max_pulse_rate.getText().toString());
                currentDetectionFilter.minPulseRate = Integer.parseInt(tv_min_pulse_rate.getText().toString());
                currentDetectionFilter.optionalData = tv_optional_data.getText().toString().equals(getString(R.string.lb_temperature)) ? ValueCodes.VARIABLE_TEMPERATURE : 0;
                break;
            case "Coded":
                currentDetectionFilter.detectionType = ValueCodes.CODED;
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
        if (tv_pulse_rate_type.getText().toString().equals(getString(R.string.lb_non_coded_fixed)))
            return !tv_pr1.getText().equals("0");
        else if (tv_pulse_rate_type.getText().toString().equals(getString(R.string.lb_non_coded_variable))) {
            int max = Integer.parseInt(tv_max_pulse_rate.getText().toString());
            int min = Integer.parseInt(tv_min_pulse_rate.getText().toString());
            return (max > 0 && max <= 240) && (min > 0 && min <= 240) && (max > min);
        }
        return true;
    }
}