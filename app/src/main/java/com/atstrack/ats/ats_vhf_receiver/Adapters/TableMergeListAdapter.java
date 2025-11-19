package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;

import com.atstrack.ats.ats_vhf_receiver.R;

import java.util.ArrayList;

public class TableMergeListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final ArrayList<Integer> tables;
    private final ArrayList<Integer> frequencies;
    private final ArrayList<Boolean> selected;
    private final Button merge_tables_button;
    private int tableNumber;

    public TableMergeListAdapter(Context context, ArrayList<Integer> tables, ArrayList<Integer> frequencies, Button merge_tables_button) {
        this.tables = tables;
        this.frequencies = frequencies;
        this.merge_tables_button = merge_tables_button;
        inflater = LayoutInflater.from(context);
        selected = new ArrayList<>(tables.size());
        for (int i = 0; i < tables.size(); i++)
            selected.add(false);
    }

    @Override
    public int getCount() {
        return tables.size();
    }

    public void initialize() {
        for (int i = 0; i < frequencies.size(); i++)
            selected.set(i, false);
    }

    public int getTableNumber() {
        return tableNumber;
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
        view = inflater.inflate(R.layout.frequency_select_item, parent, false);
        view.setElevation(4);

        CheckBox frequencyNumber = view.findViewById(R.id.frequency_number_checkBox);

        frequencyNumber.setText("Table " + tables.get(position) + " (" + frequencies.get(position) + " frequencies)");
        frequencyNumber.setChecked(selected.get(position));
        frequencyNumber.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selected.set(position, isChecked);
            if (isChecked) {
                tableNumber = tables.get(position);
                merge_tables_button.setEnabled(true);
                merge_tables_button.setAlpha(1);
                for (int i = 0; i < selected.size(); i++) {
                    if (i != position)
                        selected.set(i, false);
                }
                notifyDataSetChanged();
            } else {
                tableNumber = 0;
                merge_tables_button.setEnabled(false);
                merge_tables_button.setAlpha((float) 0.6);
            }
        });

        return view;
    }
}