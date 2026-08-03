package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDetectionFilterActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentPulseRateValuesBinding;

public class PulseRateValuesFragment extends Fragment {
    private FragmentPulseRateValuesBinding binding = null;
    private final int type;
    private int value;
    private final TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            int pulseRate = (binding.etPulseRate.getText().toString().isEmpty()) ? 0 : Integer.parseInt(binding.etPulseRate.getText().toString());
            int tolerance = binding.spPulseRateTolerance.getSelectedItemPosition() + 4;
            value = (pulseRate * 100) + tolerance;
            if (getActivity() instanceof ValueDetectionFilterActivity)
                ((ValueDetectionFilterActivity) getActivity()).value = value;
        }
    };

    public PulseRateValuesFragment(int type, int value) {
        this.type = type;
        this.value = value;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPulseRateValuesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.spPulseRateTolerance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int pulseRate = (binding.etPulseRate.getText().toString().isEmpty()) ? 0 : Integer.parseInt(binding.etPulseRate.getText().toString());
                int tolerance = binding.spPulseRateTolerance.getSelectedItemPosition() + 4;
                value = (pulseRate * 100) + tolerance;
                if (getActivity() instanceof ValueDetectionFilterActivity)
                    ((ValueDetectionFilterActivity) getActivity()).value = value;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.array_vhf_detection_pulse_tolerance, android.R.layout.simple_spinner_item);
        scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spPulseRateTolerance.setAdapter(scanRateAdapter);
        switch (type) {
            case ValueCodes.PULSE_RATE_1_CODE:
                binding.tvPulseRate.setText(R.string.lbl_vhf_detection_target_pr1_ppm);
                binding.tvPulseRateTolerance.setText(R.string.lbl_vhf_detection_target_pr1_tolerance);
                break;
            case ValueCodes.PULSE_RATE_2_CODE:
                binding.tvPulseRate.setText(R.string.lbl_vhf_detection_target_pr2_ppm);
                binding.tvPulseRateTolerance.setText(R.string.lbl_vhf_detection_target_pr2_tolerance);
                break;
            case ValueCodes.PULSE_RATE_3_CODE:
                binding.tvPulseRate.setText(R.string.lbl_vhf_detection_target_pr3_ppm);
                binding.tvPulseRateTolerance.setText(R.string.lbl_vhf_detection_target_pr3_tolerance);
                break;
            case ValueCodes.PULSE_RATE_4_CODE:
                binding.tvPulseRate.setText(R.string.lbl_vhf_detection_target_pr4_ppm);
                binding.tvPulseRateTolerance.setText(R.string.lbl_vhf_detection_target_pr4_tolerance);
                break;
        }
        setPulseRate(value / 100, value % 100);
        binding.etPulseRate.addTextChangedListener(textChangedListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setPulseRate(int pr, int tolerance) {
        binding.etPulseRate.setText(String.valueOf(pr));
        binding.spPulseRateTolerance.setSelection(tolerance - 4);
        value = (pr * 100) + tolerance;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }
}
