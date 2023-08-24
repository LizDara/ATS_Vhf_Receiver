package com.atstrack.ats.ats_vhf_receiver.BluetoothATS;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.MainMenuActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.MyViewHolder> {

    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<byte[]> mScanRecords;
    private Context context;

    public LeDeviceListAdapter(Context context) {
        mLeDevices = new ArrayList<>();
        mScanRecords = new ArrayList<>();
        this.context = context;
    }

    /**
     * Adds only ATS Vhf Receiver devices to the list.
     *
     * @param device Identifies the remote device.
     * @param scanRecord The content of the advertisement record offered by the remote device.
     */
    public void addDevice(BluetoothDevice device, byte[] scanRecord) {
        if(!mLeDevices.contains(device)) {
            final String deviceName = device.getName();
            if(deviceName != null) {
                if (deviceName.contains("ATSvr")) { // add new for filter only ATS Vhf Rec device
                    mLeDevices.add(device);
                    mScanRecords.add(scanRecord);
                }
                Log.i("Devices", deviceName);
            }
        }
    }

    /**
     * Gets the percentage of the device's battery.
     *
     * @param scanRecord The content of the advertisement record offered by the remote device.
     *
     * @return Return the battery percentage.
     */
    private String getPercentBattery(byte[] scanRecord) {
        int firstElement = Integer.parseInt(Converters.getDecimalValue(scanRecord[0]));
        return Converters.getDecimalValue(scanRecord[firstElement + 5]);
    }

    /**
     * Gets the status of the devices found.
     *
     * @param scanRecord The content of the advertisement record offered by the remote device.
     *
     * @return Return the device status.
     */
    private String getStatus(byte[] scanRecord) {
        int firstElement = Integer.parseInt(Converters.getDecimalValue(scanRecord[0]));
        String status = Converters.getHexValue(scanRecord[firstElement + 6]);
        switch (status) {
            case "00":
                status = "Not scanning";
                break;
            case "82":
                status = "Scanning, mobile";
                break;
            case "83":
                status = "Scanning, stationary";
                break;
            case "86":
                status = "Scanning, manual";
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
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.receiver_status, parent, false);
        view.setElevation(4);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Set all remote device information
        BluetoothDevice device = mLeDevices.get(position);
        String percent = getPercentBattery(mScanRecords.get(position));

        final String deviceName = device.getName().substring(0, 7);
        final int baseFrequency = Integer.parseInt(device.getName().substring(12, 15)) * 1000;
        final int frequencyRange = ((Integer.parseInt(device.getName().substring(17)) + (baseFrequency / 1000)) * 1000) - 1;
        final String range = String.valueOf(baseFrequency).substring(0, 3) + "." + String.valueOf(baseFrequency).substring(3) + "-" +
                String.valueOf(frequencyRange).substring(0, 3) + "." + String.valueOf(frequencyRange).substring(3);

        final String type = device.getName().substring(15, 16);
        final String status = getStatus(mScanRecords.get(position));

        holder.deviceName.setText(deviceName + ((type.equals("C")) ? " Coded, " : " Non coded, ") + status);

        holder.deviceStatus.setText(range + " MHz");

        holder.percentBattery.setText(percent + "%");
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
        LinearLayout device_linearLayout;
        TextView deviceName;
        TextView deviceStatus;
        TextView percentBattery;
        BluetoothDevice device;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            receiver_status = itemView.findViewById(R.id.receiver_status);
            device_linearLayout = itemView.findViewById(R.id.device_linearLayout);
            deviceStatus = itemView.findViewById(R.id.device_status_textView);
            deviceName = itemView.findViewById(R.id.device_name_textView);
            percentBattery = itemView.findViewById(R.id.percent_battery_textView);

            device_linearLayout.setOnClickListener(view -> {
                if (device == null) return;
                if (device.getName().contains("#000000")) { // Error, factory setup required
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error");
                    builder.setMessage("Factory Setup Required.");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                } else { // Connects to device
                    Intent intent = new Intent(context, MainMenuActivity.class);
                    intent.putExtra(MainMenuActivity.EXTRAS_DEVICE_NAME, deviceName.getText().toString());
                    intent.putExtra(MainMenuActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    intent.putExtra(MainMenuActivity.EXTRAS_DEVICE_STATUS, deviceStatus.getText().toString());
                    intent.putExtra(MainMenuActivity.EXTRAS_BATTERY, percentBattery.getText().toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("menu", false);
                    context.startActivity(intent);
                }
            });
        }
    }
}
