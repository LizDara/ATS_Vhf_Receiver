// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class AerialScanActivity_ViewBinding implements Unbinder {
  private AerialScanActivity target;

  private View view7f0a00d8;

  private View view7f0a022e;

  private View view7f0a0123;

  private View view7f0a00a8;

  private View view7f0a0134;

  private View view7f0a00e3;

  private View view7f0a0168;

  private View view7f0a01d2;

  private View view7f0a0044;

  private View view7f0a00ac;

  private View view7f0a0167;

  private View view7f0a0142;

  private View view7f0a01e0;

  private View view7f0a00d9;

  @UiThread
  public AerialScanActivity_ViewBinding(AerialScanActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public AerialScanActivity_ViewBinding(final AerialScanActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.ready_aerial_scan_LinearLayout = Utils.findRequiredViewAsType(source, R.id.ready_aerial_scan_LinearLayout, "field 'ready_aerial_scan_LinearLayout'", LinearLayout.class);
    target.ready_aerial_textView = Utils.findRequiredViewAsType(source, R.id.ready_aerial_textView, "field 'ready_aerial_textView'", TextView.class);
    target.scan_rate_aerial_textView = Utils.findRequiredViewAsType(source, R.id.scan_rate_aerial_textView, "field 'scan_rate_aerial_textView'", TextView.class);
    target.selected_frequency_aerial_textView = Utils.findRequiredViewAsType(source, R.id.selected_frequency_aerial_textView, "field 'selected_frequency_aerial_textView'", TextView.class);
    target.gps_aerial_textView = Utils.findRequiredViewAsType(source, R.id.gps_aerial_textView, "field 'gps_aerial_textView'", TextView.class);
    target.auto_record_aerial_textView = Utils.findRequiredViewAsType(source, R.id.auto_record_aerial_textView, "field 'auto_record_aerial_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.edit_aerial_settings_button, "field 'edit_aerial_defaults_textView' and method 'onClickEditAerialSettings'");
    target.edit_aerial_defaults_textView = Utils.castView(view, R.id.edit_aerial_settings_button, "field 'edit_aerial_defaults_textView'", TextView.class);
    view7f0a00d8 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEditAerialSettings(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.start_aerial_button, "field 'start_aerial_button' and method 'onClickStartAerial'");
    target.start_aerial_button = Utils.castView(view, R.id.start_aerial_button, "field 'start_aerial_button'", Button.class);
    view7f0a022e = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickStartAerial(p0);
      }
    });
    target.aerial_result_linearLayout = Utils.findRequiredViewAsType(source, R.id.aerial_result_linearLayout, "field 'aerial_result_linearLayout'", LinearLayout.class);
    target.max_index_aerial_textView = Utils.findRequiredViewAsType(source, R.id.max_index_aerial_textView, "field 'max_index_aerial_textView'", TextView.class);
    target.table_index_aerial_textView = Utils.findRequiredViewAsType(source, R.id.table_index_aerial_textView, "field 'table_index_aerial_textView'", TextView.class);
    target.frequency_aerial_textView = Utils.findRequiredViewAsType(source, R.id.frequency_aerial_textView, "field 'frequency_aerial_textView'", TextView.class);
    target.scan_details_linearLayout = Utils.findRequiredViewAsType(source, R.id.scan_details_linearLayout, "field 'scan_details_linearLayout'", LinearLayout.class);
    target.code_textView = Utils.findRequiredViewAsType(source, R.id.code_textView, "field 'code_textView'", TextView.class);
    target.mortality_textView = Utils.findRequiredViewAsType(source, R.id.mortality_textView, "field 'mortality_textView'", TextView.class);
    target.period_textView = Utils.findRequiredViewAsType(source, R.id.period_textView, "field 'period_textView'", TextView.class);
    target.pulse_rate_textView = Utils.findRequiredViewAsType(source, R.id.pulse_rate_textView, "field 'pulse_rate_textView'", TextView.class);
    target.line_view = Utils.findRequiredView(source, R.id.line_view, "field 'line_view'");
    target.hold_aerial_imageView = Utils.findRequiredViewAsType(source, R.id.hold_aerial_imageView, "field 'hold_aerial_imageView'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.hold_aerial_button, "field 'hold_aerial_button' and method 'onClickHoldAerial'");
    target.hold_aerial_button = Utils.castView(view, R.id.hold_aerial_button, "field 'hold_aerial_button'", TextView.class);
    view7f0a0123 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickHoldAerial(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.decrease_imageView, "field 'decrease_imageView' and method 'onClickDecrease'");
    target.decrease_imageView = Utils.castView(view, R.id.decrease_imageView, "field 'decrease_imageView'", ImageView.class);
    view7f0a00a8 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDecrease(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.increase_imageView, "field 'increase_imageView' and method 'onClickIncrease'");
    target.increase_imageView = Utils.castView(view, R.id.increase_imageView, "field 'increase_imageView'", ImageView.class);
    view7f0a0134 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickIncrease(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.edit_table_textView, "field 'edit_table_textView' and method 'onClickEditTable'");
    target.edit_table_textView = Utils.castView(view, R.id.edit_table_textView, "field 'edit_table_textView'", TextView.class);
    view7f0a00e3 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEditTable(p0);
      }
    });
    target.edit_table_linearLayout = Utils.findRequiredViewAsType(source, R.id.edit_table_linearLayout, "field 'edit_table_linearLayout'", LinearLayout.class);
    target.merge_tables_linearLayout = Utils.findRequiredViewAsType(source, R.id.merge_tables_linearLayout, "field 'merge_tables_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.merge_tables_button, "field 'merge_tables_button' and method 'onClickMergeTables'");
    target.merge_tables_button = Utils.castView(view, R.id.merge_tables_button, "field 'merge_tables_button'", Button.class);
    view7f0a0168 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickMergeTables(p0);
      }
    });
    target.audio_aerial_linearLayout = Utils.findRequiredViewAsType(source, R.id.audio_aerial_linearLayout, "field 'audio_aerial_linearLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.record_data_aerial_button, "field 'record_data_aerial_button' and method 'onClickRecordData'");
    target.record_data_aerial_button = Utils.castView(view, R.id.record_data_aerial_button, "field 'record_data_aerial_button'", Button.class);
    view7f0a01d2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickRecordData(p0);
      }
    });
    target.current_frequency_aerial_textView = Utils.findRequiredViewAsType(source, R.id.current_frequency_aerial_textView, "field 'current_frequency_aerial_textView'", TextView.class);
    target.current_index_aerial_textView = Utils.findRequiredViewAsType(source, R.id.current_index_aerial_textView, "field 'current_index_aerial_textView'", TextView.class);
    target.table_total_aerial_textView = Utils.findRequiredViewAsType(source, R.id.table_total_aerial_textView, "field 'table_total_aerial_textView'", TextView.class);
    target.tables_merge_listView = Utils.findRequiredViewAsType(source, R.id.tables_merge_listView, "field 'tables_merge_listView'", ListView.class);
    target.gps_aerial_imageView = Utils.findRequiredViewAsType(source, R.id.gps_aerial_imageView, "field 'gps_aerial_imageView'", ImageView.class);
    target.gps_state_aerial_textView = Utils.findRequiredViewAsType(source, R.id.gps_state_aerial_textView, "field 'gps_state_aerial_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.add_frequency_scan_button, "method 'onClickAddFrequencyScan'");
    view7f0a0044 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickAddFrequencyScan(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.delete_frequency_scan_button, "method 'onClickDeleteFrequencyScan'");
    view7f0a00ac = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDeleteFrequencyScan(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.merge_table_scan_button, "method 'onClickMergeTableScan'");
    view7f0a0167 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickMergeTableScan(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.left_imageView, "method 'onClickLeft'");
    view7f0a0142 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickLeft(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.right_imageView, "method 'onClickRight'");
    view7f0a01e0 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickRight(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.edit_audio_aerial_textView, "method 'onClickEditAudio'");
    view7f0a00d9 = view;
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
    AerialScanActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.ready_aerial_scan_LinearLayout = null;
    target.ready_aerial_textView = null;
    target.scan_rate_aerial_textView = null;
    target.selected_frequency_aerial_textView = null;
    target.gps_aerial_textView = null;
    target.auto_record_aerial_textView = null;
    target.edit_aerial_defaults_textView = null;
    target.start_aerial_button = null;
    target.aerial_result_linearLayout = null;
    target.max_index_aerial_textView = null;
    target.table_index_aerial_textView = null;
    target.frequency_aerial_textView = null;
    target.scan_details_linearLayout = null;
    target.code_textView = null;
    target.mortality_textView = null;
    target.period_textView = null;
    target.pulse_rate_textView = null;
    target.line_view = null;
    target.hold_aerial_imageView = null;
    target.hold_aerial_button = null;
    target.decrease_imageView = null;
    target.increase_imageView = null;
    target.edit_table_textView = null;
    target.edit_table_linearLayout = null;
    target.merge_tables_linearLayout = null;
    target.merge_tables_button = null;
    target.audio_aerial_linearLayout = null;
    target.record_data_aerial_button = null;
    target.current_frequency_aerial_textView = null;
    target.current_index_aerial_textView = null;
    target.table_total_aerial_textView = null;
    target.tables_merge_listView = null;
    target.gps_aerial_imageView = null;
    target.gps_state_aerial_textView = null;

    view7f0a00d8.setOnClickListener(null);
    view7f0a00d8 = null;
    view7f0a022e.setOnClickListener(null);
    view7f0a022e = null;
    view7f0a0123.setOnClickListener(null);
    view7f0a0123 = null;
    view7f0a00a8.setOnClickListener(null);
    view7f0a00a8 = null;
    view7f0a0134.setOnClickListener(null);
    view7f0a0134 = null;
    view7f0a00e3.setOnClickListener(null);
    view7f0a00e3 = null;
    view7f0a0168.setOnClickListener(null);
    view7f0a0168 = null;
    view7f0a01d2.setOnClickListener(null);
    view7f0a01d2 = null;
    view7f0a0044.setOnClickListener(null);
    view7f0a0044 = null;
    view7f0a00ac.setOnClickListener(null);
    view7f0a00ac = null;
    view7f0a0167.setOnClickListener(null);
    view7f0a0167 = null;
    view7f0a0142.setOnClickListener(null);
    view7f0a0142 = null;
    view7f0a01e0.setOnClickListener(null);
    view7f0a01e0 = null;
    view7f0a00d9.setOnClickListener(null);
    view7f0a00d9 = null;
  }
}
