package com.atstrack.ats.ats_vhf_receiver.Interfaces;

public interface ReceiverCallback {
    void onGattDisconnected();
    void onGattDiscovered();
    void onGattDataAvailable(byte[] packet);
}
