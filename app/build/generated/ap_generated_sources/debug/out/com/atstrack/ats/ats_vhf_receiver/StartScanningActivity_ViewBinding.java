// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
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

public class StartScanningActivity_ViewBinding implements Unbinder {
  private StartScanningActivity target;

  private View view7f0a0237;

  private View view7f0a0235;

  private View view7f0a023a;

  private View view7f0a011c;

  @UiThread
  public StartScanningActivity_ViewBinding(StartScanningActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public StartScanningActivity_ViewBinding(final StartScanningActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.menu_scan_linearLayout = Utils.findRequiredViewAsType(source, R.id.menu_scan_linearLayout, "field 'menu_scan_linearLayout'", LinearLayout.class);
    target.warning_no_tables_linearLayout = Utils.findRequiredViewAsType(source, R.id.warning_no_tables_linearLayout, "field 'warning_no_tables_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.start_manual_scan_button, "method 'onClickStartManualScan'");
    view7f0a0237 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStartManualScan(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.start_aerial_scan_button, "method 'onClickStartAerialScan'");
    view7f0a0235 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStartAerialScan(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.start_stationary_scan_button, "method 'onClickStartStationaryScan'");
    view7f0a023a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStartStationaryScan(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.go_tables_button, "method 'onClickGoTables'");
    view7f0a011c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickGoTables(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    StartScanningActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.menu_scan_linearLayout = null;
    target.warning_no_tables_linearLayout = null;

    view7f0a0237.setOnClickListener(null);
    view7f0a0237 = null;
    view7f0a0235.setOnClickListener(null);
    view7f0a0235 = null;
    view7f0a023a.setOnClickListener(null);
    view7f0a023a = null;
    view7f0a011c.setOnClickListener(null);
    view7f0a011c = null;
  }
}
