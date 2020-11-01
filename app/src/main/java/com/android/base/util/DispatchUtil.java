package com.android.base.util;


import android.app.Activity;
import android.text.TextUtils;

import com.android.util.log.LogUtil;
import com.android.base.ui.WebViewFrag;
import com.fastapp.MainActivity;

import org.json.JSONObject;

/**
 * @author 张全
 */
public class DispatchUtil {

    /**
     * 跳转页面
     */
    public static void goToPage(Activity ctx, String json) throws Exception {
        LogUtil.d("DispatchUtil", "请求参数 json=" + json);
        JSONObject jsonObject = new JSONObject(json);
        String page = jsonObject.optString("page");
        JSONObject dataJson = jsonObject.optJSONObject("data");

//        UserEntity user = UserClient.getUser();
        switch (page) {
            case "HomePage": //首页
                MainActivity.start(ctx);
                break;
            case "WebViewPage":
                String url = dataJson.optString("url");
                if (!TextUtils.isEmpty(url)) {
                    WebViewFrag.WebViewParam webViewParam = new WebViewFrag.WebViewParam();
                    webViewParam.url = url;
                    WebViewFrag.start(ctx, webViewParam);
                }
                break;
            default:
                break;
        }
    }

}
