package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentWarningMessageBinding;

public class WarningMessageFragment extends Fragment {
    private FragmentWarningMessageBinding binding = null;
    private final int parameter;
    private final byte[] data;

    public WarningMessageFragment(int parameter, byte[] data) {
        this.parameter = parameter;
        this.data = data;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWarningMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnGo.setOnClickListener(v -> {
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
        });
        setVisibility(parameter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.DETECTION_FILTER_COMMAND) {
            binding.tvWarningMessage.setText(R.string.lbl_vhf_start_scan_warn_no_detection);
            binding.btnGo.setText(R.string.btn_vhf_start_scan_go_detection);
        } else if (view == ValueCodes.TABLES_COMMAND) {
            binding.tvWarningMessage.setText(R.string.lbl_vhf_start_scan_warn_no_tables);
            binding.btnGo.setText(R.string.btn_vhf_start_scan_go_tables);
        } else if (view == ValueCodes.MOBILE_DEFAULTS_COMMAND || view == ValueCodes.STATIONARY_DEFAULTS_COMMAND) {
            binding.tvWarningMessage.setText(R.string.lbl_vhf_start_scan_warn_no_defaults);
            binding.btnGo.setText(R.string.btn_vhf_start_scan_go_settings);
        } else if (view == ValueCodes.DOWNLOAD) {
            binding.tvWarningMessage.setText(R.string.lbl_vhf_data_download_note);
            binding.btnGo.setText(R.string.btn_vhf_data_begin_download);
        } else if (view == ValueCodes.DELETE) {
            binding.tvWarningMessage.setText(R.string.lbl_vhf_data_delete_confirm_msg);
            binding.btnGo.setText(R.string.title_vhf_data_delete);
            binding.btnGo.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_stop));
        }
    }
}
