package com.atstrack.ats.ats_vhf_receiver.BluetoothReceiver;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.OnClick;

public class MenuActivity extends BaseActivity {

    @BindView(R.id.receiver_name_textView)
    TextView receiver_name_textView;

    private final static String TAG = MenuActivity.class.getSimpleName();

    @OnClick(R.id.detect_tags_button)
    public void onClickDetectTags(View v) {
        Intent intent = new Intent(this, TagDetectionActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_bluetooth_menu;
        showToolbar = true;
        deviceCategory = ValueCodes.BLUETOOTH_RECEIVER;
        title = getString(R.string.bluetooth_beacon);
        super.onCreate(savedInstanceState);

        initializeCallback();
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiver_name_textView.setText(receiverInformation.getSerialNumber() + " Bluetooth Receiver");
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
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Disconnect
            leServiceConnection.getBluetoothLeService().disconnect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}