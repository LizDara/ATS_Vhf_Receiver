// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity_ViewBinding implements Unbinder {
  private MainActivity target;

  @UiThread
  public MainActivity_ViewBinding(MainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MainActivity_ViewBinding(MainActivity target, View source) {
    this.target = target;

    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.version_textView = Utils.findRequiredViewAsType(source, R.id.version_textView, "field 'version_textView'", TextView.class);
    target.splash_screen_constraintLayout = Utils.findRequiredViewAsType(source, R.id.splash_screen_constraintLayout, "field 'splash_screen_constraintLayout'", ConstraintLayout.class);
    target.bridge_app_linearLayout = Utils.findRequiredViewAsType(source, R.id.bridge_app_linearLayout, "field 'bridge_app_linearLayout'", LinearLayout.class);
    target.bridge_subtitle_textView = Utils.findRequiredViewAsType(source, R.id.bridge_subtitle_textView, "field 'bridge_subtitle_textView'", TextView.class);
    target.bridge_message_textView = Utils.findRequiredViewAsType(source, R.id.bridge_message_textView, "field 'bridge_message_textView'", TextView.class);
    target.types_subtitle_textView = Utils.findRequiredViewAsType(source, R.id.types_subtitle_textView, "field 'types_subtitle_textView'", TextView.class);
    target.category_recyclerView = Utils.findRequiredViewAsType(source, R.id.category_recyclerView, "field 'category_recyclerView'", RecyclerView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.state_view = null;
    target.title_toolbar = null;
    target.version_textView = null;
    target.splash_screen_constraintLayout = null;
    target.bridge_app_linearLayout = null;
    target.bridge_subtitle_textView = null;
    target.bridge_message_textView = null;
    target.types_subtitle_textView = null;
    target.category_recyclerView = null;
  }
}
