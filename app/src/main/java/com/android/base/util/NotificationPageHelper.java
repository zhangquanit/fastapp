package com.android.base.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationManagerCompat;

import com.android.base.widget.AlertDialogView;

import common.widget.dialog.EffectDialogBuilder;

/**
 * 通知权限
 */
public class NotificationPageHelper {
    /**
     * 跳转到权限设置界面
     */
    public static void open(Context context) {

        if (!areNotificationsEnabled(context)) {
            AlertDialogView alertDialogView = new AlertDialogView(context)
                    .setTitle("温馨提示")
                    .setContent("为了您可以正常收到消息推送，建议您开启通知权限！")
                    .setLeftBtn("下次", v -> {
                    }).setRightBtn("好的", v -> {
                        openNotificationSetting(context);
                    });
            new EffectDialogBuilder(context)
                    .setContentView(alertDialogView)
                    .setCancelable(false)
                    .setCancelableOnTouchOutside(false)
                    .show();
        }
    }

    /**
     * 是否开启通知权限
     */
    public static boolean areNotificationsEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * 打开通知权限页面
     */
    public static void openNotificationSetting(Context context) {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            }
            context.startActivity(intent);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            context.startActivity(intent);
        } catch (Exception e) {
            //没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
            e.printStackTrace();
        }

    }


}