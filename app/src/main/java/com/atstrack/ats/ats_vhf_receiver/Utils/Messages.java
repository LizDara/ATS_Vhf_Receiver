package com.atstrack.ats.ats_vhf_receiver.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.LoadedFrequenciesAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.MainActivity;
import com.atstrack.ats.ats_vhf_receiver.Models.LoadedTable;
import com.atstrack.ats.ats_vhf_receiver.R;

import java.util.List;

public class Messages {
    public static void showDisconnectionMessage(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setView(view);
        dialog.show();
        LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();

        new Handler().postDelayed(() -> {
            try {
                dialog.dismiss();
                if (leServiceConnection.existConnection())
                    leServiceConnection.close();
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                ((Activity) context).finish();
            } catch (Exception ex) {
                Log.i("Message", ex.getLocalizedMessage());
            }
        }, ValueCodes.MESSAGE_PERIOD);
    }

    public static void showLoadingMessage(Context context, String firstMessage, String secondMessage, Runnable finishedAction, Runnable dismissAction) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.loading_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        ProgressBar loading_progressBar = view.findViewById(R.id.loading_progressBar);
        ImageView loaded_imageView = view.findViewById(R.id.loaded_imageView);
        TextView state_loading_textView = view.findViewById(R.id.state_loading_textView);
        state_loading_textView.setText(firstMessage);
        dialog.setView(view);
        dialog.show();

        new Handler().postDelayed(() -> {
            state_loading_textView.setText(secondMessage);
            loading_progressBar.setVisibility(View.GONE);
            loaded_imageView.setVisibility(View.VISIBLE);
            finishedAction.run();

            new Handler().postDelayed(() -> {
                dialog.dismiss();
                dismissAction.run();
            }, ValueCodes.MESSAGE_PERIOD);
        }, ValueCodes.MESSAGE_PERIOD);
    }

    public static void showErrorMessage(Context context, String title, String message, boolean showOptions, Runnable cancelAction) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.error_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        ImageButton close_upload_imageButton = view.findViewById(R.id.close_upload_imageButton);
        TextView title_alert_textView = view.findViewById(R.id.title_alert_textView);
        TextView message_alert_textView = view.findViewById(R.id.message_alert_textView);
        close_upload_imageButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
        title_alert_textView.setText(title);
        message_alert_textView.setText(message);
        if (showOptions) {
            LinearLayout options_alert_linearLayout = view.findViewById(R.id.options_alert_linearLayout);
            TextView return_upload_textView = view.findViewById(R.id.return_upload_textView);
            Button cancel_upload_button = view.findViewById(R.id.cancel_upload_button);
            options_alert_linearLayout.setVisibility(View.VISIBLE);
            return_upload_textView.setOnClickListener(v -> {
                dialog.dismiss();
            });
            cancel_upload_button.setOnClickListener(v -> {
                dialog.dismiss();
                cancelAction.run();
            });
        }
        dialog.setView(view);
        dialog.show();
    }

    public static void showLoadedFrequenciesMessage(Context context, String title, List<LoadedTable> loadedTables, boolean isRemoved) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.loaded_frequencies_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        ImageButton close_upload_imageButton = view.findViewById(R.id.close_loaded_frequencies_imageButton);
        TextView title_frequencies_loaded_textView = view.findViewById(R.id.title_frequencies_loaded_textView);
        ListView frequencies_loaded_listView = view.findViewById(R.id.frequencies_loaded_listView);

        close_upload_imageButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
        title_frequencies_loaded_textView.setText(title);
        LoadedFrequenciesAdapter loadedFrequenciesAdapter = new LoadedFrequenciesAdapter(context, loadedTables, isRemoved);
        frequencies_loaded_listView.setAdapter(loadedFrequenciesAdapter);

        dialog.setView(view);
        dialog.show();
    }

    public static void showMessage(Activity context, int status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Message!");
        switch (status) {
            case 0:
                builder.setMessage("Completed.");
                builder.setPositiveButton("OK", (dialog, which) -> context.finish());
                break;
            case 1:
                builder.setMessage("Data incorrect.");
                builder.setPositiveButton("OK", null);
                break;
            case 2:
                builder.setMessage("Not completed.");
                builder.setPositiveButton("OK", null);
                break;
            case 3:
                builder.setMessage("Exceeded Table Limit. Please enter no more than 100 frequencies.");
                builder.setPositiveButton("OK", null);
            case 4:
                builder.setMessage("No Message!");
                builder.setPositiveButton("OK", null);
        }
        builder.show();
    }

    public static void showMessage(Activity context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showMessage(Activity context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("ERROR");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", (dialog, which) -> context.finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
