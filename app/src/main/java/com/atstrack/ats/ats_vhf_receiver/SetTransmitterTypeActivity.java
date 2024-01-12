package com.atstrack.ats.ats_vhf_receiver;

import androidx.annotation.Nullable;
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

import java.util.UUID;

public class SetTransmitterTypeActivity extends AppCompatActivity {

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

    private int[] data;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = true;
    private String parameter = "";

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
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
                    if (parameter.equals("txType")) { // Gets the tx type information
                        onClickTxType();
                    } else if (parameter.equals("save")) { // Saves the updated data
                        onClickSave();
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("txType")) { //  Gets the tx type
                        downloadData(packet);
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

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
     * Service name: Scan.
     * Characteristic name: Tx type.
     */
    private void onClickTxType() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Writes the modified tx type data by the user.
     * Service name: Scan.
     * Characteristic name: Tx type.
     */
    private void onClickSave() {
        byte txType;
        byte[] b = new byte[]{0};
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Fixed Pulse Rate":
                txType = (byte) 0x21;
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
            case "Variable Pulse Rate":
                txType = (byte) 0x22;
                b = new byte[] {(byte) 0x47, txType, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                        (byte) (Integer.parseInt(max_pulse_rate_textView.getText().toString()) / 256),
                        (byte) (Integer.parseInt(max_pulse_rate_textView.getText().toString()) % 256),
                        (byte) (Integer.parseInt(min_pulse_rate_textView.getText().toString()) / 256),
                        (byte) (Integer.parseInt(min_pulse_rate_textView.getText().toString()) % 256),
                        (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0};
                break;
        }

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result)
            showMessage(0);
        else
            showMessage(2);
    }

    @OnClick(R.id.pulse_rate_type_linearLayout)
    public void onClickPulseRateType(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.PULSE_RATE_TYPE);
        startActivityForResult(intent, SelectValueActivity.PULSE_RATE_TYPE);
    }

    @OnClick(R.id.matches_for_valid_pattern_linearLayout)
    public void onClickMatchesValidPattern(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.MATCHES_FOR_VALID_PATTERN);
        startActivityForResult(intent, SelectValueActivity.MATCHES_FOR_VALID_PATTERN);
    }

    @OnClick(R.id.max_pulse_rate_linearLayout)
    public void onClickMaxPulseRate(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.MAX_PULSE_RATE);
        startActivityForResult(intent, SelectValueActivity.MAX_PULSE_RATE);
    }

    @OnClick(R.id.min_pulse_rate_linearLayout)
    public void onClickMinPulseRate(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.MIN_PULSE_RATE);
        startActivityForResult(intent, SelectValueActivity.MIN_PULSE_RATE);
    }

    @OnClick(R.id.optional_data_linearLayout)
    public void onClickOptionalDataCalculations(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.DATA_CALCULATION_TYPES);
        startActivityForResult(intent, SelectValueActivity.DATA_CALCULATION_TYPES);
    }

    @OnClick(R.id.pr1_linearLayout)
    public void onClickPR1(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.PULSE_RATE_1);
        startActivityForResult(intent, SelectValueActivity.PULSE_RATE_1);
    }

    @OnClick(R.id.pr2_linearLayout)
    public void onClickPR2(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.PULSE_RATE_2);
        startActivityForResult(intent, SelectValueActivity.PULSE_RATE_2);
    }

    @OnClick(R.id.pr3_linearLayout)
    public void onClickPR3(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.PULSE_RATE_3);
        startActivityForResult(intent, SelectValueActivity.PULSE_RATE_3);
    }

    @OnClick(R.id.pr4_linearLayout)
    public void onClickPR4(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.PULSE_RATE_4);
        startActivityForResult(intent, SelectValueActivity.PULSE_RATE_4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_transmitter_type);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.set_transmitter_type);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();
        parameter = "txType";

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0)
            return;
        if (requestCode == SelectValueActivity.PULSE_RATE_TYPE) {
            if (resultCode == SelectValueActivity.FIXED_PULSE_RATE) {
                pulse_rate_type_textView.setText(R.string.lb_fixed_pulse_rate);
                target_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                pulse_rates_linearLayout.setVisibility(View.GONE);
            } else {
                pulse_rate_type_textView.setText(R.string.lb_variable_pulse_rate);
                target_pulse_rate_linearLayout.setVisibility(View.GONE);
                pulse_rates_linearLayout.setVisibility(View.VISIBLE);
            }
        }
        if (requestCode == SelectValueActivity.MATCHES_FOR_VALID_PATTERN) {
            matches_for_valid_pattern_textView.setText(String.valueOf(resultCode));
        }
        if (requestCode == SelectValueActivity.MAX_PULSE_RATE) {
            max_pulse_rate_textView.setText(String.valueOf(resultCode));
        }
        if (requestCode == SelectValueActivity.MIN_PULSE_RATE) {
            min_pulse_rate_textView.setText(String.valueOf(resultCode));
        }
        if (requestCode == SelectValueActivity.DATA_CALCULATION_TYPES) {
            switch (resultCode) {
                case SelectValueActivity.NONE:
                    optional_data_textView.setText(R.string.lb_none);
                    break;
                case SelectValueActivity.TEMPERATURE:
                    optional_data_textView.setText(R.string.lb_temperature);
                    break;
                case SelectValueActivity.PERIOD:
                    optional_data_textView.setText(R.string.lb_period);
                    break;
            }
        }
        if (requestCode == SelectValueActivity.PULSE_RATE_1) {
            pr1_textView.setText(String.valueOf(resultCode / 100));
            pr1_tolerance_textView.setText(String.valueOf(resultCode % 100));
        }
        if (requestCode == SelectValueActivity.PULSE_RATE_2) {
            pr2_textView.setText(String.valueOf(resultCode / 100));
            pr2_tolerance_textView.setText(String.valueOf(resultCode % 100));
        }
        if (requestCode == SelectValueActivity.PULSE_RATE_3) {
            pr3_textView.setText(String.valueOf(resultCode / 100));
            pr3_tolerance_textView.setText(String.valueOf(resultCode % 100));
        }
        if (requestCode == SelectValueActivity.PULSE_RATE_4) {
            pr4_textView.setText(String.valueOf(resultCode / 100));
            pr4_tolerance_textView.setText(String.valueOf(resultCode % 100));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }
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

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        dialog.setView(view);
        dialog.show();

        // The message disappears after a pre-defined period and will search for other available BLE devices again
        int MESSAGE_PERIOD = 3000;
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, MESSAGE_PERIOD);
    }

    /**
     * With the received packet, gets tx type data.
     *
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("67")) {
            parameter = "";

            switch (Converters.getHexValue(data[1])) {
                case "11":
                    pulse_rate_type_textView.setText(R.string.lb_eiler_coded);
                    pulse_rate_type_imageView.setVisibility(View.GONE);
                    pulse_rate_type_linearLayout.setEnabled(false);
                    break;
                case "12":
                    pulse_rate_type_textView.setText(R.string.lb_eiler_special_coded);
                    pulse_rate_type_imageView.setVisibility(View.GONE);
                    pulse_rate_type_linearLayout.setEnabled(false);
                    break;
                case "20":
                    pulse_rate_type_textView.setText(R.string.lb_non_coded);
                    matches_for_valid_pattern_linearLayout.setVisibility(View.VISIBLE);

                    matches_for_valid_pattern_textView.setText(Converters.getDecimalValue(data[2]));

                    this.data = new int[]{Integer.parseInt(Converters.getDecimalValue(data[1])),
                            Integer.parseInt(Converters.getDecimalValue(data[2])), 0, 0, 0, 0, 0, 0, 0, 0};
                    break;
                case "21":
                    pulse_rate_type_textView.setText(R.string.lb_fixed_pulse_rate);
                    target_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                    matches_for_valid_pattern_linearLayout.setVisibility(View.VISIBLE);

                    matches_for_valid_pattern_textView.setText(Converters.getDecimalValue(data[2]));
                    pr1_textView.setText(Converters.getDecimalValue(data[3]));
                    pr1_tolerance_textView.setText(Converters.getDecimalValue(data[4]));
                    pr2_textView.setText(Converters.getDecimalValue(data[5]));
                    pr2_tolerance_textView.setText(Converters.getDecimalValue(data[6]));
                    pr3_textView.setText(Converters.getDecimalValue(data[7]));
                    pr3_tolerance_textView.setText(Converters.getDecimalValue(data[8]));
                    pr4_textView.setText(Converters.getDecimalValue(data[9]));
                    pr4_tolerance_textView.setText(Converters.getDecimalValue(data[10]));

                    this.data = new int[]{Integer.parseInt(Converters.getDecimalValue(data[1])), Integer.parseInt(Converters.getDecimalValue(data[2])),
                            Integer.parseInt(Converters.getDecimalValue(data[3])), Integer.parseInt(Converters.getDecimalValue(data[4])),
                            Integer.parseInt(Converters.getDecimalValue(data[5])), Integer.parseInt(Converters.getDecimalValue(data[6])),
                            Integer.parseInt(Converters.getDecimalValue(data[7])), Integer.parseInt(Converters.getDecimalValue(data[8])),
                            Integer.parseInt(Converters.getDecimalValue(data[9])), Integer.parseInt(Converters.getDecimalValue(data[10]))};
                    break;
                case "22":
                    pulse_rate_type_textView.setText(R.string.lb_variable_pulse_rate);
                    target_pulse_rate_linearLayout.setVisibility(View.GONE);
                    matches_for_valid_pattern_linearLayout.setVisibility(View.VISIBLE);
                    pulse_rates_linearLayout.setVisibility(View.VISIBLE);

                    matches_for_valid_pattern_textView.setText(Converters.getDecimalValue(data[2]));
                    int maxPulse = (Integer.parseInt(Converters.getDecimalValue(data[3])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[4]));
                    int minPulse = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
                    max_pulse_rate_textView.setText(String.valueOf(maxPulse));
                    min_pulse_rate_textView.setText(String.valueOf(minPulse));
                    optional_data_textView.setText(R.string.lb_none);

                    this.data = new int[]{Integer.parseInt(Converters.getDecimalValue(data[1])), Integer.parseInt(Converters.getDecimalValue(data[2])),
                            Integer.parseInt(Converters.getDecimalValue(data[3])), Integer.parseInt(Converters.getDecimalValue(data[4])),
                            Integer.parseInt(Converters.getDecimalValue(data[5])), Integer.parseInt(Converters.getDecimalValue(data[6])), 0, 0, 0, 0};
                    break;
            }
        }
    }

    /**
     * Checks for changes to the default data.
     *
     * @return Returns true, if there are changes.
     */
    private boolean checkChanges() {
        byte txType;
        int matches = (matches_for_valid_pattern_textView.getText().equals("")) ? 0 : Integer.parseInt(matches_for_valid_pattern_textView.getText().toString());
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Fixed Pulse Rate":
                txType = (byte) 0x21;
                break;
            case "Variable Pulse Rate":
                txType = (byte) 0x22;
                break;
            default:
                return false;
        }

        int pr1 = 0;
        int pr2 = 0;
        int pr3 = 0;
        int pr4 = 0;
        int prt1 = 0;
        int prt2 = 0;
        int prt3 = 0;
        int prt4 = 0;
        if (Converters.getHexValue(txType).equals("21")) {
            pr1 = Integer.parseInt(pr1_textView.getText().toString());
            pr2 = Integer.parseInt(pr2_textView.getText().toString());
            pr3 = Integer.parseInt(pr3_textView.getText().toString());
            pr4 = Integer.parseInt(pr4_textView.getText().toString());
            prt1 = Integer.parseInt(pr1_tolerance_textView.getText().toString());
            prt2 = Integer.parseInt(pr2_tolerance_textView.getText().toString());
            prt3 = Integer.parseInt(pr3_tolerance_textView.getText().toString());
            prt4 = Integer.parseInt(pr4_tolerance_textView.getText().toString());
        } else if (Converters.getHexValue(txType).equals("22")) {
            pr1 = Integer.parseInt(max_pulse_rate_textView.getText().toString()) / 256;
            prt1 = Integer.parseInt(max_pulse_rate_textView.getText().toString()) % 256;
            pr2 = Integer.parseInt(min_pulse_rate_textView.getText().toString()) / 256;
            prt2 = Integer.parseInt(min_pulse_rate_textView.getText().toString()) % 256;
        }

        return (data[0] != Integer.parseInt(Converters.getDecimalValue(txType)) || data[1] != matches || data[2] != pr1 || data[3] != prt1 ||
                data[4] != pr2 || data[5] != prt2 || data[6] != pr3 || data[7] != prt3 || data[8] != pr4 || data[9] != prt4);
    }

    /**
     * Displays a message indicating whether the writing was successful.
     *
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
