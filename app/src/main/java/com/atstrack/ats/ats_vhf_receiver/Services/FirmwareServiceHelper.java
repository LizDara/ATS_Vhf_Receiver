package com.atstrack.ats.ats_vhf_receiver.Services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentResultListener;

import com.atstrack.ats.ats_vhf_receiver.FirmwareUpdateActivity;
import com.atstrack.ats.ats_vhf_receiver.Fragments.FirmwareUpdate;
import com.atstrack.ats.ats_vhf_receiver.Models.FirmwareResponse;
import com.atstrack.ats.ats_vhf_receiver.Utils.Messages;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class FirmwareServiceHelper {
    private final Context context;
    private final RetrofitServices api;
    private DialogFragment firmwareUpdate;

    public FirmwareServiceHelper (Context context) {
        this.context = context;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitServices.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(RetrofitServices.class);
    }

    public void updateAvailable(boolean showUpdatedReceiverMessage) {
        api.getFirmwareConfig(RetrofitServices.apiKey).enqueue(new retrofit2.Callback<FirmwareResponse>() {
            @Override
            public void onResponse(Call<FirmwareResponse> call, retrofit2.Response<FirmwareResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("OTA", "Version: " + response.body().getVersion() + ", Size file: " + response.body().getSizeBytes());
                    SharedPreferences sharedPreferences = context.getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
                    int currentVersion = sharedPreferences.getInt(ValueCodes.FIRMWARE_VERSION, 0);
                    int result = compareVersions(currentVersion, response.body().getVersion());
                    if (result > 0) {
                        firmwareUpdate = FirmwareUpdate.newInstance();
                        showFirmwareMessage(response.body());
                    } else {
                        Log.d("OTA", "The receiver is already updated.");
                        if (showUpdatedReceiverMessage) {
                            Messages.showMessage((Activity) context, "Updated!", "The receiver is already updated.");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<FirmwareResponse> call, Throwable t) {
                Log.e("OTA", "Error al obtener JSON: " + t.getMessage());
            }
        });
    }

    public void downloadBinary(String url, Callback<ResponseBody> callback) {
        api.downloadFile(url).enqueue(callback);
    }

    public int compareVersions(int currentVersion, String newVersion) {
        String[] newParts = newVersion.split("\\.");
        if (Integer.parseInt(newParts[0]) > currentVersion) return 1; // Update is available
        if (Integer.parseInt(newParts[0]) < currentVersion) return -1; // The one from the device is newer
        return 0; // They are exactly the same
        /*String[] currentParts = currentVersion.split("\\.");
        String[] newParts = newVersion.split("\\.");

        int length = Math.max(currentParts.length, newParts.length);

        for (int i = 0; i < length; i++) {
            // Si una versión es más corta que otra (ej: "1.0" vs "1.0.1"), tratamos el faltante como 0
            int vCurrent = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int vNew = i < newParts.length ? Integer.parseInt(newParts[i]) : 0;

            if (vNew > vCurrent) return 1;  // Hay actualización disponible
            if (vCurrent > vNew) return -1; // La del dispositivo es más reciente
        }

        return 0; // Son exactamente iguales*/
    }

    private void showFirmwareMessage(FirmwareResponse latestVersion) {
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            activity.getSupportFragmentManager().setFragmentResultListener(ValueCodes.VALUE, activity, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    boolean update = bundle.getBoolean(ValueCodes.VALUE);
                    if (update) updateFirmware(latestVersion);
                }
            });
            try {
                firmwareUpdate.show(activity.getSupportFragmentManager(), FirmwareUpdate.TAG);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateFirmware(FirmwareResponse latestVersion) {
        Intent intent = new Intent(context, FirmwareUpdateActivity.class);
        intent.putExtra(ValueCodes.FIRMWARE_VERSION, latestVersion.getVersion());
        intent.putExtra(ValueCodes.VALUE, latestVersion.getDownloadUrl());
        context.startActivity(intent);
    }
}
