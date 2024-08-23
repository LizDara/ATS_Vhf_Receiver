// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class TableOverviewActivity_ViewBinding implements Unbinder {
  private TableOverviewActivity target;

  private View view7f0a0146;

  @UiThread
  public TableOverviewActivity_ViewBinding(TableOverviewActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public TableOverviewActivity_ViewBinding(final TableOverviewActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.tables_listView = Utils.findRequiredViewAsType(source, R.id.tables_listView, "field 'tables_listView'", ListView.class);
    view = Utils.findRequiredView(source, R.id.load_from_file_button, "method 'onClickLoadTablesFromFile'");
    view7f0a0146 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickLoadTablesFromFile(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    TableOverviewActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.tables_listView = null;

    view7f0a0146.setOnClickListener(null);
    view7f0a0146 = null;
  }
}
