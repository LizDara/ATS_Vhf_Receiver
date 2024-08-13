// Generated code from Butter Knife. Do not modify!
package com.atstrack.ats.ats_vhf_receiver;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GetDataActivity_ViewBinding implements Unbinder {
  private GetDataActivity target;

  private View view7f0a00c7;

  private View view7f0a00ec;

  private View view7f0a0067;

  private View view7f0a007a;

  private View view7f0a01db;

  private View view7f0a00af;

  private View view7f0a01dc;

  @UiThread
  public GetDataActivity_ViewBinding(GetDataActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public GetDataActivity_ViewBinding(final GetDataActivity target, View source) {
    this.target = target;

    View view;
    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.title_toolbar = Utils.findRequiredViewAsType(source, R.id.title_toolbar, "field 'title_toolbar'", TextView.class);
    target.state_view = Utils.findRequiredView(source, R.id.state_view, "field 'state_view'");
    target.memory_used_percent_textView = Utils.findRequiredViewAsType(source, R.id.memory_used_percent_textView, "field 'memory_used_percent_textView'", TextView.class);
    target.memory_used_progressBar = Utils.findRequiredViewAsType(source, R.id.memory_used_progressBar, "field 'memory_used_progressBar'", ProgressBar.class);
    target.bytes_stored_textView = Utils.findRequiredViewAsType(source, R.id.bytes_stored_textView, "field 'bytes_stored_textView'", TextView.class);
    target.menu_manage_receiver_linearLayout = Utils.findRequiredViewAsType(source, R.id.menu_manage_receiver_linearLayout, "field 'menu_manage_receiver_linearLayout'", LinearLayout.class);
    target.begin_download_linearLayout = Utils.findRequiredViewAsType(source, R.id.begin_download_linearLayout, "field 'begin_download_linearLayout'", LinearLayout.class);
    target.downloading_file_linearLayout = Utils.findRequiredViewAsType(source, R.id.downloading_file_linearLayout, "field 'downloading_file_linearLayout'", LinearLayout.class);
    target.download_complete_linearLayout = Utils.findRequiredViewAsType(source, R.id.download_complete_linearLayout, "field 'download_complete_linearLayout'", LinearLayout.class);
    target.delete_linearLayout = Utils.findRequiredViewAsType(source, R.id.delete_linearLayout, "field 'delete_linearLayout'", LinearLayout.class);
    target.deleting_linearLayout = Utils.findRequiredViewAsType(source, R.id.deleting_linearLayout, "field 'deleting_linearLayout'", LinearLayout.class);
    target.deletion_complete_linearLayout = Utils.findRequiredViewAsType(source, R.id.deletion_complete_linearLayout, "field 'deletion_complete_linearLayout'", LinearLayout.class);
    target.downloading_data_imageView = Utils.findRequiredViewAsType(source, R.id.downloading_data_imageView, "field 'downloading_data_imageView'", ImageView.class);
    target.downloading_data_textView = Utils.findRequiredViewAsType(source, R.id.downloading_data_textView, "field 'downloading_data_textView'", TextView.class);
    target.downloading_progressBar = Utils.findRequiredViewAsType(source, R.id.downloading_progressBar, "field 'downloading_progressBar'", ProgressBar.class);
    target.processing_data_imageView = Utils.findRequiredViewAsType(source, R.id.processing_data_imageView, "field 'processing_data_imageView'", ImageView.class);
    target.processing_data_textView = Utils.findRequiredViewAsType(source, R.id.processing_data_textView, "field 'processing_data_textView'", TextView.class);
    target.processing_progressBar = Utils.findRequiredViewAsType(source, R.id.processing_progressBar, "field 'processing_progressBar'", ProgressBar.class);
    target.preparing_file_imageView = Utils.findRequiredViewAsType(source, R.id.preparing_file_imageView, "field 'preparing_file_imageView'", ImageView.class);
    target.preparing_file_textView = Utils.findRequiredViewAsType(source, R.id.preparing_file_textView, "field 'preparing_file_textView'", TextView.class);
    target.preparing_progressBar = Utils.findRequiredViewAsType(source, R.id.preparing_progressBar, "field 'preparing_progressBar'", ProgressBar.class);
    view = Utils.findRequiredView(source, R.id.download_data_button, "method 'onClickDownloadData'");
    view7f0a00c7 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDownloadData(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.erase_data_button, "method 'onClickEraseData'");
    view7f0a00ec = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickEraseData(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.begin_download_button, "method 'onClickBeginDownload'");
    view7f0a0067 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBeginDownload(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.cancel_download_button, "method 'onClickCancelDownload'");
    view7f0a007a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickCancelDownload(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.return_button, "method 'onClickReturn'");
    view7f0a01db = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickReturn(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.delete_receiver_button, "method 'onClickDeleteReceiver'");
    view7f0a00af = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickDeleteReceiver(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.return_screen_button, "method 'onClickReturnScreen'");
    view7f0a01dc = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickReturnScreen(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    GetDataActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.title_toolbar = null;
    target.state_view = null;
    target.memory_used_percent_textView = null;
    target.memory_used_progressBar = null;
    target.bytes_stored_textView = null;
    target.menu_manage_receiver_linearLayout = null;
    target.begin_download_linearLayout = null;
    target.downloading_file_linearLayout = null;
    target.download_complete_linearLayout = null;
    target.delete_linearLayout = null;
    target.deleting_linearLayout = null;
    target.deletion_complete_linearLayout = null;
    target.downloading_data_imageView = null;
    target.downloading_data_textView = null;
    target.downloading_progressBar = null;
    target.processing_data_imageView = null;
    target.processing_data_textView = null;
    target.processing_progressBar = null;
    target.preparing_file_imageView = null;
    target.preparing_file_textView = null;
    target.preparing_progressBar = null;

    view7f0a00c7.setOnClickListener(null);
    view7f0a00c7 = null;
    view7f0a00ec.setOnClickListener(null);
    view7f0a00ec = null;
    view7f0a0067.setOnClickListener(null);
    view7f0a0067 = null;
    view7f0a007a.setOnClickListener(null);
    view7f0a007a = null;
    view7f0a01db.setOnClickListener(null);
    view7f0a01db = null;
    view7f0a00af.setOnClickListener(null);
    view7f0a00af = null;
    view7f0a01dc.setOnClickListener(null);
    view7f0a01dc = null;
  }
}
