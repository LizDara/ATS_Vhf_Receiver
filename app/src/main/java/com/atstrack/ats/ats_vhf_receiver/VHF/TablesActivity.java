package com.atstrack.ats.ats_vhf_receiver.VHF;

import androidx.core.content.ContextCompat;

import butterknife.BindView;
import butterknife.OnClick;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
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

public class TablesActivity extends BaseActivity {

    @BindView(R.id.tables_listView)
    ListView tables_listView;

    final private String TAG = TablesActivity.class.getSimpleName();

    private TableListAdapter tableListAdapter;

    @OnClick(R.id.load_from_file_button)
    public void onClickLoadTablesFromFile(View v) {
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
        File externalFile = externalStorageVolumes[0];
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.parse(externalFile.getPath()), "text/plain");
        startActivityForResult(intent, ValueCodes.REQUEST_CODE_OPEN_STORAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_vhf_tables;
        showToolbar = true;
        deviceCategory = ValueCodes.VHF;
        title = getString(R.string.edit_frequency_tables);
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = getIntent().getExtras().getString(ValueCodes.PARAMETER, "");
        if (parameter.isEmpty()) {
            byte[] data = getIntent().getByteArrayExtra(ValueCodes.VALUE);
            downloadData(data);
        }
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                if (parameter.equals(ValueCodes.TABLES))
                    TransferBleData.readTables(false);
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (Converters.getHexValue(packet[0]).equals("88")) return;
                if (parameter.equals(ValueCodes.TABLES)) // Gets the number of frequencies from each table
                    downloadData(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ValueCodes.REQUEST_CODE_OPEN_STORAGE) {
            if (resultCode == RESULT_OK) { // Gets the Uri of the selected file
                Uri uri = data.getData();
                String uriString = uri.toString();
                if (uriString.startsWith("content://")) {
                    try (Cursor cursor = getBaseContext().getContentResolver().query(uri, null, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            readFile(uri);
                        }
                    } catch (Exception ex) {
                        Log.i(TAG, "Cursor exception: " + ex);
                    }
                } else if (uriString.startsWith("file://")) {
                    readFile(uri);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver.mGattUpdateReceiver, TransferBleData.makeFirstGattUpdateIntentFilter());
        leServiceConnection.getBluetoothLeService().discovering();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver.mGattUpdateReceiver);
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

                String line;
                int tableNumber = 0;
                String message = "";
                int[][] tables = new int[12][];
                List<Integer> frequenciesList = new LinkedList<>();
                while ((line = bufferedReader.readLine()) != null) { // Reads each line of the file and add it to the list
                    line = line.replace(" ", "");
                    if (line.toUpperCase().contains("TABLE")) {
                        if (tableNumber > 0) {
                            tableListAdapter.setFrequenciesNumber(tableNumber, (byte) frequenciesList.size());
                            tables[tableNumber - 1] = new int[frequenciesList.size()];
                            for (int i = 0; i < frequenciesList.size(); i++)
                                tables[tableNumber - 1][i] = frequenciesList.get(i);

                            message += "Table " + tableNumber + ", " + frequenciesList.size() + " frequencies loaded" + ValueCodes.CR + ValueCodes.LF;
                        }
                        tableNumber = Integer.parseInt(line.toUpperCase().replace("TABLE", ""));
                        frequenciesList = new LinkedList<>();
                    } else {
                        frequenciesList.add(Integer.parseInt(line));
                    }
                } //Last table in the file
                tableListAdapter.setFrequenciesNumber(tableNumber, (byte) frequenciesList.size());
                tables[tableNumber - 1] = new int[frequenciesList.size()];
                for (int i = 0; i < frequenciesList.size(); i++)
                    tables[tableNumber - 1][i] = frequenciesList.get(i);
                message += "Table " + tableNumber + ", " + frequenciesList.size() + " frequencies loaded." + ValueCodes.CR + ValueCodes.LF;

                parameter = "";
                tableListAdapter.setFrequenciesFile(tables);
                tableListAdapter.notifyDataSetChanged();

                fileInputStream.close();
                showMessage(message);
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

    private void showMessage(String message) {
        message += "You now must review each of these tables in order for each one to be stored.";
        Message.showMessage(this, "Message!", message);
    }

    /**
     * With the received packet, gets the number of frequencies from each table and display on the screen.
     * @param data The received packet.
     */
    private void downloadData(byte[] data) {
        if (Converters.getHexValue(data[0]).equals("7A")) {
            tableListAdapter = new TableListAdapter(this, data);
            tables_listView.setAdapter(tableListAdapter);
        } else {
            Message.showMessage(this, "Package found: " + Converters.getHexValue(data) + ". Package expected: 0x7A ...");
        }
    }
}