package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.Snapshots;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfDiagnosticsBinding;

public class DiagnosticsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.receiver_diagnostics);
        binding = ActivityVhfDiagnosticsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        parameter = ValueCodes.DIAGNOSTIC_COMMAND;
        /*Messages.showLoadingMessage(this, getString(R.string.lb_running_diagnostics), getString(R.string.lb_diagnostics_complete), null, () -> {
            loading_linearLayout.setVisibility(View.GONE);
            test_complete_scrollView.setVisibility(View.VISIBLE);
        });*/
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
            ((ActivityVhfDiagnosticsBinding) binding).rangeTextView.setText(range);

            ((ActivityVhfDiagnosticsBinding) binding).batteryTextView.setText(Converters.getDecimalValue(data[1]));
            int numberPage = Converters.findPageNumber(new byte[]{data[18], data[17], data[16], data[15]});
            int lastPage = Converters.findPageNumber(new byte[]{data[22], data[21], data[20], data[19]});
            ((ActivityVhfDiagnosticsBinding) binding).bytesStoredTestTextView.setText(String.valueOf(numberPage * Snapshots.BYTES_PER_PAGE));
            ((ActivityVhfDiagnosticsBinding) binding).memoryUsedTextView.setText(String.valueOf((int) (((float) numberPage / (float) lastPage) * 100)));

            ((ActivityVhfDiagnosticsBinding) binding).frequencyTablesTextView.setText(Converters.getDecimalValue(data[2]));
            for (int i = 3; i <= 14; i++) { // Only shows tables that have frequencies
                if (Byte.toUnsignedInt(data[i]) > 0) {
                    View table = getLayoutInflater().inflate(R.layout.item_frequency_tables, null);
                    TextView number_of_table_textView = table.findViewById(R.id.tv_number_of_table);
                    TextView frequencies_table_textView = table.findViewById(R.id.tv_frequencies_table);
                    number_of_table_textView.setText("Table " + (i - 2) + ":");
                    frequencies_table_textView.setText(Converters.getDecimalValue(data[i]));
                    ((ActivityVhfDiagnosticsBinding) binding).frequenciesTableLinearLayout.addView(table);
                }
            }
            ((ActivityVhfDiagnosticsBinding) binding).txTypeTextView.setText(data[25] == ValueCodes.CODED ? "Coded" : "Non coded");
            ((ActivityVhfDiagnosticsBinding) binding).softwareVersionTextView.setText(Converters.getDecimalValue(data[26]));
            ((ActivityVhfDiagnosticsBinding) binding).hardwareVersionTextView.setText(Converters.getDecimalValue(data[27]));
        }
    }
}