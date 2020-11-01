package com.android.base.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.android.util.ext.ToastUtil;
import com.android.util.log.LogUtil;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * Fragment基类
 *
 * @author zhangquan
 */
public abstract class BaseFragment extends SupportFragment {
    protected View mView;
    protected FragmentActivity mContext;
    protected boolean mIsDestoryed;
    private boolean mIsPaused;
    private static final String TAG = "BaseFragment";
    protected List<String> mActionList = new ArrayList<String>();
    private boolean mEventBusRegisted;
    protected String pageName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mContext = getActivity();
        mIsDestoryed = false;
        LogUtil.d(TAG, "onCreate " + getPageObjName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreateView " + getPageName());
        mView = inflater.inflate(getLayoutId(), container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);

        boolean killed = false;
        if (null != savedInstanceState) {
            killed = savedInstanceState.getBoolean("killed", false);
        }
        if (killed) {
            restorePage();
        }
    }

    @Override
    public void onResume() {
        LogUtil.d(TAG, "onResume " + getPageObjName());
        super.onResume();
        mIsPaused = false;
        MobclickAgent.onPageStart(getPageName());
    }

    @Override
    public void onPause() {
        LogUtil.d(TAG, "onPause " + getPageObjName());
        super.onPause();
        mIsPaused = true;
        MobclickAgent.onPageEnd(getPageName());
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtil.d(TAG, "onStop " + getPageName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy " + getPageObjName());
        mIsDestoryed = true;
        if (!mActionList.isEmpty()) {
            EventBus.getDefault().unregister(this);
        }
//        cancelToast();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("killed", true);
        super.onSaveInstanceState(outState);
    }

    public String getPageName() {
        return getClass().getSimpleName();
    }

    private String getPageObjName() {
        return getPageName() + hashCode();
    }


    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    public FragmentActivity getContext() {
        return mContext;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int id) {
        View view = mView.findViewById(id);
        return (T) view;
    }

    public void setOnClickListener(int id, OnClickListener listener) {
        mView.findViewById(id).setOnClickListener(listener);
    }

    public void setOnClickListener(View view, OnClickListener listener) {
        view.setOnClickListener(listener);
    }

    protected void startService(Class<? extends Service> service) {
        startService(new Intent(mContext, service));
    }

    protected void startService(Intent intent) {
        mContext.startService(intent);
    }

    protected void stopService(Class<? extends Service> service) {
        mContext.stopService(new Intent(mContext, service));
    }


    protected void startAct(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(mContext, cls);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    protected void startAct(String action, Bundle bundle) {
        Intent intent = new Intent(action);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    protected void finish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    public void close() {
        BaseFragment preFragment = (BaseFragment) getPreFragment();
        if (preFragment != null) {
            pop();
        } else {
            getActivity().finish();
        }
    }

    public void showToastShort(String s) {
        FragmentActivity activity = getActivity();
        if (null == activity || isRemoving() || activity.isFinishing()
                || mIsPaused) {
            return;
        }
        ToastUtil.show(s);
    }

    public void showToastShort(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity || isRemoving() || activity.isFinishing()
                || mIsPaused) {
            return;
        }
        ToastUtil.show(resId);
    }

    public void cancelToast() {
        ToastUtil.cancel();
    }

    public LifecycleOwner getLifecycleOwner() {
        return (LifecycleOwner) this;
    }

    protected int getColor(int colorId) {
        return ContextCompat.getColor(getActivity(), colorId);
    }


    // #########################################################
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

    // #########################################################

    protected abstract int getLayoutId();

    protected abstract void init(Bundle savedInstanceState);

    public void restorePage() {

    }

}
