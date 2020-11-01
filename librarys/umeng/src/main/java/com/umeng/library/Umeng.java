package com.umeng.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.umcrash.UMCrash;

/**
 * @author 张全
 */
public class Umeng {
    public static IWXAPI iwxapi;

    public static void initAnalytics(Context ctx, boolean debugModel) {
        String umengChannal = null;
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
            if (null != info.metaData) {
                umengChannal = info.metaData.getString("UMENG_CHANNEL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String appKey = ctx.getString(R.string.umeng_appkey);
        UMConfigure.init(ctx, appKey, umengChannal, UMConfigure.DEVICE_TYPE_PHONE, null);
//        UMConfigure.init(ctx, UMConfigure.DEVICE_TYPE_PHONE, null); //配置在manifest.xml中就可以不再传key和channel
        UMConfigure.setLogEnabled(debugModel);
        // Android 4.0及以上版本支持Activity生命周期的自动监控 AUTO模式下SDK会自动调用MobclickAgent.onResume/MobclickAgent.onPause接口，用户无须手动调用这两个接口
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        //上报异常
        MobclickAgent.setCatchUncaughtExceptions(true);
        UMCrash.init(ctx, appKey, umengChannal);
        UMCrash.setDebug(debugModel);


        //分享配置
        PlatformConfig.setWeixin(ctx.getString(R.string.wx_appid), ctx.getString(R.string.wx_appkey));
        PlatformConfig.setSinaWeibo(ctx.getString(R.string.sina_appid), ctx.getString(R.string.sina_appkey), ctx.getString(R.string.sina_url));
        PlatformConfig.setQQZone(ctx.getString(R.string.qq_appid), ctx.getString(R.string.qq_appkey));

        // 将应用的appId注册到微信
        iwxapi = WXAPIFactory.createWXAPI(ctx, ctx.getString(R.string.wx_appid), true);
        iwxapi.registerApp(ctx.getString(R.string.wx_appid));
        //建议动态监听微信启动广播进行注册到微信
        ctx.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                iwxapi.registerApp(context.getString(R.string.wx_appid));
            }
        }, new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP));

    }

}
