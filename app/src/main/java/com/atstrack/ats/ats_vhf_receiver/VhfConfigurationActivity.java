package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.view.WindowManager;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Objects;

public class VhfConfigurationActivity extends AppCompatActivity {

    private final static String TAG = VhfConfigurationActivity.class.getSimpleName();

    private final Context mContext = this;

    private final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    Message.showDisconnectionMessage(mContext, status);
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    @OnClick(R.id.edit_frequency_tables_button)
    public void onClickEditFrequencyTables(View v) {
        Intent intent = new Intent(this, VhfTablesActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.edit_receiver_defaults_button)
    public void onClickEditReceiverDefaults(View v) {
        Intent intent = new Intent(this, VhfEditDefaultsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.set_transmitter_type_button)
    public void onClickSetTransmitterType(View v) {
        Intent intent = new Intent(this, VhfDetectionFilterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.clone_from_other_receiver_button)
    public void onClickCloneFromOtherReceiver(View v) {
        Intent intent = new Intent(this, VhfCloneActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_configuration);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, R.string.receiver_configuration);
        ActivitySetting.setReceiverStatus(this);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, Converters.makeGattUpdateIntentFilter());
        Log.i(TAG, "ON RESUME");
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
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
    }
}