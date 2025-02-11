package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
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
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import static com.atstrack.ats.ats_vhf_receiver.R.color.catskill_white;
import static com.atstrack.ats.ats_vhf_receiver.R.color.limed_spruce;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_tertiary;
import static com.atstrack.ats.ats_vhf_receiver.R.drawable.button_audio;

public class AudioOptions extends DialogFragment {

    public static String TAG = AudioOptions.class.getSimpleName();
    private byte audioOption;
    private int codeNumber;

    public AudioOptions() {}

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment AudioOptions.
     */
    public static AudioOptions newInstance() {
        return new AudioOptions();
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
        View view = inflater.inflate(R.layout.fragment_audio_options, container, false);
        ImageButton close = view.findViewById(R.id.close_imageButton);
        Button single = view.findViewById(R.id.single_button);
        Button all = view.findViewById(R.id.all_button);
        Button none = view.findViewById(R.id.none_button);
        LinearLayout enterDigit = view.findViewById(R.id.enter_digit_linearLayout);
        TextView number = view.findViewById(R.id.digit_textView);
        Button one = view.findViewById(R.id.one_button);
        Button two = view.findViewById(R.id.two_button);
        Button three = view.findViewById(R.id.three_button);
        Button four = view.findViewById(R.id.four_button);
        Button five = view.findViewById(R.id.five_button);
        Button six = view.findViewById(R.id.six_button);
        Button seven = view.findViewById(R.id.seven_button);
        Button eight = view.findViewById(R.id.eight_button);
        Button nine = view.findViewById(R.id.nine_button);
        Button zero = view.findViewById(R.id.zero_button);
        ImageView delete = view.findViewById(R.id.delete_imageView);
        SwitchCompat background = view.findViewById(R.id.play_background_signals_switch);
        Button saveChanges = view.findViewById(R.id.save_digit_button);
        close.setOnClickListener(view1 -> dismiss());
        single.setOnClickListener(v14 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            single.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            all.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            none.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            enterDigit.setVisibility(View.VISIBLE);
            number.setText("");
            audioOption = (byte) 0x59;
        });
        all.setOnClickListener(v15 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            single.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            all.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            none.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            enterDigit.setVisibility(View.GONE);
            audioOption = (byte) 0x5A;
        });
        none.setOnClickListener(v16 -> {
            single.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            single.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            all.setBackground(ContextCompat.getDrawable(view.getContext(), button_tertiary));
            all.setTextColor(ContextCompat.getColor(view.getContext(), limed_spruce));
            none.setBackground(ContextCompat.getDrawable(view.getContext(), button_audio));
            none.setTextColor(ContextCompat.getColor(view.getContext(), catskill_white));
            enterDigit.setVisibility(View.GONE);
            audioOption = (byte) 0x5B;
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
            codeNumber = Converters.getHexValue(audioOption).equals("59") ? (byte) Integer.parseInt(number.getText().toString()) : 0;
            Bundle bundle = new Bundle();
            bundle.putString(ValueCodes.PARAMETER, ValueCodes.AUDIO);
            bundle.putByte(ValueCodes.AUDIO, audioOption);
            bundle.putByte(ValueCodes.BACKGROUND, background.isChecked() ? (byte) 1 : 0);
            bundle.putInt(ValueCodes.VALUE, codeNumber);
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