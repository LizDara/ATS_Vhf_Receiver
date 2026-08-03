package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfEnterCoefficientBinding;

public class EnterCoefficientActivity extends BaseActivity {
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getIntent().getStringExtra(ValueCodes.TYPE);
        binding = ActivityVhfEnterCoefficientBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnPlusMinus.setOnClickListener(v -> {
            String number = ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString();
            if (((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().equals(getString(R.string.lbl_vhf_tables_enter_coef_a)) ||
                    ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().equals(getString(R.string.lbl_vhf_tables_enter_coef_b)) ||
                    ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().equals(getString(R.string.lbl_vhf_tables_enter_constant)) ||
                    ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().isEmpty()) {
                ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText("-");
                ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
                ((ActivityVhfEnterCoefficientBinding) binding).btnSaveCoefficient.setEnabled(true);
                ((ActivityVhfEnterCoefficientBinding) binding).btnSaveCoefficient.setAlpha(1);
            } else {
                if (number.startsWith("-"))
                    ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText(number.substring(1));
                else
                    ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText("-" + number);
            }
        });
        View.OnClickListener listener = v -> {
            Button button = (Button) v;
            if (((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().equals(getString(R.string.lbl_vhf_tables_enter_coef_a)) ||
                    ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().equals(getString(R.string.lbl_vhf_tables_enter_coef_b)) ||
                    ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().equals(getString(R.string.lbl_vhf_tables_enter_constant)) ||
                    ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().isEmpty()) {
                ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText(button.getText());
                ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
                ((ActivityVhfEnterCoefficientBinding) binding).btnSaveCoefficient.setEnabled(true);
                ((ActivityVhfEnterCoefficientBinding) binding).btnSaveCoefficient.setAlpha(1);
            } else if (((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().length() < 7) {
                String number = ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString();
                ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText(number + button.getText());
            }
        };
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnOneCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnTwoCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnThreeCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnFourCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnFiveCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnSixCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnSevenCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnEightCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnNineCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.btnZeroCoefficient.setOnClickListener(listener);
        ((ActivityVhfEnterCoefficientBinding) binding).includeCoefficientsNumber.imgDeleteCoefficient.setOnClickListener(v -> {
            String number = ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString();
            if (!number.isEmpty()) {
                ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText(number.substring(0, number.length() - 1));
                if (((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString().isEmpty()) {
                    ((ActivityVhfEnterCoefficientBinding) binding).btnSaveCoefficient.setEnabled(false);
                    ((ActivityVhfEnterCoefficientBinding) binding).btnSaveCoefficient.setAlpha((float) 0.6);
                }
            }
        });
        ((ActivityVhfEnterCoefficientBinding) binding).btnSaveCoefficient.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ValueCodes.VALUE, ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.getText().toString());
            intent.putExtra(ValueCodes.POSITION, position);
            setResult(ValueCodes.RESULT_OK, intent);
            finish();
        });

        String type = getIntent().getStringExtra(ValueCodes.TYPE);
        if (type.equals(getString(R.string.lbl_vhf_tables_coef_a))) {
            ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText(R.string.lbl_vhf_tables_enter_coef_a);
            position = -2;
        } else if (type.equals(getString(R.string.lbl_vhf_tables_coef_b))) {
            ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText(R.string.lbl_vhf_tables_enter_coef_b);
            position = -3;
        } else if (type.equals(getString(R.string.lbl_vhf_tables_constant))) {
            ((ActivityVhfEnterCoefficientBinding) binding).tvCoefficient.setText(R.string.lbl_vhf_tables_enter_constant);
            position = -4;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            setResult(ValueCodes.CANCELLED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}