package component.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppDownloadClient {
    private AppVersionDownloader versionDownloader;
    private AppVersionConfiguration configuration;
    private VersionUpdateListener mUpdateListener;
    private static AppDownloadClient instance;

    private AppDownloadClient() {
        versionDownloader = new AppVersionDownloader();
    }

    public static AppDownloadClient getInstance() {
        if (null == instance) {
            synchronized (AppDownloadClient.class) {
                if (null == instance) {
                    instance = new AppDownloadClient();
                }
            }
        }
        return instance;
    }

    public void init(AppVersionConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration can not be null");
        }
        this.configuration = configuration;
    }

    public static Context getContext() {
        return getConfiguration().ctx;
    }

    public static AppVersionConfiguration getConfiguration() {
        AppVersionConfiguration configuration = getInstance().configuration;
        if (null == configuration) {
            throw new NullPointerException("configuration has not been Initialized yet");
        }
        return configuration;
    }

    public static String getAuthority() {
        return AppDownloadClient.getContext().getPackageName() + ".appupdate.provider";
    }

    // ---------------------------------------------
    public static void startDownloadService(Context context, String action) {
        Intent service = new Intent(context, AppDownloadService.class);
        service.setAction(action);
        context.startService(service);
    }

    /**
     * 检测升级
     */
    public static void doCheckVersion(VersionUpdateListener updateListener) {
        if (AppDownloadUtil.isNetworkAvailable(getContext())) {
            getInstance().mUpdateListener = updateListener;
            VersionUpdateListener versionUpdateListener = new VersionUpdateListener() {

                @Override
                public void onNewVersionReturned(final AppVersion appVersion) {
                    if (appVersion == null) {
                        VersionUpdateListener mUpdateListener = getInstance().mUpdateListener;
                        if (null != mUpdateListener) {
                            mUpdateListener.onNewVersionReturned(appVersion);
                        }
                        return;
                    }
                    AppVersion dbVersion = getFromDB();
                    if (null == dbVersion
                            || !appVersion.versionName.equals(dbVersion.versionName)) {
                        // 数据库无缓存版本，或者缓存的版本和最新的版本不一致
                        if (getInstance().isDownloading()) {
                            // 正在下载旧版本
                            getInstance().versionDownloader.stop();
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    delUpdateFile();
                                    getContext().stopService(
                                            new Intent(getContext(),
                                                    AppDownloadService.class));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    save2DB(null);
                                }
                                // 保存新版本
                                save2DB(appVersion);
                                // 回调给调用者
                                VersionUpdateListener mUpdateListener = getInstance().mUpdateListener;
                                if (null != mUpdateListener) {
                                    mUpdateListener.onNewVersionReturned(appVersion);
                                }
                            }
                        }, 200);
                    } else {
                        // 回调给调用者
                        VersionUpdateListener mUpdateListener = getInstance().mUpdateListener;
                        if (null != mUpdateListener) {
                            mUpdateListener.onNewVersionReturned(appVersion);
                        }
                    }
                }

                @Override
                public void onNoVersionReturned() {
                    save2DB(null);
                    delUpdateFile();
                    VersionUpdateListener mUpdateListener = getInstance().mUpdateListener;
                    if (null != mUpdateListener) {
                        mUpdateListener.onNoVersionReturned();
                    }
                }

                @Override
                public void fail() {
                    VersionUpdateListener mUpdateListener = getInstance().mUpdateListener;
                    if (null != mUpdateListener) {
                        mUpdateListener.fail();
                    }
                }

            };
            getConfiguration().versionChecker.doVersionCheck(versionUpdateListener);

        }
    }

    /**
     * 停止版本检测
     */
    public static void stopCheckVersion() {
        getInstance().mUpdateListener = null;
        getConfiguration().versionChecker.stopVersionCheck();
    }

    /**
     * 用户确认下载
     */
    public static void startDownload() {
        if (getInstance().isDownloading()) {
            AppDownloadUtil.showToast("后台更新中...");
        } else if (hasNewVersionDownloaded()) {
            // 已下载好 直接安装
            installAPK();
        } else {
            AppDownloadUtil.showToast("后台更新中...");
            Intent service = new Intent(getContext(),
                    AppDownloadService.class);
            service.setAction(AppDownloadService.ACTION_DOWNLOAD_START);
            getContext().startService(service);
        }
    }

    /**
     * 获取更新提示信息
     *
     * @param version
     * @return
     */
    public static String getUpdateDes(AppVersion version) {
        StringBuilder content = new StringBuilder();
        content.append("最新版本: " + version.versionName).append("\n");
//		content.append(
//				"新版本大小: "
//						+ Formatter.formatFileSize(getContext(),
//								Long.valueOf(version.totalSize))).append("\n");
//		content.append("\n");
        content.append("更新内容").append("\n");
        content.append(version.desc);
        return content.toString();
    }

    /**
     * 保存版本信息
     *
     * @param version
     */
    public static void save2DB(AppVersion version) {
        AppVersionDB.saveVersion(version);
    }

    /**
     * 获取缓存的版本信息
     *
     * @return
     */
    public static AppVersion getFromDB() {
        return AppVersionDB.getVersion();
    }

    // #######################版本下载升级 START#################
    private final static String FILE = "update.apk";

    public void downloadFile(AppVersion version,
                             final AppDownloadCallBack callBack) {
        if (null == versionDownloader)
            versionDownloader = new AppVersionDownloader();

        if (versionDownloader.isDownloading()) {
            versionDownloader.addCallBack(callBack);
        } else {
            versionDownloader.addCallBack(callBack);
            versionDownloader.start(version);
        }
    }

    public void removeCallBack(AppDownloadCallBack callBack) {
        if (versionDownloader != null) {
            versionDownloader.removeCallback(callBack);
        }
    }

    /**
     * 是否正在下载
     *
     * @return
     */
    public boolean isDownloading() {
        if (null != versionDownloader) {
            return versionDownloader.isDownloading();
        }
        return false;
    }

    /**
     * 是否有新版本
     *
     * @return
     */
    public static boolean hasNewVersion() {
        AppVersion dbVersion = getFromDB();
        if (null == dbVersion) {
            delUpdateFile();
            return false;
        }
        if (dbVersion.versionCode > 0) {
            // 根据VersionCode来判断
            boolean hasNew = dbVersion.versionCode > getConfiguration().versionCode;
            if (hasNew) {
                return true;
            }
        } else {
            // 根据VersionName来判断 ，比如友盟升级
            String[] savedNums = dbVersion.versionName.split("\\.");
            String[] localNums = getConfiguration().versionName.split("\\.");
            List<String> savedNumsList = Arrays.asList(savedNums);
            List<String> localNumsList = Arrays.asList(localNums);

            savedNumsList = new ArrayList<String>(savedNumsList);
            localNumsList = new ArrayList<String>(localNumsList);

            int ns = savedNumsList.size();
            int ls = localNumsList.size();
            if (ns > ls) {
                for (int i = ls; i < ns; i++) {
                    localNumsList.add("0");
                }
            } else if (ns < ls) {
                for (int i = ns; i < ls; i++) {
                    savedNumsList.add("0");
                }
            }

            int n = savedNumsList.size();
            for (int i = 0; i < n; i++) {
                int si = Integer.valueOf(savedNumsList.get(i));
                int li = Integer.valueOf(localNumsList.get(i));
                if (si > li) {
                    return true;
                }
            }
        }
        AppVersionDB.saveVersion(null);
        delUpdateFile();
        return false;
    }

    /**
     * 检测升级apk是否已下载完成
     */
    public static boolean hasNewVersionDownloaded() {
        // 检测下载文件
        File updateFile = getUpdateFile();
        if (null == updateFile
                || !updateFile.exists()
                || updateFile.length() == 0) {
            return false;
        }

        // 检测是否有新 版本
        if (!hasNewVersion()) {
            return false;
        }
        // 检测保存的新版本
        AppVersion version = getFromDB();
        if (null != version && version.totalSize == updateFile.length()) {
            return true;
        }
        return false;
    }

    /**
     * 安装APK
     */
    public static boolean installAPK() {
        File updateFile = getUpdateFile();
        if (null != updateFile && updateFile.exists()
                && updateFile.length() > 0) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    String authority = getAuthority();
                    uri = FileProvider.getUriForFile(getContext(), authority, updateFile);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(updateFile);
                }
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                getContext().startActivity(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                // 删除该版本信息，重新下载
                delUpdateFile();
                save2DB(null);
            }
        }
        return false;
    }

    public static File getUpdateFile() {
        // 7.0以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File imagePath = new File(getContext().getFilesDir(), "files");
            if (!imagePath.exists()) imagePath.mkdirs();
            File newFile = new File(imagePath, FILE);
            return newFile;
        }
        //SD卡
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {//如果已经挂载
            File dir = new File(Environment.getExternalStorageDirectory(), "download");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return new File(dir, FILE);
        }
        //file目录
        return new File(getContext().getCacheDir(), FILE);
    }

    /**
     * 删除下载包
     */
    public static void delUpdateFile() {
        File updateFile = getUpdateFile();
        if (null != updateFile && updateFile.exists()) {
            updateFile.delete();
        }
    }
    // #######################版本下载升级 END#################
}
