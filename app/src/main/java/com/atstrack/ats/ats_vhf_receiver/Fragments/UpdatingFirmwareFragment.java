package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Services.FirmwareServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class UpdatingFirmwareFragment extends Fragment implements ReceiverCallback {
    @BindView(R.id.tv_update_progress)
    TextView tv_update_progress;
    @BindView(R.id.pb_updating)
    ProgressBar pb_updating;
    @BindView(R.id.tv_first_step)
    TextView tv_first_step;
    @BindView(R.id.pb_first_step)
    ProgressBar pb_first_step;
    @BindView(R.id.tv_second_step)
    TextView tv_second_step;
    @BindView(R.id.pb_second_step)
    ProgressBar pb_second_step;
    @BindView(R.id.tv_third_step)
    TextView tv_third_step;
    @BindView(R.id.pb_third_step)
    ProgressBar pb_third_step;

    private Unbinder unbinder;
    private final String downloadUrl;
    private byte[] firmwareFile;
    private final int MTU = 247;
    private int index;
    private int packetNumber = 1;
    private int parameter;

    public UpdatingFirmwareFragment(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @OnClick(R.id.btn_cancel_update)
    public void onClickCancelUpdate(View v) {
        if (getParentFragmentManager() != null)
            getParentFragmentManager().popBackStack();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updating_firmware, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setVisibility(ValueCodes.PROCESSING);
        downloadFile();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.PROCESSING) {
            tv_first_step.setText(R.string.lb_downloading_file);
            tv_second_step.setText(R.string.lb_checking_file);
            tv_third_step.setText(R.string.lb_installing_firmware);

            tv_first_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
            tv_first_step.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
            pb_first_step.setVisibility(View.GONE);
            tv_second_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
            tv_second_step.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
            pb_second_step.setVisibility(View.GONE);
            tv_third_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_light, 0, 0, 0);
            tv_third_step.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray));
            pb_third_step.setVisibility(View.GONE);
        } else if (view == ValueCodes.FIRST_STEP) {
            tv_update_progress.setText(getString(R.string.lb_downloading_file) + " ...");
            tv_first_step.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            tv_first_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
            updateProgress(0);
        } else if (view == ValueCodes.SECOND_STEP) {
            tv_update_progress.setText(getString(R.string.lb_checking_file) + " ...");
            tv_first_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
            tv_second_step.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            tv_second_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
            updateProgress(0);
        } else if (view == ValueCodes.THIRD_STEP) {
            tv_update_progress.setText(getString(R.string.lb_installing_firmware) + " ...");
            tv_second_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
            tv_third_step.setTextColor(ContextCompat.getColor(requireContext(), R.color.ebony_clay));
            tv_third_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
            updateProgress(0);
        } else if (view == ValueCodes.FOURTH_STEP) {
            tv_third_step.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
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
                        pb_updating.setProgress(80);
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
            if (isAdded() && getView() != null && pb_updating != null)
                pb_updating.setProgress(value);
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
                        pb_updating.setProgress((index * 100) / firmwareFile.length);
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
