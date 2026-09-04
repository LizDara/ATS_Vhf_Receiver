package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Models.Snapshots;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Services.DriveServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentDownloadingDataBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadingDataFragment extends Fragment implements ReceiverCallback {
    private FragmentDownloadingDataBinding binding = null;
    private File root;
    private ArrayList<byte[]> packets;
    private Timer downloadTimeout;
    private Handler receiveHandler;
    private int finalPageNumber;
    private int pageNumber;
    private int totalPackagesNumber;
    private int packetNumber;
    private boolean error;
    private boolean downloading;
    private boolean packetWaiting;
    private ArrayList<byte[]> pagePackets;
    private Data rawData;
    private Data processedData;
    private Data metricsData;
    private Data logData;
    private ArrayList<Data> dataList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDownloadingDataBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnCancelDownload.setOnClickListener(v -> {
            downloading = false;
            TransferBleData.downloadResponse(false);
            showAlertDialog("Download Timeout", "Do you want to save the downloaded bytes?", 3);
        });
        setVisibility(ValueCodes.DOWNLOADING);
        TransferBleData.requestConnectionPriority();
        try {
            Thread.sleep(ValueCodes.WAITING_PERIOD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setNotification();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ValueCodes.REQUEST_CODE_SIGN_IN) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(root, dataList.get(1).fileName, requireContext());
                driveServiceHelper.handleSignInIntent(data, "");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                .hide(this)
                                .add(R.id.fcv_activity_fragment, new SuccessfulMessageFragment(ValueCodes.DOWNLOAD))
                                .addToBackStack(String.valueOf(ValueCodes.THIRD_STEP))
                                .commit();
                    }
                }, ValueCodes.BRANDING_PERIOD);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onGattDisconnected() {
        if (downloading) {
            downloading = false;
            saveRawData();
        }
    }

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                switch (packet[0]) {
                    case ValueCodes.STORAGE_RESPONSE_COMMAND: // Get delete or download response
                        if (isTransmissionDone(packet)) {
                            successfulResponse();
                            break;
                        }
                    case ValueCodes.STORAGE_ERROR_COMMAND:
                        if (packet.length == 5 && isErrorPacket(packet)) { // Show an error when the packet contains 5 bytes and stops downloading
                            error = true;
                            downloading = false;
                            goBackMainMenu();
                            TransferBleData.downloadResponse(false);
                            showAlertDialog("Error", "Download error (Packet error).");
                            break;
                        }
                    default: // Get raw data in pages, each page contains 2048 bytes. 9 packets of 230 bytes is a page
                        if (packet.length > 4)
                            downloadRawData(packet);
                        else if (packet.length == 4)// Get pages total number
                            downloadPagesTotalNumber(packet);
                        break;
                }
            }
        });
    }

    private void goBackMainMenu() {
        if (getParentFragmentManager() != null) {
            Fragment fragment1 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.FIRST_STEP));
            Fragment fragment2 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.SECOND_STEP));
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            if (fragment2 != null) transaction.remove(fragment2);
            transaction.remove(this);
            if (fragment1 != null)
                transaction.show(fragment1);
            transaction.commit();
        }
    }

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
        packetWaiting = false;
        packets = new ArrayList<>();
        pagePackets = new ArrayList<>();
        rawData = new Data(ValueCodes.RAW_FILE);
        processedData = new Data(ValueCodes.PROCESSED_FILE);
        metricsData = new Data(ValueCodes.METRICS_FILE);
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
        Log.i("DownloadFragment", "----------------------------------Is Ok: " + isOk + " Page number: " + (isOk ? (pageNumber - 1) : pageNumber));
    }

    private void setVisibility(int view) {
        switch (view) {
            case ValueCodes.DOWNLOADING:
                binding.includeDownloadingProcess.tvFirstStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
                binding.includeDownloadingProcess.tvFirstStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
                binding.includeDownloadingProcess.pbFirstStep.setVisibility(View.GONE);
                binding.includeDownloadingProcess.tvSecondStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
                binding.includeDownloadingProcess.tvSecondStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
                binding.includeDownloadingProcess.pbSecondStep.setVisibility(View.GONE);
                binding.includeDownloadingProcess.tvThirdStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
                binding.includeDownloadingProcess.tvThirdStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
                binding.includeDownloadingProcess.pbThirdStep.setVisibility(View.GONE);
                break;
            case ValueCodes.FIRST_STEP:
                binding.includeDownloadingProcess.tvFirstStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
                binding.includeDownloadingProcess.tvFirstStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
                binding.includeDownloadingProcess.pbFirstStep.setVisibility(View.VISIBLE);
                break;
            case ValueCodes.SECOND_STEP:
                binding.includeDownloadingProcess.tvFirstStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
                binding.includeDownloadingProcess.pbFirstStep.setVisibility(View.GONE);

                binding.includeDownloadingProcess.tvSecondStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
                binding.includeDownloadingProcess.tvSecondStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
                binding.includeDownloadingProcess.pbSecondStep.setVisibility(View.VISIBLE);
                break;
            case ValueCodes.THIRD_STEP:
                binding.includeDownloadingProcess.tvSecondStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
                binding.includeDownloadingProcess.pbSecondStep.setVisibility(View.GONE);

                binding.includeDownloadingProcess.tvThirdStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
                binding.includeDownloadingProcess.tvThirdStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
                binding.includeDownloadingProcess.pbThirdStep.setVisibility(View.VISIBLE);
                break;
            case ValueCodes.FOURTH_STEP:
                binding.includeDownloadingProcess.tvThirdStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
                binding.includeDownloadingProcess.pbThirdStep.setVisibility(View.GONE);
                break;
        }
    }

    private void successfulResponse() {
        if (downloading) {
            setVisibility(ValueCodes.SECOND_STEP);
            receiveHandler.postDelayed(() -> checkPages(), ValueCodes.DOWNLOAD_PERIOD);
        }
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
        finalPageNumber = Converters.findPageNumber(new byte[] {packet[3], packet[2], packet[1], packet[0]}) * 2; // The first package indicates the total number of pages and the current page
        totalPackagesNumber = finalPageNumber * 9;
        downloading = finalPageNumber > 0;
        receiveHandler = new Handler(Looper.getMainLooper());
        downloadTimeout = new Timer();
        if (downloading) {
            binding.includeDownloadingProcess.tvDownloadPercent.setVisibility(View.VISIBLE);
            binding.includeDownloadingProcess.tvProcessPercent.setVisibility(View.VISIBLE);
            binding.includeDownloadingProcess.tvDownloadPercent.setText(" - 0%");
            binding.includeDownloadingProcess.tvProcessPercent.setText("");
            setVisibility(ValueCodes.FIRST_STEP);
            setStartDownload();
        } else { // No data to download
            goBackMainMenu();
            showAlertDialog("Message", "No data to download.");
        }
    }

    private boolean isErrorPacket(byte[] packet) {
        return Converters.getHexValue(packet).equals("AA BB CC DD EE ");
    }

    private boolean isTransmissionDone(byte[] packet) {
        return Converters.getHexValue(packet).equals("DD 00 BB EE ");
    }

    private void downloadRawData(byte[] packet) {
        if (downloading) {
            if (pagePackets.isEmpty()) {
                packetWaiting = true;
                downloadTimeout = new Timer();
                downloadTimeout.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkPackets();
                        downloadTimeout.cancel();
                        downloadTimeout.purge();
                        downloadTimeout = null;
                    }
                }, ValueCodes.DOWNLOAD_PERIOD);
            }
            if (packetWaiting) {
                pagePackets.add(packet);
                if (pagePackets.size() >= 9 && findPacketNumber(new byte[] {pagePackets.get(pagePackets.size() - 1)[228], pagePackets.get(pagePackets.size() - 1)[229]}) == 9) {
                    downloadTimeout.cancel();
                    downloadTimeout.purge();
                    downloadTimeout = null;
                    checkPackets();
                }
            }
        }
    }

    private void checkPackets() {
        boolean isOk = false;
        packetWaiting = false;
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
                            if (number < packetNumber) {
                                packets.set((number - 1) + ((pageNumber - 1) * 9), pagePacket);
                            } else if (number == packetNumber) {
                                packets.add(pagePacket);
                                packetNumber++;
                            }
                        }
                        int percent = (int) (((float) pageNumber / (float) finalPageNumber) * 100);
                        binding.includeDownloadingProcess.tvDownloadPercent.setText(" - " + percent + "%");
                        isOk = true;
                        LeServiceConnection.getInstance().getBluetoothLeService().downloadLogs += "Page " + pageNumber + " downloaded successfully." + ValueCodes.CR + ValueCodes.LF;
                    } else {
                        LeServiceConnection.getInstance().getBluetoothLeService().downloadLogs += "The 9 packages were not found." + ValueCodes.CR + ValueCodes.LF;
                        pageNumber--;
                    }
                } else {
                    LeServiceConnection.getInstance().getBluetoothLeService().downloadLogs += "Page number " + (number + 1) + " was received, expected number is " + (pageNumber + 1) + ValueCodes.CR + ValueCodes.LF;
                }
            } else {
                LeServiceConnection.getInstance().getBluetoothLeService().downloadLogs += "The last package received is not 9" + ValueCodes.CR + ValueCodes.LF;
            }
        } else {
            LeServiceConnection.getInstance().getBluetoothLeService().downloadLogs += "Only " + pagePackets.size() + " packages arrived, 9 were expected." + ValueCodes.CR + ValueCodes.LF;
        }
        packetNumber = 1;
        pagePackets = new ArrayList<>();
        if (downloading)
            setResponsePage(isOk);
    }

    private void checkPages() {
        binding.includeDownloadingProcess.tvProcessPercent.setText(" - 0%");
        fillRawData();
        binding.includeDownloadingProcess.tvProcessPercent.setText(" - 1%");
        new Thread(() -> {
            if (!error) {
                dataList.add(rawData);
                String[] texts = Converters.getPackageProcessed(rawData.packets, binding.includeDownloadingProcess.tvProcessPercent, (BaseActivity) requireActivity(), false);
                byte[] processed = Converters.convertToUTF8(texts[0]);
                byte[] metrics = Converters.convertToUTF8(texts[1]);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && getView() != null) {
                        binding.includeDownloadingProcess.tvProcessPercent.setText(" - 100%");
                        processedData.packets.add(processed);
                        metricsData.packets.add(metrics);
                        dataList.add(processedData);
                        dataList.add(metricsData);

                        setVisibility(ValueCodes.THIRD_STEP);
                        receiveHandler.postDelayed(() -> saveFiles(), ValueCodes.DOWNLOAD_PERIOD);
                    }
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

        setVisibility(ValueCodes.FOURTH_STEP);
        if (result) {
            String message = "Download finished: " + (Snapshots.BYTES_PER_PAGE * pageNumber) + " byte(s) downloaded.";
            if (error) {
                message += " No data found in bytes downloaded. No file was generated. Total Number of Packages: " + rawData.packets.size() + ". Expected: " + (finalPageNumber * 9);
                if (rawData.packets.size() != totalPackagesNumber)
                    message += ". Timeout.";
                else if (Snapshots.BYTES_PER_PAGE * pageNumber == Snapshots.BYTES_PER_PAGE * finalPageNumber)
                    message += ". Not successfully.";
                showAlertDialog("Finished", message);
            } else {
                showAlertDialog("Finished", message, 1);
            }
        }
    }

    private void saveRawData() {
        fillRawData();
        dataList.add(rawData);
        root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
        Converters.printDataFiles(root, dataList);
    }

    private void showAlertDialog(String title, String message, int buttonNum) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);
        builder.setMessage(message);
        switch (buttonNum) {
            case 2: // Save to the cloud
                builder.setPositiveButton("OK", (dialog, which) -> {
                    requestSignIn();
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                .hide(this)
                                .add(R.id.fcv_activity_fragment, new SuccessfulMessageFragment(ValueCodes.DOWNLOAD))
                                .addToBackStack(String.valueOf(ValueCodes.THIRD_STEP))
                                .commit();
                    }
                });
                break;
            case 1: // Ask if you want to save file to the cloud
                builder.setPositiveButton("OK", (dialog, which) -> {
                    showAlertDialog("Google Drive", "Do you want to send the file to the cloud?", 2);
                });
                break;
            case 3: // Cancel download. Save the download bytes
                builder.setPositiveButton("OK", (dialog, which) -> {
                    byte[] data = Converters.convertToUTF8(LeServiceConnection.getInstance().getBluetoothLeService().downloadLogs);
                    logData.packets.add(data);
                    dataList.add(logData);
                    saveRawData();
                    goBackMainMenu();
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    goBackMainMenu();
                });
                break;
        }
        AlertDialog dialog = builder.create();
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
        dialog.show();
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog dialog = Dialogs.createAlertDialog(requireActivity(), title, message, false);
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(new Scope(DriveScopes.DRIVE_FILE)).build();
        GoogleSignInClient client = GoogleSignIn.getClient(requireActivity(), signInOptions);
        startActivityForResult(client.getSignInIntent(), ValueCodes.REQUEST_CODE_SIGN_IN);
    }
}
