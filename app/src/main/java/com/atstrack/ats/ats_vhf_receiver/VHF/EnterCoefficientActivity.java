package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.OnClick;

public class EnterCoefficientActivity extends BaseActivity {

    @BindView(R.id.tv_coefficient)
    TextView tv_coefficient;
    @BindView(R.id.btn_save_coefficient)
    Button btn_save_coefficient;

    private int position;

    @OnClick(R.id.btn_plus_minus)
    public void onClickPlusMinus(View v) {
        String number = tv_coefficient.getText().toString();
        if (tv_coefficient.getText().toString().equals(getString(R.string.lb_enter_coefficient_a)) ||
                tv_coefficient.getText().toString().equals(getString(R.string.lb_enter_coefficient_b)) ||
                tv_coefficient.getText().toString().equals(getString(R.string.lb_enter_constant)) ||
                tv_coefficient.getText().toString().isEmpty()) {
            tv_coefficient.setText("-");
            tv_coefficient.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
            btn_save_coefficient.setEnabled(true);
            btn_save_coefficient.setAlpha(1);
        } else {
            if (number.startsWith("-"))
                tv_coefficient.setText(number.substring(1));
            else
                tv_coefficient.setText("-" + number);
        }
    }

    @OnClick({R.id.btn_one_coefficient, R.id.btn_two_coefficient, R.id.btn_three_coefficient, R.id.btn_four_coefficient,
            R.id.btn_five_coefficient, R.id.btn_six_coefficient, R.id.btn_seven_coefficient, R.id.btn_eight_coefficient,
            R.id.btn_nine_coefficient, R.id.btn_zero_coefficient})
    public void onClickNumber(View v) {
        Button button = (Button) v;
        if (tv_coefficient.getText().toString().equals(getString(R.string.lb_enter_coefficient_a)) ||
                tv_coefficient.getText().toString().equals(getString(R.string.lb_enter_coefficient_b)) ||
                tv_coefficient.getText().toString().equals(getString(R.string.lb_enter_constant)) ||
                tv_coefficient.getText().toString().isEmpty()) {
            tv_coefficient.setText(button.getText());
            tv_coefficient.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
            btn_save_coefficient.setEnabled(true);
            btn_save_coefficient.setAlpha(1);
        } else if (tv_coefficient.getText().toString().length() < 7) {
            String number = tv_coefficient.getText().toString();
            tv_coefficient.setText(number + button.getText());
        }
    }

    @OnClick(R.id.img_delete_coefficient)
    public void onClickDelete(View v) {
        String number = tv_coefficient.getText().toString();
        if (!number.isEmpty()) {
            tv_coefficient.setText(number.substring(0, number.length() - 1));
            if (tv_coefficient.getText().toString().isEmpty()) {
                btn_save_coefficient.setEnabled(false);
                btn_save_coefficient.setAlpha((float) 0.6);
            }
        }
    }

    @OnClick(R.id.btn_save_coefficient)
    public void onClickSaveCoefficient(View v) {
        Intent intent = new Intent();
        intent.putExtra(ValueCodes.VALUE, tv_coefficient.getText().toString());
        intent.putExtra(ValueCodes.POSITION, position);
        setResult(ValueCodes.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_enter_coefficient;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getIntent().getStringExtra(ValueCodes.TYPE);
        super.onCreate(savedInstanceState);

        String type = getIntent().getStringExtra(ValueCodes.TYPE);
        if (type.equals(getString(R.string.lb_coefficient_a))) {
            tv_coefficient.setText(R.string.lb_enter_coefficient_a);
            position = -2;
        } else if (type.equals(getString(R.string.lb_coefficient_b))) {
            tv_coefficient.setText(R.string.lb_enter_coefficient_b);
            position = -3;
        } else if (type.equals(getString(R.string.lb_constant))) {
            tv_coefficient.setText(R.string.lb_enter_constant);
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