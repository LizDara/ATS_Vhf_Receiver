package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ManualScanningFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.ManualSettingsFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class ManualScanActivity extends ScanBaseActivity implements OnDialogCreatedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_fragment;
        title = getString(R.string.lb_start_scanning);
        scanType = ValueCodes.MANUAL_SCAN_COMMAND;
        super.onCreate(savedInstanceState);
        byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fcv_activity_fragment, isScanning ? new ManualScanningFragment(baseFrequency, range, data) : new ManualSettingsFragment(baseFrequency, range))
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (isScanning) {
                boolean result = TransferBleData.writeStopScan(scanType);
                if (result) {
                    if (getSupportFragmentManager() != null) {
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.fcv_activity_fragment, new ManualSettingsFragment(baseFrequency, range))
                                .commit();
                    }
                }
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void downloadData(byte[] data) {
        super.downloadData(data);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcv_activity_fragment);
        if (currentFragment instanceof ReceiverCallback) {
            runOnUiThread(() -> {
                ((ReceiverCallback) currentFragment).onGattDataAvailable(data);
            });
        }
    }

    @Override
    public void onNewDialogAdded(AlertDialog dialog) {
        dialogList.add(dialog);
        dialog.setOnDismissListener(d -> dialogList.remove(dialog));
    }
}