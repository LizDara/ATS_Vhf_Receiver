package com.atstrack.ats.ats_vhf_receiver.Utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Calendar;

public class Snapshots implements Parcelable {

    public static final Creator<Snapshots> CREATOR = new Creator<Snapshots>() {
        @Override
        public Snapshots createFromParcel(Parcel in) {
            return new Snapshots(in);
        }

        @Override
        public Snapshots[] newArray(int size) {
            return new Snapshots[size];
        }
    };

    public static final int BYTES_PER_PAGE = 2048;
    private String fileName;
    private boolean error;
    private boolean filled;
    private final byte[] snapshot;
    public int byteIndex;
    private int size;

    public Snapshots() {
        //attributable variables
        snapshot = new byte[244];
        fileName = "";
        //internal use variables
        byteIndex = 0;
        filled = false;
        error = false;
        this.size = snapshot.length;
    }

    public Snapshots(int size) {
        //attributable variables
        snapshot = new byte[size];
        fileName = "";
        //internal use variables
        byteIndex = 0;
        filled = false;
        error = false;
        this.size = size;
    }

    private Snapshots(Parcel in) {
        fileName = in.readString();
        filled = in.readByte() != 0;
        error = in.readByte() != 0;
        snapshot = in.createByteArray();
        byteIndex = in.readInt();
    }

    public String getFileName() {
        return fileName;
    }

    private void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getSnapshot() {
        return snapshot;
    }

    public boolean isFilled() {
        return filled;
    }

    /**
     * Generates current date and time, setting variable fileName with the results.
     * Uses global variables to fill fileName.
     * @param isRaw If true, the word RAW is added to fileName.
     */
    private void setFileName(boolean isRaw) {
        Calendar time = Calendar.getInstance();
        String month = ((time.get(Calendar.MONTH) + 1) < 10) ? "0" + (time.get(Calendar.MONTH) + 1) : String.valueOf(time.get(Calendar.MONTH) + 1);
        String day = (time.get(Calendar.DAY_OF_MONTH) < 10) ? "0" + time.get(Calendar.DAY_OF_MONTH) : String.valueOf(time.get(Calendar.DAY_OF_MONTH));
        String hour = (time.get(Calendar.HOUR_OF_DAY) < 10) ? "0" + time.get(Calendar.HOUR_OF_DAY) : String.valueOf(time.get(Calendar.HOUR_OF_DAY));
        String minute = (time.get(Calendar.MINUTE) < 10) ? "0" + time.get(Calendar.MINUTE) : String.valueOf(time.get(Calendar.MINUTE));
        String second = (time.get(Calendar.SECOND) < 10) ? "0" + time.get(Calendar.SECOND) : String.valueOf(time.get(Calendar.SECOND));
        fileName = "D" + ReceiverInformation.getReceiverInformation().getSerialNumber() + "_" + month + day + (time.get(Calendar.YEAR) - 2000)
                + hour + minute + second + (isRaw ? "Raw" : "") + ".txt";
    }

    /**
     * GET RAW PACKAGES WRITTEN IN THE TXT FORMAT
     * @param packRead Conceived to receive 244 bytes.
     */
    public void processSnapshotRaw(byte[] packRead) {
        try {
            if (byteIndex == 0)
                setFileName(true);
            System.arraycopy(packRead, 0, snapshot, byteIndex, packRead.length);
            byteIndex += packRead.length;
            if (byteIndex == size) filled = true;
        }
        catch (Exception e) {
            Log.i("Snapshot", "Error Process Snapshot Raw" + e.getLocalizedMessage());
            error = true;
        }
    }

    /**
     * GET PACKAGES WRITTEN IN THE TXT FORMAT
     * @param packRead Conceived to receive 244 bytes.
     */
    public void processSnapshot(byte[] packRead) {
        try {
            if (packRead.length > 0) {
                if (byteIndex == 0)
                    setFileName(false);
                System.arraycopy(packRead, 0, snapshot, byteIndex, packRead.length);
                byteIndex += packRead.length;
            }
        } catch (Exception e) {
            Log.i("Snapshot", "Error Process Snapshot");
            error = true;
        }
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeByte((byte) (filled ? 1 : 0));
        dest.writeByte((byte) (error ? 1 : 0));
        dest.writeByteArray(snapshot);
        dest.writeInt(byteIndex);
    }
}
