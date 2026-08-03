package com.atstrack.ats.ats_vhf_receiver.Fragments;

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
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentTemperatureFrequencyBinding;

public class TemperatureFrequencyFragment extends Fragment implements ReceiverCallback {
    private FragmentTemperatureFrequencyBinding binding = null;
    private final FrequencyAdapter frequencyAdapter;
    private Coefficients coefficients;
    private final int position;
    private ActivityResultLauncher<Intent> launcher;

    public TemperatureFrequencyFragment(int position, FrequencyAdapter frequencyAdapter) {
        this.position = position;
        this.frequencyAdapter = frequencyAdapter;
        coefficients = new Coefficients();
        initializeLauncher();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTemperatureFrequencyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnFrequencyTemperature.setOnClickListener(v -> {
            String title = binding.btnFrequencyTemperature.getText().toString().isEmpty() ? getString(R.string.btn_vhf_tables_add_frequency)
                    : "Edit Frequency " + binding.btnFrequencyTemperature.getText();
            Intent intent = new Intent(requireContext(), EnterFrequencyActivity.class);
            intent.putExtra(ValueCodes.TITLE, title);
            intent.putExtra(ValueCodes.POSITION, coefficients.position);
            intent.putExtra(ValueCodes.BASE_FREQUENCY, frequencyAdapter.baseFrequency);
            intent.putExtra(ValueCodes.RANGE, frequencyAdapter.range);
            launcher.launch(intent);
        });
        binding.btnCoefficientA.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EnterCoefficientActivity.class);
            intent.putExtra(ValueCodes.TYPE, getString(R.string.lbl_vhf_tables_coef_a));
            launcher.launch(intent);
        });
        binding.btnCoefficientB.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EnterCoefficientActivity.class);
            intent.putExtra(ValueCodes.TYPE, getString(R.string.lbl_vhf_tables_coef_b));
            launcher.launch(intent);
        });
        binding.btnConstant.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EnterCoefficientActivity.class);
            intent.putExtra(ValueCodes.TYPE, getString(R.string.lbl_vhf_tables_constant));
            launcher.launch(intent);
        });
        binding.btnSaveFrequency.setOnClickListener(v -> {
            if (coefficients.position != -1) {
                if (existChangesInCoefficients())
                    addTemperatureFrequency();
            } else {
                addTemperatureFrequency();
            }
        });
        setToolbarTitle();
        if (position >= 0)
            readCoefficients(position);
        else
            initialize();
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
        binding.btnFrequencyTemperature.setText("");
        binding.btnCoefficientA.setText("0");
        binding.btnCoefficientB.setText("0");
        binding.btnConstant.setText("0");
        binding.btnSaveFrequency.setEnabled(false);
        binding.btnSaveFrequency.setAlpha((float) 0.6);
    }

    private void downloadCoefficients(byte[] data) {
        if (!Converters.areCoefficientsEmpty(data)) {
            coefficients.setData(data);
            binding.btnCoefficientA.setText(coefficients.isCoefficientANegative ? "-" + coefficients.coefficientA : String.valueOf(coefficients.coefficientA));
            binding.btnCoefficientB.setText(coefficients.isCoefficientBNegative ? "-" + coefficients.coefficientB : String.valueOf(coefficients.coefficientB));
            binding.btnConstant.setText(coefficients.isConstantNegative ? "-" + coefficients.constant : String.valueOf(coefficients.constant));
            binding.btnSaveFrequency.setEnabled(true);
            binding.btnSaveFrequency.setAlpha(1);
        } else {
            initialize();
        }
        binding.btnFrequencyTemperature.setText(String.valueOf(coefficients.frequency));
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
        int frequency = Integer.parseInt(binding.btnFrequencyTemperature.getText().toString());
        int coefficientA = Integer.parseInt(binding.btnCoefficientA.getText().toString().replace("-", "")); //985
        int coefficientB = Integer.parseInt(binding.btnCoefficientB.getText().toString().replace("-", "")); //-6121
        int constant = Integer.parseInt(binding.btnConstant.getText().toString().replace("-", "")); //11088
        //Coefficient D = 0
        byte formatA = binding.btnCoefficientA.getText().toString().contains("-") ? (byte) 0x80 : 0;
        byte formatB = binding.btnCoefficientB.getText().toString().contains("-") ? (byte) 0x80 : 0;
        byte formatC = binding.btnConstant.getText().toString().contains("-") ? (byte) 0x80 : 0;
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
                        .remove(this)
                        .show(fragment1)
                        .commit();
                getParentFragmentManager().popBackStack();
            }
        }
    }

    private boolean existChangesInCoefficients() {
        return coefficients.frequency != Integer.parseInt(binding.btnFrequencyTemperature.getText().toString())
                || (coefficients.coefficientA * (coefficients.isCoefficientANegative ? -1 : 1)) != Integer.parseInt(binding.btnCoefficientA.getText().toString())
                || (coefficients.coefficientB * (coefficients.isCoefficientBNegative ? -1 : 1)) != Integer.parseInt(binding.btnCoefficientB.getText().toString())
                || (coefficients.constant * (coefficients.isConstantNegative ? -1 : 1)) != Integer.parseInt(binding.btnConstant.getText().toString());
    }

    private boolean isDataCorrect() {
        return !binding.btnFrequencyTemperature.getText().toString().isEmpty() && !binding.btnCoefficientA.getText().toString().equals("0")
                && !binding.btnCoefficientB.getText().toString().equals("0") && !binding.btnConstant.getText().toString().equals("0");
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
                                binding.btnFrequencyTemperature.setText(String.valueOf(frequency));
                                binding.btnSaveFrequency.setEnabled(isDataCorrect());
                                binding.btnSaveFrequency.setAlpha(binding.btnSaveFrequency.isEnabled() ? 1 : (float) 0.6);
                            }
                        } else if (position == -2) { //coef A
                            String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                            binding.btnCoefficientA.setText(coefficient);
                            binding.btnSaveFrequency.setEnabled(isDataCorrect());
                            binding.btnSaveFrequency.setAlpha(binding.btnSaveFrequency.isEnabled() ? 1 : (float) 0.6);
                        } else if (position == -3) { //Coef B
                            String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                            binding.btnCoefficientB.setText(coefficient);
                            binding.btnSaveFrequency.setEnabled(isDataCorrect());
                            binding.btnSaveFrequency.setAlpha(binding.btnSaveFrequency.isEnabled() ? 1 : (float) 0.6);
                        } else if (position == -4) { //Constant
                            String coefficient = result.getData().getStringExtra(ValueCodes.VALUE);
                            binding.btnConstant.setText(coefficient);
                            binding.btnSaveFrequency.setEnabled(isDataCorrect());
                            binding.btnSaveFrequency.setAlpha(binding.btnSaveFrequency.isEnabled() ? 1 : (float) 0.6);
                        }
                    }
                });
    }
}
