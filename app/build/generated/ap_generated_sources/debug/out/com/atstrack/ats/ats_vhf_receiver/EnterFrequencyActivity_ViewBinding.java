// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class EnterFrequencyActivity_ViewBinding implements Unbinder {
  private EnterFrequencyActivity target;

  private View view7f0a01e8;

  private View view7f0a0192;

  private View view7f0a0290;

  private View view7f0a0280;

  private View view7f0a0103;

  private View view7f0a00fa;

  private View view7f0a021c;

  private View view7f0a0210;

  private View view7f0a00e6;

  private View view7f0a017c;

  private View view7f0a02b7;

  private View view7f0a00b1;

  @UiThread
  public EnterFrequencyActivity_ViewBinding(EnterFrequencyActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public EnterFrequencyActivity_ViewBinding(final EnterFrequencyActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.frequency_textView = Utils.findRequiredViewAsType(source, R.id.frequency_textView, "field 'frequency_textView'", TextView.class);
    target.line_frequency_view = Utils.findRequiredView(source, R.id.line_frequency_view, "field 'line_frequency_view'");
    target.edit_frequency_message_textView = Utils.findRequiredViewAsType(source, R.id.edit_frequency_message_textView, "field 'edit_frequency_message_textView'", TextView.class);
    target.number_buttons_linearLayout = Utils.findRequiredViewAsType(source, R.id.number_buttons_linearLayout, "field 'number_buttons_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.save_changes_button, "field 'save_changes_button' and method 'onClickSaveChanges'");
    target.save_changes_button = Utils.castView(view, R.id.save_changes_button, "field 'save_changes_button'", Button.class);
    view7f0a01e8 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSaveChanges(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.one_button, "field 'one_button' and method 'onClickNumber'");
    target.one_button = Utils.castView(view, R.id.one_button, "field 'one_button'", Button.class);
    view7f0a0192 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.two_button, "method 'onClickNumber'");
    view7f0a0290 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.three_button, "method 'onClickNumber'");
    view7f0a0280 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.four_button, "method 'onClickNumber'");
    view7f0a0103 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.five_button, "method 'onClickNumber'");
    view7f0a00fa = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.six_button, "method 'onClickNumber'");
    view7f0a021c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.seven_button, "method 'onClickNumber'");
    view7f0a0210 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.eight_button, "method 'onClickNumber'");
    view7f0a00e6 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.nine_button, "method 'onClickNumber'");
    view7f0a017c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.zero_button, "method 'onClickNumber'");
    view7f0a02b7 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.delete_imageView, "method 'onClickDelete'");
    view7f0a00b1 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDelete(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    EnterFrequencyActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.frequency_textView = null;
    target.line_frequency_view = null;
    target.edit_frequency_message_textView = null;
    target.number_buttons_linearLayout = null;
    target.save_changes_button = null;
    target.one_button = null;

    view7f0a01e8.setOnClickListener(null);
    view7f0a01e8 = null;
    view7f0a0192.setOnClickListener(null);
    view7f0a0192 = null;
    view7f0a0290.setOnClickListener(null);
    view7f0a0290 = null;
    view7f0a0280.setOnClickListener(null);
    view7f0a0280 = null;
    view7f0a0103.setOnClickListener(null);
    view7f0a0103 = null;
    view7f0a00fa.setOnClickListener(null);
    view7f0a00fa = null;
    view7f0a021c.setOnClickListener(null);
    view7f0a021c = null;
    view7f0a0210.setOnClickListener(null);
    view7f0a0210 = null;
    view7f0a00e6.setOnClickListener(null);
    view7f0a00e6 = null;
    view7f0a017c.setOnClickListener(null);
    view7f0a017c = null;
    view7f0a02b7.setOnClickListener(null);
    view7f0a02b7 = null;
    view7f0a00b1.setOnClickListener(null);
    view7f0a00b1 = null;
  }
}
