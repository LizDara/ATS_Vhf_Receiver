package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.util.UUID;

public class TestReceiverActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.device_name_textView)
    TextView device_name_textView;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
    @BindView(R.id.percent_battery_textView)
    TextView percent_battery_textView;
    @BindView(R.id.loading_linearLayout)
    LinearLayout loading_linearLayout;
    @BindView(R.id.test_complete_scrollView)
    ScrollView test_complete_scrollView;
    @BindView(R.id.range_textView)
    TextView range_textView;
    @BindView(R.id.battery_textView)
    TextView battery_textView;
    @BindView(R.id.bytes_stored_test_textView)
    TextView bytes_stored_test_textView;
    @BindView(R.id.memory_used_textView)
    TextView memory_used_textView;
    @BindView(R.id.frequency_tables_textView)
    TextView frequency_tables_textView;
    @BindView(R.id.first_table_textView)
    TextView first_table_textView;
    @BindView(R.id.second_table_textView)
    TextView second_table_textView;
    @BindView(R.id.third_table_textView)
    TextView third_table_textView;
    @BindView(R.id.fourth_table_textView)
    TextView fourth_table_textView;
    @BindView(R.id.fifth_table_textView)
    TextView fifth_table_textView;
    @BindView(R.id.sixth_table_textView)
    TextView sixth_table_textView;
    @BindView(R.id.seventh_table_textView)
    TextView seventh_table_textView;
    @BindView(R.id.eighth_table_textView)
    TextView eighth_table_textView;
    @BindView(R.id.ninth_table_textView)
    TextView ninth_table_textView;
    @BindView(R.id.tenth_table_textView)
    TextView tenth_table_textView;
    @BindView(R.id.eleventh_table_textView)
    TextView eleventh_table_textView;
    @BindView(R.id.twelfth_table_textView)
    TextView twelfth_table_textView;
    @BindView(R.id.table_1_linearLayout)
    LinearLayout table1_linearLayout;
    @BindView(R.id.table_2_linearLayout)
    LinearLayout table2_linearLayout;
    @BindView(R.id.table3_linearLayout)
    LinearLayout table3_linearLayout;
    @BindView(R.id.table_4_linearLayout)
    LinearLayout table4_linearLayout;
    @BindView(R.id.table_5_linearLayout)
    LinearLayout table5_linearLayout;
    @BindView(R.id.table_6_linearLayout)
    LinearLayout table6_linearLayout;
    @BindView(R.id.table7_linearLayout)
    LinearLayout table7_linearLayout;
    @BindView(R.id.table8_linearLayout)
    LinearLayout table8_linearLayout;
    @BindView(R.id.table9_linearLayout)
    LinearLayout table9_linearLayout;
    @BindView(R.id.table10_linearLayout)
    LinearLayout table10_linearLayout;
    @BindView(R.id.table11_linearLayout)
    LinearLayout table11_linearLayout;
    @BindView(R.id.table12_linearLayout)
    LinearLayout table12_linearLayout;

    private final static String TAG = TestReceiverActivity.class.getSimpleName();

    private final int MESSAGE_PERIOD = 3000;
    private final int TEST_PERIOD = 1000;

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;
    private boolean state = true;

    private Handler mHandlerTest;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG,"Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = false;
    private String parameter = "";

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    state = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals("test")) // Gets BLE device data
                        onClickTest();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("test")) // Gets BLE device data
                        downloadData(packet);
                }
            }
            catch (Exception e) {
                Timber.tag("DCA:BR 198").e(e, "Unexpected error.");
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
     * Requests a read for get BLE device data.
     * Service name: Diagnostic.
     * Characteristic name: DiagInfo.
     */
    private void onClickTest() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_DIAG_INFO;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_receiver);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.test_receiver);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        mHandlerTest = new Handler();
        parameter = "test";

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // Loading the test
        runningTest();
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
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
            Log.d(TAG,"Connect request result= " + result);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected && !state)
            showDisconnectionMessage();
        return true;
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        dialog.setView(view);
        dialog.show();

        // The message disappears after a pre-defined period and will search for other available BLE devices again
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, MESSAGE_PERIOD);
    }

    /**
     * Finds the page number of a 4-byte packet.
     *
     * @param packet The received packet.
     *
     * @return Returns the page number.
     */
    private int findPageNumber(byte[] packet) {
        int pageNumber = Integer.parseInt(Converters.getDecimalValue(packet[0]));
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[1])) << 8) | pageNumber;
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[2])) << 16) | pageNumber;
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[3])) << 24) | pageNumber;
        return pageNumber;
    }

    /**
     * With the received packet, gets BLE device data.
     *
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        String range;
        int baseFrequency = Integer.parseInt(Converters.getDecimalValue(data[23])) * 1000;
        range = String.valueOf(baseFrequency).substring(0, 3) + "." + String.valueOf(baseFrequency).substring(3) + "-";
        int frequencyRange = ((Integer.parseInt(Converters.getDecimalValue(data[23])) +
                Integer.parseInt(Converters.getDecimalValue(data[24]))) * 1000) - 1;
        range += String.valueOf(frequencyRange).substring(0, 3) + "." + String.valueOf(frequencyRange).substring(3);

        range_textView.setText(range);
        battery_textView.setText(Converters.getDecimalValue(data[1]));
        int numberPage = findPageNumber(new byte[]{data[18], data[17], data[16], data[15]});
        int lastPage = findPageNumber(new byte[]{data[22], data[21], data[20], data[19]});
        bytes_stored_test_textView.setText(String.valueOf(numberPage * 2048));
        memory_used_textView.setText(String.valueOf(numberPage * 100 / lastPage));
        frequency_tables_textView.setText(Converters.getDecimalValue(data[2]));

        // Only shows tables that have frequencies
        if (Integer.parseInt(Converters.getDecimalValue(data[3])) > 0) {
            first_table_textView.setText(Converters.getDecimalValue(data[3]));
            table1_linearLayout.setVisibility(View.VISIBLE);
            first_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[4])) > 0) {
            second_table_textView.setText(Converters.getDecimalValue(data[4]));
            table2_linearLayout.setVisibility(View.VISIBLE);
            second_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[5])) > 0) {
            third_table_textView.setText(Converters.getDecimalValue(data[5]));
            table3_linearLayout.setVisibility(View.VISIBLE);
            third_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[6])) > 0) {
            fourth_table_textView.setText(Converters.getDecimalValue(data[6]));
            table4_linearLayout.setVisibility(View.VISIBLE);
            fourth_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[7])) > 0) {
            fifth_table_textView.setText(Converters.getDecimalValue(data[7]));
            table5_linearLayout.setVisibility(View.VISIBLE);
            fifth_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[8])) > 0) {
            sixth_table_textView.setText(Converters.getDecimalValue(data[8]));
            table6_linearLayout.setVisibility(View.VISIBLE);
            sixth_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[9])) > 0) {
            seventh_table_textView.setText(Converters.getDecimalValue(data[9]));
            table7_linearLayout.setVisibility(View.VISIBLE);
            seventh_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[10])) > 0) {
            eighth_table_textView.setText(Converters.getDecimalValue(data[10]));
            table8_linearLayout.setVisibility(View.VISIBLE);
            eighth_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[11])) > 0) {
            ninth_table_textView.setText(Converters.getDecimalValue(data[11]));
            table9_linearLayout.setVisibility(View.VISIBLE);
            ninth_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[12])) > 0) {
            tenth_table_textView.setText(Converters.getDecimalValue(data[12]));
            table10_linearLayout.setVisibility(View.VISIBLE);
            tenth_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[13])) > 0) {
            eleventh_table_textView.setText(Converters.getDecimalValue(data[13]));
            table11_linearLayout.setVisibility(View.VISIBLE);
            eleventh_table_textView.setVisibility(View.VISIBLE);
        }
        if (Integer.parseInt(Converters.getDecimalValue(data[14])) > 0) {
            twelfth_table_textView.setText(Converters.getDecimalValue(data[14]));
            table12_linearLayout.setVisibility(View.VISIBLE);
            twelfth_table_textView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Defines the period it will take to do the test.
     */
    private void runningTest() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.connecting_test, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        ImageView spinner_testing = view.findViewById(R.id.spinner_testing);
        TextView state_test_textView = view.findViewById(R.id.state_test_textView);

        spinner_testing.setImageDrawable((AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_anim_spinner_48));
        Drawable drawable1 = spinner_testing.getDrawable();
        Animatable animatable1 = (Animatable) drawable1;
        AnimatedVectorDrawableCompat.registerAnimationCallback(drawable1, new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                new Handler().postDelayed(animatable1::start, MESSAGE_PERIOD);
            }
        });
        animatable1.start();

        dialog.setView(view);
        dialog.show();

        mHandlerTest.postDelayed(() -> {
            state_test_textView.setText(R.string.lb_test_complete);
            spinner_testing.setImageDrawable((AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.check_avd_anim));
            Drawable drawable2 = spinner_testing.getDrawable();
            Animatable animatable2 = (Animatable) drawable2;
            AnimatedVectorDrawableCompat.registerAnimationCallback(drawable2, new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    new Handler().postDelayed(animatable2::start, TEST_PERIOD);
                }
            });
            animatable2.start();

            new Handler().postDelayed(() -> {
                dialog.dismiss();
                loading_linearLayout.setVisibility(View.GONE);
                test_complete_scrollView.setVisibility(View.VISIBLE);
            }, TEST_PERIOD);
        }, MESSAGE_PERIOD);
    }
}
