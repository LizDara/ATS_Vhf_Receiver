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

import com.atstrack.ats.ats_vhf_receiver.EditTablesActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;

public class TableListAdapter extends BaseAdapter {
    private final Context context;
    private LayoutInflater inflater;
    private byte[] tables;
    private int baseFrequency;
    private int range;
    private int[][] frequencies;

    public TableListAdapter(Context context, byte[] tables) {
        this.context = context;
        this.tables = tables;
        inflater = LayoutInflater.from(context);
        frequencies = new int[12][];

        baseFrequency = Integer.parseInt(Converters.getDecimalValue(tables[13]));
        range = Integer.parseInt(Converters.getDecimalValue(tables[14]));
    }

    public void setFrequenciesFile(int[][] frequencies) {
        this.frequencies = frequencies;
    }

    @Override
    public int getCount() {
        return tables.length - 3;
    }

    public void setFrequenciesNumber(int position, byte number) {
        tables[position] = number;
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
        view = inflater.inflate(R.layout.table_information, parent, false);
        view.setElevation(4);

        LinearLayout table = view.findViewById(R.id.table_linearLayout);
        TextView tableNumber = view.findViewById(R.id.table_number_textView);
        TextView frequenciesNumber = view.findViewById(R.id.table_frequency_textView);

        tableNumber.setText("Table " + (position + 1));
        frequenciesNumber.setText(Converters.getDecimalValue(tables[position + 1]) + " frequencies");
        table.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTablesActivity.class);
            intent.putExtra("number", position + 1);
            intent.putExtra("total", Integer.parseInt(Converters.getDecimalValue(tables[position + 1])));
            intent.putExtra("baseFrequency", baseFrequency);
            intent.putExtra("range", range);
            intent.putExtra("isFile", frequencies[position] != null);
            if (frequencies[position] != null)
                intent.putExtra("frequencies", frequencies[position]);
            context.startActivity(intent);
        });

        return view;
    }
}
