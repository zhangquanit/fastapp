package com.android.base.version;

import com.android.base.data.ResponseDataObject;
import com.android.base.data.RestClient;

import io.reactivex.Observable;

/**
 * @author 张全
 */
public class VersionClient {

    public static Observable<ResponseDataObject<VersionEntity>> doUpdate(String channel) {
        return RestClient.getService(VersionApi.class).appUpdate(channel);
    }

}
