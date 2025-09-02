package com.atstrack.ats.ats_vhf_receiver.VHF;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
import com.atstrack.ats.ats_vhf_receiver.Utils.Snapshots;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.google.api.client.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RawDataActivity extends AppCompatActivity {

    @BindView(R.id.file_source_linearLayout)
    LinearLayout file_source_linearLayout;
    @BindView(R.id.sd_card_raw_imageView)
    ImageView sd_card_raw_imageView;
    @BindView(R.id.sd_card_raw_textView)
    TextView sd_card_raw_textView;
    @BindView(R.id.message_no_inserted_textView)
    TextView message_no_inserted_textView;
    @BindView(R.id.select_file_linearLayout)
    LinearLayout select_file_linearLayout;
    @BindView(R.id.selected_file_linearLayout)
    LinearLayout selected_file_linearLayout;
    @BindView(R.id.converting_raw_linearLayout)
    LinearLayout converting_raw_linearLayout;
    @BindView(R.id.file_name_textView)
    TextView file_name_textView;
    @BindView(R.id.file_description_textView)
    TextView file_description_textView;
    @BindView(R.id.convert_data_button)
    Button convert_data_button;
    @BindView(R.id.file_converted_linearLayout)
    LinearLayout file_converted_linearLayout;
    @BindView(R.id.converting_raw_progressBar)
    ProgressBar converting_raw_progressBar;
    @BindView(R.id.new_file_name_textView)
    TextView new_file_name_textView;

    private final static String TAG = RawDataActivity.class.getSimpleName();

    private File[] externalStorageVolumes;
    private File rawFile;
    private Uri uri;

    @OnClick(R.id.select_file_linearLayout)
    public void onClickSelectFile(View v) {
        File sdCardFile = externalStorageVolumes[1];
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.parse(sdCardFile.getPath()), "*/*");
        startActivityForResult(intent, ValueCodes.REQUEST_CODE_OPEN_STORAGE);
    }

    @OnClick(R.id.delete_file_imageView)
    public void onClickDeleteFile(View v) {
        select_file_linearLayout.setVisibility(View.VISIBLE);
        selected_file_linearLayout.setVisibility(View.GONE);
        convert_data_button.setEnabled(false);
        convert_data_button.setAlpha((float) 0.6);
    }

    @OnClick(R.id.convert_data_button)
    public void onClickConvertData(View v) {
        file_source_linearLayout.setVisibility(View.GONE);
        converting_raw_linearLayout.setVisibility(View.VISIBLE);
        converting_raw_progressBar.setProgress(10);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(rawFile));
            byte[] rawData = new byte[(int) rawFile.length()];
            bufferedInputStream.read(rawData, 0, rawData.length);
            bufferedInputStream.close();

            converting_raw_progressBar.setProgress(40);
            SharedPreferences sharedPreferences = getSharedPreferences(ValueCodes.DEFAULT_SETTING, 0);
            int baseFrequency = sharedPreferences.getInt(ValueCodes.BASE_FREQUENCY, 0) * 1000;
            String processData = Converters.getPackageProcessed(rawData, baseFrequency);
            byte[] data = Converters.convertToUTF8(processData);
            ArrayList<Snapshots> snapshotArray = new ArrayList<>();
            Snapshots processedData = new Snapshots(data.length);
            processedData.processSnapshot(data);
            snapshotArray.add(processedData);
            converting_raw_progressBar.setProgress(80);

            //File root = externalStorageVolumes[1]; //set the directory path
            File root = new File(uri.getPath().split(":")[0].replace("document", "storage"), Environment.DIRECTORY_DOWNLOADS + "/atstrack");
            String fileName = snapshotArray.get(0).getFileName();
            boolean result = Converters.printSnapshotFiles(root, snapshotArray);
            if (result) {
                converting_raw_progressBar.setProgress(100);
                converting_raw_linearLayout.setVisibility(View.GONE);
                file_converted_linearLayout.setVisibility(View.VISIBLE);
                new_file_name_textView.setText("File saved as " + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.cancel_conversion_button)
    public void onClickCancelConversion(View v) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_raw_data);
        ButterKnife.bind(this);
        ActivitySetting.setVhfToolbar(this, getString(R.string.convert_raw_data));
        ActivitySetting.setReceiverStatus(this);

        externalStorageVolumes = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
        boolean sdCardInserted = externalStorageVolumes.length > 1;
        sd_card_raw_imageView.setBackgroundResource(sdCardInserted ? R.drawable.ic_sd_card : R.drawable.ic_sd_card_alert);
        sd_card_raw_textView.setText(sdCardInserted ? R.string.lb_inserted : R.string.lb_none_detected);
        message_no_inserted_textView.setVisibility(sdCardInserted ? View.GONE : View.VISIBLE);
        select_file_linearLayout.setAlpha(sdCardInserted ? 1 : (float) 0.6);
        select_file_linearLayout.setEnabled(sdCardInserted);

        convert_data_button.setAlpha((float) 0.6);
        convert_data_button.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ValueCodes.REQUEST_CODE_OPEN_STORAGE) {
            if (resultCode == RESULT_OK) { // Gets the Uri of the selected file
                uri = data.getData();
                String uriString = uri.toString();
                if (uriString.startsWith("content://")) {
                    try (Cursor cursor = getBaseContext().getContentResolver().query(uri, null, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst())
                            readFile(uri);
                    } catch (Exception ex) {
                        Log.i(TAG, "Cursor exception: " + ex);
                    }
                } else if (uriString.startsWith("file://")) {
                    readFile(uri);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "ON BACK PRESSED");
    }

    /**
     * Reads a file from the local storage and get the frequencies from each table.
     * @param fileUri The directory path where is the file.
     */
    private void readFile(Uri fileUri) {
        if (isExternalStorageReadable()) {
            try {
                ContentResolver contentResolver = getContentResolver();
                ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r");
                FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                rawFile = new File(getCacheDir(), getFileName(getContentResolver(), fileUri));
                FileOutputStream outputStream = new FileOutputStream(rawFile);
                IOUtils.copy(inputStream, outputStream);

                select_file_linearLayout.setVisibility(View.GONE);
                selected_file_linearLayout.setVisibility(View.VISIBLE);

                String[] fileName = rawFile.getName().split("\\.");
                file_name_textView.setText(fileName[0]);
                file_description_textView.setText(fileName[1].toUpperCase() + " - " + (((float)(rawFile.length() / 1024)) / 1000) + " MB");
                convert_data_button.setAlpha(1);
                convert_data_button.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Cannot read from external storage");
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

    /**
     * Checks if external storage is readable.
     * @return Returns true, if external storage is readable.
     */
    private boolean isExternalStorageReadable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }
}