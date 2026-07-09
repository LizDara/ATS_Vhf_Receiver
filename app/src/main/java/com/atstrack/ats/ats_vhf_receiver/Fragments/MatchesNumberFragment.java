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
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDetectionFilterActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MatchesNumberFragment extends Fragment {
    @BindView(R.id.tv_two)
    TextView tv_two;
    @BindView(R.id.tv_three)
    TextView tv_three;
    @BindView(R.id.tv_four)
    TextView tv_four;
    @BindView(R.id.tv_five)
    TextView tv_five;
    @BindView(R.id.tv_six)
    TextView tv_six;
    @BindView(R.id.tv_seven)
    TextView tv_seven;
    @BindView(R.id.tv_eight)
    TextView tv_eight;

    private Unbinder unbinder;
    private int matches;

    public MatchesNumberFragment(int value) {
        this.matches = value;
    }

    @OnClick({R.id.tv_two, R.id.tv_three, R.id.tv_four, R.id.tv_five, R.id.tv_six, R.id.tv_seven, R.id.tv_eight})
    public void onClickTwo(View v) {
        TextView text = (TextView) v;
        setMatchesForValidPattern(Integer.parseInt(text.getText().toString()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches_number, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setMatchesForValidPattern(matches);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setMatchesForValidPattern(int matches) {
        for (int i = 0; i < 2; i ++) {
            switch (this.matches) {
                case 2:
                    tv_two.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 3:
                    tv_three.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 4:
                    tv_four.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 5:
                    tv_five.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 6:
                    tv_six.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 7:
                    tv_seven.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 8:
                    tv_eight.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
            }
            this.matches = matches;
        }
        if (getActivity() instanceof ValueDetectionFilterActivity)
            ((ValueDetectionFilterActivity) getActivity()).value = matches;
    }
}
