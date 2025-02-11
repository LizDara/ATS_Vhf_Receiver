package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.HashMap;
import java.util.Map;

public class VhfDetectionFilterActivity extends AppCompatActivity {

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
    @BindView(R.id.pulse_rate_type_linearLayout)
    LinearLayout pulse_rate_type_linearLayout;

    private final static String TAG = VhfDetectionFilterActivity.class.getSimpleName();

    private final Context mContext = this;
    Map<String, Object> originalData;

    private final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();
    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    Message.showDisconnectionMessage(mContext, status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals(ValueCodes.DETECTION_TYPE)) // Gets the tx type information
                        TransferBleData.readDetectionFilter();
                    else if (parameter.equals(ValueCodes.SAVE)) // Saves the updated data
                        setDetectionFilter();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    if (parameter.equals(ValueCodes.DETECTION_TYPE)) //  Gets the tx type
                        downloadData(packet);
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getExtras().getInt(ValueCodes.VALUE);
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
                            case ValueCodes.NONE_CODE:
                                optional_data_textView.setText(R.string.lb_none);
                                break;
                            case ValueCodes.TEMPERATURE_CODE:
                                optional_data_textView.setText(R.string.lb_temperature);
                                break;
                            case ValueCodes.PERIOD_CODE:
                                optional_data_textView.setText(R.string.lb_period);
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
        byte[] b = new byte[11];
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                b = new byte[] {(byte) 0x47, (byte) 0x08, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) Integer.parseInt(pr1_textView.getText().toString()), (byte) Integer.parseInt(pr1_tolerance_textView.getText().toString()),
                        (byte) Integer.parseInt(pr2_textView.getText().toString()), (byte) Integer.parseInt(pr2_tolerance_textView.getText().toString()),
                        0, 0, 0, 0};
                break;
            case "Non Coded (Variable Pulse Rate)":
                b = new byte[] {(byte) 0x47, (byte) 0x07, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) (Integer.parseInt(max_pulse_rate_textView.getText().toString()) / 256),
                        (byte) (Integer.parseInt(max_pulse_rate_textView.getText().toString()) % 256),
                        (byte) (Integer.parseInt(min_pulse_rate_textView.getText().toString()) / 256),
                        (byte) (Integer.parseInt(min_pulse_rate_textView.getText().toString()) % 256),
                        (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0};
                break;
            case "Coded":
                b = new byte[] {(byte) 0x47, (byte) 0x09, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0};
                break;
        }
        boolean result = TransferBleData.writeDetectionFilter(b);
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
        Intent intent = new Intent(this, VhfValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_TYPE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.matches_for_valid_pattern_linearLayout)
    public void onClickMatchesValidPattern(View v) {
        Intent intent = new Intent(this, VhfValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MATCHES_FOR_VALID_PATTERN_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.max_pulse_rate_linearLayout)
    public void onClickMaxPulseRate(View v) {
        Intent intent = new Intent(this, VhfValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MAX_PULSE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.min_pulse_rate_linearLayout)
    public void onClickMinPulseRate(View v) {
        Intent intent = new Intent(this, VhfValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.MIN_PULSE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.optional_data_linearLayout)
    public void onClickOptionalDataCalculations(View v) {
        Intent intent = new Intent(this, VhfValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.DATA_CALCULATION_TYPE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr1_linearLayout)
    public void onClickPR1(View v) {
        Intent intent = new Intent(this, VhfValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_1_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr2_linearLayout)
    public void onClickPR2(View v) {
        Intent intent = new Intent(this, VhfValueDetectionFilterActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.PULSE_RATE_2_CODE);
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_detection_filter);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, R.string.set_transmitter_type);
        ActivitySetting.setReceiverStatus(this);

        parameter = ValueCodes.DETECTION_TYPE;
        originalData = new HashMap<>();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, Converters.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(leServiceConnection.getServiceConnection());
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

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
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
                break;
            case "Variable":
                pulse_rate_type_textView.setText(R.string.lb_non_coded_variable);
                matches_for_valid_pattern_linearLayout.setVisibility(View.VISIBLE);
                target_pulse_rate_linearLayout.setVisibility(View.GONE);
                pulse_rates_linearLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * With the received packet, gets tx type data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
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

                    matches_for_valid_pattern_textView.setText(Converters.getDecimalValue(data[2]));
                    maxPulseRate = (Integer.parseInt(Converters.getDecimalValue(data[3])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[4]));
                    minPulseRate = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
                    max_pulse_rate_textView.setText(String.valueOf(maxPulseRate));
                    min_pulse_rate_textView.setText(String.valueOf(minPulseRate));
                    optional_data_textView.setText(R.string.lb_none);
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
                || (int) originalData.get(ValueCodes.MIN_PULSE_RATE) != minPulseRate;
    }

    private boolean isDataCorrect() {
        if (pulse_rate_type_textView.getText().toString().equals("Non Coded (Fixed Pulse Rate)"))
            return !pr1_textView.getText().equals("0");
        return true;
    }
}