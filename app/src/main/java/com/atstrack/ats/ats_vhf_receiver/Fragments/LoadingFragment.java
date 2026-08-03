package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.DialogAlertLoadingBinding;

public class LoadingFragment extends Fragment implements ReceiverCallback {
    private DialogAlertLoadingBinding binding = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAlertLoadingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvStateLoading.setText(R.string.lbl_vhf_data_status_deleting);
        setResponseErase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TransferBleData.downloadResponse(false);
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
                if (packet[0] == ValueCodes.STORAGE_RESPONSE_COMMAND) { // Get delete or download response
                    if (isTransmissionDone(packet)) {
                        if (getParentFragmentManager() != null) {
                            getParentFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                    .hide(this)
                                    .add(R.id.fcv_activity_fragment, new SuccessfulMessageFragment(ValueCodes.DELETE))
                                    .addToBackStack(String.valueOf(ValueCodes.THIRD_STEP))
                                    .commit();
                        }
                    }
                }
            }
        });
    }

    private void setResponseErase() {
        TransferBleData.downloadResponse(true);
        try {
            Thread.sleep(ValueCodes.WAITING_PERIOD);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        setDeleteData();
    }

    private void setDeleteData() {
        byte[] b = new byte[]{(byte) 0x93};
        TransferBleData.writeResponse(b);
    }

    private boolean isTransmissionDone(byte[] packet) {
        return Converters.getHexValue(packet).equals("DD 00 BB EE ");
    }
}
