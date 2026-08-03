package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDetectionFilterActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentMatchesNumberBinding;

public class MatchesNumberFragment extends Fragment {
    private FragmentMatchesNumberBinding binding = null;
    private int matches;

    public MatchesNumberFragment(int value) {
        this.matches = value;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMatchesNumberBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View.OnClickListener listener = v -> {
            TextView text = (TextView) v;
            setMatchesForValidPattern(Integer.parseInt(text.getText().toString()));
        };
        binding.tvTwo.setOnClickListener(listener);
        binding.tvThree.setOnClickListener(listener);
        binding.tvFour.setOnClickListener(listener);
        binding.tvFive.setOnClickListener(listener);
        binding.tvSix.setOnClickListener(listener);
        binding.tvSeven.setOnClickListener(listener);
        binding.tvEight.setOnClickListener(listener);
        setMatchesForValidPattern(matches);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setMatchesForValidPattern(int matches) {
        for (int i = 0; i < 2; i ++) {
            switch (this.matches) {
                case 2:
                    binding.tvTwo.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 3:
                    binding.tvThree.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 4:
                    binding.tvFour.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 5:
                    binding.tvFive.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 6:
                    binding.tvSix.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 7:
                    binding.tvSeven.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 8:
                    binding.tvEight.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
            }
            this.matches = matches;
        }
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = matches;
    }
}
