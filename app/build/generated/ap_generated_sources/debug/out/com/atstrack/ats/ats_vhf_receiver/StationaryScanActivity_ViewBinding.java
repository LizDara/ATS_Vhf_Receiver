// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
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

public class StationaryScanActivity_ViewBinding implements Unbinder {
  private StationaryScanActivity target;

  private View view7f0a022c;

  private View view7f0a01c7;

  private View view7f0a01c8;

  private View view7f0a0221;

  private View view7f0a010b;

  private View view7f0a01db;

  private View view7f0a01dd;

  private View view7f0a017d;

  private View view7f0a0233;

  @UiThread
  public StationaryScanActivity_ViewBinding(StationaryScanActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public StationaryScanActivity_ViewBinding(final StationaryScanActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.ready_stationary_scan_LinearLayout = Utils.findRequiredViewAsType(source, R.id.ready_stationary_scan_LinearLayout, "field 'ready_stationary_scan_LinearLayout'", LinearLayout.class);
    target.ready_stationary_textView = Utils.findRequiredViewAsType(source, R.id.ready_stationary_textView, "field 'ready_stationary_textView'", TextView.class);
    target.scan_rate_seconds_stationary_textView = Utils.findRequiredViewAsType(source, R.id.scan_rate_seconds_stationary_textView, "field 'scan_rate_seconds_stationary_textView'", TextView.class);
    target.frequency_table_number_stationary_textView = Utils.findRequiredViewAsType(source, R.id.frequency_table_number_stationary_textView, "field 'frequency_table_number_stationary_textView'", TextView.class);
    target.store_rate_minutes_stationary_textView = Utils.findRequiredViewAsType(source, R.id.store_rate_minutes_stationary_textView, "field 'store_rate_minutes_stationary_textView'", TextView.class);
    target.stationary_external_data_transfer_switch = Utils.findRequiredViewAsType(source, R.id.stationary_external_data_transfer_switch, "field 'stationary_external_data_transfer_switch'", SwitchCompat.class);
    target.number_of_antennas_stationary_textView = Utils.findRequiredViewAsType(source, R.id.number_of_antennas_stationary_textView, "field 'number_of_antennas_stationary_textView'", TextView.class);
    target.scan_timeout_seconds_stationary_textView = Utils.findRequiredViewAsType(source, R.id.scan_timeout_seconds_stationary_textView, "field 'scan_timeout_seconds_stationary_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.stationary_reference_frequency_switch, "field 'stationary_reference_frequency_switch' and method 'onCheckedChangedReferenceFrequency'");
    target.stationary_reference_frequency_switch = Utils.castView(view, R.id.stationary_reference_frequency_switch, "field 'stationary_reference_frequency_switch'", SwitchCompat.class);
    view7f0a022c = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChangedReferenceFrequency(p0, p1);
      }
    });
    target.frequency_reference_stationary_textView = Utils.findRequiredViewAsType(source, R.id.frequency_reference_stationary_textView, "field 'frequency_reference_stationary_textView'", TextView.class);
    target.reference_frequency_store_rate_stationary_textView = Utils.findRequiredViewAsType(source, R.id.reference_frequency_store_rate_stationary_textView, "field 'reference_frequency_store_rate_stationary_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.reference_frequency_stationary_linearLayout, "field 'reference_frequency_stationary_linearLayout' and method 'onClickReferenceFrequency'");
    target.reference_frequency_stationary_linearLayout = Utils.castView(view, R.id.reference_frequency_stationary_linearLayout, "field 'reference_frequency_stationary_linearLayout'", LinearLayout.class);
    view7f0a01c7 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickReferenceFrequency(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.reference_frequency_store_rate_stationary_linearLayout, "field 'reference_frequency_store_rate_stationary_linearLayout' and method 'onClickReferenceFrequencyStoreRate'");
    target.reference_frequency_store_rate_stationary_linearLayout = Utils.castView(view, R.id.reference_frequency_store_rate_stationary_linearLayout, "field 'reference_frequency_store_rate_stationary_linearLayout'", LinearLayout.class);
    view7f0a01c8 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickReferenceFrequencyStoreRate(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.start_stationary_button, "field 'start_stationary_button' and method 'onClickStartStationary'");
    target.start_stationary_button = Utils.castView(view, R.id.start_stationary_button, "field 'start_stationary_button'", Button.class);
    view7f0a0221 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStartStationary(p0);
      }
    });
    target.stationary_result_linearLayout = Utils.findRequiredViewAsType(source, R.id.stationary_result_linearLayout, "field 'stationary_result_linearLayout'", LinearLayout.class);
    target.max_index_stationary_textView = Utils.findRequiredViewAsType(source, R.id.max_index_stationary_textView, "field 'max_index_stationary_textView'", TextView.class);
    target.index_stationary_textView = Utils.findRequiredViewAsType(source, R.id.index_stationary_textView, "field 'index_stationary_textView'", TextView.class);
    target.frequency_stationary_textView = Utils.findRequiredViewAsType(source, R.id.frequency_stationary_textView, "field 'frequency_stationary_textView'", TextView.class);
    target.current_antenna_stationary_textView = Utils.findRequiredViewAsType(source, R.id.current_antenna_stationary_textView, "field 'current_antenna_stationary_textView'", TextView.class);
    target.scan_details_linearLayout = Utils.findRequiredViewAsType(source, R.id.scan_details_linearLayout, "field 'scan_details_linearLayout'", LinearLayout.class);
    target.code_textView = Utils.findRequiredViewAsType(source, R.id.code_textView, "field 'code_textView'", TextView.class);
    target.mortality_textView = Utils.findRequiredViewAsType(source, R.id.mortality_textView, "field 'mortality_textView'", TextView.class);
    target.period_textView = Utils.findRequiredViewAsType(source, R.id.period_textView, "field 'period_textView'", TextView.class);
    target.pulse_rate_textView = Utils.findRequiredViewAsType(source, R.id.pulse_rate_textView, "field 'pulse_rate_textView'", TextView.class);
    target.line_view = Utils.findRequiredView(source, R.id.line_view, "field 'line_view'");
    view = Utils.findRequiredView(source, R.id.frequency_table_number_stationary_linearLayout, "method 'onClickFrequencyTableNumber'");
    view7f0a010b = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFrequencyTableNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.scan_rate_seconds_stationary_linearLayout, "method 'onClickScanRateSeconds'");
    view7f0a01db = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickScanRateSeconds(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.scan_timeout_seconds_stationary_linearLayout, "method 'onClickScanTimeoutSeconds'");
    view7f0a01dd = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickScanTimeoutSeconds(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.number_of_antennas_stationary_linearLayout, "method 'onClickNumberOfAntennas'");
    view7f0a017d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumberOfAntennas(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.store_rate_stationary_linearLayout, "method 'onClickStoreRate'");
    view7f0a0233 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStoreRate(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    StationaryScanActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.ready_stationary_scan_LinearLayout = null;
    target.ready_stationary_textView = null;
    target.scan_rate_seconds_stationary_textView = null;
    target.frequency_table_number_stationary_textView = null;
    target.store_rate_minutes_stationary_textView = null;
    target.stationary_external_data_transfer_switch = null;
    target.number_of_antennas_stationary_textView = null;
    target.scan_timeout_seconds_stationary_textView = null;
    target.stationary_reference_frequency_switch = null;
    target.frequency_reference_stationary_textView = null;
    target.reference_frequency_store_rate_stationary_textView = null;
    target.reference_frequency_stationary_linearLayout = null;
    target.reference_frequency_store_rate_stationary_linearLayout = null;
    target.start_stationary_button = null;
    target.stationary_result_linearLayout = null;
    target.max_index_stationary_textView = null;
    target.index_stationary_textView = null;
    target.frequency_stationary_textView = null;
    target.current_antenna_stationary_textView = null;
    target.scan_details_linearLayout = null;
    target.code_textView = null;
    target.mortality_textView = null;
    target.period_textView = null;
    target.pulse_rate_textView = null;
    target.line_view = null;

    ((CompoundButton) view7f0a022c).setOnCheckedChangeListener(null);
    view7f0a022c = null;
    view7f0a01c7.setOnClickListener(null);
    view7f0a01c7 = null;
    view7f0a01c8.setOnClickListener(null);
    view7f0a01c8 = null;
    view7f0a0221.setOnClickListener(null);
    view7f0a0221 = null;
    view7f0a010b.setOnClickListener(null);
    view7f0a010b = null;
    view7f0a01db.setOnClickListener(null);
    view7f0a01db = null;
    view7f0a01dd.setOnClickListener(null);
    view7f0a01dd = null;
    view7f0a017d.setOnClickListener(null);
    view7f0a017d = null;
    view7f0a0233.setOnClickListener(null);
    view7f0a0233 = null;
  }
}
