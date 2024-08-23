// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class InputValueActivity_ViewBinding implements Unbinder {
  private InputValueActivity target;

  private View view7f0a0161;

  private View view7f0a0098;

  private View view7f0a00f2;

  private View view7f0a025d;

  private View view7f0a00eb;

  private View view7f0a0267;

  private View view7f0a0209;

  private View view7f0a0185;

  private View view7f0a01d5;

  @UiThread
  public InputValueActivity_ViewBinding(InputValueActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public InputValueActivity_ViewBinding(final InputValueActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.value_spinner = Utils.findRequiredViewAsType(source, R.id.value_spinner, "field 'value_spinner'", Spinner.class);
    target.set_value_linearLayout = Utils.findRequiredViewAsType(source, R.id.set_value_linearLayout, "field 'set_value_linearLayout'", LinearLayout.class);
    target.store_rate_linearLayout = Utils.findRequiredViewAsType(source, R.id.store_rate_linearLayout, "field 'store_rate_linearLayout'", LinearLayout.class);
    target.continuous_store_imageView = Utils.findRequiredViewAsType(source, R.id.continuous_store_imageView, "field 'continuous_store_imageView'", ImageView.class);
    target.five_minutes_imageView = Utils.findRequiredViewAsType(source, R.id.five_minutes_imageView, "field 'five_minutes_imageView'", ImageView.class);
    target.ten_minutes_imageView = Utils.findRequiredViewAsType(source, R.id.ten_minutes_imageView, "field 'ten_minutes_imageView'", ImageView.class);
    target.fifteen_minutes_imageView = Utils.findRequiredViewAsType(source, R.id.fifteen_minutes_imageView, "field 'fifteen_minutes_imageView'", ImageView.class);
    target.thirty_minutes_imageView = Utils.findRequiredViewAsType(source, R.id.thirty_minutes_imageView, "field 'thirty_minutes_imageView'", ImageView.class);
    target.sixty_minutes_imageView = Utils.findRequiredViewAsType(source, R.id.sixty_minutes_imageView, "field 'sixty_minutes_imageView'", ImageView.class);
    target.one_hundred_twenty_minutes_imageView = Utils.findRequiredViewAsType(source, R.id.one_hundred_twenty_minutes_imageView, "field 'one_hundred_twenty_minutes_imageView'", ImageView.class);
    target.merge_tables_linearLayout = Utils.findRequiredViewAsType(source, R.id.merge_tables_linearLayout, "field 'merge_tables_linearLayout'", LinearLayout.class);
    target.option_tables_textView = Utils.findRequiredViewAsType(source, R.id.option_tables_textView, "field 'option_tables_textView'", TextView.class);
    target.tables_merge_listView = Utils.findRequiredViewAsType(source, R.id.tables_merge_listView, "field 'tables_merge_listView'", ListView.class);
    view = Utils.findRequiredView(source, R.id.merge_tables_button, "field 'merge_tables_button' and method 'onClickSaveTables'");
    target.merge_tables_button = Utils.castView(view, R.id.merge_tables_button, "field 'merge_tables_button'", Button.class);
    view7f0a0161 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSaveTables(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.continuous_store_linearLayout, "method 'onClickContinuousStore'");
    view7f0a0098 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickContinuousStore(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.five_minutes_linearLayout, "method 'onClickFiveMinutes'");
    view7f0a00f2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFiveMinutes(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.ten_minutes_linearLayout, "method 'onClickTenMinutes'");
    view7f0a025d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickTenMinutes(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.fifteen_minutes_linearLayout, "method 'onClickFifteenMinutes'");
    view7f0a00eb = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFifteenMinutes(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.thirty_minutes_linearLayout, "method 'onClickThirtyMinutes'");
    view7f0a0267 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickThirtyMinutes(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.sixty_minutes_linearLayout, "method 'onClickSixtyMinutes'");
    view7f0a0209 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSixtyMinutes(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.one_hundred_twenty_minutes_linearLayout, "method 'onClickOneHundredTwentyMinutes'");
    view7f0a0185 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickOneHundredTwentyMinutes(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.save_changes_input_value_button, "method 'onClickSaveChanges'");
    view7f0a01d5 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSaveChanges(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    InputValueActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.value_spinner = null;
    target.set_value_linearLayout = null;
    target.store_rate_linearLayout = null;
    target.continuous_store_imageView = null;
    target.five_minutes_imageView = null;
    target.ten_minutes_imageView = null;
    target.fifteen_minutes_imageView = null;
    target.thirty_minutes_imageView = null;
    target.sixty_minutes_imageView = null;
    target.one_hundred_twenty_minutes_imageView = null;
    target.merge_tables_linearLayout = null;
    target.option_tables_textView = null;
    target.tables_merge_listView = null;
    target.merge_tables_button = null;

    view7f0a0161.setOnClickListener(null);
    view7f0a0161 = null;
    view7f0a0098.setOnClickListener(null);
    view7f0a0098 = null;
    view7f0a00f2.setOnClickListener(null);
    view7f0a00f2 = null;
    view7f0a025d.setOnClickListener(null);
    view7f0a025d = null;
    view7f0a00eb.setOnClickListener(null);
    view7f0a00eb = null;
    view7f0a0267.setOnClickListener(null);
    view7f0a0267 = null;
    view7f0a0209.setOnClickListener(null);
    view7f0a0209 = null;
    view7f0a0185.setOnClickListener(null);
    view7f0a0185 = null;
    view7f0a01d5.setOnClickListener(null);
    view7f0a01d5 = null;
  }
}
