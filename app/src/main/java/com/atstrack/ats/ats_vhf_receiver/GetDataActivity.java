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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverStatus;
import com.atstrack.ats.ats_vhf_receiver.Utils.Snapshots;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
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
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
    @BindView(R.id.memory_used_percent_textView)
    TextView memory_used_percent_textView;
    @BindView(R.id.memory_used_progressBar)
    ProgressBar memory_used_progressBar;
    @BindView(R.id.bytes_stored_textView)
    TextView bytes_stored_textView;
    @BindView(R.id.menu_manage_receiver_linearLayout)
    LinearLayout menu_manage_receiver_linearLayout;
    @BindView(R.id.begin_download_linearLayout)
    LinearLayout begin_download_linearLayout;
    @BindView(R.id.downloading_file_linearLayout)
    LinearLayout downloading_file_linearLayout;
    @BindView(R.id.download_complete_linearLayout)
    LinearLayout download_complete_linearLayout;
    @BindView(R.id.delete_linearLayout)
    LinearLayout delete_linearLayout;
    @BindView(R.id.deleting_linearLayout)
    LinearLayout deleting_linearLayout;
    @BindView(R.id.deletion_complete_linearLayout)
    LinearLayout deletion_complete_linearLayout;
    @BindView(R.id.downloading_data_imageView)
    ImageView downloading_data_imageView;
    @BindView(R.id.downloading_data_textView)
    TextView downloading_data_textView;
    @BindView(R.id.downloading_progressBar)
    ProgressBar downloading_progressBar;
    @BindView(R.id.processing_data_imageView)
    ImageView processing_data_imageView;
    @BindView(R.id.processing_data_textView)
    TextView processing_data_textView;
    @BindView(R.id.processing_progressBar)
    ProgressBar processing_progressBar;
    @BindView(R.id.preparing_file_imageView)
    ImageView preparing_file_imageView;
    @BindView(R.id.preparing_file_textView)
    TextView preparing_file_textView;
    @BindView(R.id.preparing_progressBar)
    ProgressBar preparing_progressBar;

    private final static String TAG = GetDataActivity.class.getSimpleName();

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    FileOutputStream stream;
    File newFile;
    File root;
    String fileName = "";
    private ArrayList<byte[]> packets;
    private ArrayList<Snapshots> snapshotArray;
    private Snapshots rawDataCollector;
    private Handler receiveHandler;
    private DriveServiceHelper driveServiceHelper;
    private int finalPageNumber;
    private int pageNumber;
    private int totalPackagesNumber;
    private int packetNumber;
    private boolean error;
    private boolean downloading;
    private ArrayList<byte[]> pagePackets;

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
    private String secondParameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    int status = intent.getIntExtra(ValueCodes.DISCONNECTION_STATUS, 0);
                    showDisconnectionMessage(status);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    switch (parameter) {
                        case ValueCodes.TEST: // Get memory used and byte stored
                            onClickTest();
                            break;
                        case ValueCodes.DOWNLOAD: // Download raw bytes
                            onClickDownloadData();
                            break;
                        case ValueCodes.DELETE_RESPONSE: // Response about erase data
                            onClickResponseErase();
                            break;
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    switch (parameter) {
                        case ValueCodes.DOWNLOAD:
                            // Get raw data in pages, each page contains 2048 bytes.
                            // 9 packets of 230 bytes
                            if (packet.length > 4)
                                downloadData(packet);
                            else if (isTransmissionDone(packet))
                                downloadData(packet);
                            else if (packet.length == 4)// Get pages total number
                                readData(packet);
                            break;
                        case ValueCodes.TEST: // Get memory used and byte stored
                            downloadTest(packet);
                            break;
                        case ValueCodes.DELETE_RESPONSE: // Response about erase data
                            deleteResponse(packet);
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
                if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_SECOND.equals(action)) {
                    switch (secondParameter) {
                        case ValueCodes.DELETE: // Delete data
                            onClickEraseData();
                            break;
                        case ValueCodes.PAGES_NUMBER: // Read Final Page Number
                            onClickReadData();
                            break;
                        case ValueCodes.READY_DOWNLOAD: // Write that it is ready to download data
                            onClickResponseDownload();
                            break;
                        case ValueCodes.PAGE_OK: // Write that it received the page correctly
                            onClickResponsePage(true);
                            break;
                        case ValueCodes.PAGE_BAD: // Write that it did not receive the page
                            onClickResponsePage(false);
                            break;
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
     */
    private void onClickTest() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_DIAGNOSTIC;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_DIAG_INFO;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Requests a read for get BLE device data before download data.
     */
    private void onClickReadData() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    /**
     * Requests a download data for the user.
     */
    private void onClickDownloadData() {
        secondParameter = ValueCodes.PAGES_NUMBER;

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, ValueCodes.WAITING_PERIOD);

        pageNumber = 0;
        packetNumber = 1; // 9 data packages of 230 bytes
        error = false;
        packets = new ArrayList<>();
        pagePackets = new ArrayList<>();
        snapshotArray = new ArrayList<>(); // The list that stores the raw and processed data
    }

    private void onClickResponseDownload() {
        byte[] b = new byte[]{(byte) 0x94};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result)
            secondParameter = "";
    }

    private void onClickResponsePage(boolean isOk) {
        byte[] b = new byte[]{isOk ? (byte) 0x95 : (byte) 0x96};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);
        Log.i(TAG, "--------------------------------------------------------------------------------------------------------Is Ok: " + isOk + " Page number: " + (pageNumber - 1));

        if (result)
            secondParameter = "";
    }

    private void onClickResponseErase() {
        secondParameter = ValueCodes.DELETE;

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        mBluetoothLeService.setCharacteristicNotificationRead(service, characteristic, true);

        new Handler().postDelayed(() -> {
            mBluetoothLeService.discoveringSecond();
        }, ValueCodes.WAITING_PERIOD);
    }

    /**
     * Writes delete data.
     */
    private void onClickEraseData() {
        byte[] b = new byte[]{(byte) 0x93};

        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_STUDY_DATA;
        boolean result = mBluetoothLeService.writeCharacteristic(service, characteristic, b);

        if (result) {
            secondParameter = "";
            setVisibility("deleting");
        }
    }

    @OnClick(R.id.download_data_button)
    public void onClickDownloadData(View v) {
        setVisibility("begin");
    }

    @OnClick(R.id.erase_data_button)
    public void onClickEraseData(View v) {
        if (!bytes_stored_textView.getText().toString().contains("(0 bytes")) {
            setVisibility("delete");
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Erase Data");
            builder.setMessage("There is no data to delete.");
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }

    @OnClick(R.id.begin_download_button)
    public void onClickBeginDownload(View v) {
        parameter = ValueCodes.DOWNLOAD;
        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.cancel_download_button)
    public void onClickCancelDownload(View v) {
        downloading = false;
        setVisibility("begin");
        parameter = "";
    }

    @OnClick(R.id.return_button)
    public void onClickReturn(View v) {
        setVisibility("menu");
    }

    @OnClick(R.id.delete_receiver_button)
    public void onClickDeleteReceiver(View v) {
        parameter = ValueCodes.DELETE_RESPONSE;
        mBluetoothLeService.discovering();
    }

    @OnClick(R.id.return_screen_button)
    public void onClickReturnScreen(View v) {
        setVisibility("menu");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        receiverInformation = ReceiverInformation.getReceiverInformation();
        ReceiverStatus.setReceiverStatus(this);

        parameter = ValueCodes.TEST;
        downloading = false;
        receiveHandler = new Handler();
        setVisibility("menu");

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ValueCodes.REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK)
                handleSignInIntent(data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(mSecondGattUpdateReceiver, makeSecondGattUpdateIntentFilter());
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (menu_manage_receiver_linearLayout.getVisibility() == View.VISIBLE)
                finish();
            else if (begin_download_linearLayout.getVisibility() == View.VISIBLE)
                setVisibility("menu");
            else if (downloading_file_linearLayout.getVisibility() == View.VISIBLE)
                setVisibility("begin");
            else if (download_complete_linearLayout.getVisibility() == View.VISIBLE || delete_linearLayout.getVisibility() == View.VISIBLE || deletion_complete_linearLayout.getVisibility() == View.VISIBLE)
                setVisibility("menu");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDisconnectionMessage(int status) {
        parameter = "";
        secondParameter = "";
        if (downloading) {
            if (rawDataCollector.byteIndex == 0) {
                for (byte[] packet : packets)
                    rawDataCollector.processSnapshotRaw(packet);
            }
            printSnapshotFiles();
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(view);
        dialog.show();
        Toast.makeText(this, "Connection failed, status: " + status, Toast.LENGTH_LONG).show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD);
    }

    private void setVisibility(String value) {
        switch (value) {
            case "menu":
                menu_manage_receiver_linearLayout.setVisibility(View.VISIBLE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                deletion_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_manage_receiver);
                break;
            case "begin":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.VISIBLE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                deletion_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_download_receiver_data);
                break;
            case "downloading":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.VISIBLE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                deletion_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "downloaded":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.VISIBLE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                deletion_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "delete":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.VISIBLE);
                deleting_linearLayout.setVisibility(View.GONE);
                deletion_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_delete_receiver_data);
                break;
            case "deleting":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.VISIBLE);
                deletion_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "deleted":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                deletion_complete_linearLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * With the received packet, Gets memory used and byte stored and display on screen.
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
     * @param data This packet indicates the writing status.
     */
    private void deleteResponse(byte[] data) {
        if (Converters.getHexValue(data).equals("DD 00 BB EE ")) {
            parameter = ValueCodes.TEST;
            mBluetoothLeService.discovering();
            setVisibility("deleted");
        } else {
            parameter = "";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Erase Data");
            builder.setMessage("Not Completed.");
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }

    private void initDownloading() {
        downloading_data_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        downloading_data_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        downloading_progressBar.setVisibility(View.GONE);
        processing_data_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        processing_data_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        processing_progressBar.setVisibility(View.GONE);
        preparing_file_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        preparing_file_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        preparing_progressBar.setVisibility(View.GONE);
    }

    /**
     * Finds the page number of a 4-byte packet.
     * @param packet The received packet.
     * @return Returns the page number.
     */
    private int findPageNumber(byte[] packet) {
        int pageNumber = Integer.parseInt(Converters.getDecimalValue(packet[0]));
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[1])) << 8) | pageNumber;
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[2])) << 16) | pageNumber;
        pageNumber = (Integer.parseInt(Converters.getDecimalValue(packet[3])) << 24) | pageNumber;
        return pageNumber;
    }

    private int findPacketNumber(byte[] packet) {
        String number = Converters.getHexValue(packet);
        int packetNumber = 0;
        switch (number) {
            case "11 11 ":
                packetNumber = 1;
                break;
            case "22 22 ":
                packetNumber = 2;
                break;
            case "33 33 ":
                packetNumber = 3;
                break;
            case "44 44 ":
                packetNumber = 4;
                break;
            case "55 55 ":
                packetNumber = 5;
                break;
            case "66 66 ":
                packetNumber = 6;
                break;
            case "77 77 ":
                packetNumber = 7;
                break;
            case "88 88 ":
                packetNumber = 8;
                break;
            case "99 99 ":
                packetNumber = 9;
                break;
        }
        return packetNumber;
    }

    /**
     * Processes the data when the download is complete.
     * @param packet The raw data.
     * @return Returns the processed data.
     */
    private String readPacket(byte[] packet) {
        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        int baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        String data = "Year, JulianDay, Hour, Min, Sec, Ant, Index, Freq, SS, Code, Mort, NumDet, Lat, Long, GpsAge, Date, PrmNum" + ValueCodes.CR + ValueCodes.LF;
        int index = 0;
        int frequency = 0;
        int frequencyTableIndex = 0;
        int year;
        Calendar calendar = Calendar.getInstance();

        while (index < packet.length) {
            String format = Converters.getHexValue(packet[index]);
            if (format.equals("83") || format.equals("82")) {
                year = Integer.parseInt(Converters.getDecimalValue(packet[index + 6]));
                index += 8;
                while (index < packet.length && !format.equals("83") && !format.equals("82") && !format.equals("86")) {
                    if (Converters.getHexValue(packet[index]).equals("F0")) { //Header
                        frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 256) +
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
                        calendar.set(year + 2000, month - 1, day, hour, minute, seconds);
                    } else if (Converters.getHexValue(packet[index]).equals("F1")) {
                        int secondsOffset = Integer.parseInt(Converters.getDecimalValue(packet[index + 1]));
                        int antenna = Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) > 128 ?
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) - 128 :
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 2]));
                        int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                        int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                        int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                        int numberDetection = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                        calendar.add(Calendar.SECOND, secondsOffset);

                        data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                                ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + antenna + ", " + frequencyTableIndex +
                                ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection + ", 0, 0, 0, " +
                                ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", 0" + ValueCodes.CR + ValueCodes.LF;

                    } else if (Converters.getHexValue(packet[index]).equals("F2")) {
                        int antenna = Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) > 128 ?
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) - 128 :
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 2]));
                        int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                        int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                        int mort = (Integer.parseInt(Converters.getDecimalValue(packet[index + 6])) * 256) +
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                        int numberDetection = (Integer.parseInt(Converters.getDecimalValue(packet[index + 1])) * 256) +
                                Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));

                        data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                                ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + antenna + ", " + frequencyTableIndex +
                                ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", " + code + ", " + mort + ", " + numberDetection + ", 0, 0, 0, " +
                                ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", 0" + ValueCodes.CR + ValueCodes.LF;

                    } else if (Converters.getHexValue(packet[index]).equals("E1") || Converters.getHexValue(packet[index]).equals("E2")) {
                        int secondsOffset = Integer.parseInt(Converters.getDecimalValue(packet[index + 1]));
                        int antenna = Integer.parseInt(Converters.getDecimalValue(packet[index + 2])) % 10;
                        int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                        calendar.add(Calendar.SECOND, secondsOffset);

                        data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                                ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", " + antenna + ", " + frequencyTableIndex +
                                ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", 0, 0, 0, 0, 0, 0, " + ((calendar.get(Calendar.MONTH) + 1) +
                                "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", 0" + ValueCodes.CR + ValueCodes.LF;
                    }
                    index += 8;
                }
            } else if (format.equals("86")) {
                year = Integer.parseInt(Converters.getDecimalValue(packet[index + 2]));
                int month = Integer.parseInt(Converters.getDecimalValue(packet[index + 3]));
                int day = Integer.parseInt(Converters.getDecimalValue(packet[index + 4]));
                int hour = Integer.parseInt(Converters.getDecimalValue(packet[index + 5]));
                int minute = Integer.parseInt(Converters.getDecimalValue(packet[index + 6]));
                int seconds = Integer.parseInt(Converters.getDecimalValue(packet[index + 7]));
                calendar.set(year + 2000, month - 1, day, hour, minute, seconds);

                if (Converters.getHexValue(packet[index + 8]).equals("D0")) {
                    frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 11]));
                    int code = Integer.parseInt(Converters.getDecimalValue(packet[index + 12]));
                    int mort = Integer.parseInt(Converters.getDecimalValue(packet[index + 13]));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", 0, " + frequencyTableIndex +
                            ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", " + code + ", " + mort + ", 0, 0, 0, 0, " +
                            ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", 0" + ValueCodes.CR + ValueCodes.LF;
                } else if (Converters.getHexValue(packet[index + 8]).equals("E0")) {
                    frequency = baseFrequency + ((Integer.parseInt(Converters.getDecimalValue(packet[index + 9])) * 256) +
                            Integer.parseInt(Converters.getDecimalValue(packet[index + 10])));
                    int signalStrength = Integer.parseInt(Converters.getDecimalValue(packet[index + 11]));

                    data += (calendar.get(Calendar.YEAR) - 2000) + ", " + calendar.get(Calendar.DAY_OF_YEAR) + ", " + calendar.get(Calendar.HOUR_OF_DAY) +
                            ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ", 0, " + frequencyTableIndex +
                            ", " + Converters.getFrequency(frequency) + ", " + signalStrength + ", 0, 0, 0, 0, 0, 0, " + ((calendar.get(Calendar.MONTH) + 1) +
                            "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.YEAR) - 2000)) + ", 0" + ValueCodes.CR + ValueCodes.LF;
                }
                index += 16;
            } else {
                index += 8;
            }
        }
        return data;
    }

    private void readData(byte[] packet) {
        finalPageNumber = findPageNumber(new byte[] {packet[3], packet[2], packet[1], packet[0]}); // The first package indicates the total number of pages and the current page
        totalPackagesNumber = finalPageNumber * 9;
        Log.i(TAG, "Total Pages: " + finalPageNumber);
        rawDataCollector = new Snapshots(finalPageNumber * Snapshots.BYTES_PER_PAGE); // size is defined
        if (finalPageNumber == 0) { // No data to download
            setVisibility("menu");
            showPrintDialog("Message", "No data to download.", 1);
            parameter = "";
        } else {
            downloading = true;
            initDownloading();
            setVisibility("downloading");
            downloading_data_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
            downloading_data_imageView.setBackgroundResource(R.drawable.ic_circle);
            downloading_progressBar.setVisibility(View.VISIBLE);
        }
        secondParameter = ValueCodes.READY_DOWNLOAD;
        mBluetoothLeService.discoveringSecond();
    }

    private boolean isErrorPacket(byte[] packet) {
        return Converters.getHexValue(packet).equals("AA BB CC DD EE ");
    }

    private boolean isTransmissionDone(byte[] packet) {
        return Converters.getHexValue(packet).equals("DD 00 BB EE ");
    }

    /**
     * With the received packet, gets the raw data.
     * @param packet The received packet.
     */
    private void downloadData(byte[] packet) {
        Log.i(TAG, Converters.getHexValue(packet) + " " + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"
                + Calendar.getInstance().get(Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND) + "." + Calendar.getInstance().get(Calendar.MILLISECOND));
        if (packet.length == 4 && downloading) {
            checkPackets();
        } else if (downloading) {
            if (pagePackets.isEmpty()) {
                receiveHandler.postDelayed(() -> {
                    if (pagePackets.size() >= 9 && downloading) {
                        if (findPacketNumber(new byte[] {pagePackets.get(pagePackets.size() - 1)[228], pagePackets.get(pagePackets.size() - 1)[229]}) == 9) {
                            int number = findPageNumber(new byte[] {pagePackets.get(pagePackets.size() - 1)[224], pagePackets.get(pagePackets.size() - 1)[225], pagePackets.get(pagePackets.size() - 1)[226], pagePackets.get(pagePackets.size() - 1)[227]});
                            if (number == pageNumber) {
                                pageNumber++;
                                for (byte[] pagePacket : pagePackets) {
                                    number = findPacketNumber(new byte[] {pagePacket[228], pagePacket[229]});
                                    if (number == packetNumber)
                                        packetNumber++;
                                }
                                if (packetNumber == 10) {
                                    packetNumber = 1;
                                    for (byte[] pagePacket : pagePackets) {
                                        number = findPacketNumber(new byte[]{pagePacket[228], pagePacket[229]});
                                        if (number == packetNumber - 1) {
                                            packets.set(number - 1, pagePacket);
                                        } else {
                                            packets.add(pagePacket);
                                            packetNumber++;
                                        }
                                    }
                                    secondParameter = ValueCodes.PAGE_OK;
                                } else {
                                    Log.i(TAG, "Llegaron bien hasta el paquete " + (packetNumber - 1));
                                    pageNumber--;
                                    secondParameter = ValueCodes.PAGE_BAD;
                                }
                            } else {
                                Log.i(TAG, "La pagina esperada es " + pageNumber + " pero llego " + findPageNumber(new byte[] {pagePackets.get(pagePackets.size() - 1)[224], pagePackets.get(pagePackets.size() - 1)[225], pagePackets.get(pagePackets.size() - 1)[226], pagePackets.get(pagePackets.size() - 1)[227]}));
                                secondParameter = ValueCodes.PAGE_BAD;
                            }
                        } else {
                            Log.i(TAG, "El ultimo paquete no es 9 si no " + findPacketNumber(new byte[] {pagePackets.get(pagePackets.size() - 1)[228], pagePackets.get(pagePackets.size() - 1)[229]}));
                            secondParameter = ValueCodes.PAGE_BAD;
                        }
                    } else if (downloading) {
                        Log.i(TAG, "Solo llegaron " + pagePackets.size() + " paquetes de la pagina " + pageNumber);
                        secondParameter = ValueCodes.PAGE_BAD;
                    }
                    packetNumber = 1;
                    pagePackets = new ArrayList<>();
                    mBluetoothLeService.discoveringSecond();
                }, ValueCodes.DOWNLOAD_PERIOD);
            }
            pagePackets.add(packet);
        }
    }

    private void checkPackets() {
        parameter = "";
        downloading_data_imageView.setBackgroundResource(R.drawable.circle_check);
        downloading_progressBar.setVisibility(View.GONE);
        processing_data_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        processing_data_imageView.setBackgroundResource(R.drawable.ic_circle);
        processing_progressBar.setVisibility(View.VISIBLE);

        for (byte[] packet : packets) {
            byte[] newPacket;
            int extraNumbers = 2;
            if (packet.length == 5 && isErrorPacket(packet)) { //Shows an error when the packet contains 5 bytes and stops downloading
                setVisibility("menu");
                showPrintDialog("Error", "Download error (Packet error).", 1);
                downloading = false;
                error = true;
                return;
            } else { // Copy the downloaded package
                if (packetNumber % 9 == 0)
                    extraNumbers = 6;
                newPacket = new byte[packet.length - extraNumbers];
                System.arraycopy(packet, 0, newPacket, 0, packet.length - extraNumbers);
                rawDataCollector.processSnapshotRaw(newPacket);
                packetNumber++;
            }
        }
        if (!error) {
            Log.i(TAG, "FILLED: " + rawDataCollector.isFilled() + " Byte Position: " + rawDataCollector.byteIndex);
            snapshotArray.add(rawDataCollector);
            String processData = readPacket(rawDataCollector.getSnapshot());
            if (!error) { // Adds the data to the list if you didn't find any errors during processing
                byte[] data = Converters.convertToUTF8(processData);
                Snapshots processDataCollector = new Snapshots(data.length);
                processDataCollector.processSnapshot(data);
                snapshotArray.add(processDataCollector);
            }
            printSnapshotFiles();
        }
    }

    /**
     * Creates a file with the downloaded data.
     */
    private void printSnapshotFiles() {
        processing_data_imageView.setBackgroundResource(R.drawable.circle_check);
        processing_progressBar.setVisibility(View.GONE);
        preparing_file_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        preparing_file_imageView.setBackgroundResource(R.drawable.ic_circle);
        preparing_progressBar.setVisibility(View.VISIBLE);

        int i = 0;
        boolean outcome;
        String msg;
        try {
            root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack"); //set the directory path
            if (!root.exists()) {
                outcome = root.mkdirs();
                if (!outcome)
                    throw new Exception("Folder 'atstrack' can't be created.");
                root.setReadable(true);
                root.setWritable(true);
            }
            while(i < snapshotArray.size()) {
                fileName = snapshotArray.get(i).getFileName(); //get the fileName and create the file path
                newFile = new File(root.getAbsolutePath(), fileName);
                int copy = 1; //see if there's a possible copy
                while (!(newFile.createNewFile())) {
                    newFile = new File(root.getAbsolutePath(), fileName.substring(0, fileName.length() - 4) + " (" + copy + ").txt");
                    copy++;
                }
                newFile.setReadable(true);
                newFile.setWritable(true);
                stream = new FileOutputStream(newFile); //write in the file created
                stream.write(snapshotArray.get(i).getSnapshot());
                stream.flush(); //save the file
                stream.close();
                i++;
            }

            if (i == snapshotArray.size()) {
                preparing_file_imageView.setBackgroundResource(R.drawable.circle_check);
                preparing_progressBar.setVisibility(View.GONE);
                msg = "Download finished: " + (Snapshots.BYTES_PER_PAGE * pageNumber) + " byte(s) downloaded.";
                if (error) {
                    msg += " No data found in bytes downloaded. No file was generated. Total Number of Packages: " + packets.size() + ". Expected: " + (finalPageNumber * 9);
                    if (packets.size() != totalPackagesNumber)
                        msg += ". Timeout.";
                    else if (Snapshots.BYTES_PER_PAGE * pageNumber == Snapshots.BYTES_PER_PAGE * finalPageNumber)
                        msg += ". Not successfully.";
                    showPrintDialog("Finished", msg, 1);
                } else {
                    showPrintDialog("Finished", msg, 3);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "Error File: " + e.getLocalizedMessage());
        }
        downloading = false;
        setVisibility("downloaded");
    }

    /**
     * Displays a message indicating the status of the download.
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
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.catskill_white)));
    }

    /**
     * Shows google login window.
     */
    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(new Scope(DriveScopes.DRIVE_FILE)).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), ValueCodes.REQUEST_CODE_SIGN_IN);
    }

    /**
     * Access the drive files of the logged in account.
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