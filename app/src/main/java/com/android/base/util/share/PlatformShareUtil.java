package com.android.base.util.share;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.android.base.Constant;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台分享(无需注册app)
 * -- shareImgs
 * --shareSingleImg
 * --shareMutiFiles
 * --shareFile
 */
public class PlatformShareUtil {
    public static File storageDir = new File(Environment.getExternalStorageDirectory(), Constant.SD_DIR + "/share");
    public static File childFile;
    public static int i;


    /**
     * 分享多张图片
     */
    public static boolean shareImgs(Context ctx, List<Bitmap> bitmapList, SHARE_MEDIA shareMedia) {
        if (!SharePlatformUtil.checkExist(ctx, shareMedia)) {
            return false;
        }
        if (shareMedia == SHARE_MEDIA.WEIXIN_CIRCLE) {
            return PlatformShareUtil.shareSingleImage(ctx, bitmapList.get(0), shareMedia);
        } else {
            return PlatformShareUtil.shareImages(ctx, bitmapList, shareMedia);
        }
    }

    /**
     * 分享单张图片
     */
    public static boolean shareSingleImg(Context ctx, Bitmap bitmap, SHARE_MEDIA shareMedia) {
        if (!SharePlatformUtil.checkExist(ctx, shareMedia)) {
            return false;
        }
        return PlatformShareUtil.shareSingleImage(ctx, bitmap, shareMedia);
    }


    /**
     * 分享 多个文件(图片或视频)
     */
    public static boolean shareMutiFiles(Context ctx, List<File> fileList, SHARE_MEDIA shareMedia) {
        if (!SharePlatformUtil.checkExist(ctx, shareMedia)) {
            return false;
        }
        if (shareMedia == SHARE_MEDIA.WEIXIN_CIRCLE) {
            return PlatformShareUtil.shareSingleFile(ctx, fileList.get(0), shareMedia);
        } else {
            return PlatformShareUtil.shareFiles(ctx, fileList, shareMedia);
        }
    }

    /**
     * 分享 单个文件(图片或视频)
     */
    public static boolean shareFile(Context ctx, File file, SHARE_MEDIA shareMedia) {
        if (!SharePlatformUtil.checkExist(ctx, shareMedia)) {
            return false;
        }
        return PlatformShareUtil.shareSingleFile(ctx, file, shareMedia);
    }

    /**
     * 分享单个视频或图片
     */
    private static boolean shareSingleFile(Context ctx, File file, SHARE_MEDIA shareMedia) {
        Intent intent = new Intent();
        intent.setComponent(SharePlatformUtil.getComponent(shareMedia));
        intent.setAction(Intent.ACTION_SEND);
        Uri imageUri;
        if (file.getAbsolutePath().endsWith(".mp4")) {
            imageUri = getVideoContentUri(ctx, file);
            intent.setType("video/*");
        } else {
            imageUri = getImageContentUri(ctx, file);
            intent.setType("image/*");
        }
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        try {
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 分享多个视频或文件
     */
    private static boolean shareFiles(Context ctx, List<File> files, SHARE_MEDIA shareMedia) {
        Intent intent = new Intent();
        intent.setComponent(SharePlatformUtil.getComponent(shareMedia));
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);

        boolean shareImg = true;
        ArrayList<Uri> imageUris = new ArrayList<>();
        int i = 0;
        for (File file : files) {
            i++;
            if (i > 9) {
                break;
            }
            if (file.getAbsolutePath().endsWith(".mp4")) {
                shareImg = false;
                imageUris.add(getVideoContentUri(ctx, file));
            } else {
                imageUris.add(getImageContentUri(ctx, file));
            }

        }

        if (shareImg) {
            intent.setType("image/*");
        } else {
            intent.setType("video/*");
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        try {
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 分享bitmap
     */
    private static boolean shareSingleImage(Context mContext, Bitmap bitmap, SHARE_MEDIA shareMedia) {
        deletePic(storageDir);
        childFile = new File(storageDir, System.currentTimeMillis() + "");
        if (!childFile.exists()) {
            childFile.mkdir();
        }
        i = 0;
        Uri file = saveImageToSdCard(mContext, bitmap);
        Intent intent = new Intent();
        intent.setComponent(SharePlatformUtil.getComponent(shareMedia));
        intent.setAction(Intent.ACTION_SEND);
        intent.addCategory(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, file);
        try {
            mContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private static boolean shareImages(Context mContext, List<Bitmap> bitmapList, SHARE_MEDIA shareMedia) {
        deletePic(storageDir);
        childFile = new File(storageDir, System.currentTimeMillis() + "");
        if (!childFile.exists()) {
            childFile.mkdir();
        }
        i = 0;
        ArrayList<Uri> uris = new ArrayList<>();
        for (int i = 0; i < ((bitmapList.size() >= 9) ? 9 : bitmapList.size()); i++) {
            Uri file = saveImageToSdCard(mContext, bitmapList.get(i));
            if (file != null) {
                uris.add(file);
            }
        }

        Intent intent = new Intent();
        intent.setComponent(SharePlatformUtil.getComponent(shareMedia));
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        try {
            mContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 将bitmap保存到本地
     *
     * @param bitmap bitmap
     * @return 文件
     */
    private static Uri saveImageToSdCard(Context mContext, Bitmap bitmap) {
        boolean success = false;
        File file = null;
        try {
            file = createStableImageFile();
            FileOutputStream outStream;
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                MediaScannerConnection.scanFile(mContext, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(uri);
                        mContext.sendBroadcast(intent);
                    }
                });
            } else {
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(storageDir.getAbsoluteFile())));
            }
        }
        if (success) {
            return getImageContentUri(mContext, file);
        } else {
            return null;
        }
    }

    /**
     * 创建本地保存路径
     *
     * @return 文件
     */
    private static File createStableImageFile() {
        i++;
        String imageFileName = System.currentTimeMillis() + ".png";
        if (childFile.exists()) {
            childFile.delete();
        }
        childFile.mkdirs();
        File image = new File(childFile, imageFileName);
        return image;
    }

    /**
     * 删除文件里面的内容
     *
     * @param storageDir
     */
    private static void deletePic(File storageDir) {
        if (storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();

            for (int j = 0; j < files.length; j++) {
                File f = files[j];
                deletePic(f);
            }
        } else {
            storageDir.delete();
        }
    }

    private static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        Uri uri = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }

            cursor.close();
        }

        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

        return uri;
    }

    private static Uri getVideoContentUri(Context context, File videoFile) {
        Uri uri = null;
        String filePath = videoFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/video/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }

            cursor.close();
        }

        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }

        return uri;
    }
}
