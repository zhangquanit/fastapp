package com.android.base.util.log;

import androidx.annotation.NonNull;

import com.android.util.LContext;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogInfo {
    public String time; //时间
    public String network; //网络
    public String thread; //线程
    public String phone; //手机号
    public String token; //用户token
    public String tag; //
    public String info; //日志内容
    private static final SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogInfo(String tag, String info) {
        time = DateFormat.format(new Date());
        network = AnalysisUtil.networkType(LContext.getContext());
        thread = Thread.currentThread().getName();
//        UserEntity user = UserClient.getUser();
//        if (null != user) {
//            phone = user.phone;
//            token = user.token;
//        } else {
//            phone = "-";
//            token = "-";
//        }
        this.tag = tag;
        this.info = info;
    }

    @NonNull
    @Override
    public String toString() {
//        2020-02-27 10.04.222  wifi/main/phone/uid/WebViewFrag: 请求url=xxxx
        return new StringBuffer()
                .append(time).append(" ")
                .append(network).append("/")
                .append(thread).append("/")
                .append(phone).append("/")
                .append(token).append("/")
                .append(tag).append(": ")
                .append(info)
                .append("\n")
                .toString();
    }
}
