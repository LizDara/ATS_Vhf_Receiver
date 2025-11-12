package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.OnClick;

import android.app.AlertDialog;
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
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class FrequenciesActivity extends BaseActivity implements OnAdapterClickListener {

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
    @BindView(R.id.frequencies_recyclerView)
    RecyclerView frequencies_recyclerView;
    @BindView(R.id.frequencies_delete_listView)
    ListView frequencies_delete_listView;
    @BindView(R.id.edit_temperature_frequency_linearLayout)
    LinearLayout edit_temperature_frequency_linearLayout;
    @BindView(R.id.frequency_temperature_button)
    TextView frequency_temperature_button;
    @BindView(R.id.coefficient_a_button)
    Button coefficient_a_button;
    @BindView(R.id.coefficient_b_button)
    Button coefficient_b_button;
    @BindView(R.id.constant_button)
    Button constant_button;
    @BindView(R.id.save_frequency_button)
    Button save_frequency_button;

    final private String TAG = FrequenciesActivity.class.getSimpleName();

    private Handler handlerMessage;
    private FrequencyListAdapter frequencyListAdapter;
    private FrequencyDeleteListAdapter frequencyDeleteListAdapter;
    private int[] originalTable;
    private int number;
    private int originalTotalFrequencies;
    private int baseFrequency;
    private int range;
    private boolean isFile;
    private boolean isTemperature;
    private boolean saveCoefficients;
    private int temperaturePosition;
    private Map<String, String> coefficients;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    int position = result.getData().getIntExtra(ValueCodes.POSITION, 0);
                    if (position > -2) {
                        int frequency = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                        if (!isTemperature) { //Save frequency in the list
                            if (position != -1) {
                                if (frequencyListAdapter.getFrequency(position) != frequency)
                                    changeSelectedFrequency(frequency, position);
                            } else {
                                addFrequency(frequency);
                            }
                        } else { //Show new frequency
                            frequency_temperature_button.setText(String.valueOf(frequency));
                            save_frequency_button.setEnabled(isDataCorrect());
                            save_frequency_button.setAlpha(save_frequency_button.isEnabled() ? 1 : (float) 0.6);
                        }
                    } else if (position == -2) {
                        String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                        coefficient_a_button.setText(coefficient);
                        save_frequency_button.setEnabled(isDataCorrect());
                        save_frequency_button.setAlpha(save_frequency_button.isEnabled() ? 1 : (float) 0.6);
                    } else if (position == -3) {
                        String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                        coefficient_b_button.setText(coefficient);
                        save_frequency_button.setEnabled(isDataCorrect());
                        save_frequency_button.setAlpha(save_frequency_button.isEnabled() ? 1 : (float) 0.6);
                    } else if (position == -4) {
                        String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                        constant_button.setText(coefficient);
                        save_frequency_button.setEnabled(isDataCorrect());
                        save_frequency_button.setAlpha(save_frequency_button.isEnabled() ? 1 : (float) 0.6);
                    }
                }
            });

    private void readCoefficients() {
        TransferBleData.notificationLog();
        new Handler().postDelayed(() -> {
            parameter = ValueCodes.FREQUENCY_COEFFICIENTS;
            sendIndex();
        }, ValueCodes.WAITING_PERIOD);
    }

    private void sendIndex() {
        byte[] b = new byte[] {(byte) 0x7D, (byte) (temperaturePosition + 1), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        TransferBleData.writeFrequencies(number, b);
    }

    private void addTemperatureFrequency() {
        parameter = "";
        int frequency = Integer.parseInt(frequency_temperature_button.getText().toString());
        int coefficientA = Integer.parseInt(coefficient_a_button.getText().toString().replace("-", "")); //985
        int coefficientB = Integer.parseInt(coefficient_b_button.getText().toString().replace("-", "")); //-6121
        int constant = Integer.parseInt(constant_button.getText().toString().replace("-", "")); //11088
        //Coefficient D = 0
        byte formatA = coefficient_a_button.getText().toString().contains("-") ? (byte) 0x80 : 0;
        byte formatB = coefficient_b_button.getText().toString().contains("-") ? (byte) 0x80 : 0;
        byte formatC = constant_button.getText().toString().contains("-") ? (byte) 0x80 : 0;
        byte[] b = new byte[] { 0x7D, (byte) (temperaturePosition == -1 ? (frequencyListAdapter.getItemCount() + 1) : (temperaturePosition + 1)), (byte) ((frequency - baseFrequency) / 256), (byte) ((frequency - baseFrequency) % 256), formatA,
                (byte) (coefficientA / 256), (byte) (coefficientA % 256), formatB, (byte) (coefficientB / 256), (byte) (coefficientB % 256), formatC,
                (byte) (constant / 256), (byte) (constant % 256), 0, 0, 0};
        boolean result = TransferBleData.writeFrequencies(number, b);
        if (result) {
            saveCoefficients = true;
            if (temperaturePosition != -1)
                changeSelectedFrequency(frequency, temperaturePosition);
            else
                addFrequency(frequency);
        }
    }

    private void setTable() {
        parameter = "";
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm = currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);
        byte[] b = new byte[isTemperature ? 10 : 244];
        b[1] = (byte) (YY - 2000);
        b[2] = (byte) MM;
        b[3] = (byte) DD;
        b[4] = (byte) hh;
        b[5] = (byte) mm;
        b[6] = (byte) ss;
        b[7] = (byte) number;//frequency number table
        b[8] = (byte) frequencyListAdapter.getItemCount();//Number of frequencies in the table
        b[9] = (byte) (baseFrequency / 1000);//base frequency
        if (!isTemperature) {
            b[0] = (byte) 0x7E;
            int index = 10;
            int i = 0;
            while (i < frequencyListAdapter.getItemCount()) {
                b[index] = (byte) ((frequencyListAdapter.getFrequency(i) - baseFrequency) / 256);
                b[index + 1] = (byte) ((frequencyListAdapter.getFrequency(i) - baseFrequency) % 256);
                i++;
                index += 2;
            }
        } else {
            b[0] = (byte) 0x7F;
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
            if (isTemperature) {
                setVisibility("temperature");
                title_toolbar.setText(R.string.lb_add_frequency);
                frequency_temperature_button.setText("");
                coefficient_a_button.setText("0");
                coefficient_b_button.setText("0");
                constant_button.setText("0");
                temperaturePosition = -1;
                save_frequency_button.setEnabled(false);
                save_frequency_button.setAlpha((float) 0.6);
            } else {
                Intent intent = new Intent(this, EnterFrequencyActivity.class);
                intent.putExtra(ValueCodes.TITLE, "Add Frequency");
                intent.putExtra(ValueCodes.POSITION, -1);
                intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
                intent.putExtra(ValueCodes.RANGE, range);
                launcher.launch(intent);
            }
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

    @OnClick(R.id.frequency_temperature_button)
    public void onClickFrequencyTemperature(View v) {
        String title = frequency_temperature_button.getText().toString().isEmpty() ? getString(R.string.lb_add_frequency)
                : "Edit Frequency " + frequency_temperature_button.getText();
        Intent intent = new Intent(this, EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, title);
        intent.putExtra(ValueCodes.POSITION, temperaturePosition);
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.coefficient_a_button)
    public void onClickCoefficientA(View v) {
        Intent intent = new Intent(this, EnterCoefficientActivity.class);
        intent.putExtra(ValueCodes.TYPE, getString(R.string.lb_coefficient_a));
        launcher.launch(intent);
    }

    @OnClick(R.id.coefficient_b_button)
    public void onClickCoefficientB(View v) {
        Intent intent = new Intent(this, EnterCoefficientActivity.class);
        intent.putExtra(ValueCodes.TYPE, getString(R.string.lb_coefficient_b));
        launcher.launch(intent);
    }

    @OnClick(R.id.constant_button)
    public void onClickConstant(View v) {
        Intent intent = new Intent(this, EnterCoefficientActivity.class);
        intent.putExtra(ValueCodes.TYPE, getString(R.string.lb_constant));
        launcher.launch(intent);
    }

    @OnClick(R.id.save_frequency_button)
    public void onClickSaveFrequency(View v) {
        if (temperaturePosition != -1) {
            if (existChangesInCoefficients()) {
                addTemperatureFrequency();
            }
        } else {
            addTemperatureFrequency();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_frequencies;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        number = getIntent().getIntExtra(ValueCodes.TABLE_NUMBER, 0);
        originalTotalFrequencies = getIntent().getIntExtra(ValueCodes.TOTAL, 0);
        title = "Table " + number + " (" + originalTotalFrequencies + " Frequencies)";
        super.onCreate(savedInstanceState);

        initializeCallback();
        handlerMessage = new Handler();
        baseFrequency = getIntent().getIntExtra(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = getIntent().getIntExtra(ValueCodes.RANGE, 0);
        isFile = getIntent().getBooleanExtra(ValueCodes.IS_FILE, false);
        isTemperature = getIntent().getBooleanExtra(ValueCodes.IS_TEMPERATURE, false);
        saveCoefficients = !isTemperature;
        if (isFile) { // Ask for the frequencies obtained from a file
            setVisibility("overview");
            originalTable = getIntent().getIntArrayExtra(ValueCodes.FREQUENCIES);
            ArrayList<Integer> frequencies = new ArrayList<>();
            for (int frequency : originalTable)
                frequencies.add(frequency);

            frequencyListAdapter = new FrequencyListAdapter(this, frequencies, baseFrequency, range, launcher, isTemperature, this);
            frequencies_recyclerView.setAdapter(frequencyListAdapter);
            frequencies_recyclerView.setLayoutManager(new LinearLayoutManager(this));
            frequencyDeleteListAdapter = new FrequencyDeleteListAdapter(this, frequencies, all_frequencies_checkBox, delete_selected_frequencies_button);
            frequencies_delete_listView.setAdapter(frequencyDeleteListAdapter);
        } else { // Ask for the frequencies from that table, the frequency base and range
            if (originalTotalFrequencies > 0) { // Ask for the frequencies from the table
                parameter = ValueCodes.TABLE;
                setVisibility("overview");
            } else { // Initializes empty
                originalTable = new int[]{};
                ArrayList<Integer> frequencies = new ArrayList<>();
                frequencyListAdapter = new FrequencyListAdapter(this, frequencies, baseFrequency, range, launcher, isTemperature, this);
                frequencies_recyclerView.setAdapter(frequencyListAdapter);
                frequencies_recyclerView.setLayoutManager(new LinearLayoutManager(this));
                frequencyDeleteListAdapter = new FrequencyDeleteListAdapter(this, frequencies, all_frequencies_checkBox, delete_selected_frequencies_button);
                frequencies_delete_listView.setAdapter(frequencyDeleteListAdapter);
                setVisibility("none");
            }
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
                Log.i(TAG, "Parameter: "+parameter);
                if (parameter.equals(ValueCodes.TABLE)) // Get the frequencies from a table
                    TransferBleData.readFrequencies(number);
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                if (Converters.getHexValue(packet[0]).equals("88")) // Battery
                    setBatteryPercent(packet);
                else if (Converters.getHexValue(packet[0]).equals("56")) // Sd Card
                    setSdCardStatus(packet);
                else if (parameter.equals(ValueCodes.TABLE)) // Gets the frequencies from a table
                    downloadData(packet);
                else if (parameter.equals(ValueCodes.FREQUENCY_COEFFICIENTS))
                    downloadCoefficients(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (frequencies_overview_linearLayout.getVisibility() == View.VISIBLE
                    || no_frequencies_linearLayout.getVisibility() == View.VISIBLE) {
                if ((isFile || existChanges()) && saveCoefficients)
                    setTable();
                else
                    finish();
            } else if (delete_frequencies_linearLayout.getVisibility() == View.VISIBLE ||
                    edit_temperature_frequency_linearLayout.getVisibility() == View.VISIBLE) {
                title_toolbar.setText("Table " + number + " (" + frequencyListAdapter.getItemCount() + " Frequencies)");
                if (frequencyListAdapter.getItemCount() > 0) {
                    setVisibility("overview");
                    changeAllCheckBox(false);
                } else {
                    setVisibility("none");
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAdapterItemClickListener(int position) {
        temperaturePosition = position;
        readCoefficients();
    }

    /**
     * Checks that the number of frequencies is not greater than 100.
     * @return Returns true, if the number of frequencies is less than or equal to 100.
     */
    private boolean isWithinLimit() {
        return frequencyListAdapter.getItemCount() < 100;
    }

    private void setVisibility(String value) {
        switch (value) {
            case "overview":
                frequencies_overview_linearLayout.setVisibility(View.VISIBLE);
                no_frequencies_linearLayout.setVisibility(View.GONE);
                delete_frequencies_linearLayout.setVisibility(View.GONE);
                edit_temperature_frequency_linearLayout.setVisibility(View.GONE);
                break;
            case "none":
                frequencies_overview_linearLayout.setVisibility(View.GONE);
                no_frequencies_linearLayout.setVisibility(View.VISIBLE);
                delete_frequencies_linearLayout.setVisibility(View.GONE);
                edit_temperature_frequency_linearLayout.setVisibility(View.GONE);
                break;
            case "delete":
                frequencies_overview_linearLayout.setVisibility(View.GONE);
                no_frequencies_linearLayout.setVisibility(View.GONE);
                delete_frequencies_linearLayout.setVisibility(View.VISIBLE);
                edit_temperature_frequency_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_delete_frequencies);
                break;
            case "temperature":
                frequencies_overview_linearLayout.setVisibility(View.GONE);
                no_frequencies_linearLayout.setVisibility(View.GONE);
                delete_frequencies_linearLayout.setVisibility(View.GONE);
                edit_temperature_frequency_linearLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * With the received packet, gets the frequencies from the table and display on the screen.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Integer.parseInt(Converters.getDecimalValue(data[0])) == number) {
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
            frequencyListAdapter = new FrequencyListAdapter(this, frequencies, baseFrequency, range, launcher, isTemperature, this);
            frequencies_recyclerView.setAdapter(frequencyListAdapter);
            frequencies_recyclerView.setLayoutManager(new LinearLayoutManager(this));
            frequencyDeleteListAdapter = new FrequencyDeleteListAdapter(this, frequencies, all_frequencies_checkBox, delete_selected_frequencies_button);
            frequencies_delete_listView.setAdapter(frequencyDeleteListAdapter);
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x" + number + " ...");
        }
    }

    private void downloadCoefficients(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("7D")) {
            parameter = "";
            title_toolbar.setText(R.string.lb_edit_frequency);
            frequency_temperature_button.setText(String.valueOf(frequencyListAdapter.getFrequency(temperaturePosition)));

            if (!Converters.areCoefficientsEmpty(data)) {
                int coefficientA = (Integer.parseInt(Converters.getDecimalValue(data[5])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[6]));
                int coefficientB = (Integer.parseInt(Converters.getDecimalValue(data[8])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[9]));
                int constant = (Integer.parseInt(Converters.getDecimalValue(data[11])) * 256) + Integer.parseInt(Converters.getDecimalValue(data[12]));
                coefficient_a_button.setText(Converters.getHexValue(data[4]).equals("80") ? "-" + coefficientA : String.valueOf(coefficientA));
                coefficient_b_button.setText(Converters.getHexValue(data[7]).equals("80") ? "-" + coefficientB : String.valueOf(coefficientB));
                constant_button.setText(Converters.getHexValue(data[10]).equals("80") ? "-" + constant : String.valueOf(constant));
                save_frequency_button.setEnabled(true);
                save_frequency_button.setAlpha(1);
            } else {
                coefficient_a_button.setText("0");
                coefficient_b_button.setText("0");
                constant_button.setText("0");
                save_frequency_button.setEnabled(false);
                save_frequency_button.setAlpha((float) 0.6);
            }
            setVisibility("temperature");

            coefficients = new HashMap<>();
            coefficients.put(ValueCodes.FREQUENCY_COEFFICIENTS, frequency_temperature_button.getText().toString());
            coefficients.put(ValueCodes.COEFFICIENT_A, coefficient_a_button.getText().toString());
            coefficients.put(ValueCodes.COEFFICIENT_B, coefficient_b_button.getText().toString());
            coefficients.put(ValueCodes.COEFFICIENT_C, constant_button.getText().toString());
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x7D ...");
        }
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

        manageMessage(R.string.lb_frequencies_deleted, frequencyListAdapter.getItemCount() == 0);
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
            title_toolbar.setText("Table " + number + " (" + frequencyListAdapter.getItemCount() + " Frequencies)");
        }, ValueCodes.MESSAGE_PERIOD);
    }

    private boolean existChanges() {
        int count = frequencyListAdapter == null ? 0 : frequencyListAdapter.getItemCount();
        if (originalTable.length != count)
            return true;
        for (int i = 0; i < originalTable.length; i++) {
            if (originalTable[i] != frequencyListAdapter.getFrequency(i))
                return true;
        }
        return false;
    }

    private boolean existChangesInCoefficients() {
        return !coefficients.get(ValueCodes.FREQUENCY_COEFFICIENTS).equals(frequency_temperature_button.getText().toString())
                || !coefficients.get(ValueCodes.COEFFICIENT_A).equals(coefficient_a_button.getText().toString())
                || !coefficients.get(ValueCodes.COEFFICIENT_B).equals(coefficient_b_button.getText().toString())
                || !coefficients.get(ValueCodes.COEFFICIENT_C).equals(constant_button.getText().toString());
    }

    private boolean isDataCorrect() {
        return !frequency_temperature_button.getText().toString().isEmpty() && !coefficient_a_button.getText().toString().equals("0")
                && !coefficient_b_button.getText().toString().equals("0") && !constant_button.getText().toString().equals("0");
    }
}