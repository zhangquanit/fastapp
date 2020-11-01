package com.android.base.util.share;

import androidx.annotation.Keep;

import org.json.JSONObject;

import java.io.Serializable;

/*
 * 分享
 */
@Keep
public class ShareInfo implements Serializable {
    public String title;
    public String content;
    public String pic_url;
    public String url;

    public String getShareInfo() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", title);
            jsonObject.put("content", content);
            jsonObject.put("url", url);
            jsonObject.put("pic_url", pic_url);
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}