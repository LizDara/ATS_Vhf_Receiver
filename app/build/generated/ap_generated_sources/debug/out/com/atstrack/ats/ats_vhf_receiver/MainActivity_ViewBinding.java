// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity_ViewBinding implements Unbinder {
  private MainActivity target;

  private View view7f0a0154;

  private View view7f0a0069;

  private View view7f0a024f;

  private View view7f0a01df;

  private View view7f0a01de;

  @UiThread
  public MainActivity_ViewBinding(MainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MainActivity_ViewBinding(final MainActivity target, View source) {
    this.target = target;

    View view;
    target.location_enable_linearLayout = Utils.findRequiredViewAsType(source, R.id.location_enable_linearLayout, "field 'location_enable_linearLayout'", LinearLayout.class);
    target.location_textView = Utils.findRequiredViewAsType(source, R.id.location_textView, "field 'location_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.location_button, "field 'location_button' and method 'enableLocation'");
    target.location_button = Utils.castView(view, R.id.location_button, "field 'location_button'", Button.class);
    view7f0a0154 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.enableLocation(p0);
      }
    });
    target.bluetooth_enable_linearLayout = Utils.findRequiredViewAsType(source, R.id.bluetooth_enable_linearLayout, "field 'bluetooth_enable_linearLayout'", LinearLayout.class);
    target.bluetooth_textView = Utils.findRequiredViewAsType(source, R.id.bluetooth_textView, "field 'bluetooth_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.bluetooth_button, "field 'bluetooth_button' and method 'enableBluetooth'");
    target.bluetooth_button = Utils.castView(view, R.id.bluetooth_button, "field 'bluetooth_button'", Button.class);
    view7f0a0069 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.enableBluetooth(p0);
      }
    });
    target.version_textView = Utils.findRequiredViewAsType(source, R.id.version_textView, "field 'version_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.switch_dark_mode, "field 'switch_dark_mode' and method 'onDarkModeClick'");
    target.switch_dark_mode = Utils.castView(view, R.id.switch_dark_mode, "field 'switch_dark_mode'", SwitchCompat.class);
    view7f0a024f = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onDarkModeClick(p0);
      }
    });
    target.searching_receivers_linearLayout = Utils.findRequiredViewAsType(source, R.id.searching_receivers_linearLayout, "field 'searching_receivers_linearLayout'", LinearLayout.class);
    target.device_recyclerView = Utils.findRequiredViewAsType(source, R.id.device_recyclerView, "field 'device_recyclerView'", RecyclerView.class);
    target.retry_linearLayout = Utils.findRequiredViewAsType(source, R.id.retry_linearLayout, "field 'retry_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.retry_button, "field 'retry_button' and method 'onClickRetry'");
    target.retry_button = Utils.castView(view, R.id.retry_button, "field 'retry_button'", Button.class);
    view7f0a01df = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickRetry(p0);
      }
    });
    target.update_linearLayout = Utils.findRequiredViewAsType(source, R.id.update_linearLayout, "field 'update_linearLayout'", LinearLayout.class);
    target.devices_linearLayout = Utils.findRequiredViewAsType(source, R.id.devices_linearLayout, "field 'devices_linearLayout'", LinearLayout.class);
    target.branding_constraintLayout = Utils.findRequiredViewAsType(source, R.id.branding_constraintLayout, "field 'branding_constraintLayout'", ConstraintLayout.class);
    target.searching_receivers_constraintLayout = Utils.findRequiredViewAsType(source, R.id.searching_receivers_constraintLayout, "field 'searching_receivers_constraintLayout'", ConstraintLayout.class);
    view = Utils.findRequiredView(source, R.id.refresh_button, "field 'refresh_button' and method 'onClickRefresh'");
    target.refresh_button = Utils.castView(view, R.id.refresh_button, "field 'refresh_button'", ImageButton.class);
    view7f0a01de = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickRefresh(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    MainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.location_enable_linearLayout = null;
    target.location_textView = null;
    target.location_button = null;
    target.bluetooth_enable_linearLayout = null;
    target.bluetooth_textView = null;
    target.bluetooth_button = null;
    target.version_textView = null;
    target.switch_dark_mode = null;
    target.searching_receivers_linearLayout = null;
    target.device_recyclerView = null;
    target.retry_linearLayout = null;
    target.retry_button = null;
    target.update_linearLayout = null;
    target.devices_linearLayout = null;
    target.branding_constraintLayout = null;
    target.searching_receivers_constraintLayout = null;
    target.refresh_button = null;

    view7f0a0154.setOnClickListener(null);
    view7f0a0154 = null;
    view7f0a0069.setOnClickListener(null);
    view7f0a0069 = null;
    view7f0a024f.setOnClickListener(null);
    view7f0a024f = null;
    view7f0a01df.setOnClickListener(null);
    view7f0a01df = null;
    view7f0a01de.setOnClickListener(null);
    view7f0a01de = null;
  }
}
