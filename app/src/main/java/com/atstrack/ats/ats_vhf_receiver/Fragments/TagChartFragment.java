package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TagAdapter;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnTimeTickListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.Detection;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetail;
import com.atstrack.ats.ats_vhf_receiver.Models.TagDetections;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Services.AudioService;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.CustomXAxisRenderer;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

public class TagChartFragment extends Fragment implements ReceiverCallback {
    @BindView(R.id.tv_tag_view)
    TextView tv_tag_view;
    @BindView(R.id.tv_detections_tag_view)
    TextView tv_detections_tag_view;
    @BindView(R.id.tv_rssi_tag)
    TextView tv_rssi_tag;
    @BindView(R.id.tv_temperature_tag)
    TextView tv_temperature_tag;
    @BindView(R.id.tv_time_since_tag)
    TextView tv_time_since_tag;
    @BindView(R.id.tv_voltage_tag)
    TextView tv_voltage_tag;
    @BindView(R.id.sp_chart_type)
    Spinner sp_chart_type;
    @BindView(R.id.lc_tag)
    LineChart lc_tag;

    private Unbinder unbinder;
    private final String type;
    private final TagAdapter tagAdapter;
    private ArrayList<TagDetail> tags;
    private final int positionTagView;
    private int chartType;
    private ArrayList<Entry> chartEntry;
    private LineData lineData;
    private LineDataSet dataSet;
    private long baseTimeMillis;
    private float minValue;
    private float maxValue;
    private TagsFragment.Coordinates coordinates;
    private final OnTimeTickListener onTimeTickListener = new OnTimeTickListener() {
        @Override
        public void onTick(String tagCode, int currentTimeSince, boolean updateTimeSince) {
            if (isAdded() && getView() != null) {
                if (updateTimeSince) {
                    if (positionTagView != -1) {
                        if (positionTagView < tagAdapter.tags.size()) {
                            TagDetections activeTag = tagAdapter.tags.get(positionTagView);
                            if (activeTag.code.equals(tagCode))
                                tv_time_since_tag.setText("Time Since (secs): " + currentTimeSince);
                        }
                    }
                }
                onTickChartTimer();
            }
        }
    };

    public TagChartFragment(String type, int position, TagAdapter tagAdapter, ArrayList<TagDetail> tags, TagsFragment.Coordinates coordinates) {
        this.type = type;
        this.positionTagView = position;
        this.tagAdapter = tagAdapter;
        this.tags = tags;
        this.coordinates = coordinates;
    }

    @OnItemSelected(R.id.sp_chart_type)
    public void onItemSelectedChartType(Spinner spinner, int position) {
        if (chartType != position + 10) {
            chartType = position + 10;
            setDataEntry();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_chart, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupChartConfiguration();
        setViewTagDetail();
        setDataEntry();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                setDetectionTagsData(packet);
            }
        });
    }

    private void setupChartConfiguration() {
        baseTimeMillis = System.currentTimeMillis();
        chartEntry = new ArrayList<>();
        chartType = ValueCodes.RSSI;

        ArrayAdapter<CharSequence> chartTypeAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.chartTypes, android.R.layout.simple_spinner_item);
        chartTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_chart_type.setAdapter(chartTypeAdapter);

        lc_tag.getDescription().setEnabled(false);
        lc_tag.setTouchEnabled(false);
        lc_tag.setDrawGridBackground(false);
        lc_tag.setClipToPadding(true);
        lc_tag.getLegend().setEnabled(false);

        lc_tag.setExtraTopOffset(15f);      // Colchón superior para el texto de -20
        lc_tag.setExtraBottomOffset(20f);   // Espacio inferior para las fechas de dos líneas
        lc_tag.setExtraLeftOffset(15f);     // Margen izquierdo para los números del RSSI
        lc_tag.setExtraRightOffset(25f);    // Espacio para la última rejilla del osciloscopio

        // --- CONFIGURACIÓN EJE Y (LADO IZQUIERDO) ---
        YAxis leftAxis = lc_tag.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setLabelCount(8, true);
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setGridColor(Color.parseColor("#33000000"));
        leftAxis.enableGridDashedLine(10f, 10f, 0f); // Estilo punteado [4, 4] idéntico a Swift
        lc_tag.getAxisRight().setEnabled(false);

        // --- CONFIGURACIÓN EJE X (TIEMPO FLUIDO) ---
        XAxis xAxis = lc_tag.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridColor(Color.parseColor("#44000000"));
        xAxis.setTextColor(Color.GRAY);

        lc_tag.setXAxisRenderer(new CustomXAxisRenderer(lc_tag.getViewPortHandler(), xAxis, lc_tag.getTransformer(YAxis.AxisDependency.LEFT)));

        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            private final SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long realMillis = baseTimeMillis + ((long) value * 1000L);
                Date date = new Date(realMillis);
                return sdfTime.format(date) + "\n" + sdfDate.format(date);
            }
        });

        dataSet = new LineDataSet(chartEntry, ValueCodes.VALUE);
        dataSet.setColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);

        lineData = new LineData(dataSet);
        lc_tag.setData(lineData);

        lc_tag.setHardwareAccelerationEnabled(true);
        lc_tag.notifyDataSetChanged();
        lc_tag.invalidate();
    }

    private void setDataEntry() {
        minValue = Float.MAX_VALUE;
        maxValue = (float) Integer.MIN_VALUE;
        chartEntry.clear();
        double windowStart = ((System.currentTimeMillis() - baseTimeMillis) / 1000.0) - 56.0 - 10f;
        for (int i = tagAdapter.tags.get(positionTagView).detections.size() - 1; i >= 0; i--) {
            double elapsedSeconds = (tagAdapter.tags.get(positionTagView).detections.get(i).timestamp - baseTimeMillis) / 1000.0;
            if (elapsedSeconds > windowStart) {
                chartEntry.add(0, getEntry(elapsedSeconds, i));
            } else
                return;
        }
        YAxis leftAxis = lc_tag.getAxisLeft();
        leftAxis.setAxisMinimum(minValue);
        leftAxis.setAxisMaximum(maxValue);
        dataSet.notifyDataSetChanged();
        lineData.notifyDataChanged();
    }

    private void setViewTagDetail() {
        tv_tag_view.setText("Tag ID: " + tagAdapter.tags.get(positionTagView).code);
        tv_detections_tag_view.setText("Detections: " + tagAdapter.tags.get(positionTagView).detections.size());
        tv_rssi_tag.setText("RSSI (dBm): " + tagAdapter.tags.get(positionTagView).getLastDetection().rssi);
        tv_time_since_tag.setText("Time Since (secs): " + tagAdapter.tags.get(positionTagView).timeSince);
        tv_temperature_tag.setText("Temperature (C): " + tagAdapter.tags.get(positionTagView).getLastDetection().temperature);
        tv_voltage_tag.setText("Voltage (mV): " + tagAdapter.tags.get(positionTagView).getLastDetection().voltage);
    }

    private void setDetectionTagsData(byte[] data) {
        int position = tagAdapter.getItemCount();
        long currentTimestamp = System.currentTimeMillis();
        String code;

        if (type.equals(ValueCodes.BLUETOOTH_RECEIVER)) {
            tags.add(new TagDetail(data, coordinates.latitude, coordinates.longitude, currentTimestamp));
            code = Converters.getAsciiValue(6, 14, data);
        } else {
            tags.add(new TagDetail(data, String.valueOf(data[data.length - 1]), coordinates.latitude, coordinates.longitude, currentTimestamp));
            code = Converters.getHexValue(data[4]) + Converters.getHexValue(data[5]) + Converters.getHexValue(data[6]) + Converters.getHexValue(data[7]);
        }
        for (int i = 0; i < tagAdapter.getItemCount(); i++) {
            if (tagAdapter.tags.get(i).code.equals(code))
                position = i;
        }
        if (position == tagAdapter.getItemCount()) {
            TagDetections newTag = new TagDetections(code, AudioService.frequencies[position]);
            tagAdapter.tags.add(newTag);
        }
        tagAdapter.tags.get(position).timeTickListener = onTimeTickListener;
        if (type.equals(ValueCodes.BLUETOOTH_RECEIVER))
            tagAdapter.tags.get(position).detections.add(new Detection(data, coordinates.latitude, coordinates.longitude, currentTimestamp));
        else
            tagAdapter.tags.get(position).detections.add(new Detection(data, coordinates.latitude, coordinates.longitude, data[data.length - 1], currentTimestamp));
        tagAdapter.tags.get(position).timeSince = 0;

        if (position == positionTagView) {
            setViewTagDetail();
            double elapsedSeconds = (currentTimestamp - baseTimeMillis) / 1000.0;
            chartEntry.add(getEntry(elapsedSeconds, -1));
            YAxis leftAxis = lc_tag.getAxisLeft();
            leftAxis.setAxisMinimum(minValue);
            leftAxis.setAxisMaximum(maxValue);
            dataSet.notifyDataSetChanged();
            lineData.notifyDataChanged();
        }

        if (tagAdapter.tags.get(position).code.equals(tagAdapter.audioIsolateTag) || tagAdapter.audioIsolateTag.isEmpty()) {
            TagDetections currentTag = tagAdapter.tags.get(position);
            AudioService.emitAudioPulse(currentTag.frequencyTone, Integer.parseInt(currentTag.getLastDetection().rssi), true);
            tagAdapter.beepTag = currentTag.code;
        }
        tagAdapter.notifyDataSetChanged();
    }

    private void onTickChartTimer() {
        double windowStart = ((System.currentTimeMillis() - baseTimeMillis) / 1000.0) - 56.0; // Ventana fija de 66 segundos reteniendo los 10 segundos de espacio a la derecha
        double windowEnd = windowStart + 66.0;

        XAxis xAxis = lc_tag.getXAxis();
        xAxis.setAxisMinimum((float) windowStart);
        xAxis.setAxisMaximum((float) windowEnd);

        float referenceInterval = 22f;
        xAxis.setGranularity(referenceInterval);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(4, true);

        if (positionTagView != -1) {
            float safetyThreshold = (float) windowStart - 10f;
            for (int i = chartEntry.size() - 1; i >= 0; i--) {
                if (chartEntry.get(i).getX() < safetyThreshold)
                    chartEntry.remove(i);
            }
            lc_tag.notifyDataSetChanged();
            lc_tag.invalidate();
        }
    }

    private Entry getEntry(double elapsedSeconds, int position) {
        Detection detection = position == -1 ? tagAdapter.tags.get(positionTagView).getLastDetection() : tagAdapter.tags.get(positionTagView).detections.get(position);
        float value = Float.parseFloat(detection.rssi);
        if (chartType == ValueCodes.VOLTAGE)
            value = Float.parseFloat(detection.voltage);
        else if (chartType == ValueCodes.TEMPERATURE)
            value = Float.parseFloat(detection.temperature);
        if (minValue >= value)
            minValue = value - 10;
        if (maxValue <= value)
            maxValue = value + 10;
        return new Entry((float) elapsedSeconds, value);
    }
}
