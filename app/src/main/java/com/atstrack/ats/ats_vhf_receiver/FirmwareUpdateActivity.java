package com.atstrack.ats.ats_vhf_receiver;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirmwareUpdateActivity extends AppCompatActivity {

    @BindView(R.id.firmware_versions_linearLayout)
    LinearLayout firmware_versions_linearLayout;
    @BindView(R.id.process_file_linearLayout)
    LinearLayout process_file_linearLayout;
    @BindView(R.id.message_complete_linearLayout)
    LinearLayout message_complete_linearLayout;
    @BindView(R.id.message_complete_textView)
    TextView message_complete_textView;
    @BindView(R.id.return_screen_button)
    Button return_screen_button;
    @BindView(R.id.first_step_imageView)
    ImageView first_step_imageView;
    @BindView(R.id.first_step_textView)
    TextView first_step_textView;
    @BindView(R.id.first_step_progressBar)
    ProgressBar first_step_progressBar;
    @BindView(R.id.second_step_imageView)
    ImageView second_step_imageView;
    @BindView(R.id.second_step_textView)
    TextView second_step_textView;
    @BindView(R.id.second_step_progressBar)
    ProgressBar second_step_progressBar;
    @BindView(R.id.third_step_imageView)
    ImageView third_step_imageView;
    @BindView(R.id.third_step_textView)
    TextView third_step_textView;
    @BindView(R.id.third_step_progressBar)
    ProgressBar third_step_progressBar;

    private final static String TAG = FirmwareUpdateActivity.class.getSimpleName();

    @OnClick(R.id.begin_update_button)
    public void onClickBeginUpdate(View v) {
        setVisibility("process");
    }

    @OnClick(R.id.cancel_update_button)
    public void onClickCancelUpdate(View v) {
        setVisibility("versions");
    }

    @OnClick(R.id.return_screen_button)
    public void onClickReturnScreen(View v) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);
        ButterKnife.bind(this);
        ActivitySetting.setVhfToolbar(this, getString(R.string.firmware_update));

        setVisibility("versions");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
    }

    private void setVisibility(String value) {
        switch (value) {
            case "versions":
                firmware_versions_linearLayout.setVisibility(View.VISIBLE);
                process_file_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.GONE);
                break;
            case "process":
                firmware_versions_linearLayout.setVisibility(View.GONE);
                process_file_linearLayout.setVisibility(View.VISIBLE);
                message_complete_linearLayout.setVisibility(View.GONE);
                first_step_textView.setText(R.string.lb_downloading_file);
                second_step_textView.setText(R.string.lb_checking_file);
                third_step_textView.setText(R.string.lb_installing_firmware);
                break;
            case "completed":
                firmware_versions_linearLayout.setVisibility(View.GONE);
                process_file_linearLayout.setVisibility(View.GONE);
                message_complete_linearLayout.setVisibility(View.VISIBLE);
                message_complete_textView.setText(R.string.lb_installation_complete);
                return_screen_button.setText(R.string.lb_return_device_screen);
                break;
        }
    }

    private void initDownloading() {
        first_step_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        first_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        first_step_progressBar.setVisibility(View.GONE);
        second_step_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        second_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        second_step_progressBar.setVisibility(View.GONE);
        third_step_imageView.setBackgroundResource(R.drawable.ic_circle_light);
        third_step_textView.setTextColor(ContextCompat.getColor(this, R.color.slate_gray));
        third_step_progressBar.setVisibility(View.GONE);
    }

    private void loadDownloading() {
        first_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        first_step_imageView.setBackgroundResource(R.drawable.ic_circle);
        first_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void loadProcessing() {
        first_step_imageView.setBackgroundResource(R.drawable.circle_check);
        first_step_progressBar.setVisibility(View.GONE);
        second_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        second_step_imageView.setBackgroundResource(R.drawable.ic_circle);
        second_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void loadPreparing() {
        second_step_imageView.setBackgroundResource(R.drawable.circle_check);
        second_step_progressBar.setVisibility(View.GONE);
        third_step_textView.setTextColor(ContextCompat.getColor(this, R.color.ebony_clay));
        third_step_imageView.setBackgroundResource(R.drawable.ic_circle);
        third_step_progressBar.setVisibility(View.VISIBLE);
    }

    private void downloaded() {
        third_step_imageView.setBackgroundResource(R.drawable.circle_check);
        third_step_progressBar.setVisibility(View.GONE);
    }
}