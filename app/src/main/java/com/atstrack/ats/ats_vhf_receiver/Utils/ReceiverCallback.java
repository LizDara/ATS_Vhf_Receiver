package com.atstrack.ats.ats_vhf_receiver.Utils;

public interface ReceiverCallback {
    void onGattDisconnected();
    void onGattDiscovered();
    void onGattDataAvailable(byte[] packet);
}
