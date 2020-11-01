package com.android.base.data;


import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.observers.DisposableObserver;


public abstract class BaseResponseObserver<T> extends DisposableObserver<T> {

    private static final String TAG = "BaseResponseObserver";

    @Override
    public final void onError(Throwable e) {
        if (e != null) {
            e.printStackTrace();
        }
        HttpResponseException errorCause;
        if (e instanceof UnknownHostException) {
            errorCause = new HttpResponseException("无网络连接, 请重试", e);
        } else if (e instanceof SocketTimeoutException || e instanceof ConnectException) {
            errorCause = new HttpResponseException("网络连接超时, 请重试", e);
        } else if (e instanceof HttpResponseException) { //服务器错误
            errorCause = (HttpResponseException) e;
        } else {
            errorCause = new HttpResponseException("网络异常", e);
        }
        onError(errorCause);
        onEnd();
    }

    @Override
    public void onNext(T value) {
        if (value instanceof ResponseData) {
            ResponseData data = (ResponseData) value;
            if (!data.isSuccessful()) {
                onError(new HttpResponseException(data));
            } else {
                onSuccess(value);
            }
        } else {
            onSuccess(value);
        }
    }

    @Override
    public void onComplete() {
        onEnd();
    }

    /**
     * 请求成功
     *
     * @param value
     */
    public abstract void onSuccess(T value);

    /**
     * 请求失败
     *
     * @param e
     */
    public abstract void onError(HttpResponseException e);

    /**
     * 请求结束
     * <p>不管请求成功还是失败，都会回调onEnd</p>
     */
    public abstract void onEnd();
}
