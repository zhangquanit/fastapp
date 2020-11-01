package com.android.base.data;


import com.android.base.util.log.LogClient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * OkHttp请求
 *
 * @author 张全
 */
public final class RestClient {
    private static final String TAG = "RestClient";
    public static final int TIMEOUT_CONNECTION = 20; //连接超时
    public static final int TIMEOUT_READ = 20; //读取超时
    public static final int TIMEOUT_WRITE = 20; //写入超时

    private static final Charset UTF8 = Charset.forName("UTF-8");
    public static final String REST_API_URL = DataConfig.API_HOST;
    private static Retrofit s_retrofit;
    private static OkHttpClient fileDownloadClient;
    private static OkHttpClient imgDownloadClient;


    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                .proxy(Proxy.NO_PROXY)
                .connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
                .addInterceptor(new HeaderIntercepter());

        if (DataConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        getUnsafeOkHttpClient(builder);
        OkHttpClient client = builder.build();

        s_retrofit = new Retrofit.Builder()
                .baseUrl(REST_API_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
    }

    /**
     * 文件下载
     *
     * @return
     */
    public static OkHttpClient getDownloadClient() {
        if (null == fileDownloadClient) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS);
            getUnsafeOkHttpClient(builder);
            fileDownloadClient = builder.build();
        }
        return fileDownloadClient;
    }

    /**
     * 图片下载
     *
     * @return
     */
    public static OkHttpClient getImgDownloadClient() {
        if (null == imgDownloadClient) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)
                    .readTimeout(50, TimeUnit.SECONDS)
                    .writeTimeout(50, TimeUnit.SECONDS);
            getUnsafeOkHttpClient(builder);
            imgDownloadClient = builder.build();
        }
        return imgDownloadClient;
    }


    public static void getUnsafeOkHttpClient(OkHttpClient.Builder builder) {
        try {
            final X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory,trustManager);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class HeaderIntercepter implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();
            Request.Builder builder = request.newBuilder();

//            String token = UserClient.getToken();
//            builder.addHeader("x-app-source", APP_SOURCE); // 1 星口袋 2 星乐桃
//            builder.addHeader("x-sign", sign); //签名
//            builder.addHeader("x-timestamp", "" + timestamp);
//            builder.addHeader("x-appid", APPID);
//            builder.addHeader("x-m", AnalysisUtil.getUniqueId());//设备id
//
//
//            if (!TextUtils.isEmpty(token)) {
//                builder.addHeader("Authorization", token);
//            }
//
//            builder.addHeader("User-Agent", AnalysisUtil.getUA());
//            builder.addHeader("Cache-Control", CacheControl.FORCE_NETWORK.toString());
//            builder.addHeader("sgn", DeviceUtil.getSignature()); //签名校验
//            builder.addHeader("pkg", DeviceUtil.getPkgName()); //包名校验
//            builder.addHeader("appVersion", LContext.versionName); //版本号
//            builder.addHeader("client-v", LContext.versionName); //版本号
//            builder.addHeader("channel", LContext.channel);//渠道
//            builder.addHeader("check-enable", "1");
//            builder.addHeader("dev-type", "1");// 1 安卓  ,  2 IOS ,  3 其他
//            builder.addHeader("client-type", "2");//1,微信小程序 , 2  APP


            request = builder.build();
            Response response = chain.proceed(request);
            checkResponse(request.url().toString(), response);
            return response;
        }

    }

    private static void checkResponse(String url, Response response) {
        try {
            ResponseBody body = response.body();
            BufferedSource source = body.source();
            Headers headers = response.headers();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                GzipSource gzippedResponseBody = null;
                try {
                    gzippedResponseBody = new GzipSource(buffer.clone());
                    buffer = new Buffer();
                    buffer.writeAll(gzippedResponseBody);
                } finally {
                    if (gzippedResponseBody != null) {
                        gzippedResponseBody.close();
                    }
                }
            }
            Charset charset = UTF8;
            MediaType contentType = body.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            String responseStr = buffer.clone().readString(charset);
            //本地日志
            LogClient.logResponse(url, responseStr);

            //登录失效
//            int code = new JSONObject(responseStr).optInt("code");
//            if (code == 201) {
//                UserClient.loginOut();
//                EventBus.getDefault().post(new PushEvent(Constant.Event.LOGIN_OUT));
//                EventBus.getDefault().post(new PushEvent(Constant.Event.LOGIN_SESSION_INVALIDATE));
//            }

        } catch (Exception e) {
        }
    }


    public static <T> T getService(Class<T> serviceClass) {
        return s_retrofit.create(serviceClass);
    }

    public static OkHttpClient getHttpClient() {
        Call.Factory factory = s_retrofit.callFactory();
        return (OkHttpClient) factory;
    }


}
