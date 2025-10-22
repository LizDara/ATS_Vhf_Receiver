package com.atstrack.ats.ats_vhf_receiver.BluetoothATS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;

public class GattUpdateReceiver {
    private final ReceiverCallback receiverCallback;

    public GattUpdateReceiver(ReceiverCallback receiverCallback) {
        this.receiverCallback = receiverCallback;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    receiverCallback.onGattDisconnected();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    receiverCallback.onGattDiscovered();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    receiverCallback.onGattDataAvailable(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
