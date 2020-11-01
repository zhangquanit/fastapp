package com.android.base.util.pay;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 支付信息
 *
 * @author 张全
 */
public class OrderPayResponse implements Serializable {
    //微信支付信息
    public String appid;
    public String partnerid;
    public String prepayid;
    @SerializedName("package")
    public String packageStr;
    public String timestamp;
    public String noncestr;
    public String sign;

    //支付宝支付信息.
    public String orderInfo;

}
