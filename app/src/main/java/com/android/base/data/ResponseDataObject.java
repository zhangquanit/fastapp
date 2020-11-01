package com.android.base.data;

import androidx.annotation.Keep;

/**
 * data为JsonObject
 *
 * @author 张全
 */
@Keep
public class ResponseDataObject<T> extends ResponseData {
    public T data;

    @Override
    public String toString() {
        return "ResponseDataObject{" +
                "data=" + data +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
