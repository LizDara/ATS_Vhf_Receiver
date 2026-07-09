package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FirmwareVersionFragment extends Fragment {
    @BindView(R.id.tv_version_name)
    TextView tv_version_name;

    private Unbinder unbinder;
    private final String latestVersion;
    private final String downloadUrl;

    public FirmwareVersionFragment(String latestVersion, String downloadUrl) {
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
    }

    @OnClick(R.id.btn_begin_update)
    public void onClickBeginUpdate(View v) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new UpdatingFirmwareFragment(downloadUrl))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_firmware_version, container, false);
        unbinder = ButterKnife.bind(this, view);
        Log.i("FIRMWARE UPDATE", "ON CREATE VIEW");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("FIRMWARE UPDATE", "ON VIEW CREATED");
        tv_version_name.setText("Firmware Version " + latestVersion);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }
}
