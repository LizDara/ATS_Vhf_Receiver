package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Models.LoadedTable;
import com.atstrack.ats.ats_vhf_receiver.R;

import java.util.List;

public class LoadedTableAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<LoadedTable> loadedTables;
    private final boolean isRemoved;

    public LoadedTableAdapter(Context context, List<LoadedTable> loadedTables, boolean isRemoved) {
        inflater = LayoutInflater.from(context);
        this.loadedTables = loadedTables;
        this.isRemoved = isRemoved;
    }

    @Override
    public int getCount() {
        return loadedTables.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.item_loaded_table, parent, false);

        TextView tv_loaded_frequencies_subtitle = view.findViewById(R.id.tv_loaded_frequencies_subtitle);
        TextView tv_loaded_frequencies = view.findViewById(R.id.tv_loaded_frequencies);

        if (isRemoved) {
            tv_loaded_frequencies_subtitle.setText("Table " + loadedTables.get(position).tableNumber);
            String frequencies = "";
            for (int frequency : loadedTables.get(position).frequenciesLoaded)
                frequencies += frequency + "\n";
            tv_loaded_frequencies.setText(frequencies);
        } else {
            tv_loaded_frequencies_subtitle.setVisibility(View.GONE);
            String frequencies = "";
            for (int frequency : loadedTables.get(position).frequenciesLoaded)
                frequencies += frequency + "\n";
            tv_loaded_frequencies.setText(frequencies);
        }

        return view;
    }
}
