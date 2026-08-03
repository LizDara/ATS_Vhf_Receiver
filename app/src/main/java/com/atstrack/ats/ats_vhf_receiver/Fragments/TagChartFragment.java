package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

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
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentTagChartBinding;
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

public class TagChartFragment extends Fragment implements ReceiverCallback {
    private FragmentTagChartBinding binding = null;
    private final String type;
    private final TagAdapter tagAdapter;
    private ArrayList<TagDetail> tags;
    private final int positionTagView;
    private int chartType = ValueCodes.RSSI;
    private ArrayList<Entry> chartEntry;
    private LineData lineData;
    private LineDataSet dataSet;
    private long baseTimeMillis;
    private float minValue;
    private float maxValue;
    private TagsFragment.Coordinates coordinates;
    private int timePosition = 2;
    private final TimeInterval[] times = {new TimeInterval(0, 10, -5), new TimeInterval(1, 30, -20), new TimeInterval(2, 60, -50), new TimeInterval(3, 300, -240)};
    private final OnTimeTickListener onTimeTickListener = new OnTimeTickListener() {
        @Override
        public void onTick(String tagCode, int currentTimeSince, boolean updateTimeSince) {
            if (isAdded() && getView() != null) {
                if (updateTimeSince) {
                    if (positionTagView != -1) {
                        if (positionTagView < tagAdapter.tags.size()) {
                            TagDetections activeTag = tagAdapter.tags.get(positionTagView);
                            if (activeTag.code.equals(tagCode))
                                binding.tvTimeSinceTag.setText("Time Since (secs): " + currentTimeSince);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTagChartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.spChartType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (chartType != i + 10) {
                    chartType = i + 10;
                    setDataEntry();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        binding.spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timePosition = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        setupChartConfiguration();
        setViewTagDetail();
        setDataEntry();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

        ArrayAdapter<CharSequence> chartTypeAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.array_bt_tag_chart_types, android.R.layout.simple_spinner_item);
        chartTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spChartType.setAdapter(chartTypeAdapter);

        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.array_bt_tag_times, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTime.setAdapter(timeAdapter);
        binding.spTime.setSelection(2);

        binding.lcTag.getDescription().setEnabled(false);
        binding.lcTag.setTouchEnabled(false);
        binding.lcTag.setDrawGridBackground(false);
        binding.lcTag.setClipToPadding(true);
        binding.lcTag.getLegend().setEnabled(false);

        binding.lcTag.setExtraTopOffset(15f);      // Colchón superior para el texto de -20
        binding.lcTag.setExtraBottomOffset(20f);   // Espacio inferior para las fechas de dos líneas
        binding.lcTag.setExtraLeftOffset(15f);     // Margen izquierdo para los números del RSSI
        binding.lcTag.setExtraRightOffset(25f);    // Espacio para la última rejilla del osciloscopio

        // --- CONFIGURACIÓN EJE Y (LADO IZQUIERDO) ---
        YAxis leftAxis = binding.lcTag.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setLabelCount(8, true);
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setGridColor(Color.parseColor("#33000000"));
        leftAxis.enableGridDashedLine(10f, 10f, 0f); // Estilo punteado [4, 4] idéntico a Swift
        binding.lcTag.getAxisRight().setEnabled(false);

        // --- CONFIGURACIÓN EJE X (TIEMPO FLUIDO) ---
        XAxis xAxis = binding.lcTag.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridColor(Color.parseColor("#44000000"));
        xAxis.setTextColor(Color.GRAY);

        binding.lcTag.setXAxisRenderer(new CustomXAxisRenderer(binding.lcTag.getViewPortHandler(), xAxis, binding.lcTag.getTransformer(YAxis.AxisDependency.LEFT)));

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
        binding.lcTag.setData(lineData);

        binding.lcTag.setHardwareAccelerationEnabled(true);
        binding.lcTag.notifyDataSetChanged();
        binding.lcTag.invalidate();
    }

    private void setDataEntry() {
        minValue = Float.MAX_VALUE;
        maxValue = (float) Integer.MIN_VALUE;
        chartEntry.clear();
        double windowStart = ((System.currentTimeMillis() - baseTimeMillis) / 1000.0) + times[timePosition].difference - 10f;
        for (int i = tagAdapter.tags.get(positionTagView).detections.size() - 1; i >= 0; i--) {
            double elapsedSeconds = (tagAdapter.tags.get(positionTagView).detections.get(i).timestamp - baseTimeMillis) / 1000.0;
            if (elapsedSeconds > windowStart)
                chartEntry.add(0, getEntry(elapsedSeconds, i));
            else
                return;
        }
        YAxis leftAxis = binding.lcTag.getAxisLeft();
        leftAxis.setAxisMinimum(minValue);
        leftAxis.setAxisMaximum(maxValue);
        dataSet.notifyDataSetChanged();
        lineData.notifyDataChanged();
    }

    private void setViewTagDetail() {
        binding.tvTagView.setText("Tag ID: " + tagAdapter.tags.get(positionTagView).code);
        binding.tvDetectionsTagView.setText("Detections: " + tagAdapter.tags.get(positionTagView).detections.size());
        binding.tvRssiTag.setText("RSSI (dBm): " + tagAdapter.tags.get(positionTagView).getLastDetection().rssi);
        binding.tvTimeSinceTag.setText("Time Since (secs): " + tagAdapter.tags.get(positionTagView).timeSince);
        binding.tvTemperatureTag.setText("Temperature (C): " + tagAdapter.tags.get(positionTagView).getLastDetection().temperature);
        binding.tvVoltageTag.setText("Voltage (mV): " + tagAdapter.tags.get(positionTagView).getLastDetection().voltage);
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
            YAxis leftAxis = binding.lcTag.getAxisLeft();
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
        double windowStart = ((System.currentTimeMillis() - baseTimeMillis) / 1000.0) + times[timePosition].difference; // Ventana fija de 66 segundos reteniendo los 10 segundos de espacio a la derecha
        double windowEnd = windowStart + times[timePosition].seconds;

        XAxis xAxis = binding.lcTag.getXAxis();
        xAxis.setAxisMinimum((float) windowStart);
        xAxis.setAxisMaximum((float) windowEnd);

        float referenceInterval = (float) times[timePosition].seconds / 3;
        xAxis.setGranularity(referenceInterval);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(4, true);

        if (positionTagView != -1) {
            float safetyThreshold = (float) windowStart - 10f;
            for (int i = chartEntry.size() - 1; i >= 0; i--) {
                if (chartEntry.get(i).getX() < safetyThreshold)
                    chartEntry.remove(i);
            }
            binding.lcTag.notifyDataSetChanged();
            binding.lcTag.invalidate();
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

    public static class TimeInterval {
        public int position;
        public int seconds;
        public int difference;

        public TimeInterval(int position, int seconds, int difference) {
            this.position = position;
            this.seconds = seconds;
            this.difference = difference;
        }
    }
}
