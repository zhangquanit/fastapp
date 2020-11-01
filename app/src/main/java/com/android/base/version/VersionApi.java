package com.android.base.version;

import androidx.annotation.Keep;

import com.android.base.ApiHost;
import com.android.base.data.ResponseDataObject;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 张全
 */
@Keep
public interface VersionApi {
    /**
     * app升级
     *
     * @return
     */
    @GET(ApiHost.APP_UPDATE)
    Observable<ResponseDataObject<VersionEntity>> appUpdate(@Query("channel")String channel);
}
