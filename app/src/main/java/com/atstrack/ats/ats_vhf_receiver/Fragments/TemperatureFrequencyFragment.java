package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.Adapters.FrequencyAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Models.Coefficients;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterCoefficientActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.EnterFrequencyActivity;
import com.atstrack.ats.ats_vhf_receiver.VHF.FrequenciesActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TemperatureFrequencyFragment extends Fragment implements ReceiverCallback {
    @BindView(R.id.btn_frequency_temperature)
    TextView btn_frequency_temperature;
    @BindView(R.id.btn_coefficient_a)
    Button btn_coefficient_a;
    @BindView(R.id.btn_coefficient_b)
    Button btn_coefficient_b;
    @BindView(R.id.btn_constant)
    Button btn_constant;
    @BindView(R.id.btn_save_frequency)
    Button btn_save_frequency;

    private Unbinder unbinder;
    private final FrequencyAdapter frequencyAdapter;
    private Coefficients coefficients;
    private final int position;
    ActivityResultLauncher<Intent> launcher;

    public TemperatureFrequencyFragment(int position, FrequencyAdapter frequencyAdapter) {
        this.position = position;
        this.frequencyAdapter = frequencyAdapter;
        coefficients = new Coefficients();
        initializeLauncher();
    }

    @OnClick(R.id.btn_frequency_temperature)
    public void onClickFrequencyTemperature(View v) {
        String title = btn_frequency_temperature.getText().toString().isEmpty() ? getString(R.string.lb_add_frequency)
                : "Edit Frequency " + btn_frequency_temperature.getText();
        Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
        intent.putExtra(ValueCodes.TITLE, title);
        intent.putExtra(ValueCodes.POSITION, coefficients.position);
        intent.putExtra(ValueCodes.BASE_FREQUENCY, frequencyAdapter.baseFrequency);
        intent.putExtra(ValueCodes.RANGE, frequencyAdapter.range);
        launcher.launch(intent);
    }

    @OnClick(R.id.btn_coefficient_a)
    public void onClickCoefficientA(View v) {
        Intent intent = new Intent(requireContext(), EnterCoefficientActivity.class);
        intent.putExtra(ValueCodes.TYPE, getString(R.string.lb_coefficient_a));
        launcher.launch(intent);
    }

    @OnClick(R.id.btn_coefficient_b)
    public void onClickCoefficientB(View v) {
        Intent intent = new Intent(requireContext(), EnterCoefficientActivity.class);
        intent.putExtra(ValueCodes.TYPE, getString(R.string.lb_coefficient_b));
        launcher.launch(intent);
    }

    @OnClick(R.id.btn_constant)
    public void onClickConstant(View v) {
        Intent intent = new Intent(requireContext(), EnterCoefficientActivity.class);
        intent.putExtra(ValueCodes.TYPE, getString(R.string.lb_constant));
        launcher.launch(intent);
    }

    @OnClick(R.id.btn_save_frequency)
    public void onClickSaveFrequency(View v) {
        if (coefficients.position != -1) {
            if (existChangesInCoefficients())
                addTemperatureFrequency();
        } else {
            addTemperatureFrequency();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temperature_frequency, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbarTitle();
        if (position >= 0)
            readCoefficients(position);
        else
            initialize();
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
                if (packet[0] == ValueCodes.COEFFICIENTS_COMMAND)
                    downloadCoefficients(packet);
            }
        });
    }

    private void setToolbarTitle() {
        if (getActivity() instanceof FrequenciesActivity) {
            ((FrequenciesActivity) getActivity()).setToolbarTitle("Edit Frequency");
        }
    }

    private void initialize() {
        btn_frequency_temperature.setText("");
        btn_coefficient_a.setText("0");
        btn_coefficient_b.setText("0");
        btn_constant.setText("0");
        btn_save_frequency.setEnabled(false);
        btn_save_frequency.setAlpha((float) 0.6);
    }

    private void downloadCoefficients(byte[] data) {
        if (!Converters.areCoefficientsEmpty(data)) {
            coefficients.setData(data);
            btn_coefficient_a.setText(coefficients.isCoefficientANegative ? "-" + coefficients.coefficientA : String.valueOf(coefficients.coefficientA));
            btn_coefficient_b.setText(coefficients.isCoefficientBNegative ? "-" + coefficients.coefficientB : String.valueOf(coefficients.coefficientB));
            btn_constant.setText(coefficients.isConstantNegative ? "-" + coefficients.constant : String.valueOf(coefficients.constant));
            btn_save_frequency.setEnabled(true);
            btn_save_frequency.setAlpha(1);
        } else {
            initialize();
        }
        btn_frequency_temperature.setText(String.valueOf(coefficients.frequency));
    }

    private void readCoefficients(int position) {
        coefficients = new Coefficients(position, frequencyAdapter.frequencies.get(position));
        sendIndex();
    }

    private void sendIndex() {
        byte[] b = new byte[] {ValueCodes.COEFFICIENTS_COMMAND, (byte) (coefficients.position + 1), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        TransferBleData.writeFrequencies(b);
    }

    private void addTemperatureFrequency() {
        int frequency = Integer.parseInt(btn_frequency_temperature.getText().toString());
        int coefficientA = Integer.parseInt(btn_coefficient_a.getText().toString().replace("-", "")); //985
        int coefficientB = Integer.parseInt(btn_coefficient_b.getText().toString().replace("-", "")); //-6121
        int constant = Integer.parseInt(btn_constant.getText().toString().replace("-", "")); //11088
        //Coefficient D = 0
        byte formatA = btn_coefficient_a.getText().toString().contains("-") ? (byte) 0x80 : 0;
        byte formatB = btn_coefficient_b.getText().toString().contains("-") ? (byte) 0x80 : 0;
        byte formatC = btn_constant.getText().toString().contains("-") ? (byte) 0x80 : 0;
        byte[] b = new byte[] {ValueCodes.COEFFICIENTS_COMMAND, (byte) (coefficients.position == -1 ? (frequencyAdapter.getItemCount() + 1) : (coefficients.position + 1)), (byte) ((frequency - frequencyAdapter.baseFrequency) / 256), (byte) ((frequency - frequencyAdapter.baseFrequency) % 256), formatA,
                (byte) (coefficientA / 256), (byte) (coefficientA % 256), formatB, (byte) (coefficientB / 256), (byte) (coefficientB % 256), formatC,
                (byte) (constant / 256), (byte) (constant % 256), 0, 0, 0};
        boolean result = TransferBleData.writeFrequencies(b);
        if (result) {
            frequencyAdapter.saveCoefficients = true;
            Bundle bundle = new Bundle();
            bundle.putInt(ValueCodes.POSITION, coefficients.position);
            bundle.putInt(ValueCodes.VALUE, frequency);
            getParentFragmentManager().setFragmentResult(ValueCodes.IS_TEMPERATURE, bundle);
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

    private boolean existChangesInCoefficients() {
        return coefficients.frequency != Integer.parseInt(btn_frequency_temperature.getText().toString())
                || (coefficients.coefficientA * (coefficients.isCoefficientANegative ? -1 : 1)) != Integer.parseInt(btn_coefficient_a.getText().toString())
                || (coefficients.coefficientB * (coefficients.isCoefficientBNegative ? -1 : 1)) != Integer.parseInt(btn_coefficient_b.getText().toString())
                || (coefficients.constant * (coefficients.isConstantNegative ? -1 : 1)) != Integer.parseInt(btn_constant.getText().toString());
    }

    private boolean isDataCorrect() {
        return !btn_frequency_temperature.getText().toString().isEmpty() && !btn_coefficient_a.getText().toString().equals("0")
                && !btn_coefficient_b.getText().toString().equals("0") && !btn_constant.getText().toString().equals("0");
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
                            if (frequencyAdapter.isTemperature) { //Save frequency in the list
                                btn_frequency_temperature.setText(String.valueOf(frequency));
                                btn_save_frequency.setEnabled(isDataCorrect());
                                btn_save_frequency.setAlpha(btn_save_frequency.isEnabled() ? 1 : (float) 0.6);
                            }
                        } else if (position == -2) { //coef A
                            String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                            btn_coefficient_a.setText(coefficient);
                            btn_save_frequency.setEnabled(isDataCorrect());
                            btn_save_frequency.setAlpha(btn_save_frequency.isEnabled() ? 1 : (float) 0.6);
                        } else if (position == -3) { //Coef B
                            String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                            btn_coefficient_b.setText(coefficient);
                            btn_save_frequency.setEnabled(isDataCorrect());
                            btn_save_frequency.setAlpha(btn_save_frequency.isEnabled() ? 1 : (float) 0.6);
                        } else if (position == -4) { //Constant
                            String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                            btn_constant.setText(coefficient);
                            btn_save_frequency.setEnabled(isDataCorrect());
                            btn_save_frequency.setAlpha(btn_save_frequency.isEnabled() ? 1 : (float) 0.6);
                        }
                    }
                });
    }
}
