package component.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * APP下载服务
 *
 * @author 张全
 */
public class AppDownloadService extends Service {
    private Notification.Builder mBuilder;
    private NotificationManager mNotifyManager;
    // 通知id
    private int notifyId;
    // 开始下载
    public static final String ACTION_DOWNLOAD_START = "ACTION_DOWNLOAD_START";
    // 清除通知
    public static final String ACTION_CLEAR_NOTIFICATION = "ACTION_CLEAR_NOTIFICATION";
    // 检测版本升级
    public static final String ACTION_CHECK_VERSION = "ACTION_CHECK_VERSION";

    @Override
    public void onCreate() {
        super.onCreate();
        mBuilder = new Notification.Builder(this);
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (null == intent || TextUtils.isEmpty(action))
            return Service.START_NOT_STICKY;

        if (ACTION_DOWNLOAD_START.equals(action)) {
            // 下载
            startDownload();
        } else if (ACTION_CHECK_VERSION.equals(action)) {
            AppDownloadClient.doCheckVersion(null);
        } else if (ACTION_CLEAR_NOTIFICATION.equals(action)) {
            // 清除通知
            mNotifyManager.cancelAll();
            stopSelf();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotifyManager.cancelAll();
    }

    private boolean isDownloading;

    private void startDownload() {
        if (!AppDownloadUtil.isNetworkAvailable(this)) {
            mNotifyManager.cancelAll();
            notifyStart();
            notifyError();
            return;
        }
        AppVersion version = AppDownloadClient.getFromDB();
        if (null == version) {
            mNotifyManager.cancelAll();
            stopSelf();
            return;
        }
        if (isDownloading) {
            return;
        }
        notifyId = version.downloadUrl.hashCode();
        isDownloading = true;
        AppDownloadClient.getInstance().downloadFile(version,
                new AppDownloadCallBack() {

                    @Override
                    public void downloadStart() {
                        notifyStart();
                    }

                    @Override
                    public void downloadSuccess() {
                        isDownloading = false;
                        notifyFinish();
                        Toast.makeText(getBaseContext(), "下载成功,正在准备安装...",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void downloadProgress(long downloadSize,
                                                 long totalSize, int percent) {
                        notifyProgress(percent);
                    }

                    @Override
                    public void downloadError(String errorMsg) {
                        isDownloading = false;
                        notifyError();
                        Toast.makeText(getBaseContext(), "下载失败,请点击通知栏重试...",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

    // ###########################

    private void notifyStart() {
        AppVersionConfiguration configuration = AppDownloadClient.getConfiguration();
        mBuilder.setContentTitle(configuration.appName).setContentText("正在下载新版本")
                .setSmallIcon(configuration.appIcon);
    }

    /**
     * 刷新下载进度
     *
     * @param url
     * @param progress
     * @param total
     */
    private int showProgress;

    private void notifyProgress(int progress) {
        if (showProgress == progress) {
            return;
        }
        showProgress = progress;
        // 部分手机Notification没有 contentIntent会报错
        Intent localIntent = new Intent(this, getClass());
        PendingIntent localPendingIntent = PendingIntent.getService(this, 0,
                localIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentText("正在下载新版本 " + progress + "%");
        mBuilder.setProgress(100, progress, false);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        notification.contentIntent = localPendingIntent;
        mNotifyManager.notify(notifyId, notification);
    }

    /**
     * 下载完毕
     */
    private void notifyFinish() {
        boolean successful = AppDownloadClient.installAPK();
        if (!successful) {
            mNotifyManager.cancel(notifyId);
            return;
        }

        File updateFile = AppDownloadClient.getUpdateFile();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = AppDownloadClient.getAuthority();
            uri = FileProvider.getUriForFile(AppDownloadClient.getContext(), authority, updateFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(updateFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        Notification notification = mBuilder.setContentText("下载完毕点击安装")
                .setProgress(0, 0, false).build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.contentIntent = PendingIntent.getActivity(this, 0, intent,
                0);
        mNotifyManager.notify(notifyId, notification);

    }

    /**
     * 下载错误
     */
    private void notifyError() {

        Intent localIntent = new Intent(this, getClass());
        localIntent.setAction(ACTION_DOWNLOAD_START);

        Notification notification = mBuilder.setContentText("下载失败,点击重试")
                .setProgress(0, 0, false).build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.contentIntent = PendingIntent.getService(this, 0,
                localIntent, 0);
        mNotifyManager.notify(notifyId, notification);

    }
}
