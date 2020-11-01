package com.android.base.util.pay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.android.util.LContext;
import com.android.util.ext.ToastUtil;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.List;

public class WXHelper {
    private IWXAPI api;

    public WXHelper(Context ctx) {
        api = WXAPIFactory.createWXAPI(ctx, null);
    }

    /**
     * 检测是否安装了微信
     *
     * @return
     */
    public static boolean isWeiXinInstalled() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = LContext.getContext().getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
            String packageName = applicationInfo.packageName;
            if (packageName.startsWith("com.tencent.mm")) {
                return true;
            }
        }
        return false;
    }

    public boolean checkEnvirment() {
        // 检测是否安装了微信客户端
        if (!WXHelper.isWeiXinInstalled()) {
            unregisterApp();
            ToastUtil.show("未安装微信");
            return false;
        }
        // 检查微信版本是否支持支付
        boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        if (!isPaySupported) {
            ToastUtil.show("您的微信版本暂不支持支付");
            unregisterApp();
            return false;
        }
        return true;
    }

    public void unregisterApp() {
        try {
            api.unregisterApp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pay(OrderPayResponse orderPayEntity) {
        if (!checkEnvirment()) {
            return;
        }
        api.registerApp(orderPayEntity.appid);
        PayReq req = new PayReq();
        req.appId = orderPayEntity.appid;
        req.partnerId = orderPayEntity.partnerid;
        req.prepayId = orderPayEntity.prepayid;
        req.nonceStr = orderPayEntity.noncestr;
        req.timeStamp = orderPayEntity.timestamp;
        req.packageValue = orderPayEntity.packageStr;
        req.sign = orderPayEntity.sign;
        api.sendReq(req);
    }

}