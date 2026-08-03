package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentStoreRateValuesBinding;

public class StoreRateValuesFragment extends Fragment {
    private FragmentStoreRateValuesBinding binding = null;
    private int storeRate;

    public StoreRateValuesFragment(int storeRate) {
        this.storeRate = storeRate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStoreRateValuesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvContinuousStore.setOnClickListener(v -> setStoreRate(0));
        binding.tvFiveMinutes.setOnClickListener(v -> setStoreRate(5));
        binding.tvTenMinutes.setOnClickListener(v -> setStoreRate(10));
        binding.tvFifteenMinutes.setOnClickListener(v -> setStoreRate(15));
        binding.tvThirtyMinutes.setOnClickListener(v -> setStoreRate(30));
        binding.tvSixtyMinutes.setOnClickListener(v -> setStoreRate(60));
        binding.tvOneHundredTwentyMinutes.setOnClickListener(v -> setStoreRate(120));
        setStoreRate(storeRate);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setStoreRate(int storeRate) {
        for (int i = 0; i < 2; i ++) {
            switch (this.storeRate) {
                case 0:
                    binding.tvContinuousStore.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 5:
                    binding.tvFiveMinutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 10:
                    binding.tvTenMinutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 15:
                    binding.tvFifteenMinutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 30:
                    binding.tvThirtyMinutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 60:
                    binding.tvSixtyMinutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 120:
                    binding.tvOneHundredTwentyMinutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
            }
            this.storeRate = storeRate;
        }
        if (getActivity() instanceof ValueDefaultsActivity)
            ((ValueDefaultsActivity) getActivity()).value = storeRate;
    }
}
