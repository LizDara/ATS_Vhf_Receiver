// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity_ViewBinding implements Unbinder {
  private MainActivity target;

  private View view7f0a007a;

  private View view7f0a0096;

  private View view7f0a01e7;

  @UiThread
  public MainActivity_ViewBinding(MainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MainActivity_ViewBinding(final MainActivity target, View source) {
    this.target = target;

    View view;
    target.main_toolbar = Utils.findRequiredViewAsType(source, R.id.main_toolbar, "field 'main_toolbar'", Toolbar.class);
    target.main_title_toolbar = Utils.findRequiredViewAsType(source, R.id.main_title_toolbar, "field 'main_title_toolbar'", TextView.class);
    target.version_textView = Utils.findRequiredViewAsType(source, R.id.version_textView, "field 'version_textView'", TextView.class);
    target.splash_screen_constraintLayout = Utils.findRequiredViewAsType(source, R.id.splash_screen_constraintLayout, "field 'splash_screen_constraintLayout'", ConstraintLayout.class);
    target.bridge_app_linearLayout = Utils.findRequiredViewAsType(source, R.id.bridge_app_linearLayout, "field 'bridge_app_linearLayout'", LinearLayout.class);
    target.bridge_subtitle_textView = Utils.findRequiredViewAsType(source, R.id.bridge_subtitle_textView, "field 'bridge_subtitle_textView'", TextView.class);
    target.bridge_message_textView = Utils.findRequiredViewAsType(source, R.id.bridge_message_textView, "field 'bridge_message_textView'", TextView.class);
    target.types_subtitle_textView = Utils.findRequiredViewAsType(source, R.id.types_subtitle_textView, "field 'types_subtitle_textView'", TextView.class);
    target.category_recyclerView = Utils.findRequiredViewAsType(source, R.id.category_recyclerView, "field 'category_recyclerView'", RecyclerView.class);
    target.select_device_constraintLayout = Utils.findRequiredViewAsType(source, R.id.select_device_constraintLayout, "field 'select_device_constraintLayout'", ConstraintLayout.class);
    target.searching_progressBar = Utils.findRequiredViewAsType(source, R.id.searching_progressBar, "field 'searching_progressBar'", ProgressBar.class);
    target.searching_devices_linearLayout = Utils.findRequiredViewAsType(source, R.id.searching_devices_linearLayout, "field 'searching_devices_linearLayout'", LinearLayout.class);
    target.devices_subtitle_textView = Utils.findRequiredViewAsType(source, R.id.devices_subtitle_textView, "field 'devices_subtitle_textView'", TextView.class);
    target.searching_message_textView = Utils.findRequiredViewAsType(source, R.id.searching_message_textView, "field 'searching_message_textView'", TextView.class);
    target.devices_scrollView = Utils.findRequiredViewAsType(source, R.id.devices_scrollView, "field 'devices_scrollView'", ScrollView.class);
    target.device_recyclerView = Utils.findRequiredViewAsType(source, R.id.device_recyclerView, "field 'device_recyclerView'", RecyclerView.class);
    target.connecting_device_linearLayout = Utils.findRequiredViewAsType(source, R.id.connecting_device_linearLayout, "field 'connecting_device_linearLayout'", LinearLayout.class);
    target.selected_device_scrollView = Utils.findRequiredViewAsType(source, R.id.selected_device_scrollView, "field 'selected_device_scrollView'", ScrollView.class);
    target.connected_imageView = Utils.findRequiredViewAsType(source, R.id.connected_imageView, "field 'connected_imageView'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.cancel_button, "field 'cancel_button' and method 'onClickCancel'");
    target.cancel_button = Utils.castView(view, R.id.cancel_button, "field 'cancel_button'", Button.class);
    view7f0a007a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickCancel(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.connect_button, "field 'connect_button' and method 'onClickConnect'");
    target.connect_button = Utils.castView(view, R.id.connect_button, "field 'connect_button'", Button.class);
    view7f0a0096 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickConnect(p0);
      }
    });
    target.no_device_found_linearLayout = Utils.findRequiredViewAsType(source, R.id.no_device_found_linearLayout, "field 'no_device_found_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.retry_button, "method 'onClickRetry'");
    view7f0a01e7 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickRetry(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    MainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.main_toolbar = null;
    target.main_title_toolbar = null;
    target.version_textView = null;
    target.splash_screen_constraintLayout = null;
    target.bridge_app_linearLayout = null;
    target.bridge_subtitle_textView = null;
    target.bridge_message_textView = null;
    target.types_subtitle_textView = null;
    target.category_recyclerView = null;
    target.select_device_constraintLayout = null;
    target.searching_progressBar = null;
    target.searching_devices_linearLayout = null;
    target.devices_subtitle_textView = null;
    target.searching_message_textView = null;
    target.devices_scrollView = null;
    target.device_recyclerView = null;
    target.connecting_device_linearLayout = null;
    target.selected_device_scrollView = null;
    target.connected_imageView = null;
    target.cancel_button = null;
    target.connect_button = null;
    target.no_device_found_linearLayout = null;

    view7f0a007a.setOnClickListener(null);
    view7f0a007a = null;
    view7f0a0096.setOnClickListener(null);
    view7f0a0096 = null;
    view7f0a01e7.setOnClickListener(null);
    view7f0a01e7 = null;
  }
}
