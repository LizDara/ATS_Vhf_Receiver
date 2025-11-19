package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Models.ScanDetail;

import java.util.ArrayList;

public class ScanDetailListAdapter extends RecyclerView.Adapter<ScanDetailListAdapter.MyViewHolder> {
    private final LayoutInflater inflater;
    private ArrayList<ScanDetail> details;
    private final boolean isCoded;

    public ScanDetailListAdapter(Context context, boolean isCoded) {
        this.isCoded = isCoded;
        inflater = LayoutInflater.from(context);
        details = new ArrayList<>();
    }

    public void addDetail(ScanDetail scanDetail) {
        details.add(scanDetail);
    }

    public ScanDetail getDetail(int position) {
        return details.get(position);
    }

    public void setDetail(int position, ScanDetail detail) {
        details.set(position, detail);
    }

    public void addDetailInPosition(int position, ScanDetail detail) {
        details.add(position, detail);
    }

    public void removeAll() {
        details = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.scan_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.first_detail_textView.setText(String.valueOf(isCoded ? details.get(position).code : details.get(position).period));
        holder.second_detail_textView.setText(String.valueOf(details.get(position).detection));
        if (isCoded)
            holder.third_detail_textView.setText(details.get(position).mortality ? "M" : "-");
        else
            holder.third_detail_textView.setText(String.valueOf(details.get(position).pulseRate));
        holder.forth_detail_textView.setText(String.valueOf(details.get(position).signalStrength));
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView first_detail_textView;
        TextView second_detail_textView;
        TextView third_detail_textView;
        TextView forth_detail_textView;

        @SuppressLint("MissingPermission")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            first_detail_textView = itemView.findViewById(R.id.first_detail_textView);
            second_detail_textView = itemView.findViewById(R.id.second_detail_textView);
            third_detail_textView = itemView.findViewById(R.id.third_detail_textView);
            forth_detail_textView = itemView.findViewById(R.id.forth_detail_textView);
        }
    }
}
