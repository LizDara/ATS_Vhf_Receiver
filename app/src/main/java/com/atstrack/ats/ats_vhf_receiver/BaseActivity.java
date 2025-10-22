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
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity {

    private final static String TAG = BaseActivity.class.getSimpleName();
    protected int contentViewId;
    protected boolean showToolbar;
    protected String deviceCategory;
    protected String title;

    protected final Context mContext = this;
    protected String parameter = "";
    protected final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();
    protected GattUpdateReceiver gattUpdateReceiver;
    protected ReceiverCallback receiverCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentViewId);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// Keep screen on

        if (showToolbar)
            setToolbar();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 33)
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter(), 2);
        else
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
        if(showToolbar && deviceCategory.equals(ValueCodes.VHF))
            ActivitySetting.setReceiverStatus(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
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

    private void setToolbar() {
        if (deviceCategory.equals(ValueCodes.VHF))
            ActivitySetting.setVhfToolbar(this, title);
        else
            ActivitySetting.setAcousticToolbar(this, title);
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
}
