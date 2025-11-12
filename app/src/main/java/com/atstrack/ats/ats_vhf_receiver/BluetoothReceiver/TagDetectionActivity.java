package com.atstrack.ats.ats_vhf_receiver.BluetoothReceiver;

import android.os.Bundle;
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
        location_data_imageView.setBackground(ContextCompat.getDrawable(this, enable ? R.drawable.ic_gps_off : R.drawable.ic_gps_valid));
        location_data_textView.setText(enable ? R.string.lb_location_disabled : R.string.lb_location_enabled);
        coordinates_textView.setText(enable ? getString(R.string.lb_location_unknown) : "00.000000, -00.000000");
        location_data_button.setText(enable ? R.string.lb_enable : R.string.lb_disable);
        enable = !enable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentViewId = R.layout.activity_bluetooth_tag_detection;
        showToolbar = true;
        title = getString(R.string.tag_detection);
        deviceCategory = ValueCodes.BLUETOOTH_RECEIVER;
        super.onCreate(savedInstanceState);

        initializeCallback();
        parameter = ValueCodes.TAGS;
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
                if (parameter.equals(ValueCodes.TAGS))
                    TransferBleData.receiveTags();
            }

            @Override
            public void onGattDataAvailable(byte[] packet) {
                if (parameter.equals(ValueCodes.TAGS))
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
        tagListAdapter.addTag(data);
        tagListAdapter.notifyDataSetChanged();
    }
}