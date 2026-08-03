package com.atstrack.ats.ats_vhf_receiver.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.Adapters.LoadedTableAdapter;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.LoadedTable;
import com.atstrack.ats.ats_vhf_receiver.R;

import java.util.List;

public class Dialogs {
    public static AlertDialog createDisconnectionDialog(Context context, String message, String deviceType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_alert_disconnect, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView disconnect_message = view.findViewById(R.id.tv_disconnect_message);
        ImageView img_receiver = view.findViewById(R.id.img_receiver);
        if (deviceType.equals(ValueCodes.VHF))
            img_receiver.setImageResource(R.drawable.receiver);
        else
            img_receiver.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_blu_track));
        disconnect_message.setText(message);
        dialog.setView(view);
        return dialog;
    }

    public static AlertDialog createLowPowerDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_alert_disconnect, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView disconnect_message = view.findViewById(R.id.tv_disconnect_message);
        ImageView img_receiver = view.findViewById(R.id.img_receiver);
        LinearLayout layout_background_alert = view.findViewById(R.id.layout_background_alert);
        layout_background_alert.setBackground(ContextCompat.getDrawable(context, R.drawable.connected_light));
        img_receiver.setImageResource(R.drawable.receiver);
        disconnect_message.setText(R.string.lbl_vhf_home_low_power);
        dialog.setView(view);
        return dialog;
    }

    public static AlertDialog createLoadingDialog(Context context, String firstMessage) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_alert_loading, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView state_loading_textView = view.findViewById(R.id.tv_state_loading);
        state_loading_textView.setText(firstMessage);
        dialog.setView(view);
        return dialog;
    }

    public static AlertDialog createErrorDialog(Context context, String title, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_alert_error, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        ImageButton close_upload_imageButton = view.findViewById(R.id.ib_close_upload);
        TextView title_alert_textView = view.findViewById(R.id.tv_title_alert);
        TextView message_alert_textView = view.findViewById(R.id.tv_message_alert);
        TextView return_upload_textView = view.findViewById(R.id.tv_return_upload);
        return_upload_textView.setOnClickListener(v -> dialog.dismiss());
        close_upload_imageButton.setOnClickListener(v -> dialog.dismiss());
        title_alert_textView.setText(title);
        message_alert_textView.setText(message);
        dialog.setView(view);
        return dialog;
    }

    public static AlertDialog createFrequenciesDialog(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_alert_frequency, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView state_message_textView = view.findViewById(R.id.tv_state_message);
        state_message_textView.setText(message);
        dialog.setView(view);
        return dialog;
    }

    public static AlertDialog createLoadedFrequenciesDialog(Context context, String title, List<LoadedTable> loadedTables, boolean isRemoved) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_alert_loaded_frequencies, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        ImageButton close_upload_imageButton = view.findViewById(R.id.ib_close_loaded_frequencies);
        TextView title_frequencies_loaded_textView = view.findViewById(R.id.tv_title_frequencies_loaded);
        ListView item_listView = view.findViewById(R.id.lv_item);
        item_listView.setDividerHeight(0);
        close_upload_imageButton.setOnClickListener(v -> dialog.dismiss());
        title_frequencies_loaded_textView.setText(title);
        LoadedTableAdapter loadedTableAdapter = new LoadedTableAdapter(context, loadedTables, isRemoved);
        item_listView.setAdapter(loadedTableAdapter);
        dialog.setView(view);
        return dialog;
    }

    public static AlertDialog createEmptyTableDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_alert_empty_table, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        ImageButton close_update_imageButton = view.findViewById(R.id.ib_close);
        close_update_imageButton.setOnClickListener(view1 -> dialog.dismiss());
        dialog.setView(view);
        return dialog;
    }

    public static AlertDialog createDetectionFilterDialog(Context context, DetectionFilter detectionFilter) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_alert_detection_filter, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView filter_type_textView = view.findViewById(R.id.tv_filter_type);
        TextView number_matches_textView = view.findViewById(R.id.tv_number_matches);
        TextView pulse_rate_1_textView = view.findViewById(R.id.tv_pulse_rate_1);
        TextView pulse_rate_2_textView = view.findViewById(R.id.tv_pulse_rate_2);
        TextView period_1_tolerance_textView = view.findViewById(R.id.tv_period_1_tolerance);
        TextView period_2_tolerance_textView = view.findViewById(R.id.tv_period_2_tolerance);
        TextView optional_data_calculation_textView = view.findViewById(R.id.tv_optional_data_calculation);
        if (detectionFilter != null) {
            filter_type_textView.setText(Converters.getDetectionFilter(detectionFilter.detectionType));
            number_matches_textView.setText(String.valueOf(detectionFilter.matches));
            pulse_rate_1_textView.setText(String.valueOf(detectionFilter.pulseRate1));
            period_1_tolerance_textView.setText(String.valueOf(detectionFilter.pulseRateTolerance1));

            if (detectionFilter.detectionType == ValueCodes.FIXED) {
                pulse_rate_2_textView.setText(String.valueOf(detectionFilter.pulseRate2));
                period_2_tolerance_textView.setText(String.valueOf(detectionFilter.pulseRateTolerance2));
                TextView calculation_description_textView = view.findViewById(R.id.tv_calculation_description);
                calculation_description_textView.setVisibility(View.GONE);
                optional_data_calculation_textView.setVisibility(View.GONE);
            } else { // Variable Pulse Rate
                TextView pr_description_textView = view.findViewById(R.id.tv_pr_description);
                TextView pr_tolerance_description_textView = view.findViewById(R.id.tv_pr_tolerance_description);
                TextView pr2_description_textView = view.findViewById(R.id.tv_pr2_description);
                TextView pr2_tolerance_description_textView = view.findViewById(R.id.tv_pr2_tolerance_description);
                pr2_tolerance_description_textView.setVisibility(View.GONE);
                pr2_description_textView.setVisibility(View.GONE);
                pulse_rate_2_textView.setVisibility(View.GONE);
                period_2_tolerance_textView.setVisibility(View.GONE);
                pr_description_textView.setText(R.string.lbl_vhf_detection_max_pulse_rate_ppm);
                pr_tolerance_description_textView.setText(R.string.lbl_vhf_detection_min_pulse_rate_ppm);
                optional_data_calculation_textView.setText(detectionFilter.optionalData == ValueCodes.VARIABLE_TEMPERATURE ? "Yes" : "No");
            }
            ImageButton close = view.findViewById(R.id.ib_close);
            close.setOnClickListener(view1 -> dialog.dismiss());
        }
        dialog.setView(view);
        return dialog;
    }

    public static AlertDialog createAlertDialog(Activity context, String title, String message, boolean finish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        if (finish)
            builder.setPositiveButton("OK", (dialog, which) -> context.finish());
        else
            builder.setPositiveButton("Ok", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}
