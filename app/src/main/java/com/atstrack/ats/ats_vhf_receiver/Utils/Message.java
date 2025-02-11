package com.atstrack.ats.ats_vhf_receiver.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.MainActivity;
import com.atstrack.ats.ats_vhf_receiver.R;

public class Message {
    public static void showDisconnectionMessage(Context context, int status) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setView(view);
        dialog.show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }, ValueCodes.DISCONNECTION_MESSAGE_PERIOD);
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
}
