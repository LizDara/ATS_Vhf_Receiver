// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.ImageView;
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

public class SetTransmitterTypeActivity_ViewBinding implements Unbinder {
  private SetTransmitterTypeActivity target;

  private View view7f0a0151;

  private View view7f0a01b6;

  private View view7f0a0158;

  private View view7f0a0165;

  private View view7f0a0189;

  private View view7f0a019f;

  private View view7f0a01a2;

  @UiThread
  public SetTransmitterTypeActivity_ViewBinding(SetTransmitterTypeActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SetTransmitterTypeActivity_ViewBinding(final SetTransmitterTypeActivity target,
      View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.pulse_rate_type_textView = Utils.findRequiredViewAsType(source, R.id.pulse_rate_type_textView, "field 'pulse_rate_type_textView'", TextView.class);
    target.matches_for_valid_pattern_textView = Utils.findRequiredViewAsType(source, R.id.matches_for_valid_pattern_textView, "field 'matches_for_valid_pattern_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.matches_for_valid_pattern_linearLayout, "field 'matches_for_valid_pattern_linearLayout' and method 'onClickMatchesValidPattern'");
    target.matches_for_valid_pattern_linearLayout = Utils.castView(view, R.id.matches_for_valid_pattern_linearLayout, "field 'matches_for_valid_pattern_linearLayout'", LinearLayout.class);
    view7f0a0151 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickMatchesValidPattern(p0);
      }
    });
    target.pulse_rates_linearLayout = Utils.findRequiredViewAsType(source, R.id.pulse_rates_linearLayout, "field 'pulse_rates_linearLayout'", LinearLayout.class);
    target.max_pulse_rate_textView = Utils.findRequiredViewAsType(source, R.id.max_pulse_rate_textView, "field 'max_pulse_rate_textView'", TextView.class);
    target.min_pulse_rate_textView = Utils.findRequiredViewAsType(source, R.id.min_pulse_rate_textView, "field 'min_pulse_rate_textView'", TextView.class);
    target.optional_data_textView = Utils.findRequiredViewAsType(source, R.id.optional_data_textView, "field 'optional_data_textView'", TextView.class);
    target.pulse_rate_type_imageView = Utils.findRequiredViewAsType(source, R.id.pulse_rate_type_imageView, "field 'pulse_rate_type_imageView'", ImageView.class);
    target.target_pulse_rate_linearLayout = Utils.findRequiredViewAsType(source, R.id.target_pulse_rate_linearLayout, "field 'target_pulse_rate_linearLayout'", LinearLayout.class);
    target.pr1_textView = Utils.findRequiredViewAsType(source, R.id.pr1_textView, "field 'pr1_textView'", TextView.class);
    target.pr1_tolerance_textView = Utils.findRequiredViewAsType(source, R.id.pr1_tolerance_textView, "field 'pr1_tolerance_textView'", TextView.class);
    target.pr2_textView = Utils.findRequiredViewAsType(source, R.id.pr2_textView, "field 'pr2_textView'", TextView.class);
    target.pr2_tolerance_textView = Utils.findRequiredViewAsType(source, R.id.pr2_tolerance_textView, "field 'pr2_tolerance_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.pulse_rate_type_linearLayout, "field 'pulse_rate_type_linearLayout' and method 'onClickPulseRateType'");
    target.pulse_rate_type_linearLayout = Utils.castView(view, R.id.pulse_rate_type_linearLayout, "field 'pulse_rate_type_linearLayout'", LinearLayout.class);
    view7f0a01b6 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickPulseRateType(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.max_pulse_rate_linearLayout, "method 'onClickMaxPulseRate'");
    view7f0a0158 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickMaxPulseRate(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.min_pulse_rate_linearLayout, "method 'onClickMinPulseRate'");
    view7f0a0165 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickMinPulseRate(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.optional_data_linearLayout, "method 'onClickOptionalDataCalculations'");
    view7f0a0189 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickOptionalDataCalculations(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.pr1_linearLayout, "method 'onClickPR1'");
    view7f0a019f = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickPR1(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.pr2_linearLayout, "method 'onClickPR2'");
    view7f0a01a2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickPR2(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    SetTransmitterTypeActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.pulse_rate_type_textView = null;
    target.matches_for_valid_pattern_textView = null;
    target.matches_for_valid_pattern_linearLayout = null;
    target.pulse_rates_linearLayout = null;
    target.max_pulse_rate_textView = null;
    target.min_pulse_rate_textView = null;
    target.optional_data_textView = null;
    target.pulse_rate_type_imageView = null;
    target.target_pulse_rate_linearLayout = null;
    target.pr1_textView = null;
    target.pr1_tolerance_textView = null;
    target.pr2_textView = null;
    target.pr2_tolerance_textView = null;
    target.pulse_rate_type_linearLayout = null;

    view7f0a0151.setOnClickListener(null);
    view7f0a0151 = null;
    view7f0a01b6.setOnClickListener(null);
    view7f0a01b6 = null;
    view7f0a0158.setOnClickListener(null);
    view7f0a0158 = null;
    view7f0a0165.setOnClickListener(null);
    view7f0a0165 = null;
    view7f0a0189.setOnClickListener(null);
    view7f0a0189 = null;
    view7f0a019f.setOnClickListener(null);
    view7f0a019f = null;
    view7f0a01a2.setOnClickListener(null);
    view7f0a01a2 = null;
  }
}
