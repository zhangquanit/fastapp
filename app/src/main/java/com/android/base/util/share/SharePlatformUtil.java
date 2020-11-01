package com.android.base.util.share;

import android.content.ComponentName;
import android.content.Context;

import com.android.util.ext.ToastUtil;
import com.android.base.util.AppInstallUtil;
import com.umeng.socialize.bean.SHARE_MEDIA;

/**
 * @author 张全
 */
public class SharePlatformUtil {

    public static boolean checkExist(Context ctx, SHARE_MEDIA shareMedia) {
        switch (shareMedia) {
            case QQ:
                if (!AppInstallUtil.isAppInstalled(ctx, "com.tencent.mobileqq")) {
                    ToastUtil.show("请先安装QQ客户端");
                    return false;
                }
                break;
            case WEIXIN:
                if (!AppInstallUtil.INSTANCE.isAppInstalled(ctx, "com.tencent.mm")) {
                    ToastUtil.show("请先安装微信客户端");
                    return false;
                }
                break;
            case SINA:
                if (!AppInstallUtil.isAppInstalled(ctx, "com.sina.weibo")) {
                    ToastUtil.show("请先安装微博客户端");
                    return false;
                }
                break;
        }
        return true;
    }

    public static ComponentName getComponent(SHARE_MEDIA shareMedia) {
        ComponentName comp;
        switch (shareMedia) {
            case WEIXIN:
                comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
                break;
            case WEIXIN_CIRCLE:
                comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                break;
            case SINA:
                comp = new ComponentName("com.sina.weibo", "com.sina.weibo.composerinde.ComposerDispatchActivity");
                break;
            default:
                comp = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
        }
        return comp;
    }
}
