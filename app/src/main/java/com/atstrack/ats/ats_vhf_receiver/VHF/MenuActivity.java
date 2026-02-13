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
import com.atstrack.ats.ats_vhf_receiver.Fragments.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class MenuActivity extends BaseActivity {

    @BindView(R.id.vhf_name_textView)
    TextView vhf_name_textView;
    @BindView(R.id.menu_imageView)
    ImageView menu_imageView;
    @BindView(R.id.percent_battery_menu_textView)
    TextView percent_battery_menu_textView;
    @BindView(R.id.sd_card_menu_textView)
    TextView sd_card_menu_textView;
    @BindView(R.id.battery_menu_imageView)
    ImageView battery_menu_imageView;
    @BindView(R.id.sd_card_menu_imageView)
    ImageView sd_card_menu_imageView;

    private final static String TAG = MenuActivity.class.getSimpleName();

    private byte detectionType;
    private DialogFragment detectionFilter;

    private void setDetectionFilter() {
        byte[] b = new byte[11];
        b[0] = (byte) 0x47;
        b[1] = detectionType;
        boolean result = TransferBleData.writeDetectionFilter(b);
        Log.i(TAG, Converters.getHexValue(b));
        if (!result) {
            detectionType = 0;
            detectionFilter.show(getSupportFragmentManager(), DetectionFilter.TAG);
        }
    }

    @OnClick(R.id.disconnect_button)
    public void onClickDisconnect(View v) {
        leServiceConnection.getBluetoothLeService().disconnect();
    }

    @OnClick(R.id.start_scanning_button)
    public void onClickStartScanning(View v) {
        Intent intent = new Intent(this, ScanningActivity.class);
        intent.putExtra(ValueCodes.PARAMETER, ValueCodes.DETECTION_TYPE);
        startActivity(intent);
    }

    @OnClick(R.id.receiver_configuration_button)
    public void onClickReceiverConfiguration(View v) {
        Intent intent = new Intent(this, ConfigurationActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.manage_receiver_data_button)
    public void onClickManageReceiverData(View v) {
        Intent intent = new Intent(this, ManageDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.convert_raw_data_button)
    public void onClickConvertRaw(View v) {
        Intent intent = new Intent(this, RawDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.diagnostics_button)
    public void onClickDiagnostics(View v) {
        Intent intent = new Intent(this, DiagnosticsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_menu;
        showToolbar = false;
        super.onCreate(savedInstanceState);

        menu_imageView.setVisibility(View.GONE);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        boolean firstTime = getIntent().getBooleanExtra(ValueCodes.FIRST_TIME, false);
        if (firstTime) { // Check the detection type
            switch (receiverInformation.getDeviceName().substring(15, 16)) {
                case "F":
                    detectionType = 0x08;
                    break;
                case "V":
                    detectionType = 0x07;
                    break;
                case "C":
                    detectionType = 0x09;
                    break;
            }
            checkDetectionType();
        }
        vhf_name_textView.setText("Receiver " + receiverInformation.getSerialNumber());
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
        if (Converters.getHexValue(data[0]).equals("56")) { // Sd Card
            receiverInformation.changeSDCard(Converters.getHexValue(data[1]).equals("80"));
            setSdCard(receiverInformation);
        } else if (Converters.getHexValue(data[0]).equals("88")) { // Battery
            receiverInformation.changeDeviceBattery(Integer.parseInt(Converters.getDecimalValue(data[1])));
            setBattery(receiverInformation);
        }
    }

    private void checkDetectionType() {
        if (detectionType == 0) {
            detectionFilter = DetectionFilter.newInstance();

            getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    detectionType = bundle.getByte(ValueCodes.VALUE);
                    setDetectionFilter();
                }
            });
            detectionFilter.show(getSupportFragmentManager(), DetectionFilter.TAG);
        }
    }

    private void setBattery(ReceiverInformation receiverInformation) {
        percent_battery_menu_textView.setText(receiverInformation.getPercentBattery() + "%");
        battery_menu_imageView.setBackground(ContextCompat.getDrawable(this, receiverInformation.getPercentBattery() > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));
    }

    private void setSdCard(ReceiverInformation receiverInformation) {
        sd_card_menu_textView.setText(receiverInformation.isSDCardInserted() ? "Inserted" : "None");
        sd_card_menu_imageView.setBackground(ContextCompat.getDrawable(this, receiverInformation.isSDCardInserted() ? R.drawable.ic_sd_card : R.drawable.ic_no_sd_card));
    }
}