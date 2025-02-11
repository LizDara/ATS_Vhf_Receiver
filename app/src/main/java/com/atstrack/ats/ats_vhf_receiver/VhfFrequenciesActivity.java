package com.atstrack.ats.ats_vhf_receiver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyDeleteListAdapter;
import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class VhfFrequenciesActivity extends AppCompatActivity {

    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
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

    final private String TAG = VhfFrequenciesActivity.class.getSimpleName();

    private final Context mContext = this;
    private Handler handlerMessage;
    private FrequencyListAdapter frequencyListAdapter;
    private FrequencyDeleteListAdapter frequencyDeleteListAdapter;
    private int[] originalTable;
    private int number;
    private int originalTotalFrequencies;
    private int baseFrequency;
    private int range;
    private boolean isFile;

    private final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();
    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    Message.showDisconnectionMessage(mContext, status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    switch (parameter) {
                        case ValueCodes.TABLE: // Gets the frequencies from a table
                            TransferBleData.readFrequencies(number);
                            break;
                        case ValueCodes.SAVE: // Sends the modified frequencies
                            setTable();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals(ValueCodes.TABLE)) // Gets the frequencies from a table
                        downloadData(packet);
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
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    int position = result.getData().getExtras().getInt(ValueCodes.POSITION);
                    int frequency = result.getData().getExtras().getInt(ValueCodes.VALUE);
                    if (position != -1) {
                        if (frequencyListAdapter.getFrequency(position) != frequency)
                            changeSelectedFrequency(frequency, position);
                    } else {
                        addFrequency(frequency);
                    }
                }
            });

    private void setTable() {
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
        boolean result = TransferBleData.writeFrequencies(number, b);
        if (result)
            Message.showMessage(this, 0);
        else
            Message.showMessage(this, 2);
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
            Intent intent = new Intent(this, VhfEnterFrequencyActivity.class);
            intent.putExtra(ValueCodes.TITLE, "Add Frequency");
            intent.putExtra(ValueCodes.POSITION, -1);
            intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
            intent.putExtra(ValueCodes.RANGE, range);
            launcher.launch(intent);
        } else {
            Message.showMessage(this, 3);
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
        setContentView(R.layout.activity_vhf_frequencies);
        ButterKnife.bind(this);
        number = getIntent().getIntExtra(ValueCodes.TABLE_NUMBER, 0);
        originalTotalFrequencies = getIntent().getIntExtra(ValueCodes.TOTAL, 0);
        ActivitySetting.setToolbar(this, "Table " + number + " (" + originalTotalFrequencies + " Frequencies)");
        ActivitySetting.setReceiverStatus(this);

        handlerMessage = new Handler();
        baseFrequency = getIntent().getIntExtra(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = getIntent().getIntExtra(ValueCodes.RANGE, 0);
        isFile = getIntent().getBooleanExtra(ValueCodes.IS_FILE, false);
        if (isFile) { // Ask for the frequencies obtained from a file
            setVisibility("overview");
            originalTable = getIntent().getIntArrayExtra(ValueCodes.FREQUENCIES);
            ArrayList<Integer> frequencies = new ArrayList<>();
            for (int frequency : originalTable)
                frequencies.add(frequency);

            frequencyListAdapter = new FrequencyListAdapter(this, frequencies, baseFrequency, range, launcher);
            frequencies_listView.setAdapter(frequencyListAdapter);
            frequencyDeleteListAdapter = new FrequencyDeleteListAdapter(this, frequencies, all_frequencies_checkBox, delete_selected_frequencies_button);
            frequencies_delete_listView.setAdapter(frequencyDeleteListAdapter);
        } else { // Ask for the frequencies from that table, the frequency base and range
            if (originalTotalFrequencies > 0) { // Ask for the frequencies from the table
                parameter = ValueCodes.TABLE;
                setVisibility("overview");
            } else { // Initializes empty
                originalTable = new int[]{};
                ArrayList<Integer> frequencies = new ArrayList<>();
                frequencyListAdapter = new FrequencyListAdapter(this, frequencies, baseFrequency, range, launcher);
                frequencies_listView.setAdapter(frequencyListAdapter);
                frequencyDeleteListAdapter = new FrequencyDeleteListAdapter(this, frequencies, all_frequencies_checkBox, delete_selected_frequencies_button);
                frequencies_delete_listView.setAdapter(frequencyDeleteListAdapter);
                setVisibility("none");
            }
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (frequencies_overview_linearLayout.getVisibility() == View.VISIBLE
                    || no_frequencies_linearLayout.getVisibility() == View.VISIBLE) {
                if (isFile || existChanges()) {
                    parameter = ValueCodes.SAVE;
                    leServiceConnection.getBluetoothLeService().discovering();
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
        registerReceiver(mGattUpdateReceiver, Converters.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (!isFile) {
            //unbindService(leServiceConnection.getServiceConnection());
        //}
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
    }

    /**
     * Checks that the number of frequencies is not greater than 100.
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
            if (frequencyDeleteListAdapter.isSelected(index))
                frequencyDeleteListAdapter.removeFrequency(index);
            else
                index++;
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

        handlerMessage.postDelayed(() -> {
            dialog.dismiss();
            if (isEmpty)
                setVisibility("none");
            else
                setVisibility("overview");
            title_toolbar.setText("Table " + number + " (" + frequencyListAdapter.getCount() + " Frequencies)");
        }, ValueCodes.MESSAGE_PERIOD);
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
}