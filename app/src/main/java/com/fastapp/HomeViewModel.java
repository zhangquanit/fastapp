package com.fastapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.android.base.BaseAndroidViewModel;
import com.android.base.data.BaseResponseObserver;
import com.android.base.data.HttpResponseException;
import com.android.base.data.NetReqResult;
import com.android.base.data.ResponseDataObject;
import com.android.base.version.VersionClient;
import com.android.base.version.VersionEntity;

/**
 * @author 张全
 */
public class HomeViewModel extends BaseAndroidViewModel {
    public static final String TAG_VERSION="TAG_VERSION";
    public MutableLiveData<NetReqResult> mNetReqResultLiveData = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    public void getVersion(){
        executeNoMapHttp(VersionClient.doUpdate("prod"), new BaseResponseObserver<ResponseDataObject<VersionEntity>>() {
            @Override
            public void onSuccess(ResponseDataObject<VersionEntity> value) {
                mNetReqResultLiveData.setValue(new NetReqResult(TAG_VERSION,null,true,value));
            }

            @Override
            public void onError(HttpResponseException e) {
                mNetReqResultLiveData.setValue(new NetReqResult(TAG_VERSION,null,false,e));
            }

            @Override
            public void onEnd() {

            }
        });
    }
}
