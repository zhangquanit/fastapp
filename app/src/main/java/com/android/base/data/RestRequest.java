package com.android.base.data;

import android.text.TextUtils;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;

/**
 * 构建OkHttp请求
 *
 * @author 张全
 */

public final class RestRequest {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String UTF8 = "UTF-8";

    private int mConnectionTime;
    private int mReadTimeout;
    private int mWriteTimeout;

    private String mUrl;
    private Map<String, String> mHeaders = new HashMap<>();
    private RequestBody mRequestBody;
    private Object mTag;
    private String mMethod;


    private RestRequest(Builder builder) {
        Headers headers = builder.mHeaders.build();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            String key = headers.name(i);
            String value = headers.value(i);
            mHeaders.put(key, value);
        }

        this.mMethod = builder.mMethod;
        this.mUrl = builder.mUrl;
        this.mRequestBody = builder.mBody;
        this.mTag = builder.mTag;
        this.mConnectionTime = builder.mConnTimeout;
        this.mReadTimeout = builder.mReadTimeout;
        this.mWriteTimeout = builder.mWriteTimeout;
    }


    public long connectTimeout() {
        return mConnectionTime;
    }

    public long readTimeout() {
        return mReadTimeout;
    }

    public long writeTimeout() {
        return mWriteTimeout;
    }

    public String getUrl() {
        return mUrl;
    }

    public Map<String, String> getHeaderMap() {
        return mHeaders;
    }

    public RequestBody getRequestBody() {
        return mRequestBody;
    }

    public Object getTag() {
        return mTag;
    }

    public String getMethod() {
        return mMethod;
    }

    public static class Builder {
        private LinkedHashMap<String, Object> mParams = new LinkedHashMap<>();
        private String mUrl;

        private String mMethod;
        private Headers.Builder mHeaders;
        private RequestBody mBody;
        private Object mTag;

        private int mConnTimeout;
        private int mReadTimeout;
        private int mWriteTimeout;


        public Builder() {
            this.mMethod = GET;
            this.mHeaders = new Headers.Builder();
        }

        public Builder(String url) {
            if (TextUtils.isEmpty(url)) throw new NullPointerException("mUrl == null");
            this.mUrl = url;
            this.mMethod = GET;
            this.mHeaders = new Headers.Builder();
        }

        public Builder url(String url) {
            if (TextUtils.isEmpty(url)) throw new NullPointerException("mUrl == null");
            this.mUrl = url;
            return this;
        }

        public String getUrl() {
            return this.mUrl;
        }

        /**
         * 设置Header，如果已经存在同名的header，则替换
         */
        public Builder header(String name, String value) {
            mHeaders.set(name, value);
            return this;
        }

        /**
         * Adds a header with {@code username} and {@code value}. Prefer this mMethod for multiply-valued
         * mHeaders like "Cookie".
         * <p>
         * <p>Note that for some mHeaders including {@code Content-Length} and {@code Content-Encoding},
         * OkHttp may replace {@code value} with a header derived from the request mBody.
         */
        public Builder addHeader(String name, String value) {
            mHeaders.add(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            mHeaders.removeAll(name);
            return this;
        }

        /**
         * Removes all mHeaders on this builder and adds {@code mHeaders}.
         */
        public Builder headers(Headers headers) {
            this.mHeaders = headers.newBuilder();
            return this;
        }

        /**
         * Sets this request's {@code Cache-Control} header, replacing any cache control mHeaders already
         * present. If {@code cacheControl} doesn't define any directives, this clears this request's
         * cache-control mHeaders.
         */
        public Builder cacheControl(CacheControl cacheControl) {
            String value = cacheControl.toString();
            if (value.isEmpty()) return removeHeader("Cache-Control");
            return header("Cache-Control", value);
        }


        /**
         * 添加请求参数
         *
         * @param key
         * @param value
         * @return
         */
        public Builder addParam(String key, Object value) {
            if (null == mParams) {
                mParams = new LinkedHashMap<>();
            }
            mParams.put(key, value);
            return this;
        }

        /**
         * 移除请求参数
         *
         * @param key
         */
        public Builder removeParam(String key) {
            if (null != mParams) {
                mParams.remove(key);
            }
            return this;
        }

        public Builder get() {
            return method(GET, null);
        }


        public Builder post(RequestBody body) {
            return method(POST, body);
        }

        public Builder post() {
            this.mMethod = POST;
            return this;
        }

        public Builder method(String method, RequestBody body) {
            if (method == null) throw new NullPointerException("mMethod == null");
            if (method.length() == 0) throw new IllegalArgumentException("mMethod.length() == 0");
            if (body != null && !HttpMethod.permitsRequestBody(method)) {
                throw new IllegalArgumentException("mMethod " + method + " must not have a request mBody.");
            }
            if (body == null && HttpMethod.requiresRequestBody(method)) {
                throw new IllegalArgumentException("mMethod " + method + " must have a request mBody.");
            }
            this.mMethod = method;
            this.mBody = body;
            return this;
        }

        public Builder tag(Object tag) {
            this.mTag = tag;
            return this;
        }

        public Builder connectTimeout(long timeout, TimeUnit unit) {
            if (timeout < 0) throw new IllegalArgumentException("timeout < 0");
            if (unit == null) throw new NullPointerException("unit == null");
            long millis = unit.toMillis(timeout);
            if (millis > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Timeout too large.");
            if (millis == 0 && timeout > 0)
                throw new IllegalArgumentException("Timeout too small.");
            mConnTimeout = (int) millis;
            return this;
        }

        /**
         * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
         * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
         */
        public Builder readTimeout(long timeout, TimeUnit unit) {
            if (timeout < 0) throw new IllegalArgumentException("timeout < 0");
            if (unit == null) throw new NullPointerException("unit == null");
            long millis = unit.toMillis(timeout);
            if (millis > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Timeout too large.");
            if (millis == 0 && timeout > 0)
                throw new IllegalArgumentException("Timeout too small.");
            mReadTimeout = (int) millis;
            return this;
        }

        /**
         * Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
         * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
         */
        public Builder writeTimeout(long timeout, TimeUnit unit) {
            if (timeout < 0) throw new IllegalArgumentException("timeout < 0");
            if (unit == null) throw new NullPointerException("unit == null");
            long millis = unit.toMillis(timeout);
            if (millis > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Timeout too large.");
            if (millis == 0 && timeout > 0)
                throw new IllegalArgumentException("Timeout too small.");
            mWriteTimeout = (int) millis;
            return this;
        }

        //-------------------------------------------------------------
        private String constructUrl(String path, String encoding) {
            if (mMethod == POST) {
                return path;
            } else {
                try {
                    StringBuilder url = new StringBuilder(path);
                    if (mParams != null && !mParams.isEmpty()) {
                        if (!url.toString().contains("?")) {
                            url.append("?");
                        } else {
                            url.append("&");
                        }
                        for (Map.Entry<String, Object> entry : mParams.entrySet()) {
                            String key = entry.getKey();
                            if (!TextUtils.isEmpty(key)) {
                                key = URLEncoder.encode(key, encoding);
                            }
                            url.append(key);
                            url.append("=");
                            Object value = entry.getValue();
                            if (null != value && value instanceof String) {
                                url.append(URLEncoder.encode((String) value, encoding));
                            } else {
                                url.append(value);
                            }
                            url.append("&");
                        }
                        url.deleteCharAt(url.length() - 1);
                    }
                    return url.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return path;
        }

        public Builder setPostJsonData(JSONObject jsonObject) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            post(requestBody);
            return this;
        }

        public Map<String, Object> getParams() {
            return mParams;
        }

        public RestRequest buildJsonBody() {
            JSONObject jsonObject = new JSONObject(mParams);
            mBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            return new RestRequest(this);
        }

        public RestRequest build() {
            if (mUrl == null) throw new IllegalStateException("mUrl == null");
            if (mMethod.equalsIgnoreCase(GET)) {
                mUrl = constructUrl(mUrl, UTF8);
            }

            if (mMethod.equalsIgnoreCase(POST)) {
                if (!mParams.isEmpty() && mBody == null) {
                    FormBody.Builder formBuilder = new FormBody.Builder();
                    for (Map.Entry<String, Object> entry : mParams.entrySet()) {
                        String key = entry.getKey();
                        String value = "";

                        Object valueObj = entry.getValue();
                        if (null != valueObj) {
                            value = String.valueOf(valueObj);
                        }
                        formBuilder.add(key, value);
                    }
                    mBody = formBuilder.build();
                }
            }

            return new RestRequest(this);
        }
    }
}
