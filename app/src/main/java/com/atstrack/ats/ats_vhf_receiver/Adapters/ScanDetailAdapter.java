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

public class ScanDetailAdapter extends RecyclerView.Adapter<ScanDetailAdapter.ScanDetailViewHolder> {
    private final LayoutInflater inflater;
    private ArrayList<ScanDetail> details;
    private final boolean isCoded;

    public ScanDetailAdapter(Context context, boolean isCoded) {
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

    public void removeInPosition(int position) {
        details.remove(position);
    }

    public void removeAll() {
        details = new ArrayList<>();
    }

    @NonNull
    @Override
    public ScanDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_scan, parent, false);
        return new ScanDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanDetailViewHolder holder, int position) {
        holder.tv_first_detail.setText(String.valueOf(isCoded ? details.get(position).code : details.get(position).period));
        holder.tv_second_detail.setText(String.valueOf(details.get(position).detection));
        if (isCoded)
            holder.tv_third_detail.setText(details.get(position).mortality ? "M" : "-");
        else
            holder.tv_third_detail.setText(String.valueOf(details.get(position).pulseRate));
        holder.tv_forth_detail.setText(String.valueOf(details.get(position).signalStrength));
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public class ScanDetailViewHolder extends RecyclerView.ViewHolder {
        TextView tv_first_detail;
        TextView tv_second_detail;
        TextView tv_third_detail;
        TextView tv_forth_detail;

        @SuppressLint("MissingPermission")
        public ScanDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_first_detail = itemView.findViewById(R.id.tv_first_detail);
            tv_second_detail = itemView.findViewById(R.id.tv_second_detail);
            tv_third_detail = itemView.findViewById(R.id.tv_third_detail);
            tv_forth_detail = itemView.findViewById(R.id.tv_forth_detail);
        }
    }
}
