package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Models.LoadedTable;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Dialogs;
import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnAdapterClickListener;
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

public class TablesActivity extends BaseActivity implements OnAdapterClickListener {

    @BindView(R.id.tv_title_toolbar)
    TextView tv_title_toolbar;
    @BindView(R.id.tv_title_table)
    TextView tv_title_table;
    @BindView(R.id.btn_frequencies)
    Button btn_frequencies;
    @BindView(R.id.layout_removed_frequencies)
    LinearLayout layout_removed_frequencies;
    @BindView(R.id.rv_item)
    RecyclerView rv_item;
    @BindView(R.id.layout_options_loaded)
    LinearLayout layout_options_loaded;
    @BindView(R.id.tv_message_frequencies)
    TextView tv_message_frequencies;
    @BindView(R.id.tv_view_tables)
    TextView tv_view_tables;
    @BindView(R.id.btn_frequency)
    Button btn_frequency;

    private TableAdapter tableAdapter;
    private List<LoadedTable> removedFrequencies;
    private Handler loadingHandler;

    @OnClick(R.id.layout_removed_frequencies)
    public void onClickRemovedFrequencies(View v) {
        showAlertDialog(getString(R.string.lb_removed_frequencies), true, removedFrequencies);
    }

    @OnClick(R.id.btn_frequencies)
    public void onClickLoadTablesFromFile(View v) {
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
        File externalFile = externalStorageVolumes[0];
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.parse(externalFile.getPath()), "text/plain");
        startActivityForResult(intent, ValueCodes.REQUEST_CODE_OPEN_STORAGE);
        tableAdapter.isFile = true;
    }

    @OnClick(R.id.btn_frequency)
    public void onClickPushFrequencies(View v) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_alert_loading, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        ProgressBar loading_progressBar = view.findViewById(R.id.pb_loading);
        ImageView loaded_imageView = view.findViewById(R.id.img_loaded);
        TextView state_loading_textView = view.findViewById(R.id.tv_state_loading);
        state_loading_textView.setText(R.string.lb_pushing_frequencies);
        dialog.setView(view);
        dialog.show();

        Timer pushingTimeout = new Timer();
        pushingTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
                showAlertDialog(getString(R.string.lb_error), getString(R.string.lb_error_push), false);
            }
        }, (long) ValueCodes.DOWNLOAD_PERIOD * 2 * tableAdapter.getItemCount());

        int index = 0;
        while (index < tableAdapter.getItemCount()) {
            while (!setTable(tableAdapter.loadedTables.get(index))) {
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
        if (index == tableAdapter.getItemCount()) {
            pushingTimeout.cancel();
            pushingTimeout.purge();
            state_loading_textView.setText(R.string.lb_frequencies_pushed);
            loading_progressBar.setVisibility(View.GONE);
            loaded_imageView.setVisibility(View.VISIBLE);
            messageHandler.postDelayed(() -> {
                TransferBleData.readTables();
                dialog.dismiss();
                setVisibility(ValueCodes.OVERVIEW);
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

        tableAdapter = new TableAdapter(this, this);
        parameter = getIntent().getByteExtra(ValueCodes.PARAMETER, ValueCodes.NONE);
        if (parameter == ValueCodes.NONE) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
        layout_options_loaded.setVisibility(View.GONE);
        layout_removed_frequencies.setVisibility(View.GONE);
        tv_view_tables.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ValueCodes.REQUEST_CODE_OPEN_STORAGE) {
            if (resultCode == RESULT_OK) { // Gets the Uri of the selected file
                Uri uri = data.getData();
                String uriString = uri.toString();
                final Cursor[] cursorContainer = { null };
                showAlertDialog(uri, uriString, cursorContainer);
            } else {
                showAlertDialog(getString(R.string.lb_error), getString(R.string.lb_error_upload), false);
                tableAdapter.isFile = false;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Go back to the previous activity
            if (tableAdapter.isFile)
                showAlertDialog(getString(R.string.lb_cancel_frequency_upload), getString(R.string.lb_cancel_frequencies), true);
            else
                finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!tableAdapter.isFile)
            TransferBleData.readTables();
    }

    @Override
    protected void onDestroy() {
        if (loadingHandler != null)
            loadingHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onAdapterItemClickListener(int position) {
        List<LoadedTable> frequenciesTable = new LinkedList<>();
        frequenciesTable.add(tableAdapter.loadedTables.get(position));
        showAlertDialog("Table " + tableAdapter.loadedTables.get(position).tableNumber, false, frequenciesTable);
    }

    @Override
    protected void downloadData(byte[] data) {
        if (data[0] == ValueCodes.TABLES_COMMAND)
            downloadTables(data);
        else if (data[0] == ValueCodes.DETECTION_FILTER_COMMAND)
            downloadDetectionFilter(data);
    }

    private void setVisibility(int view) {
        if (view == ValueCodes.OVERVIEW) {
            tv_title_table.setText(R.string.lb_select_table);
            btn_frequencies.setVisibility(View.VISIBLE);
            btn_frequencies.setText(R.string.lb_load_table_from_file);
            tv_title_toolbar.setText(R.string.edit_frequency_tables);
        } else if (view == ValueCodes.DOWNLOADED) {
            tv_title_table.setText(R.string.lb_table_summary);
            btn_frequencies.setVisibility(View.GONE);
            layout_options_loaded.setVisibility(View.VISIBLE);
            btn_frequency.setText(R.string.lb_push_frequencies);
            tv_message_frequencies.setVisibility(View.VISIBLE);
            tv_title_toolbar.setText(R.string.lb_tables_loaded);
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
                tableAdapter.loadedTables.clear();
                String line;
                int tableNumber = 0;
                List<Integer> frequenciesList = new LinkedList<>();
                List<Integer> removedFrequenciesList = new LinkedList<>();
                int frequencyRange = ((tableAdapter.getRange() + tableAdapter.getBaseFrequency()) * 1000) - 1;
                while ((line = bufferedReader.readLine()) != null) { // Reads each line of the file and add it to the list
                    line = line.replace(" ", "");
                    if (line.toUpperCase().contains("TABLE")) {
                        if (tableNumber > 0) {
                            tableAdapter.loadedTables.add(new LoadedTable(tableNumber, frequenciesList.stream().mapToInt(Integer::intValue).toArray()));
                            if (!removedFrequenciesList.isEmpty())
                                removedFrequencies.add(new LoadedTable(tableNumber, removedFrequenciesList.stream().mapToInt(Integer::intValue).toArray()));
                        }
                        tableNumber = Integer.parseInt(line.toUpperCase().replace("TABLE", ""));
                        frequenciesList = new LinkedList<>();
                        removedFrequenciesList = new LinkedList<>();
                    } else {
                        try {
                            int frequency = Integer.parseInt(line);
                            if (frequency > (tableAdapter.getBaseFrequency() * 1000) && frequency <= frequencyRange) {
                                frequenciesList.add(frequency);
                            } else {
                                removedFrequenciesList.add(frequency);
                                layout_removed_frequencies.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                tableAdapter.loadedTables.add(new LoadedTable(tableNumber, frequenciesList.stream().mapToInt(Integer::intValue).toArray())); //Last table in the file
                if (!removedFrequenciesList.isEmpty())
                    removedFrequencies.add(new LoadedTable(tableNumber, removedFrequenciesList.stream().mapToInt(Integer::intValue).toArray()));

                tableAdapter.notifyDataSetChanged();
                setVisibility(ValueCodes.DOWNLOADED);

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
        setVisibility(ValueCodes.OVERVIEW);
        tableAdapter.setData(data);
        rv_item.setAdapter(tableAdapter);
        rv_item.setLayoutManager(new LinearLayoutManager(this));
        tableAdapter.isFile = false;
        TransferBleData.readDetectionFilter();
    }

    private void downloadDetectionFilter(byte[] data) {
        tableAdapter.isTemperature = data[1] == ValueCodes.VARIABLE && data[11] == ValueCodes.VARIABLE_TEMPERATURE;
    }

    private boolean setTable(LoadedTable loadedTable) {
        byte[] b = Converters.setCalendar(244);
        b[0] = (byte) 0x7E;
        b[7] = (byte) loadedTable.tableNumber;
        b[8] = (byte) loadedTable.frequenciesLoaded.length;
        b[9] = (byte) tableAdapter.getBaseFrequency();
        int index = 10;
        int i = 0;
        while (i < loadedTable.frequenciesLoaded.length) {
            b[index] = (byte) ((loadedTable.frequenciesLoaded[i] - (tableAdapter.getBaseFrequency() * 1000)) / 256);
            b[index + 1] = (byte) ((loadedTable.frequenciesLoaded[i] - (tableAdapter.getBaseFrequency() * 1000)) % 256);
            index += 2;
            i++;
        }
        return TransferBleData.writeFrequencies(b);
    }

    private void showAlertDialog(Uri uri, String uriString, Cursor[] cursorContainer) {
        AlertDialog dialog = Dialogs.createLoadingDialog(this, getString(R.string.lb_importing_tables));
        dialogList.add(dialog);
        dialog.setOnDismissListener(d -> dialogList.remove(dialog));
        dialog.show();

        ProgressBar loading_progressBar = dialog.findViewById(R.id.pb_loading);
        ImageView loaded_imageView = dialog.findViewById(R.id.img_loaded);
        TextView state_loading_textView = dialog.findViewById(R.id.tv_state_loading);
        loadingHandler = new Handler();
        messageHandler.postDelayed(() -> {
            state_loading_textView.setText(getString(R.string.lb_frequencies_imported));
            loading_progressBar.setVisibility(View.GONE);
            loaded_imageView.setVisibility(View.VISIBLE);
            if (uriString.startsWith("content://")) {
                try {
                    cursorContainer[0] = getBaseContext().getContentResolver().query(uri, null, null, null, null);
                } catch (Exception ex) {
                    dialog.dismiss();
                    showAlertDialog(getString(R.string.lb_error), getString(R.string.lb_error_push), false);
                    tableAdapter.isFile = false;
                }
            }
            loadingHandler.postDelayed(() -> {
                if (uriString.startsWith("file://")) {
                    readFile(uri);
                } else if (cursorContainer[0] != null && cursorContainer[0].moveToFirst()) {
                    readFile(uri);
                    cursorContainer[0].close();
                }
                dialog.dismiss();
            }, ValueCodes.MESSAGE_PERIOD);
        }, ValueCodes.MESSAGE_PERIOD);
    }

    private void showAlertDialog(String title, String message, boolean showOptions) {
        AlertDialog dialog = Dialogs.createErrorDialog(this, title, message);
        dialogList.add(dialog);
        dialog.setOnDismissListener(d -> dialogList.remove(dialog));
        dialog.show();

        if (showOptions) {
            LinearLayout options_alert_linearLayout = dialog.findViewById(R.id.layout_options_alert);
            Button cancel_upload_button = dialog.findViewById(R.id.btn_cancel_upload);
            options_alert_linearLayout.setVisibility(View.VISIBLE);
            cancel_upload_button.setOnClickListener(v -> {
                setVisibility(ValueCodes.OVERVIEW);
                tableAdapter.isFile = false;
                tableAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });
        }
    }

    private void showAlertDialog(String title, boolean isRemoved, List<LoadedTable> frequencies) {
        AlertDialog dialog = Dialogs.createLoadedFrequenciesDialog(this, title, frequencies, isRemoved);
        dialogList.add(dialog);
        dialog.setOnDismissListener(d -> dialogList.remove(dialog));
        dialog.show();
    }
}