package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentSelectValueBinding;

import java.util.ArrayList;
import java.util.List;

public class SelectValueFragment extends Fragment {
    private FragmentSelectValueBinding binding = null;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSelectValueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.spValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (type == ValueCodes.SCAN_RATE_MOBILE_CODE) // Send the mobile scan rate value
                    value = (int) (Float.parseFloat(binding.spValue.getSelectedItem().toString()) * 10);
                else if (type == ValueCodes.SCAN_RATE_STATIONARY_CODE) // Send the mobile scan rate value
                    value = Integer.parseInt(binding.spValue.getSelectedItem().toString());
                else if (type == ValueCodes.TABLE_NUMBER_CODE) // Send the frequency table number
                    value = (binding.spValue.getSelectedItem().toString().equals(getString(R.string.lbl_vhf_manual_option_none))) ? 0 :
                            Integer.parseInt(binding.spValue.getSelectedItem().toString().replace("Table ", ""));
                else if (type == ValueCodes.NUMBER_OF_ANTENNAS_CODE) // Send the number of antennas
                    value = binding.spValue.getSelectedItemPosition() + 1;
                else if (type == ValueCodes.SCAN_TIMEOUT_SECONDS_CODE) // Send scan timeout value
                    value = Integer.parseInt(binding.spValue.getSelectedItem().toString());
                else if (type == ValueCodes.REFERENCE_FREQUENCY_STORE_RATE_CODE)
                    value = binding.spValue.getSelectedItemPosition() + 1;
                if (getActivity() instanceof ValueDefaultsActivity)
                    ((ValueDefaultsActivity) getActivity()).value = value;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
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
        binding = null;
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
            tables.add(getString(R.string.lbl_vhf_manual_option_none));
        ArrayAdapter<String> tablesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tables);
        tablesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spValue.setAdapter(tablesAdapter);
        binding.spValue.setSelection(position);
    }

    private void setScanRate() {
        if (type == ValueCodes.SCAN_RATE_MOBILE_CODE) { // Mobile scan rate
            ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.array_vhf_mobile_scan_rate, android.R.layout.simple_spinner_item);
            scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spValue.setAdapter(scanRateAdapter);

            int index = 0;
            for (int i = 0; i < 49; i++) {
                String item = binding.spValue.getItemAtPosition(i).toString().replace(".", "");
                if (item.equals(String.valueOf(value))) {
                    index = i;
                    break;
                }
            }
            binding.spValue.setSelection(index);
        } else { // Stationary scan rate
            ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.array_vhf_stationary_scan_rate, android.R.layout.simple_spinner_item);
            scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spValue.setAdapter(scanRateAdapter);

            if (value <= Byte.toUnsignedInt(ValueCodes.NULL))
                binding.spValue.setSelection(value - 10);
            else
                binding.spValue.setSelection(0);
        }
    }

    private void setAntennas() {
        ArrayAdapter<CharSequence> antennasAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.array_vhf_stationary_antennas, android.R.layout.simple_spinner_item);
        antennasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spValue.setAdapter(antennasAdapter);

        if (value <= 4 && value > 0)
            binding.spValue.setSelection(value - 1);
        else
            binding.spValue.setSelection(0);
    }

    private void setTimeout() {
        ArrayAdapter<CharSequence> timeoutAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.array_vhf_stationary_timeout, android.R.layout.simple_spinner_item);
        timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spValue.setAdapter(timeoutAdapter);

        if (value >= 4 && value <= 200)
            binding.spValue.setSelection(value - 4);
        else
            binding.spValue.setSelection(0);
    }

    private void setReferenceFrequencyStoreRate() {
        ArrayAdapter<CharSequence> storeRateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.array_vhf_stationary_reference_rate, android.R.layout.simple_spinner_item);
        storeRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spValue.setAdapter(storeRateAdapter);

        if (value > 0 && value <= 24)
            binding.spValue.setSelection(value - 1);
        else
            binding.spValue.setSelection(0);
    }
}
