package com.android.base.ui.splash;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.android.base.App;
import com.android.base.util.KVUtil;
import com.android.base.widget.AlertDialogView;
import com.fastapp.MainActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.net.URLDecoder;

import common.widget.dialog.EffectDialogBuilder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 欢迎页面
 *
 * @author 张全
 */
public class SplashAct extends FragmentActivity {
    private static final String SHOW_PRIVACY_DIALOG = "SHOW_PRIVACY_DIALOG";
    private Handler mHandler = new Handler();
    private static long DELAY = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.splash_layout);
        //APP冷启动不需要延迟打开主页面  热启动需要1.2秒延迟
        if (!App.coldStart) {
            DELAY = 1200;
        }
        App.coldStart = false;
        applyPermission();
    }

//    @Override
//    public int getLayoutId() {
//        return R.layout.splash_layout;
//    }
//
//    @Override
//    public void init(Bundle savedInstanceState) {
//        //APP冷启动不需要延迟打开主页面  热启动需要1.2秒延迟
//        if (!App.coldStart) {
//            DELAY = 1200;
//        }
//        App.coldStart = false;
//        applyPermission();
//    }

    /**
     * 获取权限
     */
    @SuppressLint({"AutoDispose", "CheckResult"})
    public void applyPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        if (
//                !rxPermissions.isGranted(permission.READ_PHONE_STATE)||
                        !rxPermissions.isGranted(permission.READ_EXTERNAL_STORAGE) ||
                        !rxPermissions.isGranted(permission.WRITE_EXTERNAL_STORAGE)
        ) {

            rxPermissions.request(
                    //  permission.READ_PHONE_STATE,
                    permission.READ_EXTERNAL_STORAGE,
                    permission.WRITE_EXTERNAL_STORAGE
            )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean granted) throws Exception {
                            if (granted) {
                                showPrivacyDialog(false);
                                Log.e("showPrivacyDialog", "2");
                            } else {
                                grantPermissionDialog();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                        }
                    });
        } else {
            showPrivacyDialog(true);
            Log.e("showPrivacyDialog", "1");
        }
    }

    /**
     * 展示权限弹框
     */
    private void grantPermissionDialog() {
        if (isFinishing()) return;
        AlertDialogView dialogView = new AlertDialogView(SplashAct.this)
                .setContent("为保证您正常、安全地使用，请允许开通相应权限。")
                .setSingleBtn("去允许", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        applyPermission();
                    }
                });
        new EffectDialogBuilder(SplashAct.this)
                .setContentView(dialogView)
                .setCancelableOnTouchOutside(false)
                .setCancelable(false)
                .show();
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            openPage();
        }
    };

    /**
     * 展示隐私政策弹框
     *
     * @param delay 延长时长
     */
    private void showPrivacyDialog(boolean delay) {
        if (KVUtil.getBoolean(SHOW_PRIVACY_DIALOG, false)) {
            startPage(delay);
        } else {
            if (isFinishing()) return;
            PrivacyDialog privacyDialog = new PrivacyDialog();
            privacyDialog.setCallBack(() -> {
                startPage(false);
                KVUtil.set(SHOW_PRIVACY_DIALOG, true);
                privacyDialog.dismiss();
            });

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(privacyDialog, "privacyDialog");
            ft.commitAllowingStateLoss();
        }
    }

    private void startPage(boolean delay) {
        Uri uri = getIntent().getData();
        Log.e("JMLinkAPI", "data = " + uri);
        if (uri != null) {
            String param = uri.getQueryParameter("param");
            if (TextUtils.isEmpty(param)) {
                startOrigin(delay);
                return;
            }
            String pageInfo = null;
            try {
                pageInfo = URLDecoder.decode(param.trim(), "UTF-8");
//                DispatchUtil.goToPage(this, pageInfo);
                finish();
            } catch (Exception e) {
                startOrigin(delay);
            }
        } else {
            startOrigin(delay);
        }
    }

    private void startOrigin(boolean delay) {
        if (delay) {
            if (null != mHandler) mHandler.postDelayed(mRunnable, DELAY);
        } else {
            openPage();
        }
    }

    private void openPage() {
        if (GuideActivity.hasShowGuide()) {
            MainActivity.start(this);
        } else { //跳转到引导界面
            GuideActivity.start(this);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        mRunnable = null;
        mHandler = null;
    }
}
