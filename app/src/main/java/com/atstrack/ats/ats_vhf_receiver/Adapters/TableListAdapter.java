package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Models.LoadedTable;
import com.atstrack.ats.ats_vhf_receiver.Utils.Messages;
import com.atstrack.ats.ats_vhf_receiver.VHF.FrequenciesActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.LinkedList;
import java.util.List;

public class TableListAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private byte[] tables;
    private int baseFrequency;
    private int range;
    private List<LoadedTable> loadedTables;
    private boolean isTemperature;
    private boolean isFile;

    public TableListAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        loadedTables = new LinkedList<>();
        isFile = false;
    }

    public void setData(byte[] tables) {
        this.tables = tables;
        baseFrequency = Byte.toUnsignedInt(tables[13]);
        range = Byte.toUnsignedInt(tables[14]);
    }

    public void setTemperature(boolean temperature) {
        isTemperature = temperature;
    }

    public boolean isFile() {
        return isFile;
    }

    public int getBaseFrequency() {
        return baseFrequency;
    }

    public int getRange() {
        return range;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public void addLoadedTable(LoadedTable table) {
        loadedTables.add(table);
    }

    public List<LoadedTable> getLoadedTables() {
        return loadedTables;
    }

    public void emptyLoadedTables() {
        loadedTables = new LinkedList<>();
    }

    @Override
    public int getCount() {
        return isFile ? loadedTables.size() : tables.length - 3;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.table_item, parent, false);
        view.setElevation(4);

        LinearLayout table = view.findViewById(R.id.table_linearLayout);
        TextView tableNumber = view.findViewById(R.id.table_number_textView);
        TextView frequenciesNumber = view.findViewById(R.id.table_frequency_textView);

        if (isFile) {
            tableNumber.setText("Table " + loadedTables.get(position).tableNumber);
            frequenciesNumber.setText(loadedTables.get(position).frequenciesLoaded.length + " frequencies");
        } else {
            tableNumber.setText("Table " + (position + 1));
            frequenciesNumber.setText(Byte.toUnsignedInt(tables[position + 1]) + " frequencies");
        }
        table.setOnClickListener(v -> {
            if (isFile) {
                List<LoadedTable> frequenciesTable = new LinkedList<>();
                frequenciesTable.add(loadedTables.get(position));
                Messages.showLoadedFrequenciesMessage(context, "Table " + loadedTables.get(position).tableNumber, frequenciesTable, false);
            } else {
                Intent intent = new Intent(context, FrequenciesActivity.class);
                intent.putExtra(ValueCodes.TABLE, position + 1);
                intent.putExtra(ValueCodes.TOTAL, Byte.toUnsignedInt(tables[position + 1]));
                intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
                intent.putExtra(ValueCodes.RANGE, range);
                intent.putExtra(ValueCodes.IS_TEMPERATURE, isTemperature);
                context.startActivity(intent);
                isFile = false;
            }
        });

        return view;
    }
}