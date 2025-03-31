package com.atstrack.ats.ats_vhf_receiver.Utils;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class ActivitySetting {
    public static void setReceiverStatus(Activity context) {
        ImageView percent_battery_imageView = context.findViewById(R.id.battery_imageView);
        TextView percent_battery_textView = context.findViewById(R.id.percent_battery_textView);
        ImageView sd_card_imageView = context.findViewById(R.id.sd_card_imageView);
        TextView sd_card_textView = context.findViewById(R.id.sd_card_textView);
        ReceiverInformation receiverInformation = ReceiverInformation.getReceiverInformation();
        percent_battery_imageView.setBackground(ContextCompat.getDrawable(context, receiverInformation.getPercentBattery() > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));
        percent_battery_textView.setText(receiverInformation.getPercentBattery() + "%");
        sd_card_imageView.setBackground(ContextCompat.getDrawable(context, receiverInformation.getSDCard().equals("None") ? R.drawable.ic_no_sd_card : R.drawable.ic_sd_card));
        sd_card_textView.setText(receiverInformation.getSDCard());
    }

    public static void setVhfToolbar(AppCompatActivity context, String title) {
        initializeToolbar(context, ValueCodes.VHF);
        TextView title_toolbar = context.findViewById(R.id.title_toolbar);
        title_toolbar.setText(title);
    }

    public static void setAcousticToolbar(AppCompatActivity context, String title) {
        initializeToolbar(context, ValueCodes.ACOUSTIC);
        TextView title_toolbar = context.findViewById(R.id.main_title_toolbar);
        title_toolbar.setText(title);
    }

    private static void initializeToolbar(AppCompatActivity context, String deviceCategory) {
        Toolbar toolbar;
        if (deviceCategory.equals(ValueCodes.VHF))
            toolbar = context.findViewById(R.id.toolbar);
        else
            toolbar = context.findViewById(R.id.main_toolbar);
        context.setSupportActionBar(toolbar);
        Objects.requireNonNull(context.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        context.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }
}
