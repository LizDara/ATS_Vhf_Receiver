package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Models.LoadedTable;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.VHF.FrequenciesActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.LinkedList;
import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {
    private final Context context;
    private final LayoutInflater inflater;
    private byte[] tables;
    private int baseFrequency;
    private int range;
    public List<LoadedTable> loadedTables;
    public boolean isTemperature;
    public boolean isFile;
    private final OnAdapterClickListener adapterClickListener;

    public TableAdapter(Context context, OnAdapterClickListener listener) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        loadedTables = new LinkedList<>();
        isFile = false;
        adapterClickListener = listener;
    }

    public void setData(byte[] tables) {
        this.tables = tables;
        baseFrequency = Byte.toUnsignedInt(tables[13]);
        range = Byte.toUnsignedInt(tables[14]);
    }

    public int getBaseFrequency() {
        return baseFrequency;
    }

    public int getRange() {
        return range;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_table, parent, false);
        view.setElevation(4);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        if (isFile) {
            holder.tv_table_number.setText("Table " + loadedTables.get(position).tableNumber);
            holder.tv_table_frequency.setText(loadedTables.get(position).frequenciesLoaded.length + " frequencies");
        } else {
            holder.tv_table_number.setText("Table " + (position + 1));
            holder.tv_table_frequency.setText(Byte.toUnsignedInt(tables[position + 1]) + " frequencies");
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.layout_table.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return isFile ? loadedTables.size() : tables.length - 3;
    }

    public class TableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout layout_table;
        TextView tv_table_number;
        TextView tv_table_frequency;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_table = itemView.findViewById(R.id.layout_table);
            tv_table_number = itemView.findViewById(R.id.tv_table_number);
            tv_table_frequency = itemView.findViewById(R.id.tv_table_frequency);
            if (isFile) {
                layout_table.setOnClickListener(this);
            } else {
                layout_table.setOnClickListener(v -> {
                    Intent intent = new Intent(context, FrequenciesActivity.class);
                    intent.putExtra(ValueCodes.TABLE, getLayoutPosition() + 1);
                    intent.putExtra(ValueCodes.TOTAL, Byte.toUnsignedInt(tables[getLayoutPosition() + 1]));
                    intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
                    intent.putExtra(ValueCodes.RANGE, range);
                    intent.putExtra(ValueCodes.IS_TEMPERATURE, isTemperature);
                    context.startActivity(intent);
                    isFile = false;
                });
            }
        }

        @Override
        public void onClick(View view) {
            adapterClickListener.onAdapterItemClickListener(getLayoutPosition());
        }
    }
}