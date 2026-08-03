package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableToMergeAdapter;
import com.atstrack.ats.ats_vhf_receiver.Adapters.TableToScanAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentTablesScanBinding;

import java.util.ArrayList;

public class TablesScanFragment extends Fragment {
    private FragmentTablesScanBinding binding = null;
    private BaseAdapter tableAdapter;
    private final byte[] data;
    private final int type;
    private ArrayList<Integer> tables;

    public TablesScanFragment(byte[] data, int type) {
        this.data = data;
        this.type = type;
    }

    public TablesScanFragment(byte[] data, int type, ArrayList<Integer> tables) {
        this.data = data;
        this.type = type;
        this.tables = tables;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTablesScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnMergeTables.setOnClickListener(v -> setMergeTable());
        setTables();
        if (type == ValueCodes.STATIONARY_DEFAULTS_COMMAND)
            binding.btnMergeTables.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setMergeTable() {
        byte[] b = new byte[]{ValueCodes.SCAN_FREQUENCIES_NUMBER_COMMAND, (byte) ((TableToMergeAdapter) tableAdapter).getTableNumber()};
        boolean result = TransferBleData.writeScanning(b);
        if (result) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ValueCodes.VALUE, true);
            getParentFragmentManager().setFragmentResult(ValueCodes.TABLE, bundle);
            if (getParentFragmentManager() != null) {
                Fragment fragment1 = getParentFragmentManager().findFragmentByTag(String.valueOf(ValueCodes.THIRD_STEP));
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .remove(this)
                        .show(fragment1)
                        .commit();
                getParentFragmentManager().popBackStack();
            }
        }
    }

    private void setTables() {
        if (type == ValueCodes.MOBILE_SCAN_COMMAND) {
            ArrayList<Integer> frequencies = new ArrayList<>();
            ArrayList<Integer> tables = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                if (data[i] != ValueCodes.NONE && data[i] != ValueCodes.NULL) {
                    frequencies.add(Byte.toUnsignedInt(data[i]));
                    tables.add(i);
                }
            }
            tableAdapter = new TableToMergeAdapter(requireContext(), tables, frequencies, binding.btnMergeTables);
        } else {
            tableAdapter = new TableToScanAdapter(requireContext(), data, tables, binding.tvOptionTables, binding.btnMergeTables);
        }
        binding.includeListView.lvItem.setAdapter(tableAdapter);
        binding.tvOptionTables.setText(type == ValueCodes.MOBILE_SCAN_COMMAND ? getString(R.string.lbl_vhf_mobile_select_table_merge) : tables.size() + " Selected Tables (3 Max)");
    }
}
