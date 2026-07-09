package com.atstrack.ats.ats_vhf_receiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.Fragments.FirmwareVersionFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class FirmwareUpdateActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_firmware_update;
        showToolbar = true;
        title = getString(R.string.firmware_update);
        deviceCategory = ValueCodes.ACOUSTIC;
        super.onCreate(savedInstanceState);
        String latestVersion = getIntent().getStringExtra(ValueCodes.FIRMWARE_VERSION);
        String downloadURl = getIntent().getStringExtra(ValueCodes.VALUE);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fcv_activity_fragment, new FirmwareVersionFragment(latestVersion, downloadURl))
                    .commit();
        }
    }

    @Override
    protected void gattDisconnected() {
        unbindService(leServiceConnection.getServiceConnection());
        super.gattDisconnected();
    }

    @Override
    protected void discoverCharacteristic() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcv_activity_fragment);
        if (currentFragment instanceof ReceiverCallback) {
            runOnUiThread(() -> {
                ((ReceiverCallback) currentFragment).onGattDiscovered();
            });
        }
    }

    @Override
    protected void downloadData(byte[] data) {}
}