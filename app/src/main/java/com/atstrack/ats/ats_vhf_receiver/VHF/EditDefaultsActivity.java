package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfEditDefaultsBinding;

public class EditDefaultsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.title_vhf_defaults_main);
        binding = ActivityVhfEditDefaultsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityVhfEditDefaultsBinding) binding).btnMobileDefaults.setOnClickListener(v -> {
            Intent intent = new Intent(this, MobileDefaultsActivity.class);
            intent.putExtra(ValueCodes.PARAMETER, ValueCodes.MOBILE_DEFAULTS_COMMAND);
            startActivity(intent);
        });
        ((ActivityVhfEditDefaultsBinding) binding).btnStationaryDefaults.setOnClickListener(v -> {
            Intent intent = new Intent(this, StationaryDefaultsActivity.class);
            intent.putExtra(ValueCodes.PARAMETER, ValueCodes.STATIONARY_DEFAULTS_COMMAND);
            startActivity(intent);
        });
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