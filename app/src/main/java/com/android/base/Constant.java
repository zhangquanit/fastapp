package com.android.base;

import com.android.util.LContext;
import com.android.base.data.DataConfig;

/**
 *
 */
public class Constant {
    public static String SD_DIR = ""; //SD卡保存目录
    private static String COMMON_PROVIDER = ".common.provider";


    /**
     * 事件
     */
    public interface Event {

        //退出登录
        String LOGIN_OUT = "LOGIN_OUT";
        //登录成功
        String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        //登录失效
        String LOGIN_SESSION_INVALIDATE = "LOGIN_SESSION_INVALIDATE";

        //支付
        String ORDER_BUY_SUCCESS = "ORDER_BUY_SUCCESS";
        String ORDER_BUY_FAIL = "ORDER_BUY_FAIL";
        String ORDER_BUY_CANCEL = "ORDER_BUY_CANCEL";

        //wx授权Code
        String WX_CODE = "WX_CODE";

    }


    /**
     * H5页面地址
     */
    public interface WebPage {
        String GROUND_PUSH = DataConfig.H5_HOST + "xx";

    }

    public static String getCommonAuthority() {
        return LContext.getContext().getPackageName() + COMMON_PROVIDER;
    }
}
