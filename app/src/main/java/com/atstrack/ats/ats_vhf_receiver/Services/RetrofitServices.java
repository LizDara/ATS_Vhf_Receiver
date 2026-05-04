package com.atstrack.ats.ats_vhf_receiver.Services;

import com.atstrack.ats.ats_vhf_receiver.Models.FirmwareResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

public interface RetrofitServices {
    String baseUrl = "https://ats-firmware-api-dbefa6djgwhubyd5.centralus-01.azurewebsites.net";
    String apiKey = "1BCyecc15fGEJl025BY2l6bf37vcm8Z+YnlLN5dXge0=";

    @GET("api/firmware/latest")
    Call<FirmwareResponse> getFirmwareConfig(@Header("X-Api-Key") String key);

    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);
}
