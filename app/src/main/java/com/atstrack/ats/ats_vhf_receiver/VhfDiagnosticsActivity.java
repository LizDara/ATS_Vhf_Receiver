package com.atstrack.ats.ats_vhf_receiver;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class VhfDiagnosticsActivity extends AppCompatActivity {

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
    @BindView(R.id.first_table_textView)
    TextView first_table_textView;
    @BindView(R.id.second_table_textView)
    TextView second_table_textView;
    @BindView(R.id.third_table_textView)
    TextView third_table_textView;
    @BindView(R.id.fourth_table_textView)
    TextView fourth_table_textView;
    @BindView(R.id.fifth_table_textView)
    TextView fifth_table_textView;
    @BindView(R.id.sixth_table_textView)
    TextView sixth_table_textView;
    @BindView(R.id.seventh_table_textView)
    TextView seventh_table_textView;
    @BindView(R.id.eighth_table_textView)
    TextView eighth_table_textView;
    @BindView(R.id.ninth_table_textView)
    TextView ninth_table_textView;
    @BindView(R.id.tenth_table_textView)
    TextView tenth_table_textView;
    @BindView(R.id.eleventh_table_textView)
    TextView eleventh_table_textView;
    @BindView(R.id.twelfth_table_textView)
    TextView twelfth_table_textView;
    @BindView(R.id.table_1_linearLayout)
    LinearLayout table1_linearLayout;
    @BindView(R.id.table_2_linearLayout)
    LinearLayout table2_linearLayout;
    @BindView(R.id.table3_linearLayout)
    LinearLayout table3_linearLayout;
    @BindView(R.id.table_4_linearLayout)
    LinearLayout table4_linearLayout;
    @BindView(R.id.table_5_linearLayout)
    LinearLayout table5_linearLayout;
    @BindView(R.id.table_6_linearLayout)
    LinearLayout table6_linearLayout;
    @BindView(R.id.table7_linearLayout)
    LinearLayout table7_linearLayout;
    @BindView(R.id.table8_linearLayout)
    LinearLayout table8_linearLayout;
    @BindView(R.id.table9_linearLayout)
    LinearLayout table9_linearLayout;
    @BindView(R.id.table10_linearLayout)
    LinearLayout table10_linearLayout;
    @BindView(R.id.table11_linearLayout)
    LinearLayout table11_linearLayout;
    @BindView(R.id.table12_linearLayout)
    LinearLayout table12_linearLayout;
    @BindView(R.id.tx_type_textView)
    TextView tx_type_textView;
    @BindView(R.id.software_version_textView)
    TextView software_version_textView;
    @BindView(R.id.hardware_version_textView)
    TextView hardware_version_textView;

    private final static String TAG = VhfDiagnosticsActivity.class.getSimpleName();

    private final Context mContext = this;
    private Handler mHandlerTest;

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
                    if (parameter.equals(ValueCodes.TEST)) // Gets BLE device data
                        TransferBleData.readDiagnostic();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    if (parameter.equals(ValueCodes.TEST)) // Gets BLE device data
                        downloadData(packet);
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    @OnClick(R.id.update_receiver_button)
    public void onClickUpdateReceiver(View v) {
        Intent intent = new Intent(this, VhfUpdateActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_diagnostics);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, R.string.receiver_diagnostics);
        ActivitySetting.setReceiverStatus(this);

        parameter = ValueCodes.TEST;
        mHandlerTest = new Handler();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);

        runningTest(); // Loading the test
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
        //unbindService(leServiceConnection.getServiceConnection());
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
        String range;
        int baseFrequency = Integer.parseInt(Converters.getDecimalValue(data[23])) * 1000;
        range = String.valueOf(baseFrequency).substring(0, 3) + "." + String.valueOf(baseFrequency).substring(3) + "-";
        int frequencyRange = ((Integer.parseInt(Converters.getDecimalValue(data[23])) +
                Integer.parseInt(Converters.getDecimalValue(data[24]))) * 1000) - 1;
        range += String.valueOf(frequencyRange).substring(0, 3) + "." + String.valueOf(frequencyRange).substring(3);
        range_textView.setText(range);

        battery_textView.setText(Converters.getDecimalValue(data[1]));
        int numberPage = findPageNumber(new byte[] {data[18], data[17], data[16], data[15]});
        int lastPage = findPageNumber(new byte[] {data[22], data[21], data[20], data[19]});
        bytes_stored_test_textView.setText(String.valueOf(numberPage * 2048));
        memory_used_textView.setText(String.valueOf((int) (((float) numberPage / (float) lastPage) * 100)));

        frequency_tables_textView.setText(Converters.getDecimalValue(data[2]));

        if (Integer.parseInt(Converters.getDecimalValue(data[3])) > 0) { // Only shows tables that have frequencies
            first_table_textView.setText(Converters.getDecimalValue(data[3]));
            table1_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[4])) > 0) {
            second_table_textView.setText(Converters.getDecimalValue(data[4]));
            table2_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[5])) > 0) {
            third_table_textView.setText(Converters.getDecimalValue(data[5]));
            table3_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[6])) > 0) {
            fourth_table_textView.setText(Converters.getDecimalValue(data[6]));
            table4_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[7])) > 0) {
            fifth_table_textView.setText(Converters.getDecimalValue(data[7]));
            table5_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[8])) > 0) {
            sixth_table_textView.setText(Converters.getDecimalValue(data[8]));
            table6_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[9])) > 0) {
            seventh_table_textView.setText(Converters.getDecimalValue(data[9]));
            table7_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[10])) > 0) {
            eighth_table_textView.setText(Converters.getDecimalValue(data[10]));
            table8_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[11])) > 0) {
            ninth_table_textView.setText(Converters.getDecimalValue(data[11]));
            table9_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[12])) > 0) {
            tenth_table_textView.setText(Converters.getDecimalValue(data[12]));
            table10_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[13])) > 0) {
            eleventh_table_textView.setText(Converters.getDecimalValue(data[13]));
            table11_linearLayout.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[14])) > 0) {
            twelfth_table_textView.setText(Converters.getDecimalValue(data[14]));
            table12_linearLayout.setVisibility(View.VISIBLE);
        }

        tx_type_textView.setText(
                Converters.getHexValue(data[25]).equals("09")  ? "Coded" : "Non coded");
        software_version_textView.setText(Converters.getDecimalValue(data[26]));
        hardware_version_textView.setText(Converters.getDecimalValue(data[27]));
    }

    /**
     * Defines the period it will take to do the test.
     */
    private void runningTest() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.connecting_test, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
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