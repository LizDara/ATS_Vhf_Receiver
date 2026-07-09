package com.atstrack.ats.ats_vhf_receiver.Utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Models.ReceiverInformation;
import com.atstrack.ats.ats_vhf_receiver.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class ActivitySetting {
    public static void setReceiverStatus(Activity context) {
        setSdCardStatus(context);
        setBatteryPercent(context);
    }

    public static void setSdCardStatus(Activity context) {
        ImageView sd_card_imageView = context.findViewById(R.id.img_sd_card);
        TextView sd_card_textView = context.findViewById(R.id.tv_sd_card);
        sd_card_imageView.setBackground(ContextCompat.getDrawable(context, ReceiverInformation.getReceiverInformation().isSDCardInserted() ? R.drawable.ic_sd_card : R.drawable.ic_no_sd_card));
        sd_card_textView.setText(ReceiverInformation.getReceiverInformation().isSDCardInserted() ? "Inserted" : "None");
    }

    public static void setBatteryPercent(Activity context) {
        ImageView percent_battery_imageView = context.findViewById(R.id.img_battery);
        TextView percent_battery_textView = context.findViewById(R.id.tv_percent_battery);
        percent_battery_imageView.setBackground(ContextCompat.getDrawable(context, ReceiverInformation.getReceiverInformation().getPercentBattery() > 20 ? R.drawable.ic_full_battery : R.drawable.ic_low_battery));
        percent_battery_textView.setText(ReceiverInformation.getReceiverInformation().getPercentBattery() + "%");
    }

    public static void setToolbar(AppCompatActivity context, String title, String deviceCategory) {
        Toolbar toolbar = context.findViewById(R.id.tb_main);
        if (!deviceCategory.equals(ValueCodes.VHF)) {
            View state = context.findViewById(R.id.v_state);
            state.setVisibility(View.GONE);
        }
        TextView title_toolbar = context.findViewById(R.id.tv_title_toolbar);
        title_toolbar.setText(title);
        context.setSupportActionBar(toolbar);
        Objects.requireNonNull(context.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        context.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }
}
