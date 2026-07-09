package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDefaultsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

public class SelectValueFragment extends Fragment {
    @BindView(R.id.sp_value)
    Spinner sp_value;

    private Unbinder unbinder;
    private final int type;
    private int value;
    private byte[] data;

    public SelectValueFragment(int type, int value) {
        this.type = type;
        this.value = value;
    }

    public SelectValueFragment(int type, int value, byte[] tables) {
        this.type = type;
        this.value = value;
        this.data = tables;
    }

    @OnItemSelected(R.id.sp_value)
    public void onItemSelectedValue(AdapterView<?> adapter, View v, int position, long id) {
        if (type == ValueCodes.SCAN_RATE_MOBILE_CODE) // Send the mobile scan rate value
            value = (int) (Float.parseFloat(sp_value.getSelectedItem().toString()) * 10);
        else if (type == ValueCodes.SCAN_RATE_STATIONARY_CODE) // Send the mobile scan rate value
            value = Integer.parseInt(sp_value.getSelectedItem().toString());
        else if (type == ValueCodes.TABLE_NUMBER_CODE) // Send the frequency table number
            value = (sp_value.getSelectedItem().toString().equals("None")) ? 0 :
                    Integer.parseInt(sp_value.getSelectedItem().toString().replace("Table ", ""));
        else if (type == ValueCodes.NUMBER_OF_ANTENNAS_CODE) // Send the number of antennas
            value = sp_value.getSelectedItemPosition() + 1;
        else if (type == ValueCodes.SCAN_TIMEOUT_SECONDS_CODE) // Send scan timeout value
            value = Integer.parseInt(sp_value.getSelectedItem().toString());
        else if (type == ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE)
            value = sp_value.getSelectedItemPosition();
        if (getActivity() instanceof ValueDefaultsActivity)
            ((ValueDefaultsActivity) getActivity()).value = value;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_value, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (type) {
            case ValueCodes.TABLE_NUMBER_CODE:
                setTables();
                break;
            case ValueCodes.SCAN_RATE_MOBILE_CODE:
            case ValueCodes.SCAN_RATE_STATIONARY_CODE:
                setScanRate();
                break;
            case ValueCodes.NUMBER_OF_ANTENNAS_CODE:
                setAntennas();
                break;
            case ValueCodes.SCAN_TIMEOUT_SECONDS_CODE:
                setTimeout();
                break;
            case ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE:
                setReferenceFrequencyStoreRate();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setTables() {
        List<String> tables = new ArrayList<>();
        int position = 0;
        for (int i = 1; i <= 12; i++) {
            if (data[i] > 0) {
                tables.add("Table " + i);
                if (value == i) position = tables.size() - 1;
            }
        }
        if (tables.isEmpty())
            tables.add("None");
        ArrayAdapter<String> tablesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tables);
        tablesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_value.setAdapter(tablesAdapter);
        sp_value.setSelection(position);
    }

    private void setScanRate() {
        if (type == ValueCodes.SCAN_RATE_MOBILE_CODE) { // Mobile scan rate
            ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.scanRateMobile, android.R.layout.simple_spinner_item);
            scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_value.setAdapter(scanRateAdapter);

            int index = 0;
            for (int i = 0; i < 49; i++) {
                String item = sp_value.getItemAtPosition(i).toString().replace(".", "");
                if (item.equals(String.valueOf(value))) {
                    index = i;
                    break;
                }
            }
            sp_value.setSelection(index);
        } else { // Stationary scan rate
            ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.scanRateStationary, android.R.layout.simple_spinner_item);
            scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_value.setAdapter(scanRateAdapter);

            if (value <= 255)
                sp_value.setSelection(value - 10);
            else
                sp_value.setSelection(0);
        }
    }

    private void setAntennas() {
        ArrayAdapter<CharSequence> antennasAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.antennas, android.R.layout.simple_spinner_item);
        antennasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_value.setAdapter(antennasAdapter);

        if (value <= 4 && value > 0)
            sp_value.setSelection(value - 1);
        else
            sp_value.setSelection(0);
    }

    private void setTimeout() {
        ArrayAdapter<CharSequence> timeoutAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.timeout, android.R.layout.simple_spinner_item);
        timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_value.setAdapter(timeoutAdapter);

        if (value <= 200)
            sp_value.setSelection(value - 4);
        else
            sp_value.setSelection(0);
    }

    private void setReferenceFrequencyStoreRate() {
        ArrayAdapter<CharSequence> storeRateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.referenceFrequencyStoreRate, android.R.layout.simple_spinner_item);
        storeRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_value.setAdapter(storeRateAdapter);

        if (value <= 24)
            sp_value.setSelection(value);
        else
            sp_value.setSelection(0);
    }
}
