package com.atstrack.ats.ats_vhf_receiver.VHF;

import static com.atstrack.ats.ats_vhf_receiver.R.color.ebony_clay;
import static com.atstrack.ats.ats_vhf_receiver.R.color.light_gray;
import static com.atstrack.ats.ats_vhf_receiver.R.style.body_regular;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ViewDetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.Calendar;
import java.util.TimeZone;

import butterknife.BindView;

public class ScanBaseActivity extends BaseActivity {
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.scan_details_linearLayout)
    LinearLayout scan_details_linearLayout;
    @BindView(R.id.code_textView)
    TextView code_textView;
    @BindView(R.id.mortality_textView)
    TextView mortality_textView;
    @BindView(R.id.period_textView)
    TextView period_textView;
    @BindView(R.id.pulse_rate_textView)
    TextView pulse_rate_textView;
    @BindView(R.id.line_view)
    View line_view;

    protected final String TAG = ScanBaseActivity.class.getSimpleName();
    protected AnimationDrawable animationDrawable;
    protected boolean isScanning;
    protected int baseFrequency;
    protected int range;
    protected byte detectionType;
    protected DialogFragment viewDetectionFilter;

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
        code_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        mortality_textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        period_textView.setVisibility(visibility);
        pulse_rate_textView.setVisibility(visibility);
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
        int position = getPositionNumber(code, 0);
        if (position > 0) {
            refreshCodedPosition(position, signalStrength, mortality > 0);
        } else if (position < 0) {
            createDetail();
            addNewCodedDetailInPosition(-position, code, signalStrength, mortality > 0);
        } else {
            createDetail();
            addNewCodedDetail(scan_details_linearLayout.getChildCount() - 2, code, signalStrength, mortality > 0);
        }
    }

    protected void scanNonCodedFixed(int period, int signalStrength, int type) {
        int pulseRate = 60000 / period;
        int position = getPositionNumber(type, 4);
        if (position > 0) {
            refreshNonCodedPosition(position, signalStrength, period, pulseRate);
        } else if (position < 0) {
            createDetail();
            addNewNonCodedDetailInPosition(-position, pulseRate, signalStrength, period, type);
        } else {
            createDetail();
            addNewNonCodedFixedDetail(scan_details_linearLayout.getChildCount() - 2, pulseRate, signalStrength, period, type);
        }
    }

    protected void scanNonCodedVariable(int period, int signalStrength) {
        int pulseRate = 60000 / period;
        createDetail();
        refreshNonCodedVariable(period, pulseRate, signalStrength);
    }

    private int getPositionNumber(int number, int position) {
        for (int i = 2; i < scan_details_linearLayout.getChildCount() - 1; i += 2) {
            LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            TextView numberTextView = (TextView) linearLayout.getChildAt(position);

            if (Integer.parseInt(numberTextView.getText().toString()) == number)
                return i;
            else if (number < Integer.parseInt(numberTextView.getText().toString()))
                return -i;
        }
        return 0;
    }

    protected void clear() {
        int count = scan_details_linearLayout.getChildCount();
        while (count > 2) {
            scan_details_linearLayout.removeViewAt(2);
            count--;
        }
    }

    private void refreshCodedPosition(int position, int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        int detections = Integer.parseInt(detectionsTextView.getText().toString()) + 1;
        if (detections > 1000) detections = 1;
        int mort = isMort ? Integer.parseInt(mortTextView.getText().toString()) + 1 : Integer.parseInt(mortTextView.getText().toString());
        detectionsTextView.setText(String.valueOf(detections));
        mortalityTextView.setText(isMort ? "M" : "-");
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        mortTextView.setText(String.valueOf(mort));
    }

    private void createDetail() {
        LinearLayout detail = new LinearLayout(this);
        detail.setOrientation(LinearLayout.HORIZONTAL);
        detail.setPadding(0, 8, 0, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.weight = 1;

        TextView extraTextView = new TextView(this);
        extraTextView.setVisibility(View.GONE);

        detail.addView(createTextView(params));
        detail.addView(createTextView(params)); //detections
        detail.addView(createTextView(params));
        detail.addView(createTextView(params)); //signalStrength
        detail.addView(extraTextView);

        LinearLayout line = new LinearLayout(this);
        line.setBackgroundColor(ContextCompat.getColor(this, light_gray));
        line.setLayoutParams(line_view.getLayoutParams());

        scan_details_linearLayout.addView(detail);
        scan_details_linearLayout.addView(line);
    }

    private void addNewCodedDetailInPosition(int position, int code, int signalStrength, boolean isMort) {
        for (int i = scan_details_linearLayout.getChildCount() - 2; i > position ; i -= 2) {
            LinearLayout lastLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            LinearLayout penultimateLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i - 2);

            TextView lastCodeTextView = (TextView) lastLinearLayout.getChildAt(0);
            TextView penultimateCodeTextView = (TextView) penultimateLinearLayout.getChildAt(0);
            lastCodeTextView.setText(penultimateCodeTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastMortalityTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimateMortalityTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastMortalityTextView.setText(penultimateMortalityTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());

            TextView lastMortTextView = (TextView) lastLinearLayout.getChildAt(4);
            TextView penultimateMortTextView = (TextView) penultimateLinearLayout.getChildAt(4);
            lastMortTextView.setText(penultimateMortTextView.getText());
        }
        addNewCodedDetail(position, code, signalStrength, isMort);
    }

    private void addNewCodedDetail(int position, int code, int signalStrength, boolean isMort) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView codeTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView mortalityTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView mortTextView = (TextView) linearLayout.getChildAt(4);

        codeTextView.setText(String.valueOf(code));
        detectionsTextView.setText(String.valueOf(1));
        mortalityTextView.setText(isMort ? "M" : "-");
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        mortTextView.setText(isMort ? "1" : "0");
    }

    protected void refreshNonCodedPosition(int position, int signalStrength, int period, int pulseRate) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);

        int detections = Integer.parseInt(detectionsTextView.getText().toString()) + 1;
        if (detections > 1000) detections = 1;
        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText(String.valueOf(detections));
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
    }

    protected void addNewNonCodedDetailInPosition(int position, int pulseRate, int signalStrength, int period, int type) {
        for (int i = scan_details_linearLayout.getChildCount() - 2; i > position ; i -= 2) {
            LinearLayout lastLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            LinearLayout penultimateLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i - 2);

            TextView lastPeriodTextView = (TextView) lastLinearLayout.getChildAt(0);
            TextView penultimatePeriodTextView = (TextView) penultimateLinearLayout.getChildAt(0);
            lastPeriodTextView.setText(penultimatePeriodTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastPulseRateTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimatePulseRateTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastPulseRateTextView.setText(penultimatePulseRateTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());

            TextView lastTypeTextView = (TextView) lastLinearLayout.getChildAt(4);
            TextView penultimateTypeTextView = (TextView) penultimateLinearLayout.getChildAt(4);
            lastTypeTextView.setText(penultimateTypeTextView.getText());
        }
        addNewNonCodedFixedDetail(position, pulseRate, signalStrength, period, type);
    }

    private void addNewNonCodedFixedDetail(int position, int pulseRate, int signalStrength, int period, int type) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(position);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);
        TextView typeTextView = (TextView) linearLayout.getChildAt(4);

        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText(String.valueOf(1));
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
        typeTextView.setText(String.valueOf(type));
    }

    protected void refreshNonCodedVariable(int period, int pulseRate, int signalStrength) {
        for (int i = scan_details_linearLayout.getChildCount() - 2; i > 3 ; i -= 2) {
            LinearLayout lastLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i);
            LinearLayout penultimateLinearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(i - 2);

            TextView lastPeriodTextView = (TextView) lastLinearLayout.getChildAt(0);
            TextView penultimatePeriodTextView = (TextView) penultimateLinearLayout.getChildAt(0);
            lastPeriodTextView.setText(penultimatePeriodTextView.getText());

            TextView lastDetectionsTextView = (TextView) lastLinearLayout.getChildAt(1);
            TextView penultimateDetectionsTextView = (TextView) penultimateLinearLayout.getChildAt(1);
            lastDetectionsTextView.setText(penultimateDetectionsTextView.getText());

            TextView lastPulseRateTextView = (TextView) lastLinearLayout.getChildAt(2);
            TextView penultimatePulseRateTextView = (TextView) penultimateLinearLayout.getChildAt(2);
            lastPulseRateTextView.setText(penultimatePulseRateTextView.getText());

            TextView lastSignalStrengthTextView = (TextView) lastLinearLayout.getChildAt(3);
            TextView penultimateSignalStrengthTextView = (TextView) penultimateLinearLayout.getChildAt(3);
            lastSignalStrengthTextView.setText(penultimateSignalStrengthTextView.getText());
        }
        addNewNonCodedVariableDetail(pulseRate, signalStrength, period);
    }

    private void addNewNonCodedVariableDetail(int pulseRate, int signalStrength, int period) {
        LinearLayout linearLayout = (LinearLayout) scan_details_linearLayout.getChildAt(2);
        TextView periodTextView = (TextView) linearLayout.getChildAt(0);
        TextView detectionsTextView = (TextView) linearLayout.getChildAt(1);
        TextView pulseRateTextView = (TextView) linearLayout.getChildAt(2);
        TextView signalStrengthTextView = (TextView) linearLayout.getChildAt(3);

        periodTextView.setText(String.valueOf(period));
        detectionsTextView.setText("-");
        pulseRateTextView.setText(String.valueOf(pulseRate));
        signalStrengthTextView.setText(String.valueOf(signalStrength));
    }

    private TextView createTextView(TableRow.LayoutParams params) {
        TextView textView = new TextView(this);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setTextAppearance(body_regular);
        textView.setTextColor(ContextCompat.getColor(this, ebony_clay));
        textView.setLayoutParams(params);
        return textView;
    }
}