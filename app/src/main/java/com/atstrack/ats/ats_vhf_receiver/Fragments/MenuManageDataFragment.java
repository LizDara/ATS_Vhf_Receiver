package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentMenuManageDataBinding;

public class MenuManageDataFragment extends Fragment implements ReceiverCallback {
    private FragmentMenuManageDataBinding binding = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuManageDataBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnDownloadData.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment, new WarningMessageFragment(ValueCodes.DOWNLOAD, null), String.valueOf(ValueCodes.SECOND_STEP))
                        .addToBackStack(null)
                        .commit();
            }
        });
        binding.btnEraseData.setOnClickListener(v -> {
            if (!binding.tvBytesStored.getText().toString().contains("(0 bytes")) {
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
        });
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
        binding = null;
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
        binding.tvMemoryUsedPercent.setText(((int) (((float) numberPage / (float) lastPage) * 100)) + "%");
        binding.pbMemoryUsed.setProgress((int) ((((float) numberPage / (float) lastPage)) * 100));
        binding.tvBytesStored.setText("Memory Used (" + (numberPage * Snapshots.BYTES_PER_PAGE) + " bytes stored)");
    }

    private void showAlertDialog() {
        AlertDialog dialog = Dialogs.createAlertDialog(requireActivity(), "Erase Data", "There is no data to delete.", false);
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
    }
}
