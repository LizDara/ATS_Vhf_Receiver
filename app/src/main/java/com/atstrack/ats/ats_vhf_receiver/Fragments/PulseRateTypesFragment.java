package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDetectionFilterActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentPulseRateTypesBinding;

public class PulseRateTypesFragment extends Fragment {
    private FragmentPulseRateTypesBinding binding = null;
    private int value;

    public PulseRateTypesFragment(int value) {
        this.value = value;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPulseRateTypesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvCoded.setOnClickListener(v -> setPulseRateType(ValueCodes.CODED));
        binding.tvFixedPulseRate.setOnClickListener(v -> setPulseRateType(ValueCodes.FIXED));
        binding.tvVariablePulseRate.setOnClickListener(v -> setPulseRateType(ValueCodes.VARIABLE));
        setPulseRateType((byte) value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setPulseRateType(byte detectionType) {
        binding.tvCoded.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        binding.tvFixedPulseRate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        binding.tvVariablePulseRate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (detectionType == ValueCodes.CODED)
            binding.tvCoded.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
        else if (detectionType == ValueCodes.FIXED)
            binding.tvFixedPulseRate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
        else if (detectionType == ValueCodes.VARIABLE)
            binding.tvVariablePulseRate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
        value = detectionType;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }
}
