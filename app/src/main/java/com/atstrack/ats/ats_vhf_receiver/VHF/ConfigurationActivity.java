package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class ConfigurationActivity extends BaseActivity {

    private final static String TAG = ConfigurationActivity.class.getSimpleName();

    @OnClick(R.id.edit_frequency_tables_button)
    public void onClickEditFrequencyTables(View v) {
        Intent intent = new Intent(this, TablesActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES);
        startActivity(intent);
    }

    @OnClick(R.id.edit_receiver_defaults_button)
    public void onClickEditReceiverDefaults(View v) {
        Intent intent = new Intent(this, EditDefaultsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.set_transmitter_type_button)
    public void onClickSetTransmitterType(View v) {
        Intent intent = new Intent(this, DetectionFilterActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.DETECTION_TYPE);
        startActivity(intent);
    }

    @OnClick(R.id.clone_from_other_receiver_button)
    public void onClickCloneFromOtherReceiver(View v) {
        Intent intent = new Intent(this, CloneActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_configuration;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.receiver_configuration);
        super.onCreate(savedInstanceState);

        initializeCallback();
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                TransferBleData.notificationLog();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                if (Converters.getHexValue(packet[0]).equals("88")) // Battery
                    setBatteryPercent(packet);
                else if (Converters.getHexValue(packet[0]).equals("56")) // Sd Card
                    setSdCardStatus(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}