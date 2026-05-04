package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Services.DriveServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Utils.Messages;
import com.atstrack.ats.ats_vhf_receiver.Models.Snapshots;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.ArrayList;

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
    @BindView(R.id.delete_linearLayout)
    LinearLayout delete_linearLayout;
    @BindView(R.id.loading_message_linearLayout)
    LinearLayout loading_message_linearLayout;
    @BindView(R.id.state_loading_textView)
    TextView state_loading_textView;
    @BindView(R.id.message_complete_linearLayout)
    LinearLayout message_complete_linearLayout;
    @BindView(R.id.message_complete_textView)
    TextView message_complete_textView;
    @BindView(R.id.main_complete_button)
    Button main_complete_button;
    @BindView(R.id.first_step_textView)
    TextView first_step_textView;
    @BindView(R.id.first_step_progressBar)
    ProgressBar first_step_progressBar;
    @BindView(R.id.second_step_textView)
    TextView second_step_textView;
    @BindView(R.id.second_step_progressBar)
    ProgressBar second_step_progressBar;
    @BindView(R.id.third_step_textView)
    TextView third_step_textView;
    @BindView(R.id.third_step_progressBar)
    ProgressBar third_step_progressBar;
    @BindView(R.id.download_percent_textView)
    TextView download_percent_textView;
    @BindView(R.id.process_percent_textView)
    TextView process_percent_textView;
    @BindView(R.id.return_textView)
    TextView return_textView;

    private final static String TAG = ManageDataActivity.class.getSimpleName();

    private File root;
    private ArrayList<byte[]> packets;
    private Handler receiveHandler;
    private int finalPageNumber;
    private int pageNumber;
    private int totalPackagesNumber;
    private int packetNumber;
    private boolean error;
    private boolean downloading;
    private ArrayList<byte[]> pagePackets;
    private Data rawData;
    private Data processedData;
    private Data logData;
    private ArrayList<Data> dataList;

    private void setNotification() {
        TransferBleData.downloadResponse(true);
        try {
            Thread.sleep(ValueCodes.WAITING_PERIOD);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TransferBleData.readPageNumber();

        pageNumber = 0;
        packetNumber = 1; // 9 data packages of 230 bytes
        error = false;
        packets = new ArrayList<>();
        pagePackets = new ArrayList<>();
        rawData = new Data(ValueCodes.RAW_FILE);
        processedData = new Data(ValueCodes.PROCESSED_FILE);
        logData = new Data(ValueCodes.LOG_FILE);
        dataList = new ArrayList<>();
    }

    private void setStartDownload() {
        byte[] b = new byte[]{(byte) 0x94};
        TransferBleData.writeResponse(b);
    }

    private void setResponsePage(boolean isOk) {
        byte[] b = new byte[]{isOk ? (byte) 0x95 : (byte) 0x96};
        TransferBleData.writeResponse(b);
        Log.i(TAG, "-------------------------------------------------------------Is Ok: " + isOk + " Page number: " + (isOk ? (pageNumber - 1) : pageNumber));
    }

    private void setResponseErase() {
        TransferBleData.downloadResponse(true);
        try {
            Thread.sleep(ValueCodes.WAITING_PERIOD);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        setDeleteData();
    }

    private void setDeleteData() {
        byte[] b = new byte[]{(byte) 0x93};
        boolean result = TransferBleData.writeResponse(b);
        if (result)
            setVisibility("deleting");
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
            Messages.showMessage(this, "Erase Data", "There is no data to delete.");
        }
    }

    @OnClick(R.id.begin_download_button)
    public void onClickBeginDownload(View v) {
        setNotification();
    }

    @OnClick(R.id.cancel_download_button)
    public void onClickCancelDownload(View v) {
        downloading = false;
        TransferBleData.downloadResponse(false);
        showPrintDialog("Download Timeout", "Do you want to save the downloaded bytes?", 3);
    }

    @OnClick(R.id.return_textView)
    public void onClickReturn(View v) {
        downloading = false;
        setVisibility("menu");
    }

    @OnClick(R.id.delete_receiver_button)
    public void onClickDeleteReceiver(View v) {
        setResponseErase();
    }

    @OnClick(R.id.main_complete_button)
    public void onClickMainComplete(View v) {
        if (!downloading)
            setVisibility("menu");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_manage_data;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.manage_receiver_data);
        super.onCreate(savedInstanceState);

        parameter = ValueCodes.STORAGE_COMMAND;
        downloading = false;
        receiveHandler = new Handler();
        setVisibility("menu");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ValueCodes.REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(root, dataList.get(1).fileName, this);
                driveServiceHelper.handleSignInIntent(data);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (menu_manage_receiver_linearLayout.getVisibility() == View.VISIBLE)
                finish();
            else if (downloading_file_linearLayout.getVisibility() == View.VISIBLE)
                setVisibility("begin");
            else if (begin_download_linearLayout.getVisibility() == View.VISIBLE || delete_linearLayout.getVisibility() == View.VISIBLE || message_complete_linearLayout.getVisibility() == View.VISIBLE)
                setVisibility("menu");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void gattDisconnected() {
        if (downloading) {
            downloading = false;
            saveRawData();
        }
        super.gattDisconnected();
    }

    @Override
    protected void discoverCharacteristic() {
        if (parameter == ValueCodes.STORAGE_COMMAND)
            TransferBleData.readDataInfo();
    }

    @Override
    protected void downloadData(byte[] data) {
        super.downloadData(data);
        switch (data[0]) {
            case ValueCodes.STORAGE_COMMAND:
                if (data.length < 230) {
                    downloadTest(data);
                    break;
                }
            case ValueCodes.STORAGE_RESPONSE_COMMAND: // Get delete or download response
                if (isTransmissionDone(data)) {
                    successfulResponse(data);
                    break;
                } else if (!downloading && data.length < 230) {
                    Messages.showMessage(this, "Erase Data", "Not Completed.");
                    break;
                }
            case ValueCodes.STORAGE_ERROR_COMMAND:
                if (data.length == 5 && isErrorPacket(data)) { // Show an error when the packet contains 5 bytes and stops downloading
                    error = true;
                    downloading = false;
                    setVisibility("menu");
                    TransferBleData.downloadResponse(false);
                    Messages.showMessage(getParent(), "Error", "Download error (Packet error).");
                    break;
                }
            default: // Get raw data in pages, each page contains 2048 bytes. 9 packets of 230 bytes is a page
                if (data.length > 4)
                    downloadRawData(data);
                else if (data.length == 4)// Get pages total number
                    downloadPagesTotalNumber(data);
                break;
        }
    }

    private void setVisibility(String value) {
        switch (value) {
            case "menu":
                menu_manage_receiver_linearLayout.setVisibility(View.VISIBLE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                loading_message_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.manage_receiver_data);
                break;
            case "begin":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.VISIBLE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                loading_message_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_download_receiver_data);
                break;
            case "downloading":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.VISIBLE);
                delete_linearLayout.setVisibility(View.GONE);
                loading_message_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "downloaded":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                loading_message_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.VISIBLE);
                message_complete_textView.setText(R.string.lb_download_complete);
                main_complete_button.setText(R.string.lb_open_file);
                return_textView.setVisibility(View.VISIBLE);
                break;
            case "delete":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.VISIBLE);
                loading_message_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                title_toolbar.setText(R.string.lb_delete_receiver_data);
                break;
            case "deleting":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                loading_message_linearLayout.setVisibility(View.VISIBLE);
                message_complete_linearLayout.setVisibility(View.GONE);
                state_loading_textView.setText(R.string.lb_deleting);
                break;
            case "deleted":
                menu_manage_receiver_linearLayout.setVisibility(View.GONE);
                begin_download_linearLayout.setVisibility(View.GONE);
                downloading_file_linearLayout.setVisibility(View.GONE);
                delete_linearLayout.setVisibility(View.GONE);
                loading_message_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.VISIBLE);
                message_complete_textView.setText(R.string.lb_deletion_complete);
                main_complete_button.setText(R.string.lb_return_screen);
                return_textView.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * With the received packet, Gets memory used and byte stored and display on screen.
     * @param data The received packet.
     */
    private void downloadTest(byte[] data) {
        parameter = ValueCodes.NONE;
        int numberPage = Converters.findPageNumber(new byte[]{data[4], data[3], data[2], data[1]});
        int lastPage = Converters.findPageNumber(new byte[]{data[8], data[7], data[6], data[5]});
        memory_used_percent_textView.setText(((int) (((float) numberPage / (float) lastPage) * 100)) + "%");
        memory_used_progressBar.setProgress((int) ((((float) numberPage / (float) lastPage)) * 100));
        bytes_stored_textView.setText("Memory Used (" + (numberPage * 2048) + " bytes stored)");
    }

    /**
     * Displays a message indicating whether the writing was successful.
     * @param data This packet indicates the writing status.
     */
    private void successfulResponse(byte[] data) {
        if (downloading) {
            loadProcessing();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                checkPackets();
            }, ValueCodes.DOWNLOAD_PERIOD);
        } else {
            TransferBleData.readDataInfo();
            setVisibility("deleted");
        }
    }

    private void initDownloading() {
        first_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
        first_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        first_step_progressBar.setVisibility(View.GONE);

        second_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
        second_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        second_step_progressBar.setVisibility(View.GONE);

        third_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
        third_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        third_step_progressBar.setVisibility(View.GONE);
    }

    private void loadDownloading() {
        first_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
        first_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        first_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void loadProcessing() {
        first_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
        first_step_progressBar.setVisibility(View.GONE);

        second_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
        second_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        second_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void loadPreparing() {
        second_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
        second_step_progressBar.setVisibility(View.GONE);

        second_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
        third_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        third_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void downloaded() {
        second_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
        third_step_progressBar.setVisibility(View.GONE);
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

    private void downloadPagesTotalNumber(byte[] packet) {
        finalPageNumber = Converters.findPageNumber(new byte[] {packet[3], packet[2], packet[1], packet[0]}); // The first package indicates the total number of pages and the current page
        totalPackagesNumber = finalPageNumber * 9;
        downloading = finalPageNumber > 0;
        if (downloading) {
            download_percent_textView.setVisibility(View.VISIBLE);
            process_percent_textView.setVisibility(View.VISIBLE);
            download_percent_textView.setText(" - 0%");
            process_percent_textView.setText("");
            initDownloading();
            setVisibility("downloading");
            loadDownloading();
            setStartDownload();
        } else { // No data to download
            setVisibility("menu");
            Messages.showMessage(this, "Message", "No data to download.");
        }
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
    private void downloadRawData(byte[] packet) {
        if (downloading) {
            if (pagePackets.isEmpty()) {
                receiveHandler.postDelayed(() -> {
                    boolean isOk = false;
                    if (pagePackets.size() >= 9 && downloading) {
                        if (findPacketNumber(new byte[] {pagePackets.get(pagePackets.size() - 1)[228], pagePackets.get(pagePackets.size() - 1)[229]}) == 9) {
                            int number = Converters.findPageNumber(new byte[]{pagePackets.get(pagePackets.size() - 1)[224], pagePackets.get(pagePackets.size() - 1)[225], pagePackets.get(pagePackets.size() - 1)[226], pagePackets.get(pagePackets.size() - 1)[227]});
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
                                            packets.set((number - 1) + ((pageNumber - 1) * 9), pagePacket);
                                        } else {
                                            packets.add(pagePacket);
                                            packetNumber++;
                                        }
                                    }
                                    int percent = (int) (((float) pageNumber / (float) finalPageNumber) * 100);
                                    download_percent_textView.setText(" - " + percent + "%");
                                    isOk = true;
                                    leServiceConnection.getBluetoothLeService().downloadLogs += "Page " + pageNumber + " downloaded successfully." + ValueCodes.CR + ValueCodes.LF;
                                } else {
                                    leServiceConnection.getBluetoothLeService().downloadLogs += "The 9 packages were not found." + ValueCodes.CR + ValueCodes.LF;
                                    pageNumber--;
                                }
                            } else {
                                leServiceConnection.getBluetoothLeService().downloadLogs += "Page number " + (number + 1) + " was received, expected number is " + (pageNumber + 1) + ValueCodes.CR + ValueCodes.LF;
                            }
                        } else {
                            leServiceConnection.getBluetoothLeService().downloadLogs += "The last package received is not 9" + ValueCodes.CR + ValueCodes.LF;
                        }
                    } else {
                        leServiceConnection.getBluetoothLeService().downloadLogs += "Only " + pagePackets.size() + " packages arrived, 9 were expected." + ValueCodes.CR + ValueCodes.LF;
                    }
                    packetNumber = 1;
                    pagePackets = new ArrayList<>();
                    if (downloading)
                        setResponsePage(isOk);
                }, ValueCodes.DOWNLOAD_PERIOD);
            }
            pagePackets.add(packet);
        }
    }

    private void checkPackets() {
        process_percent_textView.setText(" - 0%");
        fillRawData();
        process_percent_textView.setText(" - 1%");
        new Thread(() -> {
            if (!error) {
                dataList.add(rawData);
                String processData = Converters.getPackageProcessed(rawData.packets, process_percent_textView, this, false);
                byte[] data = Converters.convertToUTF8(processData);
                runOnUiThread(() -> {
                    process_percent_textView.setText(" - 100%");
                    processedData.packets.add(data);
                    dataList.add(processedData);

                    loadPreparing();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        saveFiles();
                    }, ValueCodes.DOWNLOAD_PERIOD);
                });
            }
        }).start();
    }

    private void fillRawData() {
        Snapshots snapshot = new Snapshots();
        for (byte[] packet : packets) {
            snapshot.processSnapshot(packet);
            if (snapshot.isFilled()) {
                rawData.packets.add(snapshot.getSnapshot());
                snapshot = new Snapshots();
            }
        }
    }

    private void saveFiles() {
        root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack"); //set the directory path
        boolean result = Converters.printDataFiles(root, dataList);

        downloaded();
        if (result) {
            String message = "Download finished: " + (Snapshots.BYTES_PER_PAGE * pageNumber) + " byte(s) downloaded.";
            if (error) {
                message += " No data found in bytes downloaded. No file was generated. Total Number of Packages: " + rawData.packets.size() + ". Expected: " + (finalPageNumber * 9);
                if (rawData.packets.size() != totalPackagesNumber)
                    message += ". Timeout.";
                else if (Snapshots.BYTES_PER_PAGE * pageNumber == Snapshots.BYTES_PER_PAGE * finalPageNumber)
                    message += ". Not successfully.";
                Messages.showMessage(this, "Finished", message);
            } else {
                showPrintDialog("Finished", message, 1);
            }
        }
    }

    private void saveRawData() {
        fillRawData();
        dataList.add(rawData);
        root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
        Converters.printDataFiles(root, dataList);
    }

    /**
     * Displays a message indicating the status of the download.
     *
     * @param message   A short explanation of the status.
     * @param buttonNum Number of the type message.
     */
    private void showPrintDialog(String title, String message, int buttonNum) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
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
                    setVisibility("downloaded");
                    showPrintDialog("Google Drive", "Do you want to send the file to the cloud?", 2);
                });
                break;
            case 3: // Cancel download. Save the download bytes
                builder.setPositiveButton("OK", (dialog, which) -> {
                    byte[] data = Converters.convertToUTF8(leServiceConnection.getBluetoothLeService().downloadLogs);
                    logData.packets.add(data);
                    dataList.add(logData);
                    saveRawData();
                    setVisibility("menu");
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    setVisibility("begin");
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
}