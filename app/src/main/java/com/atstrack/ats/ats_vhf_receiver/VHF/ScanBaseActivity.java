package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Models.ScanDetail;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;

public class ScanBaseActivity extends BaseActivity {
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.item_recyclerView)
    RecyclerView item_recyclerView;
    @BindView(R.id.code_period_textView)
    TextView code_period_textView;
    @BindView(R.id.mortality_pulse_rate_textView)
    TextView mortality_pulse_rate_textView;
    @BindView(R.id.line_view)
    View line_view;

    protected final String TAG = ScanBaseActivity.class.getSimpleName();
    protected AnimationDrawable animationDrawable;
    protected boolean isScanning;
    protected int baseFrequency;
    protected int range;
    protected byte detectionType;
    protected DialogFragment viewDetectionFilter;
    protected ScanDetailListAdapter scanDetailListAdapter;

    protected void setNotificationLog() {
        TransferBleData.notificationLog(true);
        try {
            Thread.sleep(ValueCodes.WAITING_PERIOD);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setNotificationLogScanning() {
        TransferBleData.notificationLog(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        super.onCreate(savedInstanceState);

        isScanning = getIntent().getBooleanExtra(ValueCodes.SCANNING, false);
        SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
        range = sharedPreferences.getInt(ValueCodes.RANGE, 0);
    }

    protected void updateVisibility() {
        code_period_textView.setText(detectionType == DetectionFilter.CODED ? R.string.lb_code : R.string.lb_period);
        mortality_pulse_rate_textView.setText(detectionType == DetectionFilter.CODED ? R.string.lb_mortality : R.string.lb_pulse_rate);
    }

    protected void initializeDetectionFilter(byte[] data) {
        DetectionFilter detectionFilter = new DetectionFilter();
        detectionFilter.detectionType = detectionType;
        detectionFilter.optionalData = detectionType != DetectionFilter.FIXED ? detectionType : 0;
        detectionFilter.matches = Byte.toUnsignedInt(data[19]);
        detectionFilter.pulseRate1 = Byte.toUnsignedInt(data[20]);
        detectionFilter.pulseRateTolerance1 = Byte.toUnsignedInt(data[21]);
        detectionFilter.pulseRate2 = Byte.toUnsignedInt(data[22]);
        detectionFilter.pulseRateTolerance2 = Byte.toUnsignedInt(data[23]);
        viewDetectionFilter = ViewDetectionFilter.newInstance(detectionFilter);
    }

    protected void scanCoded(int code, int signalStrength, int mortality) {
        int position = getPositionNumber(code);
        if (position > 0) {
            int detection = scanDetailListAdapter.getDetail(position - 1).detection;
            scanDetailListAdapter.setDetail(position - 1, new ScanDetail(code, detection + 1 > 1000 ? 1 : detection + 1, mortality > 0, signalStrength));
        } else if (position < 0) {
            scanDetailListAdapter.addDetailInPosition(-position - 1, new ScanDetail(code, 1, mortality > 0, signalStrength));
        } else {
            scanDetailListAdapter.addDetail(new ScanDetail(code, 1, mortality > 0, signalStrength));
        }
        scanDetailListAdapter.notifyDataSetChanged();
    }

    protected void scanNonCodedFixed(int period, int signalStrength, int type) {
        int pulseRate = 60000 / period;
        int position = getPositionNumber(type);
        if (position > 0) {
            int detection = scanDetailListAdapter.getDetail(position - 1).detection;
            scanDetailListAdapter.setDetail(position - 1, new ScanDetail(period, detection + 1, pulseRate, signalStrength, type));
        } else if (position < 0) {
            scanDetailListAdapter.addDetailInPosition(-position - 1, new ScanDetail(period, 1, pulseRate, signalStrength, type));
        } else {
            scanDetailListAdapter.addDetail(new ScanDetail(period, 1, pulseRate, signalStrength, type));
        }
        scanDetailListAdapter.notifyDataSetChanged();
    }

    protected void scanNonCodedVariable(int period, int signalStrength) {
        int pulseRate = 60000 / period;
        scanDetailListAdapter.addDetail(new ScanDetail(period, 1, pulseRate, signalStrength, -1));
        scanDetailListAdapter.notifyDataSetChanged();
    }

    private int getPositionNumber(int number) {
        for (int i = 0; i < scanDetailListAdapter.getItemCount(); i++) {
            int currentNumber = detectionType == DetectionFilter.CODED ? scanDetailListAdapter.getDetail(i).code : scanDetailListAdapter.getDetail(i).type;
            if (number == currentNumber)
                return i + 1;
            else if (number < currentNumber)
                return -(i + 1);
        }
        return 0;
    }

    protected void clear() {
        scanDetailListAdapter.removeAll();
        scanDetailListAdapter.notifyDataSetChanged();
    }
}