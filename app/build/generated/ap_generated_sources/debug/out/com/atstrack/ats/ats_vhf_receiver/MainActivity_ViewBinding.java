// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
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

    target.tb_main = Utils.findRequiredViewAsType(source, R.id.tb_main, "field 'tb_main'", Toolbar.class);
    target.v_state = Utils.findRequiredView(source, R.id.v_state, "field 'v_state'");
    target.tv_title_toolbar = Utils.findRequiredViewAsType(source, R.id.tv_title_toolbar, "field 'tv_title_toolbar'", TextView.class);
    target.tv_bridge_subtitle = Utils.findRequiredViewAsType(source, R.id.tv_bridge_subtitle, "field 'tv_bridge_subtitle'", TextView.class);
    target.tv_bridge_message = Utils.findRequiredViewAsType(source, R.id.tv_bridge_message, "field 'tv_bridge_message'", TextView.class);
    target.tv_types_subtitle = Utils.findRequiredViewAsType(source, R.id.tv_types_subtitle, "field 'tv_types_subtitle'", TextView.class);
    target.rv_item = Utils.findRequiredViewAsType(source, R.id.rv_item, "field 'rv_item'", RecyclerView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.tb_main = null;
    target.v_state = null;
    target.tv_title_toolbar = null;
    target.tv_bridge_subtitle = null;
    target.tv_bridge_message = null;
    target.tv_types_subtitle = null;
    target.rv_item = null;
  }
}
