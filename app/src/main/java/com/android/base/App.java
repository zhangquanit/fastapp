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

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import component.update.AppDownloadClient;
import component.update.AppVersionConfiguration;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import me.yokeyword.fragmentation.Fragmentation;

/**
 * @author 张全
 */
public class App extends Application {
    public static boolean devEnv = false; //开发环境
    public static boolean coldStart; //冷启动
    public static Thread.UncaughtExceptionHandler mainExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this, MMKVLogLevel.LevelDebug); //微信MMKV
        if (!ProcessUtils.isMainProcess()) {
            return;
        }
        init();//只有主进程才进行相关配置初始化
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void init() {
        coldStart = true;
        mainExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 初始化配置
        initConfigs();
        // 初始化统计
        initAnalytics();
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
        //其他初始化
        initOthers();
        //拦截异常
        handleExceptions();
    }

    /**
     * 初始化配置
     */
    private void initConfigs() {
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
//        if (devEnv) {
//            int dev = getDev();
//            if (dev == 0) { //线上
//                DataConfig.API_HOST = "";
//                DataConfig.H5_HOST = "";
//                DataConfig.LOG_HOST = "";
//            } else if (dev == 1) { //测试
//                DataConfig.API_HOST = "";
//                DataConfig.H5_HOST = "";
//                DataConfig.LOG_HOST = "";
//            } else if (dev == 2) { //开发
//                DataConfig.API_HOST = "";
//                DataConfig.H5_HOST = "";
//                DataConfig.LOG_HOST = "";
//            } else if (dev == 3) { //预发
//                DataConfig.API_HOST = "";
//                DataConfig.H5_HOST = "";
//                DataConfig.LOG_HOST = "";
//            }
//        } else {
//            DataConfig.API_HOST = BuildConfig.API_HOST;
//            DataConfig.H5_HOST = BuildConfig.H5_HOST;
//            DataConfig.LOG_HOST = BuildConfig.LOG_HOST;
//        }

        DataConfig.API_HOST = BuildConfig.API_HOST;
        DataConfig.H5_HOST = BuildConfig.H5_HOST;
        DataConfig.LOG_HOST = BuildConfig.LOG_HOST;
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
     * 初始化友盟统计
     */
    private void initAnalytics() {
        Umeng.initAnalytics(this, BuildConfig.DEBUG);
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
        //ButterKnife
        ButterKnife.setDebug(BuildConfig.DEBUG);
        //Fragmentation
        Fragmentation.builder()
                // 显示悬浮球 ; 其他Mode:SHAKE: 摇一摇唤出   NONE：隐藏
                .stackViewMode(Fragmentation.BUBBLE)
                .debug(BuildConfig.DEBUG)
                .install();

        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> {
            ClassicsHeader classicsHeader = new ClassicsHeader(context);
            classicsHeader.setBackgroundColor(Color.TRANSPARENT);
            return classicsHeader;
        });
    }

    private void handleExceptions() {
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
