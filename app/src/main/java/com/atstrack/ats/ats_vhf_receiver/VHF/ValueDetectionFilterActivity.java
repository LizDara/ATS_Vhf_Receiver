package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.Fragments.DataCalculationFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.MatchesNumberFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.MaxMinPulseRateFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.PulseRateTypesFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.PulseRateValuesFragment;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class ValueDetectionFilterActivity extends BaseActivity {
    @BindView(R.id.tv_title_toolbar)
    TextView tv_title_toolbar;

    private int type;
    public int value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_fragment;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.lb_select_value);
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
                    tv_title_toolbar.setText(R.string.pulse_rate_type_options);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new PulseRateTypesFragment(value))
                            .commit();
                    break;
                case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE:
                    tv_title_toolbar.setText(R.string.matches_for_valid_pattern);
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
                            tv_title_toolbar.setText(R.string.target_pulse_rate_1);
                            break;
                        case ValueCodes.PULSE_RATE_2_CODE:
                            tv_title_toolbar.setText(R.string.target_pulse_rate_2);
                            break;
                        case ValueCodes.PULSE_RATE_3_CODE:
                            tv_title_toolbar.setText(R.string.target_pulse_rate_3);
                            break;
                        case ValueCodes.PULSE_RATE_4_CODE:
                            tv_title_toolbar.setText(R.string.target_pulse_rate_4);
                            break;
                    }
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new PulseRateValuesFragment(type, value))
                            .commit();
                    break;
                case ValueCodes.MAX_PULSE_RATE_CODE:
                case ValueCodes.MIN_PULSE_RATE_CODE:
                    tv_title_toolbar.setText(type == ValueCodes.MIN_PULSE_RATE_CODE ? R.string.min_pulse_rate : R.string.max_pulse_rate);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new MaxMinPulseRateFragment(type, value))
                            .commit();
                    break;
                case ValueCodes.DATA_CALCULATION_TYPE_CODE:
                    tv_title_toolbar.setText(R.string.optional_data_calculations);
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