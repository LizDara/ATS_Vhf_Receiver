package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
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
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.LeServiceConnection;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ActivitySetting;
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
import java.util.Objects;
import java.util.UUID;

public class VhfTablesActivity extends AppCompatActivity {

    @BindView(R.id.tables_listView)
    ListView tables_listView;

    final private String TAG = VhfTablesActivity.class.getSimpleName();

    private final Context mContext = this;
    private TableListAdapter tableListAdapter;

    private final LeServiceConnection leServiceConnection = LeServiceConnection.getInstance();
    private String parameter = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    Message.showDisconnectionMessage(mContext, 0);
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals(ValueCodes.TABLES)) // Gets the number of frequencies from each table
                        TransferBleData.readTables(false);
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (packet == null) return;
                    if (parameter.equals(ValueCodes.TABLES)) // Gets the number of frequencies from each table
                        downloadData(packet);
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhf_tables);
        ButterKnife.bind(this);
        ActivitySetting.setToolbar(this, R.string.edit_frequency_tables);
        ActivitySetting.setReceiverStatus(this);

        parameter = ValueCodes.TABLES;

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, leServiceConnection.getServiceConnection(), BIND_AUTO_CREATE);
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
        registerReceiver(mGattUpdateReceiver, Converters.makeFirstGattUpdateIntentFilter());
        leServiceConnection.getBluetoothLeService().discovering();
        Log.i(TAG, "ON RESUME");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(leServiceConnection.getServiceConnection());
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
     * @param packet The received packet.
     */
    private void downloadData(byte[] packet) {
        tableListAdapter = new TableListAdapter(this, packet);
        tables_listView.setAdapter(tableListAdapter);
    }
}