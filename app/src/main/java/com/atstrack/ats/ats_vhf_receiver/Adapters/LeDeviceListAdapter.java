package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.MyViewHolder> {

    private final ArrayList<BluetoothDevice> mLeDevices;
    private final ArrayList<byte[]> mScanRecords;
    private final Context context;
    private final LayoutInflater inflater;
    private String deviceType;
    private final Button connect_button;
    private int selectedPosition;
    private View selectedView;
    private Calendar startDate;

    public LeDeviceListAdapter(Context context, Button connect_button) {
        mLeDevices = new ArrayList<>();
        mScanRecords = new ArrayList<>();
        this.context = context;
        this.connect_button = connect_button;
        deviceType = "";
        inflater = LayoutInflater.from(context);
    }

    /**
     * Adds only ATS Vhf Receiver devices to the list.
     * @param device Identifies the remote device.
     * @param scanRecord The content of the advertisement record offered by the remote device.
     */
    @SuppressLint("MissingPermission")
    public void addDevice(BluetoothDevice device, byte[] scanRecord) {
        if(device.getName() != null) {
            if(!mLeDevices.contains(device)) {
                if (device.getName().contains(deviceType)) { // filter only ATS device
                    mLeDevices.add(device);
                    mScanRecords.add(scanRecord);
                }
            } else {
                Calendar currentDate = Calendar.getInstance();
                currentDate.add(Calendar.SECOND, -2);
                if (currentDate.after(startDate)) {
                    int index = mLeDevices.indexOf(device);
                    mLeDevices.set(index, device);
                    startDate = Calendar.getInstance();
                }
            }
        }
    }

    public void setDeviceType(String type) {
        deviceType = type;
        startDate = Calendar.getInstance();
    }

    public BluetoothDevice getSelectedDevice() {
        return mLeDevices.get(selectedPosition);
    }

    public byte[] getScanRecord() {
        return mScanRecords.get(selectedPosition);
    }

    public View getSelectedView() {
        return selectedView;
    }

    private String getType(String name) {
        if (name.contains("vr"))
            return "VHF Receiver";
        else if (name.contains("ar"))
            return "Acoustic Receiver";
        else if (name.contains("wl"))
            return "Wildlink Receiver";
        else if (name.contains("bt"))
            return "Bluetooth Tag";
        return "Unknown";
    }

    private void setUnknownDevice(MyViewHolder holder) {
        holder.device_number_textView.setText(R.string.unknown_device);
        holder.device_status_textView.setText(R.string.lb_none);
        holder.percent_battery_textView.setText("0%");
        holder.battery_imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_light_battery));
    }

    /**
     * Remove all remote devices from the list.
     */
    public void clear() {
        mLeDevices.clear();
        mScanRecords.clear();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.device_information, parent, false);
        view.setElevation(4);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) { // Set all remote device information
        @SuppressLint("MissingPermission") String device = mLeDevices.get(position).getName();
        final String serialNumber = device.substring(0, 7);
        if (!serialNumber.equals("0000000")) {
            holder.device_number_textView.setText(serialNumber + " " + getType(device));
            holder.device_type_linearLayout.setBackground(ContextCompat.getDrawable(context, Converters.getDeviceType(device, false)));
            holder.device_type_imageView.setBackgroundResource(Converters.getDeviceType(device, true));
            if (device.contains("vr")) {
                String detectionFilter = Converters.getDetectionFilter(device.substring(15, 16));
                String status = Converters.getStatusVhfReceiver(mScanRecords.get(position));
                int percent = Converters.getPercentBatteryVhfReceiver(mScanRecords.get(position));
                int baseFrequency = Integer.parseInt(device.substring(12, 15)) * 1000;
                int frequencyRange = ((Integer.parseInt(device.substring(17)) + (baseFrequency / 1000)) * 1000) - 1;
                String range = Converters.getFrequency(baseFrequency) + "-" + Converters.getFrequency(frequencyRange);

                holder.device_status_textView.setText(detectionFilter + status);
                holder.frequency_range_textView.setText(range);
                holder.percent_battery_textView.setText(percent + "%");
                holder.battery_imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_light_battery));
            } else if (device.contains("ar")) {
                holder.device_status_textView.setText("Extra Details"); // Mas adelante se agregara mas info sobre acoustic receivers
                holder.percent_battery_textView.setText("0%");
                holder.battery_imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_light_battery));
            } else {
                holder.device_status_textView.setText("Extra Details");
                holder.percent_battery_textView.setText("0%");
                holder.battery_imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_light_battery));
            }
        } else {
            setUnknownDevice(holder);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.receiver_status_linearLayout.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout receiver_status_linearLayout;
        LinearLayout device_type_linearLayout;
        ImageView device_type_imageView;
        TextView device_number_textView;
        TextView device_status_textView;
        ImageView selected_imageView;
        LinearLayout status_footer_linearLayout;
        TextView frequency_range_textView;
        ImageView battery_imageView;
        TextView percent_battery_textView;

        @SuppressLint("MissingPermission")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            receiver_status_linearLayout = itemView.findViewById(R.id.receiver_status_linearLayout);
            device_type_linearLayout = itemView.findViewById(R.id.device_type_linearLayout);
            device_type_imageView = itemView.findViewById(R.id.device_type_imageView);
            device_number_textView = itemView.findViewById(R.id.device_number_textView);
            device_status_textView = itemView.findViewById(R.id.device_status_textView);
            selected_imageView = itemView.findViewById(R.id.selected_imageView);
            status_footer_linearLayout = itemView.findViewById(R.id.status_footer_linearLayout);
            frequency_range_textView = itemView.findViewById(R.id.frequency_range_textView);
            battery_imageView = itemView.findViewById(R.id.battery_imageView);
            percent_battery_textView = itemView.findViewById(R.id.percent_battery_textView);

            receiver_status_linearLayout.setOnClickListener(view -> {
                if (mLeDevices.get(getLayoutPosition()) == null) return;
                if (mLeDevices.get(getLayoutPosition()).getName().contains("0000000ATS")) { // Error, factory setup required
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error");
                    builder.setMessage("Factory Setup Required.");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                } else { // Device selected
                    receiver_status_linearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_device));
                    selected_imageView.setVisibility(View.VISIBLE);
                    connect_button.setEnabled(true);
                    connect_button.setAlpha(1);
                    selectedPosition = getLayoutPosition();
                    selectedView = itemView;
                    if (mLeDevices.get(getLayoutPosition()).getName().contains("vr")) {
                        status_footer_linearLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }
}