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

  private View view7f0a008b;

  private View view7f0a00f4;

  private View view7f0a0288;

  private View view7f0a0176;

  private View view7f0a025b;

  private View view7f0a0198;

  private View view7f0a027a;

  private View view7f0a026a;

  private View view7f0a00f9;

  private View view7f0a00f0;

  private View view7f0a0206;

  private View view7f0a01fa;

  private View view7f0a00e1;

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
    view7f0a008b = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickCoded(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.fixed_pulse_rate_linearLayout, "method 'onClickFixedPulseRate'");
    view7f0a00f4 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFixedPulseRate(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.variable_pulse_rate_linearLayout, "method 'onClickVariablePulseRate'");
    view7f0a0288 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickVariablePulseRate(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.none_linearLayout, "method 'onClickNone'");
    view7f0a0176 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNone(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.temperature_linearLayout, "method 'onClickTemperature'");
    view7f0a025b = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickTemperature(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.period_linearLayout, "method 'onClickPeriod'");
    view7f0a0198 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickPeriod(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.two_linearLayout, "method 'onClickTwo'");
    view7f0a027a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickTwo(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.three_linearLayout, "method 'onClickThree'");
    view7f0a026a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickThree(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.four_linearLayout, "method 'onClickFour'");
    view7f0a00f9 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFour(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.five_linearLayout, "method 'onClickFive'");
    view7f0a00f0 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickFive(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.six_linearLayout, "method 'onClickSix'");
    view7f0a0206 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSix(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.seven_linearLayout, "method 'onClickSeven'");
    view7f0a01fa = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickSeven(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.eight_linearLayout, "method 'onClickEight'");
    view7f0a00e1 = view;
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

    view7f0a008b.setOnClickListener(null);
    view7f0a008b = null;
    view7f0a00f4.setOnClickListener(null);
    view7f0a00f4 = null;
    view7f0a0288.setOnClickListener(null);
    view7f0a0288 = null;
    view7f0a0176.setOnClickListener(null);
    view7f0a0176 = null;
    view7f0a025b.setOnClickListener(null);
    view7f0a025b = null;
    view7f0a0198.setOnClickListener(null);
    view7f0a0198 = null;
    view7f0a027a.setOnClickListener(null);
    view7f0a027a = null;
    view7f0a026a.setOnClickListener(null);
    view7f0a026a = null;
    view7f0a00f9.setOnClickListener(null);
    view7f0a00f9 = null;
    view7f0a00f0.setOnClickListener(null);
    view7f0a00f0 = null;
    view7f0a0206.setOnClickListener(null);
    view7f0a0206 = null;
    view7f0a01fa.setOnClickListener(null);
    view7f0a01fa = null;
    view7f0a00e1.setOnClickListener(null);
    view7f0a00e1 = null;
  }
}
