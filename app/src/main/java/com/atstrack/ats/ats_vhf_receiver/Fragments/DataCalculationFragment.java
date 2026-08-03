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
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentDataCalculationBinding;

public class DataCalculationFragment extends Fragment {
    private FragmentDataCalculationBinding binding = null;
    private int value;

    public DataCalculationFragment(int value) {
        this.value = value;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDataCalculationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvNone.setOnClickListener(v -> setDataCalculation(ValueCodes.NONE));
        binding.tvTemperature.setOnClickListener(v -> setDataCalculation(ValueCodes.VARIABLE_TEMPERATURE));
        setDataCalculation(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setDataCalculation(int calculation) {
        binding.tvTemperature.setCompoundDrawablesWithIntrinsicBounds(0, 0, calculation == ValueCodes.VARIABLE_TEMPERATURE ? R.drawable.ic_check : 0, 0);
        binding.tvNone.setCompoundDrawablesWithIntrinsicBounds(0, 0, calculation == ValueCodes.NONE ? R.drawable.ic_check : 0, 0);
        value = calculation;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }
}