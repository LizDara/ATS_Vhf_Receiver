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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyDeleteListAdapter;
import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

public class EditTablesActivity extends AppCompatActivity {

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
    @BindView(R.id.frequencies_overview_linearLayout)
    LinearLayout frequencies_overview_linearLayout;
    @BindView(R.id.edit_options_linearLayout)
    LinearLayout edit_options_linearLayout;
    @BindView(R.id.all_frequencies_checkBox)
    CheckBox all_frequencies_checkBox;
    @BindView(R.id.delete_frequencies_linearLayout)
    LinearLayout delete_frequencies_linearLayout;
    @BindView(R.id.delete_selected_frequencies_button)
    Button delete_selected_frequencies_button;
    @BindView(R.id.no_frequencies_linearLayout)
    LinearLayout no_frequencies_linearLayout;
    @BindView(R.id.frequencies_listView)
    ListView frequencies_listView;
    @BindView(R.id.frequencies_delete_listView)
    ListView frequencies_delete_listView;

    final private String TAG = EditTablesActivity.class.getSimpleName();

    private int[] originalTable;
    private FrequencyListAdapter frequencyListAdapter;
    private FrequencyDeleteListAdapter frequencyDeleteListAdapter;
    private int number;
    private int originalTotalFrequencies;
    private int baseFrequency;
    private int range;
    private boolean isFile;

    private Handler handlerMessage;

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

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    int position = result.getData().getExtras().getInt("position");
                    int frequency = result.getData().getExtras().getInt("frequency");
                    if (position != -1) {
                        if (frequencyListAdapter.getFrequency(position) != frequency)
                            changeSelectedFrequency(frequency, position);
                    } else {
                        addFrequency(frequency);
                    }
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
        b[8] = (byte) frequencyListAdapter.getCount();//Number of frequencies in the table
        b[9] = (byte) (baseFrequency / 1000);//base frequency

        int index = 10;
        int i = 0;
        while (i < frequencyListAdapter.getCount()) {
            b[index] = (byte) ((frequencyListAdapter.getFrequency(i) - baseFrequency) / 256);
            b[index + 1] = (byte) ((frequencyListAdapter.getFrequency(i) - baseFrequency) % 256);
            i++;
            index += 2;
        }

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic;
        switch (number)     {
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

    @OnClick(R.id.delete_frequencies_button)
    public void onClickDeleteFrequencies(View v) {
        setVisibility("delete");

        frequencyDeleteListAdapter.setStateSelected(false);
        frequencyDeleteListAdapter.notifyDataSetChanged();

        delete_selected_frequencies_button.setEnabled(false);
        delete_selected_frequencies_button.setAlpha((float) 0.6);
    }

    @OnClick({R.id.add_frequency_button, R.id.add_new_frequency_button})
    public void onClickAddFrequency(View v) {
        if (isWithinLimit()) {
            Intent intent = new Intent(this, EnterFrequencyActivity.class);
            intent.putExtra("title", "Add Frequency");
            intent.putExtra("position", -1);
            intent.putExtra("baseFrequency", baseFrequency);
            intent.putExtra("range", range);
            launcher.launch(intent);
        } else {
            showMessage(1);
        }
    }

    @OnClick(R.id.delete_selected_frequencies_button)
    public void onClickDeleteSelectedFrequencies(View v) {
        deleteFrequencies();
    }

    @OnClick(R.id.all_frequencies_checkBox)
    public void onClickAllFrequencies(View v) {
        changeAllCheckBox(all_frequencies_checkBox.isChecked());
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

        handlerMessage = new Handler();

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_status_textView.setText(receiverInformation.getDeviceStatus());
        device_range_textView.setText(receiverInformation.getDeviceRange());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        baseFrequency = getIntent().getExtras().getInt("baseFrequency") * 1000;
        range = getIntent().getExtras().getInt("range");

        isFile = getIntent().getExtras().getBoolean("isFile");
        if (isFile) { // Asks for the frequencies obtained from a file
            setVisibility("overview");
            originalTable = getIntent().getExtras().getIntArray("frequencies");
            ArrayList<Integer> frequencies = new ArrayList<>();
            for (int frequency : originalTable)
                frequencies.add(frequency);

            frequencyListAdapter = new FrequencyListAdapter(this, frequencies, baseFrequency, range, launcher);
            frequencies_listView.setAdapter(frequencyListAdapter);
            frequencyDeleteListAdapter = new FrequencyDeleteListAdapter(this, frequencies, all_frequencies_checkBox, delete_selected_frequencies_button);
            frequencies_delete_listView.setAdapter(frequencyDeleteListAdapter);
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
            } else if (delete_frequencies_linearLayout.getVisibility() == View.VISIBLE) {
                setVisibility("overview");
                title_toolbar.setText("Table " + number + " (" + frequencyListAdapter.getCount() + " Frequencies)");
                changeAllCheckBox(false);
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

    /**
     * Checks that the number of frequencies is not greater than 100.
     *
     * @return Returns true, if the number of frequencies is less than or equal to 100.
     */
    private boolean isWithinLimit() {
        return frequencyListAdapter.getCount() < 100;
    }

    private void setVisibility(String value) {
        switch (value) {
            case "overview":
                frequencies_overview_linearLayout.setVisibility(View.VISIBLE);
                no_frequencies_linearLayout.setVisibility(View.GONE);
                delete_frequencies_linearLayout.setVisibility(View.GONE);
                break;
            case "none":
                frequencies_overview_linearLayout.setVisibility(View.GONE);
                no_frequencies_linearLayout.setVisibility(View.VISIBLE);
                delete_frequencies_linearLayout.setVisibility(View.GONE);
                break;
            case "delete":
                frequencies_overview_linearLayout.setVisibility(View.GONE);
                no_frequencies_linearLayout.setVisibility(View.GONE);
                delete_frequencies_linearLayout.setVisibility(View.VISIBLE);
                title_toolbar.setText(R.string.lb_delete_frequencies);
                break;
        }
    }

    /**
     * With the received packet, gets the frequencies from the table and display on the screen.
     *
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        parameter = "";
        originalTable = new int[originalTotalFrequencies];
        ArrayList<Integer> frequencies = new ArrayList<>();
        int index = 10;
        int i = 0;
        while (i < originalTable.length) {
            int frequency = (Integer.parseInt(Converters.getDecimalValue(data[index])) * 256) +
                    Integer.parseInt(Converters.getDecimalValue(data[index + 1]));
            originalTable[i] = baseFrequency + frequency;
            frequencies.add(originalTable[i]);
            i++;
            index += 2;
        }

        frequencyListAdapter = new FrequencyListAdapter(this, frequencies, baseFrequency, range, launcher);
        frequencies_listView.setAdapter(frequencyListAdapter);
        frequencyDeleteListAdapter = new FrequencyDeleteListAdapter(this, frequencies, all_frequencies_checkBox, delete_selected_frequencies_button);
        frequencies_delete_listView.setAdapter(frequencyDeleteListAdapter);
    }

    private void changeSelectedFrequency(int frequency, int position) {
        frequencyListAdapter.setFrequency(position, frequency);
        frequencyListAdapter.notifyDataSetChanged();
        frequencyDeleteListAdapter.notifyDataSetChanged();

        manageMessage(R.string.lb_frequency_saved, false);
    }

    private void addFrequency(int frequency) {
        frequencyDeleteListAdapter.addFrequency(frequency);
        frequencyListAdapter.notifyDataSetChanged();
        frequencyDeleteListAdapter.notifyDataSetChanged();

        manageMessage(R.string.lb_frequency_added, false);
    }

    private void changeAllCheckBox(boolean isChecked) {
        frequencyDeleteListAdapter.setStateSelected(isChecked);
        frequencyDeleteListAdapter.notifyDataSetChanged();

        if (isChecked) {
            delete_selected_frequencies_button.setEnabled(true);
            delete_selected_frequencies_button.setAlpha(1);
        } else {
            delete_selected_frequencies_button.setEnabled(false);
            delete_selected_frequencies_button.setAlpha((float) 0.6);
        }
    }

    private void deleteFrequencies() {
        int index = 0;
        while (index < frequencyDeleteListAdapter.getCount()) {
            if (frequencyDeleteListAdapter.isSelected(index)) {
                frequencyDeleteListAdapter.removeFrequency(index);
            } else {
                index++;
            }
        }
        frequencyListAdapter.notifyDataSetChanged();
        frequencyDeleteListAdapter.notifyDataSetChanged();

        manageMessage(R.string.lb_frequencies_deleted, frequencyListAdapter.getCount() == 0);
    }

    private void manageMessage(int idStringMessage, boolean isEmpty) {
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
            if (isEmpty)
                setVisibility("none");
            else
                setVisibility("overview");
            title_toolbar.setText("Table " + number + " (" + frequencyListAdapter.getCount() + " Frequencies)");
        }, MESSAGE_PERIOD);
    }

    private boolean existChanges() {
        int count = frequencyListAdapter == null ? 0 : frequencyListAdapter.getCount();
        if (originalTable.length != count)
            return true;
        for (int i = 0; i < originalTable.length; i++) {
            if (originalTable[i] != frequencyListAdapter.getFrequency(i))
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