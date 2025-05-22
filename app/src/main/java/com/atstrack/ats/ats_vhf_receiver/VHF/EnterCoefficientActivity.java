package com.atstrack.ats.ats_vhf_receiver.VHF;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

    private int position;

    @OnClick(R.id.plus_minus_button)
    public void onClickPlusMinus(View v) {
        String number = coefficient_textView.getText().toString();
        if (coefficient_textView.getText().toString().equals(getString(R.string.lb_enter_coefficient_a)) ||
                coefficient_textView.getText().toString().equals(getString(R.string.lb_enter_coefficient_b)) ||
                coefficient_textView.getText().toString().equals(getString(R.string.lb_enter_constant)) ||
                coefficient_textView.getText().toString().isEmpty()) {
            coefficient_textView.setText("-");
            coefficient_textView.setTextColor(ContextCompat.getColor(this, ebony_clay));
            save_coefficient_button.setEnabled(true);
            save_coefficient_button.setAlpha(1);
        } else {
            if (number.startsWith("-"))
                coefficient_textView.setText(number.substring(1));
            else
                coefficient_textView.setText("-" + number);
        }
    }

    @OnClick({R.id.one_coefficient_button, R.id.two_coefficient_button, R.id.three_coefficient_button, R.id.four_coefficient_button,
            R.id.five_coefficient_button, R.id.six_coefficient_button, R.id.seven_coefficient_button, R.id.eight_coefficient_button,
            R.id.nine_coefficient_button, R.id.zero_coefficient_button})
    public void onClickNumber(View v) {
        Button button = (Button) v;
        if (coefficient_textView.getText().toString().equals(getString(R.string.lb_enter_coefficient_a)) ||
                coefficient_textView.getText().toString().equals(getString(R.string.lb_enter_coefficient_b)) ||
                coefficient_textView.getText().toString().equals(getString(R.string.lb_enter_constant)) ||
                coefficient_textView.getText().toString().isEmpty()) {
            coefficient_textView.setText(button.getText());
            coefficient_textView.setTextColor(ContextCompat.getColor(this, ebony_clay));
            save_coefficient_button.setEnabled(true);
            save_coefficient_button.setAlpha(1);
        } else if (coefficient_textView.getText().toString().length() < 7) {
            String number = coefficient_textView.getText().toString();
            coefficient_textView.setText(number + button.getText());
        }
    }

    @OnClick(R.id.delete_coefficient_imageView)
    public void onClickDelete(View v) {
        String number = coefficient_textView.getText().toString();
        if (!number.isEmpty()) {
            coefficient_textView.setText(number.substring(0, number.length() - 1));
            if (coefficient_textView.getText().toString().isEmpty()) {
                save_coefficient_button.setEnabled(false);
                save_coefficient_button.setAlpha((float) 0.6);
            }
        }
    }

    @OnClick(R.id.save_coefficient_button)
    public void onClickSaveCoefficient(View v) {
        Intent intent = new Intent();
        intent.putExtra(ValueCodes.VALUE, coefficient_textView.getText().toString());
        intent.putExtra(ValueCodes.POSITION, position);
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
        if (type.equals(getString(R.string.lb_coefficient_a))) {
            coefficient_textView.setText(R.string.lb_enter_coefficient_a);
            position = -2;
        } else if (type.equals(getString(R.string.lb_coefficient_b))) {
            coefficient_textView.setText(R.string.lb_enter_coefficient_b);
            position = -3;
        } else if (type.equals(getString(R.string.lb_constant))) {
            coefficient_textView.setText(R.string.lb_enter_constant);
            position = -4;
        }
    }
}