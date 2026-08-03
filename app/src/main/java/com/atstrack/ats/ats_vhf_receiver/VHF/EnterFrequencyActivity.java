package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.core.content.ContextCompat;

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

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfEnterFrequencyBinding;

public class EnterFrequencyActivity extends BaseActivity {
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
            if (((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString().length() == 6) {
                int frequency = Integer.parseInt(((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString());
                if (frequency > baseFrequency && frequency <= frequencyRange) {
                    ((ActivityVhfEnterFrequencyBinding) binding).btnSaveChanges.setEnabled(true);
                    ((ActivityVhfEnterFrequencyBinding) binding).btnSaveChanges.setAlpha(1);
                    ((ActivityVhfEnterFrequencyBinding) binding).vLineFrequency.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.ghost));
                    ((ActivityVhfEnterFrequencyBinding) binding).tvEditFrequencyMessage.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.slate_gray));
                }
            } else {
                ((ActivityVhfEnterFrequencyBinding) binding).btnSaveChanges.setEnabled(false);
                ((ActivityVhfEnterFrequencyBinding) binding).btnSaveChanges.setAlpha((float) 0.6);
                ((ActivityVhfEnterFrequencyBinding) binding).vLineFrequency.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.tall_poppy));
                ((ActivityVhfEnterFrequencyBinding) binding).tvEditFrequencyMessage.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.tall_poppy));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getIntent().getStringExtra(ValueCodes.TITLE);
        binding = ActivityVhfEnterFrequencyBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        View.OnClickListener listener = v -> {
            if (((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString().length() >= 3 && ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString().length() < 6) {
                Button button = (Button) v;
                String number = ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString();
                ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.setText(number + button.getText());
            }
        };
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnOne.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnTwo.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnThree.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnFour.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnFive.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnSix.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnSeven.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnEight.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnNine.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.btnZero.setOnClickListener(listener);
        ((ActivityVhfEnterFrequencyBinding) binding).includeNumbers.imgDelete.setOnClickListener(v -> {
            String number = ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString();
            if (!number.isEmpty())
                ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.setText(number.substring(0, number.length() - 1));
        });
        ((ActivityVhfEnterFrequencyBinding) binding).btnSaveChanges.setOnClickListener(v -> {
            int newFrequency = Integer.parseInt(((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString());
            Intent intent = new Intent();
            intent.putExtra(ValueCodes.POSITION, position);
            intent.putExtra(ValueCodes.VALUE, newFrequency);
            setResult(ValueCodes.RESULT_OK, intent);
            finish();
        });

        baseFrequency = getIntent().getIntExtra(ValueCodes.BASE_FREQUENCY, 0);
        int range = getIntent().getIntExtra(ValueCodes.RANGE, 0);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        position = getIntent().getIntExtra(ValueCodes.POSITION, -2);
        ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.addTextChangedListener(textChangedListener);
        String message = "Frequency range is " + baseFrequency + " to " + frequencyRange;
        ((ActivityVhfEnterFrequencyBinding) binding).tvEditFrequencyMessage.setText(message);
        if (position == -1)
            ((ActivityVhfEnterFrequencyBinding) binding).btnSaveChanges.setText(R.string.btn_vhf_tables_add_frequency);
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
                    if (((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString().isEmpty() || ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.getText().toString().length() > 6) {
                        ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.setText(String.valueOf(finalBaseNumber));
                        ((ActivityVhfEnterFrequencyBinding) binding).tvFrequency.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.ebony_clay));
                    }
                });
                linearLayoutBaseFrequency.addView(buttonBaseFrequency);
                baseNumber++;
            }
            ((ActivityVhfEnterFrequencyBinding) binding).layoutNumberButtons.addView(linearLayoutBaseFrequency);
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