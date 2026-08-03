package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentSuccessfulMessageBinding;

public class SuccessfulMessageFragment extends Fragment {
    private FragmentSuccessfulMessageBinding binding = null;
    private final int parameter;

    public SuccessfulMessageFragment(int parameter) {
        this.parameter = parameter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSuccessfulMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnMainComplete.setOnClickListener(v -> {
            if (parameter == ValueCodes.DELETE) {
                if (getParentFragmentManager() != null) {
                    Fragment fragment1 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.FIRST_STEP));
                    Fragment fragment2 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.SECOND_STEP));
                    Fragment fragment3 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.THIRD_STEP));
                    getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    if (fragment3 != null) transaction.remove(fragment3);
                    if (fragment2 != null) transaction.remove(fragment2);
                    transaction.remove(this);
                    if (fragment1 != null)
                        transaction.show(fragment1);
                    transaction.commit();
                }
            }
        });
        binding.tvReturn.setOnClickListener(v -> {
            if (parameter == ValueCodes.DOWNLOAD) {
                if (getParentFragmentManager() != null) {
                    Fragment fragment1 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.FIRST_STEP));
                    Fragment fragment2 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.SECOND_STEP));
                    Fragment fragment3 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.THIRD_STEP));
                    getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    if (fragment3 != null) transaction.remove(fragment3);
                    if (fragment2 != null) transaction.remove(fragment2);
                    transaction.remove(this);
                    if (fragment1 != null)
                        transaction.show(fragment1);
                    transaction.commit();
                }
            }
        });
        setVisibility(parameter);
        if (parameter == ValueCodes.UPDATE) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded() && getView() != null)
                    LeServiceConnection.getInstance().getBluetoothLeService().disconnect();
            }, ValueCodes.MESSAGE_PERIOD);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.UPDATE) {
            binding.tvMessageComplete.setText(R.string.lbl_fw_update_success_complete);
            binding.btnMainComplete.setText(R.string.btn_fw_update_return_device);
            binding.tvReturn.setVisibility(View.GONE);
        } else if (view == ValueCodes.DELETE) {
            binding.tvMessageComplete.setText(R.string.lbl_vhf_data_success_delete);
            binding.btnMainComplete.setText(R.string.btn_fw_update_return_device);
            binding.tvReturn.setVisibility(View.GONE);
        } else if (view == ValueCodes.DOWNLOAD) {
            binding.tvMessageComplete.setText(R.string.lbl_vhf_data_success_download);
            binding.btnMainComplete.setText(R.string.btn_vhf_data_open_file);
            binding.tvReturn.setVisibility(View.VISIBLE);
        }
    }
}
