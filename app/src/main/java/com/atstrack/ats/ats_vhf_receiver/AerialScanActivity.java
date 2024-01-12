package com.atstrack.ats.ats_vhf_receiver;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableRow;
import android.widget.TextView;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.catskill_white;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ghost;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.limed_spruce;
import static com.atstrack.ats.ats_vhf_receiver.R.color.mountain_meadow;
import static com.atstrack.ats.ats_vhf_receiver.R.color.slate_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.color.tall_poppy;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.border;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_decrease_light;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_delete;
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
    @BindView(R.id.device_name_textView)
    TextView device_name_textView;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
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
    @BindView(R.id.table_index_aerial_textView)
    TextView table_index_aerial_textView;
    @BindView(R.id.frequency_aerial_textView)
    TextView frequency_aerial_textView;
    @BindView(R.id.scan_details_linearLayout)
    LinearLayout scan_details_linearLayout;
    @BindView(R.id.code_textView)
    TextView code_textView;
    @BindView(R.id.period_textView)
    TextView period_textView;
    @BindView(R.id.pulse_rate_textView)
    TextView pulse_rate_textView;
    @BindView(R.id.detections_textView)
    TextView detections_textView;
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
    @BindView(R.id.frequency_edit_linearLayout)
    LinearLayout frequency_edit_linearLayout;
    @BindView(R.id.merge_tables_linearLayout)
    LinearLayout merge_tables_linearLayout;
    @BindView(R.id.frequency_textView)
    TextView frequency_textView;
    @BindView(R.id.line_frequency_view)
    View line_frequency_view;
    @BindView(R.id.edit_frequency_message_textView)
    TextView edit_frequency_message_textView;
    @BindView(R.id.number_buttons_linearLayout)
    LinearLayout number_buttons_linearLayout;
    @BindView(R.id.select_tables_linearLayout)
    LinearLayout select_tables_linearLayout;
    @BindView(R.id.merge_tables_button)
    Button merge_tables_button;
    @BindView(R.id.save_changes_button)
    Button save_changes_button;

    private final static String TAG = AerialScanActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private static final int WAITING_PERIOD = 1000;

    private boolean isScanning;
    private boolean previousScanning;
    private boolean isHold;
    private int numberOfSelected;
    private Handler handlerMessage;

    private AnimationDrawable animationDrawable;
    private LinearLayout linearLayoutBaseFrequency;
    private Button buttonBaseFrequency;
    private LinearLayout linearLayoutFrequency;
    private TextView textViewFrequency;
    private CheckBox checkBoxFrequency;

    private int baseFrequency;
    private int frequencyRange;
    private byte detectionType;
    private int newFrequency;
    private int selectedTable;
    private int autoRecord;
    private int code;
    private int detections;
    private int mort;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int seconds;

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
                    Log.i(TAG, "BROADCAST, " + parameter + ": " + Converters.getHexValue(packet));
                    switch (parameter) {
                        case "aerial": // Gets aerial defaults data
                            downloadData(packet);
                            break;
                        case "sendLog": // Receives the data
                            setCurrentLog(packet);
                            break;
                        case "tables":
                            downloadTables(packet);
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
                Log.i(TAG, "BROADCAST RECEIVER 1: " + action);
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
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    /**
     * Change the period while editing the pulse rate.
     */
    private TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (frequency_textView.getText().toString().length() == 6) {
                save_changes_button.setEnabled(true);
                save_changes_button.setAlpha(1);
                line_frequency_view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), ghost));
                edit_frequency_message_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
            } else {
                save_changes_button.setEnabled(false);
                save_changes_button.setAlpha((float) 0.6);
                line_frequency_view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), tall_poppy));
                edit_frequency_message_textView.setTextColor(ContextCompat.getColor(getBaseContext(), tall_poppy));
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

    private static IntentFilter makeGattUpdateIntentFilterWrite() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND);
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
        year = YY % 100;

        byte[] b = new byte[] {
                (byte) 0x82, (byte) (YY % 100), (byte) MM, (byte) DD, (byte) hh, (byte) mm, (byte) ss, (byte) selectedTable};

        Log.i(TAG, "On Click Start Aerial");
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        isScanning = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (isScanning) {
            parameterWrite = "";
            title_toolbar.setText(R.string.lb_aerial_scanning);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
            aerial_result_linearLayout.setVisibility(View.VISIBLE);

            state_view.setBackgroundResource(R.drawable.scanning_animation);
            animationDrawable = (AnimationDrawable) state_view.getBackground();
            animationDrawable.start();
            frequency_aerial_textView.setText("");

            device_name_textView.setText(receiverInformation.getDeviceName().replace("Not scanning", "Scanning, mobile"));
        }
    }

    /**
     * Enables notification for receive the data.
     * Service name: Screen.
     * Characteristic name: SendLog.
     */
    private void onClickLog() {
        parameterWrite = "startAerial";
        Log.i(TAG, "On Click Log Aerial");

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
        Log.i(TAG, "START STOP AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            clear();
            isScanning = false;
            animationDrawable.stop();
            state_view.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
            aerial_result_linearLayout.setVisibility(View.GONE);
            ready_aerial_scan_LinearLayout.setVisibility(View.VISIBLE);
            title_toolbar.setText(R.string.aerial_scanning);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

            device_name_textView.setText(receiverInformation.getDeviceName());

            if (previousScanning) {
                parameter = "aerial";
                new Handler().postDelayed(() -> {
                    mBluetoothLeService.discovering();
                }, WAITING_PERIOD);
            } else {
                parameter = "";
            }
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
            hold_aerial_button.setText(R.string.lb_release);
            hold_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lock));
            frequency_aerial_textView.setTextColor(ContextCompat.getColor(this, mountain_meadow));
            edit_table_textView.setTextColor(ContextCompat.getColor(this, limed_spruce));
            edit_table_textView.setEnabled(true);
            decrease_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease));
            increase_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase));
            decrease_imageView.setEnabled(true);
            increase_imageView.setEnabled(true);
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
            hold_aerial_button.setText(R.string.lb_hold);
            hold_aerial_imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_unlock));
            frequency_aerial_textView.setTextColor(ContextCompat.getColor(this, ebony_clay));
            edit_table_textView.setTextColor(ContextCompat.getColor(this, ghost));
            edit_table_textView.setEnabled(false);
            decrease_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease_light));
            increase_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase_light));
            decrease_imageView.setEnabled(false);
            increase_imageView.setEnabled(false);
        }
    }

    private void onClickLogScanning() {
        parameter = "sendLog";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCREEN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_SEND_LOG;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);
    }

    /**
     * Requests a read for get the number of frequencies from each table and display it.
     * Service name: StoredData.
     * Characteristic name: FreqTable.
     */
    private void onClickTables() {
        parameter = "tables";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    private void onClickAddFrequency() {
        byte[] b = new byte[] {(byte) 0x5D, (byte) ((newFrequency - baseFrequency) / 256),
                (byte) ((newFrequency - baseFrequency) % 256)};
        Log.i(TAG, "ADD FREQUENCY AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            manageMessage(R.string.lb_frequency_added);
        }
    }

    private void onClickDeleteFrequency() {
        byte[] b = new byte[] {(byte) 0x5C,
                (byte) (Integer.parseInt(table_index_aerial_textView.getText().toString()) / 256),
                (byte) (Integer.parseInt(table_index_aerial_textView.getText().toString()) % 256)};
        Log.i(TAG, "DELETE FREQUENCY AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            manageMessage(R.string.lb_frequency_deleted);
        }
    }

    private void onClickMerge() {
        if (numberOfSelected > 0) {
            int index = 0;
            while (index < select_tables_linearLayout.getChildCount()) {
                LinearLayout linearLayout = (LinearLayout) select_tables_linearLayout.getChildAt(index);
                CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
                if (checkBox.isChecked()) {
                    TextView textView = (TextView) linearLayout.getChildAt(1);
                    int tableNumber = Integer.parseInt(textView.getText().toString().split(" ")[1]);

                    byte[] b = new byte[]{(byte) 0x8A, (byte) tableNumber};
                    Log.i(TAG, "MERGE AERIAL " + tableNumber);

                    UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
                    UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
                    boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

                    if (result) {
                        checkBox.setChecked(false);
                        numberOfSelected--;

                        if (numberOfSelected == 0) {
                            manageMessage(R.string.lb_tables_merged);
                            return;
                        }
                    }
                    new Handler().postDelayed(() -> {
                        mBluetoothLeService.discoveringSecond();
                    }, WAITING_PERIOD);
                    return;
                }
                index++;
            }
        }
    }

    private void onClickDecrease() {
        byte[] b = new byte[] {(byte) 0x5E,
                (byte) (Integer.parseInt(frequency_aerial_textView.getText().toString()) / 256),
                (byte) (Integer.parseInt(frequency_aerial_textView.getText().toString()) % 256)};
        Log.i(TAG, "DECREASE FREQUENCY AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (!result) {
            int newFrequency = Integer.parseInt(frequency_aerial_textView.getText().toString()) + 1;
            frequency_aerial_textView.setText(String.valueOf(newFrequency));
        }
    }

    private void onClickIncrease() {
        byte[] b = new byte[] {(byte) 0x5F,
                (byte) (Integer.parseInt(frequency_aerial_textView.getText().toString()) / 256),
                (byte) (Integer.parseInt(frequency_aerial_textView.getText().toString()) % 256)};
        Log.i(TAG, "INCREASE FREQUENCY AERIAL");

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_SCAN;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_AERIAL;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (!result) {
            int newFrequency = Integer.parseInt(frequency_aerial_textView.getText().toString()) - 1;
            frequency_aerial_textView.setText(String.valueOf(newFrequency));
        }
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
        int newFrequency = Integer.parseInt(frequency_aerial_textView.getText().toString()) - 1;
        frequency_aerial_textView.setText(String.valueOf(newFrequency));
        parameterWrite = "decrease";
        mBluetoothLeService.discoveringSecond();

        if (newFrequency == baseFrequency) {
            decrease_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease_light));
            decrease_imageView.setEnabled(false);
        } else if (newFrequency == frequencyRange -1) {
            increase_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase));
            increase_imageView.setEnabled(true);
        }
    }

    @OnClick(R.id.increase_imageView)
    public void onClickIncrease(View v) {
        int newFrequency = Integer.parseInt(frequency_aerial_textView.getText().toString()) + 1;
        frequency_aerial_textView.setText(String.valueOf(newFrequency));
        parameterWrite = "increase";
        mBluetoothLeService.discoveringSecond();

        if (newFrequency == baseFrequency + 1) {
            decrease_imageView.setBackground(ContextCompat.getDrawable(this, ic_decrease));
            decrease_imageView.setEnabled(true);
        } else if (newFrequency == frequencyRange) {
            increase_imageView.setBackground(ContextCompat.getDrawable(this, ic_increase_light));
            increase_imageView.setEnabled(false);
        }
    }

    @OnClick(R.id.edit_table_textView)
    public void onClickEditTable(View v) {
        setVisibility("editTable");
    }

    @OnClick(R.id.add_frequency_scan_button)
    public void onClickAddFrequencyScan(View v) {
        setVisibility("editFrequency");
        title_toolbar.setText(R.string.lb_add_frequency_scan);
        frequency_textView.setText(R.string.lb_enter_frequency_digits);
        frequency_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
        line_frequency_view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), ghost));
        edit_frequency_message_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
        save_changes_button.setEnabled(false);
        save_changes_button.setAlpha((float) 0.6);
    }

    @OnClick(R.id.delete_frequency_scan_button)
    public void onClickDeleteFrequencyScan(View v) {
        parameterWrite = "deleteFrequency";
        mBluetoothLeService.discoveringSecond();
    }

    @OnClick(R.id.merge_table_scan_button)
    public void onClickMergeTableScan(View v) {
        setVisibility("mergeTable");
        title_toolbar.setText(R.string.lb_merge_tables);
        if (select_tables_linearLayout.getChildCount() == 0) {
            parameterWrite = "tables";
            mBluetoothLeService.discoveringSecond();
        }
    }

    @OnClick(R.id.save_changes_button)
    public void onClickSaveChanges(View v) {
        newFrequency = (frequency_textView.getText().toString().isEmpty()) ? 0 : Integer.parseInt(frequency_textView.getText().toString());
        if (newFrequency >= baseFrequency && newFrequency <= frequencyRange) {
            parameterWrite = "addFrequency";
            mBluetoothLeService.discoveringSecond();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Invalid Format or Values");
            builder.setMessage("Please enter valid frequency values.");
            builder.setPositiveButton("Ok", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(catskill_white)));
        }
    }

    @OnClick(R.id.merge_tables_button)
    public void onClickMergeTables(View v) {
        parameterWrite = "merge";
        mBluetoothLeService.discoveringSecond();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerial_scan);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.aerial_scanning);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        isScanning = getIntent().getExtras().getBoolean("scanning");
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        baseFrequency = sharedPreferences.getInt("BaseFrequency", 0) * 1000;
        detectionType = (byte) sharedPreferences.getInt("DetectionType", 0);
        int range = sharedPreferences.getInt("Range", 0);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;

        frequency_textView.addTextChangedListener(textChangedListener);
        String message = "Frequency range is " + baseFrequency + " to " + frequencyRange;
        edit_frequency_message_textView.setText(message);

        int visibility = (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        detections_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);
        save_changes_button.setText(R.string.lb_add_frequency);

        isHold = false;
        numberOfSelected = 0;
        handlerMessage = new Handler();

        if (isScanning) { // The device is already scanning
            previousScanning = true;
            parameter = "sendLogScanning";
            year = getIntent().getExtras().getInt("year");
            month = getIntent().getExtras().getInt("month");
            day = getIntent().getExtras().getInt("day");
            hour = getIntent().getExtras().getInt("hour");
            minute = getIntent().getExtras().getInt("minute");
            seconds = getIntent().getExtras().getInt("seconds");

            setVisibility("scanning");
            title_toolbar.setText(R.string.lb_aerial_scanning);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

            state_view.setBackgroundResource(R.drawable.scanning_animation);
            animationDrawable = (AnimationDrawable) state_view.getBackground();
            animationDrawable.start();
        } else { // Gets aerial defaults or temporary data
            boolean isTemporary = getIntent().getExtras().getBoolean("temporary");
            if (isTemporary) {
                String scanTime = getIntent().getExtras().getString("scanTime");
                String tableNumber = getIntent().getExtras().getString("tableNumber");
                String gps = getIntent().getExtras().getString("gps");
                String autoRecord = getIntent().getExtras().getString("autoRecord");
                scan_rate_aerial_textView.setText(scanTime);
                selected_frequency_aerial_textView.setText(tableNumber);
                gps_aerial_textView.setText(gps);
                auto_record_aerial_textView.setText(autoRecord);
            } else {
                parameter = "aerial";
            }
            previousScanning = false;
            setVisibility("overview");
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        createNumberButtons(range);
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
                    title_toolbar.setText(R.string.aerial_scanning);
                } else if (frequency_edit_linearLayout.getVisibility() == View.VISIBLE) {
                    setVisibility("editTable");
                    title_toolbar.setText(R.string.aerial_scanning);
                } else if (merge_tables_linearLayout.getVisibility() == View.VISIBLE) {
                    setVisibility("editTable");
                    title_toolbar.setText(R.string.aerial_scanning);
                    for (int i = 0; i < select_tables_linearLayout.getChildCount(); i++) {
                        LinearLayout linearLayout = (LinearLayout) select_tables_linearLayout.getChildAt(i);
                        CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
                        if (checkBox.isChecked())
                            checkBox.setChecked(false);
                    }
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
                frequency_edit_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                break;
            case "scanning":
                ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
                aerial_result_linearLayout.setVisibility(View.VISIBLE);
                edit_table_linearLayout.setVisibility(View.GONE);
                frequency_edit_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                break;
            case "editTable":
                ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
                aerial_result_linearLayout.setVisibility(View.GONE);
                edit_table_linearLayout.setVisibility(View.VISIBLE);
                frequency_edit_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                break;
            case "editFrequency":
                ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
                aerial_result_linearLayout.setVisibility(View.GONE);
                edit_table_linearLayout.setVisibility(View.GONE);
                frequency_edit_linearLayout.setVisibility(View.VISIBLE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                break;
            case "mergeTable":
                ready_aerial_scan_LinearLayout.setVisibility(View.GONE);
                aerial_result_linearLayout.setVisibility(View.GONE);
                edit_table_linearLayout.setVisibility(View.GONE);
                frequency_edit_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void createNumberButtons(int range) {
        int baseNumber = baseFrequency / 1000;
        for (int i = 0; i < range / 4; i++) {
            newBaseLinearLayout();
            for (int j = 0; j < 4; j++) {
                newBaseButton(baseNumber);
                int finalBaseNumber = baseNumber;
                buttonBaseFrequency.setOnClickListener(view -> {
                    if (frequency_textView.getText().toString().isEmpty() || frequency_textView.getText().toString().length() > 6) {
                        frequency_textView.setText(String.valueOf(finalBaseNumber));
                        frequency_textView.setTextColor(ContextCompat.getColor(getBaseContext(), ebony_clay));
                    }
                });
                linearLayoutBaseFrequency.addView(buttonBaseFrequency);
                baseNumber++;
            }
            number_buttons_linearLayout.addView(linearLayoutBaseFrequency);
        }

        Space space = new Space(this);
        space.setLayoutParams(newLinearLayoutParams());
        number_buttons_linearLayout.addView(space);

        int number = 1;
        for (int i = 0; i < 3; i++) {
            newBaseLinearLayout();
            for (int j = 0; j < 4; j++) {
                if (number == 10) {
                    Space spaceBaseFrequency = new Space(this);
                    spaceBaseFrequency.setLayoutParams(newButtonParams());
                    linearLayoutBaseFrequency.addView(spaceBaseFrequency);
                }else if (number == 11) {
                    ImageView imageViewBaseFrequency = new ImageView(this);
                    imageViewBaseFrequency.setBackground(ContextCompat.getDrawable(this, button_delete));
                    imageViewBaseFrequency.setImageDrawable(ContextCompat.getDrawable(this, ic_delete));
                    imageViewBaseFrequency.setLayoutParams(newButtonDeleteParams());
                    imageViewBaseFrequency.setPadding(50, 0, 50, 0);
                    imageViewBaseFrequency.setOnClickListener(view -> {
                        if (!frequency_textView.getText().toString().isEmpty()) {
                            String previous = frequency_textView.getText().toString();
                            frequency_textView.setText(previous.substring(0, previous.length() - 1));
                        }
                    });
                    linearLayoutBaseFrequency.addView(imageViewBaseFrequency);
                } else {
                    newBaseButton(number);
                    int finalNumber = number;
                    buttonBaseFrequency.setOnClickListener(view -> {
                        if (frequency_textView.getText().toString().length() >= 3 && frequency_textView.getText().toString().length() < 6) {
                            String previous = frequency_textView.getText().toString();
                            frequency_textView.setText(previous + finalNumber);
                        }
                    });
                    linearLayoutBaseFrequency.addView(buttonBaseFrequency);
                }
                if (number == 9) number = 0;
                else if (number == 0) number = 10;
                else number++;
            }
            number_buttons_linearLayout.addView(linearLayoutBaseFrequency);
        }
    }

    private void newBaseLinearLayout() {
        linearLayoutBaseFrequency = new LinearLayout(this);
        linearLayoutBaseFrequency.setLayoutParams(newLinearLayoutParams());
        linearLayoutBaseFrequency.setOrientation(LinearLayout.HORIZONTAL);
    }

    private void newBaseButton(int baseNumber) {
        buttonBaseFrequency = new Button(this);
        buttonBaseFrequency.setBackground(ContextCompat.getDrawable(this, border));
        buttonBaseFrequency.setTextSize(16);
        buttonBaseFrequency.setTextColor(ContextCompat.getColor(this, ebony_clay));
        buttonBaseFrequency.setText(String.valueOf(baseNumber));
        buttonBaseFrequency.setLayoutParams(newButtonParams());
    }

    /**
     * Sets the margins for the LinearLayout.
     *
     * @return Returns a LayoutParams with the customize margins.
     */
    private LinearLayout.LayoutParams newLinearLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(0, 0, 0, 32);
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        return params;
    }

    private LinearLayout.LayoutParams newButtonParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(16, 0, 16, 0);
        params.weight = 1;
        return params;
    }

    private LinearLayout.LayoutParams newButtonDeleteParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(16, 0, 16, 0);
        params.height = GridLayout.LayoutParams.MATCH_PARENT;
        params.weight = 1;
        return params;
    }

    private void manageMessage(int idStringMessage) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.frequency_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        TextView state_message_textView = view.findViewById(R.id.state_message_textView);
        state_message_textView.setText(idStringMessage);

        dialog.setView(view);
        dialog.show();

        int MESSAGE_PERIOD = 3000;
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
        parameter = "sendLog";
        for (int i = 1; i <= 12; i++) {
            createNewTableCell(i, Integer.parseInt(Converters.getDecimalValue(data[i])));
        }
    }

    private void createNewTableCell(int index, int frequencies) {
        newTableCell();
        textViewFrequency.setText("Table " + index + " (" + frequencies + " frequencies)");
        checkBoxFrequency.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                merge_tables_button.setEnabled(true);
                merge_tables_button.setAlpha((float) 1);
                numberOfSelected++;
            } else {
                numberOfSelected--;
                if (numberOfSelected == 0) {
                    merge_tables_button.setEnabled(false);
                    merge_tables_button.setAlpha((float) 0.6);
                }
            }
        });
        linearLayoutFrequency.addView(checkBoxFrequency);
        linearLayoutFrequency.addView(textViewFrequency);
        select_tables_linearLayout.addView(linearLayoutFrequency);
    }

    private void newTableCell() {
        linearLayoutFrequency = new LinearLayout(this);
        linearLayoutFrequency.setBackgroundColor(ContextCompat.getColor(this, catskill_white));
        linearLayoutFrequency.setLayoutParams(newLinearLayoutParams());
        linearLayoutFrequency.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutFrequency.setElevation(4);
        linearLayoutFrequency.setGravity(Gravity.CENTER);
        linearLayoutFrequency.setPadding(32, 32, 32, 32);

        checkBoxFrequency = new CheckBox(this);

        textViewFrequency = new TextView(this);
        textViewFrequency.setTextSize(16);
        textViewFrequency.setTextColor(ContextCompat.getColor(this, limed_spruce));
        textViewFrequency.setLayoutParams(newTextViewParams());
    }

    private ViewGroup.LayoutParams newTextViewParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;
        return params;
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
                //frequency_empty_textView.setVisibility(View.VISIBLE);
                start_aerial_button.setEnabled(false);
                start_aerial_button.setAlpha((float) 0.6);
            } else { // Shows the table to be scanned
                selected_frequency_aerial_textView.setText(Converters.getDecimalValue(data[1]));
                //frequency_empty_textView.setVisibility(View.GONE);
                start_aerial_button.setEnabled(true);
                start_aerial_button.setAlpha((float) 1);
            }
            selectedTable = Integer.parseInt(Converters.getDecimalValue(data[1]));
            float scanTime = (float) (Integer.parseInt(Converters.getDecimalValue(data[3])) * 0.1);
            scan_rate_aerial_textView.setText(String.valueOf(scanTime));
            int gps = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 7 & 1;
            gps_aerial_textView.setText((gps == 1) ? R.string.lb_on : R.string.lb_off);
            autoRecord = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1;
            auto_record_aerial_textView.setText((autoRecord == 1) ? R.string.lb_on : R.string.lb_off);
        }
    }

    /**
     * With the received packet, gets the data of scanning.
     *
     * @param data The received packet.
     */
    private void setCurrentLog(byte[] data) {
        switch (Converters.getHexValue(data[0])) {
            case "82":
                startScanAerialFirstPart(data);
                break;
            case "85":
                startScanAerialSecondPart(data);
                break;
            case "F0":
                logScanHeader(data);
                break;
            case "F1":
                logScanFix(data);
                break;
            /*case "F2":
                logScanFixConsolidated(data);
                break;*/
            default: //E1 and E2
                logScanData(data);
                break;
        }
    }

    /**
     * Process the aerial data, first part of packet.
     *
     * @param data The aerial data packet.
     */
    private void startScanAerialFirstPart(byte[] data) {
        selectedTable = Integer.parseInt(Converters.getDecimalValue(data[1]));
        autoRecord = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1;
        year = Integer.parseInt(Converters.getDecimalValue(data[6]));

        byte detectionType = (byte) (Integer.parseInt(Converters.getDecimalValue(data[4])) % 16);
        int visibility = (Converters.getHexValue(detectionType).equals("11") || Converters.getHexValue(detectionType).equals("12")) ? View.GONE : View.VISIBLE;
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);
    }

    /**
     * Process the aerial data, second part of packet.
     *
     * @param data The aerial data packet.
     */
    private void startScanAerialSecondPart(byte[] data) {
    }

    /**
     * With the received packet, processes the data of scan header to display.
     *
     * @param data The received packet.
     */
    private void logScanHeader(byte[] data) {
        int frequency = (Integer.parseInt(Converters.getDecimalValue(data[1])) * 256) +
                Integer.parseInt(Converters.getDecimalValue(data[2])) + baseFrequency;
        /*int date = Converters.hexToDecimal(
                Converters.getHexValue(data[4]) + Converters.getHexValue(data[5]) + Converters.getHexValue(data[6]));
        month = date / 1000000;
        date = date % 1000000;
        day = date / 10000;
        date = date % 10000;
        hour = date / 100;
        minute = date % 100;
        seconds = Integer.parseInt(Converters.getDecimalValue(data[7]));
        currentData += month + "/" + day + "/" + year + "       " + hour + ":" + minute + ":" + seconds;*/

        /*if (!frequency_aerial_textView.getText().equals("") &&
                Integer.parseInt(frequency_aerial_textView.getText().toString().replace(".", "")) != frequency) {
            clear();
        }*/
        clear();

        table_index_aerial_textView.setText(Converters.getDecimalValue(data[3]));
        frequency_aerial_textView.setText(String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3));
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

        TextView signalStrengthTextView = new TextView(this);
        signalStrengthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        signalStrengthTextView.setTextAppearance(body_regular);
        signalStrengthTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        signalStrengthTextView.setLayoutParams(params);

        TextView detectionsTextView = new TextView(this);
        detectionsTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        detectionsTextView.setTextAppearance(body_regular);
        detectionsTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        detectionsTextView.setLayoutParams(params);

        TextView mortTextView = new TextView(this);
        mortTextView.setVisibility(View.GONE);

        newCode.addView(codeTextView);
        newCode.addView(signalStrengthTextView);
        newCode.addView(detectionsTextView);
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

        return Integer.parseInt(codeTextView.getText().toString().replace(" M", "")) == code;
    }

    /**
     * Updates data in the first row in the table.
     *
     * @param signalStrength Number of signal strength to update.
     */
    private void refreshFirstCode(int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(1);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(2);
        TextView mortTextView = (TextView) linearLayout.getChildAt(3);

        if (!codeTextView.getText().toString().contains(" M") && isMort) codeTextView.setText(codeTextView.getText().toString() + " M");
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
            TextView textView = (TextView) linearLayout.getChildAt(0);

            if (Integer.parseInt(textView.getText().toString().replace(" M", "")) == code)
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
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(2);
        TextView mortTextView = (TextView) linearLayout.getChildAt(3);

        int code = Integer.parseInt(codeTextView.getText().toString().replace(" M", ""));
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

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastMortTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateMortTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastMortTextView.setText(penultimateMortTextView.getText());
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newCodeTextView = (TextView) linearLayout.getChildAt(0);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(1);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(2);
        TextView newMortTextView = (TextView) linearLayout.getChildAt(3);

        newCodeTextView.setText(code + (isMort ? " M" : ""));
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
        newDetectionsTextView.setText(String.valueOf(detections));

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
        periodTextView.setTextAppearance(this, R.style.body_regular);
        periodTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        periodTextView.setLayoutParams(params);

        TextView pulseRateTextView = new TextView(this);
        pulseRateTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        pulseRateTextView.setTextAppearance(this, R.style.body_regular);
        pulseRateTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        pulseRateTextView.setLayoutParams(params);

        TextView signalStrengthTextView = new TextView(this);
        signalStrengthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        signalStrengthTextView.setTextAppearance(this, R.style.body_regular);
        signalStrengthTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        signalStrengthTextView.setLayoutParams(params);

        TextView detectionsTextView = new TextView(this);
        detectionsTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        detectionsTextView.setTextAppearance(this, R.style.body_regular);
        detectionsTextView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        detectionsTextView.setLayoutParams(params);

        newNonCoded.addView(periodTextView);
        newNonCoded.addView(pulseRateTextView);
        newNonCoded.addView(signalStrengthTextView);
        newNonCoded.addView(detectionsTextView);

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

            TextView lastPulseRateTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimatePulseRateTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastPulseRateTextView.setText(penultimatePulseRateTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());
        }

        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView newPeriodTextView = (TextView) linearLayout.getChildAt(0);
        TextView newPulseRateTextView = (TextView) linearLayout.getChildAt(1);
        TextView newSignalStrengthTextView = (TextView) linearLayout.getChildAt(2);
        TextView newDetectionsTextView = (TextView) linearLayout.getChildAt(3);

        newPeriodTextView.setText(String.valueOf(period));
        newPulseRateTextView.setText(String.valueOf(pulseRate));
        newSignalStrengthTextView.setText(String.valueOf(signalStrength));
        newDetectionsTextView.setText(String.valueOf(detections));
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

    /**
     * Displays a message indicating whether the writing was successful.
     *
     * @param data This packet indicates the writing status.
     */
    private void showMessage(byte[] data) {
        int status = Integer.parseInt(Converters.getDecimalValue(data[0]));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success!");
        if (status == 0)
            builder.setMessage("Completed.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
