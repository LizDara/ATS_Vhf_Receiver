// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class StationaryScanActivity_ViewBinding implements Unbinder {
  private StationaryScanActivity target;

  private View view7f0a00e1;

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
    target.scan_rate_stationary_textView = Utils.findRequiredViewAsType(source, R.id.scan_rate_stationary_textView, "field 'scan_rate_stationary_textView'", TextView.class);
    target.selected_frequency_stationary_textView = Utils.findRequiredViewAsType(source, R.id.selected_frequency_stationary_textView, "field 'selected_frequency_stationary_textView'", TextView.class);
    target.store_rate_stationary_textView = Utils.findRequiredViewAsType(source, R.id.store_rate_stationary_textView, "field 'store_rate_stationary_textView'", TextView.class);
    target.external_data_transfer_stationary_textView = Utils.findRequiredViewAsType(source, R.id.external_data_transfer_stationary_textView, "field 'external_data_transfer_stationary_textView'", TextView.class);
    target.number_antennas_stationary_textView = Utils.findRequiredViewAsType(source, R.id.number_antennas_stationary_textView, "field 'number_antennas_stationary_textView'", TextView.class);
    target.timeout_stationary_textView = Utils.findRequiredViewAsType(source, R.id.timeout_stationary_textView, "field 'timeout_stationary_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.edit_stationary_settings_button, "field 'edit_stationary_defaults_textView' and method 'onClickEditStationarySettings'");
    target.edit_stationary_defaults_textView = Utils.castView(view, R.id.edit_stationary_settings_button, "field 'edit_stationary_defaults_textView'", TextView.class);
    view7f0a00e1 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEditStationarySettings(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.start_stationary_button, "field 'start_stationary_button' and method 'onClickStartStationary'");
    target.start_stationary_button = Utils.castView(view, R.id.start_stationary_button, "field 'start_stationary_button'", Button.class);
    view7f0a0233 = view;
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
    target.scan_rate_stationary_textView = null;
    target.selected_frequency_stationary_textView = null;
    target.store_rate_stationary_textView = null;
    target.external_data_transfer_stationary_textView = null;
    target.number_antennas_stationary_textView = null;
    target.timeout_stationary_textView = null;
    target.edit_stationary_defaults_textView = null;
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

    view7f0a00e1.setOnClickListener(null);
    view7f0a00e1 = null;
    view7f0a0233.setOnClickListener(null);
    view7f0a0233 = null;
  }
}
