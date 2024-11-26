// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
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

public class AerialDefaultsActivity_ViewBinding implements Unbinder {
  private AerialDefaultsActivity target;

  private View view7f0a0115;

  private View view7f0a01ed;

  @UiThread
  public AerialDefaultsActivity_ViewBinding(AerialDefaultsActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public AerialDefaultsActivity_ViewBinding(final AerialDefaultsActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.frequency_table_number_aerial_textView = Utils.findRequiredViewAsType(source, R.id.frequency_table_number_aerial_textView, "field 'frequency_table_number_aerial_textView'", TextView.class);
    target.scan_rate_seconds_aerial_textView = Utils.findRequiredViewAsType(source, R.id.scan_rate_seconds_aerial_textView, "field 'scan_rate_seconds_aerial_textView'", TextView.class);
    target.aerial_gps_switch = Utils.findRequiredViewAsType(source, R.id.aerial_gps_switch, "field 'aerial_gps_switch'", SwitchCompat.class);
    target.aerial_auto_record_switch = Utils.findRequiredViewAsType(source, R.id.aerial_auto_record_switch, "field 'aerial_auto_record_switch'", SwitchCompat.class);
    view = Utils.findRequiredView(source, R.id.frequency_table_number_aerial_linearLayout, "method 'onClickFrequencyTableNumber'");
    view7f0a0115 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFrequencyTableNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.scan_rate_seconds_aerial_linearLayout, "method 'onClickScanRateSeconds'");
    view7f0a01ed = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickScanRateSeconds(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    AerialDefaultsActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.frequency_table_number_aerial_textView = null;
    target.scan_rate_seconds_aerial_textView = null;
    target.aerial_gps_switch = null;
    target.aerial_auto_record_switch = null;

    view7f0a0115.setOnClickListener(null);
    view7f0a0115 = null;
    view7f0a01ed.setOnClickListener(null);
    view7f0a01ed = null;
  }
}
