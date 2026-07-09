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
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDetectionFilterActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PulseRateTypesFragment extends Fragment {
    @BindView(R.id.tv_coded)
    TextView tv_coded;
    @BindView(R.id.tv_fixed_pulse_rate)
    TextView tv_fixed_pulse_rate;
    @BindView(R.id.tv_variable_pulse_rate)
    TextView tv_variable_pulse_rate;

    private Unbinder unbinder;
    private int value;

    public PulseRateTypesFragment(int value) {
        this.value = value;
    }

    @OnClick(R.id.tv_coded)
    public void onClickCoded(View v) {
        setPulseRateType(ValueCodes.CODED);
    }

    @OnClick(R.id.tv_fixed_pulse_rate)
    public void onClickFixedPulseRate(View v) {
        setPulseRateType(ValueCodes.FIXED);
    }

    @OnClick(R.id.tv_variable_pulse_rate)
    public void onClickVariablePulseRate(View v) {
        setPulseRateType(ValueCodes.VARIABLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pulse_rate_types, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPulseRateType((byte) value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setPulseRateType(byte detectionType) {
        tv_coded.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        tv_fixed_pulse_rate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        tv_variable_pulse_rate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (detectionType == ValueCodes.CODED)
            tv_coded.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
        else if (detectionType == ValueCodes.FIXED)
            tv_fixed_pulse_rate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
        else if (detectionType == ValueCodes.VARIABLE)
            tv_variable_pulse_rate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
        value = detectionType;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }
}
