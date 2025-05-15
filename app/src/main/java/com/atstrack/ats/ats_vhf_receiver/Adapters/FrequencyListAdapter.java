package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Utils.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FrequencyListAdapter extends RecyclerView.Adapter<FrequencyListAdapter.MyViewHolder> {
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<Integer> frequencies;
    private final int baseFrequency;
    private final int range;
    private final ActivityResultLauncher<Intent> launcher;
    private final boolean isTemperature;
    private final OnAdapterClickListener adapterClickListener;

    public FrequencyListAdapter(Context context, ArrayList<Integer> frequencies, int baseFrequency, int range,
                                ActivityResultLauncher<Intent> launcher, boolean isTemperature, OnAdapterClickListener listener) {
        this.context = context;
        this.frequencies = frequencies;
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.launcher = launcher;
        this.isTemperature = isTemperature;
        adapterClickListener = listener;
        inflater = LayoutInflater.from(context);
    }

    public int getFrequency(int index) {
        return frequencies.get(index);
    }

    public void setFrequency(int index, int frequency) {
        frequencies.set(index, frequency);
    }

    @NonNull
    @Override
    public FrequencyListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.device_information, parent, false);
        view.setElevation(4);
        return new FrequencyListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrequencyListAdapter.MyViewHolder holder, int position) {
        holder.frequencyNumber.setText(String.valueOf(frequencies.get(position)));
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.frequency.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return frequencies.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout frequency;
        TextView frequencyNumber;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            frequency = itemView.findViewById(R.id.frequency_linearLayout);
            frequencyNumber = itemView.findViewById(R.id.frequency_number_textView);
            if (isTemperature) {
                frequency.setOnClickListener(this);
            } else {
                frequency.setOnClickListener(v -> {
                    Intent intent = new Intent(context, EnterFrequencyActivity.class);
                    intent.putExtra(ValueCodes.TITLE, "Edit Frequency " + frequencies.get(getLayoutPosition()));
                    intent.putExtra(ValueCodes.POSITION, getLayoutPosition());
                    intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
                    intent.putExtra(ValueCodes.RANGE, range);
                    launcher.launch(intent);
                });
            }
        }

        @Override
        public void onClick(View view) {
            adapterClickListener.onAdapterItemClickListener(getLayoutPosition());
        }
    }
}