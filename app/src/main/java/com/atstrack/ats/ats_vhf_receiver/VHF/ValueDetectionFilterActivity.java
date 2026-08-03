package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.Fragments.DataCalculationFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.MatchesNumberFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.MaxMinPulseRateFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.PulseRateTypesFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.PulseRateValuesFragment;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfFragmentBinding;

public class ValueDetectionFilterActivity extends BaseActivity {
    private int type;
    public int value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.lbl_vhf_defaults_mobile_select_val);
        binding = ActivityVhfFragmentBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        type = getIntent().getIntExtra(ValueCodes.TYPE, 0);
        value = getIntent().getIntExtra(ValueCodes.VALUE, 0);
        setVisibility(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            Intent intent = new Intent();
            if (type == ValueCodes.PULSE_RATE_1_CODE || type == ValueCodes.PULSE_RATE_2_CODE || type == ValueCodes.PULSE_RATE_3_CODE || type == ValueCodes.PULSE_RATE_4_CODE) {
                int pulseRate = value / 100;
                if (pulseRate < 0 || pulseRate > 240) {
                    showAlertDialog("Please enter valid pulse rate or tolerance values.");
                    return true;
                }
            } else if (type == ValueCodes.MAX_PULSE_RATE_CODE || type == ValueCodes.MIN_PULSE_RATE_CODE) {
                if (value < 1 || value > 240) {
                    showAlertDialog("Please enter valid pulse rate.");
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

    private void setVisibility(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            switch (type) {
                case ValueCodes.PULSE_RATE_TYPE_CODE:
                    ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(R.string.title_vhf_detection_pulse_rate_type);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new PulseRateTypesFragment(value))
                            .commit();
                    break;
                case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE:
                    ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(R.string.lbl_vhf_detection_matches_pattern);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new MatchesNumberFragment(value))
                            .commit();
                    break;
                case ValueCodes.PULSE_RATE_1_CODE:
                case ValueCodes.PULSE_RATE_2_CODE:
                case ValueCodes.PULSE_RATE_3_CODE:
                case ValueCodes.PULSE_RATE_4_CODE:
                    switch (type) {
                        case ValueCodes.PULSE_RATE_1_CODE:
                            ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(R.string.lbl_vhf_detection_target_pr1);
                            break;
                        case ValueCodes.PULSE_RATE_2_CODE:
                            ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(R.string.lbl_vhf_detection_target_pr2);
                            break;
                        case ValueCodes.PULSE_RATE_3_CODE:
                            ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(R.string.lbl_vhf_detection_target_pr3);
                            break;
                        case ValueCodes.PULSE_RATE_4_CODE:
                            ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(R.string.lbl_vhf_detection_target_pr4);
                            break;
                    }
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new PulseRateValuesFragment(type, value))
                            .commit();
                    break;
                case ValueCodes.MAX_PULSE_RATE_CODE:
                case ValueCodes.MIN_PULSE_RATE_CODE:
                    ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(type == ValueCodes.MIN_PULSE_RATE_CODE ? R.string.lbl_vhf_detection_min_pulse_rate : R.string.lbl_vhf_detection_max_pulse_rate);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new MaxMinPulseRateFragment(type, value))
                            .commit();
                    break;
                case ValueCodes.DATA_CALCULATION_TYPE_CODE:
                    ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(R.string.lbl_vhf_detection_optional_data);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new DataCalculationFragment(value))
                            .commit();
                    break;
            }
        }
    }

    private void showAlertDialog(String message) {
        AlertDialog dialog = Dialogs.createAlertDialog(this, "Invalid Format or Values", message, false);
        dialogList.add(dialog);
        dialog.setOnDismissListener(d -> dialogList.remove(dialog));
    }
}