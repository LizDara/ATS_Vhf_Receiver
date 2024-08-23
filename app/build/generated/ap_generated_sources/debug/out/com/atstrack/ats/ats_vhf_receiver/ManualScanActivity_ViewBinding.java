// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ManualScanActivity_ViewBinding implements Unbinder {
  private ManualScanActivity target;

  private View view7f0a01c5;

  private View view7f0a0167;

  private View view7f0a019c;

  private View view7f0a00e6;

  private View view7f0a021e;

  private View view7f0a00d7;

  private View view7f0a00d6;

  @UiThread
  public ManualScanActivity_ViewBinding(ManualScanActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ManualScanActivity_ViewBinding(final ManualScanActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.ready_manual_scan_LinearLayout = Utils.findRequiredViewAsType(source, R.id.ready_manual_scan_linearLayout, "field 'ready_manual_scan_LinearLayout'", LinearLayout.class);
    target.frequency_manual_textView = Utils.findRequiredViewAsType(source, R.id.frequency_manual_textView, "field 'frequency_manual_textView'", TextView.class);
    target.manual_gps_switch = Utils.findRequiredViewAsType(source, R.id.manual_gps_switch, "field 'manual_gps_switch'", SwitchCompat.class);
    target.manual_scan_linearLayout = Utils.findRequiredViewAsType(source, R.id.manual_scan_linearLayout, "field 'manual_scan_linearLayout'", LinearLayout.class);
    target.frequency_scan_manual_textView = Utils.findRequiredViewAsType(source, R.id.frequency_scan_manual_textView, "field 'frequency_scan_manual_textView'", TextView.class);
    target.scan_details_linearLayout = Utils.findRequiredViewAsType(source, R.id.scan_details_linearLayout, "field 'scan_details_linearLayout'", LinearLayout.class);
    target.code_textView = Utils.findRequiredViewAsType(source, R.id.code_textView, "field 'code_textView'", TextView.class);
    target.mortality_textView = Utils.findRequiredViewAsType(source, R.id.mortality_textView, "field 'mortality_textView'", TextView.class);
    target.period_textView = Utils.findRequiredViewAsType(source, R.id.period_textView, "field 'period_textView'", TextView.class);
    target.pulse_rate_textView = Utils.findRequiredViewAsType(source, R.id.pulse_rate_textView, "field 'pulse_rate_textView'", TextView.class);
    target.line_view = Utils.findRequiredView(source, R.id.line_view, "field 'line_view'");
    view = Utils.findRequiredView(source, R.id.record_data_manual_button, "field 'record_data_manual_button' and method 'onClickRecordData'");
    target.record_data_manual_button = Utils.castView(view, R.id.record_data_manual_button, "field 'record_data_manual_button'", Button.class);
    view7f0a01c5 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickRecordData(p0);
      }
    });
    target.audio_manual_linearLayout = Utils.findRequiredViewAsType(source, R.id.audio_manual_linearLayout, "field 'audio_manual_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.minus_imageView, "field 'minus_imageView' and method 'onClickMinus'");
    target.minus_imageView = Utils.castView(view, R.id.minus_imageView, "field 'minus_imageView'", ImageView.class);
    view7f0a0167 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickMinus(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.plus_imageView, "field 'plus_imageView' and method 'onClickPlus'");
    target.plus_imageView = Utils.castView(view, R.id.plus_imageView, "field 'plus_imageView'", ImageView.class);
    view7f0a019c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickPlus(p0);
      }
    });
    target.gps_manual_imageView = Utils.findRequiredViewAsType(source, R.id.gps_manual_imageView, "field 'gps_manual_imageView'", ImageView.class);
    target.gps_state_manual_textView = Utils.findRequiredViewAsType(source, R.id.gps_state_manual_textView, "field 'gps_state_manual_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.enter_new_frequency_button, "method 'onClickEnterNewFrequency'");
    view7f0a00e6 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEnterNewFrequency(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.start_manual_button, "method 'onClickStartManual'");
    view7f0a021e = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStartManual(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.edit_frequency_button, "method 'onClickEditFrequency'");
    view7f0a00d7 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEditFrequency(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.edit_audio_manual_textView, "method 'onClickEditAudio'");
    view7f0a00d6 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEditAudio(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    ManualScanActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.ready_manual_scan_LinearLayout = null;
    target.frequency_manual_textView = null;
    target.manual_gps_switch = null;
    target.manual_scan_linearLayout = null;
    target.frequency_scan_manual_textView = null;
    target.scan_details_linearLayout = null;
    target.code_textView = null;
    target.mortality_textView = null;
    target.period_textView = null;
    target.pulse_rate_textView = null;
    target.line_view = null;
    target.record_data_manual_button = null;
    target.audio_manual_linearLayout = null;
    target.minus_imageView = null;
    target.plus_imageView = null;
    target.gps_manual_imageView = null;
    target.gps_state_manual_textView = null;

    view7f0a01c5.setOnClickListener(null);
    view7f0a01c5 = null;
    view7f0a0167.setOnClickListener(null);
    view7f0a0167 = null;
    view7f0a019c.setOnClickListener(null);
    view7f0a019c = null;
    view7f0a00e6.setOnClickListener(null);
    view7f0a00e6 = null;
    view7f0a021e.setOnClickListener(null);
    view7f0a021e = null;
    view7f0a00d7.setOnClickListener(null);
    view7f0a00d7 = null;
    view7f0a00d6.setOnClickListener(null);
    view7f0a00d6 = null;
  }
}
