package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
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

public class VhfStationaryScanActivity extends AppCompatActivity {

    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.ready_stationary_scan_LinearLayout)
    LinearLayout ready_stationary_scan_LinearLayout;
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
    @BindView(R.id.view_detection_stationary_textView)
    TextView view_detection_stationary_textView;

    private final static String TAG = VhfStationaryScanActivity.class.getSimpleName();

    private final Context mContext = this;
    private AnimationDrawable animationDrawable;
    private boolean isScanning;
    private boolean previousScanning;
    private int baseFrequency;
    //private int range;
    private byte detectionType;
    private int firstTable;
    private int secondTable;
    private int thirdTable;
    private int antennas;
    Map<String, Object> originalData;
    private DialogFragment viewDetectionFilter;

    private final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();
    private String parameter = "";
    private String parameterWrite = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    parameter = parameterWrite = "";
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    Message.showDisconnectionMessage(mContext, status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    switch (parameter) {
                        case ValueCodes.STATIONARY_DEFAULTS: // Gets stationary defaults data
                            TransferBleData.readDefaults(false);
                            break;
                        case ValueCodes.START_LOG: // Receives the data
                            setNotificationLog();
                            break;
                        case ValueCodes.CONTINUE_LOG:
                            setNotificationLogScanning();
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
            } catch (Exception e) {
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
                            setStopScan();
                            break;
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    /*ActivityResultLauncher<Intent> launcher = registerForActivityResult(
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
                leServiceConnection.getBluetoothLeService().discovering();
            });*/

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
        boolean result = leServiceConnection.getBluetoothLeService().writeCharacteristic(service, characteristic, b);

        if (result) parameter = "";
        stationary_reference_frequency_switch.setEnabled(true);
    }

    private void onClickStart() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);

        byte[] b = new byte[] {(byte) 0x83, (byte) (YY % 100), (byte) (MM + 1), (byte) DD, (byte) hh, (byte) mm, (byte) ss,
                (byte) firstTable, (byte) secondTable, (byte) thirdTable};
        isScanning = TransferBleData.writeStartScan(ValueCodes.STATIONARY_DEFAULTS, b);
        if (isScanning) {
            parameterWrite = "";
            setVisibility("scanning");
        }
    }

    private void setNotificationLog() {
        parameterWrite = ValueCodes.START_SCAN;
        TransferBleData.notificationLog();

        new Handler().postDelayed(() -> {
            leServiceConnection.getBluetoothLeService().discoveringSecond();
        }, ValueCodes.WAITING_PERIOD);
    }

    private void setStopScan() {
        boolean result = TransferBleData.writeStopScan(ValueCodes.STATIONARY_DEFAULTS);
        if (result) {
            clear();
            isScanning = false;
            setVisibility("overview");
            animationDrawable.stop();
            if (previousScanning) {
                parameter = ValueCodes.STATIONARY_DEFAULTS;
                new Handler().postDelayed(() -> {
                    leServiceConnection.getBluetoothLeService().discovering();
                }, ValueCodes.WAITING_PERIOD);
            } else {
                parameter = "";
            }
        }
    }

    private void setNotificationLogScanning() {
        parameter = ValueCodes.START_LOG;
        TransferBleData.notificationLog();
    }

    /*@OnClick(R.id.frequency_table_number_stationary_linearLayout)
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
    }*/

    @OnClick(R.id.start_stationary_button)
    public void onClickStartStationary(View v) {
        parameter = ValueCodes.START_LOG;
        leServiceConnection.getBluetoothLeService().discovering();
    }

    @OnClick(R.id.view_detection_stationary_textView)
    public void onClickViewDetection(View v) {
        viewDetectionFilter.show(getSupportFragmentManager(), ViewDetectionFilter.TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_stationary_scan);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, R.string.stationary_scanning);
        ActivitySetting.setReceiverStatus(this);

        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0);
        //range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
        isScanning = getIntent().getBooleanExtra(ValueCodes.SCANNING, false);
        originalData = new HashMap<>();
        if (isScanning) { // The device is already scanning
            previousScanning = true;
            parameter = ValueCodes.CONTINUE_LOG;
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);

            int currentFrequency = (Integer.parseInt(Converters.getDecimalValue(data[16])) * 256)
                    + Integer.parseInt(Converters.getDecimalValue(data[17])) + (baseFrequency * 1000);
            int currentIndex = (Integer.parseInt(Converters.getDecimalValue(data[7])) * 256)
                    + Integer.parseInt(Converters.getDecimalValue(data[8]));
            int currentAntenna = Integer.parseInt(Converters.getDecimalValue(data[9]));
            detectionType = getIntent().getByteExtra(ValueCodes.DETECTION_TYPE, (byte) 0);
            frequency_stationary_textView.setText(Converters.getFrequency(currentFrequency));
            index_stationary_textView.setText(String.valueOf(currentIndex));
            current_antenna_stationary_textView.setText((currentAntenna == 0) ? "All" : String.valueOf(currentAntenna));

            scanState(data);
            setVisibility("scanning");
        } else { // Gets aerial defaults data
            parameter = ValueCodes.STATIONARY_DEFAULTS;
            previousScanning = false;
            setVisibility("overview");
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                Intent intent = new Intent(this, VhfScanningActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                parameterWrite = ValueCodes.STOP_SCAN;
                leServiceConnection.getBluetoothLeService().discoveringSecond();
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
                leServiceConnection.getBluetoothLeService().discoveringSecond();
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, Converters.makeFirstGattUpdateIntentFilter());
        registerReceiver(mGattUpdateReceiverWrite, Converters.makeSecondGattUpdateIntentFilter());
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
        //unbindService(leServiceConnection.getServiceConnection());
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
        view_detection_stationary_textView.setVisibility(visibility);
    }

    /**
     * With the received packet, gets stationary defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            parameter = "";
            firstTable = Integer.parseInt(Converters.getDecimalValue(data[9]));
            secondTable = Integer.parseInt(Converters.getDecimalValue(data[10]));
            thirdTable = Integer.parseInt(Converters.getDecimalValue(data[11]));
            String tables = "";
            for (int i = 9; i < data.length; i++) {
                if (data[i] != 0 && !Converters.getDecimalValue(data[i]).equals("FF "))
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
            antennas = Integer.parseInt(Converters.getDecimalValue(data[1]));
            number_of_antennas_stationary_textView.setText((antennas == 0) ? "None" : String.valueOf(antennas));
            stationary_external_data_transfer_switch.setChecked(data[2] != 0);
            stationary_external_data_transfer_switch.setEnabled(false);
            scan_rate_seconds_stationary_textView.setText(Converters.getDecimalValue(data[3]));
            scan_timeout_seconds_stationary_textView.setText(Converters.getDecimalValue(data[4]));
            if (Converters.getHexValue(data[5]).equals("00"))
                store_rate_minutes_stationary_textView.setText(R.string.lb_continuous_store);
            else
                store_rate_minutes_stationary_textView.setText(Converters.getDecimalValue(data[5]));
            int frequency = 0;
            if (!Converters.getDecimalValue(data[6]).equals("0") && !Converters.getDecimalValue(data[7]).equals("0"))
                frequency = (Integer.parseInt(Converters.getDecimalValue(data[6])) * 256) +
                        Integer.parseInt(Converters.getDecimalValue(data[7])) + (baseFrequency * 1000);
            stationary_reference_frequency_switch.setChecked(frequency != 0);
            stationary_reference_frequency_switch.setEnabled(false);
            frequency_reference_stationary_textView.setText((frequency == 0) ? "No Reference Frequency" : Converters.getFrequency(frequency));
            reference_frequency_store_rate_stationary_textView.setText(Converters.getDecimalValue(data[8]));

            originalData.put(ValueCodes.FIRST_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[9])));
            originalData.put(ValueCodes.SECOND_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[10])));
            originalData.put(ValueCodes.THIRD_TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[11])));
            originalData.put(ValueCodes.ANTENNA_NUMBER, antennas);
            originalData.put(ValueCodes.SCAN_RATE, Integer.parseInt(Converters.getDecimalValue(data[3])));
            originalData.put(ValueCodes.SCAN_TIMEOUT, Integer.parseInt(Converters.getDecimalValue(data[4])));
            originalData.put(ValueCodes.STORE_RATE, Integer.parseInt(Converters.getDecimalValue(data[5])));
            originalData.put(ValueCodes.EXTERNAL_DATA_TRANSFER, Integer.parseInt(Converters.getDecimalValue(data[2])));
            originalData.put(ValueCodes.REFERENCE_FREQUENCY, frequency);
            originalData.put(ValueCodes.REFERENCE_FREQUENCY_STORE_RATE, Integer.parseInt(Converters.getDecimalValue(data[8])));
            //stationary_reference_frequency_switch.setChecked(frequency != 0);
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
                scanState(data);
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "F1":
            case "F2": //Consolidated
                logScanCoded(data); //Coded
                break;
            default: //E1, E2, EA Non Coded
                int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
                if (Converters.getHexValue(detectionType).equals("08")) // Non Coded Fixed
                    logScanNonCodedFixed(data, signalStrength);
                else if (Converters.getHexValue(detectionType).equals("07")) // Non Coded Variable
                    logScanNonCodedVariable(data, signalStrength);
                break;
        }
    }

    private void scanState(byte[] data) {
        int maxIndex = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        max_index_stationary_textView.setText("Table Index (" + maxIndex + " Total)");
        detectionType = data[18];
        updateVisibility();

        if (!Converters.getHexValue(detectionType).equals("09")) {
            String detection = Converters.getHexValue(detectionType).equals("08") ? "Fixed Pulse Rate" : "Variable Pulse Rate";
            String dataCalculation = "";
            switch (Converters.getHexValue(detectionType)) {
                case "06":
                    dataCalculation = "Yes";
                    break;
                case "07":
                    dataCalculation = "None";
                    break;
            }
            String matches = Converters.getDecimalValue(data[19]);
            String pr1 = Converters.getDecimalValue(data[20]);
            String pr1Tolerance = Converters.getDecimalValue(data[21]);
            String pr2 = Converters.getDecimalValue(data[22]);
            String pr2Tolerance = Converters.getDecimalValue(data[23]);
            viewDetectionFilter = ViewDetectionFilter.newInstance(detection, pr1, pr1Tolerance, pr2, pr2Tolerance, dataCalculation, matches);
        }
    }

    /**
     * With the received packet, processes the data of scan header to display.
     * @param data The received packet.
     */
    private void logScanHeader(byte[] data) {
        clear();
        int frequency = ((Integer.parseInt(Converters.getDecimalValue(data[1])) & 63) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + (baseFrequency * 1000);
        int index = (((Integer.parseInt(Converters.getDecimalValue(data[1])) >> 6) & 1) * 256) + Integer.parseInt(Converters.getDecimalValue(data[3]));
        antennas = Integer.parseInt(Converters.getDecimalValue(data[1])) >> 7;
        if (antennas == 0) {
            antennas = (Integer.parseInt(Converters.getDecimalValue(data[7])) >> 6) + 1;
            current_antenna_stationary_textView.setText(String.valueOf(antennas));
        } else {
            current_antenna_stationary_textView.setText(R.string.lb_all);
        }
        index_stationary_textView.setText(String.valueOf(index));
        frequency_stationary_textView.setText(Converters.getFrequency(frequency));
    }

    private void logScanCoded(byte[] data) {
        int code = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int mortality = Integer.parseInt(Converters.getDecimalValue(data[5]));
        int position = getPositionNumber(code, 0);
        if (position > 0) {
            refreshCodedPosition(position, signalStrength, mortality > 0);
        } else if (position < 0) {
            createDetail();
            addNewCodedDetailInPosition(-position, code, signalStrength, mortality > 0);
        } else {
            createDetail();
            addNewCodedDetail(scan_details_linearLayout.getChildCount() - 2, code, signalStrength, mortality > 0);
        }
    }

    private void logScanNonCodedFixed(byte[] data, int signalStrength) {
        int period = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        int pulseRate = 60000 / period;
        int type = Integer.parseInt(Converters.getHexValue(data[0]).replace("E", ""));
        int position = getPositionNumber(type, 4);
        if (position > 0) {
            refreshNonCodedPosition(position, signalStrength, period, pulseRate);
        } else if (position < 0) {
            createDetail();
            addNewNonCodedDetailInPosition(-position, pulseRate, signalStrength, period, type);
        } else {
            createDetail();
            addNewNonCodedFixedDetail(scan_details_linearLayout.getChildCount() - 2, pulseRate, signalStrength, period, type);
        }
    }

    private void logScanNonCodedVariable(byte[] data, int signalStrength) {
        int period = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        int pulseRate = 60000 / period;
        refreshNonCodedVariable(period, pulseRate, signalStrength);
    }

    /**
     * Looks for the position in the table of code received.
     * @param number Number of code or period to look.
     * @return Returns the position of code in the table.
     */
    private int getPositionNumber(int number, int position) {
        for (int i = 2; i < scan_details_linearLayout.getChildCount() - 1; i += 2) {
            LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            TextView numberTextView = (TextView) linearLayout.getChildAt(position);

            if (Integer.parseInt(numberTextView.getText().toString()) == number)
                return i;
            else if (number < Integer.parseInt(numberTextView.getText().toString()))
                return -i;
        }
        return 0;
    }

    private void refreshCodedPosition(int position, int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        int detections = Integer.parseInt(detectionsTextView.getText().toString()) + 1;
        int mort = isMort ? Integer.parseInt(mortTextView.getText().toString()) + 1 : Integer.parseInt(mortTextView.getText().toString());
        detectionsTextView.setText(String.valueOf(detections));
        mortalityTextView.setText(isMort ? "M" : "-");
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        mortTextView.setText(String.valueOf(mort));
    }

    private void refreshNonCodedPosition(int position, int signalStrength, int period, int pulseRate) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);

        int detections = Integer.parseInt(detectionsTextView.getText().toString()) + 1;
        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText(String.valueOf(detections));
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
    }

    private void addNewCodedDetail(int position, int code, int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        codeTextView.setText(String.valueOf(code));
        detectionsTextView.setText("1");
        mortalityTextView.setText(isMort ? "M" : "-");
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        mortTextView.setText(isMort ? "1" : "0");
    }

    private void addNewNonCodedFixedDetail(int position, int pulseRate, int signalStrength, int period, int type) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView typeTextView = (TextView) linearLayout.getChildAt(4);

        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText("1");
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        typeTextView.setText(String.valueOf(type));
    }

    private void addNewNonCodedVariableDetail(int pulseRate, int signalStrength, int period) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);

        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText("-");
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
    }

    private void addNewCodedDetailInPosition(int position, int code, int signalStrength, boolean isMort) {
        for (int i = scan_details_linearLayout.getChildCount() - 2; i > position ; i -= 2) {
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
        addNewCodedDetail(position, code, signalStrength, isMort);
    }

    private void addNewNonCodedDetailInPosition(int position, int pulseRate, int signalStrength, int period, int type) {
        for (int i = scan_details_linearLayout.getChildCount() - 2; i > position ; i -= 2) {
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

            TextView lastTypeTextView = (TextView) lastLinearLayout.getChildAt(4);
            TextView penultimateTypeTextView = (TextView) penultimateLinearLayout.getChildAt(4);
            lastTypeTextView.setText(penultimateTypeTextView.getText());
        }
        addNewNonCodedFixedDetail(position, pulseRate, signalStrength, period, type);
    }

    private void refreshNonCodedVariable(int period, int pulseRate, int signalStrength) {
        createDetail();
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
        addNewNonCodedVariableDetail(pulseRate, signalStrength, period);
    }

    private void createDetail() {
        LinearLayout detail = new LinearLayout(this);
        detail.setOrientation(LinearLayout.HORIZONTAL);
        detail.setPadding(0, 8, 0, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;

        TextView firstTextView = createTextView(params);
        TextView detectionsTextView = createTextView(params);
        TextView secondTextView = createTextView(params);
        TextView signalStrengthTextView = createTextView(params);
        TextView extraTextView = new TextView(this);
        extraTextView.setVisibility(View.GONE);

        detail.addView(firstTextView);
        detail.addView(detectionsTextView);
        detail.addView(secondTextView);
        detail.addView(signalStrengthTextView);
        detail.addView(extraTextView);

        LinearLayout line = new LinearLayout(this);
        line.setBackgroundColor(ContextCompat.getColor(this, light_gray));
        line.setLayoutParams(line_view.getLayoutParams());

        scan_details_linearLayout.addView(detail);
        scan_details_linearLayout.addView(line);
    }

    private TextView createTextView(TableRow.LayoutParams params) {
        TextView textView = new TextView(this);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setTextAppearance(body_regular);
        textView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        textView.setLayoutParams(params);
        return textView;
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