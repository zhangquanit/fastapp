package com.android.base.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.Target;

/**
 * Glide工具类
 */
public class GlideUtil {

    /**
     * 加载本地图片
     */
    public static void loadLocalPic(ImageView imageView, String path) {
        try {
            Glide.with(imageView.getContext())
                    .load(path)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载本地图片
     */
    public static void loadLocalPic(ImageView imageView, int res) {
        try {
            Glide.with(imageView.getContext())
                    .load(res)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载图片
     */
    public static void loadPic(ImageView imageView, String url) {
        try {
            url = checkUrl(url);
            Glide.with(imageView.getContext())
                    .load(url)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载图片
     */
    public static void loadPic(ImageView imageView, String url, @DrawableRes int errorRes, @DrawableRes int placeHolderRes) {
        try {
            url = checkUrl(url);
            Glide.with(imageView.getContext())
                    .load(url)
                    .apply(new RequestOptions().error(errorRes).placeholder(placeHolderRes)
                    )
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载原始图片
     */
    public static void loadOriginPic(ImageView imageView, String url, @DrawableRes int errorRes, @DrawableRes int placeHolderRes) {
        try {
            url = checkUrl(url);
            Glide.with(imageView.getContext())
                    .load(url)
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).error(errorRes).placeholder(placeHolderRes)
                    )
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载圆角图片
     *
     * @param dp
     */
    public static void loadRoundPic(ImageView imageView, String url, @DrawableRes int errorRes, @DrawableRes int placeHolderRes, int dp) {
        try {
            url = checkUrl(url);
            //设置图片圆角角度
            RequestOptions roundOptions = new RequestOptions()
                    .error(errorRes)
                    .centerCrop()
                    .placeholder(placeHolderRes)
                    .transform(new GlideRoundTransform(imageView.getContext(), dp));
            Glide.with(imageView.getContext())
                    .load(url)
                    .apply(roundOptions)
                    .thumbnail(loadTransform(imageView.getContext(), errorRes, dp))
                    .thumbnail(loadTransform(imageView.getContext(), placeHolderRes, dp))
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载原始图片bitmap，适用于需要动态适配View宽高
     *
     * @param viewTarget SimpleTarget、BitmapImageViewTarget
     */
    public static void loadBitmap(Context ctx, String url, BaseTarget<Bitmap> viewTarget) {
        try {
            Glide.with(ctx)
                    .asBitmap()
                    .load(url)
                    .into(viewTarget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static RequestBuilder<Drawable> loadTransform(Context context, @DrawableRes int placeholderId, int radius) {
        return Glide.with(context)
                .load(placeholderId)
                .apply(new RequestOptions().centerCrop()
                        .transform(new GlideRoundTransform(context, radius)));
    }


    public static String checkUrl(String url) {
        return CommonUtil.checkUrl(url);
    }
}
