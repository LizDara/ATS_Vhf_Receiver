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

import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyAdapter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.FrequenciesActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class EmptyTableFragment extends Fragment {
    private Unbinder unbinder;
    private final FrequencyAdapter frequencyAdapter;
    private ActivityResultLauncher<Intent> launcher;

    public EmptyTableFragment(FrequencyAdapter frequencyAdapter) {
        this.frequencyAdapter = frequencyAdapter;
        initializeLauncher();
    }

    @OnClick(R.id.btn_add_new_frequency)
    public void onClickAddFrequency(View v) {
        if (frequencyAdapter.isTemperature) {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment, new TemperatureFrequencyFragment(-1, frequencyAdapter), String.valueOf(ValueCodes.SECOND_STEP))
                        .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                        .commit();
            }
        } else {
            Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
            intent.putExtra(ValueCodes.TITLE, "Add Frequency");
            intent.putExtra(ValueCodes.POSITION, -1);
            intent.putExtra(ValueCodes.BASE_FREQUENCY, frequencyAdapter.baseFrequency);
            intent.putExtra(ValueCodes.RANGE, frequencyAdapter.range);
            launcher.launch(intent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_empty_table, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbarTitle();

        getParentFragmentManager().setFragmentResultListener(ValueCodes.IS_TEMPERATURE, this, (requestKey, result) -> {
            if (!isAdded() || getView() == null) return;
            int frequency = result.getInt(ValueCodes.VALUE, 0);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fcv_activity_fragment, new FrequenciesOverviewFragment(frequencyAdapter, frequency), String.valueOf(ValueCodes.FIRST_STEP))
                        .commit();
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            setToolbarTitle();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setToolbarTitle() {
        if (getActivity() instanceof FrequenciesActivity) {
            ((FrequenciesActivity) getActivity()).setToolbarTitle("Table " + frequencyAdapter.tableNumber + " (" + frequencyAdapter.getItemCount() + " Frequencies)");
        }
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (ValueCodes.CANCELLED == result.getResultCode())
                        return;
                    if (ValueCodes.RESULT_OK == result.getResultCode()) {
                        int position = result.getData().getIntExtra(ValueCodes.POSITION, 0);
                        if (position > -2) {
                            int frequency = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                            if (!frequencyAdapter.isTemperature) { //Save frequency in the list
                                if (getParentFragmentManager() != null) {
                                    getParentFragmentManager().beginTransaction()
                                            .setReorderingAllowed(true)
                                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                            .replace(R.id.fcv_activity_fragment, new FrequenciesOverviewFragment(frequencyAdapter, frequency))
                                            .commit();
                                }
                            }
                        }
                    }
                });
    }
}
