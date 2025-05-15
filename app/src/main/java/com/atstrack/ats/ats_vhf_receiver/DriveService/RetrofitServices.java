package com.atstrack.ats.ats_vhf_receiver.DriveService;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitServices {
    String baseUrl = "https://drive.usercontent.google.com/u/0/";

    @GET("uc")
    Call<VersionResponse> getVersion(@Query("id") String id, @Query("export") String export);

    @GET("uc")
    Call<ResponseBody> getGblFile(@Query("id") String id, @Query("export") String export);
}
