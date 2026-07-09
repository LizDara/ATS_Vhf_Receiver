package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Fragments.MenuManageDataFragment;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ManageDataActivity extends BaseActivity implements OnDialogCreatedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_fragment;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.manage_receiver_data);
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fcv_activity_fragment, new MenuManageDataFragment(), String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }
        parameter = ValueCodes.STORAGE_COMMAND;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (getSupportFragmentManager().getBackStackEntryCount() > 0 && getSupportFragmentManager().getBackStackEntryCount() < 3) {
                getSupportFragmentManager().popBackStack();
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 3) {
                Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.FIRST_STEP));
                Fragment fragment2 = getSupportFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.SECOND_STEP));
                Fragment fragment3 = getSupportFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.THIRD_STEP));
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcv_activity_fragment);
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                if (fragment3 != null) transaction.remove(fragment3);
                if (fragment2 != null) transaction.remove(fragment2);
                if (currentFragment != null) transaction.remove(currentFragment);
                if (fragment1 != null)
                    transaction.show(fragment1);
                transaction.commit();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void gattDisconnected() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcv_activity_fragment);
        if (currentFragment instanceof ReceiverCallback) {
            runOnUiThread(() -> {
                ((ReceiverCallback) currentFragment).onGattDisconnected();
            });
        }
        super.gattDisconnected();
    }

    @Override
    protected void discoverCharacteristic() {
        if (parameter == ValueCodes.STORAGE_COMMAND) {
            TransferBleData.readDataInfo();
            parameter = ValueCodes.NONE;
        }
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