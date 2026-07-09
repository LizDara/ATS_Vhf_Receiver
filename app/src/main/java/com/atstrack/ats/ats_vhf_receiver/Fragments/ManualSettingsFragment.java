package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.ScanBaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ManualSettingsFragment extends Fragment {
    @BindView(R.id.tv_frequency_manual)
    TextView tv_frequency_manual;
    @BindView(R.id.sw_gps)
    SwitchCompat sw_gps;

    private Unbinder unbinder;
    private final int baseFrequency;
    private final int range;
    private ActivityResultLauncher<Intent> launcher;
    private int currentFrequency;

    public ManualSettingsFragment(int baseFrequency, int range) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        initializeLauncher();
    }

    @OnClick(R.id.btn_enter_new_frequency)
    public void onClickEnterNewFrequency(View v) {
        Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, getString(R.string.lb_change_frequency));
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.btn_start_manual)
    public void onClickStartManual(View v) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fcv_activity_fragment, new ManualScanningFragment(baseFrequency, range, currentFrequency, sw_gps.isChecked()))
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manual_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentFrequency = baseFrequency;
        tv_frequency_manual.setText(String.valueOf(currentFrequency));
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
        if (unbinder != null)
            unbinder.unbind();
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (ValueCodes.CANCELLED == result.getResultCode())
                        return;
                    if (ValueCodes.RESULT_OK == result.getResultCode()) {
                        currentFrequency = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                        tv_frequency_manual.setText(Converters.getFrequency(currentFrequency));
                    }
                });
    }

    private void initialize() {
        if (getActivity() instanceof ScanBaseActivity) {
            ((ScanBaseActivity) getActivity()).setScanViews(false);
        }
    }
}
