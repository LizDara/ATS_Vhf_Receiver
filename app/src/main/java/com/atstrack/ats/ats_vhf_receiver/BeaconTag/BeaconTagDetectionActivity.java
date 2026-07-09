package com.atstrack.ats.ats_vhf_receiver.BeaconTag;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothScannerActivity;
import com.atstrack.ats.ats_vhf_receiver.Fragments.TagsFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Services.DriveServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetail;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.ArrayList;

import butterknife.OnClick;

public class BeaconTagDetectionActivity extends BluetoothScannerActivity {

    private ArrayList<TagDetail> tags;
    private File root;
    private ArrayList<Data> dataList;

    @OnClick(R.id.btn_export_data)
    public void onClickExportData(View v) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_tag_detection;
        title = getString(R.string.tag_detection);
        super.onCreate(savedInstanceState);
        tags = new ArrayList<>();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fcv_activity_fragment, new TagsFragment(ValueCodes.BEACON, tags), String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ValueCodes.REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(root, dataList.get(0).fileName, this);
                driveServiceHelper.handleSignInIntent(data);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
            scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
            scanLeDevice(false);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initScanCallback() {
        super.initScanCallback();
        mLeScanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result.getDevice().getName() != null && result.getScanRecord().getBytes() != null && result.getDevice().getName().contains(ValueCodes.BEACON)) {
                    //Log.i(TAG, result.getDevice().getName() + " - " + result.getRssi() + " - " + Converters.getHexValue(result.getScanRecord().getBytes()));

                    byte[] finalArray = new byte[result.getScanRecord().getBytes().length + 1];
                    System.arraycopy(result.getScanRecord().getBytes(), 0, finalArray, 0, result.getScanRecord().getBytes().length);
                    finalArray[finalArray.length - 1] = (byte) result.getRssi();

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcv_activity_fragment);
                    if (currentFragment instanceof ReceiverCallback) {
                        runOnUiThread(() -> {
                            ((ReceiverCallback) currentFragment).onGattDataAvailable(finalArray);
                        });
                    }
                }
            }
        };
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
                builder.setNegativeButton("Cancel", null);
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