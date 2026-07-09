package com.atstrack.ats.ats_vhf_receiver.DialogsFragment;

import android.content.SharedPreferences;
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

public class DetectionFilterDialogFragment extends DialogFragment {
    public static String TAG = DetectionFilterDialogFragment.class.getSimpleName();

    public DetectionFilterDialogFragment() {}

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetectionFilter.
     */
    public static DetectionFilterDialogFragment newInstance() {
        return new DetectionFilterDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        int width = sharedPreferences.getInt(ValueCodes.WIDTH, 0);
        getDialog().getWindow().setLayout((width / 18) * 17, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_form_detection_filter, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.rg_detection_filter);
        Button button = view.findViewById(R.id.btn_continue);
        button.setOnClickListener(v -> {
            int idRadioButton = radioGroup.getCheckedRadioButtonId();
            RadioButton radioButton = view.findViewById(idRadioButton);
            byte detectionType = ValueCodes.CODED;
            if (radioButton.getText().toString().contains("Fixed"))
                detectionType = ValueCodes.FIXED;
            else if (radioButton.getText().toString().contains("Variable"))
                detectionType = ValueCodes.VARIABLE;

            Bundle bundle = new Bundle();
            bundle.putByte(ValueCodes.VALUE, detectionType);
            getParentFragmentManager().setFragmentResult(ValueCodes.VALUE, bundle);
            dismiss();
        });
        return view;
    }
}