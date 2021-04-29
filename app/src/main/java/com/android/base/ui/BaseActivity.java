package com.android.base.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;

import com.android.util.ext.ToastUtil;
import com.android.util.log.LogUtil;
import com.blankj.utilcode.util.KeyboardUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import common.widget.dialog.LoadingDialog;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * Activity基类
 *
 * @author zhangquan
 */
public abstract class BaseActivity extends SupportActivity {

    private static final String TAG = "BaseActivity";
    private String mPageName;
    private LoadingDialog mLoadingDialog;
    protected List<String> mActionList = new ArrayList<String>();
    private boolean mEventBusRegisted;
    protected Activity mContext;
    protected int INVALIDE_LAYOUT = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = this.getClass().getSimpleName();
        mContext = this;
        LogUtil.d(TAG, "onCreate " + mPageName + ",pid=" + Process.myPid());
        int layoutId = getLayoutId();
        if (layoutId == INVALIDE_LAYOUT) {
            finish();
            return;
        }
        try {
            setContentView(layoutId);
            init(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.d(TAG, "onStart " + mPageName);
    }

    protected void onResume() {
//        Session.onResume(this); //极光魔炼
        super.onResume();
        LogUtil.d(TAG, "onResume " + mPageName);
    }

    protected void onPause() {
//        Session.onPause(this); //极光魔炼
        super.onPause();
        LogUtil.d(TAG, "onPause " + mPageName);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d(TAG, "onStop " + mPageName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy " + mPageName);
        closeLoadDialog();
        if (!mActionList.isEmpty()) {
            EventBus.getDefault().unregister(this);
        }
        KeyboardUtils.hideSoftInput(mContext);
        KeyboardUtils.fixSoftInputLeaks(mContext);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.d(TAG, "onNewIntent " + mPageName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onRestoreInstanceState " + mPageName);
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    protected String getPageName() {
        return getClass().getSimpleName();
    }


    public void showToastShort(String s) {
        if (isFinishing()) {
            return;
        }
        ToastUtil.show(s);
    }

    public void showToastShort(int resId) {
        if (isFinishing()) {
            return;
        }
        ToastUtil.show(resId);
    }

    public void showLoadingDialog(String content) {
        mLoadingDialog = LoadingDialog.showBackCancelableDialog(this, content);
    }


    public void closeLoadDialog() {
        try {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startAct(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(getApplicationContext(), cls);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startAct(intent);
    }

    protected void startAct(String action, Bundle bundle) {
        Intent intent = new Intent(action);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startAct(intent);
    }

    public void addAction(String action) {
        if (!mActionList.contains(action))
            mActionList.add(action);
        if (!mEventBusRegisted) {
            EventBus.getDefault().register(this);
        }
        mEventBusRegisted = true;
    }

    public boolean containsAction(String action) {
        return mActionList.contains(action);
    }

    protected void startAct(Intent intent) {
        startActivity(intent);
    }

    public abstract int getLayoutId();

    public abstract void init(Bundle savedInstanceState);

}
