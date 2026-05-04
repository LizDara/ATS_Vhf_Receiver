package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.core.content.ContextCompat;

import butterknife.BindView;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.DetectionFilter;
import com.atstrack.ats.ats_vhf_receiver.Models.LoadedTable;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Messages;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;
import com.google.api.client.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TablesActivity extends BaseActivity {

    @BindView(R.id.title_tables_textView)
    TextView title_tables_textView;
    @BindView(R.id.load_from_file_button)
    Button load_from_file_button;
    @BindView(R.id.removed_frequencies_linearLayout)
    LinearLayout removed_frequencies_linearLayout;
    @BindView(R.id.tables_listView)
    ListView tables_listView;
    @BindView(R.id.options_loaded_linearLayout)
    LinearLayout options_loaded_linearLayout;

    final private String TAG = TablesActivity.class.getSimpleName();

    private TableListAdapter tableListAdapter;
    private List<LoadedTable> removedFrequencies;

    @OnClick(R.id.removed_frequencies_linearLayout)
    public void onClickRemovedFrequencies(View v) {
        Messages.showLoadedFrequenciesMessage(this, getString(R.string.lb_removed_frequencies), removedFrequencies, true);
    }

    @OnClick(R.id.load_from_file_button)
    public void onClickLoadTablesFromFile(View v) {
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
        File externalFile = externalStorageVolumes[0];
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.parse(externalFile.getPath()), "text/plain");
        startActivityForResult(intent, ValueCodes.REQUEST_CODE_OPEN_STORAGE);
    }

    @OnClick(R.id.push_frequencies_button)
    public void onClickPushFrequencies(View v) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.loading_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        ProgressBar loading_progressBar = view.findViewById(R.id.loading_progressBar);
        ImageView loaded_imageView = view.findViewById(R.id.loaded_imageView);
        TextView state_loading_textView = view.findViewById(R.id.state_loading_textView);
        state_loading_textView.setText(R.string.lb_pushing_frequencies);
        dialog.setView(view);
        dialog.show();

        Timer pushingTimeout = new Timer();
        pushingTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
                Messages.showErrorMessage(getApplicationContext(), getString(R.string.lb_error), getString(R.string.lb_error_push), false, null);
            }
        }, (long) ValueCodes.DOWNLOAD_PERIOD * 2 * tableListAdapter.getCount());

        int index = 0;
        while (index < tableListAdapter.getCount()) {
            while (!setTable(tableListAdapter.getLoadedTables().get(index))) {
                try {
                    Thread.sleep(ValueCodes.WAITING_PERIOD);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                Thread.sleep(ValueCodes.DOWNLOAD_PERIOD);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            index++;
        }
        if (index == tableListAdapter.getCount()) {
            pushingTimeout.cancel();
            pushingTimeout.purge();
            state_loading_textView.setText(R.string.lb_frequencies_pushed);
            loading_progressBar.setVisibility(View.GONE);
            loaded_imageView.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                TransferBleData.readTables();
                dialog.dismiss();
                setVisibility("overview");
            }, ValueCodes.MESSAGE_PERIOD);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_tables;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.edit_frequency_tables);
        super.onCreate(savedInstanceState);

        tableListAdapter = new TableListAdapter(this);
        parameter = getIntent().getByteExtra(ValueCodes.PARAMETER, ValueCodes.NONE);
        if (parameter == ValueCodes.NONE) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ValueCodes.REQUEST_CODE_OPEN_STORAGE) {
            if (resultCode == RESULT_OK) { // Gets the Uri of the selected file
                Uri uri = data.getData();
                String uriString = uri.toString();
                final Cursor[] cursorContainer = { null };
                Messages.showLoadingMessage(this, getString(R.string.lb_importing_tables), getString(R.string.lb_frequencies_imported), () -> {
                    if (uriString.startsWith("content://")) {
                        try {
                            cursorContainer[0] = getBaseContext().getContentResolver().query(uri, null, null, null, null);
                        } catch (Exception ex) {
                            Messages.showErrorMessage(this, getString(R.string.lb_error), getString(R.string.lb_error_push), false, null);
                        }
                    }
                }, () -> {
                     if (uriString.startsWith("file://")) {
                        readFile(uri);
                    } else if (cursorContainer[0] != null && cursorContainer[0].moveToFirst()) {
                        readFile(uri);
                        cursorContainer[0].close();
                    }
                });
            } else {
                Messages.showErrorMessage(this, getString(R.string.lb_error), getString(R.string.lb_error_upload), false, null);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (tableListAdapter.isFile()) {
                Messages.showErrorMessage(this, getString(R.string.lb_cancel_frequency_upload), getString(R.string.lb_cancel_frequencies), true, () -> {
                    setVisibility("overview");
                    tableListAdapter.setFile(false);
                    tableListAdapter.notifyDataSetChanged();
                });
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!tableListAdapter.isFile())
            TransferBleData.readTables();
    }

    @Override
    protected void downloadData(byte[] data) {
        if (data[0] == ValueCodes.TABLES_COMMAND)
            downloadTables(data);
        else if (data[0] == ValueCodes.DETECTION_FILTER_COMMAND)
            downloadDetectionFilter(data);
    }

    private void setVisibility(String value) {
        switch (value) {
            case "overview":
                title_tables_textView.setText(R.string.lb_select_table);
                load_from_file_button.setVisibility(View.VISIBLE);
                options_loaded_linearLayout.setVisibility(View.GONE);
                removed_frequencies_linearLayout.setVisibility(View.GONE);
                break;
            case "loaded":
                title_tables_textView.setText(R.string.lb_table_summary);
                load_from_file_button.setVisibility(View.GONE);
                options_loaded_linearLayout.setVisibility(View.VISIBLE);
                break;
        }
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
                File file = new File(getCacheDir(), getFileName(getContentResolver(), fileUri));
                FileOutputStream outputStream = new FileOutputStream(file);
                IOUtils.copy(inputStream, outputStream);

                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                removedFrequencies = new LinkedList<>();
                tableListAdapter.emptyLoadedTables();
                String line;
                int tableNumber = 0;
                List<Integer> frequenciesList = new LinkedList<>();
                List<Integer> removedFrequenciesList = new LinkedList<>();
                int frequencyRange = ((tableListAdapter.getRange() + tableListAdapter.getBaseFrequency()) * 1000) - 1;
                while ((line = bufferedReader.readLine()) != null) { // Reads each line of the file and add it to the list
                    line = line.replace(" ", "");
                    if (line.toUpperCase().contains("TABLE")) {
                        if (tableNumber > 0) {
                            tableListAdapter.addLoadedTable(new LoadedTable(tableNumber, frequenciesList.stream().mapToInt(Integer::intValue).toArray()));
                            if (!removedFrequenciesList.isEmpty()) {
                                removedFrequencies.add(new LoadedTable(tableNumber, removedFrequenciesList.stream().mapToInt(Integer::intValue).toArray()));
                            }
                        }
                        tableNumber = Integer.parseInt(line.toUpperCase().replace("TABLE", ""));
                        frequenciesList = new LinkedList<>();
                        removedFrequenciesList = new LinkedList<>();
                    } else {
                        try {
                            int frequency = Integer.parseInt(line);
                            if (frequency > (tableListAdapter.getBaseFrequency() * 1000) && frequency <= frequencyRange) {
                                frequenciesList.add(frequency);
                            } else {
                                removedFrequenciesList.add(frequency);
                                Log.i(TAG, "FREQ ADDED (" + removedFrequenciesList.size() + ")" + removedFrequenciesList.get(removedFrequenciesList.size() - 1));
                                removed_frequencies_linearLayout.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                tableListAdapter.addLoadedTable(new LoadedTable(tableNumber, frequenciesList.stream().mapToInt(Integer::intValue).toArray())); //Last table in the file
                if (!removedFrequenciesList.isEmpty()) {
                    removedFrequencies.add(new LoadedTable(tableNumber, removedFrequenciesList.stream().mapToInt(Integer::intValue).toArray()));
                }

                tableListAdapter.setFile(true);
                tableListAdapter.notifyDataSetChanged();
                setVisibility("loaded");

                fileInputStream.close();
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

    /**
     * With the received packet, gets the number of frequencies from each table and display on the screen.
     * @param data The received packet.
     */
    private void downloadTables(byte[] data) {
        setVisibility("overview");
        tableListAdapter.setData(data);
        tables_listView.setAdapter(tableListAdapter);
        tableListAdapter.setFile(false);
        TransferBleData.readDetectionFilter();
    }

    private void downloadDetectionFilter(byte[] data) {
        boolean isTemperature = data[1] == DetectionFilter.VARIABLE && data[11] == DetectionFilter.VARIABLE_TEMPERATURE;
        tableListAdapter.setTemperature(isTemperature);
    }

    private boolean setTable(LoadedTable loadedTable) {
        byte[] b = setCalendar(244);
        b[0] = (byte) 0x7E;
        b[7] = (byte) loadedTable.tableNumber;
        b[8] = (byte) loadedTable.frequenciesLoaded.length;
        b[9] = (byte) tableListAdapter.getBaseFrequency();
        int index = 10;
        int i = 0;
        while (i < loadedTable.frequenciesLoaded.length) {
            b[index] = (byte) ((loadedTable.frequenciesLoaded[i] - (tableListAdapter.getBaseFrequency() * 1000)) / 256);
            b[index + 1] = (byte) ((loadedTable.frequenciesLoaded[i] - (tableListAdapter.getBaseFrequency() * 1000)) % 256);
            index += 2;
            i++;
        }
        return TransferBleData.writeFrequencies(b);
    }
}