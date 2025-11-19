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

public class FrequencyDeleteListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final ArrayList<Integer> frequencies;
    private final ArrayList<Boolean> selected;
    private final CheckBox all_frequencies_checkBox;
    private final Button delete_selected_frequencies_button;

    public FrequencyDeleteListAdapter(Context context, ArrayList<Integer> frequencies, CheckBox all_frequencies_checkBox, Button delete_selected_frequencies_button) {
        this.frequencies = frequencies;
        this.all_frequencies_checkBox = all_frequencies_checkBox;
        this.delete_selected_frequencies_button = delete_selected_frequencies_button;
        inflater = LayoutInflater.from(context);
        selected = new ArrayList<>();
        for (int i = 0; i < frequencies.size(); i++)
            selected.add(false);
    }

    @Override
    public int getCount() {
        return frequencies.size();
    }

    public void setStateSelected(boolean isChecked) {
        for (int i = 0; i < frequencies.size(); i++)
            selected.set(i, isChecked);
    }

    public boolean isSelected(int index) {
        return selected.get(index);
    }

    public void addFrequency(int frequency) {
        frequencies.add(frequency);
        selected.add(false);
    }

    public void removeFrequency(int index) {
        frequencies.remove(index);
        selected.remove(index);
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

        frequencyNumber.setText(String.valueOf(frequencies.get(position)));
        frequencyNumber.setChecked(selected.get(position));
        frequencyNumber.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                delete_selected_frequencies_button.setEnabled(true);
                delete_selected_frequencies_button.setAlpha(1);
            }
            selected.set(position, isChecked);
            int count = 0;
            for (boolean isSelected : selected) {
                if (isSelected)
                    count++;
            }
            all_frequencies_checkBox.setChecked(count == selected.size());
            if (count == 0) {
                delete_selected_frequencies_button.setEnabled(false);
                delete_selected_frequencies_button.setAlpha((float) 0.6);
            }
        });

        return view;
    }
}