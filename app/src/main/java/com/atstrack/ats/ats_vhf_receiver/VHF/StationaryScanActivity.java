package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.StationaryScanningFragment;
import com.atstrack.ats.ats_vhf_receiver.Fragments.StationarySettingsFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class StationaryScanActivity extends ScanBaseActivity implements OnDialogCreatedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_fragment;
        title = getString(R.string.stationary_scanning);
        scanType = ValueCodes.STATIONARY_SCAN_COMMAND;
        super.onCreate(savedInstanceState);
        byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
        if (savedInstanceState == null) {
            if (isScanning) {
                StationarySettingsFragment f1 = new StationarySettingsFragment(baseFrequency);
                StationaryScanningFragment f2 = new StationaryScanningFragment(baseFrequency, data);
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.fcv_activity_fragment, f1, String.valueOf(ValueCodes.FIRST_STEP))
                        .hide(f1)
                        .add(R.id.fcv_activity_fragment, f2, String.valueOf(ValueCodes.SECOND_STEP))
                        .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.fcv_activity_fragment, new StationarySettingsFragment(baseFrequency, data), String.valueOf(ValueCodes.FIRST_STEP))
                        .commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (isScanning) {
                boolean result = TransferBleData.writeStopScan(scanType);
                if (result) {
                    Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.FIRST_STEP));
                    Fragment fragment2 = getSupportFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.SECOND_STEP));
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    if (fragment2 != null) transaction.remove(fragment2);
                    if (fragment1 != null)
                        transaction.show(fragment1);
                    transaction.commit();
                }
            } else {
                finish();
            }
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