package com.atstrack.ats.ats_vhf_receiver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.MenuActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    public final static String TAG = BaseActivity.class.getSimpleName();
    protected ViewBinding binding = null;
    protected boolean showToolbar;
    protected String deviceCategory;
    protected String title;
    protected final List<Dialog> dialogList = new ArrayList<>();
    protected final Handler messageHandler = new Handler();

    protected final Context mContext = this;
    protected byte parameter = ValueCodes.NONE;
    protected ReceiverCallback receiverCallback;
    protected GattUpdateReceiver gattUpdateReceiver;
    protected final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// Keep screen on

        if (showToolbar)
            ActivitySetting.setToolbar(this, title, deviceCategory);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.i(TAG, "On back pressed");
            }
        });

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
    protected void onDestroy() {
        if (messageHandler != null)
            messageHandler.removeCallbacksAndMessages(null);
        for (Dialog dialog : dialogList) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        }
        dialogList.clear();
        Log.i(TAG, "ON DESTROY ACTIVITY ...");
        super.onDestroy();
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
        receiverInformation.changeSDCard(data[1] == (byte) 0x80);
        ActivitySetting.setSdCardStatus(this);
    }

    protected void setBatteryPercent(byte[] data) {
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        receiverInformation.changeDeviceBattery(Byte.toUnsignedInt(data[1]));
        ActivitySetting.setBatteryPercent(this);
    }

    protected void gattDisconnected() {
        parameter = ValueCodes.NONE;
        createLog();
        showDisconnectionAlertDialog();
    }

    protected void discoverCharacteristic() {
    }

    protected void downloadData(byte[] data) {
        switch (data[0]) {
            case ValueCodes.SD_CARD_COMMAND:
                if (data.length < 230) {
                    setSdCardStatus(data);
                    break;
                }
            case ValueCodes.BATTERY_COMMAND:
                if (data.length < 230) {
                    setBatteryPercent(data);
                    break;
                }
            case ValueCodes.LOW_POWER_COMMAND:
                if (data.length < 230) {
                    showLowPower();
                    break;
                }
        }
    }

    protected void showDisconnectionAlertDialog() {
        AlertDialog dialog = Dialogs.createDisconnectionDialog(mContext, getString(R.string.lbl_disconnect_receiver), deviceCategory);
        dialogList.add(dialog);
        messageHandler.postDelayed(() -> {
            try {
                if (leServiceConnection.existConnection())
                    leServiceConnection.close();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } catch (Exception ex) {
                Log.i("Message", ex.getLocalizedMessage());
            }
        }, ValueCodes.BRANDING_PERIOD);
        dialog.show();
    }

    protected void showLowPower() {
        AlertDialog dialog = Dialogs.createLowPowerDialog(mContext);
        dialogList.add(dialog);
        messageHandler.postDelayed(() -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, ValueCodes.BRANDING_PERIOD);
        dialog.show();
    }

    protected void createLog() {
        byte[] data = Converters.convertToUTF8(leServiceConnection.getBluetoothLeService().downloadLogs);
        Data logData = new Data(ValueCodes.LOG_FILE);
        logData.packets.add(data);
        ArrayList<Data> dataList = new ArrayList<>();
        dataList.add(logData);
        File root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
        Converters.printDataFiles(root, dataList);
    }
}
