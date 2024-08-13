// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class TemporaryStationaryActivity_ViewBinding implements Unbinder {
  private TemporaryStationaryActivity target;

  private View view7f0a0247;

  private View view7f0a023e;

  private View view7f0a01d5;

  private View view7f0a01d6;

  private View view7f0a0111;

  private View view7f0a01ea;

  private View view7f0a01ed;

  private View view7f0a0185;

  private View view7f0a01ce;

  @UiThread
  public TemporaryStationaryActivity_ViewBinding(TemporaryStationaryActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public TemporaryStationaryActivity_ViewBinding(final TemporaryStationaryActivity target,
      View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.frequency_table_number_stationary_textView = Utils.findRequiredViewAsType(source, R.id.frequency_table_number_stationary_textView, "field 'frequency_table_number_stationary_textView'", TextView.class);
    target.scan_rate_seconds_stationary_textView = Utils.findRequiredViewAsType(source, R.id.scan_rate_seconds_stationary_textView, "field 'scan_rate_seconds_stationary_textView'", TextView.class);
    target.scan_timeout_seconds_stationary_textView = Utils.findRequiredViewAsType(source, R.id.scan_timeout_seconds_stationary_textView, "field 'scan_timeout_seconds_stationary_textView'", TextView.class);
    target.number_of_antennas_stationary_textView = Utils.findRequiredViewAsType(source, R.id.number_of_antennas_stationary_textView, "field 'number_of_antennas_stationary_textView'", TextView.class);
    target.store_rate_stationary_textView = Utils.findRequiredViewAsType(source, R.id.store_rate_minutes_stationary_textView, "field 'store_rate_stationary_textView'", TextView.class);
    target.frequency_reference_stationary_textView = Utils.findRequiredViewAsType(source, R.id.frequency_reference_stationary_textView, "field 'frequency_reference_stationary_textView'", TextView.class);
    target.store_rate_stationary_imageView = Utils.findRequiredViewAsType(source, R.id.store_rate_stationary_imageView, "field 'store_rate_stationary_imageView'", ImageView.class);
    target.reference_frequency_store_rate_stationary_textView = Utils.findRequiredViewAsType(source, R.id.reference_frequency_store_rate_stationary_textView, "field 'reference_frequency_store_rate_stationary_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.store_rate_stationary_linearLayout, "field 'store_rate_stationary_linearLayout' and method 'onClickStoreRate'");
    target.store_rate_stationary_linearLayout = Utils.castView(view, R.id.store_rate_stationary_linearLayout, "field 'store_rate_stationary_linearLayout'", LinearLayout.class);
    view7f0a0247 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStoreRate(p0);
      }
    });
    target.stationary_external_data_transfer_switch = Utils.findRequiredViewAsType(source, R.id.stationary_external_data_transfer_switch, "field 'stationary_external_data_transfer_switch'", SwitchCompat.class);
    view = Utils.findRequiredView(source, R.id.stationary_reference_frequency_switch, "field 'stationary_reference_frequency_switch' and method 'onCheckedChangedReferenceFrequency'");
    target.stationary_reference_frequency_switch = Utils.castView(view, R.id.stationary_reference_frequency_switch, "field 'stationary_reference_frequency_switch'", SwitchCompat.class);
    view7f0a023e = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChangedReferenceFrequency(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.reference_frequency_stationary_linearLayout, "field 'reference_frequency_stationary_linearLayout' and method 'onClickReferenceFrequency'");
    target.reference_frequency_stationary_linearLayout = Utils.castView(view, R.id.reference_frequency_stationary_linearLayout, "field 'reference_frequency_stationary_linearLayout'", LinearLayout.class);
    view7f0a01d5 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickReferenceFrequency(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.reference_frequency_store_rate_stationary_linearLayout, "field 'reference_frequency_store_rate_stationary_linearLayout' and method 'onClickReferenceFrequencyStoreRate'");
    target.reference_frequency_store_rate_stationary_linearLayout = Utils.castView(view, R.id.reference_frequency_store_rate_stationary_linearLayout, "field 'reference_frequency_store_rate_stationary_linearLayout'", LinearLayout.class);
    view7f0a01d6 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickReferenceFrequencyStoreRate(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.frequency_table_number_stationary_linearLayout, "method 'onClickFrequencyTableNumber'");
    view7f0a0111 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFrequencyTableNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.scan_rate_seconds_stationary_linearLayout, "method 'onClickScanRateSeconds'");
    view7f0a01ea = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickScanRateSeconds(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.scan_timeout_seconds_stationary_linearLayout, "method 'onClickScanTimeoutSeconds'");
    view7f0a01ed = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickScanTimeoutSeconds(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.number_of_antennas_stationary_linearLayout, "method 'onClickNumberOfAntennas'");
    view7f0a0185 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumberOfAntennas(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.ready_stationary_scan_button, "method 'onClickReadyToStationaryScan'");
    view7f0a01ce = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickReadyToStationaryScan(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    TemporaryStationaryActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.frequency_table_number_stationary_textView = null;
    target.scan_rate_seconds_stationary_textView = null;
    target.scan_timeout_seconds_stationary_textView = null;
    target.number_of_antennas_stationary_textView = null;
    target.store_rate_stationary_textView = null;
    target.frequency_reference_stationary_textView = null;
    target.store_rate_stationary_imageView = null;
    target.reference_frequency_store_rate_stationary_textView = null;
    target.store_rate_stationary_linearLayout = null;
    target.stationary_external_data_transfer_switch = null;
    target.stationary_reference_frequency_switch = null;
    target.reference_frequency_stationary_linearLayout = null;
    target.reference_frequency_store_rate_stationary_linearLayout = null;

    view7f0a0247.setOnClickListener(null);
    view7f0a0247 = null;
    ((CompoundButton) view7f0a023e).setOnCheckedChangeListener(null);
    view7f0a023e = null;
    view7f0a01d5.setOnClickListener(null);
    view7f0a01d5 = null;
    view7f0a01d6.setOnClickListener(null);
    view7f0a01d6 = null;
    view7f0a0111.setOnClickListener(null);
    view7f0a0111 = null;
    view7f0a01ea.setOnClickListener(null);
    view7f0a01ea = null;
    view7f0a01ed.setOnClickListener(null);
    view7f0a01ed = null;
    view7f0a0185.setOnClickListener(null);
    view7f0a0185 = null;
    view7f0a01ce.setOnClickListener(null);
    view7f0a01ce = null;
  }
}
