package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TagAdapter;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnTimeTickListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.Detection;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetail;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetections;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Services.AudioService;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentTagsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import android.location.Location;

import java.util.ArrayList;

public class TagsFragment extends Fragment implements ReceiverCallback, OnAdapterClickListener {
    private FragmentTagsBinding binding = null;
    private final String type;
    private ArrayList<TagDetail> tags;
    private TagAdapter tagAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Coordinates coordinates;
    private final OnTimeTickListener onTimeTickListener = new OnTimeTickListener() {
        @Override
        public void onTick(String tagCode, int currentTimeSince, boolean updateTimeSince) {
            if (isAdded() && getView() != null) {
                if (updateTimeSince) {
                    for (TagDetections tag : tagAdapter.tags) {
                        if (tag.code.equals(tagCode) && tag.time_since_textView != null) {
                            tag.time_since_textView.setText("Time Since (secs): " + currentTimeSince);
                            break;
                        }
                    }
                }
            }
        }
    };

    public TagsFragment(String type, ArrayList<TagDetail> tags) {
        this.type = type;
        this.tags = tags;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTagsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tagAdapter = new TagAdapter(requireContext(), this);
        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        binding.includeRecyclerView.rvItem.setLayoutManager(manager);
        binding.includeRecyclerView.rvItem.setHasFixedSize(true);
        binding.includeRecyclerView.rvItem.setAdapter(tagAdapter);
        coordinates = new Coordinates();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    coordinates.latitude = location.getLatitude();
                    coordinates.longitude = location.getLongitude();
                    binding.tvCoordinates.setText(coordinates.latitude + ", " + coordinates.longitude);
                }
            }
        };
        startGpsUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        stopTagsTimer();
        binding = null;
    }

    @Override
    public void onAdapterItemClickListener(int position) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new TagChartFragment(type, position, tagAdapter, tags, coordinates), String.valueOf(ValueCodes.SECOND_STEP))
                    .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                setDetectionTagsData(packet);
            }
        });
    }

    private void startGpsUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 3000) // Intervalo de 3 segundos PRIORITY_BALANCED_POWER_ACCURACY
                .setMinUpdateIntervalMillis(2000) // Mínimo cada 2 segundos
                .build();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void setDetectionTagsData(byte[] data) {
        int position = tagAdapter.getItemCount();
        long currentTimestamp = System.currentTimeMillis();
        String code;

        if (type.equals(ValueCodes.BLUETOOTH_RECEIVER)) {
            tags.add(new TagDetail(data, coordinates.latitude, coordinates.longitude, currentTimestamp));
            code = Converters.getAsciiValue(6, 14, data);
        } else {
            tags.add(new TagDetail(data, String.valueOf(data[data.length - 1]), coordinates.latitude, coordinates.longitude, currentTimestamp));
            code = Converters.getHexValue(data[4]) + Converters.getHexValue(data[5]) + Converters.getHexValue(data[6]) + Converters.getHexValue(data[7]);
        }
        for (int i = 0; i < tagAdapter.getItemCount(); i++) {
            if (tagAdapter.tags.get(i).code.equals(code))
                position = i;
        }
        if (position == tagAdapter.getItemCount()) {
            TagDetections newTag = new TagDetections(code, AudioService.frequencies[position]);
            tagAdapter.tags.add(newTag);
        }
        tagAdapter.tags.get(position).timeTickListener = onTimeTickListener;
        if (type.equals(ValueCodes.BLUETOOTH_RECEIVER))
            tagAdapter.tags.get(position).detections.add(new Detection(data, coordinates.latitude, coordinates.longitude, currentTimestamp));
        else
            tagAdapter.tags.get(position).detections.add(new Detection(data, coordinates.latitude, coordinates.longitude, data[data.length - 1], currentTimestamp));
        tagAdapter.tags.get(position).timeSince = 0;

        if (tagAdapter.tags.get(position).code.equals(tagAdapter.audioIsolateTag) || tagAdapter.audioIsolateTag.isEmpty()) {
            TagDetections currentTag = tagAdapter.tags.get(position);
            AudioService.emitAudioPulse(currentTag.frequencyTone, Integer.parseInt(currentTag.getLastDetection().rssi), true);
            tagAdapter.beepTag = currentTag.code;
        }
        tagAdapter.notifyDataSetChanged();
    }

    private void stopTagsTimer() {
        for (TagDetections tag: tagAdapter.tags)
            tag.stopTimer();
    }

    public static class Coordinates {
        public double latitude, longitude = 0;
    }
}
