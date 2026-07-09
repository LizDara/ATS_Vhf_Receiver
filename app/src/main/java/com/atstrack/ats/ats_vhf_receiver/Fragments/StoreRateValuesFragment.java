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
import com.atstrack.ats.ats_vhf_receiver.VHF.ValueDefaultsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class StoreRateValuesFragment extends Fragment {
    @BindView(R.id.tv_continuous_store)
    TextView tv_continuous_store;
    @BindView(R.id.tv_five_minutes)
    TextView tv_five_minutes;
    @BindView(R.id.tv_ten_minutes)
    TextView tv_ten_minutes;
    @BindView(R.id.tv_fifteen_minutes)
    TextView tv_fifteen_minutes;
    @BindView(R.id.tv_thirty_minutes)
    TextView tv_thirty_minutes;
    @BindView(R.id.tv_sixty_minutes)
    TextView tv_sixty_minutes;
    @BindView(R.id.tv_one_hundred_twenty_minutes)
    TextView tv_one_hundred_twenty_minutes;

    private Unbinder unbinder;
    private int storeRate;

    public StoreRateValuesFragment(int storeRate) {
        this.storeRate = storeRate;
    }

    @OnClick(R.id.tv_continuous_store)
    public void onClickContinuousStore(View v) {
        setStoreRate(0);
    }

    @OnClick(R.id.tv_five_minutes)
    public void onClickFiveMinutes(View v) {
        setStoreRate(5);
    }

    @OnClick(R.id.tv_ten_minutes)
    public void onClickTenMinutes(View v) {
        setStoreRate(10);
    }

    @OnClick(R.id.tv_fifteen_minutes)
    public void onClickFifteenMinutes(View v) {
        setStoreRate(15);
    }

    @OnClick(R.id.tv_thirty_minutes)
    public void onClickThirtyMinutes(View v) {
        setStoreRate(30);
    }

    @OnClick(R.id.tv_sixty_minutes)
    public void onClickSixtyMinutes(View v) {
        setStoreRate(60);
    }

    @OnClick(R.id.tv_one_hundred_twenty_minutes)
    public void onClickOneHundredTwentyMinutes(View v) {
        setStoreRate(120);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_rate_values, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setStoreRate(storeRate);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setStoreRate(int storeRate) {
        for (int i = 0; i < 2; i ++) {
            switch (this.storeRate) {
                case 0:
                    tv_continuous_store.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 5:
                    tv_five_minutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 10:
                    tv_ten_minutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 15:
                    tv_fifteen_minutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 30:
                    tv_thirty_minutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 60:
                    tv_sixty_minutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
                case 120:
                    tv_one_hundred_twenty_minutes.setCompoundDrawablesWithIntrinsicBounds(0, 0, i == 0 ? 0 : R.drawable.ic_check, 0);
                    break;
            }
            this.storeRate = storeRate;
        }
        if (getActivity() instanceof ValueDefaultsActivity)
            ((ValueDefaultsActivity) getActivity()).value = storeRate;
    }
}
