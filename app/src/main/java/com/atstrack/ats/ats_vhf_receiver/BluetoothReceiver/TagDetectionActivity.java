package com.atstrack.ats.ats_vhf_receiver.BluetoothReceiver;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atstrack.ats.ats_vhf_receiver.Adapters.TagListAdapter;
import com.atstrack.ats.ats_vhf_receiver.BaseActivity;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.GattUpdateReceiver;
import com.atstrack.ats.ats_vhf_receiver.BluetoothATS.TransferBleData;
import com.atstrack.ats.ats_vhf_receiver.R;
import com.atstrack.ats.ats_vhf_receiver.Utils.Converters;
import com.atstrack.ats.ats_vhf_receiver.Utils.Message;
import com.atstrack.ats.ats_vhf_receiver.Utils.ReceiverCallback;
import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

import butterknife.BindView;
import butterknife.OnClick;

public class TagDetectionActivity extends BaseActivity {

    @BindView(R.id.location_data_imageView)
    ImageView location_data_imageView;
    @BindView(R.id.location_data_textView)
    TextView location_data_textView;
    @BindView(R.id.coordinates_textView)
    TextView coordinates_textView;
    @BindView(R.id.location_data_button)
    Button location_data_button;
    @BindView(R.id.item_recyclerView)
    RecyclerView item_recyclerView;

    private final static String TAG = TagDetectionActivity.class.getSimpleName();

    private TagListAdapter tagListAdapter;
    private boolean enable;

    @OnClick(R.id.location_data_button)
    public void onClickLocation(View v) {
        enable = !enable;
        location_data_imageView.setBackground(ContextCompat.getDrawable(this, enable ? R.drawable.ic_gps_valid : R.drawable.ic_gps_off));
        location_data_textView.setText(enable ? R.string.lb_location_enabled : R.string.lb_location_disabled);
        coordinates_textView.setText(enable ? "00.000000, -00.000000" : getString(R.string.lb_location_unknown));
        location_data_button.setText(enable ? R.string.lb_disable : R.string.lb_enable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_bluetooth_tag_detection;
        showToolbar = true;
        title = getString(R.string.tag_detection);
        deviceCategory = ValueCodes.BLUETOOTH_RECEIVER;
        super.onCreate(savedInstanceState);

        initializeCallback();
        enable = false;
        tagListAdapter = new TagListAdapter(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        item_recyclerView.setLayoutManager(manager);
        item_recyclerView.setHasFixedSize(true);
        item_recyclerView.setAdapter(tagListAdapter);
    }

    private void initializeCallback() {
        receiverCallback = new ReceiverCallback() {
            @Override
            public void onGattDisconnected() {
                unbindService(leServiceConnection.getServiceConnection());
                Message.showDisconnectionMessage(mContext);
            }

            @Override
            public void onGattDiscovered() {
                TransferBleData.receiveTags();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                Log.i(TAG, Converters.getHexValue(packet));
                setDetectionTagsData(packet);
            }
        };
        gattUpdateReceiver = new GattUpdateReceiver(receiverCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { //Disconnect
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDetectionTagsData(byte[] data) {
        int position = -1;
        for (int i = 0; i < tagListAdapter.getItemCount(); i++) {
            if (tagListAdapter.getTag(i).code.equals(Converters.getAsciiValue(6, 14, data)))
                position = i;
        }
        if (position == -1)
            tagListAdapter.addTag(data);
        else
            tagListAdapter.setTag(position, data);
        tagListAdapter.notifyDataSetChanged();
    }
}