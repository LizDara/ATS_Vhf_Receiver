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

  private View view7f0a01d4;

  private View view7f0a0183;

  private View view7f0a0278;

  private View view7f0a0268;

  private View view7f0a00f7;

  private View view7f0a00ee;

  private View view7f0a0204;

  private View view7f0a01f8;

  private View view7f0a00df;

  private View view7f0a016e;

  private View view7f0a029c;

  private View view7f0a00aa;

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
    view7f0a01d4 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSaveChanges(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.one_button, "field 'one_button' and method 'onClickNumber'");
    target.one_button = Utils.castView(view, R.id.one_button, "field 'one_button'", Button.class);
    view7f0a0183 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.two_button, "method 'onClickNumber'");
    view7f0a0278 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.three_button, "method 'onClickNumber'");
    view7f0a0268 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.four_button, "method 'onClickNumber'");
    view7f0a00f7 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.five_button, "method 'onClickNumber'");
    view7f0a00ee = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.six_button, "method 'onClickNumber'");
    view7f0a0204 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.seven_button, "method 'onClickNumber'");
    view7f0a01f8 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.eight_button, "method 'onClickNumber'");
    view7f0a00df = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.nine_button, "method 'onClickNumber'");
    view7f0a016e = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.zero_button, "method 'onClickNumber'");
    view7f0a029c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNumber(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.delete_imageView, "method 'onClickDelete'");
    view7f0a00aa = view;
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

    view7f0a01d4.setOnClickListener(null);
    view7f0a01d4 = null;
    view7f0a0183.setOnClickListener(null);
    view7f0a0183 = null;
    view7f0a0278.setOnClickListener(null);
    view7f0a0278 = null;
    view7f0a0268.setOnClickListener(null);
    view7f0a0268 = null;
    view7f0a00f7.setOnClickListener(null);
    view7f0a00f7 = null;
    view7f0a00ee.setOnClickListener(null);
    view7f0a00ee = null;
    view7f0a0204.setOnClickListener(null);
    view7f0a0204 = null;
    view7f0a01f8.setOnClickListener(null);
    view7f0a01f8 = null;
    view7f0a00df.setOnClickListener(null);
    view7f0a00df = null;
    view7f0a016e.setOnClickListener(null);
    view7f0a016e = null;
    view7f0a029c.setOnClickListener(null);
    view7f0a029c = null;
    view7f0a00aa.setOnClickListener(null);
    view7f0a00aa = null;
  }
}
