package com.atstrack.ats.ats_vhf_receiver.BluetoothReceiver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TagListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Services.DriveServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetail;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Services.AudioService;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationServices;
import com.google.api.services.drive.DriveScopes;

import android.location.Location;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class BluetoothTagDetectionActivity extends BaseActivity {

    @BindView(R.id.location_data_imageView)
    ImageView location_data_imageView;
    @BindView(R.id.location_data_textView)
    TextView location_data_textView;
    @BindView(R.id.coordinates_textView)
    TextView coordinates_textView;
    @BindView(R.id.location_data_button)
    Button location_data_button;
    @BindView(R.id.item_recyclerView)
    RecyclerView item_recyclerView;

    private ArrayList<TagDetail> tags;
    private TagListAdapter tagListAdapter;
    private boolean gpsEnable;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double latitude, longitude = 0;
    private File root;
    private ArrayList<Data> dataList;

    @OnClick(R.id.location_data_button)
    public void onClickLocation(View v) {
        /*gpsEnable = !enable;
        location_data_imageView.setBackground(ContextCompat.getDrawable(this, gpsEnable ? R.drawable.ic_gps_valid : R.drawable.ic_gps_off));
        location_data_textView.setText(gpsEnable ? R.string.lb_location_enabled : R.string.lb_location_disabled);
        coordinates_textView.setText(gpsEnable ? "00.000000, -00.000000" : getString(R.string.lb_location_unknown));
        location_data_button.setText(gpsEnable ? R.string.lb_disable : R.string.lb_enable);*/
    }

    @OnClick(R.id.export_data_button)
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
            showPrintDialog("Finished", message, 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_tag_detection;
        showToolbar = true;
        title = getString(R.string.tag_detection);
        deviceCategory = ValueCodes.BLUETOOTH_RECEIVER;
        super.onCreate(savedInstanceState);

        gpsEnable = true;
        tagListAdapter = new TagListAdapter(this);
        tags = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        item_recyclerView.setLayoutManager(manager);
        item_recyclerView.setHasFixedSize(true);
        item_recyclerView.setAdapter(tagListAdapter);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    coordinates_textView.setText(latitude + ", " + longitude);
                }
            }
        };
        startGpsUpdates();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // Disconnect
            TransferBleData.receiveTags(false);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void gattDisconnected() {
        unbindService(leServiceConnection.getServiceConnection());
        super.gattDisconnected();
    }

    @Override
    protected void discoverCharacteristic() {
        TransferBleData.receiveTags(true);
    }

    @Override
    protected void downloadData(byte[] data) {
        setDetectionTagsData(data);
    }

    private void startGpsUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000) // Intervalo de 3 segundos PRIORITY_BALANCED_POWER_ACCURACY
                .setMinUpdateIntervalMillis(1500) // Mínimo cada 2 segundos
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void setDetectionTagsData(byte[] data) {
        int position = tagListAdapter.getItemCount();
        String code = Converters.getAsciiValue(6, 14, data);
        for (int i = 0; i < tagListAdapter.getItemCount(); i++) {
            if (tagListAdapter.getTag(i).code.equals(code))
                position = i;
        }
        long currentTimestamp = System.currentTimeMillis();
        tags.add(new TagDetail(data, latitude, longitude, currentTimestamp));
        if (position == tagListAdapter.getItemCount()) {
            tagListAdapter.addBluetoothTag(data, 0, currentTimestamp);
        } else {
            long timeSince = 0;
            if (tagListAdapter.getTag(position).lastTimestamp != 0)
                timeSince = (currentTimestamp - tagListAdapter.getTag(position).lastTimestamp) / 1000; // Calculate difference and convert to seconds

            tagListAdapter.setBluetoothTag(position, data, timeSince, currentTimestamp);
        }
        TagDetail currentTag = tagListAdapter.getTag(position);
        if (currentTag.code.equals(tagListAdapter.getAudioIsolateTag()) || tagListAdapter.getAudioIsolateTag().isEmpty()) {
            AudioService.emitAudioPulse(currentTag.frequencyTone, Integer.parseInt(currentTag.rssi));
            tagListAdapter.setBeepTag(currentTag.code);
        }
        tagListAdapter.notifyDataSetChanged();
    }

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
                    showPrintDialog("Google Drive", "Do you want to send the file to the cloud?", 2);
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