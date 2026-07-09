package com.atstrack.ats.ats_vhf_receiver.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FirmwareResponse {
    @SerializedName("version")
    @Expose
    private String version;

    @SerializedName("downloadUrl")
    @Expose
    private String downloadUrl;

    @SerializedName("urlExpiresAt")
    @Expose
    private String urlExpiresDate;

    @SerializedName("fileSizeBytes")
    @Expose
    private int sizeBytes;

    public String getVersion() {
        return version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getUrlExpiresDate() {
        return urlExpiresDate;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }
}
