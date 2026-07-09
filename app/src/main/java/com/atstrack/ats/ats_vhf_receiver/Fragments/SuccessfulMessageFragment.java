package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SuccessfulMessageFragment extends Fragment {
    @BindView(R.id.tv_message_complete)
    TextView tv_message_complete;
    @BindView(R.id.btn_main_complete)
    Button btn_main_complete;
    @BindView(R.id.tv_return)
    TextView tv_return;

    private Unbinder unbinder;
    private final int parameter;

    public SuccessfulMessageFragment(int parameter) {
        this.parameter = parameter;
    }

    @OnClick(R.id.btn_main_complete)
    public void onClickMainComplete(View v) {
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
    }

    @OnClick(R.id.tv_return)
    public void onClickReturn(View v) {
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_successful_message, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        if (unbinder != null)
            unbinder.unbind();
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.UPDATE) {
            tv_message_complete.setText(R.string.lb_installation_complete);
            btn_main_complete.setText(R.string.lb_return_device_screen);
            tv_return.setVisibility(View.GONE);
        } else if (view == ValueCodes.DELETE) {
            tv_message_complete.setText(R.string.lb_deletion_complete);
            btn_main_complete.setText(R.string.lb_return_screen);
            tv_return.setVisibility(View.GONE);
        } else if (view == ValueCodes.DOWNLOAD) {
            tv_message_complete.setText(R.string.lb_download_complete);
            btn_main_complete.setText(R.string.lb_open_file);
            tv_return.setVisibility(View.VISIBLE);
        }
    }
}
