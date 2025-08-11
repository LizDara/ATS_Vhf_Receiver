package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableScanListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.List;

public class ValueDefaultsActivity extends BaseActivity {

    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.value_spinner)
    Spinner value_spinner;
    @BindView(R.id.set_value_linearLayout)
    LinearLayout set_value_linearLayout;
    @BindView(R.id.store_rate_linearLayout)
    LinearLayout store_rate_linearLayout;
    @BindView(R.id.continuous_store_imageView)
    ImageView continuous_store_imageView;
    @BindView(R.id.five_minutes_imageView)
    ImageView five_minutes_imageView;
    @BindView(R.id.ten_minutes_imageView)
    ImageView ten_minutes_imageView;
    @BindView(R.id.fifteen_minutes_imageView)
    ImageView fifteen_minutes_imageView;
    @BindView(R.id.thirty_minutes_imageView)
    ImageView thirty_minutes_imageView;
    @BindView(R.id.sixty_minutes_imageView)
    ImageView sixty_minutes_imageView;
    @BindView(R.id.one_hundred_twenty_minutes_imageView)
    ImageView one_hundred_twenty_minutes_imageView;
    @BindView(R.id.merge_tables_linearLayout)
    LinearLayout merge_tables_linearLayout;
    @BindView(R.id.option_tables_textView)
    TextView option_tables_textView;
    @BindView(R.id.tables_merge_listView)
    ListView tables_merge_listView;
    @BindView(R.id.merge_tables_button)
    Button merge_tables_button;

    private final static String TAG = ValueDefaultsActivity.class.getSimpleName();

    private TableScanListAdapter tableScanListAdapter;
    private int type;
    private int storeRate = 0;

    @OnClick(R.id.continuous_store_linearLayout)
    public void onClickContinuousStore(View v) {
        continuous_store_imageView.setVisibility(View.VISIBLE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        fifteen_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        one_hundred_twenty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 100;
    }

    @OnClick(R.id.five_minutes_linearLayout)
    public void onClickFiveMinutes(View v) {
        continuous_store_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.VISIBLE);
        ten_minutes_imageView.setVisibility(View.GONE);
        fifteen_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        one_hundred_twenty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 5;
    }

    @OnClick(R.id.ten_minutes_linearLayout)
    public void onClickTenMinutes(View v) {
        continuous_store_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.VISIBLE);
        fifteen_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        one_hundred_twenty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 10;
    }

    @OnClick(R.id.fifteen_minutes_linearLayout)
    public void onClickFifteenMinutes(View v) {
        continuous_store_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        fifteen_minutes_imageView.setVisibility(View.VISIBLE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        one_hundred_twenty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 15;
    }

    @OnClick(R.id.thirty_minutes_linearLayout)
    public void onClickThirtyMinutes(View v) {
        continuous_store_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        fifteen_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.VISIBLE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        one_hundred_twenty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 30;
    }

    @OnClick(R.id.sixty_minutes_linearLayout)
    public void onClickSixtyMinutes(View v) {
        continuous_store_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        fifteen_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.VISIBLE);
        one_hundred_twenty_minutes_imageView.setVisibility(View.GONE);
        storeRate = 60;
    }

    @OnClick(R.id.one_hundred_twenty_minutes_linearLayout)
    public void onClickOneHundredTwentyMinutes(View v) {
        continuous_store_imageView.setVisibility(View.GONE);
        five_minutes_imageView.setVisibility(View.GONE);
        ten_minutes_imageView.setVisibility(View.GONE);
        fifteen_minutes_imageView.setVisibility(View.GONE);
        thirty_minutes_imageView.setVisibility(View.GONE);
        sixty_minutes_imageView.setVisibility(View.GONE);
        one_hundred_twenty_minutes_imageView.setVisibility(View.VISIBLE);
        storeRate = 120;
    }

    @OnClick(R.id.save_changes_input_value_button)
    public void onClickSaveChanges(View v) {
        Intent intent = new Intent();
        int value = 0;
        if (parameter.equals(ValueCodes.MOBILE_DEFAULTS) && type == ValueCodes.SCAN_RATE_SECONDS_CODE) // Sends the scan rate value for aerial
            value = (int) (Float.parseFloat(value_spinner.getSelectedItem().toString()) * 10);
        else if (parameter.equals(ValueCodes.STATIONARY_DEFAULTS) && type == ValueCodes.SCAN_RATE_SECONDS_CODE) // Sends the scan rate value for stationary
            value = Integer.parseInt(value_spinner.getSelectedItem().toString());
        else if (type == ValueCodes.TABLE_NUMBER_CODE) // Sends the frequency table number
            value = (value_spinner.getSelectedItem().toString().equals("None")) ? 0 :
                    Integer.parseInt(value_spinner.getSelectedItem().toString().replace("Table ", ""));
        else if (type == ValueCodes.NUMBER_OF_ANTENNAS_CODE) // Sends the number of antennas
            value = value_spinner.getSelectedItemPosition() + 1;
        else if (type == ValueCodes.SCAN_TIMEOUT_SECONDS_CODE) // Sends scan timeout value
            value = Integer.parseInt(value_spinner.getSelectedItem().toString());
        else if (type == ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE)
            value = value_spinner.getSelectedItemPosition();
        intent.putExtra(ValueCodes.VALUE, value);
        setResult(type, intent);
        finish();
    }

    @OnClick(R.id.merge_tables_button)
    public void onClickSaveTables(View v) {
        Intent intent = new Intent();
        int[] tables = new int[tableScanListAdapter.getCountSelected()];
        for (int i = 0; i < tableScanListAdapter.getCountSelected(); i++)
            tables[i] = tableScanListAdapter.getSelected(i);
        intent.putExtra(ValueCodes.VALUE, tables);
        setResult(type, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_value_defaults;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.lb_set_value);
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = getIntent().getStringExtra(ValueCodes.PARAMETER);
        type = getIntent().getIntExtra(ValueCodes.TYPE, 0);
        if (type == ValueCodes.STORE_RATE_CODE)
            setVisibility("storeRate");
        else if (type == ValueCodes.TABLES_NUMBER_CODE)
            setVisibility("tables");
        else
            setVisibility("");
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
                    case ValueCodes.MOBILE_DEFAULTS:  // Gets aerial defaults data
                        TransferBleData.readDefaults(true);
                        break;
                    case ValueCodes.STATIONARY_DEFAULTS:  // Gets stationary defaults data
                        TransferBleData.readDefaults(false);
                        break;
                    case ValueCodes.TABLES:
                        TransferBleData.readTables(false);
                        break;
                }
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (Converters.getHexValue(packet[0]).equals("88")) return;
                switch (type) {
                    case ValueCodes.TABLE_NUMBER_CODE: // Gets the frequency table number
                    case ValueCodes.TABLES_NUMBER_CODE:
                        downloadTable(packet);
                        break;
                    case ValueCodes.SCAN_RATE_SECONDS_CODE: // Gets the scan rate seconds
                        downloadScanRate(packet);
                        break;
                    case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Gets number of antennas
                        downloadAntennas(packet);
                        break;
                    case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Gets the scan timeout seconds
                        downloadTimeout(packet);
                        break;
                    case ValueCodes.STORE_RATE_CODE: // Gets the store rate
                        downloadStoreRate(packet);
                        break;
                    case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE: // Gets the reference frequency store rate
                        downloadReferenceFrequencyStoreRate(packet);
                        break;
                }
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (type == ValueCodes.STORE_RATE_CODE) {
                Intent intent = new Intent();
                intent.putExtra(ValueCodes.VALUE, storeRate);
                setResult(type, intent);
            } else {
                setResult(ValueCodes.CANCELLED);
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 33)
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter(), 2);
        else
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
    }

    private void setVisibility(String value) {
        switch (value) {
            case "storeRate":
                store_rate_linearLayout.setVisibility(View.VISIBLE);
                set_value_linearLayout.setVisibility(View.GONE);
                merge_tables_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.store_rate);
                break;
            case "tables":
                merge_tables_linearLayout.setVisibility(View.VISIBLE);
                store_rate_linearLayout.setVisibility(View.GONE);
                set_value_linearLayout.setVisibility(View.GONE);
                merge_tables_button.setText(R.string.lb_save_changes);
                title_toolbar.setText(R.string.tables_scan);
                break;
            default:
                set_value_linearLayout.setVisibility(View.VISIBLE);
                store_rate_linearLayout.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * With the received packet, gets frequency table number and display on the screen.
     * @param data The received packet.
     */
    private void downloadTable(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("7A")) {
            if (type == ValueCodes.TABLES_NUMBER_CODE) {
                ArrayList<Integer> tables = new ArrayList<>();
                int table = getIntent().getIntExtra(ValueCodes.FIRST_TABLE_NUMBER, 0);
                if (table != 0 && table <= 12)
                    tables.add(table);
                table = getIntent().getIntExtra(ValueCodes.SECOND_TABLE_NUMBER, 0);
                if (table != 0 && table <= 12)
                    tables.add(table);
                table = getIntent().getIntExtra(ValueCodes.THIRD_TABLE_NUMBER, 0);
                if (table != 0 && table <= 12)
                    tables.add(table);
                option_tables_textView.setText(tables.size() + " Selected Tables (3 Max)");
                tableScanListAdapter = new TableScanListAdapter(this, data, tables, option_tables_textView, merge_tables_button);
                tables_merge_listView.setAdapter(tableScanListAdapter);
            } else {
                int table = getIntent().getIntExtra("table", 0);
                List<String> tables = new ArrayList<>();
                int position = 0;
                for (int i = 1; i <= 12; i++) {
                    if (data[i] > 0) {
                        tables.add("Table " + i);
                        if (table == i) position = tables.size() - 1;
                    }
                }
                if (tables.isEmpty())
                    tables.add("None");
                ArrayAdapter<String> tablesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tables);
                tablesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                value_spinner.setAdapter(tablesAdapter);
                value_spinner.setSelection(position);
            }
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x7A ...");
        }
    }

    /**
     * With the received packet, gets scan rate value and display on the screen.
     * @param data The received packet.
     */
    private void downloadScanRate(byte[] data) {
        if (parameter.equals(ValueCodes.MOBILE_DEFAULTS)) {
            if (Converters.getHexValue(data[0]).equals("6D")) {
                ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(this, R.array.scanRateAerial, android.R.layout.simple_spinner_item);
                scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                value_spinner.setAdapter(scanRateAdapter);

                int index = 0;
                for (int i = 0; i < 49; i++) {
                    String item = value_spinner.getItemAtPosition(i).toString().replace(".", "");
                    if (item.equals(Converters.getDecimalValue(data[3]))) {
                        index = i;
                        break;
                    }
                }
                value_spinner.setSelection(index);
            } else {
                Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6D ...");
            }
        } else {
            if (Converters.getHexValue(data[0]).equals("6C")) {
                ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(this, R.array.scanRateStationary, android.R.layout.simple_spinner_item);
                scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                value_spinner.setAdapter(scanRateAdapter);

                int scanRate = Integer.parseInt(Converters.getDecimalValue(data[3]));
                if (scanRate <= 255)
                    value_spinner.setSelection(scanRate - 3);
                else
                    value_spinner.setSelection(0);
            } else {
                Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6C ...");
            }
        }
    }

    /**
     * With the received packet, gets number of antennas and display on the screen.
     * @param data The received packet.
     */
    private void downloadAntennas(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            ArrayAdapter<CharSequence> antennasAdapter = ArrayAdapter.createFromResource(this, R.array.antennas, android.R.layout.simple_spinner_item);
            antennasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            value_spinner.setAdapter(antennasAdapter);

            int antennaNumber = Integer.parseInt(Converters.getDecimalValue(data[2])) & 15;
            if (antennaNumber <= 4 && antennaNumber > 0)
                value_spinner.setSelection(antennaNumber - 1);
            else
                value_spinner.setSelection(0);
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6C ...");
        }
    }

    /**
     * With the received packet, gets scan timeout value and display on the screen.
     * @param data The received packet.
     */
    private void downloadTimeout(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            ArrayAdapter<CharSequence> timeoutAdapter = ArrayAdapter.createFromResource(this, R.array.timeout, android.R.layout.simple_spinner_item);
            timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            value_spinner.setAdapter(timeoutAdapter);

            int timeout = Integer.parseInt(Converters.getDecimalValue(data[4]));
            if (timeout <= 200)
                value_spinner.setSelection(timeout - 2);
            else
                value_spinner.setSelection(0);
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6C ...");
        }
    }

    /**
     * With the received packet, gets store rate value and display on the screen.
     * @param data The received packet.
     */
    private void downloadStoreRate(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            switch (Converters.getDecimalValue(data[5])) {
                case "0":
                    continuous_store_imageView.setVisibility(View.VISIBLE);
                    break;
                case "5":
                    five_minutes_imageView.setVisibility(View.VISIBLE);
                    break;
                case "10":
                    ten_minutes_imageView.setVisibility(View.VISIBLE);
                    break;
                case "20":
                    fifteen_minutes_imageView.setVisibility(View.VISIBLE);
                    break;
                case "30":
                    thirty_minutes_imageView.setVisibility(View.VISIBLE);
                    break;
                case "60":
                    sixty_minutes_imageView.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6C ...");
        }
    }

    /**
     * With the received packet, gets reference frequency store rate value and display on the screen.
     * @param data The received packet.
     */
    private void downloadReferenceFrequencyStoreRate(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            ArrayAdapter<CharSequence> storeRateAdapter = ArrayAdapter.createFromResource(this, R.array.referenceFrequencyStoreRate, android.R.layout.simple_spinner_item);
            storeRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            value_spinner.setAdapter(storeRateAdapter);

            int referenceFrequencyStoreRate = Integer.parseInt(Converters.getDecimalValue(data[8]));
            if (referenceFrequencyStoreRate <= 24)
                value_spinner.setSelection(referenceFrequencyStoreRate);
            else
                value_spinner.setSelection(0);
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6C ...");
        }
    }
}