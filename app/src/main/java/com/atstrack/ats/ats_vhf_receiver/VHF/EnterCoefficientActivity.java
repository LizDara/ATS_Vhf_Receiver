package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EnterCoefficientActivity extends AppCompatActivity {

    @BindView(R.id.coefficient_textView)
    TextView coefficient_textView;
    @BindView(R.id.save_coefficient_button)
    Button save_coefficient_button;

    @OnClick(R.id.plus_minus_button)
    public void onClickPlusMinus(View v) {
        String number = coefficient_textView.getText().toString();
        if (number.startsWith("-"))
            coefficient_textView.setText(number.substring(1));
        else
            coefficient_textView.setText("-" + number);
    }

    @OnClick({R.id.one_button, R.id.two_button, R.id.three_button, R.id.four_button, R.id.five_button, R.id.six_button, R.id.seven_button, R.id.eight_button, R.id.nine_button, R.id.zero_button})
    public void onClickNumber(View v) {
        if (coefficient_textView.getText().toString().length() < 6) {
            Button button = (Button) v;
            String number = coefficient_textView.getText().toString();
            coefficient_textView.setText(number + button.getText());
        }
    }

    @OnClick(R.id.delete_imageView)
    public void onClickDelete(View v) {
        String number = coefficient_textView.getText().toString();
        if (!number.isEmpty()) {
            coefficient_textView.setText(number.substring(0, number.length() - 1));
        } else {
            save_coefficient_button.setEnabled(false);
            save_coefficient_button.setAlpha((float) 0.6);
        }
    }

    @OnClick(R.id.save_coefficient_button)
    public void onClickSaveCoefficient(View v) {
        Intent intent = new Intent();
        intent.putExtra(ValueCodes.VALUE, coefficient_textView.getText().toString());
        intent.putExtra(ValueCodes.POSITION, -2);
        setResult(ValueCodes.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_enter_coefficient);
        ButterKnife.bind(this);
        ActivitySetting.setVhfToolbar(this, getIntent().getStringExtra(ValueCodes.TYPE));
        ActivitySetting.setReceiverStatus(this);

        String type = getIntent().getStringExtra(ValueCodes.TYPE);
        if (type.equals(getString(R.string.lb_coefficient_a)))
            coefficient_textView.setText(R.string.lb_enter_coefficient_a);
        else if (type.equals(getString(R.string.lb_coefficient_a)))
            coefficient_textView.setText(R.string.lb_enter_coefficient_b);
        else if (type.equals(getString(R.string.lb_coefficient_a)))
            coefficient_textView.setText(R.string.lb_enter_constant);
    }
}