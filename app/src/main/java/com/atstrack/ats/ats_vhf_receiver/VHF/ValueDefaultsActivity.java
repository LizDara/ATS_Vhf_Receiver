package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.SelectValueFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.StoreRateValuesFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.TablesScanFragment;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;

public class ValueDefaultsActivity extends BaseActivity {

    @BindView(R.id.tv_title_toolbar)
    TextView tv_title_toolbar;

    private int type;
    public int value;
    private ArrayList<Integer> tables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_fragment;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.lb_select_value);
        super.onCreate(savedInstanceState);

        type = getIntent().getIntExtra(ValueCodes.TYPE, 0);
        value = getIntent().getIntExtra(ValueCodes.VALUE, 0);
        setVisibility(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            Intent intent = new Intent();
            if (type == ValueCodes.TABLES_NUMBER_CODE) {
                int[] data = tables.stream().mapToInt(i -> i).toArray();
                intent.putExtra(ValueCodes.VALUE, data);
            } else {
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
        if (parameter == ValueCodes.TABLES_COMMAND) {
            TransferBleData.readTables();
            parameter = ValueCodes.NONE;
        }
    }

    @Override
    protected void downloadData(byte[] data) {
        super.downloadData(data);
        if (data[0] == ValueCodes.TABLES_COMMAND) { // Get frequency table number
            if (type == ValueCodes.TABLES_NUMBER_CODE) {
                tables = new ArrayList<>();
                int table = getIntent().getIntExtra(ValueCodes.FIRST_TABLE_NUMBER, 0);
                if (table != 0 && table <= 12)
                    tables.add(table);
                table = getIntent().getIntExtra(ValueCodes.SECOND_TABLE_NUMBER, 0);
                if (table != 0 && table <= 12)
                    tables.add(table);
                table = getIntent().getIntExtra(ValueCodes.THIRD_TABLE_NUMBER, 0);
                if (table != 0 && table <= 12)
                    tables.add(table);
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.fcv_activity_fragment, new TablesScanFragment(data, ValueCodes.STATIONARY_DEFAULTS_COMMAND, tables))
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.fcv_activity_fragment, new SelectValueFragment(type, value, data))
                        .commit();
            }
        }
    }

    private void setVisibility(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            switch (type) {
                case ValueCodes.STORE_RATE_CODE:
                    tv_title_toolbar.setText(R.string.store_rate);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new StoreRateValuesFragment(value))
                            .commit();
                    break;
                case ValueCodes.TABLES_NUMBER_CODE:
                case ValueCodes.TABLE_NUMBER_CODE:
                    tv_title_toolbar.setText(R.string.tables_scan);
                    parameter = ValueCodes.TABLES_COMMAND;
                    break;
                default:
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcv_activity_fragment, new SelectValueFragment(type, value))
                            .commit();
                    break;
            }
        }
    }
}