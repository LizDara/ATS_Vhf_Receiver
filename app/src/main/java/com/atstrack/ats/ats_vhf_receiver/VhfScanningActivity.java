package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class VhfScanningActivity extends AppCompatActivity {

    @BindView(R.id.menu_scan_linearLayout)
    LinearLayout menu_scan_linearLayout;
    @BindView(R.id.warning_no_tables_linearLayout)
    LinearLayout warning_no_tables_linearLayout;

    private final static String TAG = VhfScanningActivity.class.getSimpleName();

    private final Context mContext = this;
    private boolean isEmpty;

    private final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();
    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    Message.showDisconnectionMessage(mContext, status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals(ValueCodes.TABLES)) // Gets the number of frequencies from each table
                        TransferBleData.readTables(false);
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    if (parameter.equals(ValueCodes.TABLES)) // Gets the number of frequencies from each table
                        downloadData(packet);
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    @OnClick(R.id.start_manual_scan_button)
    public void onClickStartManualScan(View v) {
        Intent intent = new Intent(this, VhfManualScanActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.start_aerial_scan_button)
    public void onClickStartAerialScan(View v) {
        if (isEmpty) {
            menu_scan_linearLayout.setVisibility(View.GONE);
            warning_no_tables_linearLayout.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent(this, VhfMobileScanActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.start_stationary_scan_button)
    public void onClickStartStationaryScan(View v) {
        if (isEmpty) {
            menu_scan_linearLayout.setVisibility(View.GONE);
            warning_no_tables_linearLayout.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent(this, VhfStationaryScanActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.go_tables_button)
    public void onClickGoTables(View v) {
        Intent intent = new Intent(this, VhfTablesActivity.class);
        startActivity(intent);
        menu_scan_linearLayout.setVisibility(View.VISIBLE);
        warning_no_tables_linearLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_scanning);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, R.string.lb_start_scanning);
        ActivitySetting.setReceiverStatus(this);

        parameter = ValueCodes.TABLES;
        isEmpty = false;
        menu_scan_linearLayout.setVisibility(View.VISIBLE);
        warning_no_tables_linearLayout.setVisibility(View.GONE);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
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
        //unbindService(leServiceConnection.getServiceConnection());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (warning_no_tables_linearLayout.getVisibility() == View.VISIBLE) {
                menu_scan_linearLayout.setVisibility(View.VISIBLE);
                warning_no_tables_linearLayout.setVisibility(View.GONE);
            } else {
                Intent intent = new Intent(this, VhfMenuActivity.class); // In the MainMenuActivity, only displays the main menu
                intent.putExtra(ValueCodes.MENU, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
    }

    /**
     * With the received packet, gets the number of frequencies from each table and display on the screen.
     * @param packet The received packet.
     */
    private void downloadData(byte[] packet) {
        isEmpty = packet[1] == 0 && packet[2] == 0 && packet[3] == 0 && packet[4] == 0 && packet[5] == 0 && packet[6] == 0
                && packet[7] == 0 && packet[8] == 0 && packet[9] == 0 && packet[10] == 0 && packet[11] == 0 && packet[12] == 0;
    }
}