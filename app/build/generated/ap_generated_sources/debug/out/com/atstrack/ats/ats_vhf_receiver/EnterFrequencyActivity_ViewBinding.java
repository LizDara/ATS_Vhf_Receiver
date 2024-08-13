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

  private View view7f0a01e2;

  private View view7f0a018b;

  private View view7f0a028e;

  private View view7f0a027d;

  private View view7f0a00fd;

  private View view7f0a00f4;

  private View view7f0a0216;

  private View view7f0a020a;

  private View view7f0a00e4;

  private View view7f0a0175;

  private View view7f0a02b2;

  private View view7f0a00ad;

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
    view7f0a01e2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSaveChanges(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.one_button, "field 'one_button' and method 'onClickNumber'");
    target.one_button = Utils.castView(view, R.id.one_button, "field 'one_button'", Button.class);
    view7f0a018b = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.two_button, "method 'onClickNumber'");
    view7f0a028e = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.three_button, "method 'onClickNumber'");
    view7f0a027d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.four_button, "method 'onClickNumber'");
    view7f0a00fd = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.five_button, "method 'onClickNumber'");
    view7f0a00f4 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.six_button, "method 'onClickNumber'");
    view7f0a0216 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.seven_button, "method 'onClickNumber'");
    view7f0a020a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.eight_button, "method 'onClickNumber'");
    view7f0a00e4 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.nine_button, "method 'onClickNumber'");
    view7f0a0175 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.zero_button, "method 'onClickNumber'");
    view7f0a02b2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.delete_imageView, "method 'onClickDelete'");
    view7f0a00ad = view;
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

    view7f0a01e2.setOnClickListener(null);
    view7f0a01e2 = null;
    view7f0a018b.setOnClickListener(null);
    view7f0a018b = null;
    view7f0a028e.setOnClickListener(null);
    view7f0a028e = null;
    view7f0a027d.setOnClickListener(null);
    view7f0a027d = null;
    view7f0a00fd.setOnClickListener(null);
    view7f0a00fd = null;
    view7f0a00f4.setOnClickListener(null);
    view7f0a00f4 = null;
    view7f0a0216.setOnClickListener(null);
    view7f0a0216 = null;
    view7f0a020a.setOnClickListener(null);
    view7f0a020a = null;
    view7f0a00e4.setOnClickListener(null);
    view7f0a00e4 = null;
    view7f0a0175.setOnClickListener(null);
    view7f0a0175 = null;
    view7f0a02b2.setOnClickListener(null);
    view7f0a02b2 = null;
    view7f0a00ad.setOnClickListener(null);
    view7f0a00ad = null;
  }
}
