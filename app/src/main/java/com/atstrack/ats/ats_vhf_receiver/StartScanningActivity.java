package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverStatus;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Objects;
import java.util.UUID;

public class StartScanningActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.menu_scan_linearLayout)
    LinearLayout menu_scan_linearLayout;
    @BindView(R.id.warning_no_tables_linearLayout)
    LinearLayout warning_no_tables_linearLayout;

    private final static String TAG = StartScanningActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    private boolean isEmpty;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize())
                finish();
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    showDisconnectionMessage(status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals(ValueCodes.TABLES)) // Gets the number of frequencies from each table
                        onClickTables();
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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * Requests a read for get the number of frequencies from each table and display it.
     */
    private void onClickTables() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

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
            Intent intent = new Intent(this, AerialScanActivity.class);
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
        Intent intent = new Intent(this, TableOverviewActivity.class);
        startActivity(intent);
        menu_scan_linearLayout.setVisibility(View.VISIBLE);
        warning_no_tables_linearLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_scanning);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.lb_start_scanning);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        receiverInformation = ReceiverInformation.getReceiverInformation();
        ReceiverStatus.setReceiverStatus(this);

        parameter = ValueCodes.TABLES;
        isEmpty = false;
        menu_scan_linearLayout.setVisibility(View.VISIBLE);
        warning_no_tables_linearLayout.setVisibility(View.GONE);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (warning_no_tables_linearLayout.getVisibility() == View.VISIBLE) {
                menu_scan_linearLayout.setVisibility(View.VISIBLE);
                warning_no_tables_linearLayout.setVisibility(View.GONE);
            } else {
                Intent intent = new Intent(this, MainMenuActivity.class); // In the MainMenuActivity, only displays the main menu
                intent.putExtra(ValueCodes.MENU, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDisconnectionMessage(int status) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(view);
        dialog.show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD);
    }

    /**
     * With the received packet, gets the number of frequencies from each table and display on the screen.
     * @param packet The received packet.
     */
    private void downloadData(byte[] packet) {
        if (packet.length == 1) {
            mBluetoothLeService.discovering();
        } else {
            isEmpty = packet[1] == 0 && packet[2] == 0 && packet[3] == 0 && packet[4] == 0 && packet[5] == 0 && packet[6] == 0
                    && packet[7] == 0 && packet[8] == 0 && packet[9] == 0 && packet[10] == 0 && packet[11] == 0 && packet[12] == 0;
        }
    }
}