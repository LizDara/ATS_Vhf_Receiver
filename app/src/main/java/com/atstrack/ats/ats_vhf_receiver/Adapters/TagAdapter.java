package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Models.Detection;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetections;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private final LayoutInflater inflater;
    public final ArrayList<TagDetections> tags;
    public String audioIsolateTag = "";
    public String beepTag = "";
    private final OnAdapterClickListener adapterClickListener;

    public TagAdapter(Context context, OnAdapterClickListener listener) {
        inflater = LayoutInflater.from(context);
        tags = new ArrayList<>();
        adapterClickListener = listener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_tag, parent, false);
        view.setElevation(4);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Detection currentDetection = tags.get(position).getLastDetection();
        holder.tv_tag.setText(tags.get(position).code);
        holder.tv_detections_tag.setText("Detections: " + tags.get(position).detections.size());
        holder.tv_rssi.setText("RSSI (dBm): " + currentDetection.rssi);
        holder.tv_time_since.setText("Time Since (secs): " + tags.get(position).timeSince);
        holder.tv_temperature_c.setText("Temperature (C): " + currentDetection.temperature);
        holder.tv_voltage.setText("Voltage (mV): " + currentDetection.voltage);
        holder.cb_isolate_audio.setChecked(audioIsolateTag.equals(tags.get(position).code));
        if (audioIsolateTag.equals(tags.get(position).code) || audioIsolateTag.isEmpty()) {
            holder.tv_detections_tag.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_audio_on, 0);
            if (beepTag.equals(tags.get(position).code) && (audioIsolateTag.equals(tags.get(position).code) || audioIsolateTag.isEmpty())) {
                holder.layout_beep.setBackgroundColor(ContextCompat.getColor(inflater.getContext(), R.color.keppel));
                new Handler().postDelayed(() -> {
                    beepTag = "";
                    holder.layout_beep.setBackgroundColor(ContextCompat.getColor(inflater.getContext(), R.color.catskill_white));
                }, ValueCodes.WAITING_PERIOD);
            }
        } else {
            holder.tv_detections_tag.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_audio_off, 0);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.layout_tags.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public class TagViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout layout_tags;
        TextView tv_tag;
        TextView tv_detections_tag;
        LinearLayout layout_tag_footer;
        TextView tv_rssi;
        TextView tv_time_since;
        TextView tv_temperature_c;
        TextView tv_voltage;
        LinearLayout layout_beep;
        CheckBox cb_isolate_audio;
        Button btn_view_tag;

        @SuppressLint("MissingPermission")
        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_tags = itemView.findViewById(R.id.layout_tags);
            tv_tag = itemView.findViewById(R.id.tv_tag);
            tv_detections_tag = itemView.findViewById(R.id.tv_detections_tag);
            layout_tag_footer = itemView.findViewById(R.id.layout_tag_footer);
            tv_rssi = itemView.findViewById(R.id.tv_rssi);
            tv_time_since = itemView.findViewById(R.id.tv_time_since);
            tv_temperature_c = itemView.findViewById(R.id.tv_temperature_c);
            tv_voltage = itemView.findViewById(R.id.tv_voltage);
            layout_beep = itemView.findViewById(R.id.layout_beep);
            cb_isolate_audio = itemView.findViewById(R.id.cb_isolate_audio);
            btn_view_tag = itemView.findViewById(R.id.btn_view_tag);

            layout_tags.setOnClickListener(view -> {
                layout_tag_footer.setVisibility(layout_tag_footer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                tags.get(getLayoutPosition()).time_since_textView = tv_time_since;
            });

            cb_isolate_audio.setOnClickListener(v -> {
                boolean isChecked = cb_isolate_audio.isChecked();
                if (isChecked)
                    audioIsolateTag = tags.get(getLayoutPosition()).code;
                else
                    audioIsolateTag = "";
                notifyDataSetChanged();
            });
            btn_view_tag.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            adapterClickListener.onAdapterItemClickListener(getLayoutPosition());
        }
    }
}
