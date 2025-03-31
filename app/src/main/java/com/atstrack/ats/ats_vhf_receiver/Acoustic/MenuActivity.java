package com.atstrack.ats.ats_vhf_receiver.Acoustic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.nio.charset.StandardCharsets;

import butterknife.BindView;
import butterknife.OnClick;

public class MenuActivity extends BaseActivity {

    @BindView(R.id.acoustic_name_textView)
    TextView acoustic_name_textView;
    @BindView(R.id.battery_voltage_textView)
    TextView battery_voltage_textView;
    @BindView(R.id.number_dets_textView)
    TextView number_dets_textView;
    @BindView(R.id.battery_usage_textView)
    TextView battery_usage_textView;
    @BindView(R.id.utc_time_textView)
    TextView utc_time_textView;
    @BindView(R.id.tilt_textView)
    TextView tilt_textView;
    @BindView(R.id.temperature_textView)
    TextView temperature_textView;
    @BindView(R.id.pressure_textView)
    TextView pressure_textView;
    @BindView(R.id.error_code_textView)
    TextView error_code_textView;

    private final static String TAG = MenuActivity.class.getSimpleName();

    @OnClick(R.id.disconnect_button)
    public void onClickDisconnect(View v) {
        leServiceConnection.getBluetoothLeService().disconnect();
    }

    @OnClick(R.id.menu_imageView)
    public void onClickMenu(View v) {
        Intent intent = new Intent(this, OptionActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_acoustic_menu;
        showToolbar = false;
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = ValueCodes.ACOUSTIC;
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        acoustic_name_textView.setText(receiverInformation.getSerialNumber() + " Acoustic Receiver");

        byte[] healthBeaconData = getIntent().getByteArrayExtra(ValueCodes.VALUE);
        if (healthBeaconData != null)
            setHealthBeaconData(healthBeaconData);
        else
            Message.showMessage(this, "Package 0x70 not found.");
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                unbindService(leServiceConnection.getServiceConnection());
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.ACOUSTIC))
                    TransferBleData.notificationLog();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (parameter.equals(ValueCodes.ACOUSTIC))
                    setHealthBeaconData(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
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

    private void setHealthBeaconData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("78")) {
            Log.i(TAG, Converters.getHexValue(data));
            String volts = new String(new byte[]{data[5], (byte) 46, data[6]}, StandardCharsets.UTF_8);
            battery_voltage_textView.setText(volts + " V");

            String detections = new String(new byte[]{data[7], data[8], data[9], data[10]});
            number_dets_textView.setText(String.valueOf(Integer.parseInt(detections)));

            String batteryUsage = new String(new byte[]{data[11], data[12], data[13], data[14]});
            battery_usage_textView.setText((Integer.parseInt(batteryUsage) * 100) + " mahrs");

            String status = (Converters.getDecimalValue(data[15]).equals("97") && Converters.getDecimalValue(data[16]).equals("97")) ? "NONE" : "ERROR";
            error_code_textView.setText(status);
        }
    }
}