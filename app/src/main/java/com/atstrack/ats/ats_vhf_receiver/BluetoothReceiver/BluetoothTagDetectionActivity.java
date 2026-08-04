package com.atstrack.ats.ats_vhf_receiver.BluetoothReceiver;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.TagsFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Services.DriveServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetail;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityTagDetectionBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.ArrayList;

public class BluetoothTagDetectionActivity extends BaseActivity {
    private ArrayList<TagDetail> tags;
    private File root;
    private ArrayList<Data> dataList;
    private boolean disconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        title = getString(R.string.title_bt_tag_detection);
        deviceCategory = ValueCodes.BLUETOOTH_RECEIVER;
        binding = ActivityTagDetectionBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        tags = new ArrayList<>();
        disconnect = false;
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fcv_activity_fragment, new TagsFragment(ValueCodes.BLUETOOTH_RECEIVER, tags), String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }

        ((ActivityTagDetectionBinding) binding).btnExportData.setOnClickListener(v -> saveFile());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ValueCodes.REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(root, dataList.get(0).fileName, this);
                driveServiceHelper.handleSignInIntent(data, disconnect ? ValueCodes.BLUETOOTH_RECEIVER : "");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                disconnect = true;
                saveFile();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void discoverCharacteristic() {
        TransferBleData.requestMtu(247, false);
        new Handler(getMainLooper()).postDelayed(() -> TransferBleData.receiveTags(true), ValueCodes.WAITING_PERIOD);
    }

    @Override
    protected void downloadData(byte[] data) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcv_activity_fragment);
        if (currentFragment instanceof ReceiverCallback) {
            runOnUiThread(() -> {
                ((ReceiverCallback) currentFragment).onGattDataAvailable(data);
            });
        }
    }

    private void saveFile() {
        String text = Converters.getTagsData(tags);
        byte[] data = Converters.convertToUTF8(text);
        Data processedData = new Data(ValueCodes.BLUETOOTH_FILE);
        processedData.packets.add(data);
        dataList = new ArrayList<>();
        dataList.add(processedData);
        String fileName = processedData.fileName;
        root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack"); //set the directory path
        boolean result = Converters.printDataFiles(root, dataList);
        if (result) {
            String message = "File saved as " + fileName;
            showAlertDialog("Finished", message, 1);
        }
    }

    private void showAlertDialog(String title, String message, int buttonNum) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        switch (buttonNum) {
            case 2: // Save to the cloud
                builder.setPositiveButton("OK", (dialog, which) -> {
                    requestSignIn();
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    if (disconnect)
                        leServiceConnection.getBluetoothLeService().disconnect();
                });
                break;
            case 1: // Ask if you want to save file to the cloud
                builder.setPositiveButton("OK", (dialog, which) -> {
                    showAlertDialog("Google Drive", "Do you want to send the file to the cloud?", 2);
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