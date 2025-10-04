package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ghost;
import static com.atstrack.ats.ats_vhf_receiver.R.color.slate_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.tall_poppy;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_number;

public class EnterFrequencyActivity extends BaseActivity {

    @BindView(R.id.frequency_textView)
    TextView frequency_textView;
    @BindView(R.id.line_frequency_view)
    View line_frequency_view;
    @BindView(R.id.edit_frequency_message_textView)
    TextView edit_frequency_message_textView;
    @BindView(R.id.number_buttons_linearLayout)
    LinearLayout number_buttons_linearLayout;
    @BindView(R.id.save_changes_button)
    Button save_changes_button;
    @BindView(R.id.one_button)
    Button one_button;

    private final static String TAG = EnterFrequencyActivity.class.getSimpleName();

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
            if (frequency_textView.getText().toString().length() == 6) {
                int frequency = Integer.parseInt(frequency_textView.getText().toString());
                if (frequency > baseFrequency && frequency <= frequencyRange) {
                    save_changes_button.setEnabled(true);
                    save_changes_button.setAlpha(1);
                    line_frequency_view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), ghost));
                    edit_frequency_message_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
                }
            } else {
                save_changes_button.setEnabled(false);
                save_changes_button.setAlpha((float) 0.6);
                line_frequency_view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), tall_poppy));
                edit_frequency_message_textView.setTextColor(ContextCompat.getColor(getBaseContext(), tall_poppy));
            }
        }
    };

    @OnClick({R.id.one_button, R.id.two_button, R.id.three_button, R.id.four_button, R.id.five_button, R.id.six_button, R.id.seven_button, R.id.eight_button, R.id.nine_button, R.id.zero_button})
    public void onClickNumber(View v) {
        if (frequency_textView.getText().toString().length() >= 3 && frequency_textView.getText().toString().length() < 6) {
            Button button = (Button) v;
            String number = frequency_textView.getText().toString();
            frequency_textView.setText(number + button.getText());
        }
    }

    @OnClick(R.id.delete_imageView)
    public void onClickDelete(View v) {
        String number = frequency_textView.getText().toString();
        if (!number.isEmpty())
            frequency_textView.setText(number.substring(0, number.length() - 1));
    }

    @OnClick(R.id.save_changes_button)
    public void onClickSaveChanges(View v) {
        int newFrequency = Integer.parseInt(frequency_textView.getText().toString());
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

        initializeCallback();
        baseFrequency = getIntent().getIntExtra(ValueCodes.BASE_FREQUENCY, 0);
        int range = getIntent().getIntExtra(ValueCodes.RANGE, 0);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        position = getIntent().getIntExtra(ValueCodes.POSITION, -2);
        frequency_textView.addTextChangedListener(textChangedListener);
        String message = "Frequency range is " + baseFrequency + " to " + frequencyRange;
        edit_frequency_message_textView.setText(message);
        if (position == -1)
            save_changes_button.setText(R.string.lb_add_frequency);
        createNumberButtons(range);
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                TransferBleData.notificationLog();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                if (Converters.getHexValue(packet[0]).equals("88")) // Battery
                    setBatteryPercent(packet);
                else if (Converters.getHexValue(packet[0]).equals("56")) // Sd Card
                    setSdCardStatus(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            setResult(ValueCodes.CANCELLED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 33)
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter(), 2);
        else
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
    }

    private void createNumberButtons(int range) {
        int baseNumber = baseFrequency / 1000;
        for (int i = 0; i < range / 4; i++) {
            newBaseLinearLayout();
            for (int j = 0; j < 4; j++) {
                newBaseButton(baseNumber);
                int finalBaseNumber = baseNumber;
                buttonBaseFrequency.setOnClickListener(view -> {
                    if (frequency_textView.getText().toString().isEmpty() || frequency_textView.getText().toString().length() > 6) {
                        frequency_textView.setText(String.valueOf(finalBaseNumber));
                        frequency_textView.setTextColor(ContextCompat.getColor(getBaseContext(), ebony_clay));
                    }
                });
                linearLayoutBaseFrequency.addView(buttonBaseFrequency);
                baseNumber++;
            }
            number_buttons_linearLayout.addView(linearLayoutBaseFrequency);
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
        buttonBaseFrequency.setBackground(ContextCompat.getDrawable(this, button_number));
        buttonBaseFrequency.setTextSize(16);
        buttonBaseFrequency.setTextColor(ContextCompat.getColor(this, ebony_clay));
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