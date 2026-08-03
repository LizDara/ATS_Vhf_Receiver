package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDetectionFilterActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentMaxMinPulseRateBinding;

public class MaxMinPulseRateFragment extends Fragment {
    private FragmentMaxMinPulseRateBinding binding = null;
    private final int type;
    private int value;
    private final TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            value = (binding.etMaxMinPulseRate.getText().toString().isEmpty()) ? 0 : Integer.parseInt(binding.etMaxMinPulseRate.getText().toString());
            double period = (binding.etMaxMinPulseRate.getText().toString().isEmpty() ||
                    Integer.parseInt(binding.etMaxMinPulseRate.getText().toString()) == 0) ? 0 : (double) 60000 / value;
            binding.tvPeriodPulseRate.setText(String.format("%.2f ms (period)", period));
            if (getActivity() instanceof ValueDetectionFilterActivity)
                ((ValueDetectionFilterActivity) getActivity()).value = value;
        }
    };

    public MaxMinPulseRateFragment(int type, int value) {
        this.type = type;
        this.value = value;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMaxMinPulseRateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvMaxMinPulseRate.setText(type == ValueCodes.MIN_PULSE_RATE_CODE ? R.string.lbl_vhf_detection_min_pulse_rate : R.string.lbl_vhf_detection_max_pulse_rate);
        setMaxMinPulseRate(value);
        binding.etMaxMinPulseRate.addTextChangedListener(textChangedListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setMaxMinPulseRate(int maxMin) {
        binding.etMaxMinPulseRate.setText(String.valueOf(maxMin));
        double period = (maxMin == 0) ? 0 : (double) 60000 / maxMin;
        binding.tvPeriodPulseRate.setText(String.format("%.2f ms (period)", period));
        value = maxMin;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }
}
