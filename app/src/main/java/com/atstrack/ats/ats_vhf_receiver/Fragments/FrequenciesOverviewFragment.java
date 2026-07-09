package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyAdapter;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnAdapterClickListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnDialogCreatedListener;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.FrequenciesActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FrequenciesOverviewFragment extends Fragment implements ReceiverCallback, OnAdapterClickListener {
    @BindView(R.id.rv_item)
    RecyclerView rv_item;

    private Unbinder unbinder;
    private final FrequencyAdapter frequencyAdapter;
    private ActivityResultLauncher<Intent> launcher;
    private int newFrequency = 0;

    public FrequenciesOverviewFragment(FrequencyAdapter frequencyAdapter) {
        this.frequencyAdapter = frequencyAdapter;
        initializeLauncher();
    }

    public FrequenciesOverviewFragment(FrequencyAdapter frequencyAdapter, int newFrequency) {
        this.frequencyAdapter = frequencyAdapter;
        this.newFrequency = newFrequency;
        initializeLauncher();
    }

    @OnClick(R.id.btn_frequencies)
    public void onClickDeleteFrequencies(View v) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new DeleteFrequenciesFragment(frequencyAdapter), String.valueOf(ValueCodes.SECOND_STEP))
                    .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }
    }

    @OnClick(R.id.btn_frequency)
    public void onClickAddFrequency(View v) {
        if (isWithinLimit()) {
            if (frequencyAdapter.isTemperature) {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(this)
                            .add(R.id.fcv_activity_fragment, new TemperatureFrequencyFragment(-1, frequencyAdapter), String.valueOf(ValueCodes.SECOND_STEP))
                            .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                            .commit();
                }
            } else {
                Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
                intent.putExtra(ValueCodes.TITLE, "Add Frequency");
                intent.putExtra(ValueCodes.POSITION, -1);
                intent.putExtra(ValueCodes.BASE_FREQUENCY, frequencyAdapter.baseFrequency);
                intent.putExtra(ValueCodes.RANGE, frequencyAdapter.range);
                launcher.launch(intent);
            }
        } else {
            showAlertDialog();
        }
    }

    @OnClick(R.id.tv_view_tables)
    public void onClickViewTables(View v) {
        if (getActivity() != null)
            getActivity().finish();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frequencies_overview, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        frequencyAdapter.setContext(requireContext());
        frequencyAdapter.launcher = launcher;
        frequencyAdapter.adapterClickListener = this;
        rv_item.setAdapter(frequencyAdapter);
        rv_item.setLayoutManager(new LinearLayoutManager(frequencyAdapter.context));

        if (newFrequency > 0) {
            addFrequency(newFrequency);
            newFrequency = 0;
        }

        getParentFragmentManager().setFragmentResultListener(ValueCodes.IS_TEMPERATURE, this, (requestKey, result) -> {
            if (!isAdded() || getView() == null) return;
            if (frequencyAdapter.isTemperature) {
                int position = result.getInt(ValueCodes.POSITION, -1);
                int frequency = result.getInt(ValueCodes.VALUE, 0);
                if (position > -1)
                    changeSelectedFrequency(frequency, position);
                else
                    addFrequency(frequency);
            }
        });

        getParentFragmentManager().setFragmentResultListener(ValueCodes.VALUE, this, (requestKey, result) -> {
            if (!isAdded() || getView() == null) return;
            boolean deleted = result.getBoolean(ValueCodes.VALUE, false);
            if (deleted) {
                setToolbarTitle();
                showAlertDialog(getString(R.string.lb_frequencies_deleted));
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            frequencyAdapter.notifyDataSetChanged();
            setToolbarTitle();
        }
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
                if (Byte.toUnsignedInt(packet[0]) == frequencyAdapter.tableNumber)
                    downloadFrequencies(packet);
            }
        });
    }

    private void downloadFrequencies(byte[] data) {
        ArrayList<Integer> frequencies = new ArrayList<>();
        int index = 10;
        int i = 0;
        while (i < frequencyAdapter.originalTable.length) {
            int frequency = (Byte.toUnsignedInt(data[index]) * 256) + Byte.toUnsignedInt(data[index + 1]);
            frequencyAdapter.originalTable[i] = frequencyAdapter.baseFrequency + frequency;
            frequencies.add(frequencyAdapter.originalTable[i]);
            i++;
            index += 2;
        }
        frequencyAdapter.frequencies = frequencies;
        frequencyAdapter.notifyDataSetChanged();
        setToolbarTitle();
    }

    private boolean isWithinLimit() {
        return frequencyAdapter.getItemCount() < 100;
    }

    private void setToolbarTitle() {
        if (getActivity() instanceof FrequenciesActivity) {
            ((FrequenciesActivity) getActivity()).setToolbarTitle("Table " + frequencyAdapter.tableNumber + " (" + frequencyAdapter.getItemCount() + " Frequencies)");
        }
    }

    private void initializeLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (ValueCodes.CANCELLED == result.getResultCode())
                        return;
                    if (ValueCodes.RESULT_OK == result.getResultCode()) {
                        int position = result.getData().getIntExtra(ValueCodes.POSITION, 0);
                        if (position > -2) {
                            int frequency = result.getData().getIntExtra(ValueCodes.VALUE, 0);
                            if (!frequencyAdapter.isTemperature) { //Save frequency in the list
                                if (position != -1) { // 0 ... n edited frequency position
                                    if (frequencyAdapter.frequencies.get(position) != frequency)
                                        changeSelectedFrequency(frequency, position);
                                } else { // new frequency
                                    addFrequency(frequency);
                                }
                            }
                        }
                    }
                });
    }

    private void changeSelectedFrequency(int frequency, int position) {
        frequencyAdapter.frequencies.set(position, frequency);
        frequencyAdapter.notifyDataSetChanged();

        showAlertDialog(getString(R.string.lb_frequency_saved));
    }

    private void addFrequency(int frequency) {
        frequencyAdapter.frequencies.add(frequency);
        frequencyAdapter.notifyDataSetChanged();
        setToolbarTitle();

        showAlertDialog(getString(R.string.lb_frequency_added));
    }

    private void showAlertDialog() {
        AlertDialog dialog = Dialogs.createAlertDialog(requireActivity(), "Error", "Exceeded table limit. Please enter no more than 100 frequencies.", false);
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
    }

    private void showAlertDialog(String message) {
        AlertDialog dialog = Dialogs.createFrequenciesDialog(requireContext(), message);
        if (getActivity() instanceof OnDialogCreatedListener) {
            ((OnDialogCreatedListener) getActivity()).onNewDialogAdded(dialog);
        }
        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && getView() != null) {
                dialog.dismiss();
                if (frequencyAdapter.getItemCount() == 0) {
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.fcv_activity_fragment, new EmptyTableFragment(frequencyAdapter), String.valueOf(ValueCodes.FIRST_STEP))
                                .commit();
                    }
                }
            }
        }, ValueCodes.MESSAGE_PERIOD);
    }

    @Override
    public void onAdapterItemClickListener(int position) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new TemperatureFrequencyFragment(position, frequencyAdapter), String.valueOf(ValueCodes.SECOND_STEP))
                    .addToBackStack(String.valueOf(ValueCodes.FIRST_STEP))
                    .commit();
        }
    }
}
