package com.android.base.util.filedownloader;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fastapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import common.widget.dialog.DialogView;

/**
 * 文件下载弹框
 *
 * @author 张全
 */
public class FileDownloadDialogView extends DialogView {
    private String title;
    private List<String> downloadSources;
    private List<File> files = new ArrayList<>();
    private FileDownloader fileDownloader;
    private TextView item_label2;
    private TextView btn;
    private ProgressBar progressBar;
    private int total;
    private int progress;


    public FileDownloadDialogView(Context ctx, List<String> downloadSources, String title) {
        super(ctx);
        this.fileDownloader = new FileDownloader(ctx);
        this.downloadSources = downloadSources;
        this.title = title;
        this.total = downloadSources.size();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.file_download_dialog;
    }

    @Override
    public void initView(View view) {

        item_label2 = findViewById(R.id.item_label2);
        progressBar = findViewById(R.id.progressBar);

        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(btn.getText().toString(), "下载失败")) { //重新下载
                    startDownload();
                } else {
                    fileDownloader.stop();
                    dismiss();
                }
            }
        });

        if (!TextUtils.isEmpty(title) && !"sharegoods".equals(title)) {
            TextView textView = findViewById(R.id.item_label1);
            textView.setText(title);
        }

        startDownload();
    }

    private void startDownload() {
        progress = 0;
        files.clear();
        btn.setText("取消下载");

        progressBar.setMax(total);
        progressBar.setProgress(progress);
        item_label2.setText("已下载 " + progress + "/" + total);

        fileDownloader.downloadFile(downloadSources, new FileDownloader.DownloadCallback() {
            @Override
            public void success(File file, String url) {
                files.add(file);
                updateUI(file, url);
            }

            @Override
            public void fail(String url) {
                updateUI(null, url);
            }
        });
    }

    private void updateUI(File file, String url) {
        progress++;
        progressBar.setProgress(progress);
        item_label2.setText("已下载 " + progress + "/" + total);
        if (progress == total) {
            if (files.isEmpty()) {
                btn.setText("下载失败");
            } else {
                btn.setText("下载完成");
            }
        }
    }
}
