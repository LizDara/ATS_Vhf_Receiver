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
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Models.ScanDetail;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Calendar;
import java.util.TimeZone;

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
        TransferBleData.notificationLog();
        try {
            Thread.sleep(ValueCodes.WAITING_PERIOD);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] setCalendar() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int YY = currentDate.get(Calendar.YEAR);
        int MM = currentDate.get(Calendar.MONTH);
        int DD = currentDate.get(Calendar.DAY_OF_MONTH);
        int hh = currentDate.get(Calendar.HOUR_OF_DAY);
        int mm =  currentDate.get(Calendar.MINUTE);
        int ss = currentDate.get(Calendar.SECOND);

        return new byte[] {0, (byte) (YY % 100), (byte) (MM + 1), (byte) DD, (byte) hh, (byte) mm, (byte) ss, 0, 0, 0};
    }

    protected void setNotificationLogScanning() {
        parameter = ValueCodes.START_LOG;
        TransferBleData.notificationLog();
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

    protected void updateVisibility(int visibility) {
        code_period_textView.setText(Converters.getHexValue(detectionType).equals("09") ? R.string.lb_code : R.string.lb_period);
        mortality_pulse_rate_textView.setText(Converters.getHexValue(detectionType).equals("09") ? R.string.lb_mortality : R.string.lb_pulse_rate);
    }

    protected void initializeDetectionFilter(byte[] data) {
        String detection = Converters.getHexValue(detectionType).equals("08") ? "Fixed Pulse Rate" : "Variable Pulse Rate";
        String dataCalculation = "";
        switch (Converters.getHexValue(detectionType)) {
            case "06":
                dataCalculation = "Yes";
                break;
            case "07":
                dataCalculation = "None";
                break;
        }
        String matches = Converters.getDecimalValue(data[19]);
        String pr1 = Converters.getDecimalValue(data[20]);
        String pr1Tolerance = Converters.getDecimalValue(data[21]);
        String pr2 = Converters.getDecimalValue(data[22]);
        String pr2Tolerance = Converters.getDecimalValue(data[23]);
        viewDetectionFilter = ViewDetectionFilter.newInstance(detection, pr1, pr1Tolerance, pr2, pr2Tolerance, dataCalculation, matches);
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
            int currentNumber = Converters.getHexValue(detectionType).equals("09") ? scanDetailListAdapter.getDetail(i).code : scanDetailListAdapter.getDetail(i).type;
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