package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class ViewDetectionFilter extends DialogFragment {
    public static String TAG = ViewDetectionFilter.class.getSimpleName();

    public ViewDetectionFilter() {}

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment ViewDetectionFilter.
     */
    public static ViewDetectionFilter newInstance(DetectionFilter detectionFilter) {
        ViewDetectionFilter fragment = new ViewDetectionFilter();
        Bundle args = new Bundle();
        args.putParcelable(ValueCodes.DETECTION_TYPE, detectionFilter);
        fragment.setArguments(args);
        return fragment;
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
        getDialog().getWindow().setLayout((width / 16) * 15, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_detection_filter, container, false);
        if (getArguments() != null) {
            TextView filter_type_textView = view.findViewById(R.id.filter_type_textView);
            TextView number_matches_textView = view.findViewById(R.id.number_matches_textView);
            TextView pulse_rate_1_textView = view.findViewById(R.id.pulse_rate_1_textView);
            TextView pulse_rate_2_textView = view.findViewById(R.id.pulse_rate_2_textView);
            TextView period_1_tolerance_textView = view.findViewById(R.id.period_1_tolerance_textView);
            TextView period_2_tolerance_textView = view.findViewById(R.id.period_2_tolerance_textView);
            TextView optional_data_calculation_textView = view.findViewById(R.id.optional_data_calculation_textView);
            DetectionFilter detectionFilter = getArguments().getParcelable(ValueCodes.DETECTION_TYPE);
            if (detectionFilter != null) {
                filter_type_textView.setText(Converters.getDetectionFilter(detectionFilter.detectionType));
                number_matches_textView.setText(String.valueOf(detectionFilter.matches));
                pulse_rate_1_textView.setText(String.valueOf(detectionFilter.pulseRate1));
                period_1_tolerance_textView.setText(String.valueOf(detectionFilter.pulseRateTolerance1));

                if (detectionFilter.detectionType == DetectionFilter.FIXED) {
                    pulse_rate_2_textView.setText(String.valueOf(detectionFilter.pulseRate2));
                    period_2_tolerance_textView.setText(String.valueOf(detectionFilter.pulseRateTolerance2));
                    TextView calculation_description_textView = view.findViewById(R.id.calculation_description_textView);
                    calculation_description_textView.setVisibility(View.GONE);
                    optional_data_calculation_textView.setVisibility(View.GONE);
                } else { // Variable Pulse Rate
                    TextView pr_description_textView = view.findViewById(R.id.pr_description_textView);
                    TextView pr_tolerance_description_textView = view.findViewById(R.id.pr_tolerance_description_textView);
                    TextView pr2_description_textView = view.findViewById(R.id.pr2_description_textView);
                    TextView pr2_tolerance_description_textView = view.findViewById(R.id.pr2_tolerance_description_textView);
                    pr2_tolerance_description_textView.setVisibility(View.GONE);
                    pr2_description_textView.setVisibility(View.GONE);
                    pulse_rate_2_textView.setVisibility(View.GONE);
                    period_2_tolerance_textView.setVisibility(View.GONE);
                    pr_description_textView.setText(R.string.lb_max_pulse_rate);
                    pr_tolerance_description_textView.setText(R.string.lb_min_pulse_rate);
                    optional_data_calculation_textView.setText(detectionFilter.optionalData == DetectionFilter.VARIABLE_TEMPERATURE ? "Yes" : "No");
                }
                ImageButton close = view.findViewById(R.id.close_imageButton);
                close.setOnClickListener(view1 -> dismiss());
            }
        }
        return view;
    }
}