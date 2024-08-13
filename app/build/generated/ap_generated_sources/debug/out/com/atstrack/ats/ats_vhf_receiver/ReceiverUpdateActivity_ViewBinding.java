// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ReceiverUpdateActivity_ViewBinding implements Unbinder {
  private ReceiverUpdateActivity target;

  private View view7f0a007b;

  @UiThread
  public ReceiverUpdateActivity_ViewBinding(ReceiverUpdateActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ReceiverUpdateActivity_ViewBinding(final ReceiverUpdateActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.updating_receiver_linearLayout = Utils.findRequiredViewAsType(source, R.id.updating_receiver_linearLayout, "field 'updating_receiver_linearLayout'", LinearLayout.class);
    target.updating_receiver_progressBar = Utils.findRequiredViewAsType(source, R.id.updating_receiver_progressBar, "field 'updating_receiver_progressBar'", ProgressBar.class);
    target.update_receiver_linearLayout = Utils.findRequiredViewAsType(source, R.id.update_receiver_linearLayout, "field 'update_receiver_linearLayout'", LinearLayout.class);
    target.update_done_imageView = Utils.findRequiredViewAsType(source, R.id.update_done_imageView, "field 'update_done_imageView'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.cancel_update_button, "method 'onClickCancelUpdate'");
    view7f0a007b = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickCancelUpdate(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    ReceiverUpdateActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.updating_receiver_linearLayout = null;
    target.updating_receiver_progressBar = null;
    target.update_receiver_linearLayout = null;
    target.update_done_imageView = null;

    view7f0a007b.setOnClickListener(null);
    view7f0a007b = null;
  }
}
