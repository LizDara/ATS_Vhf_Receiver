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

public class LoadedFrequenciesAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<LoadedTable> loadedTables;
    private final boolean isRemoved;

    public LoadedFrequenciesAdapter(Context context, List<LoadedTable> loadedTables, boolean isRemoved) {
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
        view = inflater.inflate(R.layout.loaded_frequencies_item, parent, false);

        TextView loaded_frequencies_subtitle_textView = view.findViewById(R.id.loaded_frequencies_subtitle_textView);
        TextView loaded_frequencies_textView = view.findViewById(R.id.loaded_frequencies_textView);

        if (isRemoved) {
            loaded_frequencies_subtitle_textView.setText("Table " + loadedTables.get(position).tableNumber);
            String frequencies = "";
            for (int frequency : loadedTables.get(position).frequenciesLoaded)
                frequencies += frequency + "\n";
            loaded_frequencies_textView.setText(frequencies);
        } else {
            loaded_frequencies_subtitle_textView.setVisibility(View.GONE);
            String frequencies = "";
            for (int frequency : loadedTables.get(position).frequenciesLoaded)
                frequencies += frequency + "\n";
            loaded_frequencies_textView.setText(frequencies);
        }

        return view;
    }
}
