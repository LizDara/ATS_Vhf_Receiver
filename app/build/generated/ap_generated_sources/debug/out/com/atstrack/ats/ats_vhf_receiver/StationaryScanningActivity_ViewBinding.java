// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class StationaryScanningActivity_ViewBinding implements Unbinder {
  private StationaryScanningActivity target;

  private View view7f0a0240;

  private View view7f0a0241;

  @UiThread
  public StationaryScanningActivity_ViewBinding(StationaryScanningActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public StationaryScanningActivity_ViewBinding(final StationaryScanningActivity target,
      View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    view = Utils.findRequiredView(source, R.id.stationary_scan_defaults_button, "method 'onClickStationaryScanDefaults'");
    view7f0a0240 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStationaryScanDefaults(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.stationary_scan_temporary_button, "method 'onClickStationaryScanTemporary'");
    view7f0a0241 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStationaryScanTemporary(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    StationaryScanningActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;

    view7f0a0240.setOnClickListener(null);
    view7f0a0240 = null;
    view7f0a0241.setOnClickListener(null);
    view7f0a0241 = null;
  }
}
