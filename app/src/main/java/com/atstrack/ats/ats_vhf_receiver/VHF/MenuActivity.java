package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Services.FirmwareServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.DialogsFragment.DetectionFilterDialogFragment;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class MenuActivity extends BaseActivity {

    @BindView(R.id.tv_vhf_name)
    TextView tv_vhf_name;
    @BindView(R.id.img_menu)
    ImageView img_menu;
    @BindView(R.id.tv_percent_battery_menu)
    TextView tv_percent_battery_menu;
    @BindView(R.id.tv_sd_card_menu)
    TextView tv_sd_card_menu;
    @BindView(R.id.img_battery_menu)
    ImageView img_battery_menu;
    @BindView(R.id.img_sd_card_menu)
    ImageView img_sd_card_menu;

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

    @OnClick(R.id.btn_disconnect)
    public void onClickDisconnect(View v) {
        leServiceConnection.getBluetoothLeService().disconnect();
    }

    @OnClick(R.id.btn_start_scanning)
    public void onClickStartScanning(View v) {
        Intent intent = new Intent(this, StartScanningActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_receiver_configuration)
    public void onClickReceiverConfiguration(View v) {
        Intent intent = new Intent(this, ConfigurationActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_manage_receiver_data)
    public void onClickManageReceiverData(View v) {
        Intent intent = new Intent(this, ManageDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_convert_raw_data)
    public void onClickConvertRaw(View v) {
        Intent intent = new Intent(this, RawDataActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_menu;
        showToolbar = false;
        super.onCreate(savedInstanceState);

        firmwareServiceHelper = new FirmwareServiceHelper(this);
        img_menu.setVisibility(View.GONE);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        checkDetectionType(receiverInformation.getDeviceName().substring(15, 16));
        if (detectionType != ValueCodes.NONE)
            firmwareServiceHelper.updateAvailable();
        tv_vhf_name.setText("Receiver " + receiverInformation.getSerialNumber());
        setBattery(receiverInformation);
        setSdCard(receiverInformation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        setSdCard(receiverInformation);
        setBattery(receiverInformation);
    }

    @Override
    protected void gattDisconnected() {
        try {
            unbindService(leServiceConnection.getServiceConnection());
        } catch (Exception ex) {
            Log.w(TAG, ex.getLocalizedMessage());
        }
        super.gattDisconnected();
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
        tv_percent_battery_menu.setText(receiverInformation.getPercentBattery() + "%");
        img_battery_menu.setBackground(ContextCompat.getDrawable(this, receiverInformation.getPercentBattery() > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));
    }

    private void setSdCard(ReceiverInformation receiverInformation) {
        tv_sd_card_menu.setText(receiverInformation.isSDCardInserted() ? "Inserted" : "None");
        img_sd_card_menu.setBackground(ContextCompat.getDrawable(this, receiverInformation.isSDCardInserted() ? R.drawable.ic_sd_card : R.drawable.ic_no_sd_card));
    }
}