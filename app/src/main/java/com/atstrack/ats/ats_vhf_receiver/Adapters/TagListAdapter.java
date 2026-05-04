package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Models.TagDetail;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Services.AudioService;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.MyViewHolder> {

    private final LayoutInflater inflater;
    private final ArrayList<TagDetail> tags;
    private String audioIsolateTag = "";
    private String beepTag = "";

    public TagListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        tags = new ArrayList<>();
    }

    public void addBluetoothTag(byte[] data, long timeSince, long lastTimestamp) {
        int frequency = AudioService.frequencies[tags.size()];
        tags.add(new TagDetail(data, frequency, timeSince, lastTimestamp));
    }

    public void addTag(byte[] data, long timeSince, long lastTimestamp, int rssi) {
        int frequency = AudioService.frequencies[tags.size()];
        tags.add(new TagDetail(data, String.valueOf(rssi), frequency, timeSince, lastTimestamp));
    }

    public void setBluetoothTag(int position, byte[] data, long timeSince, long lastTimestamp) {
        tags.get(position).updateData(data, timeSince, lastTimestamp);
    }

    public void setTag(int position, byte[] data, long timeSince, long lastTimestamp, int rssi) {
        tags.get(position).updateData(data, String.valueOf(rssi), timeSince, lastTimestamp);
    }

    public TagDetail getTag(int position) {
        return tags.get(position);
    }

    public String getAudioIsolateTag() {
        return audioIsolateTag;
    }

    public void setBeepTag(String beepTag) {
        this.beepTag = beepTag;
    }

    @NonNull
    @Override
    public TagListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.tag_item, parent, false);
        view.setElevation(4);
        return new TagListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagListAdapter.MyViewHolder holder, int position) {
        holder.tag_textView.setText(tags.get(position).code);
        holder.detections_tag_textView.setText("Detections: " + tags.get(position).detections);
        holder.rssi_textView.setText(tags.get(position).rssi);
        holder.temperature_c_textView.setText(tags.get(position).temperature);
        holder.voltage_textView.setText(tags.get(position).voltage);
        holder.time_since_textView.setText(String.valueOf(tags.get(position).timeSince));
        holder.isolate_audio_checkBox.setChecked(audioIsolateTag.equals(tags.get(position).code));
        if (audioIsolateTag.equals(tags.get(position).code) || audioIsolateTag.isEmpty()) {
            holder.detections_tag_textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_audio_on, 0);
            if (beepTag.equals(tags.get(position).code) && (audioIsolateTag.equals(tags.get(position).code) || audioIsolateTag.isEmpty())) {
                holder.beep_linearLayout.setBackgroundColor(ContextCompat.getColor(inflater.getContext(), R.color.keppel));
                new Handler().postDelayed(() -> {
                    beepTag = "";
                    holder.beep_linearLayout.setBackgroundColor(ContextCompat.getColor(inflater.getContext(), R.color.catskill_white));
                }, ValueCodes.WAITING_PERIOD);
            }
        } else {
            holder.detections_tag_textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_audio_off, 0);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.tags_linearLayout.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout tags_linearLayout;
        TextView tag_textView;
        TextView detections_tag_textView;
        LinearLayout tag_footer_linearLayout;
        TextView rssi_textView;
        TextView time_since_textView;
        TextView temperature_c_textView;
        TextView voltage_textView;
        LinearLayout beep_linearLayout;
        CheckBox isolate_audio_checkBox;

        @SuppressLint("MissingPermission")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tags_linearLayout = itemView.findViewById(R.id.tags_linearLayout);
            tag_textView = itemView.findViewById(R.id.tag_textView);
            detections_tag_textView = itemView.findViewById(R.id.detections_tag_textView);
            tag_footer_linearLayout = itemView.findViewById(R.id.tag_footer_linearLayout);
            rssi_textView = itemView.findViewById(R.id.rssi_textView);
            time_since_textView = itemView.findViewById(R.id.time_since_textView);
            temperature_c_textView = itemView.findViewById(R.id.temperature_c_textView);
            voltage_textView = itemView.findViewById(R.id.voltage_textView);
            beep_linearLayout = itemView.findViewById(R.id.beep_linearLayout);
            isolate_audio_checkBox = itemView.findViewById(R.id.isolate_audio_checkBox);

            tags_linearLayout.setOnClickListener(view -> {
                tag_footer_linearLayout.setVisibility(tag_footer_linearLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });

            isolate_audio_checkBox.setOnClickListener(v -> {
                boolean isChecked = isolate_audio_checkBox.isChecked();
                if (isChecked)
                    audioIsolateTag = tags.get(getLayoutPosition()).code;
                else
                    audioIsolateTag = "";
                notifyDataSetChanged();
            });
        }
    }
}
