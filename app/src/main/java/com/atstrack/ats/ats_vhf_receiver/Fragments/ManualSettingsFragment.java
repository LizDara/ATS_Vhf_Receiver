package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.ScanBaseActivity;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentManualSettingsBinding;

public class ManualSettingsFragment extends Fragment {
    private FragmentManualSettingsBinding binding = null;
    private final int baseFrequency;
    private final int range;
    private ActivityResultLauncher<Intent> launcher;
    private int currentFrequency;

    public ManualSettingsFragment(int baseFrequency, int range) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        initializeLauncher();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManualSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnEnterNewFrequency.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
            intent.putExtra(ValueCodes.TITLE, getString(R.string.title_vhf_manual_change_frequency));
            intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
            intent.putExtra(ValueCodes.RANGE, range);
            launcher.launch(intent);
        });
        binding.btnStartManual.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fcv_activity_fragment, new ManualScanningFragment(baseFrequency, range, currentFrequency, binding.includeGpsOption.swGps.isChecked()))
                        .commit();
            }
        });
        currentFrequency = baseFrequency;
        binding.tvFrequencyManual.setText(String.valueOf(currentFrequency));
        initialize();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            initialize();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (ValueCodes.CANCELLED == result.getResultCode())
                        return;
                    if (ValueCodes.RESULT_OK == result.getResultCode()) {
                        currentFrequency = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                        binding.tvFrequencyManual.setText(Converters.getFrequency(currentFrequency));
                    }
                });
    }

    private void initialize() {
        if (getActivity() instanceof ScanBaseActivity) {
            ((ScanBaseActivity) getActivity()).setScanViews(false);
        }
    }
}
