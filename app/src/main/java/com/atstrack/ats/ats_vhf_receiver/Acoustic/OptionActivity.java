package com.atstrack.ats.ats_vhf_receiver.Acoustic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.atstrack.ats.ats_vhf_receiver.FirmwareUpdateActivity;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class OptionActivity extends AppCompatActivity {

//    @BindView(R.id.firmware_update_message_linearLayout)
//    LinearLayout firmware_update_linearLayout;

    private final static String TAG = OptionActivity.class.getSimpleName();

    @OnClick({R.id.ib_close_update, R.id.tv_dismiss})
    public void onClickCloseUpdate(View v) {
//        firmware_update_linearLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_update_firmware)
    public void onClickUpdate(View v) {
        Intent intent = new Intent(this, FirmwareUpdateActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acoustic_option);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, getString(R.string.acoustic_receiver), ValueCodes.ACOUSTIC);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
    }
}