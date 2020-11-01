package com.android.base.version;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.util.ext.ToastUtil;
import com.android.util.log.LogUtil;
import com.android.base.data.RestClient;
import com.fastapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import common.widget.dialog.DialogView;
import component.update.AppDownloadClient;
import component.update.AppVersion;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 强制升級框
 *
 * @author 张全
 */
public class UpdateDialogView extends DialogView {
    private static final String TAG = "VersionUpdate";
    AppVersion appVersion;
    ProgressBar progressBar;
    TextView tv_progress;
    View reload;
    public static long fileSize = -1;


    public UpdateDialogView(Context ctx, AppVersion appVersion) {
        super(ctx);
        this.appVersion = appVersion;
    }

    @Override
    protected void initView(View view) {
        progressBar = findViewById(R.id.pb);
        tv_progress = findViewById(R.id.progress);
        reload = findViewById(R.id.reload);
        reload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });
        startDownload();
    }

    private void startDownload() {
        reload.setVisibility(View.GONE);
        try {
            File updateFile = AppDownloadClient.getUpdateFile();
            downLoadFile(appVersion.downloadUrl, updateFile);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show("下载失败");
            dismiss();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.update_dialog;
    }


    private class DownloadCallback extends Handler {
        private static final int msg_success = 1;
        private static final int msg_fail = 2;
        private static final int msg_progress = 3;

        public DownloadCallback() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case msg_progress:
                    int progress = (int) msg.obj;
                    progressBar.setProgress(progress);
                    tv_progress.setText(progress + "%");
                    break;
                case msg_success:
                    LogUtil.e(TAG, "下载成功");
                    fileSize = AppDownloadClient.getUpdateFile().length();
                    AppDownloadClient.installAPK();
                    dismiss();
                    break;
                case msg_fail:
                    LogUtil.e(TAG, "下载失败");
                    ToastUtil.show("下载失败,请重试");
                    reload.setVisibility(View.VISIBLE);
                    break;
            }
        }

        public void sendMsg(int what) {
            sendEmptyMessage(what);
        }

        public void updateProgress(int progress) {
            sendMessage(obtainMessage(msg_progress, progress));
        }
    }

    /**
     * 下载文件
     */
    private void downLoadFile(String url, File file) {

        final Request request = new Request.Builder().url(url).build();
        final Call call =  RestClient.getDownloadClient().newCall(request);
        DownloadCallback downloadCallback = new DownloadCallback();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                downloadCallback.sendMsg(DownloadCallback.msg_fail);
            }

            @Override
            public void onResponse(Call call, Response response) {
                int lastProgress = 0;
                if (response.isSuccessful()) {
                    InputStream is = null;
                    byte[] buf = new byte[1024];
                    int len;
                    FileOutputStream fos = null;
                    try {
                        long total = response.body().contentLength();
                        LogUtil.e(TAG, "total------>" + total);
                        long current = 0;
                        is = response.body().byteStream();
                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1) {
                            current += len;
                            fos.write(buf, 0, len);
                            LogUtil.e(TAG, "current------>" + current);
                            if (total == -1) {
                                downloadCallback.updateProgress(-1);
                            } else {
                                int progress = (int) (current * 1.0f / total * 100);
                                progress = progress >= 100 ? 99 : progress;
                                if (lastProgress != progress) {
                                    downloadCallback.updateProgress(progress);
                                }
                                lastProgress = progress;
                            }
                        }
                        fos.flush();
                        downloadCallback.sendMsg(DownloadCallback.msg_success);
                    } catch (Exception e) {
                        LogUtil.e(TAG, e.toString());
                        downloadCallback.sendMsg(DownloadCallback.msg_fail);
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (Exception e) {
                            LogUtil.e(TAG, e.toString());
                        }
                    }
                } else {
                    downloadCallback.sendMsg(DownloadCallback.msg_fail);
                }
            }
        });
    }
}
