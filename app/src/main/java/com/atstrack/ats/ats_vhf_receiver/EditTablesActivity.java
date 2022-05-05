package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
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
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_blue;

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
    @BindView(R.id.number_table_textView)
    TextView number_table_textView;
    @BindView(R.id.frequency_table)
    TableLayout frequency_table;
    @BindView(R.id.edit_table_options_linearLayout)
    LinearLayout edit_table_options_linearLayout;
    @BindView(R.id.edit_table_message_textView)
    TextView edit_table_message_textView;
    @BindView(R.id.save_changes_button)
    Button save_changes_button;
    @BindView(R.id.edit_table_option_button)
    Button edit_table_option_button;

    final private String TAG = EditTablesActivity.class.getSimpleName();

    private int[] table;
    private int number;
    private int totalFrequencies;
    private int baseFrequency;
    private int range;
    private boolean isFile;

    private TextView textCell;
    private TableRow tableRow;
    private int widthPixels;
    private int heightPixels;
    private int tableLimitEdit;
    private final static char LF  = (char) 0x0A;
    private boolean isChanged;

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
    private String parameter;

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
                    if (parameter.equals("receive")) // Gets the frequencies from a table
                        downloadData(packet);
                    else if (parameter.equals("sendTable")) // Sends the modified frequencies
                        showMessage(packet);
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

    /**
     * Writes the table number to get its frequencies.
     * Service name: StoredData.
     * Characteristic name: FreqTable.
     */
    private void onClickTable() {
        parameter = "receive";
        byte[] b = new byte[] {(byte) 0x7E, (byte) 0x7E, (byte) 0x7E, (byte) 0x7E, (byte) 0x7E, (byte) 0x7E, (byte) 0x7E, (byte) number};
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, true);
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
        b[8] = (byte) table.length;//Number of frequencies in the table
        b[9] = (byte) baseFrequency;//base frequency

        int index = 10;
        int i = 0;
        while (i < table.length) {
            table[i] = (table[i] % (baseFrequency * 1000));
            b[index] = (byte) (table[i] / 256);
            b[index + 1] = (byte) (table[i] % 256);
            i++;
            index += 2;
        }

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        mBluetoothLeService.writeCharacteristic(service, characteristic, b, false);

        finish();
    }

    /**
     * Displays a alert dialog for the user to choose to edit the table or delete all frequencies.
     */
    private void createDialogSelect() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.options_edit_table, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        Button editTable = view.findViewById(R.id.edit_table_button);
        Button clearTable = view.findViewById(R.id.clear_table_button);

        editTable.setOnClickListener(v -> {
            dialog.dismiss();
            createDialogEdit();
        });
        clearTable.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Discard Changes");
            builder.setMessage("Are you sure you want to delete all the frequencies in this table?");
            builder.setPositiveButton("Delete Frequencies", (dialog1, which) -> {
                table = new int[]{};
                isChanged = true;
                frequency_table.removeAllViews();
                showTable();

                save_changes_button.setVisibility(View.VISIBLE);
                save_changes_button.setAlpha((float) 1);
                save_changes_button.setEnabled(true);
                edit_table_options_linearLayout.setVisibility(View.VISIBLE);
                edit_table_option_button.setVisibility(View.GONE);
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialogI = builder.create();
            dialogI.show();
            dialogI.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.catskill_white)));
            dialog.dismiss();
        });
        dialog.getWindow().setLayout(widthPixels / 3, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setView(view);
        dialog.show();
    }

    /**
     * Displays a alert dialog where the user can edit the frequencies of that table, also check that each frequency is correct.
     */
    private void createDialogEdit() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.edit_frequencies, null);
        final AlertDialog builder = new AlertDialog.Builder(this).create();

        // Does not allow the alert dialog to disappear when touch outside of it
        builder.setCanceledOnTouchOutside(false);

        // Asks if you want to lose the data already modified when touching back while the alert dialog is displayed
        builder.setOnKeyListener((dialogI, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getBaseContext());
                builder1.setTitle("Discard Changes");
                builder1.setMessage("If you leave without saving you will lose your changes. Do you wish to continue?");
                builder1.setNegativeButton("Cancel", null);
                builder1.setPositiveButton("Discard", (dialog, which) -> {
                    dialogI.dismiss();
                });
                AlertDialog dialog = builder1.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.catskill_white)));
                return true;
            }
            return false;
        });

        // Defines the size of the alert dialog
        ScrollView editScrollView = view.findViewById(R.id.edit_table_scrollView);
        ViewGroup.LayoutParams params = editScrollView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = heightPixels / 3;
        editScrollView.setLayoutParams(params);

        // Customizes the style of the alert dialog
        TextView title = new TextView(this);
        title.setText("Edit Table " + number);
        title.setPadding(100, 80, 10, 10);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setBackgroundColor(ContextCompat.getColor(this, catskill_white));
        title.setTextColor(ContextCompat.getColor(this, light_blue));
        builder.setCustomTitle(title);

        EditText editTable = view.findViewById(R.id.edit_table_editText);
        String frequencies = "";

        for (int i = 0; i < table.length; i++) {
            if (i > 0)
                frequencies += "" + LF;
            frequencies += table[i];
        }
        // Set the frequencies to edit
        editTable.setText(frequencies);

        // Keeps keys visible
        editTable.requestFocus();
        editTable.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        Button undoChanges = view.findViewById(R.id.undo_changes_button);
        Button done = view.findViewById(R.id.done_button);

        // Copy the initial frequencies
        String initFrequencies = frequencies;
        undoChanges.setOnClickListener(v -> {
            AlertDialog.Builder builder12 = new AlertDialog.Builder(v.getContext());
            builder12.setTitle("Warning");
            builder12.setMessage("Are you sure you want to discard your changes?");
            builder12.setNegativeButton("No", null);
            builder12.setPositiveButton("Yes", (dialog, which) -> {
                editTable.getText().clear();
                editTable.setText(initFrequencies);
            });
            AlertDialog dialogI = builder12.create();
            dialogI.show();
            dialogI.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(catskill_white)));
        });
        done.setOnClickListener(v -> {
            String frequencies1 = String.valueOf(editTable.getText());
            if (checkFormat(frequencies1)) {
                if (checkTableLimit()) {
                    saveChanges(frequencies1);
                    // Removes all frequencies
                    frequency_table.removeAllViews();
                    // Set the modified frequencies
                    showTable();
                    if (isChanged) { // If changes were made, enables the discard changes button and save changes button
                        save_changes_button.setVisibility(View.VISIBLE);
                        save_changes_button.setAlpha((float) 1);
                        save_changes_button.setEnabled(true);
                        edit_table_options_linearLayout.setVisibility(View.VISIBLE);
                        edit_table_option_button.setVisibility(View.GONE);
                    }
                    builder.dismiss();
                } else {
                    showWarningDialog("Exceeded Table Limit", "Please enter no more than 100 frequencies.");
                }
            } else {
                showWarningDialog("Invalid Format or Values", "Please enter valid frequency values, each on a separate line.");
            }
        });
        builder.setView(view);
        builder.show();

        // Enables the keys when the alert dialog appears
        builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        builder.getWindow().setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Checks that all edited frequencies are in the correct format.
     *
     * @param value The edited frequencies.
     *
     * @return Returns true, if the frequencies are correct.
     */
    private boolean checkFormat(String value) {
        tableLimitEdit = 0;
        boolean correct = true;

        String[] frequencies = value.split(String.valueOf(LF));
        for (String frequency : frequencies) {
            tableLimitEdit++;
            if (frequency.length() != 6)
                correct = false;
            else if ((Integer.parseInt(frequency) < (baseFrequency * 1000))
                    || (Integer.parseInt(frequency) > (baseFrequency + range) * 1000)) //>=
                correct = false;
        }

        return correct;
    }

    /**
     * Checks that the number of frequencies is not greater than 100.
     *
     * @return Returns true, if the number of frequencies is less than or equal to 100.
     */
    private boolean checkTableLimit() {
        return tableLimitEdit <= 100;
    }

    /**
     * Displays a specific error message.
     *
     * @param error The error name.
     * @param message The message to be displayed.
     */
    private void showWarningDialog(String error, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(error);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(catskill_white)));
    }

    /**
     * Saves the edited frequencies and updates the vector containing all the previous frequencies.
     *
     * @param value The edited frequencies.
     */
    private void saveChanges(String value) {
        int[] newTable = new int[tableLimitEdit];

        // If the number of edited frequencies is greater than the previous frequencies then it means that there were changes
        if (tableLimitEdit != table.length)
            isChanged = true;
        String[] list = value.split(String.valueOf(LF));
        for (int i = 0; i < list.length; i++) {
            newTable[i] = Integer.parseInt(list[i]);
            if (!isChanged && newTable[i] != table[i])
                isChanged = true;
        }
        // The table is updated
        table = newTable;
    }

    @OnClick(R.id.edit_frequency_table_button)
    public void onClickEditDefaults(View v) {
        createDialogSelect();
    }

    @OnClick(R.id.discard_changes_button)
    public void onClickDiscardChanges(View v) {
        finish();
    }

    @OnClick(R.id.save_changes_button)
    public void onClickSaveChanges(View v) {
        parameter = "sendTable";
        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.edit_table_option_button)
    public void onClickEditTable(View v) {
        createDialogSelect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tables);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.edit_frequency_tables);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Gets the size of the cell phone screen in pixels
        widthPixels = getResources().getDisplayMetrics().widthPixels;
        heightPixels = getResources().getDisplayMetrics().heightPixels;

        isChanged = false;

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        // Gets the table number to ask its frequencies
        number = getIntent().getExtras().getInt("number");
        // Gets the number of frequencies from that table
        totalFrequencies = getIntent().getExtras().getInt("total");
        number_table_textView.setText("Table " + number);

        isFile = getIntent().getExtras().getBoolean("isFile");
        if (isFile) { // Asks for the frequencies obtained from a file
            int[] fileTable = getIntent().getExtras().getIntArray("frequencies");
            table = new int[totalFrequencies];
            if (table.length >= 0) System.arraycopy(fileTable, 0, table, 0, table.length);

            showTable();
        } else { // Asks for the frequencies from that table, the frequency base and range
            baseFrequency = getIntent().getExtras().getInt("baseFrequency");
            range = getIntent().getExtras().getInt("range");
            if (totalFrequencies > 0) { // Asks for the frequencies from the table
                parameter = "table";
            } else { // Initializes empty
                downloadData(new byte[]{});
            }
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (isChanged) { // Asks if you want to lose changes
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Discard Changes");
                builder.setMessage("If you leave without saving you will lose your changes. Do you wish to continue?");
                builder.setPositiveButton("Discard", (dialog, which) -> {
                    finish();
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(catskill_white)));
            } else {
                finish();
            }
            return true;
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
     * Displays the frequencies on the screen.
     */
    private void showTable() {
        for (int i = 0; i < table.length - 1; i++) {
            newCell();
            textCell.setText(String.valueOf(table[i]));
            tableRow.addView(textCell, newTableRowParams(16));
            frequency_table.addView(tableRow);
        }
        if (table.length > 0) {
            newCell();
            textCell.setText(String.valueOf(table[table.length - 1]));
            tableRow.addView(textCell, newTableRowParams(0));
            frequency_table.addView(tableRow);
        }
    }

    /**
     * Initializes the customize TextView.
     */
    private void newCell() {
        tableRow = new TableRow(this);
        textCell = new TextView(this);
        textCell.setTextSize(16);
        textCell.setTextColor(ContextCompat.getColor(this, light_blue));
    }

    /**
     * Sets the margins for the TableRow.
     *
     * @param bottom The size bottom margin.
     *
     * @return Returns a LayoutParams with the customize margins.
     */
    private TableRow.LayoutParams newTableRowParams(int bottom) {
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(0, 0, 0, bottom);
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
        isChanged = false;
        save_changes_button.setAlpha((float) 0.6);
        save_changes_button.setEnabled(false);
        table = new int[totalFrequencies];

        int index = 10;
        int i = 0;
        while (i < table.length) {
            int frequency = (Integer.parseInt(Converters.getDecimalValue(data[index])) * 256) +
                    Integer.parseInt(Converters.getDecimalValue(data[index + 1]));
            table[i] = (baseFrequency * 1000) + frequency;
            i++;
            index += 2;
        }

        frequency_table.removeAllViews();
        showTable();
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
