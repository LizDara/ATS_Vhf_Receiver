package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.atstrack.ats.ats_vhf_receiver.Adapters.ScanDetailAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.ScanDetail;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.ScanBaseActivity;

public class ScanBaseFragment extends Fragment {
    protected ViewBinding binding = null;
    protected int scanType;
    protected boolean isScanning;
    protected int baseFrequency;
    protected int range;
    protected byte detectionType;
    protected boolean errorScan;
    protected int totalFrequencies;
    protected int currentIndex;
    protected int currentFrequency;
    protected int frequencyRange;
    protected ScanDetailAdapter scanDetailAdapter;
    private DetectionFilter detectionFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        frequencyRange = ((range + (baseFrequency / 1000)) * 1000) - 1;
        initialize();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected void initialize() {
        if (getActivity() instanceof ScanBaseActivity) {
            ((ScanBaseActivity) getActivity()).setScanViews(true);
        }
    }

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

    protected void updateVisibility(TextView tv_code_period, TextView tv_mortality_pulse_rate) {
        tv_code_period.setText(detectionType == ValueCodes.CODED ? R.string.lbl_vhf_manual_code : R.string.lbl_vhf_manual_period);
        tv_mortality_pulse_rate.setText(detectionType == ValueCodes.CODED ? R.string.lbl_vhf_manual_mortality : R.string.lbl_vhf_manual_pulse_rate);
    }

    protected void initializeDetectionFilter(byte[] data) {
        detectionFilter = new DetectionFilter();
        detectionFilter.detectionType = detectionType;
        detectionFilter.optionalData = detectionType != ValueCodes.FIXED ? detectionType : 0;
        detectionFilter.matches = Byte.toUnsignedInt(data[19]);
        detectionFilter.pulseRate1 = Byte.toUnsignedInt(data[20]);
        detectionFilter.pulseRateTolerance1 = Byte.toUnsignedInt(data[21]);
        detectionFilter.pulseRate2 = Byte.toUnsignedInt(data[22]);
        detectionFilter.pulseRateTolerance2 = Byte.toUnsignedInt(data[23]);
    }

    protected void scanCoded(int code, int signalStrength, int mortality) {
        int position = getPositionNumber(code);
        if (position > 0) {
            int detection = scanDetailAdapter.getDetail(position - 1).detection;
            scanDetailAdapter.setDetail(position - 1, new ScanDetail(code, detection + 1 > 1000 ? 1 : detection + 1, mortality > 0, signalStrength));
        } else if (position < 0) {
            scanDetailAdapter.addDetailInPosition(-position - 1, new ScanDetail(code, 1, mortality > 0, signalStrength));
        } else {
            scanDetailAdapter.addDetail(new ScanDetail(code, 1, mortality > 0, signalStrength));
        }
        scanDetailAdapter.notifyDataSetChanged();
    }

    protected void scanNonCodedFixed(int period, int signalStrength, int type) {
        int pulseRate = 60000 / period;
        int position = getPositionNumber(type);
        if (position > 0) {
            int detection = scanDetailAdapter.getDetail(position - 1).detection;
            scanDetailAdapter.setDetail(position - 1, new ScanDetail(period, detection + 1, pulseRate, signalStrength, type));
        } else if (position < 0) {
            scanDetailAdapter.addDetailInPosition(-position - 1, new ScanDetail(period, 1, pulseRate, signalStrength, type));
        } else {
            scanDetailAdapter.addDetail(new ScanDetail(period, 1, pulseRate, signalStrength, type));
        }
        scanDetailAdapter.notifyDataSetChanged();
    }

    protected void scanNonCodedVariable(int period, int signalStrength) {
        int pulseRate = 60000 / period;
        scanDetailAdapter.addDetail(new ScanDetail(period, 1, pulseRate, signalStrength, -1));
        scanDetailAdapter.notifyDataSetChanged();
    }

    private int getPositionNumber(int number) {
        for (int i = 0; i < scanDetailAdapter.getItemCount(); i++) {
            int currentNumber = detectionType == ValueCodes.CODED ? scanDetailAdapter.getDetail(i).code : scanDetailAdapter.getDetail(i).type;
            if (number == currentNumber)
                return i + 1;
            else if (number < currentNumber)
                return -(i + 1);
        }
        return 0;
    }

    protected void clear() {
        scanDetailAdapter.removeAll();
        scanDetailAdapter.notifyDataSetChanged();
    }

    protected void showAlertDialog() {
        errorScan = true;
        AlertDialog dialog = Dialogs.createErrorDialog(requireContext(), getString(R.string.lbl_vhf_mobile_fatal_scan_error), getString(R.string.lbl_vhf_mobile_receiver_repair));
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && getView() != null) {
                if (dialog.isShowing())
                    dialog.dismiss();
                LeServiceConnection.getInstance().getBluetoothLeService().disconnect();
            }
        }, ValueCodes.MESSAGE_PERIOD * 14);
    }

    protected void showDetectionAlertDialog() {
        AlertDialog dialog = Dialogs.createDetectionFilterDialog(requireContext(), detectionFilter);
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
        dialog.show();
    }
}
