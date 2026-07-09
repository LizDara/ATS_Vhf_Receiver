package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDetectionFilterActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

public class PulseRateValuesFragment extends Fragment {
    @BindView(R.id.tv_pulse_rate)
    TextView tv_pulse_rate;
    @BindView(R.id.et_pulse_rate)
    EditText et_pulse_rate;
    @BindView(R.id.tv_pulse_rate_tolerance)
    TextView tv_pulse_rate_tolerance;
    @BindView(R.id.sp_pulse_rate_tolerance)
    Spinner sp_pulse_rate_tolerance;

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
            int pulseRate = (et_pulse_rate.getText().toString().isEmpty()) ? 0 : Integer.parseInt(et_pulse_rate.getText().toString());
            int tolerance = sp_pulse_rate_tolerance.getSelectedItemPosition() + 4;
            value = (pulseRate * 100) + tolerance;
            if (getActivity() instanceof ValueDetectionFilterActivity)
                ((ValueDetectionFilterActivity) getActivity()).value = value;
        }
    };

    public PulseRateValuesFragment(int type, int value) {
        this.type = type;
        this.value = value;
    }

    @OnItemSelected(R.id.sp_pulse_rate_tolerance)
    public void onItemSelectedValue(AdapterView<?> adapter, View v, int position, long id) {
        int pulseRate = (et_pulse_rate.getText().toString().isEmpty()) ? 0 : Integer.parseInt(et_pulse_rate.getText().toString());
        int tolerance = sp_pulse_rate_tolerance.getSelectedItemPosition() + 4;
        value = (pulseRate * 100) + tolerance;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pulse_rate_values, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayAdapter<CharSequence> scanRateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.pulseRateTolerance, android.R.layout.simple_spinner_item);
        scanRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_pulse_rate_tolerance.setAdapter(scanRateAdapter);
        switch (type) {
            case ValueCodes.PULSE_RATE_1_CODE:
                tv_pulse_rate.setText(R.string.lb_pr1);
                tv_pulse_rate_tolerance.setText(R.string.lb_pr1_tolerance);
                break;
            case ValueCodes.PULSE_RATE_2_CODE:
                tv_pulse_rate.setText(R.string.lb_pr2);
                tv_pulse_rate_tolerance.setText(R.string.lb_pr2_tolerance);
                break;
            case ValueCodes.PULSE_RATE_3_CODE:
                tv_pulse_rate.setText(R.string.lb_pr3);
                tv_pulse_rate_tolerance.setText(R.string.lb_pr3_tolerance);
                break;
            case ValueCodes.PULSE_RATE_4_CODE:
                tv_pulse_rate.setText(R.string.lb_pr4);
                tv_pulse_rate_tolerance.setText(R.string.lb_pr4_tolerance);
                break;
        }
        setPulseRate(value / 100, value % 100);
        et_pulse_rate.addTextChangedListener(textChangedListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setPulseRate(int pr, int tolerance) {
        et_pulse_rate.setText(String.valueOf(pr));
        sp_pulse_rate_tolerance.setSelection(tolerance - 4);
        value = (pr * 100) + tolerance;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }
}
