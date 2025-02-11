package com.atstrack.ats.ats_vhf_receiver;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AcousticOptionActivity extends AppCompatActivity {

    @BindView(R.id.firmware_update_linearLayout)
    LinearLayout firmware_update_linearLayout;

    private final static String TAG = AcousticOptionActivity.class.getSimpleName();

    @OnClick({R.id.close_update_imageButton, R.id.dismiss_textView})
    public void onClickCloseUpdate(View v) {
        firmware_update_linearLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.update_acoustic_button)
    public void onClickUpdate(View v) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acoustic_option);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, R.string.acoustic_receiver);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
    }
}