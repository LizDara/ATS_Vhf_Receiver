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

public class AerialScanningActivity_ViewBinding implements Unbinder {
  private AerialScanningActivity target;

  private View view7f0a004c;

  private View view7f0a004d;

  @UiThread
  public AerialScanningActivity_ViewBinding(AerialScanningActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public AerialScanningActivity_ViewBinding(final AerialScanningActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    view = Utils.findRequiredView(source, R.id.aerial_scan_defaults_button, "method 'onClickAerialScanDefaults'");
    view7f0a004c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickAerialScanDefaults(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.aerial_scan_temporary_button, "method 'onClickAerialScanTemporary'");
    view7f0a004d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickAerialScanTemporary(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    AerialScanningActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;

    view7f0a004c.setOnClickListener(null);
    view7f0a004c = null;
    view7f0a004d.setOnClickListener(null);
    view7f0a004d = null;
  }
}
