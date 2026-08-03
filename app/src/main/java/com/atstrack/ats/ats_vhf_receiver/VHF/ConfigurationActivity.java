package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityVhfConfigurationBinding;

public class ConfigurationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.title_vhf_config_main);
        binding = ActivityVhfConfigurationBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        ((ActivityVhfConfigurationBinding) binding).btnEditFrequencyTables.setOnClickListener(v -> {
            Intent intent = new Intent(this, TablesActivity.class);
            intent.putExtra(ValueCodes.PARAMETER, ValueCodes.TABLES_COMMAND);
            startActivity(intent);
        });
        ((ActivityVhfConfigurationBinding) binding).btnEditReceiverDefaults.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditDefaultsActivity.class);
            startActivity(intent);
        });
        ((ActivityVhfConfigurationBinding) binding).btnSetTransmitterType.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetectionFilterActivity.class);
            intent.putExtra(ValueCodes.PARAMETER, ValueCodes.DETECTION_FILTER_COMMAND);
            startActivity(intent);
        });
        ((ActivityVhfConfigurationBinding) binding).btnCloneFromOtherReceiver.setOnClickListener(v -> {
            Intent intent = new Intent(this, CloneActivity.class);
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