package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfFragmentBinding;

import java.util.Objects;

public class ScanBaseActivity extends BaseActivity {
    protected final String TAG = ScanBaseActivity.class.getSimpleName();
    protected AnimationDrawable animationDrawable;
    protected boolean isScanning;
    protected byte scanType;
    protected int baseFrequency;
    protected int range;
    protected byte detectionType;
    protected boolean errorScan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        binding = ActivityVhfFragmentBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        isScanning = getIntent().getBooleanExtra(ValueCodes.IS_SCANNING, false);
        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
        errorScan = false;
    }

    @Override
    protected void gattDisconnected() {
        unbindService(leServiceConnection.getServiceConnection());
        super.gattDisconnected();
    }

    public void setScanViews(boolean isScanning) {
        this.isScanning = isScanning;
        int titleId = isScanning ? R.string.title_vhf_manual_scanning : R.string.title_vhf_manual_settings;
        if (scanType == ValueCodes.MOBILE_SCAN_COMMAND)
            titleId = isScanning ? R.string.title_vhf_mobile_scanning : R.string.title_vhf_mobile_settings;
        else if (scanType == ValueCodes.STATIONARY_SCAN_COMMAND)
            titleId = isScanning ? R.string.title_vhf_stationary_scanning : R.string.title_vhf_stationary_settings;
        if (isScanning) {
            ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(titleId);
            Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close);
            ((ActivityVhfFragmentBinding) binding).includeToolbar.vState.setBackgroundResource(R.drawable.scanning_animation);
            animationDrawable = (AnimationDrawable) ((ActivityVhfFragmentBinding) binding).includeToolbar.vState.getBackground();
            animationDrawable.start();
        } else {
            ((ActivityVhfFragmentBinding) binding).includeToolbar.tvTitleToolbar.setText(titleId);
            Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
            ((ActivityVhfFragmentBinding) binding).includeToolbar.vState.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
        }
    }
}