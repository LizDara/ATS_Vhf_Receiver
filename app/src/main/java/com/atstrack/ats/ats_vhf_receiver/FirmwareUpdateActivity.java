package com.atstrack.ats.ats_vhf_receiver;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Services.FirmwareServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class FirmwareUpdateActivity extends BaseActivity {

    @BindView(R.id.firmware_versions_linearLayout)
    LinearLayout firmware_versions_linearLayout;
    @BindView(R.id.version_name_textView)
    TextView version_name_textView;
    @BindView(R.id.update_progress_textView)
    TextView update_progress_textView;
    @BindView(R.id.updating_progressBar)
    ProgressBar updating_progressBar;
    @BindView(R.id.process_file_linearLayout)
    LinearLayout process_file_linearLayout;
    @BindView(R.id.message_complete_linearLayout)
    LinearLayout message_complete_linearLayout;
    @BindView(R.id.message_complete_textView)
    TextView message_complete_textView;
    @BindView(R.id.main_complete_button)
    Button main_complete_button;
    @BindView(R.id.first_step_textView)
    TextView first_step_textView;
    @BindView(R.id.first_step_progressBar)
    ProgressBar first_step_progressBar;
    @BindView(R.id.second_step_textView)
    TextView second_step_textView;
    @BindView(R.id.second_step_progressBar)
    ProgressBar second_step_progressBar;
    @BindView(R.id.third_step_textView)
    TextView third_step_textView;
    @BindView(R.id.third_step_progressBar)
    ProgressBar third_step_progressBar;

    private String downloadURl;
    private byte[] firmwareFile;
    private final int MTU = 247;
    private int index;
    private int packetNumber = 1;

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
            Thread.sleep(2000);
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
            loadInstalling();

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
            new Handler().postDelayed(() -> {
                boolean success = TransferBleData.writeOTA(payload);
                if (success) {
                    index += currentChunkSize;
                    packetNumber++;
                    updating_progressBar.setProgress((index * 100) / firmwareFile.length);
                } else {
                    new Handler().postDelayed(() -> {
                        otaUpload();
                    }, 50);
                }
            }, 10);
        } else {
            Log.i("OTA", "OTA UPLOAD SEND DONE");
            new Handler().postDelayed(() -> {
                otaEnd();
            }, 1000);
        }
    }

    private void otaEnd() {
        installed();
        byte[] b = new byte[] {0x03};
        int i = 0;
        while (!TransferBleData.writeOTA(b)) {
            i++;
            Log.i("OTA", "Failed to write end 0x03 retry:" + i);
        }
        parameter = ValueCodes.FINISH;
    }

    @OnClick(R.id.begin_update_button)
    public void onClickBeginUpdate(View v) {
        setVisibility("process");
        downloadFile();
    }

    @OnClick(R.id.cancel_update_button)
    public void onClickCancelUpdate(View v) {
        setVisibility("versions");
    }

    @OnClick(R.id.main_complete_button)
    public void onClickMainComplete(View v) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_firmware_update;
        showToolbar = true;
        title = getString(R.string.firmware_update);
        deviceCategory = ValueCodes.ACOUSTIC;
        super.onCreate(savedInstanceState);

        String latestVersion = getIntent().getStringExtra(ValueCodes.FIRMWARE_VERSION);
        downloadURl = getIntent().getStringExtra(ValueCodes.VALUE);
        version_name_textView.setText("Firmware Version " + latestVersion);
        setVisibility("version");
    }

    @Override
    protected void gattDisconnected() {
        unbindService(leServiceConnection.getServiceConnection());
        super.gattDisconnected();
    }

    @Override
    protected void discoverCharacteristic() {
        switch (parameter) {
            case ValueCodes.MTU:
                requestMTU();
                break;
            case ValueCodes.UPDATE:
                otaUpload();
                break;
            case ValueCodes.FINISH:
                setVisibility("completed");
                leServiceConnection.getBluetoothLeService().disconnect();
                break;
        }
    }

    @Override
    protected void downloadData(byte[] data) {
    }

    private void setVisibility(String value) {
        switch (value) {
            case "version":
                firmware_versions_linearLayout.setVisibility(View.VISIBLE);
                process_file_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "process":
                firmware_versions_linearLayout.setVisibility(View.GONE);
                process_file_linearLayout.setVisibility(View.VISIBLE);
                message_complete_linearLayout.setVisibility(View.GONE);
                first_step_textView.setText(R.string.lb_downloading_file);
                second_step_textView.setText(R.string.lb_checking_file);
                third_step_textView.setText(R.string.lb_installing_firmware);
                break;
            case "completed":
                firmware_versions_linearLayout.setVisibility(View.GONE);
                process_file_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.VISIBLE);
                message_complete_textView.setText(R.string.lb_installation_complete);
                main_complete_button.setText(R.string.lb_return_device_screen);
                break;
        }
    }

    private void loadDownloading() {
        update_progress_textView.setText(getString(R.string.lb_downloading_file) + " ...");
        first_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        first_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
        updateProgress(0);
    }

    private void loadChecking() {
        update_progress_textView.setText(getString(R.string.lb_checking_file) + " ...");
        first_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
        second_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        second_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
        updateProgress(0);
    }

    private void loadInstalling() {
        update_progress_textView.setText(getString(R.string.lb_installing_firmware) + " ...");
        second_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
        third_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        third_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
        updateProgress(0);
    }

    private void installed() {
        third_step_textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_check, 0, 0, 0);
    }

    private void downloadFile() {
        loadDownloading();
        updateProgress(10);
        FirmwareServiceHelper firmwareServiceHelper = new FirmwareServiceHelper(this);
        Callback<ResponseBody> callback = new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        updating_progressBar.setProgress(80);
                        firmwareFile = response.body().bytes();
                        Log.d("OTA", "¡Descarga lista! Tamaño: " + firmwareFile.length + " bytes");
                        updateProgress(100);
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
        firmwareServiceHelper.downloadBinary(downloadURl, callback);
        updateProgress(50);
    }

    private void checkFile() {
        loadChecking();
        updateProgress(20);
        setOtaBegin();
    }

    private void updateProgress(int value) {
        runOnUiThread(() -> updating_progressBar.setProgress(value));
    }
}