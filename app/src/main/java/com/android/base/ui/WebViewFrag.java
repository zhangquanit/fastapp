package com.android.base.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.util.LContext;
import com.android.util.ext.ToastUtil;
import com.android.util.log.LogUtil;
import com.android.base.Constant;
import com.android.base.event.EventMsg;
import com.android.base.util.CommonUtil;
import com.android.base.util.DispatchUtil;
import com.android.base.util.NotificationPageHelper;
import com.android.base.util.filedownloader.FileDownloadDialogView;
import com.android.base.util.filedownloader.FileDownloader;
import com.android.base.util.share.ShareDialog;
import com.android.base.util.share.ShareInfo;
import com.android.base.widget.AlertDialogView;
import com.android.base.widget.StatusBar;
import com.android.base.widget.TitleBarView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.fastapp.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import common.widget.LoadingBar;
import common.widget.dialog.EffectDialogBuilder;
import common.widget.dialog.LoadingDialog;

/**
 * WebView页面
 *
 * @author zhangquan
 */
public class WebViewFrag extends SimpleFrag {
    public static final String PARAM = "PARAM";
    private WebView webView;
    private ProgressBar progressBar;
    private LoadingBar loadingBar;
    private WebViewParam webViewParam;
    private TitleBarView titleBarView;
    private FrameLayout fullScreenView;
    private RelativeLayout webContainer;
    private View myView;
    private static final String TAG = "WebViewFrag";
    private LoadingDialog loadingDialog;
    private String original_Url; //进入页面的url，没有添加公共参数
    private boolean isInit = false;

    public static class WebViewParam implements Serializable {
        //标题
        public String title;
        //是否需要重置title 显示webview的标题
        public boolean shouldResetTitle = true;
        //链接URL  url目前支持needLogin(先登录)、hideTitlebar(隐藏标题栏)、lightModel(透明状态栏、黑色文字)
        public String url;
        public String titleBarColor; //标题栏color

        public ShareInfo shareInfo; //分享信息
        public boolean isShowShare; // 是否显示右上角分享按钮
        public boolean checkNetwork = true; //入口检查网络
        public boolean sensorOriention; //自动旋转屏幕方向
    }

    public static Bundle getParamBundle(WebViewParam param) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM, param);
        return bundle;
    }

    public static SimpleFragAct.SimpleFragParam getStartParam(WebViewParam param) {
        return new SimpleFragAct.SimpleFragParam(param.title,
                WebViewFrag.class, WebViewFrag.getParamBundle(param));
    }

    public static void start(Context ctx, SimpleFragAct.SimpleFragParam param) {
        SimpleFragAct.start(ctx, param);
    }

    public static void start(Context ctx, WebViewParam param) {
        try {
//            String url = param.url;
//            String needLogin = Uri.parse(url).getQueryParameter("needLogin");
//            if (TextUtils.equals(needLogin, "1") && UserClient.getUser() == null) { //首先登录
//                LoginFragment.start(ctx);
//            } else {
//                SimpleFragAct.SimpleFragParam startParam = getStartParam(param);
//                startParam.mutliPage = true;
//                SimpleFragAct.start(ctx, startParam,param.sensorOriention);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.common_webview;
    }

    @Override
    public void init(Bundle savedInstanceState) {

        if (null != getArguments()) {
            webViewParam = (WebViewParam) getArguments().getSerializable(PARAM);
        }

        if (null == webViewParam) {
            close();
            return;
        }

        // 网络无连接
        if (webViewParam.checkNetwork && !NetworkUtils.isAvailable()) {
            showToastShort(R.string.net_noconnection);
            close();
            return;
        }

        //请求无链接
        if (TextUtils.isEmpty(webViewParam.url)) {
            showToastShort("请求无链接");
            close();
            return;
        }

        LogUtil.d(TAG, webViewParam.url);

        addAction(Constant.Event.LOGIN_SUCCESS);
        init();
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            Method onResume = webView.getClass().getMethod("onResume");
            if (null != onResume) onResume.invoke(webView, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isInit) {
            viewWillAppear();
        }
        isInit = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Method onPause = webView.getClass().getMethod("onPause");
            if (null != onPause) onPause.invoke(webView, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean handleBackEvent() {
        if (null != webView && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    protected void init() {
        webView = findViewById(R.id.webview);
        handleLongClick(); //长按保存图片
        fullScreenView = findViewById(R.id.video_fullscreen); //webview视频全屏播放
        webContainer = findViewById(R.id.web_parent);
        progressBar = findViewById(R.id.pb);
        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loadingBar.canLoading()) {
                    return;
                }
                if (!NetworkUtils.isAvailable()) {
                    ToastUtil.show(R.string.net_noconnection);
                    return;
                }
                webView.reload();
            }
        });

        titleBarView = getTitleBar();

        if (null != titleBarView) {
            View.OnClickListener backListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (handleBackEvent()) {
                        return;
                    }
                    close();
                }
            };
            titleBarView.setOnLeftTxtClickListener(backListener);
            titleBarView.setOnLeftBtnClickListener(backListener);


            //刷新按钮
            titleBarView.setRightBtnDrawable(R.drawable.icon_refresh);
            titleBarView.mRightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkUtils.isAvailable()) {
                        ToastUtil.show(R.string.net_noconnection);
                        return;
                    }
                    webView.reload();
                }
            });

            //分享
            if (null != webViewParam.shareInfo) {
                titleBarView.setRightBtnDrawable(R.drawable.icon_share);
                titleBarView.mRightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShareDialog.share(getActivity(), webViewParam.shareInfo);
                    }
                });
            }

            if (webViewParam.isShowShare) {
                titleBarView.findViewById(R.id.title_icon_share).setVisibility(View.VISIBLE);
                titleBarView.findViewById(R.id.title_icon_share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            } else {
                titleBarView.findViewById(R.id.title_icon_share).setVisibility(View.GONE);
            }
        }


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);//自动播放视频
        webSettings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        webSettings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        webSettings.setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        webSettings.setAllowUniversalAccessFromFileURLs(false);


        //设置缓存
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        int statusBarHeight = 0;
        try {
            statusBarHeight = BarUtils.getStatusBarHeight();
            statusBarHeight = SizeUtils.px2dp(statusBarHeight);
        } catch (Exception e) {
            statusBarHeight = 25;
        }
        LogUtil.d(TAG, "statusBarHeight=" + statusBarHeight);

        original_Url = webViewParam.url;

        //拼接参数
        Uri uri = Uri.parse(webViewParam.url);
        Uri.Builder builder = uri.buildUpon();
        Set<String> parameterNames = uri.getQueryParameterNames();
        if (!parameterNames.contains("appVersion")) {
            builder.appendQueryParameter("appVersion", LContext.versionName);
        }
        if (!parameterNames.contains("isApp")) {
            builder.appendQueryParameter("isApp", "1");
        }

        if (!parameterNames.contains("statusBarH")) { //状态栏高度
            builder.appendQueryParameter("statusBarH", statusBarHeight + "");
        }

        webViewParam.url = builder.build().toString();

        //标题栏背景
        if (webViewParam.titleBarColor != null) {
            getTitleBar().setBackgroundColor(Color.parseColor(webViewParam.titleBarColor));
            getTitleBar().setTitleTextColor(R.color.white);
            getTitleBar().setLeftBtnWhiteColor();
        } else {
            titleBarView.setBackgroundColor(Color.WHITE);
        }

        uri = Uri.parse(webViewParam.url);
        //隐藏标题栏
        String hideTitlebar = uri.getQueryParameter("hideTitlebar");
        if (TextUtils.equals(hideTitlebar, "1")) {
            titleBarView.setVisibility(View.GONE);
            String lightModel = uri.getQueryParameter("lightModel");
            if (TextUtils.equals(lightModel, "1")) {
                StatusBar.setStatusBar(mContext, true, titleBarView);
            } else {
                StatusBar.setStatusBar(mContext, false, titleBarView);
            }
        } else {
            StatusBar.setStatusBar(mContext, true, titleBarView);
        }


        LogUtil.d(TAG, "加载 url=" + webViewParam.url);
        webView.loadUrl(webViewParam.url);
        webView.setWebChromeClient(new DWebChromeClient());
        webView.setWebViewClient(new DWebViewClient());
        webView.setDownloadListener(new DWebViewDownLoadListener());

    }

    /**
     * 辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
     */
    private final class DWebChromeClient extends WebChromeClient {

        /**
         * 全屏模式
         *
         * @param view     需要展示的view内容
         * @param callback
         */
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            LogUtil.d(TAG, "onShowCustomView  view=" + view);
            try {
                ViewGroup parent = (ViewGroup) webView.getParent();
                parent.removeView(webView);

                fullScreenView.addView(view);
                fullScreenView.setVisibility(View.VISIBLE);
                getTitleBar().setVisibility(View.GONE);
                myView = view;
                setFullScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            LogUtil.d(TAG, "onHideCustomView  ");
            try {
                if (myView != null) {
                    fullScreenView.removeAllViews();
                    webContainer.addView(webView);
                    fullScreenView.setVisibility(View.GONE);

                    getTitleBar().setVisibility(View.VISIBLE);
                    myView = null;
                    quitFullScreen();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            LogUtil.d(TAG, "onJsAlert,url=" + url + ",message=" + message);
            AlertDialogView dialogView = new AlertDialogView(webView.getContext())
                    .setTitle("提示")
                    .setContent(message)//
                    .setSingleBtn("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            result.confirm();
                        }
                    });

            new EffectDialogBuilder(webView.getContext())
                    .setCancelable(false)
                    .setCancelableOnTouchOutside(false)
                    .setContentView(dialogView).show();

//            return super.onJsAlert(view, url, message, result);
            return true;
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            return super.onJsBeforeUnload(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            LogUtil.d(TAG, "onJsConfirm,url=" + url + ",message=" + message);
            AlertDialogView dialogView = new AlertDialogView(webView.getContext())
                    .setContent(message)//
                    .setRightBtn("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            result.confirm();
                        }
                    })
                    .setLeftBtn("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            result.cancel();
                        }
                    });
            new EffectDialogBuilder(webView.getContext())
                    .setCancelable(false)
                    .setCancelableOnTouchOutside(false)
                    .setContentView(dialogView).show();
//            return super.onJsConfirm(view, url, message, result);
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            LogUtil.d(TAG, "onJsPrompt,url=" + url + ",message=" + message);
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }


        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            LogUtil.d(TAG, "onReceivedTitle,url=" + view.getUrl());
            LogUtil.d(TAG, "onReceivedTitle,title=" + title);
            if (webViewParam.shouldResetTitle && !TextUtils.isEmpty(title) && !title.startsWith("http")) {
                if (null != titleBarView) {
                    titleBarView.setTitleText(title);
                }
                if (!"登录".equals(title)) {
                    webViewParam.title = title;
                }
            }

        }

        /**
         * 上传文件
         *
         * @param webView
         * @param filePathCallback
         * @param fileChooserParams
         * @return
         */
        @Override
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            fileCallback = filePathCallback;
            openFileChooser(null != fileChooserParams ? fileChooserParams.getAcceptTypes() : null);
            return true;
        }
    }

    /**
     * 设置全屏
     */
    private void setFullScreen() {
        // 设置全屏的相关属性，获取当前的屏幕状态，然后设置全屏
        try {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出全屏
     */
    private void quitFullScreen() {
        // 声明当前屏幕状态的参数并获取
        try {
            final WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().setAttributes(attrs);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_CHOOSER_RESULT_CODE = 11;


    /**
     * webview选择上传图片或视频
     *
     * @param types
     */
    private void openFileChooser(String[] types) {
        LogUtil.d(TAG, "onShowFileChooser 请求type=" + Arrays.toString(types));
        String type = "";
        if (null != types && types.length > 0) {
            type = types[0];
        }
        if (TextUtils.isEmpty(type)) {
            type = "image/*;video/*";
        }

        if (type.contains("jpg") || type.contains("png")) {
            type = "image/*";
        } else if (type.contains("mp4")) {
            type = "video/*";
        }
        LogUtil.d(TAG, "onShowFileChooser 执行type=" + type);

        try {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            i.setType(type);
            startActivityForResult(Intent.createChooser(i, "选择图片/视频"), FILE_CHOOSER_RESULT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show("无法选择文件");
            fileCallback.onReceiveValue(null);
            fileCallback = null;
        }
    }

    /**
     * 长按保存图片
     */
    private void handleLongClick() {
        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                if (null == hitTestResult) return false;
                int type = hitTestResult.getType();
                LogUtil.d(TAG, "onLongClick type=" + type);
                // 如果是图片类型或者是带有图片链接的类型
                if (type == WebView.HitTestResult.IMAGE_TYPE ||
                        type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    AlertDialogView dialogView = new AlertDialogView(webView.getContext())
                            .setContent("保存到相册")//
                            .setRightBtn("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String pic = hitTestResult.getExtra();//获取图片
                                    LogUtil.d(TAG, "onLongClick  图片url=" + pic);
                                    if (TextUtils.isEmpty(pic)) {
                                        return;
                                    }
                                    new FileDownloader(mContext)
                                            .downloadFile(pic, new FileDownloader.DownloadCallback() {
                                                @Override
                                                public void success(File file, String url) {
                                                    ToastUtil.show("保存成功");
                                                }

                                                @Override
                                                public void fail(String url) {
                                                    ToastUtil.show("保存失败");
                                                }
                                            });
                                }
                            })
                            .setLeftBtn("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                    new EffectDialogBuilder(webView.getContext())
                            .setCancelable(false)
                            .setCancelableOnTouchOutside(false)
                            .setContentView(dialogView).show();

                    return true;
                }
                return false; //保持长按复制文字
            }
        };
        webView.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //上传图片或视频
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == fileCallback) return;
            try {
                Uri[] results = null;
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String dataString = data.getDataString();
                    ClipData clipData = data.getClipData();
                    LogUtil.d(TAG, "onShowFileChooser,dataString=" + dataString + ",clipData=" + clipData);
                    if (!TextUtils.isEmpty(dataString)) {
                        results = new Uri[]{Uri.parse(dataString)};
                    } else if (clipData != null && clipData.getItemCount() > 0) {
                        results = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            results[i] = item.getUri();
                        }
                    }
                }


                LogUtil.d(TAG, "onShowFileChooser,results=" + results);
                fileCallback.onReceiveValue(results);
                fileCallback = null;
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.show("选择文件失败");
                fileCallback.onReceiveValue(null);
                fileCallback = null;
            }
        }
    }


    /**
     * 主要帮助WebView处理各种通知、请求事件的
     */
    private final int LOAD_START = 1;
    private final int LOAD_ERROR = 2;
    private final int LOAD_FINISHED = 3;
    private int loadStatus = LOAD_FINISHED;

    private class DWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            progressBar.setProgress(view.getProgress());
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            loadStatus = LOAD_START;
            progressBar.setProgress(1);
            progressBar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setProgress(100);
            progressBar.setVisibility(View.GONE);
            if (loadStatus != LOAD_ERROR) {
                loadingBar.setLoadingStatus(LoadingBar.LoadingStatus.SUCCESS);
            }
            loadStatus = LOAD_FINISHED;
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            loadStatus = LOAD_ERROR;
            //显示上层的错误页面
            if (!NetworkUtils.isAvailable()) {
                loadingBar.setLoadingStatus(LoadingBar.LoadingStatus.NOCONNECTION, R.drawable.icon_fail);
            } else {
                loadingBar.setLoadingStatus(LoadingBar.LoadingStatus.RELOAD, R.drawable.icon_fail);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUrl(url);
        }
    }

    /**
     * 下载
     *
     * @author zhangquan
     */
    private class DWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent,
                                    String contentDisposition, String mimetype, long contentLength) {
            try {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public boolean onBackPressedSupport() {
        return handleBackEvent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWebView();
    }

    private void releaseWebView() {
        try {
            if (null != webView) {
                webView.setVisibility(View.GONE);
                webView.removeAllViews();
                webView.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //------------------------------url handle
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMsg eventMsg) {
        if (TextUtils.equals(eventMsg.getAction(), Constant.Event.LOGIN_SUCCESS)) {
            loginCallback();
        }
    }

    private static final String URL_SHARE = "xkd://app/share?param="; //分享
    private static final String URL_GETTOKEN = "xkd://user/getToken";//获取token
    private static final String URL_USERINFO = "xkd://user/info";//获取用户信息
    private static final String URL_LOGIN = "xkd://user/login"; //登录
    private static final String URL_OPENPAGE = "xkd://app/appPage?param=";//打开原生页面
    private static final String URL_CLOSEPAGE = "xkd://app/closepage";//关闭页面
    private static final String URL_OPENURL = "xkd://app/openURL";//通过浏览器打开
    private static final String URL_CLIPBOARD = "xkd://app/clipboard"; //复制到剪贴板
    private static final String URL_CLIPBOARD_READ = "xkd://app/readclipboard"; //读取剪贴板
    private static final String URL_NOTIFICATION_ENABLED = "xkd://app/notification/status";//获取通知状态  1开启 0关闭
    private static final String URL_NOTIFICATION_OPEN = "xkd://app/notification/setting";//打开通知设置界面
    private static final String URL_SAVE_PIC = "xkd://app/storage/imgs"; //保存图片
    private static final String URL_STATUSBAR = "xkd://app/statusbar"; //修改状态栏
    private static final String URL_H5_ADJUMP = "xkd://app/nativeAdJump";//h5广告跳转
    private static final String URL_SAVE_PHOTOS = "xkd://savedPhotosAlbum";


    private static final String URL_WEIXIN = "weixin://"; //打开微信
    private static final String URL_OPEN_ALIPAY = "alipays://"; //打开支付宝
    private static final String URL_VPAGE = "vipshop://";//唯品会
    private static final String URL_TPOPEN = "tbopen://";//淘宝t
    private static final String URL_TAOBAO = "taobao://";//淘宝
    private static final String URL_PDD = "pinduoduo://";//拼多多
    private static final String URL_BDISK = "bdnetdisk://";//百度网盘
    private static final String URL_TIANMAO = "tmall://";//天猫
    private static final String URL_MEITUAN = "imeituan://";//美团
    private static final String URL_QQ = "wtloginmqq://";//QQ
    private static final String URL_SUNING = "suning://";//苏宁


    private boolean handleUrl(String path) {
        LogUtil.d(TAG, "shouldOverrideUrlLoading ,url=" + path);

        //拼多多
        if (path.startsWith(URL_PDD)) {
            openUrl(path, "打开失败，检查是否安装拼多多APP ");
            return true;
        }

        //唯品会
        if (path.startsWith(URL_VPAGE)) {
            openUrl(path, "打开失败，检查是否安装唯品会APP");
            return true;
        }

        //打开淘宝
        if (path.startsWith(URL_TPOPEN)) {
            openUrl(path, "打开失败，检查是否安装淘宝APP");
            return true;
        }

        if (path.startsWith(URL_TAOBAO)) {
            openUrl(path, "打开失败，检查是否安装淘宝APP");
            return true;
        }

        //打开苏宁
        if (path.startsWith(URL_SUNING)) {
            openUrl(path, "");
            return true;
        }


        //拨打电话
        if (path.startsWith("tel")) {
            try {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(path)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }


        //打开支付宝
        if (path.startsWith(URL_OPEN_ALIPAY)) {
            openUrl(path, "打开失败，检查是否安装支付宝App");
            return true;
        }


        //跳转到微信
        if (path.startsWith(URL_WEIXIN)) {
            openUrl(path, "打开失败，检查是否安装微信App");
            return true;
        }

        //百度网盘
        if (path.startsWith(URL_BDISK)) {
            openUrl(path, "打开失败，检查是否安装百度网盘APP");
            return true;
        }

        if (path.startsWith(URL_MEITUAN)) {
            openUrl(path, "打开失败，检查是否安装美团APP (非美团外卖APP) ");
            return true;
        }
        if (path.startsWith(URL_QQ)) {
            openUrl(path, "打开失败");
            return true;
        }
        if (path.startsWith(URL_SUNING)) {
            openUrl(path, "");
            return true;
        }

        //自定义协议
        if (!path.startsWith(LContext.getString(R.string.app_schema))) {
            return false;
        }
        try {
            String param = Uri.parse(path).getQueryParameter("param");
            if (path.startsWith(URL_SHARE)) {
                share(param);
            } else if (path.startsWith(URL_GETTOKEN)) {
                loginCallback();
            } else if (path.startsWith(URL_USERINFO)) {
                userInfoCallback();
            } else if (path.startsWith(URL_LOGIN)) {
                login();
            } else if (path.startsWith(URL_OPENURL)) { //打开浏览器
                openUrl(param, "打开失败");
            } else if (path.startsWith(URL_OPENPAGE)) { //打开APP页面
                DispatchUtil.goToPage(mContext, param);
                if (!TextUtils.isEmpty(param)) {
                    JSONObject jsonObject = new JSONObject(param);
                    int closepage = jsonObject.optInt("closepage");
                    if (closepage == 1) {
                        close();
                    }
                }
            } else if (path.startsWith(URL_CLOSEPAGE)) { //关闭页面
                close();
            } else if (path.startsWith(URL_CLIPBOARD)) { //复制到剪贴板
                try {
                    ClipboardManager cm = (ClipboardManager) LContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(param);
                    ToastUtil.show("复制成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.show("复制失败，请长按文字复制");
                }
            } else if (path.startsWith(URL_CLIPBOARD_READ)) { //读取剪贴板
                readClipboard();
            } else if (path.startsWith(URL_NOTIFICATION_ENABLED)) { //app是否开启通知权限
                notificationCallback();
            } else if (path.startsWith(URL_NOTIFICATION_OPEN)) { //跳转到app通知权限页面
                NotificationPageHelper.openNotificationSetting(mContext);
            } else if (path.startsWith(URL_SAVE_PIC)) { //保存图片到相册
                if (!TextUtils.isEmpty(param)) {
                    LoadingDialog loadingDialog = LoadingDialog.showCancelableDialog(mContext, "图片保存中");
                    new FileDownloader(mContext).downloadFile(param, new FileDownloader.DownloadCallback() {
                        @Override
                        public void success(File file, String url) {
                            loadingDialog.dismiss();
                            ToastUtil.show("保存成功");
                        }

                        @Override
                        public void fail(String url) {
                            loadingDialog.dismiss();
                            ToastUtil.show("保存失败");
                        }
                    });
                } else {
                    ToastUtil.show("图片地址为空");
                }
            } else if (path.startsWith(URL_STATUSBAR)) { //修改状态栏
                boolean lightModel = TextUtils.equals(param, "1");
                StatusBar.setStatusBar(mContext, lightModel, titleBarView);
            } else if (path.startsWith(URL_SAVE_PHOTOS)) {
                savePics(param);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 保存图片或视频到相册
     *
     * @param param
     */
    private void savePics(String param) {
        JSONObject jsonObject = null;
        try {
            List<String> saveFiles = new ArrayList<>();

            jsonObject = new JSONObject(param);

            JSONArray jsonElements = jsonObject.optJSONArray("videos");
            if (jsonElements != null && jsonElements.length() > 0) {
                for (int i = 0; i < jsonElements.length(); i++) {
                    saveFiles.add(jsonElements.get(i).toString());
                }
            }
            jsonElements = jsonObject.optJSONArray("images");
            if (jsonElements != null && jsonElements.length() > 0) {
                for (int i = 0; i < jsonElements.length(); i++) {
                    saveFiles.add(jsonElements.get(i).toString());
                }
            }

            if (!saveFiles.isEmpty()) {
                FileDownloadDialogView dialogView = new FileDownloadDialogView(mContext, saveFiles, "素材下载");
                new EffectDialogBuilder(mContext)
                        .setContentView(dialogView)
                        .setCancelable(true)
                        .show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openUrl(String param, String msg) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(param));
            mContext.startActivity(intent);
            LogUtil.e("url=" + param + "," + intent.resolveActivity(getActivity().getPackageManager()));
        } catch (Exception e) {
            e.printStackTrace();
            if (!TextUtils.isEmpty(msg)) {
                ToastUtil.show(msg);
            }
        }
    }

    /**
     * 登录
     */
    public void login() {
//        LoginFragment.Companion.start(mContext);
    }

    /**
     * 登录回调
     */
    private void loginCallback() {
//        String token = UserClient.getToken();
//        webView.loadUrl("javascript:userLoginStatus('" + token + "')");
    }

    /**
     * 获取用户信息回调
     */
    private void userInfoCallback() {
//        UserEntity user = UserClient.getUser();
//        String userInfo = "";
//        if (null != user) {
//            userInfo = new Gson().toJson(user);
//        }
//        webView.loadUrl("javascript:userInfoCallback('" + userInfo + "')");
    }

    private void viewWillAppear() {
        webView.loadUrl("javascript:viewWillAppearEvent()");
    }

    private void notificationCallback() {
        int status = NotificationPageHelper.areNotificationsEnabled(mContext) ? 1 : 0;
        webView.loadUrl("javascript:notificationEnabled('" + status + "')");
    }

    /**
     * 读取剪贴板
     */
    private void readClipboard() {
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence text = "";
        ClipData clipData = cm.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            text = clipData.getItemAt(0).getText();
        }
        if (!TextUtils.isEmpty(text)) {
            String content = text.toString();
            String[] test = content.split("\n");
            StringBuffer sb = new StringBuffer();
            for (String line : test) {
                sb.append(line).append("\\n");
            }
            webView.loadUrl("javascript:readClipboard('" + sb.toString() + "')");
            CommonUtil.addToClipboard(null);
        }
    }

    /**
     * 分享
     */
    public void share(String json) throws Exception {
        if (TextUtils.isEmpty(json)) return;
        //{"title":"分享标题","content":"分享内容","url":"分享链接","pic_url":"分享图片链接"}

        ShareInfo shareInfo = new ShareInfo();
        JSONObject jsonObject = new JSONObject(json);
        shareInfo.title = jsonObject.optString("title");
        shareInfo.content = jsonObject.optString("content");
        shareInfo.url = jsonObject.optString("url");
        shareInfo.pic_url = jsonObject.optString("pic_url");

        ShareDialog.share(getActivity(), shareInfo);
    }
}
