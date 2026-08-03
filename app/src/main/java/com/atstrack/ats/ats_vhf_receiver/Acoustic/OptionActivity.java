package com.atstrack.ats.ats_vhf_receiver.Acoustic;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.ActivityAcousticOptionBinding;

public class OptionActivity extends AppCompatActivity {
    private final static String TAG = OptionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.atstrack.ats.ats_vhf_receiver.databinding.ActivityAcousticOptionBinding binding = ActivityAcousticOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActivitySetting.setToolbar(this, getString(R.string.acoustic_receiver), ValueCodes.ACOUSTIC);
    }
}