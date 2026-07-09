package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.DetectionFilterActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.MobileDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.StationaryDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.TablesActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class WarningMessageFragment extends Fragment {
    @BindView(R.id.tv_warning_message)
    TextView tv_warning_message;
    @BindView(R.id.btn_go)
    Button btn_go;

    private Unbinder unbinder;
    private final int parameter;
    private final byte[] data;

    public WarningMessageFragment(int parameter, byte[] data) {
        this.parameter = parameter;
        this.data = data;
    }

    @OnClick(R.id.btn_go)
    public void onClickGo(View v) {
        if (data != null) {
            Intent intent;
            if (parameter == ValueCodes.DETECTION_FILTER_COMMAND) {
                intent = new Intent(requireContext(), DetectionFilterActivity.class);
            } else if (parameter == ValueCodes.TABLES_COMMAND) {
                intent = new Intent(requireContext(), TablesActivity.class);
            } else {
                if (parameter == ValueCodes.MOBILE_DEFAULTS_COMMAND) {
                    intent = new Intent(requireContext(), MobileDefaultsActivity.class);
                } else {
                    intent = new Intent(requireContext(), StationaryDefaultsActivity.class);
                }
            }
            intent.putExtra(ValueCodes.VALUE, data);
            startActivity(intent);
            getParentFragmentManager().popBackStack();
        } else {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment,
                                parameter == ValueCodes.DOWNLOAD ? new DownloadingDataFragment() : new LoadingFragment(), String.valueOf(ValueCodes.THIRD_STEP))
                        .addToBackStack(String.valueOf(ValueCodes.SECOND_STEP))
                        .commit();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_warning_message, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setVisibility(parameter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.DETECTION_FILTER_COMMAND) {
            tv_warning_message.setText(R.string.lb_warning_no_detection);
            btn_go.setText(R.string.lb_go_detection);
        } else if (view == ValueCodes.TABLES_COMMAND) {
            tv_warning_message.setText(R.string.lb_warning_no_tables);
            btn_go.setText(R.string.lb_go_tables);
        } else if (view == ValueCodes.MOBILE_DEFAULTS_COMMAND || view == ValueCodes.STATIONARY_DEFAULTS_COMMAND) {
            tv_warning_message.setText(R.string.lb_warning_no_defaults);
            btn_go.setText(R.string.lb_go_settings);
        } else if (view == ValueCodes.DOWNLOAD) {
            tv_warning_message.setText(R.string.lb_begin_download_note);
            btn_go.setText(R.string.lb_begin_download);
        } else if (view == ValueCodes.DELETE) {
            tv_warning_message.setText(R.string.lb_delete_all);
            btn_go.setText(R.string.lb_delete_receiver_data);
            btn_go.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_stop));
        }
    }
}
