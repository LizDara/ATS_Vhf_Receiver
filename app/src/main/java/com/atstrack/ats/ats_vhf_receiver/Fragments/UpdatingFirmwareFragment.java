package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Services.FirmwareServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentUpdatingFirmwareBinding;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class UpdatingFirmwareFragment extends Fragment implements ReceiverCallback {
    private FragmentUpdatingFirmwareBinding binding = null;
    private final String downloadUrl;
    private byte[] firmwareFile;
    private final int MTU = 247;
    private int index;
    private int packetNumber = 1;
    private int parameter;

    public UpdatingFirmwareFragment(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUpdatingFirmwareBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnCancelUpdate.setOnClickListener(v -> {
            if (getParentFragmentManager() != null)
                getParentFragmentManager().popBackStack();
        });
        setVisibility(ValueCodes.PROCESSING);
        downloadFile();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.PROCESSING) {
            binding.includeDownloadingProcess.tvFirstStep.setText(R.string.lbl_fw_update_status_downloading);
            binding.includeDownloadingProcess.tvSecondStep.setText(R.string.lbl_fw_update_status_checking);
            binding.includeDownloadingProcess.tvThirdStep.setText(R.string.lbl_fw_update_status_installing);

            binding.includeDownloadingProcess.tvFirstStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
            binding.includeDownloadingProcess.tvFirstStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
            binding.includeDownloadingProcess.pbFirstStep.setVisibility(View.GONE);
            binding.includeDownloadingProcess.tvSecondStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
            binding.includeDownloadingProcess.tvSecondStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
            binding.includeDownloadingProcess.pbSecondStep.setVisibility(View.GONE);
            binding.includeDownloadingProcess.tvThirdStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
            binding.includeDownloadingProcess.tvThirdStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
            binding.includeDownloadingProcess.pbThirdStep.setVisibility(View.GONE);
        } else if (view == ValueCodes.FIRST_STEP) {
            binding.tvUpdateProgress.setText(getString(R.string.lbl_fw_update_status_downloading) + " ...");
            binding.includeDownloadingProcess.tvFirstStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            binding.includeDownloadingProcess.tvFirstStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
            updateProgress(0);
        } else if (view == ValueCodes.SECOND_STEP) {
            binding.tvUpdateProgress.setText(getString(R.string.lbl_fw_update_status_checking) + " ...");
            binding.includeDownloadingProcess.tvFirstStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
            binding.includeDownloadingProcess.tvSecondStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            binding.includeDownloadingProcess.tvSecondStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
            updateProgress(0);
        } else if (view == ValueCodes.THIRD_STEP) {
            binding.tvUpdateProgress.setText(getString(R.string.lbl_fw_update_status_installing) + " ...");
            binding.includeDownloadingProcess.tvSecondStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
            binding.includeDownloadingProcess.tvThirdStep.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            binding.includeDownloadingProcess.tvThirdStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
            updateProgress(0);
        } else if (view == ValueCodes.FOURTH_STEP) {
            binding.includeDownloadingProcess.tvThirdStep.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
        }
    }

    private void downloadFile() {
        setVisibility(ValueCodes.FIRST_STEP);
        updateProgress(10);
        FirmwareServiceHelper firmwareServiceHelper = new FirmwareServiceHelper(requireContext());
        Callback<ResponseBody> callback = new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        binding.pbUpdating.setProgress(80);
                        firmwareFile = response.body().bytes();
                        updateProgress(100);
                        Log.d("OTA", "¡Descarga lista! Tamaño: " + firmwareFile.length + " bytes");
                        checkFile();
                    } catch (Exception e) {
                        Log.e("OTA", "Error al procesar bytes: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("OTA", "Error al bajar .gbl: " + t.getMessage());
            }
        };
        updateProgress(20);
        firmwareServiceHelper.downloadBinary(downloadUrl, callback);
        updateProgress(50);
    }

    private void checkFile() {
        setVisibility(ValueCodes.SECOND_STEP);
        updateProgress(20);
        setOtaBegin();
    }

    private void updateProgress(int value) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null && binding.pbUpdating != null)
                binding.pbUpdating.setProgress(value);
        });
    }

    private void setOtaBegin() {
        updateProgress(40);
        byte[] b = new byte[] {0x00};
        boolean result = TransferBleData.writeOTA(b);
        updateProgress(60);
        if (result)
            parameter = ValueCodes.MTU;
    }

    private void requestMTU() {
        updateProgress(80);
        try {
            Thread.sleep(ValueCodes.MESSAGE_PERIOD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = TransferBleData.requestMtu(MTU, true);
        updateProgress(100);
        if (result)
            parameter = ValueCodes.UPDATE;
    }

    private void otaUpload() {
        if (index == 0) {
            setVisibility(ValueCodes.THIRD_STEP);

            TransferBleData.requestConnectionPriority();
            try {
                Thread.sleep(ValueCodes.WAITING_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (index < firmwareFile.length) {
            int restSize = firmwareFile.length - index;
            int currentChunkSize = Math.min(restSize, MTU - 3);

            byte[] payload = new byte[currentChunkSize];
            System.arraycopy(firmwareFile, index, payload, 0, currentChunkSize);

            Log.d("OTA", "Enviando paquete " + packetNumber + " (size " + payload.length + "): index " + index + " de " + firmwareFile.length);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded() && getView() != null) {
                    boolean success = TransferBleData.writeOTA(payload);
                    if (success) {
                        index += currentChunkSize;
                        packetNumber++;
                        binding.pbUpdating.setProgress((index * 100) / firmwareFile.length);
                    } else {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isAdded() && getView() != null)
                                otaUpload();
                        }, 50);
                    }
                }
            }, 10);
        } else {
            Log.i("OTA", "OTA UPLOAD SEND DONE");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded() && getView() != null)
                    otaEnd();
            }, ValueCodes.MESSAGE_PERIOD);
        }
    }

    private void otaEnd() {
        setVisibility(ValueCodes.FOURTH_STEP);
        byte[] b = new byte[] {0x03};
        int i = 0;
        while (!TransferBleData.writeOTA(b)) {
            i++;
            Log.i("OTA", "Failed to write end 0x03 retry:" + i);
        }
        parameter = ValueCodes.FINISH;
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {
        if (!isAdded() || getView() == null) return;
        switch (parameter) {
            case ValueCodes.MTU:
                requestMTU();
                break;
            case ValueCodes.UPDATE:
                otaUpload();
                break;
            case ValueCodes.FINISH:
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(this)
                            .add(R.id.fcv_activity_fragment, new SuccessfulMessageFragment(ValueCodes.UPDATE))
                            .addToBackStack(null)
                            .commit();
                }
                break;
        }
    }

    @Override
    public void onGattDataAvailable(byte[] packet) {}
}
