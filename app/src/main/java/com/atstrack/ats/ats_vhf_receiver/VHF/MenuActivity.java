package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Services.FirmwareServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.DialogsFragment.DetectionFilterDialogFragment;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfMenuBinding;

public class MenuActivity extends BaseActivity {
    private final static String TAG = MenuActivity.class.getSimpleName();

    private byte detectionType;
    private DialogFragment detectionFilter;
    private FirmwareServiceHelper firmwareServiceHelper;

    private void setDetectionFilter() {
        byte[] b = new byte[11];
        b[0] = (byte) 0x47;
        b[1] = detectionType;
        boolean result = TransferBleData.writeDetectionFilter(b);
        Log.i(TAG, Converters.getHexValue(b));
        if (result) {
            firmwareServiceHelper.updateAvailable();
        } else {
            detectionType = ValueCodes.NONE;
            detectionFilter.show(getSupportFragmentManager(), DetectionFilterDialogFragment.TAG);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = false;
        deviceCategory = ValueCodes.VHF;
        binding = ActivityVhfMenuBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityVhfMenuBinding) binding).includeDisconnectMenu.btnDisconnect.setOnClickListener(v -> leServiceConnection.getBluetoothLeService().disconnect());
        ((ActivityVhfMenuBinding) binding).btnStartScanning.setOnClickListener(v -> {
            Intent intent = new Intent(this, StartScanningActivity.class);
            startActivity(intent);
        });
        ((ActivityVhfMenuBinding) binding).btnReceiverConfiguration.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfigurationActivity.class);
            startActivity(intent);
        });
        ((ActivityVhfMenuBinding) binding).btnManageReceiverData.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageDataActivity.class);
            startActivity(intent);
        });
        ((ActivityVhfMenuBinding) binding).btnConvertRawData.setOnClickListener(v -> {
            Intent intent = new Intent(this, RawDataActivity.class);
            startActivity(intent);
        });

        firmwareServiceHelper = new FirmwareServiceHelper(this);
        ((ActivityVhfMenuBinding) binding).includeDisconnectMenu.imgMenu.setVisibility(View.GONE);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        checkDetectionType(receiverInformation.getDeviceName().substring(15, 16));
        if (detectionType != ValueCodes.NONE)
            firmwareServiceHelper.updateAvailable();
        ((ActivityVhfMenuBinding) binding).tvVhfName.setText("Receiver " + receiverInformation.getSerialNumber());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        setSdCard(receiverInformation);
        setBattery(receiverInformation);
    }

    @Override
    protected void downloadData(byte[] data) {
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        if (data[0] == ValueCodes.SD_CARD_COMMAND) {
            receiverInformation.changeSDCard(data[1] == (byte) 0x80);
            setSdCard(receiverInformation);
        } else if (data[0] == ValueCodes.BATTERY_COMMAND) {
            receiverInformation.changeDeviceBattery(Byte.toUnsignedInt(data[1]));
            setBattery(receiverInformation);
        }
    }

    private void checkDetectionType(String detection) {
        switch (detection) {
            case "F":
                detectionType = ValueCodes.FIXED;
                break;
            case "V":
                detectionType = ValueCodes.VARIABLE;
                break;
            case "C":
                detectionType = ValueCodes.CODED;
                break;
        }
        if (detectionType == ValueCodes.NONE) {
            detectionFilter = DetectionFilterDialogFragment.newInstance();

            getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    detectionType = bundle.getByte(ValueCodes.VALUE);
                    setDetectionFilter();
                }
            });
            detectionFilter.show(getSupportFragmentManager(), DetectionFilterDialogFragment.TAG);
        }
    }

    private void setBattery(ReceiverInformation receiverInformation) {
        ((ActivityVhfMenuBinding) binding).tvPercentBatteryMenu.setText(receiverInformation.getPercentBattery() + "%");
        ((ActivityVhfMenuBinding) binding).imgBatteryMenu.setBackground(ContextCompat.getDrawable(this, receiverInformation.getPercentBattery() > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));
    }

    private void setSdCard(ReceiverInformation receiverInformation) {
        ((ActivityVhfMenuBinding) binding).tvSdCardMenu.setText(receiverInformation.isSDCardInserted() ? "Inserted" : "None");
        ((ActivityVhfMenuBinding) binding).imgSdCardMenu.setBackground(ContextCompat.getDrawable(this, receiverInformation.isSDCardInserted() ? R.drawable.ic_sd_card : R.drawable.ic_no_sd_card));
    }
}