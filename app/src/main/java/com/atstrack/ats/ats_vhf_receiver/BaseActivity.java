package com.atstrack.ats.ats_vhf_receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
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
    protected String secondParameter = "";
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
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED ...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ON DESTROY ACTIVITY ...");
    }

    private void setToolbar() {
        if (deviceCategory.equals(ValueCodes.VHF)) {
            ActivitySetting.setVhfToolbar(this, title);
            ActivitySetting.setReceiverStatus(this);
        } else {
            ActivitySetting.setAcousticToolbar(this, title);
        }
    }
}
