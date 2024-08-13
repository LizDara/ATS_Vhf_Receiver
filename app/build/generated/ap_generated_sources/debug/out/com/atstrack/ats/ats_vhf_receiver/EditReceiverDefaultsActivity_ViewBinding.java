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

public class EditReceiverDefaultsActivity_ViewBinding implements Unbinder {
  private EditReceiverDefaultsActivity target;

  private View view7f0a0049;

  private View view7f0a023c;

  @UiThread
  public EditReceiverDefaultsActivity_ViewBinding(EditReceiverDefaultsActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public EditReceiverDefaultsActivity_ViewBinding(final EditReceiverDefaultsActivity target,
      View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    view = Utils.findRequiredView(source, R.id.aerial_defaults_button, "method 'onClickAerialDefaults'");
    view7f0a0049 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickAerialDefaults(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.stationary_defaults_button, "method 'onClickStationaryDefaults'");
    view7f0a023c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStationaryDefaults(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    EditReceiverDefaultsActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;

    view7f0a0049.setOnClickListener(null);
    view7f0a0049 = null;
    view7f0a023c.setOnClickListener(null);
    view7f0a023c = null;
  }
}
