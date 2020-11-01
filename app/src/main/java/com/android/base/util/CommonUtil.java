package com.android.base.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.android.util.LContext;
import com.android.util.log.LogUtil;

import java.io.File;

/**
 * @author 张全
 */
public class CommonUtil {

    /**
     * 保存文件到相册
     *
     * @param ctx
     * @param file
     */
    public static void saveFileToGallery(Context ctx, File file) {
        if (null == file) return;
        try {
            MediaStore.Images.Media.insertImage(ctx.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
            LogUtil.d("gallery", "保存相册成功");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("gallery", "保存相册失败 e=" + e.getMessage());
        }

        // 通知图库更新11
        try {
            ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("gallery", "通知图库更新失败 111 e=" + e.getMessage());
        }

        // 通知图库更新22
        try {
            String[] paths = new String[]{file.getAbsolutePath()};
            MediaScannerConnection.scanFile(ctx, paths, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(uri);
                    ctx.sendBroadcast(mediaScanIntent);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("gallery", "通知图库更新失败 222 e=" + e.getMessage());
        }
    }

    public static void clearClipboard() {
        ClipboardManager manager = (ClipboardManager) LContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(ClipData.newPlainText(null, null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addToClipboard(String text) {
        try {
            ClipboardManager cm = (ClipboardManager) LContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String checkUrl(String url) {
        if (!TextUtils.isEmpty(url) && !url.contains("http")) {
            if (url.startsWith("//")) {
                url = "https:" + url.subSequence(1, url.length());
            } else if (url.startsWith("/")) {
                url = "https:/" + url.subSequence(1, url.length());
            } else {
                url = "https://" + url;
            }
        }
        return url;
    }
}
