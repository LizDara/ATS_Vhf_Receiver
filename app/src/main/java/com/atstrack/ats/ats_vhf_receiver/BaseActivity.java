package com.atstrack.ats.ats_vhf_receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity {

    private final static String TAG = BaseActivity.class.getSimpleName();
    protected int contentViewId;
    protected boolean showToolbar;
    protected String deviceCategory;
    protected String title;

    protected final Context mContext = this;
    protected int number;
    protected String parameter = "";
    protected ReceiverCallback receiverCallback;
    protected GattUpdateReceiver gattUpdateReceiver;
    protected final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentViewId);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// Keep screen on

        if (showToolbar)
            ActivitySetting.setToolbar(this, title, deviceCategory);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);

        initializeCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 33)
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeGattUpdateIntentFilter(), 2);
        else
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeGattUpdateIntentFilter());
        if(showToolbar && deviceCategory.equals(ValueCodes.VHF))
            ActivitySetting.setReceiverStatus(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
        } catch (Exception ex) {
            Log.i(TAG, "Failed to unregister receiver");
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED ...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ON DESTROY ACTIVITY ...");
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                gattDisconnected();
            }

            @Override
            public void onGattDiscovered() {
                discoverCharacteristic();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                downloadData(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    protected void setSdCardStatus(byte[] data) {
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.changeSDCard(Converters.getHexValue(data[1]).equals("80"));
        ActivitySetting.setSdCardStatus(this);
    }

    protected void setBatteryPercent(byte[] data) {
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.changeDeviceBattery(Integer.parseInt(Converters.getDecimalValue(data[1])));
        ActivitySetting.setBatteryPercent(this);
    }

    protected void gattDisconnected() {
        parameter = "";
        Message.showDisconnectionMessage(mContext);
    }

    protected void discoverCharacteristic() {
    }

    protected void downloadData(byte[] data) {
        switch (Converters.getHexValue(data[0])) {
            case "56": // Sd Card
                if (data.length < 230) {
                    setSdCardStatus(data);
                    break;
                }
            case "88": // Battery
                if (data.length < 230) {
                    setBatteryPercent(data);
                    break;
                }
        }
    }
}
