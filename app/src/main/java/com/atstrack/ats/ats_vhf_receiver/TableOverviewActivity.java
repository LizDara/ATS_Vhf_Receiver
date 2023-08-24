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
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.BluetoothLeService;
import com.atstrack.ats.ats_vhf_receiver.Utils.AtsVhfReceiverUuids;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
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
    @BindView(R.id.table1_frequency)
    TextView table1_frequency;
    @BindView(R.id.table2_frequency)
    TextView table2_frequency;
    @BindView(R.id.table3_frequency)
    TextView table3_frequency;
    @BindView(R.id.table4_frequency)
    TextView table4_frequency;
    @BindView(R.id.table5_frequency)
    TextView table5_frequency;
    @BindView(R.id.table6_frequency)
    TextView table6_frequency;
    @BindView(R.id.table7_frequency)
    TextView table7_frequency;
    @BindView(R.id.table8_frequency)
    TextView table8_frequency;
    @BindView(R.id.table9_frequency)
    TextView table9_frequency;
    @BindView(R.id.table10_frequency)
    TextView table10_frequency;
    @BindView(R.id.table11_frequency)
    TextView table11_frequency;
    @BindView(R.id.table12_frequency)
    TextView table12_frequency;
    @BindView(R.id.google_drive_webView)
    WebView google_drive_webView;
    @BindView(R.id.google_drive_linearLayout)
    LinearLayout google_drive_linearLayout;
    @BindView(R.id.table_overview_linearLayout)
    LinearLayout table_overview_linearLayout;

    final private String TAG = TableOverviewActivity.class.getSimpleName();

    //private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_OPEN_STORAGE = 3;

    private int[] data;
    private int[][] tables;
    private int baseFrequency;
    private int range;

    /*private DriveServiceHelper driveServiceHelper;
    private String fileUrl;
    private String fileId;*/

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
                        onClickFrequencies();
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
    private void onClickFrequencies() {
        UUID service = AtsVhfReceiverUuids.UUID_SERVICE_STORED_DATA;
        UUID characteristic = AtsVhfReceiverUuids.UUID_CHARACTERISTIC_FREQ_TABLE;
        mBluetoothLeService.readCharacteristicDiagnostic(service, characteristic);
    }

    @OnClick(R.id.load_from_file_button)
    public void onClickLoadTablesFromFile(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().getPath()), "*/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_STORAGE);
        /*requestSignIn();

        google_drive_webView.loadUrl("https://drive.google.com/drive/my-drive");
        table_overview_linearLayout.setVisibility(View.GONE);
        google_drive_linearLayout.setVisibility(View.VISIBLE);*/
    }

    /*@OnClick(R.id.ok_drive_button)
    public void onClickOK(View v) {
        fileId = findFileId();
        requestSignIn();
    }

    public String findFileId() {
        String[] word = fileUrl.split("/");
        return word[5];
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, "LOAD: "+url);
            fileUrl = url;
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(new Scope(DriveScopes.DRIVE_FILE)).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
            @Override
            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        TableOverviewActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));

                credential.setSelectedAccount(googleSignInAccount.getAccount());

                Drive googleDriveService = new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName("ATS VHF Receiver").build();

                driveServiceHelper = new DriveServiceHelper(googleDriveService);

                downloadFile();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void readFile() {
        if (driveServiceHelper != null) {
            Log.d(TAG, "Reading file " + fileId);

            driveServiceHelper.readFile(fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;

                        Log.i(TAG, name);
                        Log.i(TAG, content);

                        google_drive_linearLayout.setVisibility(View.GONE);
                        table_overview_linearLayout.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't read file.", exception));
        }
    }

    public void downloadFile() {
        if (driveServiceHelper != null) {
            Log.d(TAG, "Downloading file " + fileId);

            driveServiceHelper.downloadFile(fileId)
                    .addOnSuccessListener(outputStream -> {
                        Log.i(TAG, "Downloading...");
                    })
                    .addOnFailureListener(exception -> Log.e(TAG, "Couldn't download file.", exception));
        }
    }*/

    @OnClick(R.id.table_1_linearLayout)
    public void onClickTable1(View v) {
        setTableData(1);
    }

    @OnClick(R.id.table_2_linearLayout)
    public void onClickTable2(View v) {
        setTableData(2);
    }

    @OnClick(R.id.table_3_linearLayout)
    public void onClickTable3(View v) {
        setTableData(3);
    }

    @OnClick(R.id.table_4_linearLayout)
    public void onClickTable4(View v) {
        setTableData(4);
    }

    @OnClick(R.id.table_5_linearLayout)
    public void onClickTable5(View v) {
        setTableData(5);
    }

    @OnClick(R.id.table_6_linearLayout)
    public void onClickTable6(View v) {
        setTableData(6);
    }

    @OnClick(R.id.table_7_linearLayout)
    public void onClickTable7(View v) {
        setTableData(7);
    }

    @OnClick(R.id.table_8_linearLayout)
    public void onClickTable8(View v) {
        setTableData(8);
    }

    @OnClick(R.id.table_9_linearLayout)
    public void onClickTable9(View v) {
        setTableData(9);
    }

    @OnClick(R.id.table_10_linearLayout)
    public void onClickTable10(View v) {
        setTableData(10);
    }

    @OnClick(R.id.table_11_linearLayout)
    public void onClickTable11(View v) {
        setTableData(11);
    }

    @OnClick(R.id.table_12_linearLayout)
    public void onClickTable12(View v) {
        setTableData(12);
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
        tables = new int[12][];

        //google_drive_webView.getSettings().setJavaScriptEnabled(true);
        //google_drive_webView.setWebViewClient(new Callback());

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
                /*case REQUEST_CODE_SIGN_IN:
                if (resultCode == RESULT_OK)
                    handleSignInIntent(data);
                break;*/
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
                Log.i(TAG, "NEW PATH: " + newPath);
                File file = new File(Environment.getExternalStorageDirectory(), newPath);
                FileInputStream fileInputStream = new FileInputStream(file);

                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader =  new BufferedReader(inputStreamReader);

                String line;
                int tableNumber = 0;
                List<Integer> frequenciesList = new LinkedList<>();
                while ((line = bufferedReader.readLine()) != null) { // Reads each line of the file and add it to the list
                    if (line.toUpperCase().contains("TABLE")) {
                        if (tableNumber > 0) {
                            data[tableNumber] = frequenciesList.size();
                            tables[tableNumber - 1] = new int[frequenciesList.size()];
                            for (int i = 0; i < frequenciesList.size(); i++)
                                tables[tableNumber - 1][i] = frequenciesList.get(i);
                        }
                        tableNumber = Integer.parseInt(line.toUpperCase().replace("TABLE", "").replace(" ", ""));
                        frequenciesList = new LinkedList<>();
                    } else {
                        frequenciesList.add(Integer.parseInt(line));
                    }
                } //Last table in the file
                data[tableNumber] = frequenciesList.size();
                tables[tableNumber - 1] = new int[frequenciesList.size()];
                for (int i = 0; i < frequenciesList.size(); i++)
                    tables[tableNumber - 1][i] = frequenciesList.get(i);

                parameter = "";
                table1_frequency.setText(data[1] + " frequencies");
                table2_frequency.setText(data[2] + " frequencies");
                table3_frequency.setText(data[3] + " frequencies");
                table4_frequency.setText(data[4] + " frequencies");
                table5_frequency.setText(data[5] + " frequencies");
                table6_frequency.setText(data[6] + " frequencies");
                table7_frequency.setText(data[7] + " frequencies");
                table8_frequency.setText(data[8] + " frequencies");
                table9_frequency.setText(data[9] + " frequencies");
                table10_frequency.setText(data[10] + " frequencies");
                table11_frequency.setText(data[11] + " frequencies");
                table12_frequency.setText(data[12] + " frequencies");

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
            Log.i(TAG, "TOTALES: " + Converters.getDecimalValue(packet));
            table1_frequency.setText(Converters.getDecimalValue(packet[1]) + " frequencies");
            table2_frequency.setText(Converters.getDecimalValue(packet[2]) + " frequencies");
            table3_frequency.setText(Converters.getDecimalValue(packet[3]) + " frequencies");
            table4_frequency.setText(Converters.getDecimalValue(packet[4]) + " frequencies");
            table5_frequency.setText(Converters.getDecimalValue(packet[5]) + " frequencies");
            table6_frequency.setText(Converters.getDecimalValue(packet[6]) + " frequencies");
            table7_frequency.setText(Converters.getDecimalValue(packet[7]) + " frequencies");
            table8_frequency.setText(Converters.getDecimalValue(packet[8]) + " frequencies");
            table9_frequency.setText(Converters.getDecimalValue(packet[9]) + " frequencies");
            table10_frequency.setText(Converters.getDecimalValue(packet[10]) + " frequencies");
            table11_frequency.setText(Converters.getDecimalValue(packet[11]) + " frequencies");
            table12_frequency.setText(Converters.getDecimalValue(packet[12]) + " frequencies");

            baseFrequency = Integer.parseInt(Converters.getDecimalValue(packet[13]));
            range = Integer.parseInt(Converters.getDecimalValue(packet[14]));

            data = new int[packet.length];
            for (int i = 0; i < packet.length; i++) {
                data[i] = Integer.parseInt(Converters.getDecimalValue(packet[i]));
            }
        }
    }

    /**
     * Redirects to the next activity to edit the selected table.
     *
     * @param number The table number to edit.
     */
    private void setTableData(int number) {
        Intent intent = new Intent(this, EditTablesActivity.class);
        intent.putExtra("number", number);
        intent.putExtra("total", data[number]);
        intent.putExtra("baseFrequency", baseFrequency);
        intent.putExtra("range", range);
        intent.putExtra("isFile", tables[number - 1] != null);
        if (tables[number - 1] != null)
            intent.putExtra("frequencies", tables[number - 1]);
        startActivity(intent);
    }
}

