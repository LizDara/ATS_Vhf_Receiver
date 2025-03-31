package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class EditDefaultsActivity extends BaseActivity {

    private final static String TAG = EditDefaultsActivity.class.getSimpleName();

    @OnClick(R.id.aerial_defaults_button)
    public void onClickAerialDefaults(View v) {
        Intent intent = new Intent(this, MobileDefaultsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.stationary_defaults_button)
    public void onClickStationaryDefaults(View v) {
        Intent intent = new Intent(this, StationaryDefaultsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_edit_defaults;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.edit_receiver_defaults);
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
            public void onGattDiscovered() {}

            @Override
            public void onGattDataAvailable(byte[] packet) {}
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
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