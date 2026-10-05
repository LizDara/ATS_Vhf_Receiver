package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.atstrack.ats.ats_vhf_receiver.databinding.FragmentFileSourceBinding;
import com.google.api.client.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FileSourceFragment extends Fragment {
    private FragmentFileSourceBinding binding = null;
    private String sdCardId;
    private Uri uri;
    private File rawFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFileSourceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.layoutSelectFile.setOnClickListener(v -> {
            String folderId = sdCardId + ":";
            Uri targetFolderUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", folderId);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(targetFolderUri, "vnd.android.document/directory");
            intent.setType("text/plain");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, targetFolderUri);

            try {
                startActivityForResult(intent, ValueCodes.REQUEST_CODE_OPEN_STORAGE);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "No se pudo abrir la raíz de la tarjeta SD.", Toast.LENGTH_SHORT).show();
            }
        });
        binding.imgDeleteFile.setOnClickListener(v -> setVisibility(ValueCodes.OVERVIEW));
        binding.btnConvertData.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .add(R.id.fcv_activity_fragment, new ConvertingRawFragment(uri, rawFile))
                        .addToBackStack(null)
                        .commit();
            }
        });
        sdCardId = getExternalSDCardId();
        Log.i("FileSourceFragment", "SD Card ID: " + sdCardId);
        setVisibility(ValueCodes.OVERVIEW);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ValueCodes.REQUEST_CODE_OPEN_STORAGE) {
            if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                uri = data.getData();
                if (uri == null) return;
                String uriString = uri.toString();
                if (uriString.startsWith("content://")) {
                    try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            readFile(uri);
                        }
                    } catch (Exception ex) {
                        Log.e("FileSourceFragment", "Cursor exception: " + ex);
                    }
                } else if (uriString.startsWith("file://")) {
                    readFile(uri);
                }
            }
        }
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.OVERVIEW) {
            binding.btnConvertData.setAlpha((float) 0.6);
            binding.btnConvertData.setEnabled(false);
            binding.imgSdCardRaw.setBackgroundResource(sdCardId != null ? R.drawable.ic_sd_card : R.drawable.ic_sd_card_alert);
            binding.tvSdCardRaw.setText(sdCardId != null ? R.string.lbl_vhf_home_sd_card_inserted : R.string.lbl_vhf_raw_none_detected);
            binding.tvMessageNoInserted.setVisibility(sdCardId != null ? View.GONE : View.VISIBLE);
            binding.layoutSelectFile.setVisibility(View.VISIBLE);
            binding.layoutSelectedFile.setVisibility(View.GONE);
            binding.layoutSelectFile.setAlpha(sdCardId != null ? 1 : (float) 0.6);
            binding.layoutSelectFile.setEnabled(sdCardId != null);
        } else if (view == ValueCodes.FOUNDED) {
            binding.layoutSelectFile.setVisibility(View.GONE);
            binding.layoutSelectedFile.setVisibility(View.VISIBLE);
            binding.btnConvertData.setAlpha(1);
            binding.btnConvertData.setEnabled(true);
        }
    }

    private void readFile(Uri fileUri) {
        if (isExternalStorageReadable()) {
            try {
                ContentResolver contentResolver = requireContext().getContentResolver();
                ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r");
                if (parcelFileDescriptor == null) return;
                FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                rawFile = new File(requireContext().getCacheDir(), getFileName(contentResolver, fileUri));
                FileOutputStream outputStream = new FileOutputStream(rawFile);
                IOUtils.copy(inputStream, outputStream);

                setVisibility(ValueCodes.FOUNDED);
                String[] fileName = rawFile.getName().split("\\.");
                binding.tvFileName.setText(fileName[0]);
                binding.tvFileDescription.setText(fileName[1].toUpperCase() + " - " + (((float)(rawFile.length() / 1024)) / 1000) + " MB");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("FileSourceFragment", "Cannot read from external storage");
        }
    }

    private String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String name = "";
        Cursor returnCursor = contentResolver.query(fileUri, null, null, null, null);
        if (returnCursor != null) {
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            name = returnCursor.getString(nameIndex);
            returnCursor.close();
        }
        return name;
    }

    private boolean isExternalStorageReadable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    private String getExternalSDCardId() {
        StorageManager storageManager = (StorageManager) requireContext().getSystemService(Context.STORAGE_SERVICE);
        if (storageManager != null) {
            List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
            for (StorageVolume volume : storageVolumes) {
                if (volume.isRemovable() && !volume.isPrimary()) {
                    String uuid = volume.getUuid();
                    if (uuid != null) {
                        return uuid;
                    }
                }
            }
        }
        return null;
    }
}
