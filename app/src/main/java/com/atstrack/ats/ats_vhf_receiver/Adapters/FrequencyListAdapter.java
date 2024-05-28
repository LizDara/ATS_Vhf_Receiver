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

import com.atstrack.ats.ats_vhf_receiver.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.R;

import java.util.ArrayList;

import androidx.activity.result.ActivityResultLauncher;

public class FrequencyListAdapter extends BaseAdapter {
    private final Context context;
    private LayoutInflater inflater;
    private ArrayList<Integer> frequencies;
    private int baseFrequency;
    private int range;
    private ActivityResultLauncher<Intent> launcher;

    public FrequencyListAdapter(Context context, ArrayList<Integer> frequencies, int baseFrequency, int range, ActivityResultLauncher<Intent> launcher) {
        this.context = context;
        this.frequencies = frequencies;
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.launcher = launcher;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return frequencies.size();
    }

    public int getFrequency(int index) {
        return frequencies.get(index);
    }

    public void setFrequency(int index, int frequency) {
        frequencies.set(index, frequency);
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
        view = inflater.inflate(R.layout.frequency_information, parent, false);
        view.setElevation(4);

        LinearLayout frequency = view.findViewById(R.id.frequency_linearLayout);
        TextView frequencyNumber = view.findViewById(R.id.frequency_number_textView);

        frequencyNumber.setText(String.valueOf(frequencies.get(position)));
        frequency.setOnClickListener(v -> {
            Intent intent = new Intent(context, EnterFrequencyActivity.class);
            intent.putExtra("title", "Edit Frequency " + frequencies.get(position));
            intent.putExtra("position", position);
            intent.putExtra("baseFrequency", baseFrequency);
            intent.putExtra("range", range);
            launcher.launch(intent);
        });

        return view;
    }
}
