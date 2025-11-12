package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class ScanningActivity extends BaseActivity {

    @BindView(R.id.menu_scan_linearLayout)
    LinearLayout menu_scan_linearLayout;
    @BindView(R.id.warning_message_linearLayout)
    LinearLayout warning_message_linearLayout;
    @BindView(R.id.warning_message_textView)
    TextView warning_message_textView;
    @BindView(R.id.go_button)
    Button go_button;

    private final static String TAG = ScanningActivity.class.getSimpleName();

    private boolean isDetectionFilterEmpty;
    private boolean areTablesEmpty;
    private boolean isDefaultEmpty;
    private byte[] detectionData;
    private byte[] tablesData;
    private byte[] defaultData;

    @OnClick(R.id.start_manual_scan_button)
    public void onClickStartManualScan(View v) {
        if (isDetectionFilterEmpty) {
            showNoDetectionFilter();
        } else {
            Intent intent = new Intent(this, ManualScanActivity.class);
            startActivity(intent);
            parameter = ValueCodes.DETECTION_TYPE;
        }
    }

    @OnClick(R.id.start_aerial_scan_button)
    public void onClickStartAerialScan(View v) {
        parameter = ValueCodes.MOBILE_DEFAULTS;
        TransferBleData.readDefaults(true);
    }

    @OnClick(R.id.start_stationary_scan_button)
    public void onClickStartStationaryScan(View v) {
        parameter = ValueCodes.STATIONARY_DEFAULTS;
        TransferBleData.readDefaults(false);
    }

    @OnClick(R.id.go_button)
    public void onClickGo(View v) {
        Intent intent;
        if (go_button.getText().toString().equals(getString(R.string.lb_go_detection))) {
            intent = new Intent(this, DetectionFilterActivity.class);
            intent.putExtra(ValueCodes.VALUE, detectionData);
            startActivity(intent);
        } else if (go_button.getText().toString().equals(getString(R.string.lb_go_tables))) {
            intent = new Intent(this, TablesActivity.class);
            intent.putExtra(ValueCodes.VALUE, tablesData);
            startActivity(intent);
        } else if (go_button.getText().toString().equals(getString(R.string.lb_go_settings))) {
            if (parameter.equals(ValueCodes.MOBILE_DEFAULTS)) {
                intent = new Intent(this, MobileDefaultsActivity.class);
            } else {
                intent = new Intent(this, StationaryDefaultsActivity.class);
            }
            intent.putExtra(ValueCodes.VALUE, defaultData);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_scanning;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.lb_start_scanning);
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = getIntent().getExtras().getString(ValueCodes.PARAMETER, "");
        isDetectionFilterEmpty = false;
        areTablesEmpty = false;
        isDefaultEmpty = false;
        menu_scan_linearLayout.setVisibility(View.VISIBLE);
        warning_message_linearLayout.setVisibility(View.GONE);
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                Log.i(TAG, "Parameter: " + parameter);
                if (parameter.equals(ValueCodes.DETECTION_TYPE))
                    TransferBleData.readDetectionFilter();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                if (Converters.getHexValue(packet[0]).equals("88")) // Battery
                    setBatteryPercent(packet);
                else if (Converters.getHexValue(packet[0]).equals("56")) // Sd Card
                    setSdCardStatus(packet);
                else {
                    switch (parameter) {
                        case ValueCodes.DETECTION_TYPE:
                            downloadDetectionType(packet);
                            break;
                        case ValueCodes.TABLES: // Gets the number of frequencies from each table
                            downloadTables(packet);
                            break;
                        case ValueCodes.MOBILE_DEFAULTS:
                            downloadMobileDefaults(packet);
                            break;
                        case ValueCodes.STATIONARY_DEFAULTS:
                            downloadStationaryDefaults(packet);
                            break;
                    }
                }
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        menu_scan_linearLayout.setVisibility(View.VISIBLE);
        warning_message_linearLayout.setVisibility(View.GONE);
        if (isDetectionFilterEmpty) {
            parameter = ValueCodes.DETECTION_TYPE;
            TransferBleData.readDetectionFilter();
        } else if (areTablesEmpty) {
            parameter = ValueCodes.TABLES;
            TransferBleData.readTables();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (warning_message_linearLayout.getVisibility() == View.VISIBLE) {
                menu_scan_linearLayout.setVisibility(View.VISIBLE);
                warning_message_linearLayout.setVisibility(View.GONE);
            } else {
                Intent intent = new Intent(this, MenuActivity.class); // In the MainMenuActivity, only displays the main menu
                startActivity(intent);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadDetectionType(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("67")) {
            detectionData = data;
            isDetectionFilterEmpty = false;
            if (!Converters.getHexValue(data[1]).equals("09")) {
                isDetectionFilterEmpty = data[2] == 0 && data[3] == 0 && data[4] == 0 && data[5] == 0 && data[6] == 0 && data[7] == 0
                        && data[8] == 0 && data[9] == 0 && data[10] == 0 && data[11] == 0;
            }
            parameter = ValueCodes.TABLES;
            TransferBleData.readTables();
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x67 ...");
        }
    }

    /**
     * With the received packet, gets the number of frequencies from each table and display on the screen.
     * @param data The received packet.
     */
    private void downloadTables(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("7A")) {
            tablesData = data;
            areTablesEmpty = data[1] == 0 && data[2] == 0 && data[3] == 0 && data[4] == 0 && data[5] == 0 && data[6] == 0
                    && data[7] == 0 && data[8] == 0 && data[9] == 0 && data[10] == 0 && data[11] == 0 && data[12] == 0;
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x7A ...");
        }
    }

    private void downloadMobileDefaults(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6D")) {
            defaultData = data;
            isDefaultEmpty = Converters.isDefaultEmpty(data);
            if (isDetectionFilterEmpty) {
                showNoDetectionFilter();
            } else if (areTablesEmpty) {
                showNoTables();
            } else if (isDefaultEmpty) {
                showNoDefaultsSetting();
            } else {
                Intent intent = new Intent(this, MobileScanActivity.class);
                intent.putExtra(ValueCodes.VALUE, data);
                startActivity(intent);
            }
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6D ...");
        }
    }

    private void downloadStationaryDefaults(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("6C")) {
            defaultData = data;
            isDefaultEmpty = Converters.isDefaultEmpty(data);
            if (isDetectionFilterEmpty) {
                showNoDetectionFilter();
            } else if (areTablesEmpty) {
                showNoTables();
            } else if (isDefaultEmpty) {
                showNoDefaultsSetting();
            } else {
                Intent intent = new Intent(this, StationaryScanActivity.class);
                intent.putExtra(ValueCodes.VALUE, data);
                startActivity(intent);
            }
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x6C ...");
        }
    }

    private void showNoDetectionFilter() {
        menu_scan_linearLayout.setVisibility(View.GONE);
        warning_message_textView.setText(R.string.lb_warning_no_detection);
        go_button.setText(R.string.lb_go_detection);
        warning_message_linearLayout.setVisibility(View.VISIBLE);
    }

    private void showNoTables() {
        menu_scan_linearLayout.setVisibility(View.GONE);
        warning_message_textView.setText(R.string.lb_warning_no_tables);
        go_button.setText(R.string.lb_go_tables);
        warning_message_linearLayout.setVisibility(View.VISIBLE);
    }

    private void showNoDefaultsSetting() {
        menu_scan_linearLayout.setVisibility(View.GONE);
        warning_message_textView.setText(R.string.lb_warning_no_defaults);
        go_button.setText(R.string.lb_go_settings);
        warning_message_linearLayout.setVisibility(View.VISIBLE);
    }
}