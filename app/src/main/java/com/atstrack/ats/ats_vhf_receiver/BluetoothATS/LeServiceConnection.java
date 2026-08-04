package com.atstrack.ats.ats_vhf_receiver.BluetoothATS;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;

public class LeServiceConnection {
    private final String TAG = LeServiceConnection.class.getSimpleName();
    private static LeServiceConnection leServiceConnection;

    private BluetoothLeService bluetoothLeService;
    private final ServiceConnection serviceConnection = new ServiceConnection() { // Code to manage Service lifecycle.
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothLeService.initialize()) {
                ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
                // Automatically connects to the device upon successful start-up initialization.
                boolean connected = bluetoothLeService.connect(receiverInformation.getDeviceAddress());
                Log.i(TAG, "Connect to " + receiverInformation.getDeviceAddress() + ": " + connected);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    private LeServiceConnection() {}

    public static LeServiceConnection getInstance() {
        if (leServiceConnection == null)
            leServiceConnection = new LeServiceConnection();
        return leServiceConnection;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public BluetoothLeService getBluetoothLeService() {
        return bluetoothLeService;
    }


    public boolean existConnection() {
        return serviceConnection != null && bluetoothLeService != null;
    }

    public void close(Context context) {
        if (bluetoothLeService != null) {
            bluetoothLeService.close();

            try {
                context.unbindService(serviceConnection);
                Log.i(TAG, "Service unbound successfully from Context");
            } catch (Exception e) {
                Log.w(TAG, "Service already unbound or failed to unbind: " + e.getMessage());
            }
            bluetoothLeService = null;
        }
    }
}
