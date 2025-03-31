package com.atstrack.ats.ats_vhf_receiver.VHF;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

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
    @BindView(R.id.warning_no_tables_linearLayout)
    LinearLayout warning_no_tables_linearLayout;

    private final static String TAG = ScanningActivity.class.getSimpleName();

    private boolean isEmpty;

    @OnClick(R.id.start_manual_scan_button)
    public void onClickStartManualScan(View v) {
        Intent intent = new Intent(this, ManualScanActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.start_aerial_scan_button)
    public void onClickStartAerialScan(View v) {
        if (isEmpty) {
            menu_scan_linearLayout.setVisibility(View.GONE);
            warning_no_tables_linearLayout.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent(this, MobileScanActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.start_stationary_scan_button)
    public void onClickStartStationaryScan(View v) {
        if (isEmpty) {
            menu_scan_linearLayout.setVisibility(View.GONE);
            warning_no_tables_linearLayout.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent(this, StationaryScanActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.go_tables_button)
    public void onClickGoTables(View v) {
        Intent intent = new Intent(this, TablesActivity.class);
        startActivity(intent);
        menu_scan_linearLayout.setVisibility(View.VISIBLE);
        warning_no_tables_linearLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_scanning;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.lb_start_scanning);
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = ValueCodes.TABLES;
        isEmpty = false;
        menu_scan_linearLayout.setVisibility(View.VISIBLE);
        warning_no_tables_linearLayout.setVisibility(View.GONE);
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.TABLES)) // Gets the number of frequencies from each table
                    TransferBleData.readTables(false);
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (parameter.equals(ValueCodes.TABLES)) // Gets the number of frequencies from each table
                    downloadTables(packet);
            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (warning_no_tables_linearLayout.getVisibility() == View.VISIBLE) {
                menu_scan_linearLayout.setVisibility(View.VISIBLE);
                warning_no_tables_linearLayout.setVisibility(View.GONE);
            } else {
                Intent intent = new Intent(this, MenuActivity.class); // In the MainMenuActivity, only displays the main menu
                startActivity(intent);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * With the received packet, gets the number of frequencies from each table and display on the screen.
     * @param data The received packet.
     */
    private void downloadTables(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("7A")) {
            isEmpty = data[1] == 0 && data[2] == 0 && data[3] == 0 && data[4] == 0 && data[5] == 0 && data[6] == 0
                    && data[7] == 0 && data[8] == 0 && data[9] == 0 && data[10] == 0 && data[11] == 0 && data[12] == 0;
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x7A ...");
        }
    }
}