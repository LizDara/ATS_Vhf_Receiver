package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

import java.util.ArrayList;

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.MyViewHolder> {

    private final LayoutInflater inflater;
    private ArrayList<byte[]> tags;

    public TagListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        tags = new ArrayList<>();
    }

    public void addTag(byte[] data) {
        tags.add(data);
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
        holder.detections_tag_textView.setText(Converters.getHexValue(tags.get(position)));
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

            tags_linearLayout.setOnClickListener(view -> {
                tag_footer_linearLayout.setVisibility(tag_footer_linearLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }
    }
}
