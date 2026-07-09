package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDetectionFilterActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MaxMinPulseRateFragment extends Fragment {
    @BindView(R.id.tv_max_min_pulse_rate)
    TextView tv_max_min_pulse_rate;
    @BindView(R.id.et_max_min_pulse_rate)
    EditText et_max_min_pulse_rate;
    @BindView(R.id.tv_period_pulse_rate)
    TextView tv_period_pulse_rate;

    private Unbinder unbinder;
    private final int type;
    private int value;
    private final TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            value = (et_max_min_pulse_rate.getText().toString().isEmpty()) ? 0 : Integer.parseInt(et_max_min_pulse_rate.getText().toString());
            double period = (et_max_min_pulse_rate.getText().toString().isEmpty() ||
                    Integer.parseInt(et_max_min_pulse_rate.getText().toString()) == 0) ? 0 : (double) 60000 / value;
            tv_period_pulse_rate.setText(String.format("%.2f ms (period)", period));
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
        View view = inflater.inflate(R.layout.fragment_max_min_pulse_rate, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_max_min_pulse_rate.setText(type == ValueCodes.MIN_PULSE_RATE_CODE ? R.string.lb_min_pulse_rate : R.string.lb_max_pulse_rate);
        setMaxMinPulseRate(value);
        et_max_min_pulse_rate.addTextChangedListener(textChangedListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setMaxMinPulseRate(int maxMin) {
        et_max_min_pulse_rate.setText(String.valueOf(maxMin));
        double period = (maxMin == 0) ? 0 : (double) 60000 / maxMin;
        tv_period_pulse_rate.setText(String.format("%.2f ms (period)", period));
        value = maxMin;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }
}
