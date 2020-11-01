package com.android.base.util.share;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;

import com.android.base.widget.BottomInDialog;
import com.fastapp.R;
import com.umeng.socialize.bean.SHARE_MEDIA;

/**
 * 分享弹框
 *
 * @author 张全
 */
public class ShareDialog {

    public static void share(Activity ctx, ShareInfo shareInfo) {
        if (null == shareInfo) return;
        share(ctx, null, shareInfo);
    }

    public static void share(Activity ctx, Bitmap bitmap) {
        if (null == bitmap) return;
        share(ctx, bitmap, null);
    }

    public static void share(Activity ctx, Bitmap bitmap, ShareInfo shareInfo) {
        BottomInDialog bottomInDialog = new BottomInDialog(ctx);
        View view = LayoutInflater.from(ctx).inflate(R.layout.share_dialog, null);
        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SHARE_MEDIA media = SHARE_MEDIA.WEIXIN;
                switch (v.getId()) {
                    case R.id.ll_wx:
                        media = SHARE_MEDIA.WEIXIN;
                        break;
                    case R.id.ll_circle:
                        media = SHARE_MEDIA.WEIXIN_CIRCLE;
                        break;
                    case R.id.ll_qq:
                        media = SHARE_MEDIA.QQ;
                        break;
                    case R.id.ll_wb:
                        media = SHARE_MEDIA.SINA;
                        break;
                }
                if (null != bitmap) {
                    PlatformShareUtil.shareSingleImg(ctx, bitmap, media);
                } else if (null != shareInfo) {
                    UmengShareUtil.share(ctx, shareInfo.url, shareInfo.title, shareInfo.content, shareInfo.pic_url, media, new SimpleUmengShareListener());
                }
                bottomInDialog.dismiss();
            }
        };
        view.findViewById(R.id.ll_wx).setOnClickListener(clickListener);
        view.findViewById(R.id.ll_circle).setOnClickListener(clickListener);
        view.findViewById(R.id.ll_qq).setOnClickListener(clickListener);
        view.findViewById(R.id.tv_cancel).setOnClickListener(v -> bottomInDialog.dismiss());
        view.findViewById(R.id.container).setOnClickListener(v -> bottomInDialog.dismiss());
        bottomInDialog.setContentView(view);
        bottomInDialog.show();
    }
}
