package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.FirmwareUpdateActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
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

    private final static String TAG = DiagnosticsActivity.class.getSimpleName();

    private Handler mHandlerTest;
    private AlertDialog dialog;

    @OnClick(R.id.update_receiver_button)
    public void onClickUpdateReceiver(View v) {
        Intent intent = new Intent(this, FirmwareUpdateActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_diagnostics;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.receiver_diagnostics);
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = ValueCodes.TEST;
        mHandlerTest = new Handler();
        runningTest(); // Loading the test
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                dialog.dismiss();
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.TEST)) // Gets BLE device data
                    TransferBleData.readDiagnostic();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (Converters.getHexValue(packet[0]).equals("88")) return;
                if (parameter.equals(ValueCodes.TEST)) // Gets BLE device data
                    downloadData(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
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

    /**
     * Finds the page number of a 4-byte packet.
     * @param packet The received packet.
     * @return Returns the page number.
     */
    private int findPageNumber(byte[] packet) {
        int pageNumber = Integer.parseInt(Converters.getDecimalValue(packet[0]));
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[1])) << 8) | pageNumber;
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[2])) << 16) | pageNumber;
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[3])) << 24) | pageNumber;
        return pageNumber;
    }

    /**
     * With the received packet, gets BLE device data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("89")) {
            parameter = "";
            int baseFrequency = Integer.parseInt(Converters.getDecimalValue(data[23])) * 1000;
            String range = String.valueOf(baseFrequency).substring(0, 3) + "." + String.valueOf(baseFrequency).substring(3) + "-";
            int frequencyRange = ((Integer.parseInt(Converters.getDecimalValue(data[23])) +
                    Integer.parseInt(Converters.getDecimalValue(data[24]))) * 1000) - 1;
            range += String.valueOf(frequencyRange).substring(0, 3) + "." + String.valueOf(frequencyRange).substring(3);
            range_textView.setText(range);

            battery_textView.setText(Converters.getDecimalValue(data[1]));
            int numberPage = findPageNumber(new byte[]{data[18], data[17], data[16], data[15]});
            int lastPage = findPageNumber(new byte[]{data[22], data[21], data[20], data[19]});
            bytes_stored_test_textView.setText(String.valueOf(numberPage * 2048));
            memory_used_textView.setText(String.valueOf((int) (((float) numberPage / (float) lastPage) * 100)));

            frequency_tables_textView.setText(Converters.getDecimalValue(data[2]));
            for (int i = 3; i <= 14; i++) { // Only shows tables that have frequencies
                if (Integer.parseInt(Converters.getDecimalValue(data[i])) > 0) {
                    View table = getLayoutInflater().inflate(R.layout.frequency_tables, null);
                    TextView number_of_table_textView = table.findViewById(R.id.number_of_table_textView);
                    TextView frequencies_table_textView = table.findViewById(R.id.frequencies_table_textView);
                    number_of_table_textView.setText("Table " + (i - 2) + ":");
                    frequencies_table_textView.setText(Converters.getDecimalValue(data[i]));
                    frequencies_table_linearLayout.addView(table);
                }
            }
            tx_type_textView.setText(Converters.getHexValue(data[25]).equals("09") ? "Coded" : "Non coded");
            software_version_textView.setText(Converters.getDecimalValue(data[26]));
            hardware_version_textView.setText(Converters.getDecimalValue(data[27]));
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x89 ...");
        }
    }

    /**
     * Defines the period it will take to do the test.
     */
    private void runningTest() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.connecting_test, null);
        dialog = new AlertDialog.Builder(this).create();
        ProgressBar testing_progressBar = view.findViewById(R.id.testing_progressBar);
        ImageView complete_test_imageView = view.findViewById(R.id.complete_test_imageView);
        TextView state_test_textView = view.findViewById(R.id.state_test_textView);
        dialog.setView(view);
        dialog.show();

        mHandlerTest.postDelayed(() -> {
            state_test_textView.setText(R.string.lb_diagnostics_complete);
            testing_progressBar.setVisibility(View.GONE);
            complete_test_imageView.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> {
                dialog.dismiss();
                loading_linearLayout.setVisibility(View.GONE);
                test_complete_scrollView.setVisibility(View.VISIBLE);
            }, ValueCodes.MESSAGE_PERIOD);
        }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD);
    }
}