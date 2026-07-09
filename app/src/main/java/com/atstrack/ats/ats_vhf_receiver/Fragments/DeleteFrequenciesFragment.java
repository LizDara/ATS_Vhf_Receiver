package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyAdapter;
import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyToDeleteAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.MobileDefaults;
import com.atstrack.ats.ats_vhf_receiver.Models.StationaryDefaults;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.FrequenciesActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.MobileDefaultsActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.StationaryDefaultsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DeleteFrequenciesFragment extends Fragment implements ReceiverCallback {
    @BindView(R.id.lv_item)
    ListView lv_item;
    @BindView(R.id.cb_all_frequencies)
    CheckBox cb_all_frequencies;
    @BindView(R.id.btn_delete_frequencies)
    Button btn_delete_frequencies;

    private Unbinder unbinder;
    private final FrequencyAdapter frequencyAdapter;
    private FrequencyToDeleteAdapter frequencyToDeleteAdapter;
    private MobileDefaults mobileDefaults;
    private StationaryDefaults stationaryDefaults;

    public DeleteFrequenciesFragment(FrequencyAdapter frequencyAdapter) {
        this.frequencyAdapter = frequencyAdapter;
    }

    @OnClick(R.id.btn_delete_frequencies)
    public void onClickDeleteSelectedFrequencies(View v) {
        if (cb_all_frequencies.isChecked())
            TransferBleData.readDefaults(true);
        else
            deleteFrequencies(true);
    }

    @OnCheckedChanged(R.id.cb_all_frequencies)
    public void onCheckedChangeAllFrequencies(CompoundButton button, boolean isChecked) {
        changeAllCheckBox(isChecked);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_frequencies, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbarTitle();
        frequencyToDeleteAdapter = new FrequencyToDeleteAdapter(requireContext(), frequencyAdapter.frequencies, cb_all_frequencies, btn_delete_frequencies);
        lv_item.setAdapter(frequencyToDeleteAdapter);
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
                if (packet[0] == ValueCodes.MOBILE_DEFAULTS_COMMAND)
                    downloadMobileDefaults(packet);
                else if (packet[0] == ValueCodes.STATIONARY_DEFAULTS_COMMAND)
                    downloadStationaryDefaults(packet);
            }
        });
    }

    private void downloadMobileDefaults(byte[] data) {
        if (!Converters.isDefaultEmpty(data))
            mobileDefaults = new MobileDefaults(data);
        else
            mobileDefaults = new MobileDefaults();
        TransferBleData.readDefaults(false);
    }

    private void downloadStationaryDefaults(byte[] data) {
        if (!Converters.isDefaultEmpty(data))
            stationaryDefaults = new StationaryDefaults(frequencyAdapter.baseFrequency, data);
        else
            stationaryDefaults = new StationaryDefaults();
        checkScanTable();
    }

    private void checkScanTable() {
        boolean showMessage = true;
        if (frequencyAdapter.tableNumber == mobileDefaults.tableNumber || frequencyAdapter.tableNumber == stationaryDefaults.firstTableNumber
                || frequencyAdapter.tableNumber == stationaryDefaults.secondTableNumber || frequencyAdapter.tableNumber == stationaryDefaults.thirdTableNumber) {
            frequencyToDeleteAdapter.selected.set(0, false);
            showMessage = false;
            showAlertDialog(frequencyToDeleteAdapter.getCount() > 1);
        }
        deleteFrequencies(showMessage);
        cb_all_frequencies.setChecked(false);
    }

    private void changeAllCheckBox(boolean isChecked) {
        frequencyToDeleteAdapter.setStateSelected(isChecked);
        frequencyToDeleteAdapter.notifyDataSetChanged();

        if (isChecked) {
            btn_delete_frequencies.setEnabled(true);
            btn_delete_frequencies.setAlpha(1);
        } else {
            btn_delete_frequencies.setEnabled(false);
            btn_delete_frequencies.setAlpha((float) 0.6);
        }
    }

    private void deleteFrequencies(boolean showMessage) {
        int index = 0;
        while (index < frequencyToDeleteAdapter.getCount()) {
            if (frequencyToDeleteAdapter.selected.get(index))
                frequencyToDeleteAdapter.removeFrequency(index);
            else
                index++;
        }

        if (showMessage) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ValueCodes.VALUE, true);
            getParentFragmentManager().setFragmentResult(ValueCodes.VALUE, bundle);
            if (getParentFragmentManager() != null) {
                Fragment fragment1 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.FIRST_STEP));
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .remove(this) // Nos removemos a nosotros mismos (Fragment3)
                        .show(fragment1) // Hacemos visible el paso anterior
                        .commit();
                getParentFragmentManager().popBackStack();
            }
        }
    }

    private void setToolbarTitle() {
        if (getActivity() instanceof FrequenciesActivity) {
            ((FrequenciesActivity) getActivity()).setToolbarTitle("Delete Frequencies");
        }
    }

    private void showAlertDialog(boolean deleted) {
        AlertDialog dialog = Dialogs.createEmptyTableDialog(requireContext());
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
        dialog.show();

        Button update_firmware_button = dialog.findViewById(R.id.btn_edit_default);
        TextView dismiss_textView = dialog.findViewById(R.id.tv_dismiss);
        dismiss_textView.setOnClickListener(view1 -> {
            dialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putBoolean(ValueCodes.VALUE, deleted);
            getParentFragmentManager().setFragmentResult(ValueCodes.VALUE, bundle);
            if (getParentFragmentManager() != null) {
                Fragment fragment1 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.FIRST_STEP));
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .remove(this) // Nos removemos a nosotros mismos (Fragment3)
                        .show(fragment1) // Hacemos visible el paso anterior
                        .commit();
                getParentFragmentManager().popBackStack();
            }
        });
        update_firmware_button.setOnClickListener(view1 -> {
            dialog.dismiss();
            Intent intent;
            if (frequencyAdapter.tableNumber == mobileDefaults.tableNumber) {
                intent = new Intent(requireContext(), MobileDefaultsActivity.class);
                intent.putExtra(ValueCodes.VALUE, mobileDefaults.originalBytes);
            } else {
                intent = new Intent(requireContext(), StationaryDefaultsActivity.class);
                intent.putExtra(ValueCodes.VALUE, stationaryDefaults.originalBytes);
            }
            startActivity(intent);
        });
    }
}
