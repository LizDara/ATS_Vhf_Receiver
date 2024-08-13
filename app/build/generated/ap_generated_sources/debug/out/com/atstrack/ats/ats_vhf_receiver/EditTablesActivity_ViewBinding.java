// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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

public class EditTablesActivity_ViewBinding implements Unbinder {
  private EditTablesActivity target;

  private View view7f0a0052;

  private View view7f0a00b0;

  private View view7f0a00aa;

  private View view7f0a0043;

  private View view7f0a0045;

  @UiThread
  public EditTablesActivity_ViewBinding(EditTablesActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public EditTablesActivity_ViewBinding(final EditTablesActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.frequencies_overview_linearLayout = Utils.findRequiredViewAsType(source, R.id.frequencies_overview_linearLayout, "field 'frequencies_overview_linearLayout'", LinearLayout.class);
    target.edit_options_linearLayout = Utils.findRequiredViewAsType(source, R.id.edit_options_linearLayout, "field 'edit_options_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.all_frequencies_checkBox, "field 'all_frequencies_checkBox' and method 'onClickAllFrequencies'");
    target.all_frequencies_checkBox = Utils.castView(view, R.id.all_frequencies_checkBox, "field 'all_frequencies_checkBox'", CheckBox.class);
    view7f0a0052 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickAllFrequencies(p0);
      }
    });
    target.delete_frequencies_linearLayout = Utils.findRequiredViewAsType(source, R.id.delete_frequencies_linearLayout, "field 'delete_frequencies_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.delete_selected_frequencies_button, "field 'delete_selected_frequencies_button' and method 'onClickDeleteSelectedFrequencies'");
    target.delete_selected_frequencies_button = Utils.castView(view, R.id.delete_selected_frequencies_button, "field 'delete_selected_frequencies_button'", Button.class);
    view7f0a00b0 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDeleteSelectedFrequencies(p0);
      }
    });
    target.no_frequencies_linearLayout = Utils.findRequiredViewAsType(source, R.id.no_frequencies_linearLayout, "field 'no_frequencies_linearLayout'", LinearLayout.class);
    target.frequencies_listView = Utils.findRequiredViewAsType(source, R.id.frequencies_listView, "field 'frequencies_listView'", ListView.class);
    target.frequencies_delete_listView = Utils.findRequiredViewAsType(source, R.id.frequencies_delete_listView, "field 'frequencies_delete_listView'", ListView.class);
    view = Utils.findRequiredView(source, R.id.delete_frequencies_button, "method 'onClickDeleteFrequencies'");
    view7f0a00aa = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDeleteFrequencies(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.add_frequency_button, "method 'onClickAddFrequency'");
    view7f0a0043 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickAddFrequency(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.add_new_frequency_button, "method 'onClickAddFrequency'");
    view7f0a0045 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickAddFrequency(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    EditTablesActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.frequencies_overview_linearLayout = null;
    target.edit_options_linearLayout = null;
    target.all_frequencies_checkBox = null;
    target.delete_frequencies_linearLayout = null;
    target.delete_selected_frequencies_button = null;
    target.no_frequencies_linearLayout = null;
    target.frequencies_listView = null;
    target.frequencies_delete_listView = null;

    view7f0a0052.setOnClickListener(null);
    view7f0a0052 = null;
    view7f0a00b0.setOnClickListener(null);
    view7f0a00b0 = null;
    view7f0a00aa.setOnClickListener(null);
    view7f0a00aa = null;
    view7f0a0043.setOnClickListener(null);
    view7f0a0043 = null;
    view7f0a0045.setOnClickListener(null);
    view7f0a0045 = null;
  }
}
