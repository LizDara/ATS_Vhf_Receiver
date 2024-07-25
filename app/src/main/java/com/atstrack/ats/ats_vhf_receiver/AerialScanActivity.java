package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableMergeListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.catskill_white;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ghost;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.limed_spruce;
import static com.atstrack.ats.ats_vhf_receiver.R.color.mountain_meadow;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_audio;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_primary;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_tertiary;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease_light;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase_light;
import static com.atstrack.ats.ats_vhf_receiver.R.style.body_regular;

public class AerialScanActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
    @BindView(R.id.device_range_textView)
    TextView device_range_textView;
    @BindView(R.id.percent_battery_textView)
    TextView percent_battery_textView;
    @BindView(R.id.ready_aerial_scan_LinearLayout)
    LinearLayout ready_aerial_scan_LinearLayout;
    @BindView(R.id.ready_aerial_textView)
    TextView ready_aerial_textView;
    @BindView(R.id.scan_rate_aerial_textView)
    TextView scan_rate_aerial_textView;
    @BindView(R.id.selected_frequency_aerial_textView)
    TextView selected_frequency_aerial_textView;
    @BindView(R.id.gps_aerial_textView)
    TextView gps_aerial_textView;
    @BindView(R.id.auto_record_aerial_textView)
    TextView auto_record_aerial_textView;
    @BindView(R.id.edit_aerial_settings_button)
    TextView edit_aerial_defaults_textView;
    @BindView(R.id.start_aerial_button)
    Button start_aerial_button;
    @BindView(R.id.aerial_result_linearLayout)
    LinearLayout aerial_result_linearLayout;
    @BindView(R.id.max_index_aerial_textView)
    TextView max_index_aerial_textView;
    @BindView(R.id.table_index_aerial_textView)
    TextView table_index_aerial_textView;
    @BindView(R.id.frequency_aerial_textView)
    TextView frequency_aerial_textView;
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
    @BindView(R.id.hold_aerial_imageView)
    ImageView hold_aerial_imageView;
    @BindView(R.id.hold_aerial_button)
    TextView hold_aerial_button;
    @BindView(R.id.decrease_imageView)
    ImageView decrease_imageView;
    @BindView(R.id.increase_imageView)
    ImageView increase_imageView;
    @BindView(R.id.edit_table_textView)
    TextView edit_table_textView;
    @BindView(R.id.edit_table_linearLayout)
    LinearLayout edit_table_linearLayout;
    @BindView(R.id.merge_tables_linearLayout)
    LinearLayout merge_tables_linearLayout;
    @BindView(R.id.merge_tables_button)
    Button merge_tables_button;
    @BindView(R.id.audio_aerial_linearLayout)
    LinearLayout audio_aerial_linearLayout;
    @BindView(R.id.record_data_aerial_button)
    Button record_data_aerial_button;
    @BindView(R.id.current_frequency_aerial_textView)
    TextView current_frequency_aerial_textView;
    @BindView(R.id.current_index_aerial_textView)
    TextView current_index_aerial_textView;
    @BindView(R.id.table_total_aerial_textView)
    TextView table_total_aerial_textView;
    @BindView(R.id.tables_merge_listView)
    ListView tables_merge_listView;
    @BindView(R.id.gps_aerial_imageView)
    ImageView gps_aerial_imageView;
    @BindView(R.id.gps_state_aerial_textView)
    TextView gps_state_aerial_textView;

    private final static String TAG = AerialScanActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private static final int WAITING_PERIOD = 1000;

    private boolean isScanning;
    private boolean previousScanning;
    private boolean isHold;
    private boolean isRecord; //This can change during scanning
    private Handler handlerMessage;
    private TableMergeListAdapter tableMergeListAdapter;

    private AnimationDrawable animationDrawable;

    private int baseFrequency;
    private int frequencyRange;
    private int range;
    private byte detectionType;
    private int newFrequency;
    private int selectedTable;
    private int autoRecord; //This is the default record
    private int gps;
    private int code;
    private int detections;
    private int mort;

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
    private String parameterWrite = "";

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
                    switch (parameter) {
                        case "aerial": // Gets aerial defaults data
                            onClickAerial();
                            break;
                        case "sendLog": // Receives the data
                            onClickLog();
                            break;
                        case "sendLogScanning":
                            onClickLogScanning();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    switch (parameter) {
                        case "aerial": // Gets aerial defaults data
                            downloadData(packet);
                            break;
                        case "sendLog": // Receives the data
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
                        case "startAerial": // Starts to scan
                            onClickStart();
                            break;
                        case "stopAerial": // Stops scan
                            onClickStop();
                            break;
                        case "hold":
                            onClickHold();
                            break;
                        case "removeHold":
                            onClickRemoveHold();
                            break;
                        case "tables":
                            onClickTables();
                            break;
                        case "addFrequency":
                            onClickAddFrequency();
                            break;
                        case "deleteFrequency":
                            onClickDeleteFrequency();
                            break;
                        case "decrease":
                            onClickDecrease();
                            break;
                        case "increase":
                            onClickIncrease();
                            break;
                        case "merge":
                            onClickMerge();
                            break;
                        case "recordData":
                            onClickRecord();
                            break;
                        case "stopRecordData":
                            onClickStopRecord();
                            break;
                        case "left":
                            onClickLeft();
                            break;
                        case "right":
                            onClickRight();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE_SECOND.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameterWrite.equals("tables")) {
                        downloadTables(packet);
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
                if (ValueCodes.CANCELLED == result.getResultCode()) {
                    setVisibility("overview");
                }
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    newFrequency = result.getData().getExtras().getInt("frequency");
                    parameter = "sendLogScanning";
                    parameterWrite = "addFrequency";
                    Log.i(TAG, "ADDING FREQUENCY");
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

    private static IntentFilter makeGattUpdateIntentFilterWrite() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_SECOND);
        return intentFilter;
    }

    /**
     * Requests a read for get aerial defaults data.
     * Service name: Scan.
     * Characteristic name: Aerial.
     */
    private void onClickAerial() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Writes the aerial scan data for start to scan.
     * Service name: Scan.
     * Characteristic name: Aerial.
     */
    private void onClickStart() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);

        byte[] b = new byte[] {
                (byte) 0x82, (byte) (YY % 100), (byte) MM, (byte) DD, (byte) hh, (byte) mm, (byte) ss, (byte) selectedTable, (byte) 0x0, (byte) 0x0};

        Log.i(TAG, "On Click Start Aerial: " + Converters.getHexValue(b));
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        isScanning = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (isScanning) {
            parameterWrite = "";
            setVisibility("scanning");
            removeHold();
            isRecord = autoRecord == 1;
            if (isRecord) setRecord(); else removeRecord();
            setGpsOff(R.string.lb_searching_gps);
            gps_state_aerial_textView.setVisibility(gps == 1 ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Enables notification for receive the data.
     * Service name: Screen.
     * Characteristic name: SendLog.
     */
    private void onClickLog() {
        parameterWrite = "startAerial";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, WAITING_PERIOD);
    }

    /**
     * Writes a value for stop scan.
     * Service name: Scan.
     * Characteristic name: Aerial.
     */
    private void onClickStop() {
        byte[] b = new byte[] {(byte) 0x87};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            clear();
            isScanning = false;

            if (previousScanning) {
                parameter = "aerial";
                new Handler().postDelayed(() -> {
                    mBluetoothLeService.discovering();
                }, WAITING_PERIOD);
            } else {
                parameter = "";
            }
            setVisibility("overview");
            animationDrawable.stop();
        }
    }

    /**
     * Writes a value for hold scan.
     * Service name: Scan.
     * Characteristic name: Aerial.
     */
    private void onClickHold() {
        byte[] b = new byte[] {(byte) 0x81};
        Log.i(TAG, "START HOLD AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            isHold = true;
            setHold();
        }
    }

    /**
     * Writes a value for remove hold scan.
     * Service name: Scan.
     * Characteristic name: Aerial.
     */
    private void onClickRemoveHold() {
        byte[] b = new byte[] {(byte) 0x80};
        Log.i(TAG, "REMOVE HOLD AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            isHold = false;
            removeHold();
        }
    }

    private void onClickLogScanning() {
        parameter = "sendLog";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            if (newFrequency != 0)
                mBluetoothLeService.discoveringSecond();
        }, WAITING_PERIOD);
    }

    /**
     * Requests a read for get the number of frequencies from each table and display it.
     * Service name: StoredData.
     * Characteristic name: FreqTable.
     */
    private void onClickTables() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        mBluetoothLeService.readCharacteristicDiagnosticSecond(service, characteristic);
    }

    private void onClickAddFrequency() {
        Log.i(TAG, "ADD FREQUENCY AERIAL");
        byte[] b = new byte[] {(byte) 0x5D, (byte) ((newFrequency - baseFrequency) / 256),
                (byte) ((newFrequency - baseFrequency) % 256)};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            parameterWrite = "";
            newFrequency = 0;
            manageMessage(R.string.lb_frequency_added);
        }
    }

    private void onClickDeleteFrequency() {
        int index = Integer.parseInt(table_index_aerial_textView.getText().toString());
        byte[] b = new byte[] {(byte) 0x5C, (byte) (index / 256), (byte) (index % 256)};
        Log.i(TAG, "DELETE FREQUENCY AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            manageMessage(R.string.lb_frequency_deleted);
        }
    }

    private void onClickMerge() {
        for (int i = 0; i < tableMergeListAdapter.getCount();  i++) {
            if (tableMergeListAdapter.isSelected(i)) {
                byte[] b = new byte[]{(byte) 0x8A, (byte) tableMergeListAdapter.getTableNumber(i)};
                Log.i(TAG, "MERGE AERIAL " + tableMergeListAdapter.getTableNumber(i));

                UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
                UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
                boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

                if (result) {
                    tableMergeListAdapter.setNotSelected(i);
                }
                new Handler().postDelayed(() -> {
                    mBluetoothLeService.discoveringSecond();
                }, WAITING_PERIOD);
                return;
            }
        }
        manageMessage(R.string.lb_tables_merged);
    }

    private void onClickDecrease() {
        byte[] b = new byte[] {(byte) 0x5E};
        Log.i(TAG, "DECREASE FREQUENCY AERIAL " + Converters.getDecimalValue(b));

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            if (newFrequency == baseFrequency) {
                decrease_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease_light));
                decrease_imageView.setEnabled(false);
            } else if (newFrequency == frequencyRange - 1) {
                increase_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase));
                increase_imageView.setEnabled(true);
            }
        }
    }

    private void onClickIncrease() {
        byte[] b = new byte[] {(byte) 0x5F};
        Log.i(TAG, "INCREASE FREQUENCY AERIAL " + Converters.getDecimalValue(b) + " : " + newFrequency);

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            if (newFrequency == baseFrequency + 1) {
                decrease_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease));
                decrease_imageView.setEnabled(true);
            } else if (newFrequency == frequencyRange) {
                increase_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase_light));
                increase_imageView.setEnabled(false);
            }
        }
    }

    /**
     * Records the specific code information, the code received.
     */
    private void onClickRecord() {
        byte[] b = new byte[] {(byte) 0x8C};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);
        Log.i(TAG, "RECORD: " + Converters.getDecimalValue(b));

        if (result) {
            isRecord = true;
            setRecord();
        }
    }

    private void onClickStopRecord() {
        byte[] b = new byte[] {(byte) 0x8E};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);
        Log.i(TAG, "RECORD: " + Converters.getDecimalValue(b));

        if (result) {
            isRecord = false;
            removeRecord();
        }
    }

    private void onClickLeft() {
        byte[] b = new byte[] {(byte) 0x57};
        Log.i(TAG, "LEFT AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);
    }

    private void onClickRight() {
        byte[] b = new byte[] {(byte) 0x58};
        Log.i(TAG, "RIGHT AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SCAN_TABLE;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);
    }

    @OnClick(R.id.edit_aerial_settings_button)
    public void onClickEditAerialSettings(View v) {
        parameter = "aerial";
        Intent intent = new Intent(this, AerialDefaultsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.start_aerial_button)
    public void onClickStartAerial(View v) {
        parameter = "sendLog";
        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.hold_aerial_button)
    public void onClickHoldAerial(View v) {
        parameterWrite = isHold ? "removeHold" : "hold";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.decrease_imageView)
    public void onClickDecrease(View v) {
        newFrequency = getFrequencyNumber(frequency_aerial_textView.getText().toString()) - 1;
        parameterWrite = "decrease";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.increase_imageView)
    public void onClickIncrease(View v) {
        newFrequency = getFrequencyNumber(frequency_aerial_textView.getText().toString()) + 1;
        parameterWrite = "increase";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.edit_table_textView)
    public void onClickEditTable(View v) {
        setVisibility("editTable");
        current_frequency_aerial_textView.setText(frequency_aerial_textView.getText());
        current_index_aerial_textView.setText(table_index_aerial_textView.getText());
    }

    @OnClick(R.id.add_frequency_scan_button)
    public void onClickAddFrequencyScan(View v) {
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra("title", "Add a Frequency to Table");
        intent.putExtra("position", -1);
        intent.putExtra("baseFrequency", baseFrequency);
        intent.putExtra("range", range);
        launcher.launch(intent);
    }

    @OnClick(R.id.delete_frequency_scan_button)
    public void onClickDeleteFrequencyScan(View v) {
        parameterWrite = "deleteFrequency";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.merge_table_scan_button)
    public void onClickMergeTableScan(View v) {
        setVisibility("mergeTable");
        if (tableMergeListAdapter == null) {
            parameterWrite = "tables";
            mBluetoothLeService.discoveringSecond();
        }
    }

    @OnClick(R.id.merge_tables_button)
    public void onClickMergeTables(View v) {
        parameterWrite = "merge";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.record_data_aerial_button)
    public void onClickRecordData(View v) {
        parameterWrite = isRecord ? "stopRecordData" : "recordData";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.left_imageView)
    public void onClickLeft(View v) {
        parameterWrite = "left";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.right_imageView)
    public void onClickRight(View v) {
        parameterWrite = "right";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.edit_audio_aerial_textView)
    public void onClickEditAudio(View v) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.audio_options, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        ImageButton close = view.findViewById(R.id.close_imageButton);
        Button single = view.findViewById(R.id.single_button);
        Button all = view.findViewById(R.id.all_button);
        Button none = view.findViewById(R.id.none_button);
        LinearLayout enterDigit = view.findViewById(R.id.enter_digit_linearLayout);
        TextView number = view.findViewById(R.id.digit_textView);
        Button one = view.findViewById(R.id.one_button);
        Button two = view.findViewById(R.id.two_button);
        Button three = view.findViewById(R.id.three_button);
        Button four = view.findViewById(R.id.four_button);
        Button five = view.findViewById(R.id.five_button);
        Button six = view.findViewById(R.id.six_button);
        Button seven = view.findViewById(R.id.seven_button);
        Button eight = view.findViewById(R.id.eight_button);
        Button nine = view.findViewById(R.id.nine_button);
        Button zero = view.findViewById(R.id.zero_button);
        ImageView delete = view.findViewById(R.id.delete_imageView);
        Button saveChanges = view.findViewById(R.id.save_digit_button);
        close.setOnClickListener(v1 -> dialog.dismiss());
        single.setOnClickListener(v14 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            single.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            all.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            none.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            enterDigit.setVisibility(View.VISIBLE);
            number.setText("");
        });
        all.setOnClickListener(v15 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            single.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            all.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            none.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            enterDigit.setVisibility(View.GONE);
        });
        none.setOnClickListener(v16 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            single.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            all.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            none.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            enterDigit.setVisibility(View.GONE);
        });
        View.OnClickListener clickListener = v17 -> {
            String text = number.getText().toString();
            if (!text.isEmpty()) {
                Button buttonNumber = (Button) v17;
                number.setText(text + buttonNumber.getText());
            }
        };
        one.setOnClickListener(clickListener);
        two.setOnClickListener(clickListener);
        three.setOnClickListener(clickListener);
        four.setOnClickListener(clickListener);
        five.setOnClickListener(clickListener);
        six.setOnClickListener(clickListener);
        seven.setOnClickListener(clickListener);
        eight.setOnClickListener(clickListener);
        nine.setOnClickListener(clickListener);
        zero.setOnClickListener(clickListener);
        delete.setOnClickListener(v13 -> {
            String text = number.getText().toString();
            number.setText(text.substring(0, text.length() - 1));
        });
        saveChanges.setOnClickListener(v12 -> dialog.dismiss());

        dialog.setView(view);
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerial_scan);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        isScanning = getIntent().getExtras().getBoolean("scanning");
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_status_textView.setText(receiverInformation.getDeviceStatus());
        device_range_textView.setText(receiverInformation.getDeviceRange());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        baseFrequency = sharedPreferences.getInt("BaseFrequency", 0) * 1000;
        range = sharedPreferences.getInt("Range", 0);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;

        isHold = false;
        newFrequency = 0;
        handlerMessage = new Handler();

        if (isScanning) { // The device is already scanning
            previousScanning = true;
            parameter = "sendLogScanning";

            isHold = getIntent().getExtras().getBoolean("isHold");
            isRecord = getIntent().getExtras().getBoolean("autoRecord");
            int currentFrequency = getIntent().getExtras().getInt("frequency") + baseFrequency;
            int currentIndex = getIntent().getExtras().getInt("index");
            int total = getIntent().getExtras().getInt("maxIndex");
            detectionType = getIntent().getExtras().getByte("detectionType");
            frequency_aerial_textView.setText(getFrequency(currentFrequency));
            table_index_aerial_textView.setText(String.valueOf(currentIndex));
            max_index_aerial_textView.setText("Table Index (" + total + " Total)");
            table_total_aerial_textView.setText(String.valueOf(total));
            autoRecord = isRecord ? 1 : 0;
            if (isHold) setHold(); else removeHold();
            if (isRecord) setRecord(); else removeRecord();

            updateVisibility();
            setVisibility("scanning");
        } else { // Gets aerial defaults or temporary data
            boolean isTemporary = getIntent().getExtras().getBoolean("temporary");
            if (isTemporary) {
                String scanTime = getIntent().getExtras().getString("scanTime");
                String tableNumber = getIntent().getExtras().getString("tableNumber");
                String gps = getIntent().getExtras().getString("gps");
                String autoRecordState = getIntent().getExtras().getString("autoRecord");
                scan_rate_aerial_textView.setText(scanTime);
                selected_frequency_aerial_textView.setText(tableNumber);
                gps_aerial_textView.setText(gps);
                auto_record_aerial_textView.setText(autoRecordState);

                autoRecord = autoRecordState.equals("ON") ? 1 : 0;
            } else {
                parameter = "aerial";
            }
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
                Intent intent = new Intent(this, AerialScanningActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                if (edit_table_linearLayout.getVisibility() == View.VISIBLE) {
                    setVisibility("scanning");
                } else if (merge_tables_linearLayout.getVisibility() == View.VISIBLE) {
                    setVisibility("editTable");
                    changeAllCheckBox(false);
                } else {
                    parameterWrite = "stopAerial";
                    mBluetoothLeService.discoveringSecond();
                    Log.i(TAG, "STOP AERIAL");
                }
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
            builder.setTitle("Stop Aerial");
            builder.setMessage("Are you sure you want to stop scanning?");
            builder.setPositiveButton("OK", (dialog, which) -> {
                parameterWrite = "stopAerial";
                mBluetoothLeService.discoveringSecond();
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.catskill_white)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(mGattUpdateReceiverWrite, makeGattUpdateIntentFilterWrite());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
            Log.i(TAG, "Existing connection: " + result);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected)
            showDisconnectionMessage();
        return true;
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        parameter = "";
        parameterWrite = "";
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

    private void setVisibility(String value) {
        switch (value) {
            case "overview":
                ready_aerial_scan_LinearLayout.setVisibility(View.VISIBLE);
                aerial_result_linearLayout.setVisibility(View.GONE);
                edit_table_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.aerial_scanning);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
                device_status_textView.setText(receiverInformation.getDeviceStatus());
                break;
            case "scanning":
                ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
                aerial_result_linearLayout.setVisibility(View.VISIBLE);
                edit_table_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_aerial_scanning);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
                state_view.setBackgroundResource(R.drawable.scanning_animation);
                animationDrawable = (AnimationDrawable) state_view.getBackground();
                animationDrawable.start();
                device_status_textView.setText(receiverInformation.getDeviceStatus().replace("Not scanning", "Scanning, mobile"));
                break;
            case "editTable":
                ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
                aerial_result_linearLayout.setVisibility(View.GONE);
                edit_table_linearLayout.setVisibility(View.VISIBLE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_aerial_scanning);
                break;
            case "mergeTable":
                ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
                aerial_result_linearLayout.setVisibility(View.GONE);
                edit_table_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.VISIBLE);
                title_toolbar.setText(R.string.lb_merge_tables);
                break;
        }
    }

    private void updateVisibility() {
        int visibility = Converters.getHexValue(detectionType).equals("09") ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        mortality_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        audio_aerial_linearLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);
    }

    private void manageMessage(int idStringMessage) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.frequency_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        TextView state_message_textView = view.findViewById(R.id.state_message_textView);
        state_message_textView.setText(idStringMessage);

        dialog.setView(view);
        dialog.show();

        int MESSAGE_PERIOD = 1000;
        handlerMessage.postDelayed(() -> {
            dialog.dismiss();
            title_toolbar.setText(R.string.aerial_scanning);
            setVisibility("scanning");
        }, MESSAGE_PERIOD);
    }

    /**
     * Displays the tables on the screen.
     */
    private void downloadTables(byte[] data) {
        parameterWrite = "";
        ArrayList<Integer> frequencies = new ArrayList<>();
        ArrayList<Integer> tables = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            if (data[i] != 0) {
                frequencies.add(Integer.parseInt(Converters.getDecimalValue(data[i])));
                tables.add(i);
            }
        }
        tableMergeListAdapter = new TableMergeListAdapter(this, tables, frequencies, merge_tables_button);
        tables_merge_listView.setAdapter(tableMergeListAdapter);
    }

    private void changeAllCheckBox(boolean isChecked) {
        tableMergeListAdapter.setStateSelected(isChecked);
        tableMergeListAdapter.notifyDataSetChanged();

        if (isChecked) {
            merge_tables_button.setEnabled(true);
            merge_tables_button.setAlpha(1);
        } else {
            merge_tables_button.setEnabled(false);
            merge_tables_button.setAlpha((float) 0.6);
        }
    }

    /**
     * With the received packet, gets aerial defaults data.
     *
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (data.length == 1) {
            parameter = "aerial";
            mBluetoothLeService.discovering();
        } else if (Converters.getHexValue(data[0]).equals("6D")) {
            parameter = "";
            if (Integer.parseInt(Converters.getDecimalValue(data[1])) == 0) { // There are no tables with frequencies to scan
                selected_frequency_aerial_textView.setText(R.string.lb_none);
                start_aerial_button.setEnabled(false);
                start_aerial_button.setAlpha((float) 0.6);
            } else { // Shows the table to be scanned
                selected_frequency_aerial_textView.setText(Converters.getDecimalValue(data[1]));
                start_aerial_button.setEnabled(true);
                start_aerial_button.setAlpha((float) 1);
            }
            selectedTable = Integer.parseInt(Converters.getDecimalValue(data[1]));
            float scanTime = (float) (Integer.parseInt(Converters.getDecimalValue(data[3])) * 0.1);
            scan_rate_aerial_textView.setText(String.valueOf(scanTime));
            gps = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 7 & 1;
            gps_aerial_textView.setText((gps == 1) ? R.string.lb_on : R.string.lb_off);
            autoRecord = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1;
            auto_record_aerial_textView.setText((autoRecord == 1) ? R.string.lb_on : R.string.lb_off);
        }
    }

    private String getFrequency(int frequency) {
        return String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3);
    }

    private int getFrequencyNumber(String frequency) {
        return Integer.parseInt(frequency.replace(".", ""));
    }

    private void setHold() {
        hold_aerial_button.setText(R.string.lb_release);
        hold_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lock));
        frequency_aerial_textView.setTextColor(ContextCompat.getColor(this, mountain_meadow));
        edit_table_textView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        edit_table_textView.setEnabled(true);
    }

    private void removeHold() {
        hold_aerial_button.setText(R.string.lb_hold);
        hold_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_unlock));
        frequency_aerial_textView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        edit_table_textView.setTextColor(ContextCompat.getColor(this, ghost));
        edit_table_textView.setEnabled(false);
    }

    private void setRecord() {
        record_data_aerial_button.setText(R.string.lb_stop_recording);
        record_data_aerial_button.setBackground(ContextCompat.getDrawable(this, button_delete));
    }

    private void removeRecord() {
        record_data_aerial_button.setText(R.string.lb_record_data);
        record_data_aerial_button.setBackground(ContextCompat.getDrawable(this, button_primary));
    }

    private void setGpsOff(int idString) {
        gps_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_off));
        gps_state_aerial_textView.setText(idString);
    }

    private void setGpsOn() {
        gps_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_on));
        gps_state_aerial_textView.setText(R.string.lb_valid_gps);
    }

    /**
     * With the received packet, gets the data of scanning.
     *
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        switch (Converters.getHexValue(data[0])) {
            case "50":
                scanState(data);
                break;
            case "51":
                gpsState(data);
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "F1":
                logScanFix(data);
                break;
            case "A1":
                //GPS
                break;
            default: //E1 and E2
                logScanData(data);
                break;
        }
    }

    private void scanState(byte[] data) {
        int maxIndex = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        max_index_aerial_textView.setText("Table Index (" + maxIndex + " Total)");
        table_total_aerial_textView.setText(String.valueOf(maxIndex));
        detectionType = data[18];
        updateVisibility();
    }

    private void gpsState(byte[] data) {
        int state = Integer.parseInt(Converters.getDecimalValue(data[1]));
        if (state == 3)
            setGpsOn();
        else if (state == 2)
            setGpsOff(R.string.lb_failed_gps);
        else if (state == 1)
            setGpsOff(R.string.lb_searching_gps);
    }

    /**
     * With the received packet, processes the data of scan header to display.
     *
     * @param data The received packet.
     */
    private void logScanHeader(byte[] data) {
        clear();
        int frequency = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + baseFrequency;
        table_index_aerial_textView.setText(Converters.getDecimalValue(data[3]));
        frequency_aerial_textView.setText(getFrequency(frequency));
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is code.
     *
     * @param data The received packet.
     */
    private void logScanFix(byte[] data) {
        int position;
        int code = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = Integer.parseInt(Converters.getDecimalValue(data[7]));
        int mort = Integer.parseInt(Converters.getDecimalValue(data[5]));

        if (scan_details_linearLayout.getChildCount() > 2 && isEqualFirstCode(code)) {
            refreshFirstCode(signalStrength, mort > 0);
        } else if ((position = positionCode(code)) != 0) {
            refreshPosition(position, signalStrength, mort > 0);
        } else {
            createCodeDetail(code, signalStrength, detections, mort > 0);
        }
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is code.
     *
     * @param data The received packet.
     */
    private void logScanFixConsolidated(byte[] data) {
        int position;
        int code = Integer.parseInt(Converters.getDecimalValue(data[3]));
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = Integer.parseInt(Converters.getDecimalValue(data[7]));
        int mort = Integer.parseInt(Converters.getDecimalValue(data[5]));

        if (scan_details_linearLayout.getChildCount() > 2 && isEqualFirstCode(code)) {
            refreshFirstCode(signalStrength, mort > 1);
        } else if ((position = positionCode(code)) != 0) {
            refreshPosition(position, signalStrength, mort > 0);
        } else {
            createCodeDetail(code, signalStrength, detections, mort > 0);
        }
    }

    /**
     * With the received packet, processes the data to display. The pulse rate type is non code.
     *
     * @param data The received packet.
     */
    private void logScanData(byte[] data) {
        int period = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        float pulseRate = (float) (60000 / period);
        int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
        int detections = Integer.parseInt(Converters.getDecimalValue(data[2])) / 10;

        refreshNonCoded(period, pulseRate, signalStrength, detections);
    }

    /**
     * Creates a row of code data and display it.
     *
     * @param code Number of code.
     * @param signalStrength Number of signal strength.
     * @param detections Number of detections.
     * @param isMort True, if the code is mort.
     */
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

        refreshCode(scan_details_linearLayout.getChildCount() - 2, code, signalStrength, detections + 1, isMort, 0); //detections start in 1
    }

    /**
     * Checks that the first code in the table is equal to code received.
     *
     * @param code Number of code received.
     *
     * @return Returns true, if the first code is equal to code received.
     */
    private boolean isEqualFirstCode(int code) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);

        return Integer.parseInt(codeTextView.getText().toString()) == code;
    }

    /**
     * Updates data in the first row in the table.
     *
     * @param signalStrength Number of signal strength to update.
     */
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
        detections = Integer.parseInt(detectionsTextView.getText().toString());
        mort = Integer.parseInt(mortTextView.getText().toString());
    }

    /**
     * Looks for the position in the table of code received.
     *
     * @param code Number of code to look.
     *
     * @return Returns the position of code in the table.
     */
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

    /**
     * Updates data at a specific position in the table.
     *
     * @param position Number of position in the table to update.
     * @param signalStrength Number of signal strength.
     */
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

    /**
     * Updates data from a specific position to the first position in the table.
     *
     * @param finalPosition Number of rows to update in the table.
     * @param code Number of code received.
     * @param signalStrength Number of signal strength received.
     * @param detections Number of signal strength calculated.
     * @param isMort True, if the code is mort.
     */
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

        this.detections =  detections;
        this.mort = isMort ? mort + 1 : mort;
        Log.i(TAG, "Code: " + newCodeTextView.getText() + " SS: " + newSignalStrengthTextView.getText() + " Det: " + newDetectionsTextView.getText() + " Mort: " + newMortTextView.getText() + " Size: " + scan_details_linearLayout.getChildCount());
    }

    /**
     * Creates a row of non code data and display it.
     *
     * @param period Number of period received.
     * @param pulseRate Number of pulse rate received.
     * @param signalStrength Number of signal strength received.
     * @param detections Number of detections received.
     */
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
        table_index_aerial_textView.setText("");
        frequency_aerial_textView.setText("");
        int count = scan_details_linearLayout.getChildCount();
        while (count > 2) {
            scan_details_linearLayout.removeViewAt(2);
            count--;
        }
    }
}
