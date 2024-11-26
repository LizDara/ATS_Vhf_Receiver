// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
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

public class TestReceiverActivity_ViewBinding implements Unbinder {
  private TestReceiverActivity target;

  private View view7f0a0299;

  @UiThread
  public TestReceiverActivity_ViewBinding(TestReceiverActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public TestReceiverActivity_ViewBinding(final TestReceiverActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.loading_linearLayout = Utils.findRequiredViewAsType(source, R.id.loading_linearLayout, "field 'loading_linearLayout'", LinearLayout.class);
    target.test_complete_scrollView = Utils.findRequiredViewAsType(source, R.id.test_complete_scrollView, "field 'test_complete_scrollView'", ScrollView.class);
    target.range_textView = Utils.findRequiredViewAsType(source, R.id.range_textView, "field 'range_textView'", TextView.class);
    target.battery_textView = Utils.findRequiredViewAsType(source, R.id.battery_textView, "field 'battery_textView'", TextView.class);
    target.bytes_stored_test_textView = Utils.findRequiredViewAsType(source, R.id.bytes_stored_test_textView, "field 'bytes_stored_test_textView'", TextView.class);
    target.memory_used_textView = Utils.findRequiredViewAsType(source, R.id.memory_used_textView, "field 'memory_used_textView'", TextView.class);
    target.frequency_tables_textView = Utils.findRequiredViewAsType(source, R.id.frequency_tables_textView, "field 'frequency_tables_textView'", TextView.class);
    target.first_table_textView = Utils.findRequiredViewAsType(source, R.id.first_table_textView, "field 'first_table_textView'", TextView.class);
    target.second_table_textView = Utils.findRequiredViewAsType(source, R.id.second_table_textView, "field 'second_table_textView'", TextView.class);
    target.third_table_textView = Utils.findRequiredViewAsType(source, R.id.third_table_textView, "field 'third_table_textView'", TextView.class);
    target.fourth_table_textView = Utils.findRequiredViewAsType(source, R.id.fourth_table_textView, "field 'fourth_table_textView'", TextView.class);
    target.fifth_table_textView = Utils.findRequiredViewAsType(source, R.id.fifth_table_textView, "field 'fifth_table_textView'", TextView.class);
    target.sixth_table_textView = Utils.findRequiredViewAsType(source, R.id.sixth_table_textView, "field 'sixth_table_textView'", TextView.class);
    target.seventh_table_textView = Utils.findRequiredViewAsType(source, R.id.seventh_table_textView, "field 'seventh_table_textView'", TextView.class);
    target.eighth_table_textView = Utils.findRequiredViewAsType(source, R.id.eighth_table_textView, "field 'eighth_table_textView'", TextView.class);
    target.ninth_table_textView = Utils.findRequiredViewAsType(source, R.id.ninth_table_textView, "field 'ninth_table_textView'", TextView.class);
    target.tenth_table_textView = Utils.findRequiredViewAsType(source, R.id.tenth_table_textView, "field 'tenth_table_textView'", TextView.class);
    target.eleventh_table_textView = Utils.findRequiredViewAsType(source, R.id.eleventh_table_textView, "field 'eleventh_table_textView'", TextView.class);
    target.twelfth_table_textView = Utils.findRequiredViewAsType(source, R.id.twelfth_table_textView, "field 'twelfth_table_textView'", TextView.class);
    target.table1_linearLayout = Utils.findRequiredViewAsType(source, R.id.table_1_linearLayout, "field 'table1_linearLayout'", LinearLayout.class);
    target.table2_linearLayout = Utils.findRequiredViewAsType(source, R.id.table_2_linearLayout, "field 'table2_linearLayout'", LinearLayout.class);
    target.table3_linearLayout = Utils.findRequiredViewAsType(source, R.id.table3_linearLayout, "field 'table3_linearLayout'", LinearLayout.class);
    target.table4_linearLayout = Utils.findRequiredViewAsType(source, R.id.table_4_linearLayout, "field 'table4_linearLayout'", LinearLayout.class);
    target.table5_linearLayout = Utils.findRequiredViewAsType(source, R.id.table_5_linearLayout, "field 'table5_linearLayout'", LinearLayout.class);
    target.table6_linearLayout = Utils.findRequiredViewAsType(source, R.id.table_6_linearLayout, "field 'table6_linearLayout'", LinearLayout.class);
    target.table7_linearLayout = Utils.findRequiredViewAsType(source, R.id.table7_linearLayout, "field 'table7_linearLayout'", LinearLayout.class);
    target.table8_linearLayout = Utils.findRequiredViewAsType(source, R.id.table8_linearLayout, "field 'table8_linearLayout'", LinearLayout.class);
    target.table9_linearLayout = Utils.findRequiredViewAsType(source, R.id.table9_linearLayout, "field 'table9_linearLayout'", LinearLayout.class);
    target.table10_linearLayout = Utils.findRequiredViewAsType(source, R.id.table10_linearLayout, "field 'table10_linearLayout'", LinearLayout.class);
    target.table11_linearLayout = Utils.findRequiredViewAsType(source, R.id.table11_linearLayout, "field 'table11_linearLayout'", LinearLayout.class);
    target.table12_linearLayout = Utils.findRequiredViewAsType(source, R.id.table12_linearLayout, "field 'table12_linearLayout'", LinearLayout.class);
    target.tx_type_textView = Utils.findRequiredViewAsType(source, R.id.tx_type_textView, "field 'tx_type_textView'", TextView.class);
    target.software_version_textView = Utils.findRequiredViewAsType(source, R.id.software_version_textView, "field 'software_version_textView'", TextView.class);
    target.hardware_version_textView = Utils.findRequiredViewAsType(source, R.id.hardware_version_textView, "field 'hardware_version_textView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.update_receiver_button, "method 'onClickUpdateReceiver'");
    view7f0a0299 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickUpdateReceiver(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    TestReceiverActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.loading_linearLayout = null;
    target.test_complete_scrollView = null;
    target.range_textView = null;
    target.battery_textView = null;
    target.bytes_stored_test_textView = null;
    target.memory_used_textView = null;
    target.frequency_tables_textView = null;
    target.first_table_textView = null;
    target.second_table_textView = null;
    target.third_table_textView = null;
    target.fourth_table_textView = null;
    target.fifth_table_textView = null;
    target.sixth_table_textView = null;
    target.seventh_table_textView = null;
    target.eighth_table_textView = null;
    target.ninth_table_textView = null;
    target.tenth_table_textView = null;
    target.eleventh_table_textView = null;
    target.twelfth_table_textView = null;
    target.table1_linearLayout = null;
    target.table2_linearLayout = null;
    target.table3_linearLayout = null;
    target.table4_linearLayout = null;
    target.table5_linearLayout = null;
    target.table6_linearLayout = null;
    target.table7_linearLayout = null;
    target.table8_linearLayout = null;
    target.table9_linearLayout = null;
    target.table10_linearLayout = null;
    target.table11_linearLayout = null;
    target.table12_linearLayout = null;
    target.tx_type_textView = null;
    target.software_version_textView = null;
    target.hardware_version_textView = null;

    view7f0a0299.setOnClickListener(null);
    view7f0a0299 = null;
  }
}
