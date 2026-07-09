package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.R;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;

public class TableToScanAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final byte[] frequencies;
    private final ArrayList<Boolean> selected;
    private final ArrayList<Integer> tables;
    private final TextView option_tables_textView;
    private final Button merge_tables_button;

    public TableToScanAdapter(Context context, byte[] frequencies, ArrayList<Integer> tables, TextView option_tables_textView, Button merge_tables_button) {
        this.frequencies = frequencies;
        this.tables = tables;
        this.option_tables_textView = option_tables_textView;
        this.merge_tables_button = merge_tables_button;
        inflater = LayoutInflater.from(context);
        selected = new ArrayList<>();
        for (int i = 0; i < frequencies.length; i++)
            selected.add(false);
        for (int number : tables) {
            if (number <= 12)
                selected.set(number - 1, true);
        }
    }

    @Override
    public int getCount() {
        return frequencies.length - 3;
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
        view = inflater.inflate(R.layout.item_frequency_select, parent, false);
        view.setElevation(4);

        CheckBox cb_frequency_number = view.findViewById(R.id.cb_frequency_number);

        if (frequencies[position + 1] == 0) {
            cb_frequency_number.setEnabled(false);
            cb_frequency_number.setTextColor(ContextCompat.getColor(inflater.getContext(), R.color.slate_gray));
        }
        cb_frequency_number.setText("Table " + (position + 1) + " (" + frequencies[position + 1] + " frequencies)");
        cb_frequency_number.setChecked(selected.get(position));
        cb_frequency_number.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tables.add(position + 1);
            } else {
                int index = 0;
                while (index < tables.size()) {
                    if (tables.get(index) == position + 1) {
                        tables.remove(index);
                        index = tables.size();
                    } else {
                        index++;
                    }
                }
            }
            selected.set(position, isChecked);
            option_tables_textView.setText(tables.size() + " Selected Tables (3 Max)");
            if (tables.size() > 3) {
                merge_tables_button.setEnabled(false);
                merge_tables_button.setAlpha((float) 0.6);
            } else {
                merge_tables_button.setEnabled(true);
                merge_tables_button.setAlpha(1);
            }
        });

        return view;
    }
}