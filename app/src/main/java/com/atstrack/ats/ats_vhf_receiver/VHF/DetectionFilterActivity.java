package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.HashMap;
import java.util.Map;

public class DetectionFilterActivity extends BaseActivity {

    @BindView(R.id.pulse_rate_type_textView)
    TextView pulse_rate_type_textView;
    @BindView(R.id.matches_for_valid_pattern_textView)
    TextView matches_for_valid_pattern_textView;
    @BindView(R.id.matches_for_valid_pattern_linearLayout)
    LinearLayout matches_for_valid_pattern_linearLayout;
    @BindView(R.id.pulse_rates_linearLayout)
    LinearLayout pulse_rates_linearLayout;
    @BindView(R.id.max_pulse_rate_textView)
    TextView max_pulse_rate_textView;
    @BindView(R.id.min_pulse_rate_textView)
    TextView min_pulse_rate_textView;
    @BindView(R.id.optional_data_textView)
    TextView optional_data_textView;
    @BindView(R.id.pulse_rate_type_imageView)
    ImageView pulse_rate_type_imageView;
    @BindView(R.id.target_pulse_rate_linearLayout)
    LinearLayout target_pulse_rate_linearLayout;
    @BindView(R.id.pr1_textView)
    TextView pr1_textView;
    @BindView(R.id.pr1_tolerance_textView)
    TextView pr1_tolerance_textView;
    @BindView(R.id.pr2_textView)
    TextView pr2_textView;
    @BindView(R.id.pr2_tolerance_textView)
    TextView pr2_tolerance_textView;

    private final static String TAG = DetectionFilterActivity.class.getSimpleName();

    private Map<String, Object> originalData;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                switch (result.getResultCode()) {
                    case ValueCodes.PULSE_RATE_TYPE_CODE: // Gets the modified pulse rate type
                        if (value == ValueCodes.FIXED_PULSE_RATE_CODE)
                            setVisibility("Fixed");
                        else if (value == ValueCodes.VARIABLE_PULSE_RATE_CODE)
                            setVisibility("Variable");
                        else if (value == ValueCodes.CODED_CODE)
                            setVisibility("Coded");
                        break;
                    case ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE: // Gets the modified matches for valid pattern
                        matches_for_valid_pattern_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MAX_PULSE_RATE_CODE: // Gets the modified max pulse rate
                        max_pulse_rate_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MIN_PULSE_RATE_CODE:
                        min_pulse_rate_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.DATA_CALCULATION_TYPE_CODE:
                        switch (value) {
                            case 0:
                                optional_data_textView.setText(R.string.lb_none);
                                break;
                            case 6:
                                optional_data_textView.setText(R.string.lb_temperature);
                                break;
                        }
                        break;
                    case ValueCodes.PULSE_RATE_1_CODE:
                        pr1_textView.setText(String.valueOf(value / 100));
                        pr1_tolerance_textView.setText(String.valueOf(value % 100));
                        break;
                    case ValueCodes.PULSE_RATE_2_CODE:
                        pr2_textView.setText(String.valueOf(value / 100));
                        pr2_tolerance_textView.setText(String.valueOf(value % 100));
                        break;
                }
            });

    /**
     * Writes the modified tx type data by the user.
     */
    private void setDetectionFilter() {
        byte[] b = new byte[12];
        b[0] = (byte) 0x47;
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                b = new byte[] {(byte) 0x47, (byte) 0x08, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) Integer.parseInt(pr1_textView.getText().toString()), (byte) Integer.parseInt(pr1_tolerance_textView.getText().toString()),
                        (byte) Integer.parseInt(pr2_textView.getText().toString()), (byte) Integer.parseInt(pr2_tolerance_textView.getText().toString()),
                        0, 0, 0, 0, 0};
                break;
            case "Non Coded (Variable Pulse Rate)":
                int optionalData = optional_data_textView.getText().toString().equals(getString(R.string.lb_none)) ? 0 : 6;
                b = new byte[] {(byte) 0x47, (byte) 0x07, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) (Integer.parseInt(max_pulse_rate_textView.getText().toString())), 0,
                        (byte) (Integer.parseInt(min_pulse_rate_textView.getText().toString())), 0, 0, 0, 0, 0, (byte) optionalData};
                break;
            case "Coded":
                b[1] = (byte) 0x09;
                break;
        }
        boolean result = TransferBleData.writeDetectionFilter(b);
        Log.i(TAG, Converters.getHexValue(b));
        if (result) {
            SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
            SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
            sharedPreferencesEdit.putInt(ValueCodes.DETECTION_TYPE, Integer.parseInt(Converters.getDecimalValue(b[1])));
            sharedPreferencesEdit.apply();
            Message.showMessage(this, 0);
        } else
            Message.showMessage(this, 2);
    }

    @OnClick(R.id.pulse_rate_type_linearLayout)
    public void onClickPulseRateType(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_TYPE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.matches_for_valid_pattern_linearLayout)
    public void onClickMatchesValidPattern(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.max_pulse_rate_linearLayout)
    public void onClickMaxPulseRate(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MAX_PULSE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.min_pulse_rate_linearLayout)
    public void onClickMinPulseRate(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MIN_PULSE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.optional_data_linearLayout)
    public void onClickOptionalDataCalculations(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.DATA_CALCULATION_TYPE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr1_linearLayout)
    public void onClickPR1(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_1_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr2_linearLayout)
    public void onClickPR2(View v) {
        Intent intent = new Intent(this, ValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_2_CODE);
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_detection_filter;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.set_transmitter_type);
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = getIntent().getExtras().getString(ValueCodes.PARAMETER, "");
        if (parameter.isEmpty()) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
        originalData = new HashMap<>();
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.DETECTION_TYPE)) // Gets the tx type information
                    TransferBleData.readDetectionFilter();
                else if (parameter.equals(ValueCodes.SAVE)) // Saves the updated data
                    setDetectionFilter();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (Converters.getHexValue(packet[0]).equals("88")) return;
                if (parameter.equals(ValueCodes.DETECTION_TYPE)) //  Gets the tx type
                    downloadData(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (checkChanges()) {
                if (isDataCorrect()) {
                    parameter = ValueCodes.SAVE;
                    leServiceConnection.getBluetoothLeService().discovering();
                } else {
                    Message.showMessage(this, 1);
                }
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setVisibility(String value) {
        switch (value) {
            case "Coded":
                pulse_rate_type_textView.setText(R.string.lb_coded);
                matches_for_valid_pattern_linearLayout.setVisibility(View.GONE);
                target_pulse_rate_linearLayout.setVisibility(View.GONE);
                pulse_rates_linearLayout.setVisibility(View.GONE);
                break;
            case "Fixed":
                pulse_rate_type_textView.setText(R.string.lb_non_coded_fixed);
                matches_for_valid_pattern_linearLayout.setVisibility(View.VISIBLE);
                target_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                pulse_rates_linearLayout.setVisibility(View.GONE);
                matches_for_valid_pattern_textView.setText("3");
                pr1_textView.setText("0");
                pr1_tolerance_textView.setText("0");
                pr2_textView.setText("0");
                pr2_tolerance_textView.setText("0");
                break;
            case "Variable":
                pulse_rate_type_textView.setText(R.string.lb_non_coded_variable);
                matches_for_valid_pattern_linearLayout.setVisibility(View.VISIBLE);
                target_pulse_rate_linearLayout.setVisibility(View.GONE);
                pulse_rates_linearLayout.setVisibility(View.VISIBLE);
                matches_for_valid_pattern_textView.setText("3");
                max_pulse_rate_textView.setText("0");
                min_pulse_rate_textView.setText("0");
                optional_data_textView.setText(getString(R.string.lb_none));
                break;
        }
    }

    /**
     * With the received packet, gets tx type data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        Log.i(TAG, Converters.getHexValue(data));
        if (Converters.getHexValue(data[0]).equals("67")) {
            parameter = "";
            int pulseRateType = Integer.parseInt(Converters.getDecimalValue(data[1]));
            int matches = Integer.parseInt(Converters.getDecimalValue(data[2]));
            int pulseRate1 = 0;
            int pulseRate2 = 0;
            int pulseRate3 = 0;
            int pulseRate4 = 0;
            int pulseRateTolerance1 = 0;
            int pulseRateTolerance2 = 0;
            int pulseRateTolerance3 = 0;
            int pulseRateTolerance4 = 0;
            int maxPulseRate = 0;
            int minPulseRate = 0;
            int optionalData = 0;
            switch (Converters.getHexValue(data[1])) {
                case "09":
                    setVisibility("Coded");
                    break;
                case "08":
                    setVisibility("Fixed");

                    matches_for_valid_pattern_textView.setText(Converters.getDecimalValue(data[2]));
                    pr1_textView.setText(Converters.getDecimalValue(data[3]));
                    pr1_tolerance_textView.setText(Converters.getDecimalValue(data[4]));
                    pr2_textView.setText(Converters.getDecimalValue(data[5]));
                    pr2_tolerance_textView.setText(Converters.getDecimalValue(data[6]));

                    pulseRate1 = Integer.parseInt(Converters.getDecimalValue(data[3]));
                    pulseRateTolerance1 = Integer.parseInt(Converters.getDecimalValue(data[4]));
                    pulseRate2 = Integer.parseInt(Converters.getDecimalValue(data[5]));
                    pulseRateTolerance2 = Integer.parseInt(Converters.getDecimalValue(data[6]));
                    pulseRate3 = Integer.parseInt(Converters.getDecimalValue(data[7]));
                    pulseRateTolerance3 = Integer.parseInt(Converters.getDecimalValue(data[8]));
                    pulseRate4 = Integer.parseInt(Converters.getDecimalValue(data[9]));
                    pulseRateTolerance4 = Integer.parseInt(Converters.getDecimalValue(data[10]));
                    break;
                case "07":
                    setVisibility("Variable");

                    maxPulseRate = Integer.parseInt(Converters.getDecimalValue(data[3]));
                    minPulseRate = Integer.parseInt(Converters.getDecimalValue(data[5]));
                    optionalData = Integer.parseInt(Converters.getDecimalValue(data[11]));
                    matches_for_valid_pattern_textView.setText(Converters.getDecimalValue(data[2]));
                    max_pulse_rate_textView.setText(String.valueOf(maxPulseRate));
                    min_pulse_rate_textView.setText(String.valueOf(minPulseRate));
                    optional_data_textView.setText(Converters.getHexValue(data[11]).equals("06") ? R.string.lb_temperature : R.string.lb_none);
                    break;
            }
            originalData.put(ValueCodes.PULSE_RATE_TYPE, pulseRateType);
            originalData.put(ValueCodes.MATCHES, matches);
            originalData.put(ValueCodes.PULSE_RATE_1, pulseRate1);
            originalData.put(ValueCodes.PULSE_RATE_2, pulseRate2);
            originalData.put(ValueCodes.PULSE_RATE_3, pulseRate3);
            originalData.put(ValueCodes.PULSE_RATE_4, pulseRate4);
            originalData.put(ValueCodes.PULSE_RATE_TOLERANCE_1, pulseRateTolerance1);
            originalData.put(ValueCodes.PULSE_RATE_TOLERANCE_2, pulseRateTolerance2);
            originalData.put(ValueCodes.PULSE_RATE_TOLERANCE_3, pulseRateTolerance3);
            originalData.put(ValueCodes.PULSE_RATE_TOLERANCE_4, pulseRateTolerance4);
            originalData.put(ValueCodes.MAX_PULSE_RATE, maxPulseRate);
            originalData.put(ValueCodes.MIN_PULSE_RATE, minPulseRate);
            originalData.put(ValueCodes.DATA_CALCULATION, optionalData);
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x67 ...");
        }
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean checkChanges() {
        byte pulseRateType = 0;
        int matches = (matches_for_valid_pattern_textView.getText().equals(""))
                ? 0 : Integer.parseInt(matches_for_valid_pattern_textView.getText().toString());
        int pulseRate1 = 0;
        int pulseRate2 = 0;
        int pulseRate3 = 0;
        int pulseRate4 = 0;
        int pulseRateTolerance1 = 0;
        int pulseRateTolerance2 = 0;
        int pulseRateTolerance3 = 0;
        int pulseRateTolerance4 = 0;
        int maxPulseRate = 0;
        int minPulseRate = 0;
        int optionalData = 0;
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                pulseRateType = (byte) 0x08;
                pulseRate1 = Integer.parseInt(pr1_textView.getText().toString());
                pulseRate2 = Integer.parseInt(pr2_textView.getText().toString());
                pulseRateTolerance1 = Integer.parseInt(pr1_tolerance_textView.getText().toString());
                pulseRateTolerance2 = Integer.parseInt(pr2_tolerance_textView.getText().toString());
                break;
            case "Non Coded (Variable Pulse Rate)":
                pulseRateType = (byte) 0x07;
                maxPulseRate = Integer.parseInt(max_pulse_rate_textView.getText().toString());
                minPulseRate = Integer.parseInt(min_pulse_rate_textView.getText().toString());
                optionalData = optional_data_textView.getText().toString().equals(getString(R.string.lb_temperature)) ? 6 : 0;
                break;
            case "Coded":
                pulseRateType = (byte) 0x09;
                break;
        }
        return (int) originalData.get(ValueCodes.PULSE_RATE_TYPE) != Integer.parseInt(Converters.getDecimalValue(pulseRateType))
                || (int) originalData.get(ValueCodes.MATCHES) != matches || (int) originalData.get(ValueCodes.PULSE_RATE_1) != pulseRate1
                || (int) originalData.get(ValueCodes.PULSE_RATE_2) != pulseRate2 || (int) originalData.get(ValueCodes.PULSE_RATE_3) != pulseRate3
                || (int) originalData.get(ValueCodes.PULSE_RATE_4) != pulseRate4 || (int) originalData.get(ValueCodes.PULSE_RATE_TOLERANCE_1) != pulseRateTolerance1
                || (int) originalData.get(ValueCodes.PULSE_RATE_TOLERANCE_2) != pulseRateTolerance2 || (int) originalData.get(ValueCodes.PULSE_RATE_TOLERANCE_3) != pulseRateTolerance3
                || (int) originalData.get(ValueCodes.PULSE_RATE_TOLERANCE_4) != pulseRateTolerance4 || (int) originalData.get(ValueCodes.MAX_PULSE_RATE) != maxPulseRate
                || (int) originalData.get(ValueCodes.MIN_PULSE_RATE) != minPulseRate || (int) originalData.get(ValueCodes.DATA_CALCULATION) != optionalData;
    }

    private boolean isDataCorrect() {
        if (pulse_rate_type_textView.getText().toString().equals(getString(R.string.lb_non_coded_fixed)))
            return !pr1_textView.getText().equals("0");
        else if (pulse_rate_type_textView.getText().toString().equals(getString(R.string.lb_non_coded_variable))) {
            int max = Integer.parseInt(max_pulse_rate_textView.getText().toString());
            int min = Integer.parseInt(min_pulse_rate_textView.getText().toString());
            return (max > 0 && max <= 240) && (min > 0 && min <= 240) && (max > min);
        }
        return true;
    }
}