package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Objects;

import butterknife.BindView;

public class ScanBaseActivity extends BaseActivity {
    @BindView(R.id.tv_title_toolbar)
    TextView tv_title_toolbar;
    @BindView(R.id.v_state)
    View v_state;

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
        int titleId = isScanning ? R.string.lb_manual_scanning : R.string.manual_scanning;
        if (scanType == ValueCodes.MOBILE_SCAN_COMMAND)
            titleId = isScanning ? R.string.lb_aerial_scanning : R.string.aerial_scanning;
        else if (scanType == ValueCodes.STATIONARY_SCAN_COMMAND)
            titleId = isScanning ? R.string.lb_stationary_scanning : R.string.stationary_scanning;
        if (isScanning) {
            tv_title_toolbar.setText(titleId);
            Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close);
            v_state.setBackgroundResource(R.drawable.scanning_animation);
            animationDrawable = (AnimationDrawable) v_state.getBackground();
            animationDrawable.start();
        } else {
            tv_title_toolbar.setText(titleId);
            Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
            v_state.setBackgroundColor(ContextCompat.getColor(this, R.color.mountain_meadow));
        }
    }
}