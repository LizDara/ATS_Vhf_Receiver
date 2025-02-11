package com.atstrack.ats.ats_vhf_receiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class VhfMenuActivity extends AppCompatActivity {

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

    private final static String TAG = VhfMenuActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_STATUS = "DEVICE_STATUS";

    private final Context mContext = this;
    private byte detectionType;
    private DialogFragment detectionFilter;

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
                    if (parameter.equals(ValueCodes.DETECTION_TYPE))
                        setDetectionFilter();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

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
        Intent intent = new Intent(this, VhfScanningActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.receiver_configuration_button)
    public void onClickReceiverConfiguration(View v) {
        Intent intent = new Intent(this, VhfConfigurationActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.manage_receiver_data_button)
    public void onClickManageReceiverData(View v) {
        Intent intent = new Intent(this, VhfManageDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.convert_raw_data_button)
    public void onClickConvertRaw(View v) {
        Intent intent = new Intent(this, VhfRawDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.diagnostics_button)
    public void onClickDiagnostics(View v) {
        Intent intent = new Intent(this, VhfDiagnosticsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_menu);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on
        menu_imageView.setVisibility(View.GONE);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation(); // Get device data from previous activity

        boolean isMenu = getIntent().getBooleanExtra(ValueCodes.MENU, false);
        if (!isMenu) { // Check the detection type
            String status = getIntent().getStringExtra(EXTRAS_DEVICE_STATUS);
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
        vhf_name_textView.setText("Receiver " + receiverInformation.getSerialNumber());
        percent_battery_menu_textView.setText(receiverInformation.getPercentBattery() + "%");
        battery_menu_imageView.setBackground(ContextCompat.getDrawable(this, receiverInformation.getPercentBattery() > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));
        sd_card_menu_textView.setText(receiverInformation.getSDCard());
        sd_card_menu_imageView.setBackground(ContextCompat.getDrawable(this, receiverInformation.getSDCard().equals("Inserted") ? R.drawable.ic_sd_card : R.drawable.ic_no_sd_card));

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
        registerReceiver(mGattUpdateReceiver, Converters.makeFirstGattUpdateIntentFilter());
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