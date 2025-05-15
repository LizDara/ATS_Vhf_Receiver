package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class FirmwareUpdate extends DialogFragment {
    public static String TAG = FirmwareUpdate.class.getSimpleName();

    public FirmwareUpdate() {}

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment AudioOptions.
     */
    public static FirmwareUpdate newInstance() {
        return new FirmwareUpdate();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
        int width = sharedPreferences.getInt(ValueCodes.WIDTH, 0);
        getDialog().getWindow().setLayout((width / 18) * 17, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_firmware_update, container, false);
        ImageButton close_update_imageButton = view.findViewById(R.id.close_update_imageButton);
        TextView dismiss_textView = view.findViewById(R.id.dismiss_textView);
        Button update_firmware_button = view.findViewById(R.id.update_firmware_button);
        close_update_imageButton.setOnClickListener(view1 -> dismiss());
        dismiss_textView.setOnClickListener(view1 -> dismiss());
        update_firmware_button.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ValueCodes.VALUE, true);
            getParentFragmentManager().setFragmentResult(ValueCodes.UPDATE, bundle);
            dismiss();
        });
        return view;
    }
}
