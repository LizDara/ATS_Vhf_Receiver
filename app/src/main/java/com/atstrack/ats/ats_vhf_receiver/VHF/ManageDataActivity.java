package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.DriveService.DriveServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
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
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.OnClick;

public class ManageDataActivity extends BaseActivity {

    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
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
    @BindView(R.id.message_complete_linearLayout)
    LinearLayout message_complete_linearLayout;
    @BindView(R.id.message_complete_textView)
    TextView message_complete_textView;
    @BindView(R.id.return_screen_button)
    Button return_screen_button;
    @BindView(R.id.first_step_imageView)
    ImageView first_step_imageView;
    @BindView(R.id.first_step_textView)
    TextView first_step_textView;
    @BindView(R.id.first_step_progressBar)
    ProgressBar first_step_progressBar;
    @BindView(R.id.second_step_imageView)
    ImageView second_step_imageView;
    @BindView(R.id.second_step_textView)
    TextView second_step_textView;
    @BindView(R.id.second_step_progressBar)
    ProgressBar second_step_progressBar;
    @BindView(R.id.third_step_imageView)
    ImageView third_step_imageView;
    @BindView(R.id.third_step_textView)
    TextView third_step_textView;
    @BindView(R.id.third_step_progressBar)
    ProgressBar third_step_progressBar;
    @BindView(R.id.download_percent_textView)
    TextView download_percent_textView;

    private final static String TAG = ManageDataActivity.class.getSimpleName();

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
    private int downloadPercent;

    private GattUpdateReceiver secondGattUpdateReceiver;
    private final ReceiverCallback secondReceiverCallback = new ReceiverCallback() {
        @Override
        public void onGattDisconnected() {}

        @Override
        public void onGattDiscovered() {
            switch (secondParameter) {
                case ValueCodes.DELETE: // Delete data
                    setDeleteData();
                    break;
                case ValueCodes.PAGES_NUMBER: // Read Final Page Number
                    TransferBleData.readPageNumber();
                    break;
                case ValueCodes.READY_DOWNLOAD: // Write that it is ready to download data
                    setStartDownload();
                    break;
                case ValueCodes.PAGE_OK: // Write that it received the page correctly
                    setResponsePage(true);
                    break;
                case ValueCodes.PAGE_BAD: // Write that it did not receive the page
                    setResponsePage(false);
                    break;
            }
        }

        @Override
        public void onGattDataAvailable(byte[] packet) {}
    };

    private void setNotification() {
        secondParameter = ValueCodes.PAGES_NUMBER;
        TransferBleData.downloadResponse();
        try {
            Thread.sleep(ValueCodes.WAITING_PERIOD);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        leServiceConnection.getBluetoothLeService().discoveringSecond();

        pageNumber = 0;
        packetNumber = 1; // 9 data packages of 230 bytes
        error = false;
        packets = new ArrayList<>();
        pagePackets = new ArrayList<>();
        snapshotArray = new ArrayList<>(); // The list that stores the raw and processed data
    }

    private void setStartDownload() {
        byte[] b = new byte[]{(byte) 0x94};
        boolean result = TransferBleData.writeResponse(b);
        if (result) secondParameter = "";
    }

    private void setResponsePage(boolean isOk) {
        byte[] b = new byte[]{isOk ? (byte) 0x95 : (byte) 0x96};
        boolean result = TransferBleData.writeResponse(b);
        Log.i(TAG, "-------------------------------------------------------------Is Ok: " + isOk + " Page number: " + (isOk ? (pageNumber - 1) : pageNumber));
        if (result) secondParameter = "";
    }

    private void setResponseErase() {
        secondParameter = ValueCodes.DELETE;
        TransferBleData.downloadResponse();
        try {
            Thread.sleep(ValueCodes.WAITING_PERIOD);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        leServiceConnection.getBluetoothLeService().discoveringSecond();
    }

    private void setDeleteData() {
        byte[] b = new byte[]{(byte) 0x93};
        boolean result = TransferBleData.writeResponse(b);
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
        leServiceConnection.getBluetoothLeService().discovering();
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
        leServiceConnection.getBluetoothLeService().discovering();
    }

    @OnClick(R.id.return_screen_button)
    public void onClickReturnScreen(View v) {
        setVisibility("menu");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_manage_data;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.manage_receiver_data);
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = ValueCodes.TEST;
        downloading = false;
        receiveHandler = new Handler();
        setVisibility("menu");
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                showDisconnectionMessage();
            }

            @Override
            public void onGattDiscovered() {
                switch (parameter) {
                    case ValueCodes.TEST: // Get memory used and byte stored
                        TransferBleData.readDataInfo();
                        break;
                    case ValueCodes.DOWNLOAD: // Download raw bytes
                        setNotification();
                        break;
                    case ValueCodes.DELETE_RESPONSE: // Response about erase data
                        setResponseErase();
                        break;
                }
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (Converters.getHexValue(packet[0]).equals("88")) return;
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
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
        secondGattUpdateReceiver = new GattUpdateReceiver(secondReceiverCallback, false);
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
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter(), 2);
            registerReceiver(secondGattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeSecondGattUpdateIntentFilter(), 2);
        } else {
            registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
            registerReceiver(secondGattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeSecondGattUpdateIntentFilter());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
        unregisterReceiver(secondGattUpdateReceiver.mGattUpdateReceiver);
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
            else if (download_complete_linearLayout.getVisibility() == View.VISIBLE || delete_linearLayout.getVisibility() == View.VISIBLE || message_complete_linearLayout.getVisibility() == View.VISIBLE)
                setVisibility("menu");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDisconnectionMessage() {
        parameter = "";
        secondParameter = "";
        if (downloading) {
            if (rawDataCollector.byteIndex == 0) {
                for (byte[] packet : packets)
                    rawDataCollector.processSnapshotRaw(packet);
            }
            root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
            Converters.printSnapshotFiles(root, snapshotArray);
        }
        Message.showDisconnectionMessage(this);
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
                message_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.manage_receiver_data);
                break;
            case "begin":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.VISIBLE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_download_receiver_data);
                break;
            case "downloading":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.VISIBLE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "downloaded":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.VISIBLE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "delete":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.VISIBLE);
                deleting_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_delete_receiver_data);
                break;
            case "deleting":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.VISIBLE);
                message_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "deleted":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                download_complete_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                deleting_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.VISIBLE);
                message_complete_textView.setText(R.string.lb_deletion_complete);
                return_screen_button.setText(R.string.lb_return_screen);
                break;
        }
    }

    /**
     * With the received packet, Gets memory used and byte stored and display on screen.
     * @param data The received packet.
     */
    private void downloadTest(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("52")) {
            int numberPage = findPageNumber(new byte[]{data[4], data[3], data[2], data[1]});
            int lastPage = findPageNumber(new byte[]{data[8], data[7], data[6], data[5]});
            memory_used_percent_textView.setText(((int) (((float) numberPage / (float) lastPage) * 100)) + "%");
            memory_used_progressBar.setProgress((int) ((((float) numberPage / (float) lastPage)) * 100));
            bytes_stored_textView.setText("Memory Used (" + (numberPage * 2048) + " bytes stored)");
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x89 ...");
        }
    }

    /**
     * Displays a message indicating whether the writing was successful.
     * @param data This packet indicates the writing status.
     */
    private void deleteResponse(byte[] data) {
        if (Converters.getHexValue(data).equals("DD 00 BB EE ")) {
            parameter = ValueCodes.TEST;
            leServiceConnection.getBluetoothLeService().discovering();
            setVisibility("deleted");
        } else {
            parameter = "";
            Message.showMessage(this, "Erase Data", "Not Completed.");
        }
    }

    private void initDownloading() {
        first_step_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        first_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        first_step_progressBar.setVisibility(View.GONE);
        second_step_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        second_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        second_step_progressBar.setVisibility(View.GONE);
        third_step_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        third_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        third_step_progressBar.setVisibility(View.GONE);
    }

    private void loadDownloading() {
        first_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        first_step_imageView.setBackgroundResource(R.drawable.ic_circle);
        first_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void loadProcessing() {
        first_step_imageView.setBackgroundResource(R.drawable.circle_check);
        first_step_progressBar.setVisibility(View.GONE);
        second_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        second_step_imageView.setBackgroundResource(R.drawable.ic_circle);
        second_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void loadPreparing() {
        second_step_imageView.setBackgroundResource(R.drawable.circle_check);
        second_step_progressBar.setVisibility(View.GONE);
        third_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        third_step_imageView.setBackgroundResource(R.drawable.ic_circle);
        third_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void downloaded() {
        third_step_imageView.setBackgroundResource(R.drawable.circle_check);
        third_step_progressBar.setVisibility(View.GONE);
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

    private void readData(byte[] packet) {
        finalPageNumber = findPageNumber(new byte[] {packet[3], packet[2], packet[1], packet[0]}); // The first package indicates the total number of pages and the current page
        totalPackagesNumber = finalPageNumber * 9;
        Log.i(TAG, "Total Pages: " + finalPageNumber);
        rawDataCollector = new Snapshots(finalPageNumber * Snapshots.BYTES_PER_PAGE); // size is defined
        if (finalPageNumber == 0) { // No data to download
            setVisibility("menu");
            Message.showMessage(this, "Message", "No data to download.");
            parameter = "";
        } else {
            download_percent_textView.setVisibility(View.VISIBLE);
            download_percent_textView.setText(" - 0%");
            downloading = true;
            initDownloading();
            setVisibility("downloading");
            loadDownloading();
        }
        secondParameter = ValueCodes.READY_DOWNLOAD;
        leServiceConnection.getBluetoothLeService().discoveringSecond();
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
        if (packet.length == 4 && downloading) {
            checkPackets();
        } else if (downloading) {
            if (pagePackets.isEmpty()) {
                receiveHandler.postDelayed(() -> {
                    secondParameter = ValueCodes.PAGE_BAD;
                    if (pagePackets.size() >= 9 && downloading) {
                        if (findPacketNumber(new byte[] {pagePackets.get(pagePackets.size() - 1)[228], pagePackets.get(pagePackets.size() - 1)[229]}) == 9) {
                            int number = findPageNumber(new byte[]{pagePackets.get(pagePackets.size() - 1)[224], pagePackets.get(pagePackets.size() - 1)[225], pagePackets.get(pagePackets.size() - 1)[226], pagePackets.get(pagePackets.size() - 1)[227]});
                            if (number == pageNumber) {
                                pageNumber++;
                                for (byte[] pagePacket : pagePackets) {
                                    number = findPacketNumber(new byte[]{pagePacket[228], pagePacket[229]});
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
                                    int percent = ((int) (((float) pageNumber / (float) finalPageNumber) * 100));
                                    download_percent_textView.setText(" - " + percent + "%");
                                    secondParameter = ValueCodes.PAGE_OK;
                                } else {
                                    Log.i(TAG, "No se encontraron los 9 paquetes");
                                    pageNumber--;
                                }
                            } else {
                                Log.i(TAG, "Numero de pagina que llego es " + number + ", numero esperado: " + pageNumber);
                            }
                        } else {
                            Log.i(TAG, "Ultimo paquete no es 9");
                        }
                    } else {
                        Log.i(TAG, "Llegaron " + pagePackets.size() + " paquetes");
                    }
                    packetNumber = 1;
                    pagePackets = new ArrayList<>();
                    leServiceConnection.getBluetoothLeService().discoveringSecond();
                }, ValueCodes.DOWNLOAD_PERIOD);
            }
            pagePackets.add(packet);
        }
    }

    private void checkPackets() {
        parameter = "";
        loadProcessing();

        for (byte[] packet : packets) {
            byte[] newPacket;
            int extraNumbers = 2;
            if (packet.length == 5 && isErrorPacket(packet)) { //Shows an error when the packet contains 5 bytes and stops downloading
                setVisibility("menu");
                Message.showMessage(this, "Error", "Download error (Packet error).");
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
            SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
            int baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
            String processData = Converters.readPacket(rawDataCollector.getSnapshot(), baseFrequency);
            byte[] data = Converters.convertToUTF8(processData);
            Snapshots processDataCollector = new Snapshots(data.length);
            processDataCollector.processSnapshot(data);
            snapshotArray.add(processDataCollector);

            loadPreparing();
            saveFiles();
        }
    }

    private void saveFiles() {
        root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack"); //set the directory path
        fileName = snapshotArray.get(snapshotArray.size() - 1).getFileName();
        boolean result = Converters.printSnapshotFiles(root, snapshotArray);

        downloaded();
        if (result) {
            String message = "Download finished: " + (Snapshots.BYTES_PER_PAGE * pageNumber) + " byte(s) downloaded.";
            if (error) {
                message += " No data found in bytes downloaded. No file was generated. Total Number of Packages: " + packets.size() + ". Expected: " + (finalPageNumber * 9);
                if (packets.size() != totalPackagesNumber)
                    message += ". Timeout.";
                else if (Snapshots.BYTES_PER_PAGE * pageNumber == Snapshots.BYTES_PER_PAGE * finalPageNumber)
                    message += ". Not successfully.";
                Message.showMessage(this, "Finished", message);
            } else {
                showPrintDialog(message, 1);
            }
        }
        downloading = false;
        setVisibility("downloaded");
    }

    /**
     * Displays a message indicating the status of the download.
     *
     * @param message   A short explanation of the status.
     * @param buttonNum Number of the type message.
     */
    private void showPrintDialog(String message, int buttonNum) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finished");
        builder.setMessage(message);
        switch (buttonNum) {
            case 2: // Save to the cloud
                builder.setPositiveButton("OK", (dialog, which) -> {
                    requestSignIn();
                });
                builder.setNegativeButton("Cancel", null);
                break;
            case 1: // Ask if you want to save file to the cloud
                builder.setPositiveButton("OK", (dialog, which) -> {
                    showPrintDialog("Do you want to send the file to the cloud?", 2);
                });
                break;
        }
        AlertDialog dialog = builder.create();
        dialog.show();
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
                    ManageDataActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));

            credential.setSelectedAccount(googleSignInAccount.getAccount());
            Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName("ATS Bridge").build();
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