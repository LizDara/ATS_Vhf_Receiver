package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailListAdapter;
import com.atstrack.ats.ats_vhf_receiver.Adapters.TableMergeListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.AudioOptions;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.Objects;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ghost;
import static com.atstrack.ats.ats_vhf_receiver.R.color.mountain_meadow;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_primary;

public class MobileScanActivity extends ScanBaseActivity {

    @BindView(R.id.ready_aerial_scan_LinearLayout)
    LinearLayout ready_aerial_scan_LinearLayout;
    @BindView(R.id.scan_rate_seconds_aerial_textView)
    TextView scan_rate_seconds_aerial_textView;
    @BindView(R.id.frequency_table_number_aerial_textView)
    TextView frequency_table_number_aerial_textView;
    @BindView(R.id.gps_switch)
    SwitchCompat gps_switch;
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
    @BindView(R.id.audio_linearLayout)
    LinearLayout audio_linearLayout;
    @BindView(R.id.id_audio_textView)
    TextView id_audio_textView;
    @BindView(R.id.record_data_button)
    Button record_data_button;
    @BindView(R.id.current_frequency_aerial_textView)
    TextView current_frequency_aerial_textView;
    @BindView(R.id.current_index_aerial_textView)
    TextView current_index_aerial_textView;
    @BindView(R.id.table_total_aerial_textView)
    TextView table_total_aerial_textView;
    @BindView(R.id.tables_merge_listView)
    ListView tables_merge_listView;
    @BindView(R.id.gps_imageView)
    ImageView gps_imageView;
    @BindView(R.id.gps_state_textView)
    TextView gps_state_textView;
    @BindView(R.id.view_detection_aerial_textView)
    TextView view_detection_aerial_textView;
    @BindView(R.id.coordinates_linearLayout)
    LinearLayout coordinates_linearLayout;
    @BindView(R.id.latitude_textView)
    TextView latitude_textView;
    @BindView(R.id.longitude_textView)
    TextView longitude_textView;

    private Handler handlerMessage;
    private TableMergeListAdapter tableMergeListAdapter;
    private boolean previousScanning;
    private boolean isHold; // This can change during scanning
    private boolean isRecord; // This can change during scanning
    private int frequencyRange;
    private final byte[] audioOption = {(byte) 0x5A, 0, 0};
    private DialogFragment audioOptions;
    private MobileDefaults mobileDefaults;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    new Handler().postDelayed(() -> {
                        setNewFrequency(value);
                    }, ValueCodes.WAITING_PERIOD);
                } else if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) { // Gets the modified frequency table number
                    frequency_table_number_aerial_textView.setText(String.valueOf(value));
                    setTemporary(ValueCodes.TABLE_NUMBER_CODE);
                } else if (ValueCodes.SCAN_RATE_MOBILE_CODE == result.getResultCode()) { // Gets the modified scan rate
                    scan_rate_seconds_aerial_textView.setText(String.valueOf(value * 0.1));
                    setTemporary(ValueCodes.SCAN_RATE_MOBILE_CODE);
                }
            });

    private void setTemporary(int type) {
        int info = (mobileDefaults.gpsOn ? 1 : 0) << 7;
        info = info | ((mobileDefaults.autoRecordOn ? 1 : 0) << 6);
        byte[] b = new byte[]{(byte) 0x6F, (byte) mobileDefaults.tableNumber, (byte) info, (byte) ((int) (mobileDefaults.scanRate * 10))};
        switch (type) {
            case ValueCodes.TABLE_NUMBER_CODE:
                b[1] = (byte) Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
                mobileDefaults.tableNumber = Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
                break;
            case ValueCodes.SCAN_RATE_MOBILE_CODE:
                b[3] = (byte) (Float.parseFloat(scan_rate_seconds_aerial_textView.getText().toString()) * 10);
                mobileDefaults.scanRate = Double.parseDouble(scan_rate_seconds_aerial_textView.getText().toString());
                break;
            case ValueCodes.GPS_CODE:
                b[2] = gps_switch.isChecked() ? (byte) (Integer.parseInt(Converters.getDecimalValue(b[2]) + 128))
                        : (byte) (Integer.parseInt(Converters.getDecimalValue(b[2])) - 128);
                mobileDefaults.gpsOn = gps_switch.isChecked();
                break;
            case ValueCodes.AUTO_RECORD_CODE:
                b[2] = aerial_auto_record_switch.isChecked() ? (byte) (Integer.parseInt(Converters.getDecimalValue(b[2])) + 64)
                        : (byte) (Integer.parseInt(Converters.getDecimalValue(b[2])) - 64);
                mobileDefaults.autoRecordOn = aerial_auto_record_switch.isChecked();
                break;
        }
        boolean result = TransferBleData.writeDefaults(true, b);
        if (!result) downloadData(mobileDefaults.originalBytes);
        gps_switch.setEnabled(true);
        aerial_auto_record_switch.setEnabled(true);
    }

    private void setStartScan() {
        byte[] b = setCalendar();
        b[0] = (byte) 0x82;
        b[7] = (byte) mobileDefaults.tableNumber;
        isScanning = TransferBleData.writeStartScan(ValueCodes.MOBILE_DEFAULTS, b);
        if (isScanning) {
            removeHold();
            isRecord = mobileDefaults.autoRecordOn;
            if (isRecord) setRecord(); else removeRecord();
            if (mobileDefaults.gpsOn) setGpsSearching(); else setGpsOff();
            setVisibility("scanning");
        }
    }

    private void setStopScan() {
        boolean result = TransferBleData.writeStopScan(ValueCodes.MOBILE_DEFAULTS);
        if (result) {
            clear();
            isScanning = false;
            if (previousScanning) {
                new Handler().postDelayed(() -> {
                    TransferBleData.readDefaults(true);
                }, ValueCodes.WAITING_PERIOD);
                previousScanning = false;
            }
            setVisibility("overview");
            animationDrawable.stop();
        }
    }

    private void setHoldScan() {
        boolean result = TransferBleData.setHold(isHold);
        if (result) {
            isHold = !isHold;
            if (isHold) setHold();
            else removeHold();
        }
    }

    /**
     * Writes a value for add a frequency in the table.
     */
    private void setNewFrequency(int newFrequency) {
        byte[] b = new byte[] {(byte) 0x5D, (byte) ((newFrequency - baseFrequency) / 256),
                (byte) ((newFrequency - baseFrequency) % 256)};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
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
        if (result)
            manageMessage(R.string.lb_frequency_deleted);
    }

    /**
     * Writes a value for merge other tables to the selected table.
     */
    private void setMergeTable() {
        byte[] b = new byte[]{(byte) 0x8A, (byte) tableMergeListAdapter.getTableNumber()};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
            isHold = false;
            removeHold();
            manageMessage(R.string.lb_tables_merged);
        }
    }

    private void setDecreaseOrIncrease(boolean isDecrease) {
        boolean result = TransferBleData.writeDecreaseIncrease(isDecrease);
    }

    private void setRecordScan() {
        boolean result = TransferBleData.writeRecord(!isRecord, false);
        if (result) {
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
            id_audio_textView.setText(audioDescription);
        }
    }

    @OnClick(R.id.frequency_table_number_aerial_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.tableNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_aerial_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_MOBILE_CODE);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.scanRate);
        launcher.launch(intent);
    }

    @OnCheckedChanged(R.id.gps_switch)
    public void onCheckedChangedGps(CompoundButton button, boolean isChecked) {
        if (parameter.isEmpty()) {
            gps_switch.setEnabled(false);
            setTemporary(ValueCodes.GPS_CODE);
        }
    }

    @OnCheckedChanged(R.id.aerial_auto_record_switch)
    public void onCheckedChangedAutoRecord(CompoundButton button, boolean isChecked) {
        if (parameter.isEmpty()) {
            aerial_auto_record_switch.setEnabled(false);
            setTemporary(ValueCodes.AUTO_RECORD_CODE);
        }
    }

    @OnClick(R.id.start_aerial_button)
    public void onClickStartAerial(View v) {
        setNotificationLog();
        setStartScan();
    }

    @OnClick(R.id.hold_aerial_button)
    public void onClickHoldAerial(View v) {
        setHoldScan();
    }

    @OnClick(R.id.decrease_imageView)
    public void onClickDecrease(View v) {
        int currentFrequency = Converters.getFrequencyNumber(frequency_aerial_textView.getText().toString());
        if (currentFrequency > baseFrequency)
            setDecreaseOrIncrease(true);
    }

    @OnClick(R.id.increase_imageView)
    public void onClickIncrease(View v) {
        int currentFrequency = Converters.getFrequencyNumber(frequency_aerial_textView.getText().toString());
        if (currentFrequency < frequencyRange)
            setDecreaseOrIncrease(false);
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
        intent.putExtra(ValueCodes.TITLE, getString(R.string.lb_add_frequency_scan));
        intent.putExtra(ValueCodes.POSITION, -1);
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.delete_frequency_scan_button)
    public void onClickDeleteFrequencyScan(View v) {
        setDeleteFrequency();
    }

    @OnClick(R.id.merge_table_scan_button)
    public void onClickMergeTableScan(View v) {
        setVisibility("mergeTable");
        if (tableMergeListAdapter == null)
            TransferBleData.readTables();
    }

    @OnClick(R.id.merge_tables_button)
    public void onClickMergeTables(View v) {
        setMergeTable();
    }

    @OnClick(R.id.record_data_button)
    public void onClickRecordData(View v) {
        setRecordScan();
    }

    @OnClick(R.id.left_imageView)
    public void onClickLeft(View v) {
        TransferBleData.writeLeftRight(true);
    }

    @OnClick(R.id.right_imageView)
    public void onClickRight(View v) {
        TransferBleData.writeLeftRight(false);
    }

    @OnClick(R.id.edit_audio_textView)
    public void onClickEditAudio(View v) {
        getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                String state = bundle.getString(ValueCodes.PARAMETER);
                if (state != null && state.equals(ValueCodes.AUDIO)) {
                    audioOption[0] = bundle.getByte(ValueCodes.AUDIO);
                    audioOption[1] = (byte) bundle.getInt(ValueCodes.VALUE);
                    audioOption[2] = bundle.getByte(ValueCodes.BACKGROUND);
                    setAudio();
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
        contentViewId = R.layout.activity_vhf_mobile_scan;
        title = getString(R.string.aerial_scanning);
        super.onCreate(savedInstanceState);

        initializeCallback();
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        isHold = false;
        handlerMessage = new Handler();
        byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
        if (isScanning) { // The device is already scanning
            previousScanning = true;
            parameter = ValueCodes.CONTINUE_LOG;

            mobileDefaults = new MobileDefaults();
            int currentFrequency = (Integer.parseInt(Converters.getDecimalValue(data[16])) * 256)
                    + Integer.parseInt(Converters.getDecimalValue(data[17])) + baseFrequency;
            int currentIndex = (Integer.parseInt(Converters.getDecimalValue(data[7])) * 256)
                    + Integer.parseInt(Converters.getDecimalValue(data[8]));
            mobileDefaults.autoRecordOn = isRecord = (Integer.parseInt(Converters.getDecimalValue(data[15])) >> 6 & 1) == 1;
            mobileDefaults.gpsOn = (Integer.parseInt(Converters.getDecimalValue(data[15])) >> 7 & 1) == 1;
            isHold = Converters.getHexValue(data[1]).equals("81");
            frequency_aerial_textView.setText(Converters.getFrequency(currentFrequency));
            table_index_aerial_textView.setText(String.valueOf(currentIndex));
            if (isHold) setHold(); else removeHold();
            if (isRecord) setRecord(); else removeRecord();
            if (mobileDefaults.gpsOn) setGpsSearching(); else setGpsOff();
            scanState(data);
            setVisibility("scanning");
        } else { // Gets aerial defaults data
            downloadData(data);
            previousScanning = false;
            setVisibility("overview");
        }
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                switch (parameter) {
                    case ValueCodes.MOBILE_DEFAULTS: // Gets aerial defaults data
                        TransferBleData.readDefaults(true);
                        break;
                    case ValueCodes.CONTINUE_LOG:
                        setNotificationLogScanning();
                        break;
                }
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                switch (Converters.getHexValue(packet[0])) {
                    case "88": // Battery
                        setBatteryPercent(packet);
                        break;
                    case "56": // Sd Card
                        setSdCardStatus(packet);
                        break;
                    case "7A": // Get tables
                        downloadTables(packet);
                        break;
                    case "6D": // Get aerial defaults data
                        downloadData(packet);
                        break;
                    default: // Receive the data
                        setCurrentLog(packet);
                        break;
                }
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!isScanning) {
                Intent intent = new Intent(this, ScanningActivity.class);
                intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES);
                startActivity(intent);
                finish();
            } else {
                if (edit_table_linearLayout.getVisibility() == View.VISIBLE) {
                    setVisibility("scanning");
                } else if (merge_tables_linearLayout.getVisibility() == View.VISIBLE) {
                    setVisibility("editTable");
                    changeAllCheckBox();
                } else {
                    setStopScan();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void updateVisibility(int visibility) {
        super.updateVisibility(visibility);
        audio_linearLayout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
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
        ArrayList<Integer> frequencies = new ArrayList<>();
        ArrayList<Integer> tables = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            if (data[i] != 0 && !Converters.getHexValue(data[i]).equals("FF")) {
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
        mobileDefaults = new MobileDefaults(data);
        if (mobileDefaults.tableNumber == 0) { // There are no tables with frequencies to scan
            frequency_table_number_aerial_textView.setText(R.string.lb_none);
            start_aerial_button.setEnabled(false);
            start_aerial_button.setAlpha((float) 0.6);
        } else { // Shows the table to be scanned
            frequency_table_number_aerial_textView.setText(String.valueOf(mobileDefaults.tableNumber));
            start_aerial_button.setEnabled(true);
            start_aerial_button.setAlpha((float) 1);
        }
        scan_rate_seconds_aerial_textView.setText(String.valueOf(mobileDefaults.scanRate));
        gps_switch.setChecked(mobileDefaults.gpsOn);
        aerial_auto_record_switch.setChecked(mobileDefaults.autoRecordOn);
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
        record_data_button.setText(R.string.lb_stop_recording);
        record_data_button.setBackground(ContextCompat.getDrawable(this, button_delete));
    }

    private void removeRecord() {
        record_data_button.setText(R.string.lb_record_data);
        record_data_button.setBackground(ContextCompat.getDrawable(this, button_primary));
    }

    private void setGpsOff() {
        gps_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_off));
        gps_state_textView.setText(R.string.lb_off_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
    }

    private void setGpsSearching() {
        gps_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_searching));
        gps_state_textView.setText(R.string.lb_searching_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
        latitude_textView.setText("");
        longitude_textView.setText("");
    }

    private void setGpsFailed() {
        gps_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_failed));
        gps_state_textView.setText(R.string.lb_failed_gps);
        coordinates_linearLayout.setVisibility(View.GONE);
    }

    private void setGpsValid() {
        gps_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_gps_valid));
        gps_state_textView.setText(R.string.lb_valid_gps);
        coordinates_linearLayout.setVisibility(View.VISIBLE);
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
            case "A1":
                logGps(data);
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
            case "E1":
            case "E2":
            case "EA": //Non Coded
                int signalStrength = Integer.parseInt(Converters.getDecimalValue(data[4]));
                int period = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
                if (Converters.getHexValue(detectionType).equals("08")) // Non Coded Fixed
                    logScanNonCodedFixed(data[0], period, signalStrength);
                else if (Converters.getHexValue(detectionType).equals("07")) // Non Coded Variable
                    scanNonCodedVariable(period, signalStrength);
                break;
        }
    }

    private void scanState(byte[] data) {
        int maxIndex = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
        max_index_aerial_textView.setText("Table Index (" + maxIndex + " Total)");
        table_total_aerial_textView.setText(String.valueOf(maxIndex));
        detectionType = data[18];
        scanDetailListAdapter = new ScanDetailListAdapter(this, Converters.getHexValue(detectionType).equals("09"));
        item_recyclerView.setAdapter(scanDetailListAdapter);
        item_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        int visibility = Converters.getHexValue(detectionType).equals("09") ? View.GONE : View.VISIBLE;
        updateVisibility(visibility);
        if (!Converters.getHexValue(detectionType).equals("09")) {
            initializeDetectionFilter(data);
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
        scanCoded(code, signalStrength, mortality);
    }

    private void logScanNonCodedFixed(byte format, int period, int signalStrength) {
        int type = Integer.parseInt(Converters.getHexValue(format).replace("E", ""));
        scanNonCodedFixed(period, signalStrength, type);
    }

    private void logGps(byte[] data) {
        String[] coordinates = Converters.getGpsData(data);
        latitude_textView.setText(coordinates[0]);
        longitude_textView.setText(coordinates[1]);
    }

    @Override
    protected void clear() {
        table_index_aerial_textView.setText("");
        frequency_aerial_textView.setText("");
        super.clear();
    }
}