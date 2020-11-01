package com.android.base.version;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.android.util.ext.ToastUtil;
import com.android.util.log.LogUtil;
import com.android.base.data.RestClient;
import com.fastapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import component.update.AppDownloadClient;
import component.update.AppVersion;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * App后台下载服务
 *
 * @author 张全
 */
public class UpdateService extends Service {
    private static final String TAG = "VersionUpdate";
    private static final String APP_VERSION = "APP_VERSION";
    private boolean isDownload;

    public static void update(Context ctx, AppVersion appVersion) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.putExtra(APP_VERSION, appVersion);
        ctx.startService(intent);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent || !intent.hasExtra(APP_VERSION))
            return super.onStartCommand(intent, flags, startId);

        ToastUtil.show("后台下载中,请稍候..");

        if (isDownload) return super.onStartCommand(intent, flags, startId);
        try {
            AppVersion appVersion = (AppVersion) intent.getSerializableExtra(APP_VERSION);
            File updateFile = AppDownloadClient.getUpdateFile();
            downLoadFile(appVersion.downloadUrl, updateFile);
        } catch (Exception e) {
            e.printStackTrace();
            isDownload = false;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private static class DownloadCallback extends Handler {
        private static final int msg_success = 1;
        private static final int msg_fail = 2;
        private static final int msg_progress = 3;
        private int notificationId;
        final String channelId = "1";
        private NotificationManager notificationManager;
        private NotificationCompat.Builder mBuilder;
        private Context ctx;

        public DownloadCallback(Context ctx, String url) {
            super(Looper.getMainLooper());
            this.ctx = ctx;
            notificationId = url.hashCode();
            notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel1", NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setVibrationPattern(new long[]{0});
                channel.setSound(null, null);
                channel.setLightColor(Color.RED); //小红点颜色
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                mBuilder = new NotificationCompat.Builder(ctx, channelId);
                notificationManager.createNotificationChannel(channel);
            } else {
                mBuilder = new NotificationCompat.Builder(ctx);
                mBuilder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                        .setVibrate(new long[]{0})
                        .setSound(null);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case msg_progress:
                    int progress = (int) msg.obj;
                    sendNotification(progress);
                    break;
                case msg_success:
                    LogUtil.e(TAG, "下载成功");
                    notificationManager.cancel(notificationId);
                    AppDownloadClient.installAPK();
                    break;
                case msg_fail:
                    LogUtil.e(TAG, "下载失败");
                    notificationManager.cancel(notificationId);
                    ToastUtil.show("下载失败,请重试");
                    break;
            }
        }

        public void sendMsg(int what) {
            sendEmptyMessage(what);
        }

        public void updateProgress(int progress) {
            sendMessage(obtainMessage(msg_progress, progress));
        }

        public void sendNotification(int progress) {
            LogUtil.e(TAG, "progress=" + progress);
            if (progress < 100) {
                Intent localIntent = new Intent(ctx, UpdateService.class);
                PendingIntent localPendingIntent = PendingIntent.getService(ctx, 0,
                        localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (progress <= 0) {
                    mBuilder.setContentTitle("星乐桃")
                            .setContentText("版本下载中");
                } else {
                    mBuilder.setContentTitle("版本下载")
                            .setContentText("当前进度：" + progress + "%")
                            .setProgress(100, progress, false);
                }
                mBuilder
                        .setContentIntent(localPendingIntent)
                        .setTicker("版本下载中...")
                        .setWhen(System.currentTimeMillis())
//                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true);


            } else {
                File updateFile = AppDownloadClient.getUpdateFile();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    String authority = AppDownloadClient.getAuthority();
                    uri = FileProvider.getUriForFile(AppDownloadClient.getContext(), authority, updateFile);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(updateFile);
                }
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent,
                        0);
                mBuilder.setContentText("下载完毕点击安装")
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
            }

            Notification notify = mBuilder.build();
            notificationManager.notify(notificationId, notify);
        }
    }


    /**
     * 下载文件
     */
    private void downLoadFile(String url, File file) {
//        url="https://www.9ben.cn/download/andriod/9benfresh.apk";
        isDownload = true;
        final Request request = new Request.Builder().url(url).build();
        final Call call = RestClient.getDownloadClient().newCall(request);
        DownloadCallback downloadCallback = new DownloadCallback(this, url);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isDownload = false;
                downloadCallback.sendMsg(DownloadCallback.msg_fail);
            }

            @Override
            public void onResponse(Call call, Response response) {
                int lastProgress = 0;
                if (response.isSuccessful()) {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
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
                        try {
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                isDownload = false;
            }
        });
    }
}
