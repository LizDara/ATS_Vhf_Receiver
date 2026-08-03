package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.StationaryDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentStationaryScanningBinding;

public class StationaryScanningFragment extends ScanBaseFragment implements ReceiverCallback {
    private StationaryDefaults stationaryDefaults;
    private byte[] scanState;

    public StationaryScanningFragment(int baseFrequency, StationaryDefaults stationaryDefaults) {
        this.baseFrequency = baseFrequency;
        this.stationaryDefaults = stationaryDefaults;
        isScanning = false;
    }

    public StationaryScanningFragment(int baseFrequency, byte[] data) {
        this.baseFrequency = baseFrequency;
        this.scanState = data;
        isScanning = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        scanType = ValueCodes.STATIONARY_SCAN_COMMAND;
        binding = FragmentStationaryScanningBinding.inflate(inflater, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((FragmentStationaryScanningBinding) binding).tvViewDetectionStationary.setOnClickListener(v -> showDetectionAlertDialog());
        if (isScanning) {
            setNotificationLogScanning();
            initializeScanning();
        } else {
            setNotificationLog();
            setStartScan();
        }
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                if (packet[0] == ValueCodes.FATAL_SCAN_ERROR_COMMAND) {
                    if (!errorScan)
                        showAlertDialog();
                } else {
                    setCurrentLog(packet);
                }
            }
        });
    }

    @Override
    protected void updateVisibility(TextView tv_code_period, TextView tv_mortality_pulse_rate) {
        super.updateVisibility(tv_code_period, tv_mortality_pulse_rate);
        ((FragmentStationaryScanningBinding) binding).tvViewDetectionStationary.setVisibility(detectionType == ValueCodes.CODED ? View.GONE : View.VISIBLE);
    }

    private void setStartScan() {
        byte[] b = Converters.setCalendar(10);
        b[0] = ValueCodes.STATIONARY_SCAN_COMMAND;
        b[7] = (byte) stationaryDefaults.firstTableNumber;
        b[8] = (byte) stationaryDefaults.secondTableNumber;
        b[9] = (byte) stationaryDefaults.thirdTableNumber;
        isScanning = TransferBleData.writeStartScan(ValueCodes.STATIONARY_SCAN_COMMAND, b);
    }

    private void initializeScanning() {
        currentFrequency = (Byte.toUnsignedInt(scanState[16]) * 256)
                + Byte.toUnsignedInt(scanState[17]) + baseFrequency;
        currentIndex = (Byte.toUnsignedInt(scanState[7]) * 256)
                + Byte.toUnsignedInt(scanState[8]);
        int currentAntenna = Byte.toUnsignedInt(scanState[9]);
        ((FragmentStationaryScanningBinding) binding).tvFrequencyStationary.setText(Converters.getFrequency(currentFrequency));
        ((FragmentStationaryScanningBinding) binding).tvIndexStationary.setText(String.valueOf(currentIndex));
        ((FragmentStationaryScanningBinding) binding).tvCurrentAntennaStationary.setText((currentAntenna == 0) ? "All" : String.valueOf(currentAntenna));
        scanState(scanState);
    }

    private void setCurrentLog(byte[] data) {
        switch (data[0]) {
            case ValueCodes.SCAN_STATE_COMMAND:
                scanState(data);
                break;
            case ValueCodes.SCAN_HEADER_COMMAND:
                logScanHeader(data);
                break;
            case ValueCodes.SCAN_FIX_CODED_COMMAND:
            case ValueCodes.SCAN_FIX_CONSOLIDATED_CODED_COMMAND:
                logScanCoded(data);
                break;
            case ValueCodes.SCAN_DATA_FIXED_NON_CODED_COMMAND:
            case ValueCodes.SCAN_FIXED_CONSOLIDATED_NON_CODED_COMMAND:
            case ValueCodes.SCAN_DATA_VARIABLE_NON_CODED_COMMAND:
            case ValueCodes.SCAN_VARIABLE_CONSOLIDATED_NON_CODED_COMMAND:
                int signalStrength = Byte.toUnsignedInt(data[4]);
                int period = (Byte.toUnsignedInt(data[5]) * 256) + Byte.toUnsignedInt(data[6]);
                if (detectionType == ValueCodes.FIXED)
                    logScanNonCodedFixed(data[0], period, signalStrength);
                else if (detectionType == ValueCodes.VARIABLE) {
                    if (period > 0)
                        scanNonCodedVariable(period, signalStrength);
                }
                break;
        }
    }

    private void scanState(byte[] data) {
        totalFrequencies = (Byte.toUnsignedInt(data[5]) * 256) + Byte.toUnsignedInt(data[6]);
        ((FragmentStationaryScanningBinding) binding).tvMaxIndexStationary.setText("Table Index (" + totalFrequencies + " Total)");
        detectionType = data[18];
        scanDetailAdapter = new ScanDetailAdapter(requireContext(), detectionType == ValueCodes.CODED);
        ((FragmentStationaryScanningBinding) binding).includeScanDetails.includeRecyclerView.rvItem.setAdapter(scanDetailAdapter);
        ((FragmentStationaryScanningBinding) binding).includeScanDetails.includeRecyclerView.rvItem.setLayoutManager(new LinearLayoutManager(requireContext()));
        updateVisibility(((FragmentStationaryScanningBinding) binding).includeScanDetails.tvCodePeriod, ((FragmentStationaryScanningBinding) binding).includeScanDetails.tvMortalityPulseRate);
        if (detectionType != ValueCodes.CODED)
            initializeDetectionFilter(data);
    }

    /**
     * With the received packet, processes the data of scan header to display.
     * @param data The received packet.
     */
    private void logScanHeader(byte[] data) {
        clear();
        currentFrequency = ((Byte.toUnsignedInt(data[1]) & 63) * 256) +
                Byte.toUnsignedInt(data[2]) + baseFrequency;
        currentIndex = (((Byte.toUnsignedInt(data[1]) >> 6) & 1) * 256) + Byte.toUnsignedInt(data[3]);
        int antennas = Byte.toUnsignedInt(data[1]) >> 7;
        if (antennas == 0) {
            antennas = (Byte.toUnsignedInt(data[7]) >> 6) + 1;
            ((FragmentStationaryScanningBinding) binding).tvCurrentAntennaStationary.setText(String.valueOf(antennas));
        } else {
            ((FragmentStationaryScanningBinding) binding).tvCurrentAntennaStationary.setText(R.string.lbl_vhf_manual_option_all);
        }
        ((FragmentStationaryScanningBinding) binding).tvIndexStationary.setText(String.valueOf(currentIndex));
        ((FragmentStationaryScanningBinding) binding).tvFrequencyStationary.setText(Converters.getFrequency(currentFrequency));
    }

    private void logScanCoded(byte[] data) {
        int code = Byte.toUnsignedInt(data[3]);
        int signalStrength = Byte.toUnsignedInt(data[4]);
        int mortality = Byte.toUnsignedInt(data[5]);
        if (data[0] == ValueCodes.SCAN_FIX_CONSOLIDATED_CODED_COMMAND)
            mortality = (Byte.toUnsignedInt(data[6]) << 8) | Byte.toUnsignedInt(data[5]);
        scanCoded(code, signalStrength, mortality);
    }

    private void logScanNonCodedFixed(byte format, int period, int signalStrength) {
        int type = Integer.parseInt(Converters.getHexValue(format).replace("E", ""));
        if (period > 0)
            scanNonCodedFixed(period, signalStrength, type);
    }

    @Override
    protected void clear() {
        ((FragmentStationaryScanningBinding) binding).tvFrequencyStationary.setText("");
        ((FragmentStationaryScanningBinding) binding).tvIndexStationary.setText("");
        super.clear();
    }
}
