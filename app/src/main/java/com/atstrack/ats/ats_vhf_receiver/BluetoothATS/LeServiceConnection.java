package com.atstrack.ats.ats_vhf_receiver.BluetoothATS;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

public class LeServiceConnection {
    private static LeServiceConnection leServiceConnection;

    private BluetoothLeService bluetoothLeService;
    private boolean connected;
    private ServiceConnection serviceConnection;

    private LeServiceConnection() {}

    public static LeServiceConnection getInstance() {
        if (leServiceConnection == null)
            leServiceConnection = new LeServiceConnection();
        return leServiceConnection;
    }

    public ServiceConnection getServiceConnection() {
        if (serviceConnection == null)
            initialize();
        return serviceConnection;
    }

    public BluetoothLeService getBluetoothLeService() {
        return bluetoothLeService;
    }

    public boolean isConnected() {
        return connected;
    }

    private void initialize() {
        Log.i("LESERVICECONNECTION", "INITIALIZE");
        serviceConnection = new ServiceConnection() { // Code to manage Service lifecycle.
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (bluetoothLeService.initialize()) {
                    ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
                    // Automatically connects to the device upon successful start-up initialization.
                    connected = bluetoothLeService.connect(receiverInformation.getDeviceAddress());
                    Log.i("LESERVICECONNECTION", "CONNECTED: " + connected);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                connected = false;
                bluetoothLeService = null;
            }
        };
    }

    public void close() {
        serviceConnection = null;
        bluetoothLeService = null;
        connected = false;
    }
}
