// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainMenuActivity_ViewBinding implements Unbinder {
  private MainMenuActivity target;

  private View view7f0a00c4;

  private View view7f0a0232;

  private View view7f0a01d0;

  private View view7f0a0153;

  private View view7f0a00bb;

  @UiThread
  public MainMenuActivity_ViewBinding(MainMenuActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MainMenuActivity_ViewBinding(final MainMenuActivity target, View source) {
    this.target = target;

    View view;
    target.menu_linearLayout = Utils.findRequiredViewAsType(source, R.id.menu_linearLayout, "field 'menu_linearLayout'", LinearLayout.class);
    target.vhf_constraintLayout = Utils.findRequiredViewAsType(source, R.id.vhf_constraintLayout, "field 'vhf_constraintLayout'", ConstraintLayout.class);
    target.state_textView = Utils.findRequiredViewAsType(source, R.id.state_textView, "field 'state_textView'", TextView.class);
    target.status_textView = Utils.findRequiredViewAsType(source, R.id.status_device_menu_textView, "field 'status_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.disconnect_button, "field 'disconnect_button' and method 'onClickDisconnect'");
    target.disconnect_button = Utils.castView(view, R.id.disconnect_button, "field 'disconnect_button'", TextView.class);
    view7f0a00c4 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDisconnect(p0);
      }
    });
    target.connecting_device_linearLayout = Utils.findRequiredViewAsType(source, R.id.connecting_device_linearLayout, "field 'connecting_device_linearLayout'", LinearLayout.class);
    target.percent_battery_menu_textView = Utils.findRequiredViewAsType(source, R.id.percent_battery_menu_textView, "field 'percent_battery_menu_textView'", TextView.class);
    target.connecting_progressBar = Utils.findRequiredViewAsType(source, R.id.connecting_progressBar, "field 'connecting_progressBar'", ProgressBar.class);
    target.connected_imageView = Utils.findRequiredViewAsType(source, R.id.connected_imageView, "field 'connected_imageView'", ImageView.class);
    target.sd_card_menu_textView = Utils.findRequiredViewAsType(source, R.id.sd_card_menu_textView, "field 'sd_card_menu_textView'", TextView.class);
    target.battery_menu_imageView = Utils.findRequiredViewAsType(source, R.id.battery_menu_imageView, "field 'battery_menu_imageView'", ImageView.class);
    target.sd_card_menu_imageView = Utils.findRequiredViewAsType(source, R.id.sd_card_menu_imageView, "field 'sd_card_menu_imageView'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.start_scanning_button, "method 'onClickStartScanning'");
    view7f0a0232 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStartScanning(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.receiver_configuration_button, "method 'onClickReceiverConfiguration'");
    view7f0a01d0 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickReceiverConfiguration(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.manage_receiver_data_button, "method 'onClickManageReceiverData'");
    view7f0a0153 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickManageReceiverData(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.diagnostics_button, "method 'onClickDiagnostics'");
    view7f0a00bb = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDiagnostics(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    MainMenuActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.menu_linearLayout = null;
    target.vhf_constraintLayout = null;
    target.state_textView = null;
    target.status_textView = null;
    target.disconnect_button = null;
    target.connecting_device_linearLayout = null;
    target.percent_battery_menu_textView = null;
    target.connecting_progressBar = null;
    target.connected_imageView = null;
    target.sd_card_menu_textView = null;
    target.battery_menu_imageView = null;
    target.sd_card_menu_imageView = null;

    view7f0a00c4.setOnClickListener(null);
    view7f0a00c4 = null;
    view7f0a0232.setOnClickListener(null);
    view7f0a0232 = null;
    view7f0a01d0.setOnClickListener(null);
    view7f0a01d0 = null;
    view7f0a0153.setOnClickListener(null);
    view7f0a0153 = null;
    view7f0a00bb.setOnClickListener(null);
    view7f0a00bb = null;
  }
}
