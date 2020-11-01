package com.android.base.data;

import androidx.annotation.Keep;

/**
 * @author 张全
 */
@Keep
public class ResponseData{
    public int code;
    public String message;

    public boolean isSuccessful() {
        return code == 0;
    }
}
