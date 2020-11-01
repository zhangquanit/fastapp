package com.android.base.util.share;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.android.util.LContext;
import com.android.util.ext.ToastUtil;
import com.android.base.data.RestClient;
import com.fastapp.R;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 友盟分享
 */
public class UmengShareUtil {

    /**
     * 分享
     *
     * @param activity 上下文
     * @param url      分享的连接
     * @param title    分享的title
     * @param content  分享的内容
     * @param imgRes   bitmap
     *                 回调监听
     */
    public static void share(Activity activity, String url, String title, String content, String imgRes, SHARE_MEDIA shareMedia) {
        share(activity, url, title, content, imgRes, shareMedia, new UMShareListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onResult(SHARE_MEDIA share_media) {
                ToastUtil.show("分享成功");
            }

            @Override
            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                ToastUtil.show("分享失败");
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {
                ToastUtil.show("取消分享");
            }
        });
    }

    @SuppressLint("CheckResult")
    public static void share(Activity ctx, String url, String title, String content, String imgRes, SHARE_MEDIA shareMedia, UMShareListener umShareListener) {
        if (!SharePlatformUtil.checkExist(ctx, shareMedia)) {
            return;
        }

        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            UMImage image;
            if (!TextUtils.isEmpty(imgRes)) {
                // 小程序消息封面图片
                image = new UMImage(ctx, getHtmlByteArray(imgRes));//网络图片
            } else {
                // 小程序消息封面图片
                image = new UMImage(ctx, R.mipmap.ic_launcher);//
            }
            image.compressStyle = UMImage.CompressStyle.SCALE;
            image.compressFormat = Bitmap.CompressFormat.PNG;
            ShareAction shareAction = new ShareAction(ctx);
            shareAction.setPlatform(shareMedia);
            if (TextUtils.isEmpty(url) && TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
                image.setThumb(image);
                shareAction.withMedia(image);
            } else {
                UMWeb web = new UMWeb(url);
                web.setTitle(title);//标题
                web.setThumb(image);  //缩略图
                web.setDescription(content);//描述
                shareAction.withMedia(web);
            }
            shareAction
                    .setCallback(umShareListener)
                    .share();
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Functions.emptyConsumer(), new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }


    /**
     * 分享
     *
     * @param activity 上下文
     * @param title    分享的title
     *                 bitmap
     *                 回调监听
     */
    public static void shareImg(Activity activity, String title, Bitmap bitmap, SHARE_MEDIA shareMedia) {
        if (!SharePlatformUtil.checkExist(activity, shareMedia)) {
            return;
        }
        UMImage image;
        if (bitmap != null) {
            // 截图
            image = new UMImage(activity, bitmap);//网络图片
        } else {
            // 小程序消息封面图片
            image = new UMImage(activity, R.mipmap.ic_launcher);//网络图片
        }
        image.compressStyle = UMImage.CompressStyle.SCALE;
        image.compressFormat = Bitmap.CompressFormat.PNG;
        new ShareAction(activity).setPlatform(shareMedia)
                .withText(title)
                .withMedia(image)
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onResult(SHARE_MEDIA share_media) {
                        ToastUtil.show("分享成功");
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                        ToastUtil.show("分享失败");
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media) {
                        ToastUtil.show("取消分享");
                    }
                })
                .share();
    }


    private static byte[] getHtmlByteArray(final String url) {
        InputStream inStream;
        byte[] data;
        try {
            Response response = RestClient.getHttpClient().newCall(new Request.Builder().url(url).build()).execute();
            inStream = response.body().byteStream();
            data = inputStreamToByte(inStream);
        } catch (Exception e) {
            data = bmpToByteArray(BitmapFactory.decodeResource(LContext.getContext().getResources(), R.mipmap.ic_launcher), true);
        }
        return data;
    }

    private static byte[] inputStreamToByte(InputStream is) {
        try {
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                bytestream.write(ch);
            }
            byte imgdata[] = bytestream.toByteArray();
            bytestream.close();
            return imgdata;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}

