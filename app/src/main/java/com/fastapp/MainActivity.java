package com.fastapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.base.Constant;
import com.android.base.data.HttpResponseException;
import com.android.base.data.NetReqResult;
import com.android.base.data.ResponseDataObject;
import com.android.base.event.PushEvent;
import com.android.base.task.BackgroundTask;
import com.android.base.ui.BaseActivity;
import com.android.base.util.GlideUtil;
import com.android.base.util.share.ShareDialog;
import com.android.base.util.share.ShareInfo;
import com.android.base.widget.AlertDialogView;
import com.android.util.ext.ToastUtil;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.OnClick;
import common.widget.dialog.EffectDialogBuilder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends BaseActivity {

    public static void start(Context ctx) {
        ctx.startActivity(new Intent(ctx, MainActivity.class));
    }

    @androidx.annotation.NonNull
    @NotNull
    @Override
    public AppCompatDelegate getDelegate() {
        return super.getDelegate();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        addAction(Constant.Event.LOGIN_SUCCESS);
        //加载webp
        GlideUtil.loadLocalPic(findViewById(R.id.imageView), R.drawable.icon_vip);

        dirTest();
        doTask();

        LinearLayout view = findViewById(R.id.testbar);
        Drawable background = view.getBackground();
        System.out.println("MainActivity background=" + background + ",view=" + view);
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEvent(PushEvent event) {
        if (containsAction(event.getAction())) {
            ToastUtil.show((String) event.getData());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ClickUtils.back2HomeFriendly("再按一次退出");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @OnClick({R.id.testFrag, R.id.showDialog, R.id.http, R.id.share})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.testFrag:
                String[] permissions = new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                };
                boolean isGranted = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        isGranted = true;
                    } else {
                        isGranted = false;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    for (String permission : permissions) {
                        int ps = ContextCompat.checkSelfPermission(mContext, permission);
                        if (ps != PackageManager.PERMISSION_GRANTED) {
                            isGranted = false;
                            break;
                        }
                    }
                } else {
                    for (String permission : permissions) {
                        int ps = PermissionChecker.checkSelfPermission(mContext, permission);
                        if (ps != PackageManager.PERMISSION_GRANTED) {
                            isGranted = false;
                            break;
                        }
                    }
                }

                System.out.println("isGranted=" + isGranted);
                break;
            case R.id.showDialog:
                showDialog();
                break;
            case R.id.http:
                checkVersion();
                break;
            case R.id.share:
                share();
                break;
        }
    }


    private void showDialog() {
        AlertDialogView alertDialogView = new AlertDialogView(MainActivity.this)
                .setTitle("温馨提示")
                .setContent("本应用为非官方正版应用，请从正规市场或官网下载.")
                .setSingleBtn("好的", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
        Dialog dialog = new EffectDialogBuilder(MainActivity.this)
                .setContentView(alertDialogView)
                .setCancelable(true)
                .setCancelableOnTouchOutside(false)
                .show();
    }

    private void checkVersion() {
        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        System.out.println("homeViewModel=" + homeViewModel);
        homeViewModel.mNetReqResultLiveData.observe(this, new Observer<NetReqResult>() {
            @Override
            public void onChanged(NetReqResult netReqResult) {
                if (TextUtils.equals(netReqResult.tag, HomeViewModel.TAG_VERSION)) {
                    if (netReqResult.successful) {
                        ResponseDataObject responseDataObject = (ResponseDataObject) netReqResult.data;
                        System.out.println("checkVersion  success =" + responseDataObject);
                    } else {
                        HttpResponseException exception = (HttpResponseException) netReqResult.data;
                        System.out.println("checkVersion  fail =" + exception);
                    }
                }
            }
        });

        homeViewModel.getVersion();
    }

    private void share() {
        //系统分享
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_icon);
//        ShareDialog.share(this,bitmap);

        //友盟分享
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.title = "分享标题分享标题";
        shareInfo.content = "分享内容分享内容";
        shareInfo.pic_url = "https://pics4.baidu.com/feed/279759ee3d6d55fb63d1aeb7e76fa24c21a4dde5.png?token=dca942371742d627d415d20477365232";
        shareInfo.url = "https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9447162866675341698%22%7D&n_type=0&p_from=1";
        ShareDialog.share(this, shareInfo);

    }

    private void dirTest() {
        //  /data 下的目录
        File dataDir = getDataDir(); //   /data/user/0/com.fastapp
        File filesDir = getFilesDir(); // /data/user/0/com.fastapp/files
        File cacheDir = getCacheDir();//  /data/user/0/com.fastapp/cache

        //SD卡目录
        File externalStorageDirectory = Environment.getExternalStorageDirectory();//SD卡根目录 (公有)  /storage/emulated/0
        File externalCacheDir = getExternalCacheDir(); //SD卡app私有目录           /storage/emulated/0/Android/data/com.fastapp/cache
        File externalFilesDir = getExternalFilesDir(null); //SD卡app私有目录 /storage/emulated/0/Android/data/com.fastapp/files
        File imageFilesDir = getExternalFilesDir("image"); //SD卡app私有目录 /storage/emulated/0/Android/data/com.fastapp/files/image


        System.out.println("dataDir=" + dataDir);
        System.out.println("filesDir=" + filesDir);
        System.out.println("cacheDir=" + cacheDir);


        System.out.println("externalStorageDirectory=" + externalStorageDirectory);
        System.out.println("externalCacheDir=" + externalCacheDir);
        System.out.println("externalFilesDir=" + externalFilesDir);
        System.out.println("externalFilesDir(\"image\")=" + imageFilesDir);


    }

    private void rxjavaTest() {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                System.out.println("emitter=" + emitter);
                System.out.println("thread=" + Thread.currentThread().getName());
                emitter.onNext("hello world");
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("result=" + s);
                        System.out.println("thread=" + Thread.currentThread().getName());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        System.out.println("disposable=" + disposable);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void doTask() {
        BackgroundTask.execute(MyTask.class);
    }

}
