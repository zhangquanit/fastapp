package com.android.util.ext;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.android.util.LContext;
import com.blankj.utilcode.util.SizeUtils;

import java.lang.reflect.Field;

import common.util.R;

public class ToastUtil {

    public static void show(int resId) {
        show(LContext.getString(resId));
    }

    public static void show(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }

        if (mToast == null) {
            Context context = LContext.getContext();
//			mToast = Toast.makeText(LContext.getContext(), text,
//					Toast.LENGTH_SHORT);
            int d14 = SizeUtils.dp2px(14);
            int d8 = SizeUtils.dp2px(8);
            mToast = new Toast(context);
            TextView textView = new TextView(context);
            textView.setText(text);
            textView.setTextSize(15);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundResource(R.drawable.toast_bg);
            textView.setPadding(d14, d8, d14, d8);
            mToast.setView(textView);
            mToast.setGravity(Gravity.CENTER, 0, 0);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        hook(mToast);
        mToast.show();
    }

    public static void showLong(int resId) {
        showLong(LContext.getString(resId));
    }

    public static void showLong(CharSequence text) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }

        if (mToast == null) {
            Context context = LContext.getContext();
//            mToast = Toast.makeText(context, text,
//					Toast.LENGTH_LONG);
            int d14 = SizeUtils.dp2px(14);
            int d8 = SizeUtils.dp2px(8);
            mToast = new Toast(context);
            TextView textView = new TextView(context);
            textView.setText(text);
            textView.setTextSize(15);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundResource(R.drawable.toast_bg);
            textView.setPadding(d14, d8, d14, d8);
            mToast.setView(textView);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        hook(mToast);
        mToast.show();
    }

    public static void cancel() {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }

    //-----------------------------------------------------------------------------------------
    private static Toast mToast;
    private static Field sField_TN;
    private static Field sField_TN_Handler;

    static {
        if (Build.VERSION.SDK_INT == 25) {
            try {
                sField_TN = Toast.class.getDeclaredField("mTN");
                sField_TN.setAccessible(true);
                sField_TN_Handler = sField_TN.getType().getDeclaredField("mHandler");
                sField_TN_Handler.setAccessible(true);
            } catch (Exception e) {
            }
        }
    }

    private static void hook(Toast toast) {
        if (Build.VERSION.SDK_INT == 25) {
            try {
                Object tn = sField_TN.get(toast);
                Handler preHandler = (Handler) sField_TN_Handler.get(tn);
                sField_TN_Handler.set(tn, new SafelyHandlerWarpper(preHandler));
            } catch (Exception e) {
            }
        }
    }

    /**
     * 解决7.1.1 7.1.2
     * 参考：https://cloud.tencent.com/developer/article/1034223
     * android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@5204ea5 is not valid; is your activity running?
     * 直接在 Toast.show 函数外增加 try-catch 是没有意义的。因为 Toast.show 实际上只是发了一条命令给 NotificationManager 服务。
     * 真正的显示需要等 NotificationManager 通知我们的 TN 对象 show 的时候才能触发。NotificationManager 通知给 TN 对象的消息，都会被 TN.mHandler 这个内部对象进行处理
     */
    private static class SafelyHandlerWarpper extends Handler {

        private Handler impl;

        public SafelyHandlerWarpper(Handler impl) {
            this.impl = impl;
        }

        @Override
        public void dispatchMessage(Message msg) {
            try {
                super.dispatchMessage(msg);
            } catch (Exception e) {
            }
        }

        @Override
        public void handleMessage(Message msg) {
            impl.handleMessage(msg);//需要委托给原Handler执行
        }
    }
}
