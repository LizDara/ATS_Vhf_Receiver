package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class DetectionFilter extends DialogFragment {
    public static String TAG = DetectionFilter.class.getSimpleName();

    public DetectionFilter() {}

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetectionFilter.
     */
    public static DetectionFilter newInstance() {
        return new DetectionFilter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detection_filter, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.detection_filter_radioGroup);
        Button button = view.findViewById(R.id.continue_button);
        button.setOnClickListener(v -> {
            int idRadioButton = radioGroup.getCheckedRadioButtonId();
            RadioButton radioButton = view.findViewById(idRadioButton);
            byte detectionType = (byte) 0x09;
            if (radioButton.getText().toString().contains("Fixed"))
                detectionType = (byte) 0x08;
            else if (radioButton.getText().toString().contains("Variable"))
                detectionType = (byte) 0x07;

            Bundle bundle = new Bundle();
            bundle.putByte(ValueCodes.VALUE, detectionType);
            getParentFragmentManager().setFragmentResult(ValueCodes.VALUE, bundle);
            dismiss();
        });
        return view;
    }
}