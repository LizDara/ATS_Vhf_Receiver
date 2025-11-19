package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class MobileDefaultsActivity extends BaseActivity {

    @BindView(R.id.frequency_table_number_aerial_textView)
    TextView frequency_table_number_aerial_textView;
    @BindView(R.id.scan_rate_seconds_aerial_textView)
    TextView scan_rate_seconds_aerial_textView;
    @BindView(R.id.gps_switch)
    SwitchCompat gps_switch;
    @BindView(R.id.aerial_auto_record_switch)
    SwitchCompat aerial_auto_record_switch;

    private final static String TAG = MobileDefaultsActivity.class.getSimpleName();

    private MobileDefaults mobileDefaults;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                if (ValueCodes.TABLE_NUMBER_CODE == result.getResultCode()) // Get the modified frequency table number
                    frequency_table_number_aerial_textView.setText(String.valueOf(value));
                else if (ValueCodes.SCAN_RATE_MOBILE_CODE == result.getResultCode()) // Get the modified scan rate
                    scan_rate_seconds_aerial_textView.setText(String.valueOf(value * 0.1));
            });

    /**
     * Writes the modified aerial defaults data by the user.
     */
    private void setMobileDefaults() {
        int info = (gps_switch.isChecked() ? 1 : 0) << 7;
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
        intent.putExtra(ValueCodes.TYPE, ValueCodes.TABLE_NUMBER_CODE);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.tableNumber);
        launcher.launch(intent);
    }

    @OnClick(R.id.scan_rate_seconds_aerial_linearLayout)
    public void onClickScanRateSeconds(View v) {
        Intent intent = new Intent(this, ValueDefaultsActivity.class);
        intent.putExtra(ValueCodes.TYPE, ValueCodes.SCAN_RATE_MOBILE_CODE);
        intent.putExtra(ValueCodes.VALUE, mobileDefaults.scanRate);
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
        parameter = getIntent().getExtras().getString(ValueCodes.PARAMETER, "");
        if (parameter.isEmpty()) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
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
                if (parameter.equals(ValueCodes.MOBILE_DEFAULTS)) // Gets aerial defaults data
                    TransferBleData.readDefaults(true);
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                switch (Converters.getHexValue(packet[0])) {
                    case "88": // Battery
                        setBatteryPercent(packet);
                        break;
                    case "56": // Sd Card
                        setSdCardStatus(packet);
                        break;
                    case "6D": // Gets aerial defaults data
                        downloadData(packet);
                        break;
                }
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (!existNotSet()) {
                if (existChanges())
                    setMobileDefaults();
                else
                    finish();
            } else {
                Message.showMessage(this, "Complete all fields.");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * With the received packet, gets aerial defaults data.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (!Converters.isDefaultEmpty(data)) {
            mobileDefaults = new MobileDefaults(data);
            frequency_table_number_aerial_textView.setText(
                    (mobileDefaults.tableNumber == 0) ? "None" : Converters.getDecimalValue(data[1]));
            gps_switch.setChecked(mobileDefaults.gpsOn);
            aerial_auto_record_switch.setChecked(mobileDefaults.autoRecordOn);
            scan_rate_seconds_aerial_textView.setText(String.valueOf(mobileDefaults.scanRate));
        } else {
            frequency_table_number_aerial_textView.setText(R.string.lb_not_set);
            scan_rate_seconds_aerial_textView.setText(R.string.lb_not_set);
            gps_switch.setChecked(true);
            aerial_auto_record_switch.setChecked(true);
        }
    }

    private boolean existNotSet() {
        if (frequency_table_number_aerial_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        if (scan_rate_seconds_aerial_textView.getText().toString().equals(getString(R.string.lb_not_set)))
            return true;
        return false;
    }

    /**
     * Checks for changes to the default data.
     * @return Returns true, if there are changes.
     */
    private boolean existChanges() {
        int tableNumber = (frequency_table_number_aerial_textView.getText().toString().equals("None")) ? 0 :
                Integer.parseInt(frequency_table_number_aerial_textView.getText().toString());
        double scanRate = Double.parseDouble(scan_rate_seconds_aerial_textView.getText().toString());

        return mobileDefaults.tableNumber != tableNumber || mobileDefaults.gpsOn != gps_switch.isChecked()
                || mobileDefaults.autoRecordOn != aerial_auto_record_switch.isChecked() || mobileDefaults.scanRate != scanRate;
    }
}