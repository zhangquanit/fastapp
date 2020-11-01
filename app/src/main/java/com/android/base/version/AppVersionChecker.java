package com.android.base.version;

import android.annotation.SuppressLint;

import com.android.util.LContext;
import com.android.base.data.BaseResponseObserver;
import com.android.base.data.HttpResponseException;
import com.android.base.data.ResponseDataObject;

import component.update.AppVersion;
import component.update.IAppVersionChecker;
import component.update.VersionUpdateListener;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author 张全
 */
public class AppVersionChecker implements IAppVersionChecker {
    private VersionUpdateListener mUpdateListener;

    @SuppressLint({"AutoDispose", "CheckResult"})
    @Override
    public void doVersionCheck(final VersionUpdateListener updateListener) {
        this.mUpdateListener = updateListener;
        VersionClient.doUpdate(LContext.channel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseResponseObserver<ResponseDataObject<VersionEntity>>() {
                    @Override
                    public void onSuccess(ResponseDataObject<VersionEntity> data) {
                        if (null == mUpdateListener) {
                            return;
                        }

                        if (data.isSuccessful()) {
                            VersionEntity versionEntity = data.data;
                            if (null != versionEntity && LContext.versionCode < versionEntity.version_code) {
                                AppVersion appVersion = new AppVersion();
                                appVersion.versionName = versionEntity.version;
                                appVersion.versionCode = versionEntity.version_code;
                                appVersion.downloadUrl = versionEntity.url;
                                appVersion.desc = versionEntity.update_log;
                                appVersion.forceUpdate = versionEntity.forced_update;
                                updateListener.onNewVersionReturned(appVersion);
                            } else {
                                updateListener.onNoVersionReturned();
                            }
                        } else {
                            updateListener.fail();
                        }
                    }

                    @Override
                    public void onError(HttpResponseException e) {
                        if (null != updateListener) {
                            updateListener.fail();
                        }
                    }

                    @Override
                    public void onEnd() {

                    }
                });
    }

    @Override
    public void stopVersionCheck() {
        mUpdateListener = null;
    }
}
