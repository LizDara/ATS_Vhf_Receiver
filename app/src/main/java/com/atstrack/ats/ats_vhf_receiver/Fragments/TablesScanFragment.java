package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableToMergeAdapter;
import com.atstrack.ats.ats_vhf_receiver.Adapters.TableToScanAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TablesScanFragment extends Fragment {
    @BindView(R.id.tv_option_tables)
    TextView tv_option_tables;
    @BindView(R.id.lv_item)
    ListView lv_item;
    @BindView(R.id.btn_merge_tables)
    Button btn_merge_tables;

    private Unbinder unbinder;
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

    @OnClick(R.id.btn_merge_tables)
    public void onClickMergeTables(View v) {
        setMergeTable();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tables_scan, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTables();
        if (type == ValueCodes.STATIONARY_DEFAULTS_COMMAND)
            btn_merge_tables.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
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
            tableAdapter = new TableToMergeAdapter(requireContext(), tables, frequencies, btn_merge_tables);
        } else {
            tableAdapter = new TableToScanAdapter(requireContext(), data, tables, tv_option_tables, btn_merge_tables);
        }
        lv_item.setAdapter(tableAdapter);
        tv_option_tables.setText(type == ValueCodes.MOBILE_SCAN_COMMAND ? getString(R.string.lb_select_table_merge) : tables.size() + " Selected Tables (3 Max)");
    }
}
