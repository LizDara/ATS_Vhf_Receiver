package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
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
        parameter = "";
        byte[] b = new byte[11];
        b[0] = (byte) 0x47;
        b[1] = detectionType;
        boolean result = TransferBleData.writeDetectionFilter(b);
        if (result) {
            SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
            SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
            sharedPreferencesEdit.putInt(ValueCodes.DETECTION_TYPE, Integer.parseInt(Converters.getDecimalValue(b[1])));
            sharedPreferencesEdit.apply();
        } else {
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

        initializeCallback();
        menu_imageView.setVisibility(View.GONE);
        boolean firstTime = getIntent().getBooleanExtra(ValueCodes.FIRST_TIME, false);
        if (firstTime) { // Check the detection type
            String status = getIntent().getStringExtra(ValueCodes.STATUS);
            switch (status) {
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
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        vhf_name_textView.setText("Receiver " + receiverInformation.getSerialNumber());
        percent_battery_menu_textView.setText(receiverInformation.getPercentBattery() + "%");
        battery_menu_imageView.setBackground(ContextCompat.getDrawable(this, receiverInformation.getPercentBattery() > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));
        sd_card_menu_textView.setText(receiverInformation.getSDCard());
        sd_card_menu_imageView.setBackground(ContextCompat.getDrawable(this, receiverInformation.getSDCard().equals("Inserted") ? R.drawable.ic_sd_card : R.drawable.ic_no_sd_card));
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                unbindService(leServiceConnection.getServiceConnection());
                Log.i(TAG, "ON BROADCAST RECEIVER: CLOSE CONNECTION");
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.DETECTION_TYPE))
                    setDetectionFilter();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {}
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

    private void checkDetectionType() {
        if (detectionType == 0) {
            detectionFilter = DetectionFilter.newInstance();

            getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    parameter = bundle.getString(ValueCodes.PARAMETER);
                    if (parameter != null && parameter.equals(ValueCodes.DETECTION_TYPE)) {
                        detectionType = bundle.getByte(ValueCodes.VALUE);
                        leServiceConnection.getBluetoothLeService().discovering();
                    }
                }
            });
            detectionFilter.show(getSupportFragmentManager(), DetectionFilter.TAG);
        }
    }
}