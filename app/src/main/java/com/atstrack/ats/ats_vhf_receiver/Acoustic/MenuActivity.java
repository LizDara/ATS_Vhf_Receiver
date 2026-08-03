package com.atstrack.ats.ats_vhf_receiver.Acoustic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Services.FirmwareServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.nio.charset.StandardCharsets;

import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityAcousticMenuBinding;

public class MenuActivity extends BaseActivity {
    private final static String TAG = MenuActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = false;
        binding = ActivityAcousticMenuBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityAcousticMenuBinding) binding).acousticDisconnectMenuInclude.btnDisconnect.setOnClickListener(v -> leServiceConnection.getBluetoothLeService().disconnect());
        ((ActivityAcousticMenuBinding) binding).acousticDisconnectMenuInclude.imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), OptionActivity.class);
                startActivity(intent);
            }
        });

        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        ((ActivityAcousticMenuBinding) binding).acousticNameTextView.setText(receiverInformation.getSerialNumber() + " Acoustic Receiver");
        FirmwareServiceHelper firmwareServiceHelper = new FirmwareServiceHelper(this);
        //firmwareServiceHelper.updateAvailable(false);
    }

    @Override
    protected void gattDisconnected() {
        unbindService(leServiceConnection.getServiceConnection());
        super.gattDisconnected();
    }

    @Override
    protected void discoverCharacteristic() {
        TransferBleData.notificationLog(true);
    }

    @Override
    protected void downloadData(byte[] data) {
        setHealthBeaconData(data);
    }

    private void setHealthBeaconData(byte[] data) {
        if (data[0] == ValueCodes.ACOUSTIC_STATUS_COMMAND) {
            Log.i(TAG, Converters.getHexValue(data));
            String volts = new String(new byte[]{data[5], (byte) 46, data[6]}, StandardCharsets.UTF_8);
            ((ActivityAcousticMenuBinding) binding).batteryVoltageTextView.setText(volts + " V");

            String detections = new String(new byte[]{data[7], data[8], data[9], data[10]});
            ((ActivityAcousticMenuBinding) binding).numberDetsTextView.setText(String.valueOf(Integer.parseInt(detections)));

            String batteryUsage = new String(new byte[]{data[11], data[12], data[13], data[14]});
            ((ActivityAcousticMenuBinding) binding).batteryUsageTextView.setText((Integer.parseInt(batteryUsage) * 100) + " mahrs");

            String status = (Byte.toUnsignedInt(data[15]) == 97 && Byte.toUnsignedInt(data[16]) == 97) ? "NONE" : "ERROR";
            ((ActivityAcousticMenuBinding) binding).errorCodeTextView.setText(status);
        }
    }
}