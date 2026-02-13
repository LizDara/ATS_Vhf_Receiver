package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
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
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_value_defaults;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.lb_select_value);
        super.onCreate(savedInstanceState);

        type = getIntent().getIntExtra(ValueCodes.TYPE, 0);
        switch (type) {
            case ValueCodes.STORE_RATE_CODE: // Get the store rate
                setVisibility("storeRate");
                int storeRate = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setStoreRate(storeRate);
                break;
            case ValueCodes.TABLE_NUMBER_CODE: // Get the frequency table number
                setVisibility("");
                parameter = ValueCodes.TABLES;
                break;
            case ValueCodes.TABLES_NUMBER_CODE: // Get the frequency tables number
                setVisibility("tables");
                parameter = ValueCodes.TABLES;
                break;
            case ValueCodes.SCAN_RATE_MOBILE_CODE: // Get the scan rate mobile
                setVisibility("");
                double scanRateMobile = getIntent().getDoubleExtra(ValueCodes.VALUE, 0);
                setScanRate((int)(scanRateMobile * 10));
                break;
            case ValueCodes.SCAN_RATE_STATIONARY_CODE: // Get the scan rate mobile
                setVisibility("");
                int scanRateStationary = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setScanRate(scanRateStationary);
                break;
            case ValueCodes.NUMBER_OF_ANTENNAS_CODE: // Get number of antennas
                setVisibility("");
                int antenna = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setAntennas(antenna);
                break;
            case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE: // Get the scan timeout seconds
                setVisibility("");
                int timeout = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setTimeout(timeout);
                break;
            case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE: // Get the reference frequency store rate
                setVisibility("");
                int referenceStoreRate = getIntent().getIntExtra(ValueCodes.VALUE, 0);
                setReferenceFrequencyStoreRate(referenceStoreRate);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            Intent intent = new Intent();
            if (type == ValueCodes.TABLES_NUMBER_CODE) {
                int[] tables = new int[tableScanListAdapter.getCountSelected()];
                for (int i = 0; i < tableScanListAdapter.getCountSelected(); i++)
                    tables[i] = tableScanListAdapter.getSelected(i);
                intent.putExtra(ValueCodes.VALUE, tables);
            } else {
                int value = 0;
                if (type == ValueCodes.SCAN_RATE_MOBILE_CODE) // Send the mobile scan rate value
                    value = (int) (Float.parseFloat(value_spinner.getSelectedItem().toString()) * 10);
                else if (type == ValueCodes.SCAN_RATE_STATIONARY_CODE) // Send the mobile scan rate value
                    value = Integer.parseInt(value_spinner.getSelectedItem().toString());
                else if (type == ValueCodes.TABLE_NUMBER_CODE) // Send the frequency table number
                    value = (value_spinner.getSelectedItem().toString().equals("None")) ? 0 :
                            Integer.parseInt(value_spinner.getSelectedItem().toString().replace("Table ", ""));
                else if (type == ValueCodes.NUMBER_OF_ANTENNAS_CODE) // Send the number of antennas
                    value = value_spinner.getSelectedItemPosition() + 1;
                else if (type == ValueCodes.SCAN_TIMEOUT_SECONDS_CODE) // Send scan timeout value
                    value = Integer.parseInt(value_spinner.getSelectedItem().toString());
                else if (type == ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE)
                    value = value_spinner.getSelectedItemPosition();
                else if (type == ValueCodes.STORE_RATE_CODE) {
                    value = storeRate;
                }
                intent.putExtra(ValueCodes.VALUE, value);
            }
            setResult(type, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void discoverCharacteristic() {
        if (parameter.equals(ValueCodes.TABLES))
            TransferBleData.readTables();
    }

    @Override
    protected void downloadData(byte[] data) {
        super.downloadData(data);
        if (Converters.getHexValue(data[0]).equals("7A")) { // Get frequency table number
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
                int table = getIntent().getIntExtra(ValueCodes.VALUE, 0);
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
        }
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
                merge_tables_button.setVisibility(View.GONE);
                title_toolbar.setText(R.string.tables_scan);
                break;
            default:
                set_value_linearLayout.setVisibility(View.VISIBLE);
                store_rate_linearLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void setScanRate(int scanRate) {
        if (type == ValueCodes.SCAN_RATE_MOBILE_CODE) { // Mobile scan rate
            ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(this, R.array.scanRateAerial, android.R.layout.simple_spinner_item);
            scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            value_spinner.setAdapter(scanRateAdapter);

            int index = 0;
            for (int i = 0; i < 49; i++) {
                String item = value_spinner.getItemAtPosition(i).toString().replace(".", "");
                if (item.equals(String.valueOf(scanRate))) {
                    index = i;
                    break;
                }
            }
            value_spinner.setSelection(index);
        } else { // Stationary scan rate
            ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(this, R.array.scanRateStationary, android.R.layout.simple_spinner_item);
            scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            value_spinner.setAdapter(scanRateAdapter);

            if (scanRate <= 255)
                value_spinner.setSelection(scanRate - 3);
            else
                value_spinner.setSelection(0);
        }
    }

    private void setAntennas(int antenna) {
        ArrayAdapter<CharSequence> antennasAdapter = ArrayAdapter.createFromResource(this, R.array.antennas, android.R.layout.simple_spinner_item);
        antennasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        value_spinner.setAdapter(antennasAdapter);

        if (antenna <= 4 && antenna > 0)
            value_spinner.setSelection(antenna - 1);
        else
            value_spinner.setSelection(0);
    }

    private void setTimeout(int timeout) {
        ArrayAdapter<CharSequence> timeoutAdapter = ArrayAdapter.createFromResource(this, R.array.timeout, android.R.layout.simple_spinner_item);
        timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        value_spinner.setAdapter(timeoutAdapter);

        if (timeout <= 200)
            value_spinner.setSelection(timeout - 3);
        else
            value_spinner.setSelection(0);
    }

    private void setStoreRate(int storeRate) {
        switch (storeRate) {
            case 0:
                continuous_store_imageView.setVisibility(View.VISIBLE);
                break;
            case 5:
                five_minutes_imageView.setVisibility(View.VISIBLE);
                break;
            case 10:
                ten_minutes_imageView.setVisibility(View.VISIBLE);
                break;
            case 20:
                fifteen_minutes_imageView.setVisibility(View.VISIBLE);
                break;
            case 30:
                thirty_minutes_imageView.setVisibility(View.VISIBLE);
                break;
            case 60:
                sixty_minutes_imageView.setVisibility(View.VISIBLE);
                break;
        }
        this.storeRate = storeRate;
    }

    private void setReferenceFrequencyStoreRate(int storeRate) {
        ArrayAdapter<CharSequence> storeRateAdapter = ArrayAdapter.createFromResource(this, R.array.referenceFrequencyStoreRate, android.R.layout.simple_spinner_item);
        storeRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        value_spinner.setAdapter(storeRateAdapter);

        if (storeRate <= 24)
            value_spinner.setSelection(storeRate);
        else
            value_spinner.setSelection(0);
    }
}