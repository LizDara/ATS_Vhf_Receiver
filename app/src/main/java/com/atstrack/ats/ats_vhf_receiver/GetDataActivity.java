package com.atstrack.ats.ats_vhf_receiver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.DriveServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.Utils.Snapshots;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GetDataActivity extends AppCompatActivity {

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
    @BindView(R.id.memory_used_percent_textView)
    TextView memory_used_percent_textView;
    @BindView(R.id.memory_used_progressBar)
    ProgressBar memory_used_progressBar;
    @BindView(R.id.bytes_stored_textView)
    TextView bytes_stored_textView;
    @BindView(R.id.download_data_linearLayout)
    LinearLayout download_data_linearLayout;
    @BindView(R.id.iv_ProgressGIF)
    ImageView progressGIF;
    @BindView(R.id.percentage_textView)
    TextView percentage_textView;
    @BindView(R.id.menu_manage_receiver_linearLayout)
    LinearLayout menu_manage_receiver_linearLayout;

    private final static String TAG = GetDataActivity.class.getSimpleName();

    public final static char CR = (char) 0x0D;
    public final static char LF = (char) 0x0A;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int WAITING_PERIOD = 1000;

    FileOutputStream stream;
    File newFile;
    File root;
    String fileName = "";

    private ArrayList<Snapshots> snapshotArray;
    private Snapshots rawDataCollector;
    private Snapshots processDataCollector;
    private int finalPageNumber;
    private int pageNumber;
    private int totalPackagesNumber;
    private int packetNumber;
    private int percent;
    private boolean error;

    private AnimationDrawable animationDrawable;

    private Handler processHandler;
    private Handler receiveHandler;
    private DriveServiceHelper driveServiceHelper;

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
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

    private boolean mConnected = true;
    private String parameter = "";
    private String secondParameter = "";

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
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    switch (parameter) {
                        case "test": // Gets memory used and byte stored
                            onClickTest();
                            break;
                        /*case "readData": // Gets pages total number
                            onClickReadData();
                            break;*/
                        case "downloadData": // Downloads raw bytes
                            onClickDownloadData();
                            break;
                        case "response": // Response about erase data
                            onClickResponse();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    Log.i(TAG, parameter + ": " + Converters.getHexValue(packet));
                    if (packet == null) return;
                    switch (parameter) {
                        /*case "readData": // Gets pages total number
                            readData(packet);
                            break;*/
                        case "downloadData":
                            // Gets raw data in pages, each page contains 2048 bytes.
                            // 8 packets of 244 bytes and one of 96 bytes
                            if (packet.length == 4)
                                readData(packet);
                            else
                                downloadData(packet);
                            break;
                        case "test": // Gets memory used and byte stored
                            downloadTest(packet);
                            break;
                        case "response": // Response about erase data
                            downloadResponse(packet);
                            break;
                    }
                }
            }
            catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    private final BroadcastReceiver mSecondGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                Log.i(TAG, "BROADCAST RECEIVER 1: " + action);
                if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND.equals(action)) {
                    if ("eraseData".equals(secondParameter)) { // Deletes data
                        onClickEraseData();
                    } else if ("readData".equals(secondParameter)) {
                        onClickReadData();
                    }
                }
            }
            catch (Exception e) {
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

    private static IntentFilter makeSecondGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND);
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

    /**
     * Requests a read for get BLE device data before download data.
     * Service name: Stored Data.
     * Characteristic name: Study Data.
     */
    private void onClickReadData() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        boolean result = mBluetoothLeService.readCharacteristicDiagnosticResponse(service, characteristic);

        if (result)
            secondParameter = "";
    }

    /**
     * Requests a download data for the user.
     * Service name: Stored Data.
     * Characteristic name: Study Data.
     */
    private void onClickDownloadData() {
        secondParameter = "readData";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, WAITING_PERIOD);
    }

    private void onClickResponse() {
        secondParameter = "eraseData";

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, WAITING_PERIOD);
    }

    /**
     * Writes delete data.
     * Service name: StoredData.
     * Characteristic name: StudyData.
     */
    private void onClickEraseData() {
        byte[] b = new byte[]{(byte) 0x93};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            secondParameter = "";
            menu_manage_receiver_linearLayout.setVisibility(View.GONE);
            download_data_linearLayout.setVisibility(View.VISIBLE);
            progressGIF.setBackgroundResource(R.drawable.connecting_animation);
            animationDrawable = (AnimationDrawable) progressGIF.getBackground();
            animationDrawable.start();
        }
    }

    @OnClick(R.id.download_data_button)
    public void onClickDownloadData(View v) {
        parameter = "downloadData";
        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.erase_data_button)
    public void onClickEraseData(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (!bytes_stored_textView.getText().toString().contains("(0 bytes")) {
            builder.setTitle("Warning");
            builder.setMessage("Are you sure you want to delete data?");
            builder.setNegativeButton("Cancel", null);
            builder.setPositiveButton("Delete", (dialog, which) -> {
                parameter = "response";
                mBluetoothLeService.discovering();
            });
        } else {
            builder.setTitle("Erase Data");
            builder.setMessage("There is no data to delete.");
            builder.setPositiveButton("OK", null);
        }
        builder.show();
    }

    @OnClick(R.id.memory_button)
    public void onClickMemory(View v) {
        parameter = "";
        mBluetoothLeService.discovering();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.manage_receiver_data);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();
        parameter = "test";

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        processHandler = new Handler();
        receiveHandler = new Handler();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK)
                handleSignInIntent(data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(mSecondGattUpdateReceiver, makeSecondGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(mSecondGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected)
            showDisconnectionMessage();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        int MESSAGE_PERIOD = 3000;
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, MESSAGE_PERIOD);
    }

    /**
     * With the received packet, Gets memory used and byte stored and display on screen.
     *
     * @param data The received packet.
     */
    private void downloadTest(byte[] data) {
        int numberPage = findPageNumber(new byte[]{data[18], data[17], data[16], data[15]});
        int lastPage = findPageNumber(new byte[]{data[22], data[21], data[20], data[19]});
        memory_used_percent_textView.setText(((int) (((float) numberPage / (float) lastPage) * 100)) + "%");
        memory_used_progressBar.setProgress((int) ((((float) numberPage / (float) lastPage)) * 100));
        bytes_stored_textView.setText("Memory Used (" + (numberPage * 2048) + " bytes stored)");
    }

    /**
     * Displays a message indicating whether the writing was successful.
     *
     * @param data This packet indicates the writing status.
     */
    private void downloadResponse(byte[] data) {
        animationDrawable.stop();
        download_data_linearLayout.setVisibility(View.GONE);
        menu_manage_receiver_linearLayout.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Erase Data");
        if (Converters.getHexValue(data).equals("DD 00 BB EE "))
            builder.setMessage("Completed.");
        else
            builder.setMessage("Not Completed.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            finish();
        });
        builder.show();

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
     * Processes the data when the download is complete.
     *
     * @param packet The raw data.
     *
     * @return Returns the processed data.
     */
    private String readPacket(byte[] packet) {
        String data = "";
        int index = 0;

        SharedPreferences sharedPreferences = getSharedPreferences("Defaults", 0);
        int baseFrequency = sharedPreferences.getInt("BaseFrequency", 0);
        int frequency = 0;
        int frequencyTableIndex = 0;
        int year;
        Calendar calendar = Calendar.getInstance();
        String format = Converters.getHexValue(packet[0]);

        if (format.equals("83") || format.equals("82")) {

            data += "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsAge, Date, PrmNum" + CR + LF;
            year = Integer.parseInt(Converters.getDecimalValue(packet[6]));
            index += 8;

            while (index < packet.length) {
                if (Converters.getHexValue(packet[index]).equals("F0")) {
                    frequency = (baseFrequency * 1000) + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 2])));
                    frequencyTableIndex = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));

                    int date = Converters.hexToDecimal(
                            Converters.getHexValue(packet[index + 4]) + Converters.getHexValue(packet[index + 5]) + Converters.getHexValue(packet[index + 6]));

                    int month = date / 1000000;
                    date = date % 1000000;
                    int day = date / 10000;
                    date = date % 10000;
                    int hour = date / 100;
                    int minute = date % 100;
                    int seconds = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                    calendar.set(year + 2000, month - 1, day);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, seconds);
                    Log.i(TAG, "Y: " + (calendar.get(Calendar.YEAR) - 2000) + " M: " + (calendar.get(Calendar.MONTH) + 1) + " D: " + calendar.get(Calendar.DAY_OF_MONTH) + " H: " + calendar.get(Calendar.HOUR_OF_DAY) + " M: " + calendar.get(Calendar.MINUTE) + " S: " + calendar.get(Calendar.SECOND) + " J: " + calendar.get(Calendar.DAY_OF_YEAR));

                } else if (Converters.getHexValue(packet[index]).equals("F1")) {
                    String frequencyText = String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3);

                    int secondsOffset = Integer.parseInt(Converters.getDecimalValue(packet[index + 1]));
                    int antenna = Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) > 128 ?
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) - 128 :
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 2]));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4])) + 200;
                    int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                    int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                    int numberDetection = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                    calendar.add(Calendar.SECOND, secondsOffset);
                    Log.i(TAG, "Y: " + (calendar.get(Calendar.YEAR) - 2000) + " M: " + (calendar.get(Calendar.MONTH) + 1) + " D: " + calendar.get(Calendar.DAY_OF_MONTH) + " H: " + calendar.get(Calendar.HOUR_OF_DAY) + " M: " + calendar.get(Calendar.MINUTE) + " S: " + calendar.get(Calendar.SECOND) + " J: " + calendar.get(Calendar.DAY_OF_YEAR));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + antenna + ", " + frequencyTableIndex +
                            ", " + frequencyText + ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection + ", 0, 0, 0, " +
                            ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", 0" + CR + LF;

                } else if (Converters.getHexValue(packet[index]).equals("F2")) {
                    String frequencyText = String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3);

                    int antenna = Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) > 128 ?
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) - 128 :
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 2]));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4])) + 200;
                    int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                    int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                    int numberDetection = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + antenna + ", " + frequencyTableIndex +
                            ", " + frequencyText + ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection + ", 0, 0, 0, " +
                            ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", 0" + CR + LF;

                } else if (Converters.getHexValue(packet[index]).equals("E1") || Converters.getHexValue(packet[index]).equals("E2")) {
                    String frequencyText = String.valueOf(frequency).substring(0, 3) + "." + String.valueOf(frequency).substring(3);

                    int secondsOffset = Integer.parseInt(Converters.getDecimalValue(packet[index + 1]));
                    int antenna = Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) % 10;
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4])) + 200;
                    calendar.add(Calendar.SECOND, secondsOffset);
                    Log.i(TAG, "Y: " + (calendar.get(Calendar.YEAR) - 2000) + " M: " + (calendar.get(Calendar.MONTH) + 1) + " D: " + calendar.get(Calendar.DAY_OF_MONTH) + " H: " + calendar.get(Calendar.HOUR_OF_DAY) + " M: " + calendar.get(Calendar.MINUTE) + " S: " + calendar.get(Calendar.SECOND) + " J: " + calendar.get(Calendar.DAY_OF_YEAR));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + antenna + ", " + frequencyTableIndex +
                            ", " + frequencyText + ", " + signalStrength + ", 0, 0, 0, 0, 0, 0, " + ((calendar.get(Calendar.MONTH) + 1) +
                            "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", 0" + CR + LF;
                }

                index += 8;
            }
        } else if (format.equals("86")) {

            data += "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsAge, Date, PrmNum" + CR + LF;
            year = Integer.parseInt(Converters.getDecimalValue(packet[2]));
            int date = Converters.hexToDecimal(
                    Converters.getHexValue(packet[3]) +
                            Converters.getHexValue(packet[4]) +
                            Converters.getHexValue(packet[5]));
            int month = date / 1000000;
            date = date % 1000000;
            int day = date / 10000;
            date = date % 10000;
            int hour = date / 100;
            int minute = date % 100;
            int seconds = Integer.parseInt(Converters.getDecimalValue(packet[6]));

            index += 8;

            while (index < packet.length) {

                // cuando es 0xD0 agregar a data

                index += 8;
            }
        } else {
            error = true;
        }

        return data;
    }

    private void readData(byte[] packet) {
        // The first package indicates the total number of pages and the current page
        finalPageNumber = findPageNumber(new byte[] {packet[3], packet[2], packet[1], packet[0]});
        pageNumber = -1;
        totalPackagesNumber = finalPageNumber * 9;
        packetNumber = 0; // 9 data packages of 228 bytes
        error = false;
        percent = 0;
        percentage_textView.setText(percent + "%");

        snapshotArray = new ArrayList<>(); // The list that stores the raw and processed data
        rawDataCollector = new Snapshots(finalPageNumber * Snapshots.BYTES_PER_PAGE); // size is defined

        if (finalPageNumber == 0) { // No data to download
            animationDrawable.stop();
            download_data_linearLayout.setVisibility(View.GONE);
            menu_manage_receiver_linearLayout.setVisibility(View.VISIBLE);
            showPrintDialog("Message", "No data to download.", 1);
            parameter = "";
        } else {
            menu_manage_receiver_linearLayout.setVisibility(View.GONE);
            download_data_linearLayout.setVisibility(View.VISIBLE);
            progressGIF.setBackgroundResource(R.drawable.connecting_animation);
            animationDrawable = (AnimationDrawable) progressGIF.getBackground();
            animationDrawable.start();

            receiveHandler.postDelayed(() -> {
                if (!rawDataCollector.isFilled() && !error) {
                    showPrintDialog("Message", "Timeout error.", 1);
                    parameter = "";
                    error = true;
                    printSnapshotFiles();
                }
            }, 4000 * finalPageNumber);
        }
    }

    private boolean isErrorPacket(byte[] packet) {
        return (Converters.getHexValue(packet).equals("AA BB CC DD EE "));
    }

    private boolean isTransmissionDone(byte[] packet) {
        return Converters.getHexValue(packet).equals("DD 00 BB EE ");
    }

    /**
     * With the received packet, gets the raw data.
     *
     * @param packet The received packet.
     */
    private void downloadData(byte[] packet) {
        if (packet.length == 5 && isErrorPacket(packet)) { //Shows an error when the packet contains 5 bytes and stops downloading
            Log.i(TAG, Converters.getHexValue(packet));
            animationDrawable.stop();
            download_data_linearLayout.setVisibility(View.GONE);
            menu_manage_receiver_linearLayout.setVisibility(View.VISIBLE);
            showPrintDialog("Error", "Download error.", 1);
            error = true;
            parameter = "";
        } else if (packet.length == 4 && rawDataCollector.isFilled() && isTransmissionDone(packet)) {
            // Completed the download and have to process the data
            percent = 100;
            percentage_textView.setText(percent + "%");
            Log.i(TAG, "RAW DATA COLLECTOR IS FILLED.");
            processHandler.postDelayed(() -> {
                snapshotArray.add(rawDataCollector);
                String processData = readPacket(rawDataCollector.getSnapshot());

                if (!error) { // Adds the data to the list if you didn't find any errors during processing
                    byte[] data = Converters.convertToUTF8(processData);
                    processDataCollector = new Snapshots(data.length);
                    processDataCollector.processSnapshot(data);
                    snapshotArray.add(processDataCollector);
                }
                parameter = "";
                printSnapshotFiles();
            }, 1500);
        } else { // Copy the downloaded package
            packetNumber++;
            // Download percentage is updated
            percent = (int) ((((float) packetNumber / (float) totalPackagesNumber)) * 100);
            percentage_textView.setText(percent + "%");
            if (packetNumber % 9 == 0) { //Get current page number when is first package
                if ((pageNumber + 1) == findPageNumber(new byte[] {packet[224], packet[225], packet[226], packet[227]}) && (pageNumber + 1) < finalPageNumber) {
                    // The current page number must be one more than the previous one and less than the total number of pages
                    pageNumber++;
                    byte[] newPacket = new byte[224];
                    System.arraycopy(packet, 0, newPacket, 0, 224);
                    packet = newPacket;
                } else { // Shows an error and stops downloading
                    animationDrawable.stop();
                    download_data_linearLayout.setVisibility(View.GONE);
                    menu_manage_receiver_linearLayout.setVisibility(View.VISIBLE);
                    showPrintDialog("Error", "Download error.", 1);
                    error = true;
                    parameter = "";
                }
            }
            rawDataCollector.processSnapshotRaw(packet);
            Log.i(TAG, "PAGE: " + pageNumber + " PACKET: " + packetNumber + " IS FILLED: " + rawDataCollector.isFilled() + " PERCENT: " + percent);
        }
    }

    /**
     * Creates a file with the downloaded data.
     */
    private void printSnapshotFiles() {
        int i = 0;
        boolean outcome;
        String msg;
        try {
            //set the directory path
            root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
            if (!root.exists()) {
                outcome = root.mkdirs();
                if (!outcome)
                    throw new Exception("Folder 'atstrack' can't be created.");
                root.setReadable(true);
                root.setWritable(true);
            }
            while(i < snapshotArray.size()) {
                //get the fileName and create the file path
                fileName = snapshotArray.get(i).getFileName();
                newFile = new File(root.getAbsolutePath(), fileName);
                //see if there's a possible copy
                int copy = 1;
                while (!(newFile.createNewFile())) {
                    newFile = new File(root.getAbsolutePath(), fileName.substring(0, fileName.length() - 4) + " (" + copy + ").txt");
                    copy++;
                }
                newFile.setReadable(true);
                newFile.setWritable(true);
                //write in the file created
                stream = new FileOutputStream(newFile);
                stream.write(snapshotArray.get(i).getSnapshot());
                //save the file
                stream.flush();
                stream.close();
                //go for the next
                i++;
            }

            if (i == snapshotArray.size()) {
                animationDrawable.stop();
                download_data_linearLayout.setVisibility(View.GONE);
                menu_manage_receiver_linearLayout.setVisibility(View.VISIBLE);
                Log.i(TAG, "%s byte(s) downloaded successfully. No fails!");
                msg = "Download finished: " + (Snapshots.BYTES_PER_PAGE * finalPageNumber) + " byte(s) downloaded successfully.";
                if (error) {
                    msg += " No data found in bytes downloaded. No file was generated.";
                    showPrintDialog("Finished", msg, 1);
                } else {
                    showPrintDialog("Finished", msg, 3);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            animationDrawable.stop();
            download_data_linearLayout.setVisibility(View.GONE);
            menu_manage_receiver_linearLayout.setVisibility(View.VISIBLE);
            Log.i(TAG, "%s fail(s), %ss byte(s) downloaded in total.");
            msg = "Download finished: "+ (Snapshots.BYTES_PER_PAGE * finalPageNumber) + " byte(s) downloaded.";
            if (error) {
                msg += " No data found in bytes downloaded. No file was generated.";
                showPrintDialog("Finished", msg, 1);
            } else {
                showPrintDialog("Finished", msg, 3);
            }
        }
    }

    /**
     * Displays a message indicating the status of the download.
     *
     * @param title The title of the message.
     * @param message A short explanation of the status.
     * @param buttonNum Number of the type message.
     */
    private void showPrintDialog(String title, String message, int buttonNum) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (title != null) builder.setTitle(title);
        builder.setMessage(message);
        if (buttonNum == 1) // No data found in bytes downloaded
            builder.setPositiveButton("OK", null);
        if (buttonNum == 2) { // Save to the cloud
            builder.setPositiveButton("OK", (dialog, which) -> {
                requestSignIn();
            });
            builder.setNegativeButton("Cancel", null);
        }
        if (buttonNum == 3) // Ask if you want to save file to the cloud
            builder.setPositiveButton("OK", (dialog, which) -> {
                showPrintDialog("Finished", "Do you want to send the file to the cloud?", 2);
            });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.catskill_white)));
    }

    /**
     * Shows google login window.
     */
    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(new Scope(DriveScopes.DRIVE_FILE)).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Access the drive files of the logged in account.
     *
     * @param data Content of account information.
     */
    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(googleSignInAccount -> {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    GetDataActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));

            credential.setSelectedAccount(googleSignInAccount.getAccount());

            Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName("ATS VHF Receiver").build();

            driveServiceHelper = new DriveServiceHelper(googleDriveService);

            uploadFile();
        }).addOnFailureListener(e -> Log.i(TAG, e.toString()));
    }

    /**
     * Saves the document in the drive account.
     */
    private void uploadFile() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading to Google Drive.");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        driveServiceHelper.createFile(root.getAbsolutePath(), fileName).addOnSuccessListener(s -> {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Uploaded successfully.", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Check your Google Drive Api key", Toast.LENGTH_LONG).show();
        });
    }
}