package com.atstrack.ats.ats_vhf_receiver.Fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.google.api.client.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FileSourceFragment extends Fragment {
    @BindView(R.id.img_sd_card_raw)
    ImageView img_sd_card_raw;
    @BindView(R.id.tv_sd_card_raw)
    TextView tv_sd_card_raw;
    @BindView(R.id.tv_message_no_inserted)
    TextView tv_message_no_inserted;
    @BindView(R.id.layout_select_file)
    LinearLayout layout_select_file;
    @BindView(R.id.layout_selected_file)
    LinearLayout layout_selected_file;
    @BindView(R.id.tv_file_name)
    TextView tv_file_name;
    @BindView(R.id.tv_file_description)
    TextView tv_file_description;
    @BindView(R.id.btn_convert_data)
    Button btn_convert_data;

    private Unbinder unbinder;
    private File[] externalStorageVolumes;
    private Uri uri;
    private File rawFile;

    @OnClick(R.id.layout_select_file)
    public void onClickSelectFile(View v) {
        File sdCardFile = externalStorageVolumes[externalStorageVolumes.length - 1];
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        File root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
        intent.setDataAndType(Uri.parse(root.getAbsolutePath()), "*/*");
        // CLAVE: En un fragmento se llama directo sin el prefijo 'getActivity().' para que la respuesta viaje de regreso a este mismo fragmento.
        startActivityForResult(intent, ValueCodes.REQUEST_CODE_OPEN_STORAGE);
    }

    @OnClick(R.id.img_delete_file)
    public void onClickDeleteFile(View v) {
        setVisibility(ValueCodes.OVERVIEW);
    }

    @OnClick(R.id.btn_convert_data)
    public void onClickConvertData(View v) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(this)
                    .add(R.id.fcv_activity_fragment, new ConvertingRawFragment(uri, rawFile))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_source, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        externalStorageVolumes = ContextCompat.getExternalFilesDirs(requireContext().getApplicationContext(), null);
        setVisibility(ValueCodes.OVERVIEW);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
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
            btn_convert_data.setAlpha((float) 0.6);
            btn_convert_data.setEnabled(false);
            img_sd_card_raw.setBackgroundResource(externalStorageVolumes.length > 1 ? R.drawable.ic_sd_card : R.drawable.ic_sd_card_alert);
            tv_sd_card_raw.setText(externalStorageVolumes.length > 1 ? R.string.lb_inserted : R.string.lb_none_detected);
            tv_message_no_inserted.setVisibility(externalStorageVolumes.length > 1 ? View.GONE : View.VISIBLE);
            layout_select_file.setVisibility(View.VISIBLE);
            layout_selected_file.setVisibility(View.GONE);
            layout_select_file.setAlpha(externalStorageVolumes.length > 1 ? 1 : (float) 0.6);
            layout_select_file.setEnabled(externalStorageVolumes.length > 1);
        } else if (view == ValueCodes.FOUNDED) {
            layout_select_file.setVisibility(View.GONE);
            layout_selected_file.setVisibility(View.VISIBLE);
            btn_convert_data.setAlpha(1);
            btn_convert_data.setEnabled(true);
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
                tv_file_name.setText(fileName[0]);
                tv_file_description.setText(fileName[1].toUpperCase() + " - " + (((float)(rawFile.length() / 1024)) / 1000) + " MB");
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
}
