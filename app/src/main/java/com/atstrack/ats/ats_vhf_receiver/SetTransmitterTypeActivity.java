package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverStatus;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetTransmitterTypeActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
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
    @BindView(R.id.pr3_textView)
    TextView pr3_textView;
    @BindView(R.id.pr3_tolerance_textView)
    TextView pr3_tolerance_textView;
    @BindView(R.id.pr4_textView)
    TextView pr4_textView;
    @BindView(R.id.pr4_tolerance_textView)
    TextView pr4_tolerance_textView;
    @BindView(R.id.pulse_rate_type_linearLayout)
    LinearLayout pulse_rate_type_linearLayout;

    private final static String TAG = SetTransmitterTypeActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    Map<String, Object> originalData;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize())
                finish();
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = true;
    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals("txType")) // Gets the tx type information
                        onClickTxType();
                    else if (parameter.equals("save")) // Saves the updated data
                        onClickSave();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("txType")) //  Gets the tx type
                        downloadData(packet);
                }
            }
            catch (Exception e) {
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
                    case ValueCodes.PULSE_RATE_TYPE: // Gets the modified pulse rate type
                        if (value == ValueCodes.FIXED_PULSE_RATE)
                            setVisibility("Fixed");
                        else if (value == ValueCodes.VARIABLE_PULSE_RATE)
                            setVisibility("Variable");
                        else if (value == ValueCodes.CODED)
                            setVisibility("Coded");
                        break;
                    case ValueCodes.MATCHES_FOR_VALID_PATTERN: // Gets the modified matches for valid pattern
                        matches_for_valid_pattern_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MAX_PULSE_RATE: // Gets the modified max pulse rate
                        max_pulse_rate_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.MIN_PULSE_RATE:
                        min_pulse_rate_textView.setText(String.valueOf(value));
                        break;
                    case ValueCodes.DATA_CALCULATION_TYPES:
                        switch (value) {
                            case ValueCodes.NONE:
                                optional_data_textView.setText(R.string.lb_none);
                                break;
                            case ValueCodes.TEMPERATURE:
                                optional_data_textView.setText(R.string.lb_temperature);
                                break;
                            case ValueCodes.PERIOD:
                                optional_data_textView.setText(R.string.lb_period);
                                break;
                        }
                        break;
                    case ValueCodes.PULSE_RATE_1:
                        pr1_textView.setText(String.valueOf(value / 100));
                        pr1_tolerance_textView.setText(String.valueOf(value % 100));
                        break;
                    case ValueCodes.PULSE_RATE_2:
                        pr2_textView.setText(String.valueOf(value / 100));
                        pr2_tolerance_textView.setText(String.valueOf(value % 100));
                        break;
                    case ValueCodes.PULSE_RATE_3:
                        pr3_textView.setText(String.valueOf(value / 100));
                        pr3_tolerance_textView.setText(String.valueOf(value % 100));
                        break;
                    case ValueCodes.PULSE_RATE_4:
                        pr4_textView.setText(String.valueOf(value / 100));
                        pr4_tolerance_textView.setText(String.valueOf(value % 100));
                        break;
                }
            });

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * Requests a read for tx type data.
     */
    private void onClickTxType() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Writes the modified tx type data by the user.
     */
    private void onClickSave() {
        byte txType;
        byte[] b = new byte[11];
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Non Coded (Fixed Pulse Rate)":
                txType = (byte) 0x08;
                b = new byte[] {(byte) 0x47, txType, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) Integer.parseInt(pr1_textView.getText().toString()),
                        (!pr1_textView.getText().toString().equals("0")) ? (byte) Integer.parseInt(pr1_tolerance_textView.getText().toString()) : 0,
                        (byte) Integer.parseInt(pr2_textView.getText().toString()),
                        (!pr2_textView.getText().toString().equals("0")) ? (byte) Integer.parseInt(pr2_tolerance_textView.getText().toString()) : 0,
                        (byte) Integer.parseInt(pr3_textView.getText().toString()),
                        (!pr3_textView.getText().toString().equals("0")) ? (byte) Integer.parseInt(pr3_tolerance_textView.getText().toString()) : 0,
                        (byte) Integer.parseInt(pr4_textView.getText().toString()),
                        (!pr4_textView.getText().toString().equals("0")) ? (byte) Integer.parseInt(pr4_tolerance_textView.getText().toString()) : 0};
                break;
            case "Non Coded (Variable Pulse Rate)":
                txType = (byte) 0x07;
                b = new byte[] {(byte) 0x47, txType, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) (Integer.parseInt(max_pulse_rate_textView.getText().toString()) / 256),
                        (byte) (Integer.parseInt(max_pulse_rate_textView.getText().toString()) % 256),
                        (byte) (Integer.parseInt(min_pulse_rate_textView.getText().toString()) / 256),
                        (byte) (Integer.parseInt(min_pulse_rate_textView.getText().toString()) % 256),
                        (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0};
                break;
            case "Coded":
                txType = (byte) 0x09;
                b = new byte[] {(byte) 0x47, txType, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0};
                break;
        }

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
            SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
            sharedPreferencesEdit.putInt("DetectionType", Integer.parseInt(Converters.getDecimalValue(b[1])));
            sharedPreferencesEdit.apply();
            receiverInformation.changeTxType(b[1]);
            showMessage(0);
        } else
            showMessage(2);
    }

    @OnClick(R.id.pulse_rate_type_linearLayout)
    public void onClickPulseRateType(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.PULSE_RATE_TYPE);
        launcher.launch(intent);
    }

    @OnClick(R.id.matches_for_valid_pattern_linearLayout)
    public void onClickMatchesValidPattern(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.MATCHES_FOR_VALID_PATTERN);
        launcher.launch(intent);
    }

    @OnClick(R.id.max_pulse_rate_linearLayout)
    public void onClickMaxPulseRate(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.MAX_PULSE_RATE);
        launcher.launch(intent);
    }

    @OnClick(R.id.min_pulse_rate_linearLayout)
    public void onClickMinPulseRate(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.MIN_PULSE_RATE);
        launcher.launch(intent);
    }

    @OnClick(R.id.optional_data_linearLayout)
    public void onClickOptionalDataCalculations(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.DATA_CALCULATION_TYPES);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr1_linearLayout)
    public void onClickPR1(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.PULSE_RATE_1);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr2_linearLayout)
    public void onClickPR2(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.PULSE_RATE_2);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr3_linearLayout)
    public void onClickPR3(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.PULSE_RATE_3);
        launcher.launch(intent);
    }

    @OnClick(R.id.pr4_linearLayout)
    public void onClickPR4(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", ValueCodes.PULSE_RATE_4);
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_transmitter_type);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.set_transmitter_type);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        receiverInformation = ReceiverInformation.getReceiverInformation();
        ReceiverStatus.setReceiverStatus(this);

        parameter = "txType";
        originalData = new HashMap();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (checkChanges()) {
                parameter = "save";
                mBluetoothLeService.discovering();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected)
            showDisconnectionMessage();
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "Back Button Pressed");
    }

    private void showDisconnectionMessage() {
        parameter = "";
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(view);
        dialog.show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD);
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
                    pr3_textView.setText(Converters.getDecimalValue(data[7]));
                    pr3_tolerance_textView.setText(Converters.getDecimalValue(data[8]));
                    pr4_textView.setText(Converters.getDecimalValue(data[9]));
                    pr4_tolerance_textView.setText(Converters.getDecimalValue(data[10]));

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
            originalData.put("PulseRateType", pulseRateType);
            originalData.put("Matches", matches);
            originalData.put("PulseRate1", pulseRate1);
            originalData.put("PulseRate2", pulseRate2);
            originalData.put("PulseRate3", pulseRate3);
            originalData.put("PulseRate4", pulseRate4);
            originalData.put("PulseRateTolerance1", pulseRateTolerance1);
            originalData.put("PulseRateTolerance2", pulseRateTolerance2);
            originalData.put("PulseRateTolerance3", pulseRateTolerance3);
            originalData.put("PulseRateTolerance4", pulseRateTolerance4);
            originalData.put("MaxPulseRate", maxPulseRate);
            originalData.put("MinPulseRate", minPulseRate);
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
                pulseRate3 = Integer.parseInt(pr3_textView.getText().toString());
                pulseRate4 = Integer.parseInt(pr4_textView.getText().toString());
                pulseRateTolerance1 = Integer.parseInt(pr1_tolerance_textView.getText().toString());
                pulseRateTolerance2 = Integer.parseInt(pr2_tolerance_textView.getText().toString());
                pulseRateTolerance3 = Integer.parseInt(pr3_tolerance_textView.getText().toString());
                pulseRateTolerance4 = Integer.parseInt(pr4_tolerance_textView.getText().toString());
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
        return (int) originalData.get("PulseRateType") != Integer.parseInt(Converters.getDecimalValue(pulseRateType))
                || (int) originalData.get("Matches") != matches || (int) originalData.get("PulseRate1") != pulseRate1
                || (int) originalData.get("PulseRate2") != pulseRate2 || (int) originalData.get("PulseRate3") != pulseRate3
                || (int) originalData.get("PulseRate4") != pulseRate4 || (int) originalData.get("PulseRateTolerance1") != pulseRateTolerance1
                || (int) originalData.get("PulseRateTolerance2") != pulseRateTolerance2 || (int) originalData.get("PulseRateTolerance3") != pulseRateTolerance3
                || (int) originalData.get("PulseRateTolerance4") != pulseRateTolerance4 || (int) originalData.get("MaxPulseRate") != maxPulseRate
                || (int) originalData.get("MinPulseRate") != minPulseRate;
    }

    /**
     * Displays a message indicating whether the writing was successful.
     * @param status This number indicates the writing status.
     */
    private void showMessage(int status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success!");
        if (status == 0) {
            builder.setMessage("Completed.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                finish();
            });
        } else if (status == 2) {
            builder.setMessage("Not completed.");
            builder.setPositiveButton("OK", null);
        }
        builder.show();
    }
}