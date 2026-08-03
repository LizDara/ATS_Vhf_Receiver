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

import com.atstrack.ats.ats_vhf_receiver.Models.DeviceData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public final ArrayList<DeviceData> devicesData;
    public final ArrayList<DeviceData> selectedDevice;
    private final Context context;
    private final LayoutInflater inflater;
    public String deviceType;
    private final Button connect_button;
    private final TextView subtitle;
    private final TextView message;
    private int selectedPosition;

    public DeviceAdapter(Context context, String type, Button connect_button, TextView subtitle, TextView message) {
        devicesData = new ArrayList<>();
        selectedDevice = new ArrayList<>();
        this.context = context;
        this.connect_button = connect_button;
        this.subtitle = subtitle;
        this.message = message;
        deviceType = type;
        inflater = LayoutInflater.from(context);
    }

    /**
     * Adds only ATS Receiver devices to the list.
     * @param device Identifies the remote device.
     * @param scanRecord The content of the advertisement record offered by the remote device.
     */
    @SuppressLint("MissingPermission")
    public void addDevice(BluetoothDevice device, byte[] scanRecord) {
        if(!containsDeviceData(device)) {
            if (device.getName().contains(deviceType)) { // filter only ATS device
                Log.i("LeDeviceListAdapter", "Device: " + device.getName());
                devicesData.add(new DeviceData(device, scanRecord));
                notifyDataSetChanged();
            }
        }/* else {
            Calendar currentDate = Calendar.getInstance();
            currentDate.add(Calendar.SECOND, -2);
            if (currentDate.after(startDate)) {
                int index = mLeDevices.indexOf(device);
                mLeDevices.set(index, device);
                startDate = Calendar.getInstance();
            }
        }*/
    }

    /*public void setDeviceType(String type) {
        deviceType = type;
        startDate = Calendar.getInstance();
    }*/

    private boolean containsDeviceData(BluetoothDevice device) {
        for (DeviceData deviceData : devicesData) {
            if (Objects.equals(deviceData.bluetoothDevice.getAddress(), device.getAddress()))
                return true;
        }
        return false;
    }

    public void setSelectedDevice() {
        selectedDevice.add(devicesData.get(selectedPosition));
        notifyDataSetChanged();
    }

    private void setUnknownDevice(DeviceViewHolder holder) {
        holder.tv_device_number.setText(R.string.lbl_receiver_connection_unknown_device);
        holder.tv_device_status.setText(R.string.lbl_vhf_manual_option_none);
        holder.tv_percent_battery.setText("0%");
        holder.img_battery.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_light_battery));
    }

    /**
     * Remove all remote devices from the list.
     */
    public void clear() {
        devicesData.clear();
        selectedDevice.clear();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_information, parent, false);
        view.setElevation(4);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, @SuppressLint("RecyclerView") int position) { // Set all remote device information
        DeviceData deviceData = selectedDevice.size() == 1 ? selectedDevice.get(position) : devicesData.get(position);
        if (!deviceData.serialNumber.equals("0000000")) {
            holder.tv_device_number.setText(deviceData.serialNumber + " Receiver");
            holder.layout_device_type.setBackground(ContextCompat.getDrawable(context, Converters.getDeviceType(deviceType, false)));
            holder.img_device_type.setBackgroundResource(Converters.getDeviceType(deviceType, true));
            if (deviceType.contains(ValueCodes.VHF)) {
                holder.tv_device_status.setText(deviceData.detectionFilter + deviceData.status);
                holder.tv_frequency_range.setText(deviceData.range);
                holder.tv_percent_battery.setText(deviceData.batteryPercent + "%");
                holder.img_battery.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_light_battery));
                if (deviceData.selected)
                    holder.layout_status_footer.setVisibility(View.VISIBLE);
                else
                    holder.layout_status_footer.setVisibility(View.GONE);
            } else if (deviceType.contains(ValueCodes.ACOUSTIC)) {
                holder.tv_device_status.setText("Extra Details"); // Mas adelante se agregara mas info sobre acoustic receivers
                holder.tv_percent_battery.setText("0%");
                holder.img_battery.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_light_battery));
            } else {
                holder.tv_device_status.setText("Extra Details");
                holder.tv_percent_battery.setText("0%");
                holder.img_battery.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_light_battery));
            }
            if (deviceData.selected) {
                holder.layout_receiver_status.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_device));
                holder.img_selected.setVisibility(View.VISIBLE);
            } else {
                holder.layout_receiver_status.setBackgroundColor(ContextCompat.getColor(context, R.color.catskill_white));
                holder.img_selected.setVisibility(View.GONE);
            }
        } else {
            setUnknownDevice(holder);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.layout_receiver_status.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return selectedDevice.size() == 1 ? 1 : devicesData.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_receiver_status;
        LinearLayout layout_device_type;
        ImageView img_device_type;
        TextView tv_device_number;
        TextView tv_device_status;
        ImageView img_selected;
        LinearLayout layout_status_footer;
        TextView tv_frequency_range;
        ImageView img_battery;
        TextView tv_percent_battery;

        @SuppressLint("MissingPermission")
        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_receiver_status = itemView.findViewById(R.id.layout_receiver_status);
            layout_device_type = itemView.findViewById(R.id.layout_device_type);
            img_device_type = itemView.findViewById(R.id.img_device_type);
            tv_device_number = itemView.findViewById(R.id.tv_device_number);
            tv_device_status = itemView.findViewById(R.id.tv_device_status);
            img_selected = itemView.findViewById(R.id.img_selected);
            layout_status_footer = itemView.findViewById(R.id.layout_status_footer);
            tv_frequency_range = itemView.findViewById(R.id.tv_frequency_range);
            img_battery = itemView.findViewById(R.id.img_battery);
            tv_percent_battery = itemView.findViewById(R.id.tv_percent_battery);

            layout_receiver_status.setOnClickListener(view -> {
                if (devicesData.get(getLayoutPosition()) == null) return;
                if (devicesData.get(getLayoutPosition()).serialNumber.contains("0000000")) { // Error, factory setup required
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error");
                    builder.setMessage("Factory Setup Required.");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                } else { // Device selected
                    devicesData.get(getLayoutPosition()).selected = !devicesData.get(getLayoutPosition()).selected;
                    if (devicesData.get(getLayoutPosition()).selected) {
                        connect_button.setEnabled(true);
                        connect_button.setAlpha(1);
                        subtitle.setText(R.string.lbl_device_selection_selected);
                        message.setText(R.string.lbl_device_selection_click_instruction);
                        selectedPosition = getLayoutPosition();

                        //Clear the other selection
                        for (int i = 0; i < devicesData.size(); i++) {
                            if (i != getLayoutPosition())
                                devicesData.get(i).selected = false;
                        }
                    } else {
                        connect_button.setEnabled(false);
                        connect_button.setAlpha((float) 0.6);
                        subtitle.setText("Found " + devicesData.size() + " Devices");
                        message.setText(R.string.lbl_device_selection_guide);
                        selectedPosition = -1;
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }
}