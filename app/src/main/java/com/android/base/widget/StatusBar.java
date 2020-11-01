package com.android.base.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 状态栏
 */

public class StatusBar {

    private Window window;
    private boolean lightStatusBar;
    private boolean transparentStatusBar;
    private boolean isSetActionbarPadding;
    private View actionBarView;
    private int statusBarColor = -1;

    private StatusBar(Window window, boolean lightStatusBar, boolean transparentStatusBar, boolean isSetActionbarPadding, View actionBarView, int statusBarColor) {
        this.window = window;
        this.lightStatusBar = lightStatusBar;
        this.transparentStatusBar = transparentStatusBar;
        this.isSetActionbarPadding = isSetActionbarPadding;
        this.actionBarView = actionBarView;
        this.statusBarColor = statusBarColor;
    }

    private static boolean isLessKitkat() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
    }

    private void process() {
        if (isLessKitkat()) return;
        if (lightStatusBar) {   //白底黑色图标
            //魅族、小米
//            if (processPrivateAPI()) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //6.0以上
                int flag = window.getDecorView().getSystemUiVisibility();
                flag |= (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.getDecorView().setSystemUiVisibility(flag);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //5.0-6.0
                int flag = window.getDecorView().getSystemUiVisibility();
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                window.setStatusBarColor(Color.parseColor("#80000000")); //半透明
                window.getDecorView().setSystemUiVisibility(flag);
            } else { //4.4-5.0
                WindowManager.LayoutParams winParams = window.getAttributes();
                final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS; //一条从上到下的半透明渐变层
                winParams.flags |= bits;
                window.setAttributes(winParams);
            }
        } else {    //随顶部背景 白色图标
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //5.0以上
                int flag = window.getDecorView().getSystemUiVisibility();
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                if (statusBarColor != -1) {
                    window.setStatusBarColor(statusBarColor);
                } else {
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
                window.getDecorView().setSystemUiVisibility(flag);

            } else { //4.4-5.0
                WindowManager.LayoutParams winParams = window.getAttributes();
                final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS; //一条从上到下的半透明渐变层
                winParams.flags |= bits;
                window.setAttributes(winParams);
            }
        }


        /**
         setActionbarView+setActionbarPadding 设置setActionbarView的paddingTop为状态栏高度
         或者在xml中设置fitsSystemWindows属性为处，或者设置marginTop
         */
        if (isSetActionbarPadding) {
            processActionBar(actionBarView);
        }
    }

    /**
     * Default status dp = 24 or 25
     * mhdpi = dp * 1
     * hdpi = dp * 1.5
     * xhdpi = dp * 2
     * xxhdpi = dp * 3
     * eg : 1920x1080, xxhdpi, => status/all = 25/640(dp) = 75/1080(px)
     * <p>
     * don't forget toolbar's dp = 48
     *
     * @return px
     */
    @IntRange(from = 0, to = 75)
    public static int getStatusBarOffsetPx(Context context) {
        if (isLessKitkat()) {
            return 0;
        }
        try {
            Context appContext = context.getApplicationContext();
            int result = 0;
            int resourceId =
                    appContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = appContext.getResources().getDimensionPixelSize(resourceId);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 调用私有API处理颜色
     */
    private boolean processPrivateAPI() {
        boolean handled = false;
//        handled = processFlyMe(true);
//        if (handled) return true;

//        handled = processMIUI(true);
//        if (handled) return true;

        try {
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            if (null != darkFlag && null != meizuFlags) {
//                processActionBar(actionBarView);
                handled = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return handled;
    }

    /**
     * 改变小米的状态栏字体颜色为黑色, 要求MIUI6以上
     * Tested on: MIUIV7 5.0 Redmi-Note3
     */
    private boolean processMIUI(boolean lightStatusBar) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();

            try {
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                int darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", Integer.TYPE, Integer.TYPE);
                if (lightStatusBar) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);
                }

                result = true;
                if (Build.VERSION.SDK_INT >= 23) {
                    if (lightStatusBar) {
                        window.getDecorView().setSystemUiVisibility(9216);
                    } else {
                        window.getDecorView().setSystemUiVisibility(0);
                    }
                }
            } catch (Exception var9) {

            }
        }

        return result;
    }

    /**
     * 改变魅族的状态栏字体为黑色，要求FlyMe4以上
     */
    private boolean processFlyMe(boolean isLightStatusBar) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt((Object) null);
                int value = meizuFlags.getInt(lp);
                if (isLightStatusBar) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }

                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception var8) {
                ;
            }
        }

        return result;
    }

    private void processActionBar(final View view) {
        if (view == null) {
            return;
        }

        view.post(new Runnable() {
            @Override
            public void run() {
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop()
                                + getStatusBarOffsetPx(view.getContext()),
                        view.getPaddingRight(), view.getPaddingBottom());
                view.getLayoutParams().height += getStatusBarOffsetPx(view.getContext());
            }
        });

    }

    private static Builder from(Activity activity) {
        return new Builder().setWindow(activity);
    }

    public static class Builder {
        private Window window;
        private boolean lightStatusBar = false;
        private boolean transparentStatusBar = false;
        private boolean isSetActionbarPadding = false;
        private View actionBarView;
        private int statusBarColor = -1;


        //需要支持dialog等
        private Builder setWindow(@NonNull Activity activity) {
            this.window = activity.getWindow();
            return this;
        }

        /**
         * 沉浸式背景色为白色，状态栏的图标为黑色
         * 小米魅族等需要单独设置
         * MIUI 6+ FlyMe 4+
         *
         * @param lightStatusBar
         */
        private Builder setLightStatusBar(boolean lightStatusBar) {
            this.lightStatusBar = lightStatusBar;
            return this;
        }

        /**
         * 设置状态栏背景色为透明的
         *
         * @param transparentStatusBar
         * @return
         */
        private Builder setTransparentStatusBar(boolean transparentStatusBar) {
            this.transparentStatusBar = transparentStatusBar;
            return this;
        }

        /**
         * 设置状态栏背景色
         *
         * @param statusBarColor
         * @return
         */
        private Builder setStatusBarColor(int statusBarColor) {
            this.statusBarColor = statusBarColor;
            return this;
        }

        /**
         * 设置顶部View的paddingTop为状态栏高度
         *
         * @return
         */
        private Builder setActionbarPadding(View view) {
            if (null != view) {
                this.isSetActionbarPadding = true;
                this.actionBarView = view;
            }
            return this;
        }

        private void process() {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) return;
            try {
                new StatusBar(window, lightStatusBar, transparentStatusBar, isSetActionbarPadding, actionBarView, statusBarColor).process();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 这个方法是清空系统状态栏的状态
     * 主要用于一个Activity中，页面切换时重新设置状态栏的样式
     *
     * @param window
     */
    public static void clearSystemUiVisibility(Window window) {
        window.getDecorView().setSystemUiVisibility(0);
    }

    /**
     * 设置状态栏
     *
     * @param context
     * @param lightModel True:背景色为白色，状态栏图标为黑色  False:状态栏透明
     */
    public static void setStatusBar(Activity context, boolean lightModel) {
        if (null == context) return;
        StatusBar.clearSystemUiVisibility(context.getWindow());
        StatusBar.from(context)
                .setLightStatusBar(lightModel)
                .process();
    }

    /**
     * 设置状态栏
     *
     * @param context
     * @param lightModel True:背景色为白色，状态栏图标为黑色  False:状态栏透明
     * @param topView    顶部View，设置paddingTop为状态栏高度
     */
    public static void setStatusBar(Activity context, boolean lightModel, View topView) {
        if (null == context) return;
        StatusBar.clearSystemUiVisibility(context.getWindow());
        StatusBar.from(context)
                .setLightStatusBar(lightModel)
                .setActionbarPadding(topView)
                .process();
    }

    /**
     * 设置状态栏
     *
     * @param context
     * @param lightModel True:背景色为白色，状态栏图标为黑色  False:状态栏透明
     * @param color      状态栏颜色，lightModel=true时无效
     * @param topView    顶部View，设置paddingTop为状态栏高度
     */
    public static void setStatusBar(Activity context, boolean lightModel, @ColorRes int color, View topView) {
        if (null == context) return;
        StatusBar.clearSystemUiVisibility(context.getWindow());
        StatusBar.from(context)
                .setLightStatusBar(lightModel)
                .setStatusBarColor(ContextCompat.getColor(context, color))
                .setActionbarPadding(topView)
                .process();
    }
}
