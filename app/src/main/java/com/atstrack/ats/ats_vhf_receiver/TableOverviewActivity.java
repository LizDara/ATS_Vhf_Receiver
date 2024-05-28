package com.atstrack.ats.ats_vhf_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TableListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverInformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TableOverviewActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_toolbar)
    TextView title_toolbar;
    @BindView(R.id.state_view)
    View state_view;
    @BindView(R.id.device_name_textView)
    TextView device_name_textView;
    @BindView(R.id.device_status_textView)
    TextView device_status_textView;
    @BindView(R.id.percent_battery_textView)
    TextView percent_battery_textView;
    @BindView(R.id.tables_listView)
    ListView tables_listView;

    final private String TAG = TableOverviewActivity.class.getSimpleName();

    private static final int REQUEST_CODE_OPEN_STORAGE = 3;

    private int[][] tables;
    private TableListAdapter tableListAdapter;

    private ReceiverInformation receiverInformation;
    private BluetoothLeService mBluetoothLeService;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean mConnected = true;
    private String parameter = "";

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (parameter.equals("tables")) // Gets the number of frequencies from each table
                        onClickTables();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (parameter.equals("tables")) // Gets the number of frequencies from each table
                        downloadData(packet);
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * Requests a read for get the number of frequencies from each table and display it.
     * Service name: StoredData.
     * Characteristic name: FreqTable.
     */
    private void onClickTables() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    @OnClick(R.id.load_from_file_button)
    public void onClickLoadTablesFromFile(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().getPath()), "*/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_STORAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_overview);
        ButterKnife.bind(this);

        // Customize the activity menu
        setSupportActionBar(toolbar);
        title_toolbar.setText(R.string.edit_frequency_tables);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get device data from previous activity
        receiverInformation = ReceiverInformation.getReceiverInformation();

        device_name_textView.setText(receiverInformation.getDeviceName());
        device_status_textView.setText(receiverInformation.getDeviceStatus());
        percent_battery_textView.setText(receiverInformation.getPercentBattery());

        // Gets the number of frequencies from each table
        parameter = "tables";

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OPEN_STORAGE) {
            if (resultCode == RESULT_OK) { // Gets the Uri of the selected file
                Uri uri = data.getData();
                String uriString = uri.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();
                if (uriString.startsWith("content://")) {
                    try (Cursor cursor = getBaseContext().getContentResolver().query(uri, null, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            @SuppressLint("Range") String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            Log.i(TAG, "FILE NAME: " + fileName);
                            readFile(path);
                        }
                    } catch (Exception ex) {
                        Log.i(TAG, "Cursor exception: " + ex.toString());
                    }
                } else if (uriString.startsWith("file://")) {
                    readFile(path);
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
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(receiverInformation.getDeviceAddress());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mConnected)
            showDisconnectionMessage();
        return true;
    }

    /**
     * Shows an alert dialog because the connection with the BLE device was lost or the client disconnected it.
     */
    private void showDisconnectionMessage() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.disconnect_message, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        dialog.setView(view);
        dialog.show();

        // The message disappears after a pre-defined period and will search for other available BLE devices again
        int MESSAGE_PERIOD = 3000;
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, MESSAGE_PERIOD);
    }

    /**
     * Reads a file from the local storage and get the frequencies from each table.
     *
     * @param path The directory path where is the file.
     */
    private void readFile(String path) {
        if (isExternalStorageReadable()) {
            try {
                // Gets the selected file
                String newPath = findPath(path);
                File file = new File(Environment.getExternalStorageDirectory(), newPath);
                FileInputStream fileInputStream = new FileInputStream(file);

                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader =  new BufferedReader(inputStreamReader);

                String line;
                int tableNumber = 0;
                tables = new int[12][];
                List<Integer> frequenciesList = new LinkedList<>();
                while ((line = bufferedReader.readLine()) != null) { // Reads each line of the file and add it to the list
                    line = line.replace(" ", "");
                    if (line.toUpperCase().contains("TABLE")) {
                        if (tableNumber > 0) {
                            tableListAdapter.setFrequenciesNumber(tableNumber, (byte) frequenciesList.size());
                            tables[tableNumber - 1] = new int[frequenciesList.size()];
                            for (int i = 0; i < frequenciesList.size(); i++)
                                tables[tableNumber - 1][i] = frequenciesList.get(i);
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

                parameter = "";
                tableListAdapter.setFrequenciesFile(tables);
                tableListAdapter.notifyDataSetChanged();

                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Cannot read from external storage");
        }
    }

    /**
     * Finds the directory path of the selected file.
     *
     * @param path The directory path where is the file.
     *
     * @return Returns the directory path separated by /.
     */
    private String findPath(String path) {
        String[] splitPath = path.split("%");
        String newPath = "";
        for (int i = 1; i < splitPath.length; i++) {
            newPath += "/" + splitPath[i].substring(2);
        }
        return newPath;
    }

    /**
     * Checks if external storage is readable.
     *
     * @return Returns true, if external storage is readable.
     */
    private boolean isExternalStorageReadable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    /**
     * With the received packet, gets the number of frequencies from each table and display on the screen.
     *
     * @param packet The received packet.
     */
    private void downloadData(byte[] packet) {
        if (packet.length == 1) {
            mBluetoothLeService.discovering();
        } else {
            tableListAdapter = new TableListAdapter(this, packet);
            tables_listView.setAdapter(tableListAdapter);
        }
    }
}