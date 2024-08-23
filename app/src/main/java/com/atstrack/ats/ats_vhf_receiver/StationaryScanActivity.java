package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverStatus;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.style.body_regular;

public class StationaryScanActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.ready_stationary_scan_LinearLayout)
    LinearLayout ready_stationary_scan_LinearLayout;
    @BindView(R.id.ready_stationary_textView)
    TextView ready_stationary_textView;
    @BindView(R.id.scan_rate_seconds_stationary_textView)
    TextView scan_rate_seconds_stationary_textView;
    @BindView(R.id.frequency_table_number_stationary_textView)
    TextView frequency_table_number_stationary_textView;
    @BindView(R.id.store_rate_minutes_stationary_textView)
    TextView store_rate_minutes_stationary_textView;
    @BindView(R.id.stationary_external_data_transfer_switch)
    SwitchCompat stationary_external_data_transfer_switch;
    @BindView(R.id.number_of_antennas_stationary_textView)
    TextView number_of_antennas_stationary_textView;
    @BindView(R.id.scan_timeout_seconds_stationary_textView)
    TextView scan_timeout_seconds_stationary_textView;
    @BindView(R.id.stationary_reference_frequency_switch)
    SwitchCompat stationary_reference_frequency_switch;
    @BindView(R.id.frequency_reference_stationary_textView)
    TextView frequency_reference_stationary_textView;
    @BindView(R.id.reference_frequency_store_rate_stationary_textView)
    TextView reference_frequency_store_rate_stationary_textView;
    @BindView(R.id.reference_frequency_stationary_linearLayout)
    LinearLayout reference_frequency_stationary_linearLayout;
    @BindView(R.id.reference_frequency_store_rate_stationary_linearLayout)
    LinearLayout reference_frequency_store_rate_stationary_linearLayout;
    @BindView(R.id.start_stationary_button)
    Button start_stationary_button;
    @BindView(R.id.stationary_result_linearLayout)
    LinearLayout stationary_result_linearLayout;
    @BindView(R.id.max_index_stationary_textView)
    TextView max_index_stationary_textView;
    @BindView(R.id.index_stationary_textView)
    TextView index_stationary_textView;
    @BindView(R.id.frequency_stationary_textView)
    TextView frequency_stationary_textView;
    @BindView(R.id.current_antenna_stationary_textView)
    TextView current_antenna_stationary_textView;
    @BindView(R.id.scan_details_linearLayout)
    LinearLayout scan_details_linearLayout;
    @BindView(R.id.code_textView)
    TextView code_textView;
    @BindView(R.id.mortality_textView)
    TextView mortality_textView;
    @BindView(R.id.period_textView)
    TextView period_textView;
    @BindView(R.id.pulse_rate_textView)
    TextView pulse_rate_textView;
    @BindView(R.id.line_view)
    View line_view;

    private final static String TAG = StationaryScanActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    private AnimationDrawable animationDrawable;
    private boolean isScanning;
    private boolean previousScanning;
    private int baseFrequency;
    private int range;
    private byte detectionType;
    private int firstTable;
    private int secondTable;
    private int thirdTable;
    private int numberAntennas;
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

    private String parameter = "";
    private String parameterWrite = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    showDisconnectionMessage(status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    switch (parameter) {
                        case ValueCodes.STATIONARY_DEFAULTS: // Gets stationary defaults data
                            onClickStationary();
                            break;
                        case ValueCodes.START_LOG: // Receives the data
                            onClickLog();
                            break;
                        case ValueCodes.CONTINUE_LOG:
                            onClickLogScanning();
                            break;
                        case ValueCodes.TABLES:
                        case ValueCodes.SCAN_RATE:
                        case ValueCodes.SCAN_TIMEOUT:
                        case ValueCodes.ANTENNA_NUMBER:
                        case ValueCodes.STORE_RATE:
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE:
                        case ValueCodes.REFERENCE_FREQUENCY:
                            onClickTemporary();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    switch (parameter) {
                        case ValueCodes.STATIONARY_DEFAULTS: // Gets stationary defaults data
                            downloadData(packet);
                            break;
                        case ValueCodes.START_LOG: // Receives the data
                            setCurrentLog(packet);
                            break;
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    private final BroadcastReceiver mGattUpdateReceiverWrite = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND.equals(action)) {
                    switch (parameterWrite) {
                        case ValueCodes.START_SCAN: // Starts to scan
                            onClickStart();
                            break;
                        case ValueCodes.STOP_SCAN: // Stops scan
                            onClickStop();
                            break;
                    }
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
                if (ValueCodes.TABLES_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                    int[] value = result.getData().getExtras().getIntArray(ValueCodes.VALUE);
                    String numbers = "";
                    for (int number : value)
                        numbers += number + ", ";
                    frequency_table_number_stationary_textView.setText(numbers.substring(0, numbers.length() - 2));
                    parameter = ValueCodes.TABLES;
                } else {
                    int value = result.getData().getExtras().getInt(ValueCodes.VALUE);
                    switch (result.getResultCode()) {
                        case ValueCodes.SCAN_RATE_SECONDS_CODE: // Gets the modified scan rate
                            scan_rate_seconds_stationary_textView.setText(String.valueOf(value));
                            parameter = ValueCodes.SCAN_RATE;
                            break;
                        case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Gets the modified scan timeout
                            scan_timeout_seconds_stationary_textView.setText(String.valueOf(value));
                            parameter = ValueCodes.SCAN_TIMEOUT;
                            break;
                        case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Gets the modified number of antennas
                            number_of_antennas_stationary_textView.setText(String.valueOf(value));
                            parameter = ValueCodes.ANTENNA_NUMBER;
                            break;
                        case ValueCodes.STORE_RATE_CODE:
                            store_rate_minutes_stationary_textView.setText((value == 100) ? "Continuous Store" : String.valueOf(value));
                            parameter = ValueCodes.STORE_RATE;
                            break;
                        case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE:
                            reference_frequency_store_rate_stationary_textView.setText(String.valueOf(value));
                            parameter = ValueCodes.REFERENCE_FREQUENCY_STORE_RATE;
                            break;
                        case ValueCodes.RESULT_OK:
                            stationary_reference_frequency_switch.setEnabled(false);
                            frequency_reference_stationary_textView.setText(Converters.getFrequency(value));
                            parameter = ValueCodes.REFERENCE_FREQUENCY;
                            break;
                    }
                }
                mBluetoothLeService.discovering();
            });

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private static IntentFilter makeGattUpdateIntentFilterWrite() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND);
        return intentFilter;
    }

    /**
     * Requests a read for get stationary defaults data.
     */
    private void onClickStationary() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    private void onClickTemporary() {
        byte[] b = new byte[]{(byte) 0x6F, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        switch (parameter) {
            case ValueCodes.TABLES:
                String[] tables = frequency_table_number_stationary_textView.getText().toString().split(", ");
                int firstTableNumber = (tables.length > 0) ? Integer.parseInt(tables[0]) : 0;
                int secondTableNumber = (tables.length > 1) ? Integer.parseInt(tables[1]) : 0;
                int thirdTableNumber = (tables.length > 2) ? Integer.parseInt(tables[2]) : 0;
                b[11] = (byte) firstTableNumber;
                b[12] = (byte) secondTableNumber;
                b[13] = (byte) thirdTableNumber;
                break;
            case ValueCodes.SCAN_RATE:
                b[2] = (byte) (Float.parseFloat(scan_rate_seconds_stationary_textView.getText().toString()) * 10);
                break;
            case ValueCodes.SCAN_TIMEOUT:
                b[3] = (byte) Integer.parseInt(scan_timeout_seconds_stationary_textView.getText().toString());
                break;
            case ValueCodes.ANTENNA_NUMBER:
                b[1] = (byte) Integer.parseInt(number_of_antennas_stationary_textView.getText().toString());
                break;
            case ValueCodes.STORE_RATE:
                b[7] = (byte) Integer.parseInt(store_rate_minutes_stationary_textView.getText().toString());
                break;
            case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE:
                b[6] = (byte) Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());
                break;
            case ValueCodes.REFERENCE_FREQUENCY:
                int frequency = Integer.parseInt(reference_frequency_store_rate_stationary_textView.getText().toString());
                b[4] = (byte) (frequency % 256);
                b[5] = (byte) (frequency / 256);
                break;

        }
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) parameter = "";
        stationary_reference_frequency_switch.setEnabled(true);
    }

    /**
     * Writes the stationary scan data for start to scan.
     */
    private void onClickStart() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);

        byte[] b = new byte[] {(byte) 0x83, (byte) (YY % 100), (byte) MM, (byte) DD, (byte) hh, (byte) mm, (byte) ss,
                (byte) firstTable, (byte) secondTable, (byte) thirdTable};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        isScanning = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (isScanning) {
            parameterWrite = "";
            setVisibility("scanning");
        }
    }

    /**
     * Enables notification for receive the data.
     */
    private void onClickLog() {
        parameterWrite = ValueCodes.START_SCAN;

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, ValueCodes.WAITING_PERIOD);
    }

    /**
     * Writes a value for stop scan.
     */
    private void onClickStop() {
        byte[] b = new byte[] {(byte) 0x87};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STATIONARY;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            clear();
            isScanning = false;
            setVisibility("overview");
            animationDrawable.stop();
            if (previousScanning) {
                parameter = ValueCodes.STATIONARY_DEFAULTS;
                new Handler().postDelayed(() -> {
                    mBluetoothLeService.discovering();
                }, ValueCodes.WAITING_PERIOD);
            } else {
                parameter = "";
            }
        }
    }

    private void onClickLogScanning() {
        parameter = ValueCodes.START_LOG;

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);
    }

    @OnClick(R.id.frequency_table_number_stationary_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLES_NUMBER_CODE);
        intent.putExtra(ValueCodes.FIRST_TABLE_NUMBER, (int) originalData.get(ValueCodes.FIRST_TABLE_NUMBER));
        intent.putExtra(ValueCodes.SECOND_TABLE_NUMBER, (int) originalData.get(ValueCodes.SECOND_TABLE_NUMBER));
        intent.putExtra(ValueCodes.THIRD_TABLE_NUMBER, (int) originalData.get(ValueCodes.THIRD_TABLE_NUMBER));
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_stationary_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_timeout_seconds_stationary_linearLayout)
    public void onClickScanTimeoutSeconds(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_TIMEOUT_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.number_of_antennas_stationary_linearLayout)
    public void onClickNumberOfAntennas(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.NUMBER_OF_ANTENNAS_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.store_rate_stationary_linearLayout)
    public void onClickStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.STORE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.stationary_reference_frequency_switch)
    public void onCheckedChangedReferenceFrequency(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            reference_frequency_stationary_linearLayout.setEnabled(true);
            int frequency = (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY);
            frequency_reference_stationary_textView.setText(frequency != 0 ? Converters.getFrequency(frequency) : "0");
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(true);
            int storeRate = (int) originalData.get(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE);
            reference_frequency_store_rate_stationary_textView.setText(String.valueOf(storeRate));
        } else {
            reference_frequency_stationary_linearLayout.setEnabled(false);
            frequency_reference_stationary_textView.setText(R.string.lb_no_reference_frequency);
            reference_frequency_store_rate_stationary_linearLayout.setEnabled(false);
            reference_frequency_store_rate_stationary_textView.setText("0");
        }
    }

    @OnClick(R.id.reference_frequency_stationary_linearLayout)
    public void onClickReferenceFrequency(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, "Reference Frequency");
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.reference_frequency_store_rate_stationary_linearLayout)
    public void onClickReferenceFrequencyStoreRate(View v) {
        Intent intent = new Intent(this, InputValueActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE);
        launcher.launch(intent);
    }

    @OnClick(R.id.start_stationary_button)
    public void onClickStartStationary(View v) {
        parameter = ValueCodes.START_LOG;
        mBluetoothLeService.discovering();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary_scan);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        receiverInformation = ReceiverInformation.getReceiverInformation();
        ReceiverStatus.setReceiverStatus(this);

        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0);
        range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
        isScanning = getIntent().getBooleanExtra(ValueCodes.SCANNING, false);
        originalData = new HashMap<>();
        if (isScanning) { // The device is already scanning
            previousScanning = true;
            parameter = ValueCodes.CONTINUE_LOG;
            int currentFrequency = getIntent().getIntExtra(ValueCodes.FREQUENCY, 0) + (baseFrequency * 1000);
            int currentIndex = getIntent().getIntExtra(ValueCodes.INDEX, 0);
            int total = getIntent().getIntExtra(ValueCodes.MAX_INDEX, 0);
            detectionType = getIntent().getByteExtra(ValueCodes.DETECTION_TYPE, (byte) 0);
            frequency_stationary_textView.setText(Converters.getFrequency(currentFrequency));
            index_stationary_textView.setText(String.valueOf(currentIndex));
            max_index_stationary_textView.setText("Table Index (" + total + " Total)");

            updateVisibility();
            setVisibility("scanning");
        } else { // Gets aerial defaults data
            parameter = ValueCodes.STATIONARY_DEFAULTS;
            previousScanning = false;
            setVisibility("overview");
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                Intent intent = new Intent(this, StartScanningActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                parameterWrite = ValueCodes.STOP_SCAN;
                mBluetoothLeService.discoveringSecond();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isScanning) { // Asks if you want to stop the scan
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stop Stationary");
            builder.setMessage("Are you sure you want to stop scanning?");
            builder.setPositiveButton("OK", (dialog, which) -> {
                parameterWrite = ValueCodes.STOP_SCAN;
                mBluetoothLeService.discoveringSecond();
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.catskill_white)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(mGattUpdateReceiverWrite, makeGattUpdateIntentFilterWrite());
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(mGattUpdateReceiverWrite);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private void showDisconnectionMessage(int status) {
        parameter = "";
        parameterWrite = "";
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(view);
        dialog.show();
        Toast.makeText(this, "Connection failed, status: " + status, Toast.LENGTH_LONG).show();

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
            case "overview":
                ready_stationary_scan_LinearLayout.setVisibility(View.VISIBLE);
                stationary_result_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.stationary_scanning);
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
                state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
                break;
            case "scanning":
                ready_stationary_scan_LinearLayout.setVisibility(View.GONE);
                stationary_result_linearLayout.setVisibility(View.VISIBLE);
                title_toolbar.setText(R.string.lb_stationary_scanning);
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close);
                state_view.setBackgroundResource(R.drawable.scanning_animation);
                animationDrawable = (AnimationDrawable) state_view.getBackground();
                animationDrawable.start();
                break;
        }
    }

    private void updateVisibility() {
        int visibility = Converters.getHexValue(detectionType).equals("09") ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        mortality_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        //audio_manual_linearLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);
    }

    /**
     * With the received packet, gets stationary defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (data.length == 1) {
            parameter = ValueCodes.STATIONARY_DEFAULTS;
            mBluetoothLeService.discovering();
        } else if (Converters.getHexValue(data[0]).equals("6C")) {
            parameter = "";
            firstTable = Integer.parseInt(Converters.getDecimalValue(data[9]));
            secondTable = Integer.parseInt(Converters.getDecimalValue(data[10]));
            thirdTable = Integer.parseInt(Converters.getDecimalValue(data[11]));
            String tables = "";
            for (int i = 9; i < data.length; i++) {
                if (data[i] != 0)
                    tables += Converters.getDecimalValue(data[i]) + ", ";
            }
            if (tables.isEmpty()) { // There are no tables with frequencies to scan
                frequency_table_number_stationary_textView.setText(R.string.lb_none);
                start_stationary_button.setEnabled(false);
                start_stationary_button.setAlpha((float) 0.6);
            } else { // Shows the table to be scanned
                frequency_table_number_stationary_textView.setText(tables.substring(0, tables.length() - 2));
                start_stationary_button.setEnabled(true);
                start_stationary_button.setAlpha((float) 1);
            }
            numberAntennas = Integer.parseInt(Converters.getDecimalValue(data[1]));
            number_of_antennas_stationary_textView.setText((numberAntennas == 0) ? "None" : String.valueOf(numberAntennas));
            stationary_external_data_transfer_switch.setChecked(data[2] != 0);
            scan_rate_seconds_stationary_textView.setText(Converters.getDecimalValue(data[3]));
            scan_timeout_seconds_stationary_textView.setText(Converters.getDecimalValue(data[4]));
            if (Converters.getHexValue(data[5]).equals("00"))
                store_rate_minutes_stationary_textView.setText(R.string.lb_continuous_store);
            else
                store_rate_minutes_stationary_textView.setText(Converters.getDecimalValue(data[5]));
            int frequency = 0;
            if (!Converters.getDecimalValue(data[6]).equals("0") && !Converters.getDecimalValue(data[7]).equals("0"))
                frequency = (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                        Integer.parseInt(Converters.getDecimalValue(data[7])) + baseFrequency;
            //frequency_reference_stationary_textView.setText((frequency == 0) ? "No Reference Frequency" : Converters.getFrequency(frequency));
            //reference_frequency_store_rate_stationary_textView.setText(Converters.getDecimalValue(data[8]));

            originalData.put(ValueCodes.FIRST_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[9])));
            originalData.put(ValueCodes.SECOND_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[10])));
            originalData.put(ValueCodes.THIRD_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[11])));
            originalData.put(ValueCodes.ANTENNA_NUMBER, numberAntennas);
            originalData.put(ValueCodes.SCAN_RATE, Integer.parseInt(Converters.getDecimalValue(data[3])));
            originalData.put(ValueCodes.SCAN_TIMEOUT, Integer.parseInt(Converters.getDecimalValue(data[4])));
            originalData.put(ValueCodes.STORE_RATE, Integer.parseInt(Converters.getDecimalValue(data[5])));
            originalData.put(ValueCodes.EXTERNAL_DATA_TRANSFER, Integer.parseInt(Converters.getDecimalValue(data[2])));
            originalData.put(ValueCodes.REFERENCE_FREQUENCY, frequency);
            originalData.put(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE, Integer.parseInt(Converters.getDecimalValue(data[8])));
            stationary_reference_frequency_switch.setChecked(frequency != 0);
        }
    }

    /**
     * With the received packet, gets the data of scanning.
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        Log.i(TAG, Converters.getHexValue(data));
        switch (Converters.getHexValue(data[0])) {
            case "50":
                int maxIndex = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
                max_index_stationary_textView.setText("Table Index (" + maxIndex + " Total)");
                detectionType = data[18];
                updateVisibility();
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "F1":
                logScanFix(data);
                break;
            case "F2":
                logScanFixConsolidated(data);
                break;
            default: //E1 and E2
                logScanData(data);
                break;
        }
    }

    /**
     * With the received packet, processes the data of scan header to display.
     * @param data The received packet.
     */
    private void logScanHeader(byte[] data) {
        clear();
        int frequency = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + (baseFrequency * 1000);
        index_stationary_textView.setText(Converters.getDecimalValue(data[3]));
        frequency_stationary_textView.setText(Converters.getFrequency(frequency));
        current_antenna_stationary_textView.setText((numberAntennas == 0) ? "All" : String.valueOf(numberAntennas));
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is code.
     * @param data The received packet.
     */
    private void logScanFix(byte[] data) {
        int position;
        int code = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = Integer.parseInt(Converters.getDecimalValue(data[7]));
        int mort = Integer.parseInt(Converters.getDecimalValue(data[5]));

        if (scan_details_linearLayout.getChildCount() > 2 && isEqualFirstCode(code))
            refreshFirstCode(signalStrength, mort > 0);
        else if ((position = positionCode(code)) != 0)
            refreshPosition(position, signalStrength, mort > 0);
        else
            createCodeDetail(code, signalStrength, detections + 1, mort > 0);
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is code.
     * @param data The received packet.
     */
    private void logScanFixConsolidated(byte[] data) {
        int position;
        int code = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[7]));
        int mort = (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[5]));

        if (scan_details_linearLayout.getChildCount() > 2 && isEqualFirstCode(code))
            refreshFirstCode(signalStrength, mort > 0);
        else if ((position = positionCode(code)) != 0)
            refreshPosition(position, signalStrength, mort > 0);
        else
            createCodeDetail(code, signalStrength, detections + 1, mort > 0);
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is non code.
     * @param data The received packet.
     */
    private void logScanData(byte[] data) {
        int period = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        float pulseRate = (float) (60000 / period);
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = Integer.parseInt(Converters.getDecimalValue(data[2])) / 10;

        refreshNonCoded(period, pulseRate, signalStrength, detections);
    }

    private void createCodeDetail(int code, int signalStrength, int detections, boolean isMort) {
        LinearLayout newCode = new LinearLayout(this);
        newCode.setOrientation(LinearLayout.HORIZONTAL);
        newCode.setPadding(0, 8, 0, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;

        TextView codeTextView = new TextView(this);
        codeTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        codeTextView.setTextAppearance(body_regular);
        codeTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        codeTextView.setLayoutParams(params);

        TextView detectionsTextView = new TextView(this);
        detectionsTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        detectionsTextView.setTextAppearance(body_regular);
        detectionsTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        detectionsTextView.setLayoutParams(params);

        TextView mortalityTextView = new TextView(this);
        mortalityTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        mortalityTextView.setTextAppearance(body_regular);
        mortalityTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        mortalityTextView.setLayoutParams(params);

        TextView signalStrengthTextView = new TextView(this);
        signalStrengthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        signalStrengthTextView.setTextAppearance(body_regular);
        signalStrengthTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        signalStrengthTextView.setLayoutParams(params);

        TextView mortTextView = new TextView(this);
        mortTextView.setVisibility(View.GONE);

        newCode.addView(codeTextView);
        newCode.addView(detectionsTextView);
        newCode.addView(mortalityTextView);
        newCode.addView(signalStrengthTextView);
        newCode.addView(mortTextView);

        LinearLayout line = new LinearLayout(this);
        line.setBackgroundColor(ContextCompat.getColor(this, light_gray));
        line.setLayoutParams(line_view.getLayoutParams());

        scan_details_linearLayout.addView(newCode);
        scan_details_linearLayout.addView(line);

        refreshCode(scan_details_linearLayout.getChildCount() - 2, code, signalStrength, detections, isMort, 0);
    }

    private boolean isEqualFirstCode(int code) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);

        return Integer.parseInt(codeTextView.getText().toString()) == code;
    }

    private void refreshFirstCode(int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        mortalityTextView.setText(isMort ? "M" : "-");
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        detectionsTextView.setText(String.valueOf(Integer.parseInt(detectionsTextView.getText().toString()) + 1));
        if (isMort) mortTextView.setText(String.valueOf(Integer.parseInt(mortTextView.getText().toString()) + 1));
    }

    private int positionCode(int code) {
        int position = 0;
        for (int i = 4; i < scan_details_linearLayout.getChildCount() - 1; i += 2) {
            LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            TextView codeTextView = (TextView) linearLayout.getChildAt(0);
            if (Integer.parseInt(codeTextView.getText().toString()) == code)
                position = i;
        }
        return position;
    }

    private void refreshPosition(int position, int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        int code = Integer.parseInt(codeTextView.getText().toString());
        int detections = Integer.parseInt(detectionsTextView.getText().toString());
        int mort = Integer.parseInt(mortTextView.getText().toString());

        refreshCode(position, code, signalStrength, detections + 1, isMort, mort);
    }

    private void refreshCode(int finalPosition, int code, int signalStrength, int detections, boolean isMort, int mort) {
        for (int i = finalPosition; i > 3 ; i -= 2) {
            LinearLayout lastLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            LinearLayout penultimateLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i - 2);

            TextView lastCodeTextView = (TextView) lastLinearLayout.getChildAt(0);
            TextView penultimateCodeTextView = (TextView) penultimateLinearLayout.getChildAt(0);
            lastCodeTextView.setText(penultimateCodeTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastMortalityTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimateMortalityTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastMortalityTextView.setText(penultimateMortalityTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());

            TextView lastMortTextView = (TextView) lastLinearLayout.getChildAt(4);
            TextView penultimateMortTextView = (TextView) penultimateLinearLayout.getChildAt(4);
            lastMortTextView.setText(penultimateMortTextView.getText());
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newCodeTextView = (TextView) linearLayout.getChildAt(0);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView newMortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView newMortTextView = (TextView) linearLayout.getChildAt(4);

        newCodeTextView.setText(String.valueOf(code));
        newDetectionsTextView.setText(String.valueOf(detections));
        newMortalityTextView.setText(isMort ? "M" : "-");
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
        newMortTextView.setText(isMort ? String.valueOf(mort + 1) : String.valueOf(mort));

        Log.i(TAG, "Code: " + newCodeTextView.getText() + " SS: " + newSignalStrengthTextView.getText() + " Det: " + newDetectionsTextView.getText() + " Mort: " + newMortTextView.getText() + " Size: " + scan_details_linearLayout.getChildCount());
    }

    private void refreshNonCoded(int period, float pulseRate, int signalStrength, int detections) {
        LinearLayout newNonCoded = new LinearLayout(this);
        newNonCoded.setOrientation(LinearLayout.HORIZONTAL);
        newNonCoded.setPadding(0, 8, 0, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;

        TextView periodTextView = new TextView(this);
        periodTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        periodTextView.setTextAppearance(body_regular);
        periodTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        periodTextView.setLayoutParams(params);

        TextView detectionsTextView = new TextView(this);
        detectionsTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        detectionsTextView.setTextAppearance(body_regular);
        detectionsTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        detectionsTextView.setLayoutParams(params);

        TextView pulseRateTextView = new TextView(this);
        pulseRateTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        pulseRateTextView.setTextAppearance(body_regular);
        pulseRateTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        pulseRateTextView.setLayoutParams(params);

        TextView signalStrengthTextView = new TextView(this);
        signalStrengthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        signalStrengthTextView.setTextAppearance(body_regular);
        signalStrengthTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        signalStrengthTextView.setLayoutParams(params);

        newNonCoded.addView(periodTextView);
        newNonCoded.addView(detectionsTextView);
        newNonCoded.addView(pulseRateTextView);
        newNonCoded.addView(signalStrengthTextView);

        LinearLayout line = new LinearLayout(this);
        line.setBackgroundColor(ContextCompat.getColor(this, light_gray));
        line.setLayoutParams(line_view.getLayoutParams());

        scan_details_linearLayout.addView(newNonCoded);
        scan_details_linearLayout.addView(line);

        for (int i = scan_details_linearLayout.getChildCount() - 2; i > 3 ; i -= 2) {
            LinearLayout lastLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            LinearLayout penultimateLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i - 2);

            TextView lastPeriodTextView = (TextView) lastLinearLayout.getChildAt(0);
            TextView penultimatePeriodTextView = (TextView) penultimateLinearLayout.getChildAt(0);
            lastPeriodTextView.setText(penultimatePeriodTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastPulseRateTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimatePulseRateTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastPulseRateTextView.setText(penultimatePulseRateTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newPeriodTextView = (TextView) linearLayout.getChildAt(0);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView newPulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(3);

        newPeriodTextView.setText(String.valueOf(period));
        newDetectionsTextView.setText(String.valueOf(detections));
        newPulseRateTextView.setText(String.valueOf(pulseRate));
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
    }

    /**
     * Clears the screen to start displaying the data.
     */
    private void clear() {
        frequency_stationary_textView.setText("");
        index_stationary_textView.setText("");
        int count = scan_details_linearLayout.getChildCount();
        while (count > 2) {
            scan_details_linearLayout.removeViewAt(2);
            count--;
        }
    }
}