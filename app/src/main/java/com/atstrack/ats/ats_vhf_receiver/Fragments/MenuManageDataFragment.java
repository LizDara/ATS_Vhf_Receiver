package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.Snapshots;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MenuManageDataFragment extends Fragment implements ReceiverCallback {
    @BindView(R.id.tv_memory_used_percent)
    TextView tv_memory_used_percent;
    @BindView(R.id.pb_memory_used)
    ProgressBar pb_memory_used;
    @BindView(R.id.tv_bytes_stored)
    TextView tv_bytes_stored;

    private Unbinder unbinder;

    @OnClick(R.id.btn_download_data)
    public void onClickDownloadData(View v) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new WarningMessageFragment(ValueCodes.DOWNLOAD, null), String.valueOf(ValueCodes.SECOND_STEP))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @OnClick(R.id.btn_erase_data)
    public void onClickEraseData(View v) {
        if (!tv_bytes_stored.getText().toString().contains("(0 bytes")) {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment, new WarningMessageFragment(ValueCodes.DELETE, null), String.valueOf(ValueCodes.SECOND_STEP))
                        .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                        .commit();
            }
        } else {
            showAlertDialog();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_manage_data, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            TransferBleData.readDataInfo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                if (packet[0] == ValueCodes.STORAGE_COMMAND) {
                    if (packet.length < 230)
                        downloadTest(packet);
                }
            }
        });
    }

    private void downloadTest(byte[] data) {
        int numberPage = Converters.findPageNumber(new byte[]{data[4], data[3], data[2], data[1]}) * 2;
        int lastPage = Converters.findPageNumber(new byte[]{data[8], data[7], data[6], data[5]});
        tv_memory_used_percent.setText(((int) (((float) numberPage / (float) lastPage) * 100)) + "%");
        pb_memory_used.setProgress((int) ((((float) numberPage / (float) lastPage)) * 100));
        tv_bytes_stored.setText("Memory Used (" + (numberPage * Snapshots.BYTES_PER_PAGE) + " bytes stored)");
    }

    private void showAlertDialog() {
        AlertDialog dialog = Dialogs.createAlertDialog(requireActivity(), "Erase Data", "There is no data to delete.", false);
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
    }
}
