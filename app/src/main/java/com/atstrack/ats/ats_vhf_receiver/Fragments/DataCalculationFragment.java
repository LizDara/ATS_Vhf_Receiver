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

public class DataCalculationFragment extends Fragment {
    @BindView(R.id.tv_none)
    TextView tv_none;
    @BindView(R.id.tv_temperature)
    TextView tv_temperature;

    private Unbinder unbinder;
    private int value;

    public DataCalculationFragment(int value) {
        this.value = value;
    }

    @OnClick(R.id.tv_none)
    public void onClickNone(View v) {
        setDataCalculation(ValueCodes.NONE);
    }

    @OnClick(R.id.tv_temperature)
    public void onClickTemperature(View v) {
        setDataCalculation(ValueCodes.VARIABLE_TEMPERATURE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_calculation, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDataCalculation(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setDataCalculation(int calculation) {
        tv_temperature.setCompoundDrawablesWithIntrinsicBounds(0, 0, calculation == ValueCodes.VARIABLE_TEMPERATURE ? R.drawable.ic_check : 0, 0);
        tv_none.setCompoundDrawablesWithIntrinsicBounds(0, 0, calculation == ValueCodes.NONE ? R.drawable.ic_check : 0, 0);
        value = calculation;
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = value;
    }
}