package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class EditTableScanFragment extends Fragment implements ReceiverCallback {
    @BindView(R.id.tv_current_frequency_mobile)
    TextView tv_current_frequency_mobile;
    @BindView(R.id.tv_current_index_mobile)
    TextView tv_current_index_mobile;
    @BindView(R.id.tv_table_total_mobile)
    TextView tv_table_total_mobile;

    private Unbinder unbinder;
    private final int baseFrequency;
    private final int range;
    private final int index;
    private final int currentFrequency;
    private int total;
    private ActivityResultLauncher<Intent> launcher;
    private byte[] tables;

    public EditTableScanFragment(int baseFrequency, int range, int index, int currentFrequency, int total) {
        this.baseFrequency = baseFrequency;
        this.range = range;
        this.index = index;
        this.currentFrequency = currentFrequency;
        this.total = total;
        initializeLauncher();
    }

    @OnClick(R.id.btn_add_frequency_scan)
    public void onClickAddFrequencyScan(View v) {
        Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, getString(R.string.lb_add_frequency_scan));
        intent.putExtra(ValueCodes.POSITION, -1);
        intent.putExtra(ValueCodes.BASE_FREQUENCY, baseFrequency);
        intent.putExtra(ValueCodes.RANGE, range);
        launcher.launch(intent);
    }

    @OnClick(R.id.btn_delete_frequency_scan)
    public void onClickDeleteFrequencyScan(View v) {
        setDeleteFrequency();
    }

    @OnClick(R.id.btn_merge_table_scan)
    public void onClickMergeTableScan(View v) {
        if (tables == null) {
            TransferBleData.readTables();
        } else {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment, new TablesScanFragment(tables, ValueCodes.MOBILE_SCAN_COMMAND), String.valueOf(ValueCodes.FOURTH_STEP))
                        .addToBackStack(String.valueOf(ValueCodes.THIRD_STEP))
                        .commit();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_table_scan, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_current_frequency_mobile.setText(String.valueOf(currentFrequency));
        tv_current_index_mobile.setText(String.valueOf(index));
        tv_table_total_mobile.setText(String.valueOf(total));

        getParentFragmentManager().setFragmentResultListener(ValueCodes.TABLE, this, (requestKey, result) -> {
            if (!isAdded() || getView() == null) return;
            boolean tableMerged = result.getBoolean(ValueCodes.VALUE, false);
            if (tableMerged) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(ValueCodes.VALUE, true);
                getParentFragmentManager().setFragmentResult(ValueCodes.TOTAL, bundle);
                if (getParentFragmentManager() != null) {
                    Fragment fragment1 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.SECOND_STEP));
                    getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .remove(this)
                            .show(fragment1)
                            .commit();
                    getParentFragmentManager().popBackStack();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (ValueCodes.CANCELLED == result.getResultCode())
                    return;
                int value = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                if (ValueCodes.RESULT_OK == result.getResultCode()) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded() && getView() != null) {
                            setNewFrequency(value);
                        }
                    }, ValueCodes.WAITING_PERIOD);
                }
            });
    }

    private void setNewFrequency(int newFrequency) {
        byte[] b = new byte[] {(byte) 0x5D, (byte) ((newFrequency - baseFrequency) / 256), (byte) ((newFrequency - baseFrequency) % 256)};
        boolean result = TransferBleData.writeScanning(b);
        if (result)
            showAlertDialog(getString(R.string.lb_frequency_added));
    }

    private void setDeleteFrequency() {
        byte[] b = new byte[] {(byte) 0x5C, (byte) (index / 256), (byte) (index % 256)};
        boolean result = TransferBleData.writeScanning(b);
        if (result)
            showAlertDialog(getString(R.string.lb_frequency_deleted));
    }

    private void showAlertDialog(String message) {
        AlertDialog dialog = Dialogs.createFrequenciesDialog(requireContext(), message);
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && getView() != null)
                dialog.dismiss();
        }, ValueCodes.MESSAGE_PERIOD);
    }

    @Override
    public void onGattDisconnected() {}

    @Override
    public void onGattDiscovered() {}

    @Override
    public void onGattDataAvailable(byte[] packet) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null) {
                if (packet[0] == ValueCodes.TABLES_COMMAND)
                    downloadTables(packet);
                else if (packet[0] == ValueCodes.SCAN_FREQUENCIES_NUMBER_COMMAND)
                    frequenciesNumber(packet);
            }
        });
    }

    private void downloadTables(byte[] data) {
        tables = data;
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new TablesScanFragment(tables, ValueCodes.MOBILE_SCAN_COMMAND), String.valueOf(ValueCodes.FOURTH_STEP))
                    .addToBackStack(String.valueOf(ValueCodes.THIRD_STEP))
                    .commit();
        }
    }

    private void frequenciesNumber(byte[] data) {
        total = (Byte.toUnsignedInt(data[1]) * 256) + Byte.toUnsignedInt(data[2]);
        tv_table_total_mobile.setText(String.valueOf(total));
    }
}
