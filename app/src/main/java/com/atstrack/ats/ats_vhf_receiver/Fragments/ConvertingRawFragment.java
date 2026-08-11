package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.Models.Data;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentConvertingRawBinding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ConvertingRawFragment extends Fragment {
    private FragmentConvertingRawBinding binding = null;
    private final Uri uri;
    private final File rawFile;

    public ConvertingRawFragment(Uri uri, File rawFile) {
        this.uri = uri;
        this.rawFile = rawFile;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConvertingRawBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnCancelConversion.setOnClickListener(v -> {
            if (getParentFragmentManager() != null)
                getParentFragmentManager().popBackStack();
        });
        convertRawData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void convertRawData() {
        Context fragmentContext = requireContext();
        new Thread(() -> {
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(rawFile));
                byte[] rawData = new byte[(int) rawFile.length()];
                bufferedInputStream.read(rawData, 0, rawData.length);
                bufferedInputStream.close();

                updateProgress(20);

                ArrayList<byte[]> rawList = new ArrayList<>();
                rawList.add(rawData);

                String[] texts = Converters.getPackageProcessed(rawList, binding.pbConvertingRaw, (BaseActivity) fragmentContext, true);
                byte[] processed = Converters.convertToUTF8(texts[0]);
                byte[] metrics = Converters.convertToUTF8(texts[1]);

                Data processedData = new Data(ValueCodes.PROCESSED_FILE);
                processedData.packets.add(processed);
                Data metricsData = new Data(ValueCodes.METRICS_FILE);
                metricsData.packets.add(metrics);
                ArrayList<Data> dataList = new ArrayList<>();
                dataList.add(processedData);
                dataList.add(metricsData);

                File root = new File(uri.getPath().split(":")[0].replace("document", "storage"), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
                String fileName = dataList.get(0).fileName;
                boolean result = Converters.printDataFiles(root, dataList);

                if (result) {
                    updateProgress(100);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded() && getView() != null) {
                            if (getParentFragmentManager() != null) {
                                getParentFragmentManager().beginTransaction()
                                        .setReorderingAllowed(true)
                                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                        .hide(this)
                                        .add(R.id.fcv_activity_fragment, new FileConvertedFragment(fileName))
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }
                    }, ValueCodes.MESSAGE_PERIOD);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void updateProgress(int value) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getView() != null)
                binding.pbConvertingRaw.setProgress(value);
        });
    }
}
