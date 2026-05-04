package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;
import butterknife.OnClick;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Services.FirmwareServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.Snapshots;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Messages;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class DiagnosticsActivity extends BaseActivity {

    @BindView(R.id.loading_linearLayout)
    LinearLayout loading_linearLayout;
    @BindView(R.id.test_complete_scrollView)
    ScrollView test_complete_scrollView;
    @BindView(R.id.range_textView)
    TextView range_textView;
    @BindView(R.id.battery_textView)
    TextView battery_textView;
    @BindView(R.id.bytes_stored_test_textView)
    TextView bytes_stored_test_textView;
    @BindView(R.id.memory_used_textView)
    TextView memory_used_textView;
    @BindView(R.id.frequency_tables_textView)
    TextView frequency_tables_textView;
    @BindView(R.id.tx_type_textView)
    TextView tx_type_textView;
    @BindView(R.id.software_version_textView)
    TextView software_version_textView;
    @BindView(R.id.hardware_version_textView)
    TextView hardware_version_textView;
    @BindView(R.id.frequencies_table_linearLayout)
    LinearLayout frequencies_table_linearLayout;

    @OnClick(R.id.update_receiver_button)
    public void onClickUpdateReceiver(View v) {
        FirmwareServiceHelper firmwareServiceHelper = new FirmwareServiceHelper(this);
        firmwareServiceHelper.updateAvailable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_diagnostics;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.receiver_diagnostics);
        super.onCreate(savedInstanceState);

        parameter = ValueCodes.DIAGNOSTIC_COMMAND;
        Messages.showLoadingMessage(this, getString(R.string.lb_running_diagnostics), getString(R.string.lb_diagnostics_complete), null, () -> {
            loading_linearLayout.setVisibility(View.GONE);
            test_complete_scrollView.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void discoverCharacteristic() {
        if (parameter == ValueCodes.DIAGNOSTIC_COMMAND)
            TransferBleData.readDiagnostic();
    }

    @Override
    protected void downloadData(byte[] data) {
        super.downloadData(data);
        if (data[0] == ValueCodes.DIAGNOSTIC_COMMAND) { // Get device diagnostic
            parameter = ValueCodes.NONE;
            int baseFrequency = Byte.toUnsignedInt(data[23]) * 1000;
            int frequencyRange = ((Byte.toUnsignedInt(data[23]) + Byte.toUnsignedInt(data[24])) * 1000) - 1;
            String range = Converters.getFrequency(baseFrequency) + "-" + Converters.getFrequency(frequencyRange);
            range_textView.setText(range);

            battery_textView.setText(Converters.getDecimalValue(data[1]));
            int numberPage = Converters.findPageNumber(new byte[]{data[18], data[17], data[16], data[15]});
            int lastPage = Converters.findPageNumber(new byte[]{data[22], data[21], data[20], data[19]});
            bytes_stored_test_textView.setText(String.valueOf(numberPage * Snapshots.BYTES_PER_PAGE));
            memory_used_textView.setText(String.valueOf((int) (((float) numberPage / (float) lastPage) * 100)));

            frequency_tables_textView.setText(Converters.getDecimalValue(data[2]));
            for (int i = 3; i <= 14; i++) { // Only shows tables that have frequencies
                if (Byte.toUnsignedInt(data[i]) > 0) {
                    View table = getLayoutInflater().inflate(R.layout.frequency_tables, null);
                    TextView number_of_table_textView = table.findViewById(R.id.number_of_table_textView);
                    TextView frequencies_table_textView = table.findViewById(R.id.frequencies_table_textView);
                    number_of_table_textView.setText("Table " + (i - 2) + ":");
                    frequencies_table_textView.setText(Converters.getDecimalValue(data[i]));
                    frequencies_table_linearLayout.addView(table);
                }
            }
            tx_type_textView.setText(data[25] == DetectionFilter.CODED ? "Coded" : "Non coded");
            software_version_textView.setText(Converters.getDecimalValue(data[26]));
            hardware_version_textView.setText(Converters.getDecimalValue(data[27]));
        }
    }
}