package com.android.base;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Looper;
import android.widget.ImageView;

import androidx.multidex.MultiDex;

import com.android.base.data.DataConfig;
import com.android.base.util.GlideUtil;
import com.android.base.util.KVUtil;
import com.android.base.util.crash.AppCrashHandler;
import com.android.base.version.AppVersionChecker;
import com.android.util.LContext;
import com.blankj.utilcode.util.ProcessUtils;
import com.fastapp.BuildConfig;
import com.fastapp.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.mmkv.MMKV;
import com.tencent.mmkv.MMKVLogLevel;
import com.umeng.library.Umeng;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

import net.medlinker.android.splash.SplashUtil;

import java.util.List;
import java.util.Locale;

import component.update.AppDownloadClient;
import component.update.AppVersionConfiguration;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * @author 张全
 */
public class App extends Application {
    private static App mApp;
    public static boolean devEnv = false; //开发环境
    public static boolean coldStart; //冷启动
    public static Thread.UncaughtExceptionHandler mainExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void init() {
        coldStart = true;
        mApp = this;
        MMKV.initialize(this, MMKVLogLevel.LevelDebug); //微信MMKV
        initAppInfo();

        if (!ProcessUtils.isMainProcess()) {
            return;
        }
        mainExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // 开启服务、任务
        initBackgroundTasks();
        //相册选择
        initAlbum();
        // 检测版本
        AppVersionConfiguration configuration = new AppVersionConfiguration.Builder(this)
                .appIcon(R.mipmap.ic_launcher)
                .isDebug(BuildConfig.DEBUG)
                .setVersionChecker(new AppVersionChecker())
                .build();
        AppDownloadClient.getInstance().init(configuration);

        //是否同意隐私政策
        boolean privacyGranted = SplashUtil.isPrivacyGranted();
        if (privacyGranted) {
            initAfterPrivacy();
        }
        initAsync();
        //其他初始化
        initOthers();
        //拦截异常
        handleRxJavaException();
    }

    /**
     * 初始化配置
     */
    private void initAppInfo() {
        PackageManager pm = getPackageManager();
        int versionCode = 0;
        String versionName = null;
        String appName = null;
        String channel = null;
        try {
            ApplicationInfo info = pm.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            PackageInfo packInfo = pm.getPackageInfo(getPackageName(), 0);
            appName = pm.getApplicationLabel(info).toString();
            if (packInfo != null) {
                versionName = packInfo.versionName;
                versionCode = packInfo.versionCode;
            }
            if (null != info.metaData) {
                channel = info.metaData.getString("UMENG_CHANNEL");
                devEnv = info.metaData.getBoolean("env", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        LContext.init(this, BuildConfig.DEBUG);
        LContext.appIcon = R.mipmap.ic_launcher;
        LContext.appName = appName;
        LContext.pkgName = getPackageName();
        LContext.versionCode = versionCode;
        LContext.versionName = versionName;
        LContext.channel = channel;

        DataConfig.DEBUG = BuildConfig.DEBUG;

        DataConfig.API_HOST = BuildConfig.API_HOST;
        DataConfig.H5_HOST = BuildConfig.H5_HOST;
        DataConfig.LOG_HOST = BuildConfig.LOG_HOST;
    }

    private void initAsync() {
        new Thread(() -> {
            initAlbum();

        }).start();
    }

    /**
     * 初始化图片选择框架
     */
    private void initAlbum() {
        Album.initialize(AlbumConfig.newBuilder(this)
                .setLocale(Locale.CHINA)
                .setAlbumLoader(new AlbumLoader() {
                    @Override
                    public void load(ImageView imageView, AlbumFile albumFile) {
                        GlideUtil.loadLocalPic(imageView, albumFile.getPath());
                    }

                    @Override
                    public void load(ImageView imageView, String url) {
                        GlideUtil.loadPic(imageView, url);
                    }
                }).build());

    }

    /**
     * 初始化后台定时任务
     */
    private void initBackgroundTasks() {
        //        ScheduleTaskManager taskManager = ScheduleTaskManager.getInstance();
        //        // 更新任务
        //        taskManager.addTask(new UpdateTask());
        //
        //       //开启后台服务
        //        CoreService.start(this);


    }

    /**
     * 其他初始化
     */
    private void initOthers() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> {
            ClassicsHeader classicsHeader = new ClassicsHeader(context);
            classicsHeader.setBackgroundColor(Color.TRANSPARENT);
            return classicsHeader;
        });
    }

    private void handleRxJavaException() {
        //处理RxJava  没有设置onError回调  io.reactivex.exceptions.OnErrorNotImplementedException
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable instanceof CompositeException) { //打印异常
                    CompositeException exception = (CompositeException) throwable;
                    List<Throwable> exceptions = exception.getExceptions();
                    for (Throwable item : exceptions) {
                        item.printStackTrace();
                    }
                } else {
                    throwable.printStackTrace();
                }
            }
        });
        AppCrashHandler.install(new AppCrashHandler.ExceptionHandler() {
            @Override
            public void handlerException(Thread thread, Throwable throwable) {
                if (thread == Looper.getMainLooper().getThread()) { //主线程
                    System.out.println("主线程");
                } else { //子线程
                    System.out.println("子线程");
                    //如果有第三方需要处理exception
//                    mainExceptionHandler.uncaughtException(thread,throwable);
                }
                throwable.printStackTrace();
            }
        });
    }

    public static void initAfterPrivacy() {
        // 初始化统计
        Umeng.initAnalytics(mApp, BuildConfig.DEBUG);
    }


    //===================================================================================================================================================
    public static int getDev() {
        return KVUtil.getInt("dev_env", BuildConfig.DEBUG ? 3 : 0);
    }

    /**
     * @param dev 0正式 1测试  2开发  3预发
     */
    public static void setDev(int dev) {
        KVUtil.set("dev_env", dev);
    }


}
