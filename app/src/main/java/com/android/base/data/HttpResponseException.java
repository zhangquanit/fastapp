package com.android.base.data;


import android.text.TextUtils;

import androidx.annotation.Keep;


/**
 * @author <a href="mailto:zhangqiushi@snqu.com">张秋实</a>
 * @version ${VERSION}
 * @description ${DESC}
 * @time 2017/5/23 15:34
 */

@Keep
public class HttpResponseException extends RuntimeException {
    public int resultCode = -1;// 服务器响应状态码 0正常
    public String message; //string返回操作信息，主要是给接口调用都阅读，不要抛出给用户看到。通常是success,code!=0时会有相应错误信息
    public String alert; //string用户层面的提示，客户端可直接notify用户，如果有的话。
    public Object data;

    public HttpResponseException(ResponseData data) {
        super(data.message);
        this.message = data.message;
        this.alert = data.message;
        this.data = data;
        this.resultCode = data.code;
        if (TextUtils.isEmpty(this.message)) {
            this.alert = "请求失败";
        }
    }

    public HttpResponseException(String alert, Throwable e) {
        super(e.getMessage(), e);
        this.resultCode = -1;
        this.message = e.getMessage();
        this.alert = alert;
    }

    public String getMsg() {
        return message;
    }


    @Override
    public String toString() {
        return "HttpResponseException{" +
                "resultCode=" + resultCode +
                ", message='" + message + '\'' +
                ", alert='" + alert + '\'' +
                ", error=" + super.toString() +
                '}';
    }
}
