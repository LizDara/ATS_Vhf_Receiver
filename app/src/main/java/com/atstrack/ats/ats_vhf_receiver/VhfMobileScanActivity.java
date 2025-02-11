package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableMergeListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.AudioOptions;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ghost;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.mountain_meadow;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_primary;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease_light;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_increase_light;
import static com.atstrack.ats.ats_vhf_receiver.R.style.body_regular;

public class VhfMobileScanActivity extends AppCompatActivity {

    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.ready_aerial_scan_LinearLayout)
    LinearLayout ready_aerial_scan_LinearLayout;
    @BindView(R.id.scan_rate_seconds_aerial_textView)
    TextView scan_rate_seconds_aerial_textView;
    @BindView(R.id.frequency_table_number_aerial_textView)
    TextView frequency_table_number_aerial_textView;
    @BindView(R.id.aerial_gps_switch)
    SwitchCompat aerial_gps_switch;
    @BindView(R.id.aerial_auto_record_switch)
    SwitchCompat aerial_auto_record_switch;
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
    @BindView(R.id.id_audio_aerial_textView)
    TextView id_audio_aerial_textView;
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
    @BindView(R.id.view_detection_aerial_textView)
    TextView view_detection_aerial_textView;

    private final static String TAG = VhfMobileScanActivity.class.getSimpleName();

    private final Context mContext = this;
    private Handler handlerMessage;
    private TableMergeListAdapter tableMergeListAdapter;
    private AnimationDrawable animationDrawable;
    private boolean isScanning;
    private boolean previousScanning;
    private boolean isHold;
    private boolean isRecord; //This can change during scanning
    private int baseFrequency;
    private int frequencyRange;
    private int range;
    private byte detectionType;
    private int newFrequency;
    private int selectedTable;
    private int autoRecord; //This is the default record
    private int gps;
    private final byte[] audioOption = {(byte) 0x5A, 0, 0};
    private DialogFragment viewDetectionFilter;
    private DialogFragment audioOptions;
    private byte[] aerialDefaults;

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
                        case ValueCodes.MOBILE_DEFAULTS: // Gets aerial defaults data
                            TransferBleData.readDefaults(true);
                            break;
                        case ValueCodes.START_LOG: // Receives the data
                            setNotificationLog();
                            break;
                        case ValueCodes.CONTINUE_LOG:
                            setNotificationLogScanning();
                            break;
                        case ValueCodes.TABLE_NUMBER:
                        case ValueCodes.SCAN_RATE:
                        case ValueCodes.GPS:
                        case ValueCodes.AUTO_RECORD:
                            setTemporary();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    Log.i(TAG, Converters.getHexValue(packet));
                    if (packet == null) return;
                    switch (parameter) {
                        case ValueCodes.MOBILE_DEFAULTS: // Gets aerial defaults data
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
                            setStartScan();
                            break;
                        case ValueCodes.STOP_SCAN: // Stops scan
                            setStopScan();
                            break;
                        case ValueCodes.HOLD:
                            setHoldScan();
                            break;
                        case ValueCodes.TABLES:
                            TransferBleData.readTables(true);
                            break;
                        case ValueCodes.ADD_FREQUENCY:
                            setNewFrequency();
                            break;
                        case ValueCodes.DELETE_FREQUENCY:
                            setDeleteFrequency();
                            break;
                        case ValueCodes.DECREASE:
                            setDecreaseOrIncrease(true);
                            break;
                        case ValueCodes.INCREASE:
                            setDecreaseOrIncrease(false);
                            break;
                        case ValueCodes.MERGE:
                            setMergeTable();
                            break;
                        case ValueCodes.RECORD:
                            setRecordScan();
                            break;
                        case ValueCodes.LEFT:
                            TransferBleData.writeLeftRight(true);
                            break;
                        case ValueCodes.RIGHT:
                            TransferBleData.writeLeftRight(false);
                            break;
                        case ValueCodes.AUDIO:
                            setAudio();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE_SECOND.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameterWrite.equals(ValueCodes.TABLES))
                        downloadTables(packet);
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
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    newFrequency = value;
                    parameter = ValueCodes.CONTINUE_LOG;
                    parameterWrite = ValueCodes.ADD_FREQUENCY;
                    return;
                } else if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                    frequency_table_number_aerial_textView.setText(String.valueOf(value));
                    parameter = ValueCodes.TABLE_NUMBER;
                } else if (ValueCodes.SCAN_RATE_SECONDS_CODE == result.getResultCode()) { // Gets the modified scan rate
                    scan_rate_seconds_aerial_textView.setText(String.valueOf(value * 0.1));
                    parameter = ValueCodes.SCAN_RATE;
                }
                leServiceConnection.getBluetoothLeService().discovering();
            });

    private void setTemporary() {
        byte[] b = new byte[]{(byte) 0x6F, aerialDefaults[1], aerialDefaults[2], aerialDefaults[3]};
        switch (parameter) {
            case ValueCodes.TABLE_NUMBER:
                b[1] = (byte) Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
                break;
            case ValueCodes.SCAN_RATE:
                b[3] = (byte) (Float.parseFloat(scan_rate_seconds_aerial_textView.getText().toString()) * 10);
                break;
            case ValueCodes.GPS:
                b[2] = aerial_gps_switch.isChecked() ? (byte) (Integer.parseInt(Converters.getDecimalValue(aerialDefaults[2])) + 128)
                        : (byte) (Integer.parseInt(Converters.getDecimalValue(aerialDefaults[2])) - 128);
                break;
            case ValueCodes.AUTO_RECORD:
                b[2] = aerial_auto_record_switch.isChecked() ? (byte) (Integer.parseInt(Converters.getDecimalValue(aerialDefaults[2])) + 64)
                        : (byte) (Integer.parseInt(Converters.getDecimalValue(aerialDefaults[2])) - 64);
                break;
        }
        boolean result = TransferBleData.writeDefaults(true, b);
        if (result) parameter = "";
        else downloadData(aerialDefaults);
        aerial_gps_switch.setEnabled(true);
        aerial_auto_record_switch.setEnabled(true);
    }

    private void setStartScan() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);

        byte[] b = new byte[] {
                (byte) 0x82, (byte) (YY % 100), (byte) (MM + 1), (byte) DD, (byte) hh, (byte) mm, (byte) ss, (byte) selectedTable, (byte) 0x0, (byte) 0x0};
        isScanning = TransferBleData.writeStartScan(ValueCodes.MOBILE_DEFAULTS, b);
        if (isScanning) {
            parameterWrite = "";
            setVisibility("scanning");
            removeHold();
            isRecord = autoRecord == 1;
            if (isRecord) setRecord(); else removeRecord();
            if (gps == 1) setGpsSearching(); else setGpsOff();
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
        boolean result = TransferBleData.writeStopScan(ValueCodes.MOBILE_DEFAULTS);
        if (result) {
            clear();
            isScanning = false;
            parameterWrite = "";
            if (previousScanning) {
                parameter = ValueCodes.MOBILE_DEFAULTS;
                new Handler().postDelayed(() -> {
                    leServiceConnection.getBluetoothLeService().discovering();
                }, ValueCodes.WAITING_PERIOD);
            } else {
                parameter = "";
            }
            setVisibility("overview");
            animationDrawable.stop();
        }
    }

    private void setHoldScan() {
        boolean result = TransferBleData.setHold(isHold);
        if (result) {
            parameterWrite = "";
            isHold = !isHold;
            if (isHold) setHold();
            else removeHold();
        }
    }

    private void setNotificationLogScanning() {
        parameter = ValueCodes.START_LOG;
        TransferBleData.notificationLog();

        new Handler().postDelayed(() -> {
            if (newFrequency != 0)
                leServiceConnection.getBluetoothLeService().discoveringSecond();
        }, ValueCodes.WAITING_PERIOD);
    }

    /**
     * Writes a value for add a frequency in the table.
     */
    private void setNewFrequency() {
        byte[] b = new byte[] {(byte) 0x5D, (byte) ((newFrequency - baseFrequency) / 256),
                (byte) ((newFrequency - baseFrequency) % 256)};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
            parameterWrite = "";
            newFrequency = 0;
            manageMessage(R.string.lb_frequency_added);
        }
    }

    /**
     * Writes a value for delete a frequency of the table.
     */
    private void setDeleteFrequency() {
        int index = Integer.parseInt(table_index_aerial_textView.getText().toString());
        byte[] b = new byte[] {(byte) 0x5C, (byte) (index / 256), (byte) (index % 256)};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
            parameterWrite = "";
            manageMessage(R.string.lb_frequency_deleted);
        }
    }

    /**
     * Writes a value for merge other tables to the selected table.
     */
    private void setMergeTable() {
        byte[] b = new byte[]{(byte) 0x8A, (byte) tableMergeListAdapter.getTableNumber()};
        boolean result = TransferBleData.writeScanning(b);
        if (result) manageMessage(R.string.lb_tables_merged);
    }

    private void setDecreaseOrIncrease(boolean isDecrease) {
        boolean result = TransferBleData.writeDecreaseIncrease(isDecrease);
        if (result) {
            parameterWrite = "";
            if (isDecrease) {
                if (newFrequency == baseFrequency) {
                    decrease_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease_light));
                    decrease_imageView.setEnabled(false);
                } else if (newFrequency == frequencyRange - 1) {
                    increase_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase));
                    increase_imageView.setEnabled(true);
                }
            } else {
                if (newFrequency == baseFrequency + 1) {
                    decrease_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease));
                    decrease_imageView.setEnabled(true);
                } else if (newFrequency == frequencyRange) {
                    increase_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase_light));
                    increase_imageView.setEnabled(false);
                }
            }
        }
    }

    private void setRecordScan() {
        boolean result = TransferBleData.writeRecord(!isRecord, false);
        if (result) {
            parameterWrite = "";
            isRecord = !isRecord;
            if (isRecord) setRecord();
            else removeRecord();
        }
    }

    private void setAudio() {
        byte[] b;
        String audioDescription = "All";
        if (Converters.getHexValue(audioOption[0]).equals("59"))
            b = new byte[] {audioOption[0], audioOption[1], audioOption[2]};
        else
            b = new byte[] {audioOption[0], audioOption[2]};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
            if (Converters.getHexValue(audioOption[0]).equals("59"))
                audioDescription = "Single (" + Converters.getDecimalValue(audioOption[1]) + ")";
            else if (Converters.getHexValue(audioOption[0]).equals("5B"))
                audioDescription = "None";
            id_audio_aerial_textView.setText(audioDescription);
        }
    }

    @OnClick(R.id.frequency_table_number_aerial_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, VhfValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
        intent.putExtra(ValueCodes.TABLE, Integer.parseInt(frequency_table_number_aerial_textView.getText().toString()));
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_aerial_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, VhfValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.MOBILE_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_SECONDS_CODE);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.aerial_gps_switch)
    public void onCheckedChangedGps(CompoundButton button, boolean isChecked) {
        if (parameter.isEmpty()) {
            parameter = ValueCodes.GPS;
            leServiceConnection.getBluetoothLeService().discovering();
            aerial_gps_switch.setEnabled(false);
        }
    }

    @OnCheckedChanged(R.id.aerial_auto_record_switch)
    public void onCheckedChangedAutoRecord(CompoundButton button, boolean isChecked) {
        if (parameter.isEmpty()) {
            parameter = ValueCodes.AUTO_RECORD;
            leServiceConnection.getBluetoothLeService().discovering();
            aerial_auto_record_switch.setEnabled(false);
        }
    }

    @OnClick(R.id.start_aerial_button)
    public void onClickStartAerial(View v) {
        parameter = ValueCodes.START_LOG;
        leServiceConnection.getBluetoothLeService().discovering();
    }

    @OnClick(R.id.hold_aerial_button)
    public void onClickHoldAerial(View v) {
        parameterWrite = ValueCodes.HOLD;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.decrease_imageView)
    public void onClickDecrease(View v) {
        newFrequency = Converters.getFrequencyNumber(frequency_aerial_textView.getText().toString()) - 1;
        parameterWrite = ValueCodes.DECREASE;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.increase_imageView)
    public void onClickIncrease(View v) {
        newFrequency = Converters.getFrequencyNumber(frequency_aerial_textView.getText().toString()) + 1;
        parameterWrite = ValueCodes.INCREASE;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.edit_table_textView)
    public void onClickEditTable(View v) {
        setVisibility("editTable");
        current_frequency_aerial_textView.setText(frequency_aerial_textView.getText());
        current_index_aerial_textView.setText(table_index_aerial_textView.getText());
    }

    @OnClick(R.id.add_frequency_scan_button)
    public void onClickAddFrequencyScan(View v) {
        Intent intent = new Intent(this, VhfEnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, "Add a Frequency to Table");
        intent.putExtra(ValueCodes.POSITION, -1);
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.delete_frequency_scan_button)
    public void onClickDeleteFrequencyScan(View v) {
        parameterWrite = ValueCodes.DELETE_FREQUENCY;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.merge_table_scan_button)
    public void onClickMergeTableScan(View v) {
        setVisibility("mergeTable");
        if (tableMergeListAdapter == null) {
            parameterWrite = ValueCodes.TABLES;
            leServiceConnection.getBluetoothLeService().discoveringSecond();
            Log.i(TAG, "Tables is null");
        }
    }

    @OnClick(R.id.merge_tables_button)
    public void onClickMergeTables(View v) {
        parameterWrite = ValueCodes.MERGE;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.record_data_aerial_button)
    public void onClickRecordData(View v) {
        parameterWrite = ValueCodes.RECORD;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.left_imageView)
    public void onClickLeft(View v) {
        parameterWrite = ValueCodes.LEFT;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.right_imageView)
    public void onClickRight(View v) {
        parameterWrite = ValueCodes.RIGHT;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    @OnClick(R.id.edit_audio_aerial_textView)
    public void onClickEditAudio(View v) {
        getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                parameterWrite = bundle.getString(ValueCodes.PARAMETER);
                if (parameterWrite != null && parameterWrite.equals(ValueCodes.AUDIO)) {
                    audioOption[0] = bundle.getByte(ValueCodes.AUDIO);
                    audioOption[1] = (byte) bundle.getInt(ValueCodes.VALUE);
                    audioOption[2] = bundle.getByte(ValueCodes.BACKGROUND);

                    leServiceConnection.getBluetoothLeService().discoveringSecond();
                }
            }
        });
        audioOptions.show(getSupportFragmentManager(), AudioOptions.TAG);
    }

    @OnClick(R.id.view_detection_aerial_textView)
    public void onClickViewDetection(View v) {
        viewDetectionFilter.show(getSupportFragmentManager(), ViewDetectionFilter.TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_mobile_scan);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, R.string.aerial_scanning);
        ActivitySetting.setReceiverStatus(this);

        isScanning = getIntent().getBooleanExtra(ValueCodes.SCANNING, false);
        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;

        isHold = false;
        newFrequency = 0;
        handlerMessage = new Handler();
        if (isScanning) { // The device is already scanning
            previousScanning = true;
            parameter = ValueCodes.CONTINUE_LOG;
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);

            int currentFrequency = (Integer.parseInt(Converters.getDecimalValue(data[16])) * 256)
                    + Integer.parseInt(Converters.getDecimalValue(data[17])) + baseFrequency;
            int currentIndex = (Integer.parseInt(Converters.getDecimalValue(data[7])) * 256)
                    + Integer.parseInt(Converters.getDecimalValue(data[8]));
            autoRecord = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1;
            isRecord = autoRecord == 1;
            isHold = Converters.getHexValue(data[1]).equals("81");
            frequency_aerial_textView.setText(Converters.getFrequency(currentFrequency));
            table_index_aerial_textView.setText(String.valueOf(currentIndex));
            if (isHold) setHold(); else removeHold();
            if (isRecord) setRecord(); else removeRecord();

            scanState(data);
            setVisibility("scanning");
        } else { // Gets aerial defaults data
            parameter = ValueCodes.MOBILE_DEFAULTS;
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
                if (edit_table_linearLayout.getVisibility() == View.VISIBLE) {
                    setVisibility("scanning");
                } else if (merge_tables_linearLayout.getVisibility() == View.VISIBLE) {
                    setVisibility("editTable");
                    changeAllCheckBox();
                } else {
                    parameterWrite = ValueCodes.STOP_SCAN;
                    leServiceConnection.getBluetoothLeService().discoveringSecond();
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
        registerReceiver(mGattUpdateReceiverWrite, Converters.makeThirdGattUpdateIntentFilter());
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
                ready_aerial_scan_LinearLayout.setVisibility(View.VISIBLE);
                aerial_result_linearLayout.setVisibility(View.GONE);
                edit_table_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.aerial_scanning);
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
                state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
                break;
            case "scanning":
                ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
                aerial_result_linearLayout.setVisibility(View.VISIBLE);
                edit_table_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_aerial_scanning);
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close);
                state_view.setBackgroundResource(R.drawable.scanning_animation);
                animationDrawable = (AnimationDrawable) state_view.getBackground();
                animationDrawable.start();
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
        view_detection_aerial_textView.setVisibility(visibility);
    }

    private void manageMessage(int idStringMessage) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.frequency_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        TextView state_message_textView = view.findViewById(R.id.state_message_textView);
        state_message_textView.setText(idStringMessage);
        dialog.setView(view);
        dialog.show();

        handlerMessage.postDelayed(() -> {
            dialog.dismiss();
            title_toolbar.setText(R.string.aerial_scanning);
            setVisibility("scanning");
        }, ValueCodes.MESSAGE_PERIOD);
    }

    /**
     * Displays the tables on the screen.
     */
    private void downloadTables(byte[] data) {
        parameterWrite = "";
        ArrayList<Integer> frequencies = new ArrayList<>();
        ArrayList<Integer> tables = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            if (data[i] != 0 && !Converters.getDecimalValue(data[i]).equals("FF ")) {
                frequencies.add(Integer.parseInt(Converters.getDecimalValue(data[i])));
                tables.add(i);
            }
        }
        tableMergeListAdapter = new TableMergeListAdapter(this, tables, frequencies, merge_tables_button);
        tables_merge_listView.setAdapter(tableMergeListAdapter);
    }

    private void changeAllCheckBox() {
        tableMergeListAdapter.initialize();
        tableMergeListAdapter.notifyDataSetChanged();

        merge_tables_button.setEnabled(false);
        merge_tables_button.setAlpha((float) 0.6);
    }

    /**
     * With the received packet, gets aerial defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6D")) {
            aerialDefaults = data;
            selectedTable = Integer.parseInt(Converters.getDecimalValue(data[1]));
            if (selectedTable == 0) { // There are no tables with frequencies to scan
                frequency_table_number_aerial_textView.setText(R.string.lb_none);
                start_aerial_button.setEnabled(false);
                start_aerial_button.setAlpha((float) 0.6);
            } else { // Shows the table to be scanned
                frequency_table_number_aerial_textView.setText(Converters.getDecimalValue(data[1]));
                start_aerial_button.setEnabled(true);
                start_aerial_button.setAlpha((float) 1);
            }
            double scanRate = Integer.parseInt(Converters.getDecimalValue(data[3])) * 0.1;
            scan_rate_seconds_aerial_textView.setText(String.valueOf(scanRate));
            gps = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 7 & 1;
            aerial_gps_switch.setChecked(gps == 1);
            autoRecord = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1;
            aerial_auto_record_switch.setChecked(autoRecord == 1);

            parameter = "";
        }
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

    private void setGpsOff() {
        gps_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_off));
        gps_state_aerial_textView.setText(R.string.lb_off_gps);
    }

    private void setGpsSearching() {
        gps_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_searching));
        gps_state_aerial_textView.setText(R.string.lb_searching_gps);
    }

    private void setGpsFailed() {
        gps_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_failed));
        gps_state_aerial_textView.setText(R.string.lb_failed_gps);
    }

    private void setGpsValid() {
        gps_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_valid));
        gps_state_aerial_textView.setText(R.string.lb_valid_gps);
    }

    /**
     * With the received packet, gets the data of scanning.
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
            case "8A":
                frequenciesNumber(data);
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "F1":
                logScanCoded(data); //Coded 0x09
                break;
            case "A1":
                //GPS
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
        max_index_aerial_textView.setText("Table Index (" + maxIndex + " Total)");
        table_total_aerial_textView.setText(String.valueOf(maxIndex));
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
        } else {
            audioOptions = AudioOptions.newInstance();
        }
    }

    private void gpsState(byte[] data) {
        int state = Integer.parseInt(Converters.getDecimalValue(data[1]));
        if (state == 3) // Valid
            setGpsValid();
        else if (state == 2) // Failed
            setGpsFailed();
        else if (state == 1) // Searching
            setGpsSearching();
    }

    private void frequenciesNumber(byte[] data) {
        int maxIndex = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[2]));
        max_index_aerial_textView.setText("Table Index (" + maxIndex + " Total)");
        table_total_aerial_textView.setText(String.valueOf(maxIndex));
    }

    /**
     * With the received packet, processes the data of scan header to display.
     * @param data The received packet.
     */
    private void logScanHeader(byte[] data) {
        clear();
        int frequency = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + baseFrequency;
        int index = (((Integer.parseInt(Converters.getDecimalValue(data[1])) >> 6) & 1) * 256) + Integer.parseInt(Converters.getDecimalValue(data[3]));
        table_index_aerial_textView.setText(String.valueOf(index));
        frequency_aerial_textView.setText(Converters.getFrequency(frequency));
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
        table_index_aerial_textView.setText("");
        frequency_aerial_textView.setText("");
        int count = scan_details_linearLayout.getChildCount();
        while (count > 2) {
            scan_details_linearLayout.removeViewAt(2);
            count--;
        }
    }
}