package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.HashMap;
import java.util.Map;

public class MobileDefaultsActivity extends BaseActivity {

    @BindView(R.id.frequency_table_number_aerial_textView)
    TextView frequency_table_number_aerial_textView;
    @BindView(R.id.scan_rate_seconds_aerial_textView)
    TextView scan_rate_seconds_aerial_textView;
    @BindView(R.id.aerial_gps_switch)
    SwitchCompat aerial_gps_switch;
    @BindView(R.id.aerial_auto_record_switch)
    SwitchCompat aerial_auto_record_switch;

    private final static String TAG = MobileDefaultsActivity.class.getSimpleName();

    Map<String, Object> originalData;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) // Gets the modified frequency table number
                    frequency_table_number_aerial_textView.setText(String.valueOf(value));
                else if (ValueCodes.SCAN_RATE_SECONDS_CODE == result.getResultCode()) // Gets the modified scan rate
                    scan_rate_seconds_aerial_textView.setText(String.valueOf(value * 0.1));
            });

    /**
     * Writes the modified aerial defaults data by the user.
     */
    private void setMobileDefaults() {
        int info = (aerial_gps_switch.isChecked() ? 1 : 0) << 7;
        info = info | ((aerial_auto_record_switch.isChecked() ? 1 : 0) << 6);
        float scanRate = Float.parseFloat(scan_rate_seconds_aerial_textView.getText().toString());
        int frequencyTableNumber = (frequency_table_number_aerial_textView.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
        byte[] b = new byte[] {(byte) 0x4D, (byte) frequencyTableNumber, (byte) info, (byte) (scanRate * 10), 0, 0, 0, 0};
        boolean result = TransferBleData.writeDefaults(true, b);
        if (result)
            Message.showMessage(this, 0);
        else
            Message.showMessage(this, 2);
    }

    @OnClick(R.id.frequency_table_number_aerial_linearLayout)
    public void onClickFrequencyTableNumber(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
        intent.putExtra(ValueCodes.TABLE_NUMBER, (int) originalData.get(ValueCodes.TABLE_NUMBER));
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_aerial_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.MOBILE_DEFAULTS);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_SECONDS_CODE);
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_mobile_defaults;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.aerial_defaults);
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = ValueCodes.MOBILE_DEFAULTS;
        originalData = new HashMap<>();
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.SAVE)) // Save aerial defaults data
                    setMobileDefaults();
                else if (parameter.equals(ValueCodes.MOBILE_DEFAULTS)) // Gets aerial defaults data
                    TransferBleData.readDefaults(true);
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (parameter.equals(ValueCodes.MOBILE_DEFAULTS)) // Gets aerial defaults data
                    downloadData(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (checkChanges()) {
                parameter = ValueCodes.SAVE;
                leServiceConnection.getBluetoothLeService().discovering();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
    }

    /**
     * With the received packet, gets aerial defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6D")) {
            parameter = "";
            frequency_table_number_aerial_textView.setText(
                    (Converters.getDecimalValue(data[1]).equals("0")) ? "None" : Converters.getDecimalValue(data[1]));
            int gps = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 7 & 1;
            aerial_gps_switch.setChecked(gps == 1);
            int autoRecord = Integer.parseInt(Converters.getDecimalValue(data[2])) >> 6 & 1;
            aerial_auto_record_switch.setChecked(autoRecord == 1);
            double scanRate = Integer.parseInt(Converters.getDecimalValue(data[3])) * 0.1;
            scan_rate_seconds_aerial_textView.setText(String.valueOf(scanRate));

            originalData.put(ValueCodes.TABLE_NUMBER, Integer.parseInt(Converters.getDecimalValue(data[1])));
            originalData.put(ValueCodes.GPS, gps == 1);
            originalData.put(ValueCodes.AUTO_RECORD, autoRecord == 1);
            originalData.put(ValueCodes.SCAN_RATE, scanRate);
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6D ...");
        }
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean checkChanges() {
        int tableNumber = (frequency_table_number_aerial_textView.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
        double scanRate = Double.parseDouble(scan_rate_seconds_aerial_textView.getText().toString());

        return (int) originalData.get(ValueCodes.TABLE_NUMBER) != tableNumber || (boolean) originalData.get(ValueCodes.GPS) != aerial_gps_switch.isChecked()
                || (boolean) originalData.get(ValueCodes.AUTO_RECORD) != aerial_auto_record_switch.isChecked()
                || (double) originalData.get(ValueCodes.SCAN_RATE) != scanRate;
    }
}