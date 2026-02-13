package com.atstrack.ats.ats_vhf_receiver.BluetoothReceiver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TagListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetail;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationServices;
import android.location.Location;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;

public class TagDetectionActivity extends BaseActivity {

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
    private ToneGenerator toneGen;

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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String text = "Tag Type, Tag ID, Date/Time, RSSI, Latitude, Longitude, Tag Version, Vcc (mv), Temp (C)" + ValueCodes.CR + ValueCodes.LF;
        for (TagDetail tag : tags)
            text += "Bluetooth, " + tag.code + ", " + simpleDateFormat.format(Calendar.getInstance().getTime()) + ", " + tag.rssi + ", " + tag.latitude + ", " + tag.longitude + ", 1.0, " + tag.voltage + ", " + tag.temperature + ValueCodes.CR + ValueCodes.LF;
        byte[] data = Converters.convertToUTF8(text);
        Data processedData = new Data(ValueCodes.BLUETOOTH_FILE);
        processedData.packets.add(data);
        ArrayList<Data> dataList = new ArrayList<>();
        dataList.add(processedData);
        String fileName = processedData.fileName;
        File root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack"); //set the directory path
        boolean result = Converters.printDataFiles(root, dataList);
        if (result) {
            String message = "File saved as " + fileName;
            Message.showMessage(this, "Finished", message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_bluetooth_tag_detection;
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
        toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Disconnect
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
                Priority.PRIORITY_HIGH_ACCURACY, 3000) // Intervalo de 5 segundos PRIORITY_BALANCED_POWER_ACCURACY
                .setMinUpdateIntervalMillis(1500) // Mínimo cada 2 segundos
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void setDetectionTagsData(byte[] data) {
        tags.add(new TagDetail(data, latitude, longitude));
        int position = -1;
        for (int i = 0; i < tagListAdapter.getItemCount(); i++) {
            if (tagListAdapter.getTag(i).code.equals(Converters.getAsciiValue(6, 14, data)))
                position = i;
        }
        if (position == -1)
            tagListAdapter.addTag(data);
        else
            tagListAdapter.setTag(position, data);
        tagListAdapter.notifyDataSetChanged();
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150); // 150ms duration
    }
}