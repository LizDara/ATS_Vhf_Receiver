package com.atstrack.ats.ats_vhf_receiver;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.DriveService.DriveServiceHelper;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirmwareUpdateActivity extends BaseActivity {

    @BindView(R.id.firmware_versions_linearLayout)
    LinearLayout firmware_versions_linearLayout;
    @BindView(R.id.version_name_textView)
    TextView version_name_textView;
    @BindView(R.id.process_file_linearLayout)
    LinearLayout process_file_linearLayout;
    @BindView(R.id.message_complete_linearLayout)
    LinearLayout message_complete_linearLayout;
    @BindView(R.id.message_complete_textView)
    TextView message_complete_textView;
    @BindView(R.id.return_screen_button)
    Button return_screen_button;
    @BindView(R.id.first_step_imageView)
    ImageView first_step_imageView;
    @BindView(R.id.first_step_textView)
    TextView first_step_textView;
    @BindView(R.id.first_step_progressBar)
    ProgressBar first_step_progressBar;
    @BindView(R.id.second_step_imageView)
    ImageView second_step_imageView;
    @BindView(R.id.second_step_textView)
    TextView second_step_textView;
    @BindView(R.id.second_step_progressBar)
    ProgressBar second_step_progressBar;
    @BindView(R.id.third_step_imageView)
    ImageView third_step_imageView;
    @BindView(R.id.third_step_textView)
    TextView third_step_textView;
    @BindView(R.id.third_step_progressBar)
    ProgressBar third_step_progressBar;

    private final static String TAG = FirmwareUpdateActivity.class.getSimpleName();
    private String latestVersion;
    private String idFile;
    private byte[] firmwareFile;
    private final int MTU = 247;
    private int index;

    private void setOtaBegin() {
        byte[] b = new byte[] {0x00};
        boolean result = TransferBleData.writeOTA(b);
        if (result)
            parameter = ValueCodes.MTU;
    }

    private void requestMTU() {
        boolean result = TransferBleData.requestMtu(MTU + 3);
        if (result)
            parameter = ValueCodes.UPDATE;
    }

    private void otaUpload() {
        loadInstalling();
        parameter = "";
        index = 0;
        new Thread(() -> {
            boolean last = false;
            int packageCount = 0;
            while (!last) {
                byte[] payload = new byte[MTU];
                if (index + MTU >= firmwareFile.length) {
                    int restSize = firmwareFile.length - index;
                    System.arraycopy(firmwareFile, index, payload, 0, restSize); //copy rest bytes
                    last = true;
                } else {
                    payload = Arrays.copyOfRange(firmwareFile, index, index + MTU);
                }
                Log.d("OTA", "index :" + index + " firmware lenght:" + firmwareFile.length);
                while (!TransferBleData.writeOTA(payload)) { // attempt to write until getting success
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                packageCount = packageCount + 1;
                index = index + MTU;
            }
            Log.i("OTA", "OTA UPLOAD SEND DONE");
            otaEnd();
        }).start();
    }

    private void otaEnd() {
        byte[] b = new byte[] {0x03};
        int i = 0;
        while (!TransferBleData.writeOTA(b)) {
            i++;
            Log.i("OTA", "Failed to write end 0x03 retry:" + i);
        }
        parameter = ValueCodes.OTA_END;
    }

    private void rebootTargetDevice() {
        installed();
        byte[] b = new byte[] {0x04};
        boolean result = TransferBleData.writeOTA(b);
        if (result)
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

    @OnClick(R.id.return_screen_button)
    public void onClickReturnScreen(View v) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_firmware_update;
        showToolbar = true;
        title = getString(R.string.firmware_update);
        deviceCategory = ValueCodes.ACOUSTIC;
        super.onCreate(savedInstanceState);

        initializeCallback();
        latestVersion = getIntent().getStringExtra(ValueCodes.VERSION);
        idFile = getIntent().getStringExtra(ValueCodes.VALUE);
        version_name_textView.setText("Firmware Version " + latestVersion);
        setVisibility("version");
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                unbindService(leServiceConnection.getServiceConnection());
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                switch (parameter) {
                    case ValueCodes.MTU:
                        requestMTU();
                        break;
                    case ValueCodes.UPDATE:
                        otaUpload();
                        break;
                    case ValueCodes.OTA_END:
                        rebootTargetDevice();
                        break;
                    case ValueCodes.FINISH:
                        setVisibility("completed");
                        break;
                }
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {}
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
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
                return_screen_button.setText(R.string.lb_return_device_screen);
                break;
        }
    }

    private void loadDownloading() {
        first_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        first_step_imageView.setBackgroundResource(R.drawable.ic_circle);
    }

    private void loadChecking() {
        first_step_imageView.setBackgroundResource(R.drawable.circle_check);
        second_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        second_step_imageView.setBackgroundResource(R.drawable.ic_circle);
    }

    private void loadInstalling() {
        second_step_imageView.setBackgroundResource(R.drawable.circle_check);
        third_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        third_step_imageView.setBackgroundResource(R.drawable.ic_circle);
    }

    private void installed() {
        third_step_imageView.setBackgroundResource(R.drawable.circle_check);
    }

    private void downloadFile() {
        loadDownloading();
        Callback<ResponseBody> callback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        firmwareFile = response.body().bytes();
                        checkFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.i(TAG, "Not successfully call.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "Error: " + t.getLocalizedMessage());
            }
        };
        DriveServiceHelper.getGblVersion(idFile, callback);
    }

    private void checkFile() {
        loadChecking();
        setOtaBegin();
    }
}