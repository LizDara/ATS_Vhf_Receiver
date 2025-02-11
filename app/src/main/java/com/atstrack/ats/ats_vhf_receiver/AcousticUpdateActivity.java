package com.atstrack.ats.ats_vhf_receiver;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class AcousticUpdateActivity extends AppCompatActivity {

    private final static String TAG = AcousticUpdateActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acoustic_update);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
    }
}