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

public class ReceiverConfigurationActivity_ViewBinding implements Unbinder {
  private ReceiverConfigurationActivity target;

  private View view7f0a00d9;

  private View view7f0a00dc;

  private View view7f0a01f6;

  private View view7f0a0084;

  @UiThread
  public ReceiverConfigurationActivity_ViewBinding(ReceiverConfigurationActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ReceiverConfigurationActivity_ViewBinding(final ReceiverConfigurationActivity target,
      View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    view = Utils.findRequiredView(source, R.id.edit_frequency_tables_button, "method 'onClickEditFrequencyTables'");
    view7f0a00d9 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEditFrequencyTables(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.edit_receiver_defaults_button, "method 'onClickEditReceiverDefaults'");
    view7f0a00dc = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEditReceiverDefaults(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.set_transmitter_type_button, "method 'onClickSetTransmitterType'");
    view7f0a01f6 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSetTransmitterType(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.clone_from_other_receiver_button, "method 'onClickCloneFromOtherReceiver'");
    view7f0a0084 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickCloneFromOtherReceiver(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    ReceiverConfigurationActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;

    view7f0a00d9.setOnClickListener(null);
    view7f0a00d9 = null;
    view7f0a00dc.setOnClickListener(null);
    view7f0a00dc = null;
    view7f0a01f6.setOnClickListener(null);
    view7f0a01f6 = null;
    view7f0a0084.setOnClickListener(null);
    view7f0a0084 = null;
  }
}
