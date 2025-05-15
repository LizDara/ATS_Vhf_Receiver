package com.atstrack.ats.ats_vhf_receiver.DriveService;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionResponse implements Parcelable {
    @SerializedName("version")
    @Expose
    private String version;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("date")
    @Expose
    private String date;

    protected VersionResponse(Parcel in) {
        version = in.readString();
        id = in.readString();
        date = in.readString();
    }

    public static final Creator<VersionResponse> CREATOR = new Creator<VersionResponse>() {
        @Override
        public VersionResponse createFromParcel(Parcel in) {
            return new VersionResponse(in);
        }

        @Override
        public VersionResponse[] newArray(int size) {
            return new VersionResponse[size];
        }
    };

    public String getVersion() {
        return version;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(version);
        parcel.writeString(id);
        parcel.writeString(date);
    }
}
