package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class EnterFrequencyActivity extends BaseActivity {

    @BindView(R.id.tv_frequency)
    TextView tv_frequency;
    @BindView(R.id.v_line_frequency)
    View v_line_frequency;
    @BindView(R.id.tv_edit_frequency_message)
    TextView tv_edit_frequency_message;
    @BindView(R.id.layout_number_buttons)
    LinearLayout layout_number_buttons;
    @BindView(R.id.btn_save_changes)
    Button btn_save_changes;

    private int position;
    private int baseFrequency;
    private int frequencyRange;

    private LinearLayout linearLayoutBaseFrequency;
    private Button buttonBaseFrequency;

    /**
     * Change the period while editing the pulse rate.
     */
    private final TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (tv_frequency.getText().toString().length() == 6) {
                int frequency = Integer.parseInt(tv_frequency.getText().toString());
                if (frequency > baseFrequency && frequency <= frequencyRange) {
                    btn_save_changes.setEnabled(true);
                    btn_save_changes.setAlpha(1);
                    v_line_frequency.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.ghost));
                    tv_edit_frequency_message.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.slate_gray));
                }
            } else {
                btn_save_changes.setEnabled(false);
                btn_save_changes.setAlpha((float) 0.6);
                v_line_frequency.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.tall_poppy));
                tv_edit_frequency_message.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.tall_poppy));
            }
        }
    };

    @OnClick({R.id.btn_one, R.id.btn_two, R.id.btn_three, R.id.btn_four, R.id.btn_five, R.id.btn_six, R.id.btn_seven, R.id.btn_eight, R.id.btn_nine, R.id.btn_zero})
    public void onClickNumber(View v) {
        if (tv_frequency.getText().toString().length() >= 3 && tv_frequency.getText().toString().length() < 6) {
            Button button = (Button) v;
            String number = tv_frequency.getText().toString();
            tv_frequency.setText(number + button.getText());
        }
    }

    @OnClick(R.id.img_delete)
    public void onClickDelete(View v) {
        String number = tv_frequency.getText().toString();
        if (!number.isEmpty())
            tv_frequency.setText(number.substring(0, number.length() - 1));
    }

    @OnClick(R.id.btn_save_changes)
    public void onClickSaveChanges(View v) {
        int newFrequency = Integer.parseInt(tv_frequency.getText().toString());
        Intent intent = new Intent();
        intent.putExtra(ValueCodes.POSITION, position);
        intent.putExtra(ValueCodes.VALUE, newFrequency);
        setResult(ValueCodes.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_enter_frequency;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getIntent().getStringExtra(ValueCodes.TITLE);
        super.onCreate(savedInstanceState);

        baseFrequency = getIntent().getIntExtra(ValueCodes.BASE_FREQUENCY, 0);
        int range = getIntent().getIntExtra(ValueCodes.RANGE, 0);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        position = getIntent().getIntExtra(ValueCodes.POSITION, -2);
        tv_frequency.addTextChangedListener(textChangedListener);
        String message = "Frequency range is " + baseFrequency + " to " + frequencyRange;
        tv_edit_frequency_message.setText(message);
        if (position == -1)
            btn_save_changes.setText(R.string.lb_add_frequency);
        createNumberButtons(range);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            setResult(ValueCodes.CANCELLED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createNumberButtons(int range) {
        int baseNumber = baseFrequency / 1000;
        for (int i = 0; i < range / 4; i++) {
            newBaseLinearLayout();
            for (int j = 0; j < 4; j++) {
                newBaseButton(baseNumber);
                int finalBaseNumber = baseNumber;
                buttonBaseFrequency.setOnClickListener(view -> {
                    if (tv_frequency.getText().toString().isEmpty() || tv_frequency.getText().toString().length() > 6) {
                        tv_frequency.setText(String.valueOf(finalBaseNumber));
                        tv_frequency.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.ebony_clay));
                    }
                });
                linearLayoutBaseFrequency.addView(buttonBaseFrequency);
                baseNumber++;
            }
            layout_number_buttons.addView(linearLayoutBaseFrequency);
        }
    }

    private void newBaseLinearLayout() {
        linearLayoutBaseFrequency = new LinearLayout(this);
        linearLayoutBaseFrequency.setLayoutParams(newLinearLayoutParams());
        linearLayoutBaseFrequency.setOrientation(LinearLayout.HORIZONTAL);
    }

    private void newBaseButton(int baseNumber) {
        buttonBaseFrequency = new Button(new ContextThemeWrapper(this, R.style.button_number_small), null, R.style.button_number_small);
        buttonBaseFrequency.setLayoutParams(newButtonParams());
        buttonBaseFrequency.setGravity(Gravity.CENTER);
        buttonBaseFrequency.setBackground(ContextCompat.getDrawable(this, R.drawable.button_number));
        buttonBaseFrequency.setTextSize(16);
        buttonBaseFrequency.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        buttonBaseFrequency.setText(String.valueOf(baseNumber));
    }

    private LinearLayout.LayoutParams newLinearLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(0, 0, 0, 32);
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        return params;
    }

    private LinearLayout.LayoutParams newButtonParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(8, 0, 8, 0);
        params.weight = 1;
        return params;
    }
}