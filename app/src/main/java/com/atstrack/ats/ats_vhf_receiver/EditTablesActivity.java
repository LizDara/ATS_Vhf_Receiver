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
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static com.atstrack.ats.ats_vhf_receiver.R.color.catskill_white;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.ghost;
import static com.atstrack.ats.ats_vhf_receiver.R.color.limed_spruce;
import static com.atstrack.ats.ats_vhf_receiver.R.color.tall_poppy;
import static com.atstrack.ats.ats_vhf_receiver.R.color.slate_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.border;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_delete;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.ic_next;

public class EditTablesActivity extends AppCompatActivity {

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
    @BindView(R.id.frequencies_overview_linearLayout)
    LinearLayout frequencies_overview_linearLayout;
    @BindView(R.id.edit_options_linearLayout)
    LinearLayout edit_options_linearLayout;
    @BindView(R.id.frequencies_linearLayout)
    LinearLayout frequencies_linearLayout;
    @BindView(R.id.frequency_edit_linearLayout)
    LinearLayout frequency_edit_linearLayout;
    @BindView(R.id.frequency_textView)
    TextView frequency_textView;
    @BindView(R.id.line_frequency_view)
    View line_frequency_view;
    @BindView(R.id.edit_frequency_message_textView)
    TextView edit_frequency_message_textView;
    @BindView(R.id.number_buttons_linearLayout)
    LinearLayout number_buttons_linearLayout;
    @BindView(R.id.save_changes_button)
    Button save_changes_button;
    @BindView(R.id.all_frequencies_checkBox)
    CheckBox all_frequencies_checkBox;
    @BindView(R.id.delete_frequencies_linearLayout)
    LinearLayout delete_frequencies_linearLayout;
    @BindView(R.id.select_frequencies_linearLayout)
    LinearLayout select_frequencies_linearLayout;
    @BindView(R.id.delete_selected_frequencies_button)
    Button delete_selected_frequencies_button;
    @BindView(R.id.no_frequencies_linearLayout)
    LinearLayout no_frequencies_linearLayout;

    final private String TAG = EditTablesActivity.class.getSimpleName();

    private int[] originalTable;
    private int number;
    private int originalTotalFrequencies;
    private int baseFrequency;
    private int frequencyRange;
    private boolean isFile;
    private List<Integer> editedFrequencies;

    private LinearLayout linearLayoutFrequency;
    private TextView textViewFrequency;
    private ImageView imageViewFrequency;
    private CheckBox checkBoxFrequency;
    private LinearLayout linearLayoutBaseFrequency;
    private Button buttonBaseFrequency;

    private int selectedFrequencyIndex;
    private int selectedFrequency;
    private Handler handlerMessage;
    private int numberOfSelected;

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

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
                    switch (parameter) {
                        case "table": // Ask for frequencies from a table
                            onClickTable();
                            break;
                        case "receive": // Gets the frequencies from a table
                            onReceiveTable();
                            break;
                        case "sendTable": // Sends the modified frequencies
                            onClickSendTable();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    Log.i(TAG, Converters.getHexValue(packet));
                    if (parameter.equals("table")) // Gets the frequencies from a table
                        downloadData(packet);
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

    /**
     * Writes the table number to get its frequencies.
     * Service name: StoredData.
     * Characteristic name: FreqTable.
     */
    private void onClickTable() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic;
        switch (number) {
            case 1:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_1;
                break;
            case 2:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_2;
                break;
            case 3:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_3;
                break;
            case 4:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_4;
                break;
            case 5:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_5;
                break;
            case 6:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_6;
                break;
            case 7:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_7;
                break;
            case 8:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_8;
                break;
            case 9:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_9;
                break;
            case 10:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_10;
                break;
            case 11:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_11;
                break;
            case 12:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_12;
                break;
            default:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        }
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Enables notification for receive the frequencies from that table.
     * Service name: StoredData.
     * Characteristic name: FreqTable.
     */
    private void onReceiveTable() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);
    }

    /**
     * Writes the modified frequencies by the user.
     * Service name: StoredData.
     * Characteristic name: FreqTable.
     */
    private void onClickSendTable() {
        parameter = "";
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);

        byte[] b = new byte[244];
        b[0] = (byte) 0x7E;
        b[1] = (byte) YY;
        b[2] = (byte) MM;
        b[3] = (byte) DD;
        b[4] = (byte) hh;
        b[5] = (byte) mm;
        b[6] = (byte) ss;
        b[7] = (byte) number;//frequency number table
        b[8] = (byte) editedFrequencies.size();//Number of frequencies in the table
        b[9] = (byte) (baseFrequency / 1000);//base frequency

        int index = 10;
        int i = 0;
        while (i < editedFrequencies.size()) {
            b[index] = (byte) ((editedFrequencies.get(i) - baseFrequency) / 256);
            b[index + 1] = (byte) ((editedFrequencies.get(i) - baseFrequency) % 256);
            i++;
            index += 2;
        }

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic;
        switch (number) {
            case 1:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_1;
                break;
            case 2:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_2;
                break;
            case 3:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_3;
                break;
            case 4:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_4;
                break;
            case 5:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_5;
                break;
            case 6:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_6;
                break;
            case 7:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_7;
                break;
            case 8:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_8;
                break;
            case 9:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_9;
                break;
            case 10:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_10;
                break;
            case 11:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_11;
                break;
            case 12:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_TABLE_12;
                break;
            default:
                characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        }
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result)
            showMessage(0);
        else
            showMessage(2);
    }

    /**
     * Checks that the number of frequencies is not greater than 100.
     *
     * @return Returns true, if the number of frequencies is less than or equal to 100.
     */
    private boolean isWithinLimit() {
        return editedFrequencies.size() <= 100;
    }

    @OnClick(R.id.delete_frequencies_button)
    public void onClickDeleteFrequencies(View v) {
        setVisibility("delete");
        title_toolbar.setText(R.string.lb_delete_frequencies);

        delete_selected_frequencies_button.setEnabled(false);
        delete_selected_frequencies_button.setAlpha((float) 0.6);
    }

    @OnClick(R.id.add_frequency_button)
    public void onClickAddFrequency(View v) {
        if (isWithinLimit()) {
            setVisibility("edit");
            title_toolbar.setText(R.string.lb_add_frequency);

            frequency_textView.setText(R.string.lb_enter_frequency_digits);
            frequency_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
            line_frequency_view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), ghost));
            edit_frequency_message_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
            save_changes_button.setText(R.string.lb_add_frequency);
            save_changes_button.setEnabled(false);
            save_changes_button.setAlpha((float) 0.6);
        } else {
            showMessage(1);
        }
    }

    @OnClick(R.id.save_changes_button)
    public void onClickSaveChanges(View v) {
        int frequency = Integer.parseInt(frequency_textView.getText().toString());
        if (frequency >= baseFrequency && frequency <= frequencyRange) {
            if (selectedFrequency != 0) {
                if (selectedFrequency != frequency)
                    changeSelectedFrequency(frequency);
            } else {
                addFrequency(frequency);
            }
        } else {
            line_frequency_view.setBackgroundColor(ContextCompat.getColor(this, tall_poppy));
            edit_frequency_message_textView.setTextColor(ContextCompat.getColor(this, tall_poppy));
            save_changes_button.setEnabled(false);
            save_changes_button.setAlpha((float) 0.6);
        }
    }

    @OnClick(R.id.delete_selected_frequencies_button)
    public void onClickDeleteSelectedFrequencies(View v) {
        deleteFrequencies();
    }

    @OnClick(R.id.add_new_frequency_button)
    public void onClickAddNewFrequency(View v) {
        setVisibility("edit");
        title_toolbar.setText(R.string.lb_add_frequency);

        frequency_textView.setText(R.string.lb_enter_frequency_digits);
        frequency_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
        line_frequency_view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), ghost));
        edit_frequency_message_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
        save_changes_button.setText(R.string.lb_add_frequency);
        save_changes_button.setEnabled(false);
        save_changes_button.setAlpha((float) 0.6);
    }

    @OnClick(R.id.all_frequencies_checkBox)
    public void onClickAllFrequencies(View v) {
        for (int i = 0; i < select_frequencies_linearLayout.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) select_frequencies_linearLayout.getChildAt(i);
            CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
            checkBox.setChecked(all_frequencies_checkBox.isChecked());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tables);
        ButterKnife.bind(this);

        // Gets the table number to ask its frequencies
        number = getIntent().getExtras().getInt("number");
        originalTotalFrequencies = getIntent().getExtras().getInt("total");

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText("Table " + number + " (" + originalTotalFrequencies + " Frequencies)");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        selectedFrequency = 0;
        selectedFrequencyIndex = -1;
        editedFrequencies = new LinkedList<>();
        handlerMessage = new Handler();
        numberOfSelected = 0;

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        // Gets the number of frequencies from that table
        baseFrequency = getIntent().getExtras().getInt("baseFrequency") * 1000;
        int range = getIntent().getExtras().getInt("range");
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;

        frequency_textView.addTextChangedListener(textChangedListener);
        String message = "Frequency range is " + baseFrequency + " to " + frequencyRange;
        edit_frequency_message_textView.setText(message);

        isFile = getIntent().getExtras().getBoolean("isFile");
        if (isFile) { // Asks for the frequencies obtained from a file
            setVisibility("overview");
            originalTable = getIntent().getExtras().getIntArray("frequencies");
            editedFrequencies = new LinkedList<>();
            for (int frequency : originalTable)
                editedFrequencies.add(frequency);

            showTable();
        } else { // Asks for the frequencies from that table, the frequency base and range
            if (originalTotalFrequencies > 0) { // Asks for the frequencies from the table
                parameter = "table";
                setVisibility("overview");
            } else { // Initializes empty
                originalTable = new int[]{};
                setVisibility("none");
            }
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        createNumberButtons(range);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (frequencies_overview_linearLayout.getVisibility() == View.VISIBLE
                    || no_frequencies_linearLayout.getVisibility() == View.VISIBLE) {
                if (existChanges()) {
                    parameter = "sendTable";
                    mBluetoothLeService.discovering();
                } else {
                    finish();
                }
            } else if (frequency_edit_linearLayout.getVisibility() == View.VISIBLE) {
                setVisibility("overview");
                title_toolbar.setText("Table " + number + " (" + editedFrequencies.size() + " Frequencies)");
                selectedFrequency = 0;
                selectedFrequencyIndex = -1;
                frequency_textView.setText(R.string.lb_enter_frequency_digits);
            } else if (delete_frequencies_linearLayout.getVisibility() == View.VISIBLE) {
                for (int i = 0; i < select_frequencies_linearLayout.getChildCount(); i++) {
                    LinearLayout linearLayout = (LinearLayout) select_frequencies_linearLayout.getChildAt(i);
                    CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
                    if (checkBox.isChecked())
                        checkBox.setChecked(false);
                }
                setVisibility("overview");
                title_toolbar.setText("Table " + number + " (" + editedFrequencies.size() + " Frequencies)");
            }
        }
        return super.onOptionsItemSelected(item);
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
        if (!isFile) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
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

    private void setVisibility(String value) {
        switch (value) {
            case "overview":
                frequencies_overview_linearLayout.setVisibility(View.VISIBLE);
                no_frequencies_linearLayout.setVisibility(View.GONE);
                frequency_edit_linearLayout.setVisibility(View.GONE);
                delete_frequencies_linearLayout.setVisibility(View.GONE);
                break;
            case "none":
                frequencies_overview_linearLayout.setVisibility(View.GONE);
                no_frequencies_linearLayout.setVisibility(View.VISIBLE);
                frequency_edit_linearLayout.setVisibility(View.GONE);
                delete_frequencies_linearLayout.setVisibility(View.GONE);
                break;
            case "edit":
                frequencies_overview_linearLayout.setVisibility(View.GONE);
                no_frequencies_linearLayout.setVisibility(View.GONE);
                frequency_edit_linearLayout.setVisibility(View.VISIBLE);
                delete_frequencies_linearLayout.setVisibility(View.GONE);
                break;
            case "delete":
                frequencies_overview_linearLayout.setVisibility(View.GONE);
                no_frequencies_linearLayout.setVisibility(View.GONE);
                frequency_edit_linearLayout.setVisibility(View.GONE);
                delete_frequencies_linearLayout.setVisibility(View.VISIBLE);
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
     * Displays the frequencies on the screen.
     */
    private void showTable() {
        for (int i = 0; i < editedFrequencies.size(); i++) {
            createNewFrequencyCell(i);
            createNewFrequencyDeleteCell(i);
        }
    }

    private void createNewFrequencyCell(int index) {
        newFrequencyCell();
        textViewFrequency.setText(String.valueOf(editedFrequencies.get(index)));
        linearLayoutFrequency.addView(textViewFrequency);
        linearLayoutFrequency.addView(imageViewFrequency);
        linearLayoutFrequency.setOnClickListener(view -> {
            selectedFrequency = editedFrequencies.get(index);
            selectedFrequencyIndex = index;
            setVisibility("edit");
            title_toolbar.setText("Edit Frequency " + selectedFrequency);

            frequency_textView.setText(R.string.lb_enter_frequency_digits);
            frequency_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
            line_frequency_view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), ghost));
            edit_frequency_message_textView.setTextColor(ContextCompat.getColor(getBaseContext(), slate_gray));
            save_changes_button.setText(R.string.lb_save_changes);
            save_changes_button.setEnabled(false);
            save_changes_button.setAlpha((float) 0.6);
        });
        frequencies_linearLayout.addView(linearLayoutFrequency);
    }

    private void createNewFrequencyDeleteCell(int index) {
        newFrequencyDeleteCell();
        textViewFrequency.setText(String.valueOf(editedFrequencies.get(index)));
        checkBoxFrequency.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                delete_selected_frequencies_button.setEnabled(true);
                delete_selected_frequencies_button.setAlpha((float) 1);
                numberOfSelected++;
                if (numberOfSelected == select_frequencies_linearLayout.getChildCount())
                    all_frequencies_checkBox.setChecked(true);
            } else {
                numberOfSelected--;
                if (numberOfSelected == 0) {
                    delete_selected_frequencies_button.setEnabled(false);
                    delete_selected_frequencies_button.setAlpha((float) 0.6);
                } else if (numberOfSelected == select_frequencies_linearLayout.getChildCount() - 1)
                    all_frequencies_checkBox.setChecked(false);
            }
        });
        linearLayoutFrequency.addView(checkBoxFrequency);
        linearLayoutFrequency.addView(textViewFrequency);
        select_frequencies_linearLayout.addView(linearLayoutFrequency);
    }

    /**
     * Initializes the customize TextView.
     */
    private void newFrequencyCell() {
        linearLayoutFrequency = new LinearLayout(this);
        linearLayoutFrequency.setBackgroundColor(ContextCompat.getColor(this, catskill_white));
        linearLayoutFrequency.setLayoutParams(newLinearLayoutParams());
        linearLayoutFrequency.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutFrequency.setElevation(4);
        linearLayoutFrequency.setGravity(Gravity.CENTER);
        linearLayoutFrequency.setPadding(32, 32, 32, 32);

        textViewFrequency = new TextView(this);
        textViewFrequency.setTextSize(16);
        textViewFrequency.setTextColor(ContextCompat.getColor(this, limed_spruce));
        textViewFrequency.setLayoutParams(newTextViewParams());

        imageViewFrequency = new ImageView(this);
        imageViewFrequency.setBackground(ContextCompat.getDrawable(this, ic_next));
    }

    private void newFrequencyDeleteCell() {
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

    private ViewGroup.LayoutParams newTextViewParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;
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

    /**
     * With the received packet, gets the frequencies from the table and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        parameter = "";
        originalTable = new int[originalTotalFrequencies];
        int index = 10;
        int i = 0;
        while (i < originalTable.length) {
            int frequency = (Integer.parseInt(Converters.getDecimalValue(data[index])) * 256) +
                    Integer.parseInt(Converters.getDecimalValue(data[index + 1]));
            originalTable[i] = baseFrequency + frequency;
            editedFrequencies.add(originalTable[i]);
            i++;
            index += 2;
        }

        frequencies_linearLayout.removeAllViews();
        showTable();
    }

    private void changeSelectedFrequency(int frequency) {
        // Change frequency cell
        LinearLayout linearLayout = (LinearLayout) frequencies_linearLayout.getChildAt(selectedFrequencyIndex);
        TextView textView = (TextView) linearLayout.getChildAt(0);
        textView.setText(String.valueOf(frequency));

        // Change frequency delete cell
        LinearLayout linearLayoutSelectDelete = (LinearLayout) select_frequencies_linearLayout.getChildAt(selectedFrequencyIndex);
        TextView textViewSelectDelete = (TextView) linearLayoutSelectDelete.getChildAt(1);
        textViewSelectDelete.setText(String.valueOf(frequency));

        editedFrequencies.set(selectedFrequencyIndex, frequency);
        selectedFrequency = 0;
        selectedFrequencyIndex = -1;

        manageMessage(R.string.lb_frequency_saved, false);
    }

    private void addFrequency(int frequency) {
        int index = editedFrequencies.size();
        editedFrequencies.add(frequency);
        createNewFrequencyCell(index);
        createNewFrequencyDeleteCell(index);

        manageMessage(R.string.lb_frequency_added, false);
    }

    private void deleteFrequencies() {
        int index = 0;
        while (index < select_frequencies_linearLayout.getChildCount()) {
            LinearLayout linearLayout = (LinearLayout) select_frequencies_linearLayout.getChildAt(index);
            CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
            if (checkBox.isChecked()) {
                editedFrequencies.remove(index);
                frequencies_linearLayout.removeViewAt(index);
                select_frequencies_linearLayout.removeViewAt(index);
            } else {
                index++;
            }
        }
        manageMessage(R.string.lb_frequencies_deleted, frequencies_linearLayout.getChildCount() == 0);
    }

    private void manageMessage(int idStringMessage, boolean isEmpty) {
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
            if (isEmpty)
                setVisibility("none");
            else
                setVisibility("overview");
            title_toolbar.setText("Table " + number + " (" + editedFrequencies.size() + " Frequencies)");
        }, MESSAGE_PERIOD);
    }

    private boolean existChanges() {
        if (originalTable.length != editedFrequencies.size())
            return true;
        for (int i = 0; i < originalTable.length; i++) {
            if (originalTable[i] != editedFrequencies.get(i))
                return true;
        }
        return false;
    }

    /**
     * Displays a message indicating whether the writing was successful.
     *
     * @param status This number indicates the writing status.
     */
    private void showMessage(int status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message!");
        if (status == 0) {
            builder.setMessage("Completed.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                finish();
            });
        } else if (status == 2) {
            builder.setMessage("Not completed.");
            builder.setPositiveButton("OK", null);
        } else if (status == 1) {
            builder.setMessage("Exceeded Table Limit. Please enter no more than 100 frequencies.");
            builder.setPositiveButton("OK", null);
        }
        builder.show();
    }
}
