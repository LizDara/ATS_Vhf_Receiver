package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ghost;
import static com.atstrack.ats.ats_vhf_receiver.R.color.slate_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.tall_poppy;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.border;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_delete;

public class EnterFrequencyActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.device_name_textView)
    TextView device_name_textView;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
    @BindView(R.id.percent_battery_textView)
    TextView percent_battery_textView;
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

    private final static String TAG = InputValueActivity.class.getSimpleName();

    private int position;
    private int baseFrequency;
    private int frequencyRange;

    private LinearLayout linearLayoutBaseFrequency;
    private Button buttonBaseFrequency;

    private ReceiverInformation receiverInformation;

    /**
     * Change the period while editing the pulse rate.
     */
    private TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (frequency_textView.getText().toString().length() == 6) {
                int frequency = Integer.parseInt(frequency_textView.getText().toString());
                if (frequency >= baseFrequency && frequency <= frequencyRange) {
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

    @OnClick(R.id.save_changes_button)
    public void onClickSaveChanges(View v) {
        int newFrequency = Integer.parseInt(frequency_textView.getText().toString());
        Intent intent = new Intent();
        intent.putExtra("position", position);
        intent.putExtra("frequency", newFrequency);
        setResult(ValueCodes.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_frequency);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(getIntent().getExtras().getString("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        // Gets the number of frequencies from that table
        baseFrequency = getIntent().getExtras().getInt("baseFrequency");
        int range = getIntent().getExtras().getInt("range");
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;

        frequency_textView.addTextChangedListener(textChangedListener);
        String message = "Frequency range is " + baseFrequency + " to " + frequencyRange;
        edit_frequency_message_textView.setText(message);

        position = getIntent().getExtras().getInt("position");
        if (position == -1)
            save_changes_button.setText(R.string.lb_add_frequency);

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

        Space space = new Space(this);
        space.setLayoutParams(newLinearLayoutParams());
        number_buttons_linearLayout.addView(space);

        int number = 1;
        for (int i = 0; i < 3; i++) {
            newBaseLinearLayout();
            for (int j = 0; j < 4; j++) {
                if (number == 10) {
                    Space spaceBaseFrequency = new Space(this);
                    spaceBaseFrequency.setLayoutParams(newButtonParams());
                    linearLayoutBaseFrequency.addView(spaceBaseFrequency);
                } else if (number == 11) {
                    ImageView imageViewBaseFrequency = new ImageView(this);
                    imageViewBaseFrequency.setBackground(ContextCompat.getDrawable(this, button_delete));
                    imageViewBaseFrequency.setImageDrawable(ContextCompat.getDrawable(this, ic_delete));
                    imageViewBaseFrequency.setLayoutParams(newButtonDeleteParams());
                    imageViewBaseFrequency.setPadding(50, 0, 50, 0);
                    imageViewBaseFrequency.setOnClickListener(view -> {
                        if (!frequency_textView.getText().toString().isEmpty()) {
                            String previous = frequency_textView.getText().toString();
                            frequency_textView.setText(previous.substring(0, previous.length() - 1));
                        }
                    });
                    linearLayoutBaseFrequency.addView(imageViewBaseFrequency);
                } else {
                    newBaseButton(number);
                    int finalNumber = number;
                    buttonBaseFrequency.setOnClickListener(view -> {
                        if (frequency_textView.getText().toString().length() >= 3 && frequency_textView.getText().toString().length() < 6) {
                            String previous = frequency_textView.getText().toString();
                            frequency_textView.setText(previous + finalNumber);
                        }
                    });
                    linearLayoutBaseFrequency.addView(buttonBaseFrequency);
                }
                if (number == 9) number = 0;
                else if (number == 0) number = 10;
                else number++;
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
        buttonBaseFrequency = new Button(this);
        buttonBaseFrequency.setBackground(ContextCompat.getDrawable(this, border));
        buttonBaseFrequency.setTextSize(16);
        buttonBaseFrequency.setTextColor(ContextCompat.getColor(this, ebony_clay));
        buttonBaseFrequency.setText(String.valueOf(baseNumber));
        buttonBaseFrequency.setLayoutParams(newButtonParams());
    }

    /**
     * Sets the margins for the LinearLayout.
     *
     * @return Returns a LayoutParams with the customize margins.
     */
    private LinearLayout.LayoutParams newLinearLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(0, 0, 0, 32);
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        return params;
    }

    private LinearLayout.LayoutParams newButtonParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(16, 0, 16, 0);
        params.weight = 1;
        return params;
    }

    private LinearLayout.LayoutParams newButtonDeleteParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(16, 0, 16, 0);
        params.height = GridLayout.LayoutParams.MATCH_PARENT;
        params.weight = 1;
        return params;
    }
}