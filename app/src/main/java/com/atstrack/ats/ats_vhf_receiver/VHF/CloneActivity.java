package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class CloneActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_clone;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.clone_receiver);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}