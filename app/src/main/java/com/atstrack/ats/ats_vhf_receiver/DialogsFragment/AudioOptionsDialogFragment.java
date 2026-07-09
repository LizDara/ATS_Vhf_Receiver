package com.atstrack.ats.ats_vhf_receiver.DialogsFragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class AudioOptionsDialogFragment extends DialogFragment {
    public static String TAG = AudioOptionsDialogFragment.class.getSimpleName();
    private byte audioOption;
    private int codeNumber;

    public AudioOptionsDialogFragment() {}

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment AudioOptions.
     */
    public static AudioOptionsDialogFragment newInstance() {
        return new AudioOptionsDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioOption = (byte) 0x5A;
        codeNumber = 0;
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
        View view = inflater.inflate(R.layout.dialog_form_audio_options, container, false);
        ImageButton close = view.findViewById(R.id.ib_close);
        Button single = view.findViewById(R.id.btn_single);
        Button all = view.findViewById(R.id.btn_all);
        Button background = view.findViewById(R.id.btn_background);
        LinearLayout enterDigit = view.findViewById(R.id.layout_enter_digit);
        TextView number = view.findViewById(R.id.tv_digit);
        Button one = view.findViewById(R.id.btn_one);
        Button two = view.findViewById(R.id.btn_two);
        Button three = view.findViewById(R.id.btn_three);
        Button four = view.findViewById(R.id.btn_four);
        Button five = view.findViewById(R.id.btn_five);
        Button six = view.findViewById(R.id.btn_six);
        Button seven = view.findViewById(R.id.btn_seven);
        Button eight = view.findViewById(R.id.btn_eight);
        Button nine = view.findViewById(R.id.btn_nine);
        Button zero = view.findViewById(R.id.btn_zero);
        ImageView delete = view.findViewById(R.id.img_delete);
        Button saveChanges = view.findViewById(R.id.btn_save_digit);
        close.setOnClickListener(view1 -> dismiss());
        single.setOnClickListener(v14 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_audio));
            single.setTextColor(ContextCompat.getColor(view.getContext(), R.color.catskill_white));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_tertiary));
            all.setTextColor(ContextCompat.getColor(view.getContext(), R.color.limed_spruce));
            background.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_tertiary));
            background.setTextColor(ContextCompat.getColor(view.getContext(), R.color.limed_spruce));
            enterDigit.setVisibility(View.VISIBLE);
            number.setText("");
            audioOption = ValueCodes.AUDIO_ONE_COMMAND;
        });
        all.setOnClickListener(v15 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_tertiary));
            single.setTextColor(ContextCompat.getColor(view.getContext(), R.color.limed_spruce));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_audio));
            all.setTextColor(ContextCompat.getColor(view.getContext(), R.color.catskill_white));
            background.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_tertiary));
            background.setTextColor(ContextCompat.getColor(view.getContext(), R.color.limed_spruce));
            enterDigit.setVisibility(View.GONE);
            audioOption = ValueCodes.AUDIO_ALL_COMMAND;
        });
        background.setOnClickListener(v16 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_tertiary));
            single.setTextColor(ContextCompat.getColor(view.getContext(), R.color.limed_spruce));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_tertiary));
            all.setTextColor(ContextCompat.getColor(view.getContext(), R.color.limed_spruce));
            background.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.button_audio));
            background.setTextColor(ContextCompat.getColor(view.getContext(), R.color.catskill_white));
            enterDigit.setVisibility(View.GONE);
            audioOption = ValueCodes.AUDIO_BACKGROUND_COMMAND;
        });
        View.OnClickListener clickListener = v17 -> {
            String text = number.getText().toString();
            Button buttonNumber = (Button) v17;
            number.setText(text + buttonNumber.getText());
        };
        one.setOnClickListener(clickListener);
        two.setOnClickListener(clickListener);
        three.setOnClickListener(clickListener);
        four.setOnClickListener(clickListener);
        five.setOnClickListener(clickListener);
        six.setOnClickListener(clickListener);
        seven.setOnClickListener(clickListener);
        eight.setOnClickListener(clickListener);
        nine.setOnClickListener(clickListener);
        zero.setOnClickListener(clickListener);
        delete.setOnClickListener(v13 -> {
            if (!number.getText().toString().isEmpty()) {
                String text = number.getText().toString();
                number.setText(text.substring(0, text.length() - 1));
            }
        });
        saveChanges.setOnClickListener(view1 -> {
            codeNumber = audioOption == ValueCodes.AUDIO_ONE_COMMAND ? (byte) Integer.parseInt(number.getText().toString()) : 0;
            Bundle bundle = new Bundle();
            bundle.putByteArray(ValueCodes.VALUE, new byte[] {audioOption, (byte) codeNumber, 0x0});
            getParentFragmentManager().setFragmentResult(ValueCodes.VALUE, bundle);
            dismiss();
        });
        return view;
    }

    @Override
    public int show(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        return 100;
    }
}