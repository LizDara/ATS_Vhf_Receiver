package com.atstrack.ats.ats_vhf_receiver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

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
    @BindView(R.id.filter_type_textView)
    TextView filter_type_textView;
    @BindView(R.id.matches_for_valid_pattern_textView)
    TextView matches_for_valid_pattern_textView;
    @BindView(R.id.filter_type_option_linearLayout)
    LinearLayout filter_type_linearLayout;
    @BindView(R.id.matches_for_valid_pattern_linearLayout)
    LinearLayout matches_for_valid_pattern_linearLayout;
    @BindView(R.id.pulse_rate_type_imageView)
    ImageView pulse_rate_type_imageView;
    @BindView(R.id.target_pulse_rate_linearLayout)
    LinearLayout target_pulse_rate_linearLayout;
    @BindView(R.id.other_pulse_rates_linearLayout)
    LinearLayout other_pulse_rates_linearLayout;
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
    private boolean state = true;

    private int[] data;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG,"Unable to initialize Bluetooth");
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

    private boolean mConnected = false;
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
                    state = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals("txType")) {
                        onClickTxType();
                    } else if (parameter.equals("save")) {
                        onClickSave();
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("txType")) {
                        downloadData(packet);
                    }
                }
            }
            catch (Exception e) {
                Timber.tag("DCA:BR 198").e(e, "Unexpected error.");
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

    public void onClickTxType() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    public void onClickSave() {
        byte txType = (byte) 0x0;
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Eiler Coded":
                txType = (byte) 0x11;
                break;
            case "Eiler Special Coded":
                txType = (byte) 0x12;
                break;
            case "Non Coded":
                txType = (byte) 0x20;
                break;
            case "Fixed Pulse Rate":
                txType = (byte) 0x21;
                break;
            case "Variable Pulse Rate":
                txType = (byte) 0x22;
                break;
        }
        byte[] b;
        if (target_pulse_rate_linearLayout.getVisibility() == View.VISIBLE) {
            b = new byte[]{(byte) 0x47, txType, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                    (byte) Integer.parseInt(pr1_textView.getText().toString()), (byte) Integer.parseInt(pr1_tolerance_textView.getText().toString()),
                    (byte) Integer.parseInt(pr2_textView.getText().toString()), (byte) Integer.parseInt(pr2_tolerance_textView.getText().toString()),
                    (byte) Integer.parseInt(pr3_textView.getText().toString()), (byte) Integer.parseInt(pr3_tolerance_textView.getText().toString()),
                    (byte) Integer.parseInt(pr4_textView.getText().toString()), (byte) Integer.parseInt(pr4_tolerance_textView.getText().toString())};
        } else {
            b = new byte[]{(byte) 0x47, txType, (byte) Integer.parseInt(matches_for_valid_pattern_textView.getText().toString()),
                    (byte) Integer.parseInt(pr1_textView.getText().toString()), (byte) Integer.parseInt(pr1_tolerance_textView.getText().toString()),
                    (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0};
        }

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TX_TYPE;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, false);
        finish();
    }

    @OnClick(R.id.pulse_rate_type_linearLayout)
    public void onClickPulseRateType(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.PULSE_RATE_TYPE);
        startActivityForResult(intent, SelectValueActivity.PULSE_RATE_TYPE);
    }

    @OnClick(R.id.filter_type_linearLayout)
    public void onClickFilterType(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        if (pulse_rate_type_textView.getText().toString().contains("Fixed"))
            intent.putExtra("type", SelectValueActivity.FIXED_PULSE_RATE);
        else
            intent.putExtra("type", SelectValueActivity.VARIABLE_PULSE_RATE);
        startActivityForResult(intent, SelectValueActivity.FILTER_TYPE);
    }

    @OnClick(R.id.matches_for_valid_pattern_linearLayout)
    public void onClickMatchesValidPattern(View v) {
        Intent intent = new Intent(this, SelectValueActivity.class);
        intent.putExtra("type", SelectValueActivity.MATCHES_FOR_VALID_PATTERN);
        startActivityForResult(intent, SelectValueActivity.MATCHES_FOR_VALID_PATTERN);
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
                filter_type_textView.setText(R.string.lb_pattern_matching);
                target_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                other_pulse_rates_linearLayout.setVisibility(View.VISIBLE);
            } else {
                pulse_rate_type_textView.setText(R.string.lb_variable_pulse_rate);
                filter_type_textView.setText(R.string.lb_temperature);
                target_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                other_pulse_rates_linearLayout.setVisibility(View.GONE);
            }
        }
        if (requestCode == SelectValueActivity.FILTER_TYPE) {
            if (resultCode == SelectValueActivity.PATTERN_MATCHING)
                filter_type_textView.setText(R.string.lb_pattern_matching);
            if (resultCode == SelectValueActivity.PULSES_PER_SCAN_TIME)
                filter_type_textView.setText(R.string.lb_pulses_per_scan_time);
            if (resultCode == SelectValueActivity.TEMPERATURE)
                filter_type_textView.setText(R.string.lb_temperature);
            if (resultCode == SelectValueActivity.PERIOD)
                filter_type_textView.setText(R.string.lb_period);
            if (resultCode == SelectValueActivity.ALTITUDE)
                filter_type_textView.setText(R.string.lb_altitude);
            if (resultCode == SelectValueActivity.DEPTH)
                filter_type_textView.setText(R.string.lb_depth);
        }
        if (requestCode == SelectValueActivity.MATCHES_FOR_VALID_PATTERN) {
            matches_for_valid_pattern_textView.setText(String.valueOf(resultCode));
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
            Log.d(TAG,"Connect request result= " + result);
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
        if (item.getItemId() == android.R.id.home) { //hago un case por si en un futuro agrego mas opciones
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
        if (!mConnected && !state)
            showMessageDisconnect();
        return true;
    }

    private void showMessageDisconnect() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view =inflater.inflate(R.layout.disconnect_message, null);
        final androidx.appcompat.app.AlertDialog dialog = new AlertDialog.Builder(this).create();

        dialog.setView(view);
        dialog.show();

        int MESSAGE_PERIOD = 3000;
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, MESSAGE_PERIOD);
    }

    public void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("67")) {
            Log.i(TAG, Converters.getHexValue(data));
            parameter = "";
            if (Converters.getHexValue(data[1]).equals("11")) {
                pulse_rate_type_textView.setText(R.string.lb_eiler_coded);
                pulse_rate_type_imageView.setVisibility(View.GONE);
                pulse_rate_type_linearLayout.setEnabled(false);
                matches_for_valid_pattern_linearLayout.setVisibility(View.GONE);
            } else if (Converters.getHexValue(data[1]).equals("12")) {
                pulse_rate_type_textView.setText(R.string.lb_eiler_special_coded);
                pulse_rate_type_imageView.setVisibility(View.GONE);
                pulse_rate_type_linearLayout.setEnabled(false);
                matches_for_valid_pattern_linearLayout.setVisibility(View.GONE);
            } else if (Converters.getHexValue(data[1]).equals("20") || Converters.getHexValue(data[1]).equals("00")) {
                pulse_rate_type_textView.setText(R.string.lb_non_coded);
            } else if (Converters.getHexValue(data[1]).equals("21")) {
                pulse_rate_type_textView.setText(R.string.lb_fixed_pulse_rate);
                target_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
            } else if (Converters.getHexValue(data[1]).equals("22")) {
                pulse_rate_type_textView.setText(R.string.lb_variable_pulse_rate);
                target_pulse_rate_linearLayout.setVisibility(View.VISIBLE);
                other_pulse_rates_linearLayout.setVisibility(View.GONE);
            }
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
        }
    }

    public boolean checkChanges() {
        byte txType = (byte) 0x0;
        switch (pulse_rate_type_textView.getText().toString()) {
            case "Eiler Coded":
                txType = (byte) 0x11;
                break;
            case "Eiler Special Coded":
                txType = (byte) 0x12;
                break;
            case "Non Coded":
                txType = (byte) 0x20;
                break;
            case "Fixed Pulse Rate":
                txType = (byte) 0x21;
                break;
            case "Variable Pulse Rate":
                txType = (byte) 0x22;
                break;
        }

        int matches = Integer.parseInt(matches_for_valid_pattern_textView.getText().toString());
        int pr1 = Integer.parseInt(pr1_textView.getText().toString());
        int pr2 = Integer.parseInt(pr2_textView.getText().toString());
        int pr3 = Integer.parseInt(pr3_textView.getText().toString());
        int pr4 = Integer.parseInt(pr4_textView.getText().toString());
        int prt1 = Integer.parseInt(pr1_tolerance_textView.getText().toString());
        int prt2 = Integer.parseInt(pr2_tolerance_textView.getText().toString());
        int prt3 = Integer.parseInt(pr3_tolerance_textView.getText().toString());
        int prt4 = Integer.parseInt(pr4_tolerance_textView.getText().toString());

        return (data[0] != Integer.parseInt(Converters.getDecimalValue(txType)) || data[1] != matches || data[2] != pr1 || data[3] != prt1 ||
                data[4] != pr2 || data[5] != prt2 || data[6] != pr3 || data[7] != prt3 || data[8] != pr4 || data[9] != prt4);
    }
}
