package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.MainMenuActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.MyViewHolder> {

    private final ArrayList<BluetoothDevice> mLeDevices;
    private final ArrayList<byte[]> mScanRecords;
    private final Context context;
    private final LayoutInflater inflater;

    public LeDeviceListAdapter(Context context) {
        mLeDevices = new ArrayList<>();
        mScanRecords = new ArrayList<>();
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    /**
     * Adds only ATS Vhf Receiver devices to the list.
     * @param device Identifies the remote device.
     * @param scanRecord The content of the advertisement record offered by the remote device.
     */
    public void addDevice(BluetoothDevice device, byte[] scanRecord) {
        if(!mLeDevices.contains(device)) {
            @SuppressLint("MissingPermission") final String deviceName = device.getName();
            if(deviceName != null) {
                if (deviceName.contains("ATSvr")) { // filter only ATS Vhf Rec device
                    mLeDevices.add(device);
                    mScanRecords.add(scanRecord);
                }
                Log.i("Devices", deviceName + " : " + deviceName.length());
            }
        }
    }

    /**
     * Gets the percentage of the device's battery.
     * @param scanRecord The content of the advertisement record offered by the remote device.
     * @return Return the battery percentage.
     */
    private int getPercentBattery(byte[] scanRecord) {
        int firstElement = Integer.parseInt(Converters.getDecimalValue(scanRecord[0]));
        return Integer.parseInt(Converters.getDecimalValue(scanRecord[firstElement + 5]));
    }

    /**
     * Gets the status of the devices found.
     * @param scanRecord The content of the advertisement record offered by the remote device.
     * @return Return the device status.
     */
    private String getStatus(byte[] scanRecord) {
        int firstElement = Integer.parseInt(Converters.getDecimalValue(scanRecord[0]));
        String status = Converters.getHexValue(scanRecord[firstElement + 6]);
        switch (status) {
            case "00":
                status = " Not scanning";
                break;
            case "82":
            case "81":
            case "80":
                status = " Scanning, mobile";
                break;
            case "83":
                status = " Scanning, stationary";
                break;
            case "86":
                status = " Scanning, manual";
                break;
            default:
                status = " None";
                break;
        }
        return status;
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
        int percent = getPercentBattery(mScanRecords.get(position));
        final String deviceName = device.substring(0, 7);
        if (!deviceName.equals("#000000")) {
            final int baseFrequency = Integer.parseInt(device.substring(12, 15)) * 1000;
            final int frequencyRange = ((Integer.parseInt(device.substring(17)) + (baseFrequency / 1000)) * 1000) - 1;
            final String range = Converters.getFrequency(baseFrequency) + "-" + Converters.getFrequency(frequencyRange);

            String type = device.substring(15, 16);
            final String status = getStatus(mScanRecords.get(position));
            switch (type) {
                case "C":
                    type = " Coded,";
                    break;
                case "F":
                    type = " Fixed PR,";
                    break;
                case "V":
                    type = " Variable PR,";
                    break;
            }
            holder.deviceName.setText(deviceName + type + status);
            holder.deviceRange.setText(range + " MHz");
            holder.percentBattery.setText(percent + "%");
            holder.deviceBattery.setBackground(ContextCompat.getDrawable(context, percent > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));
        } else {
            holder.deviceName.setText(deviceName);
            holder.deviceRange.setText(R.string.lb_none);
            holder.percentBattery.setText("0%");
            holder.deviceBattery.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_low_battery));
        }
        holder.device = mLeDevices.get(position);
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.receiver_status.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout receiver_status;
        TextView deviceName;
        TextView deviceRange;
        TextView percentBattery;
        ImageView deviceBattery;
        BluetoothDevice device;

        @SuppressLint("MissingPermission")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            receiver_status = itemView.findViewById(R.id.receiver_status);
            deviceRange = itemView.findViewById(R.id.device_range_textView);
            deviceName = itemView.findViewById(R.id.device_status_textView);
            percentBattery = itemView.findViewById(R.id.percent_battery_device_textView);
            deviceBattery = itemView.findViewById(R.id.battery_device_textView);

            receiver_status.setOnClickListener(view -> {
                if (device == null) return;
                if (device.getName().contains("#000000")) { // Error, factory setup required
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error");
                    builder.setMessage("Factory Setup Required.");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                } else { // Connects to device
                    SharedPreferences sharedPreferences = context.getSharedPreferences("Defaults", 0);
                    SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences.edit();
                    sharedPreferencesEdit.putInt("BaseFrequency", Integer.parseInt(device.getName().substring(12, 15)));
                    sharedPreferencesEdit.putInt("Range", Integer.parseInt(device.getName().substring(17)));

                    Intent intent = new Intent(context, MainMenuActivity.class);
                    intent.putExtra(MainMenuActivity.EXTRAS_DEVICE_STATUS, deviceName.getText().toString());
                    intent.putExtra(MainMenuActivity.EXTRAS_DEVICE_NAME, device.getName().substring(0, 7));
                    intent.putExtra(MainMenuActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    intent.putExtra(MainMenuActivity.EXTRAS_DEVICE_RANGE, deviceRange.getText().toString());
                    intent.putExtra(MainMenuActivity.EXTRAS_BATTERY, percentBattery.getText().toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("menu", false);
                    context.startActivity(intent);
                }
            });
        }
    }
}