package com.atstrack.ats.ats_vhf_receiver.Models;

import android.util.Log;

public class Snapshots {
    public static final int BYTES_PER_PAGE = 2048;
    private boolean filled;
    private final byte[] snapshot;
    public int byteIndex;
    private final int size;

    public Snapshots() {
        //attributable variables
        snapshot = new byte[BYTES_PER_PAGE];
        //internal use variables
        byteIndex = 0;
        filled = false;
        this.size = snapshot.length;
    }

    public Snapshots(int size) {
        //attributable variables
        snapshot = new byte[size];
        //internal use variables
        byteIndex = 0;
        filled = false;
        this.size = size;
    }

    public byte[] getSnapshot() {
        return snapshot;
    }

    public boolean isFilled() {
        return filled;
    }

    /**
     * GET PACKAGES WRITTEN IN THE TXT FORMAT
     * @param packRead Conceived to receive ? processed bytes.
     */
    /*public void processSnapshot(byte[] packRead) {
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
    }*/

    public void processSnapshot(byte[] packRead) {
        try {
            int extraBytes;
            if (byteIndex < 1824)
                extraBytes = 2;
            else
                extraBytes = 6;
            System.arraycopy(packRead, 0, snapshot, byteIndex, packRead.length - extraBytes);
            byteIndex += packRead.length - extraBytes;
            if (byteIndex == size) filled = true;
        }
        catch (Exception e) {
            Log.i("Snapshot", "Error Process Snapshot Raw" + e.getLocalizedMessage());
        }
    }
}
