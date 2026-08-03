package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfDetectionFilterBinding;

public class DetectionFilterActivity extends BaseActivity {
    private DetectionFilter detectionFilter;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                switch (result.getResultCode()) {
                    case ValueCodes.PULSE_RATE_TYPE_CODE:
                        setVisibility(value);
                        break;
                    case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE:
                        ((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MAX_PULSE_RATE_CODE:
                        ((ActivityVhfDetectionFilterBinding) binding).tvMaxPulseRate.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MIN_PULSE_RATE_CODE:
                        ((ActivityVhfDetectionFilterBinding) binding).tvMinPulseRate.setText(String.valueOf(value));
                        break;
                    case ValueCodes.DATA_CALCULATION_TYPE_CODE:
                        if (value == 0)
                            ((ActivityVhfDetectionFilterBinding) binding).tvOptionalData.setText(R.string.lbl_vhf_manual_option_none);
                        else if (value == ValueCodes.VARIABLE_TEMPERATURE)
                            ((ActivityVhfDetectionFilterBinding) binding).tvOptionalData.setText(R.string.lbl_vhf_detection_temperature);
                        break;
                    case ValueCodes.PULSE_RATE_1_CODE:
                        ((ActivityVhfDetectionFilterBinding) binding).tvPr1.setText(String.valueOf(value / 100));
                        ((ActivityVhfDetectionFilterBinding) binding).tvPr1Tolerance.setText(String.valueOf(value % 100));
                        break;
                    case ValueCodes.PULSE_RATE_2_CODE:
                        ((ActivityVhfDetectionFilterBinding) binding).tvPr2.setText(String.valueOf(value / 100));
                        ((ActivityVhfDetectionFilterBinding) binding).tvPr2Tolerance.setText(String.valueOf(value % 100));
                        break;
                }
                boolean changed = existChanges();
                ((ActivityVhfDetectionFilterBinding) binding).btnSaveChangesDetection.setEnabled(changed);
                ((ActivityVhfDetectionFilterBinding) binding).btnSaveChangesDetection.setAlpha(changed ? (float) 1 : (float) 0.6);
            });

    /**
     * Writes the modified tx type data by the user.
     */
    private void setDetectionFilter() {
        byte[] b = new byte[12];
        b[0] = (byte) 0x47;
        switch (((ActivityVhfDetectionFilterBinding) binding).tvPulseRateType.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                b = new byte[] {(byte) 0x47, ValueCodes.FIXED, (byte) Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.getText().toString()),
                        (byte) Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvPr1.getText().toString()), (byte) Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvPr1Tolerance.getText().toString()),
                        (byte) Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvPr2.getText().toString()), (byte) Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvPr2Tolerance.getText().toString()),
                        0, 0, 0, 0, 0};
                break;
            case "Non Coded (Variable Pulse Rate)":
                int optionalData = ((ActivityVhfDetectionFilterBinding) binding).tvOptionalData.getText().toString().equals(getString(R.string.lbl_vhf_manual_option_none)) ? 0 : ValueCodes.VARIABLE_TEMPERATURE;
                b = new byte[] {(byte) 0x47, ValueCodes.VARIABLE, (byte) Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.getText().toString()),
                        (byte) (Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMaxPulseRate.getText().toString())), 0,
                        (byte) (Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMinPulseRate.getText().toString())), 0, 0, 0, 0, 0, (byte) optionalData};
                break;
            case "Coded":
                b[1] = ValueCodes.CODED;
                break;
        }
        boolean result = TransferBleData.writeDetectionFilter(b);
        if (result)
            finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.title_vhf_detection_main);
        binding = ActivityVhfDetectionFilterBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityVhfDetectionFilterBinding) binding).layoutPulseRateType.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_TYPE_CODE);
            intent.putExtra(ValueCodes.VALUE, (int) detectionFilter.detectionType);
            launcher.launch(intent);
        });
        ((ActivityVhfDetectionFilterBinding) binding).layoutMatchesForValidPattern.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE);
            intent.putExtra(ValueCodes.VALUE, detectionFilter.matches);
            launcher.launch(intent);
        });
        ((ActivityVhfDetectionFilterBinding) binding).layoutMaxPulseRate.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.MAX_PULSE_RATE_CODE);
            intent.putExtra(ValueCodes.VALUE, detectionFilter.maxPulseRate);
            launcher.launch(intent);
        });
        ((ActivityVhfDetectionFilterBinding) binding).layoutMinPulseRate.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.MIN_PULSE_RATE_CODE);
            intent.putExtra(ValueCodes.VALUE, detectionFilter.minPulseRate);
            launcher.launch(intent);
        });
        ((ActivityVhfDetectionFilterBinding) binding).layoutOptionalData.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.DATA_CALCULATION_TYPE_CODE);
            intent.putExtra(ValueCodes.VALUE, detectionFilter.optionalData);
            launcher.launch(intent);
        });
        ((ActivityVhfDetectionFilterBinding) binding).layoutPr1.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_1_CODE);
            intent.putExtra(ValueCodes.VALUE, (detectionFilter.pulseRate1 * 100) + detectionFilter.pulseRateTolerance1);
            launcher.launch(intent);
        });
        ((ActivityVhfDetectionFilterBinding) binding).layoutPr2.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
            intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_2_CODE);
            intent.putExtra(ValueCodes.VALUE, (detectionFilter.pulseRate2 * 100) + detectionFilter.pulseRateTolerance2);
            launcher.launch(intent);
        });
        ((ActivityVhfDetectionFilterBinding) binding).btnSaveChangesDetection.setOnClickListener(v -> {
            if (existChanges()) {
                if (isDataCorrect())
                    setDetectionFilter();
                else {
                    AlertDialog dialog = Dialogs.createAlertDialog(this, "Error", "Data Incorrect", false);
                    dialogList.add(dialog);
                    dialog.setOnDismissListener(d -> dialogList.remove(dialog));
                }
            }
        });

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
                    ((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.setText(String.valueOf(detectionFilter.matches));
                    ((ActivityVhfDetectionFilterBinding) binding).tvPr1.setText(String.valueOf(detectionFilter.pulseRate1));
                    ((ActivityVhfDetectionFilterBinding) binding).tvPr1Tolerance.setText(String.valueOf(detectionFilter.pulseRateTolerance1));
                    ((ActivityVhfDetectionFilterBinding) binding).tvPr2.setText(String.valueOf(detectionFilter.pulseRate2));
                    ((ActivityVhfDetectionFilterBinding) binding).tvPr2Tolerance.setText(String.valueOf(detectionFilter.pulseRateTolerance2));
                    break;
                case ValueCodes.VARIABLE:
                    ((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.setText(String.valueOf(detectionFilter.matches));
                    ((ActivityVhfDetectionFilterBinding) binding).tvMaxPulseRate.setText(String.valueOf(detectionFilter.maxPulseRate));
                    ((ActivityVhfDetectionFilterBinding) binding).tvMinPulseRate.setText(String.valueOf(detectionFilter.minPulseRate));
                    ((ActivityVhfDetectionFilterBinding) binding).tvOptionalData.setText(detectionFilter.optionalData == 6 ? R.string.lbl_vhf_detection_temperature : R.string.lbl_vhf_manual_option_none);
                    break;
            }
            ((ActivityVhfDetectionFilterBinding) binding).btnSaveChangesDetection.setEnabled(false);
            ((ActivityVhfDetectionFilterBinding) binding).btnSaveChangesDetection.setAlpha((float) 0.6);
        }
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.CODED) {
            ((ActivityVhfDetectionFilterBinding) binding).tvPulseRateType.setText(R.string.lbl_vhf_detection_type_coded);
            ((ActivityVhfDetectionFilterBinding) binding).layoutMatchesForValidPattern.setVisibility(View.GONE);
            ((ActivityVhfDetectionFilterBinding) binding).layoutTargetPulseRate.setVisibility(View.GONE);
            ((ActivityVhfDetectionFilterBinding) binding).layoutPulseRates.setVisibility(View.GONE);
        } else if (view == ValueCodes.FIXED) {
            ((ActivityVhfDetectionFilterBinding) binding).tvPulseRateType.setText(R.string.lbl_vhf_detection_type_non_coded_fixed);
            ((ActivityVhfDetectionFilterBinding) binding).layoutMatchesForValidPattern.setVisibility(View.VISIBLE);
            ((ActivityVhfDetectionFilterBinding) binding).layoutTargetPulseRate.setVisibility(View.VISIBLE);
            ((ActivityVhfDetectionFilterBinding) binding).layoutPulseRates.setVisibility(View.GONE);
            ((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.setText("3");
            ((ActivityVhfDetectionFilterBinding) binding).tvPr1.setText("0");
            ((ActivityVhfDetectionFilterBinding) binding).tvPr1Tolerance.setText("0");
            ((ActivityVhfDetectionFilterBinding) binding).tvPr2.setText("0");
            ((ActivityVhfDetectionFilterBinding) binding).tvPr2Tolerance.setText("0");
        } else if (view == ValueCodes.VARIABLE) {
            ((ActivityVhfDetectionFilterBinding) binding).tvPulseRateType.setText(R.string.lbl_vhf_detection_type_non_coded_variable);
            ((ActivityVhfDetectionFilterBinding) binding).layoutMatchesForValidPattern.setVisibility(View.VISIBLE);
            ((ActivityVhfDetectionFilterBinding) binding).layoutTargetPulseRate.setVisibility(View.GONE);
            ((ActivityVhfDetectionFilterBinding) binding).layoutPulseRates.setVisibility(View.VISIBLE);
            ((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.setText("3");
            ((ActivityVhfDetectionFilterBinding) binding).tvMaxPulseRate.setText("0");
            ((ActivityVhfDetectionFilterBinding) binding).tvMinPulseRate.setText("0");
            ((ActivityVhfDetectionFilterBinding) binding).tvOptionalData.setText(getString(R.string.lbl_vhf_manual_option_none));
        }
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        DetectionFilter currentDetectionFilter = new DetectionFilter();
        currentDetectionFilter.matches = (((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.getText().equals(""))
                ? 0 : Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMatchesForValidPattern.getText().toString());
        switch (((ActivityVhfDetectionFilterBinding) binding).tvPulseRateType.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                currentDetectionFilter.detectionType = ValueCodes.FIXED;
                currentDetectionFilter.pulseRate1 = Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvPr1.getText().toString());
                currentDetectionFilter.pulseRate2 = Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvPr2.getText().toString());
                currentDetectionFilter.pulseRateTolerance1 = Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvPr1Tolerance.getText().toString());
                currentDetectionFilter.pulseRateTolerance2 = Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvPr2Tolerance.getText().toString());
                break;
            case "Non Coded (Variable Pulse Rate)":
                currentDetectionFilter.detectionType = ValueCodes.VARIABLE;
                currentDetectionFilter.maxPulseRate = Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMaxPulseRate.getText().toString());
                currentDetectionFilter.minPulseRate = Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMinPulseRate.getText().toString());
                currentDetectionFilter.optionalData = ((ActivityVhfDetectionFilterBinding) binding).tvOptionalData.getText().toString().equals(getString(R.string.lbl_vhf_detection_temperature)) ? ValueCodes.VARIABLE_TEMPERATURE : 0;
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
        if (((ActivityVhfDetectionFilterBinding) binding).tvPulseRateType.getText().toString().equals(getString(R.string.lbl_vhf_detection_type_non_coded_fixed)))
            return !((ActivityVhfDetectionFilterBinding) binding).tvPr1.getText().equals("0");
        else if (((ActivityVhfDetectionFilterBinding) binding).tvPulseRateType.getText().toString().equals(getString(R.string.lbl_vhf_detection_type_non_coded_variable))) {
            int max = Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMaxPulseRate.getText().toString());
            int min = Integer.parseInt(((ActivityVhfDetectionFilterBinding) binding).tvMinPulseRate.getText().toString());
            return (max > 0 && max <= 240) && (min > 0 && min <= 240) && (max > min);
        }
        return true;
    }
}