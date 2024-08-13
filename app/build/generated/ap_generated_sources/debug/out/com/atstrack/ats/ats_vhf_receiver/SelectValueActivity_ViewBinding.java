// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SelectValueActivity_ViewBinding implements Unbinder {
  private SelectValueActivity target;

  private View view7f0a008e;

  private View view7f0a00fa;

  private View view7f0a029e;

  private View view7f0a017d;

  private View view7f0a0270;

  private View view7f0a01a0;

  private View view7f0a0290;

  private View view7f0a027f;

  private View view7f0a00ff;

  private View view7f0a00f6;

  private View view7f0a0218;

  private View view7f0a020c;

  private View view7f0a00e6;

  @UiThread
  public SelectValueActivity_ViewBinding(SelectValueActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SelectValueActivity_ViewBinding(final SelectValueActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.select_pulse_rate_linearLayout = Utils.findRequiredViewAsType(source, R.id.select_pulse_rate_linearLayout, "field 'select_pulse_rate_linearLayout'", LinearLayout.class);
    target.number_of_matches_scrollView = Utils.findRequiredViewAsType(source, R.id.number_of_matches_scrollView, "field 'number_of_matches_scrollView'", ScrollView.class);
    target.coded_imageView = Utils.findRequiredViewAsType(source, R.id.coded_imageView, "field 'coded_imageView'", ImageView.class);
    target.fixed_pulse_rate_imageView = Utils.findRequiredViewAsType(source, R.id.fixed_pulse_rate_imageView, "field 'fixed_pulse_rate_imageView'", ImageView.class);
    target.variable_pulse_rate_imageView = Utils.findRequiredViewAsType(source, R.id.variable_pulse_rate_imageView, "field 'variable_pulse_rate_imageView'", ImageView.class);
    target.max_min_pulse_rate_linearLayout = Utils.findRequiredViewAsType(source, R.id.max_min_pulse_rate_linearLayout, "field 'max_min_pulse_rate_linearLayout'", LinearLayout.class);
    target.max_min_pulse_rate_textView = Utils.findRequiredViewAsType(source, R.id.max_min_pulse_rate_textView, "field 'max_min_pulse_rate_textView'", TextView.class);
    target.max_min_pulse_rate_editText = Utils.findRequiredViewAsType(source, R.id.max_min_pulse_rate_editText, "field 'max_min_pulse_rate_editText'", EditText.class);
    target.period_pulse_rate_textView = Utils.findRequiredViewAsType(source, R.id.period_pulse_rate_textView, "field 'period_pulse_rate_textView'", TextView.class);
    target.data_calculation_types_linearLayout = Utils.findRequiredViewAsType(source, R.id.data_calculation_types_linearLayout, "field 'data_calculation_types_linearLayout'", LinearLayout.class);
    target.none_imageView = Utils.findRequiredViewAsType(source, R.id.none_imageView, "field 'none_imageView'", ImageView.class);
    target.temperature_imageView = Utils.findRequiredViewAsType(source, R.id.temperature_imageView, "field 'temperature_imageView'", ImageView.class);
    target.period_imageView = Utils.findRequiredViewAsType(source, R.id.period_imageView, "field 'period_imageView'", ImageView.class);
    target.two_imageView = Utils.findRequiredViewAsType(source, R.id.two_imageView, "field 'two_imageView'", ImageView.class);
    target.three_imageView = Utils.findRequiredViewAsType(source, R.id.three_imageView, "field 'three_imageView'", ImageView.class);
    target.four_imageView = Utils.findRequiredViewAsType(source, R.id.four_imageView, "field 'four_imageView'", ImageView.class);
    target.five_imageView = Utils.findRequiredViewAsType(source, R.id.five_imageView, "field 'five_imageView'", ImageView.class);
    target.six_imageView = Utils.findRequiredViewAsType(source, R.id.six_imageView, "field 'six_imageView'", ImageView.class);
    target.seven_imageView = Utils.findRequiredViewAsType(source, R.id.seven_imageView, "field 'seven_imageView'", ImageView.class);
    target.eight_imageView = Utils.findRequiredViewAsType(source, R.id.eight_imageView, "field 'eight_imageView'", ImageView.class);
    target.pulse_rate_linearLayout = Utils.findRequiredViewAsType(source, R.id.pulse_rate_linearLayout, "field 'pulse_rate_linearLayout'", LinearLayout.class);
    target.pulse_rate_textView = Utils.findRequiredViewAsType(source, R.id.pulse_rate_textView, "field 'pulse_rate_textView'", TextView.class);
    target.pulse_rate_editText = Utils.findRequiredViewAsType(source, R.id.pulse_rate_editText, "field 'pulse_rate_editText'", EditText.class);
    target.pulse_rate_tolerance_textView = Utils.findRequiredViewAsType(source, R.id.pulse_rate_tolerance_textView, "field 'pulse_rate_tolerance_textView'", TextView.class);
    target.pulse_rate_tolerance_editText = Utils.findRequiredViewAsType(source, R.id.pulse_rate_tolerance_editText, "field 'pulse_rate_tolerance_editText'", EditText.class);
    view = Utils.findRequiredView(source, R.id.coded_linearLayout, "method 'onClickCoded'");
    view7f0a008e = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickCoded(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.fixed_pulse_rate_linearLayout, "method 'onClickFixedPulseRate'");
    view7f0a00fa = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFixedPulseRate(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.variable_pulse_rate_linearLayout, "method 'onClickVariablePulseRate'");
    view7f0a029e = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickVariablePulseRate(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.none_linearLayout, "method 'onClickNone'");
    view7f0a017d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNone(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.temperature_linearLayout, "method 'onClickTemperature'");
    view7f0a0270 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickTemperature(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.period_linearLayout, "method 'onClickPeriod'");
    view7f0a01a0 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickPeriod(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.two_linearLayout, "method 'onClickTwo'");
    view7f0a0290 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickTwo(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.three_linearLayout, "method 'onClickThree'");
    view7f0a027f = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickThree(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.four_linearLayout, "method 'onClickFour'");
    view7f0a00ff = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFour(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.five_linearLayout, "method 'onClickFive'");
    view7f0a00f6 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFive(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.six_linearLayout, "method 'onClickSix'");
    view7f0a0218 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSix(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.seven_linearLayout, "method 'onClickSeven'");
    view7f0a020c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSeven(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.eight_linearLayout, "method 'onClickEight'");
    view7f0a00e6 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEight(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    SelectValueActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.select_pulse_rate_linearLayout = null;
    target.number_of_matches_scrollView = null;
    target.coded_imageView = null;
    target.fixed_pulse_rate_imageView = null;
    target.variable_pulse_rate_imageView = null;
    target.max_min_pulse_rate_linearLayout = null;
    target.max_min_pulse_rate_textView = null;
    target.max_min_pulse_rate_editText = null;
    target.period_pulse_rate_textView = null;
    target.data_calculation_types_linearLayout = null;
    target.none_imageView = null;
    target.temperature_imageView = null;
    target.period_imageView = null;
    target.two_imageView = null;
    target.three_imageView = null;
    target.four_imageView = null;
    target.five_imageView = null;
    target.six_imageView = null;
    target.seven_imageView = null;
    target.eight_imageView = null;
    target.pulse_rate_linearLayout = null;
    target.pulse_rate_textView = null;
    target.pulse_rate_editText = null;
    target.pulse_rate_tolerance_textView = null;
    target.pulse_rate_tolerance_editText = null;

    view7f0a008e.setOnClickListener(null);
    view7f0a008e = null;
    view7f0a00fa.setOnClickListener(null);
    view7f0a00fa = null;
    view7f0a029e.setOnClickListener(null);
    view7f0a029e = null;
    view7f0a017d.setOnClickListener(null);
    view7f0a017d = null;
    view7f0a0270.setOnClickListener(null);
    view7f0a0270 = null;
    view7f0a01a0.setOnClickListener(null);
    view7f0a01a0 = null;
    view7f0a0290.setOnClickListener(null);
    view7f0a0290 = null;
    view7f0a027f.setOnClickListener(null);
    view7f0a027f = null;
    view7f0a00ff.setOnClickListener(null);
    view7f0a00ff = null;
    view7f0a00f6.setOnClickListener(null);
    view7f0a00f6 = null;
    view7f0a0218.setOnClickListener(null);
    view7f0a0218 = null;
    view7f0a020c.setOnClickListener(null);
    view7f0a020c = null;
    view7f0a00e6.setOnClickListener(null);
    view7f0a00e6 = null;
  }
}
