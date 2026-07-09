package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.fragment.app.Fragment;

import butterknife.BindView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.EmptyTableFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.FrequenciesOverviewFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class FrequenciesActivity extends BaseActivity implements OnDialogCreatedListener {

    @BindView(R.id.tv_title_toolbar)
    TextView tv_title_toolbar;

    private FrequencyAdapter frequencyAdapter;

    private void setTable() {
        byte[] b = Converters.setCalendar(frequencyAdapter.isTemperature ? 10 : 244);
        b[7] = (byte) frequencyAdapter.tableNumber;
        b[8] = (byte) frequencyAdapter.getItemCount();
        b[9] = (byte) (frequencyAdapter.baseFrequency / 1000);
        if (!frequencyAdapter.isTemperature) {
            b[0] = (byte) 0x7E;
            int index = 10;
            int i = 0;
            while (i < frequencyAdapter.getItemCount()) {
                b[index] = (byte) ((frequencyAdapter.frequencies.get(i) - frequencyAdapter.baseFrequency) / 256);
                b[index + 1] = (byte) ((frequencyAdapter.frequencies.get(i) - frequencyAdapter.baseFrequency) % 256);
                index += 2;
                i++;
            }
        } else {
            b[0] = (byte) 0x7F;
        }
        boolean result = TransferBleData.writeFrequencies(b);
        if (result)
            showAlertDialog("Message!", "Save successfully.", true);
        else
            showAlertDialog("Error", "Not saved.", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_fragment;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        int tableNumber = getIntent().getIntExtra(ValueCodes.TABLE, 0);
        int total = getIntent().getIntExtra(ValueCodes.TOTAL, 0);
        title = "Table " + tableNumber + " (" + total + " Frequencies)";
        super.onCreate(savedInstanceState);

        int baseFrequency = getIntent().getIntExtra(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        int range = getIntent().getIntExtra(ValueCodes.RANGE, 0);
        boolean isTemperature = getIntent().getBooleanExtra(ValueCodes.IS_TEMPERATURE, false);
        frequencyAdapter = new FrequencyAdapter(tableNumber, baseFrequency, range, isTemperature, total);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fcv_activity_fragment, total > 0 ? new FrequenciesOverviewFragment(frequencyAdapter) : new EmptyTableFragment(frequencyAdapter), String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }
        if (total > 0) // Ask for the frequencies from the table
            parameter = ValueCodes.TABLES_COMMAND;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                if (existChanges() || frequencyAdapter.saveCoefficients)
                    setTable();
                else
                    finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void discoverCharacteristic() {
        if (parameter == ValueCodes.TABLES_COMMAND) {
            TransferBleData.readFrequencies(frequencyAdapter.tableNumber);
            parameter = ValueCodes.NONE;
        }
    }

    @Override
    protected void downloadData(byte[] data) {
        super.downloadData(data);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcv_activity_fragment);
        if (currentFragment instanceof ReceiverCallback) {
            runOnUiThread(() -> {
                ((ReceiverCallback) currentFragment).onGattDataAvailable(data);
            });
        }
    }

    public void setToolbarTitle(String title) {
        tv_title_toolbar.setText(title);
    }

    private boolean existChanges() {
        if (frequencyAdapter != null) {
            int count = frequencyAdapter.getItemCount();
            int originalCount = frequencyAdapter.originalTable == null ? 0 : frequencyAdapter.originalTable.length;
            if (originalCount != count)
                return true;
            if (frequencyAdapter.originalTable != null) {
                for (int i = 0; i < frequencyAdapter.originalTable.length; i++) {
                    if (frequencyAdapter.originalTable[i] != frequencyAdapter.frequencies.get(i))
                        return true;
                }
            }
        }
        return false;
    }

    private void showAlertDialog(String title, String message, boolean finish) {
        AlertDialog dialog = Dialogs.createAlertDialog(this, title, message, finish);
        dialogList.add(dialog);
        dialog.setOnDismissListener(d -> dialogList.remove(dialog));
    }

    @Override
    public void onNewDialogAdded(AlertDialog dialog) {
        dialogList.add(dialog);
        dialog.setOnDismissListener(d -> dialogList.remove(dialog));
    }
}