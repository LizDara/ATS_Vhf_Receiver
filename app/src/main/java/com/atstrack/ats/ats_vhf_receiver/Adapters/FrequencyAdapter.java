package com.atstrack.ats.ats_vhf_receiver.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FrequencyAdapter extends RecyclerView.Adapter<FrequencyAdapter.FrequencyViewHolder> {
    public Context context;
    private LayoutInflater inflater;
    public int[] originalTable;
    public ArrayList<Integer> frequencies;
    public final int tableNumber;
    public final int baseFrequency;
    public final int range;
    public ActivityResultLauncher<Intent> launcher;
    public final boolean isTemperature;
    public boolean saveCoefficients;
    public OnAdapterClickListener adapterClickListener;

    public FrequencyAdapter(int tableNumber, int baseFrequency, int range, boolean isTemperature, int total) {
        this.tableNumber = tableNumber;
        this.frequencies = new ArrayList<>();
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.isTemperature = isTemperature;
        this.originalTable = new int[total];
        saveCoefficients = false;
    }

    public void setContext(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public FrequencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_frequency, parent, false);
        view.setElevation(4);
        return new FrequencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrequencyViewHolder holder, int position) {
        holder.tv_frequency_number.setText(String.valueOf(frequencies.get(position)));
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(32, 16, 32, 16);
        holder.layout_frequency.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return frequencies.size();
    }

    public class FrequencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout layout_frequency;
        TextView tv_frequency_number;

        public FrequencyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_frequency = itemView.findViewById(R.id.layout_frequency);
            tv_frequency_number = itemView.findViewById(R.id.tv_frequency_number);
            if (isTemperature) {
                layout_frequency.setOnClickListener(this);
            } else {
                layout_frequency.setOnClickListener(v -> {
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