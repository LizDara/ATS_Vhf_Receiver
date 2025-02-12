package com.atstrack.ats.ats_vhf_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AcousticMenuActivity extends AppCompatActivity {

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

    private final static String TAG = AcousticMenuActivity.class.getSimpleName();

    private final Context mContext = this;
    private final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();
    private boolean mConnected = false;
    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action) && mConnected) {
                    mConnected = false;
                    unbindService(leServiceConnection.getServiceConnection());
                    leServiceConnection.close();
                    Message.showDisconnectionMessage(mContext, 0);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    //TODO
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @OnClick(R.id.disconnect_button)
    public void onClickDisconnect(View v) {
        leServiceConnection.getBluetoothLeService().disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acoustic_menu);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        acoustic_name_textView.setText(receiverInformation.getSerialNumber() + " Acoustic Receiver");

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        leServiceConnection.getBluetoothLeService().disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}