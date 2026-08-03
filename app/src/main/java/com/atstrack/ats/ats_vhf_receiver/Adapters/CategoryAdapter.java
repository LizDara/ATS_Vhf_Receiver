package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnAdapterClickListener;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;
    public String[] types;
    private final OnAdapterClickListener adapterClickListener;

    public CategoryAdapter(Context context, OnAdapterClickListener listener) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        adapterClickListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_information, parent, false);
        view.setElevation(4);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.tv_device_number.setText(types[position]);
        holder.layout_device_type.setBackground(ContextCompat.getDrawable(context, Converters.getDeviceType(types[position], false)));
        holder.img_device_type.setBackgroundResource(Converters.getDeviceType(types[position], true));
        holder.tv_device_status.setText(types.length == 2 ? R.string.lbl_bluetooth_tags_brief_description : R.string.lbl_bridge_app_supported_models);
        holder.img_selected.setVisibility(View.VISIBLE);
        holder.img_selected.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_next));
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.layout_receiver_status.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return types.length;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout layout_receiver_status;
        LinearLayout layout_device_type;
        ImageView img_device_type;
        TextView tv_device_number;
        TextView tv_device_status;
        ImageView img_selected;

        @SuppressLint("MissingPermission")
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_receiver_status = itemView.findViewById(R.id.layout_receiver_status);
            layout_device_type = itemView.findViewById(R.id.layout_device_type);
            img_device_type = itemView.findViewById(R.id.img_device_type);
            tv_device_number = itemView.findViewById(R.id.tv_device_number);
            tv_device_status = itemView.findViewById(R.id.tv_device_status);
            img_selected = itemView.findViewById(R.id.img_selected);
            layout_receiver_status.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            adapterClickListener.onAdapterItemClickListener(getLayoutPosition());
        }
    }
}
