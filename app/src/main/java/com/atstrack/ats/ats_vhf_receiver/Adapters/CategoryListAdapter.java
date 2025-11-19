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
import com.atstrack.ats.ats_vhf_receiver.Utils.OnAdapterClickListener;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.MyViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;
    private String[] types;
    private final OnAdapterClickListener adapterClickListener;

    public CategoryListAdapter(Context context, OnAdapterClickListener listener) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        adapterClickListener = listener;
        types = context.getResources().getStringArray(R.array.categories);
    }

    public void setBluetoothTags() {
        types = context.getResources().getStringArray(R.array.connection_modes);
    }

    public String[] getTypes() {
        return types;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.information_item, parent, false);
        view.setElevation(4);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.device_number_textView.setText(types[position]);
        holder.device_type_linearLayout.setBackground(ContextCompat.getDrawable(context, Converters.getDeviceType(types[position], false)));
        holder.device_type_imageView.setBackgroundResource(Converters.getDeviceType(types[position], true));
        holder.device_status_textView.setText(types.length == 2 ? R.string.lb_brief_description : R.string.lb_supported_models);
        holder.selected_imageView.setVisibility(View.VISIBLE);
        holder.selected_imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_next));
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.receiver_status_linearLayout.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return types.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout receiver_status_linearLayout;
        LinearLayout device_type_linearLayout;
        ImageView device_type_imageView;
        TextView device_number_textView;
        TextView device_status_textView;
        ImageView selected_imageView;

        @SuppressLint("MissingPermission")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            receiver_status_linearLayout = itemView.findViewById(R.id.receiver_status_linearLayout);
            device_type_linearLayout = itemView.findViewById(R.id.device_type_linearLayout);
            device_type_imageView = itemView.findViewById(R.id.device_type_imageView);
            device_number_textView = itemView.findViewById(R.id.device_number_textView);
            device_status_textView = itemView.findViewById(R.id.device_status_textView);
            selected_imageView = itemView.findViewById(R.id.selected_imageView);
            receiver_status_linearLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            adapterClickListener.onAdapterItemClickListener(getLayoutPosition());
        }
    }
}
