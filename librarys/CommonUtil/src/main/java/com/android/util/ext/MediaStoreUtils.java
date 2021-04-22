package com.android.util.ext;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 保存到相册
 * <pre>
 *              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
 *                 //适配android Q
 *                 ThreadUtils.runOnSubThread(new Runnable() {
 *                     @Override
 *                     public void run() {
 *                         MediaStoreUtils.saveVideoToMediaStore(AlivcPublishActivity.this, mOutputPath);
 *                     }
 *                 });
 *
 *             } else {
 *                 MediaScannerConnection.scanFile(getApplicationContext(),
 *                         new String[]{mOutputPath}, new String[]{"video/mp4"}, null);
 *             }
 * </pre>
 *
 */
public class MediaStoreUtils {

    private static final String TAG = "MediaStoreUtils";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void saveImgToMediaStore(Context context, String fileName) {

        long startTime = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        String name = startTime + "-photo.jpg";
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri item = resolver.insert(collection, values);

        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(item, "w", null)) {
            // Write data into the pending image.
            BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
            ParcelFileDescriptor.AutoCloseOutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
            BufferedOutputStream bot = new BufferedOutputStream(outputStream);
            byte[] bt = new byte[2048];
            int len;
            while ((len = bin.read(bt)) >= 0) {
                bot.write(bt, 0, len);
                bot.flush();
            }
            bin.close();
            bot.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now that we're finished, release the "pending" status, and allow other apps
        // to view the image.
        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        resolver.update(item, values, null, null);
        //打印写入时间
        Log.i(TAG, "duration : " + (System.currentTimeMillis() - startTime));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void saveVideoToMediaStore(Context context, String fileName) {

        long startTime = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        String name = "";
        int lastSeparatorIndex = -1;
        if (!TextUtils.isEmpty(fileName)){
            lastSeparatorIndex = fileName.lastIndexOf(File.separator);
        }
        if (lastSeparatorIndex>=0&&lastSeparatorIndex<fileName.length()-1){
            name = fileName.substring(lastSeparatorIndex+1);
        }else {
            name = startTime + "-video.mp4";
        }
        values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.IS_PENDING, 1);
        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri item = resolver.insert(collection, values);

        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(item, "w", null)) {
            // Write data into the pending video.
            BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
            ParcelFileDescriptor.AutoCloseOutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
            BufferedOutputStream bot = new BufferedOutputStream(outputStream);
            byte[] bt = new byte[2048];
            int len;
            while ((len = bin.read(bt)) >= 0) {
                bot.write(bt, 0, len);
                bot.flush();
            }
            bin.close();
            bot.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now that we're finished, release the "pending" status, and allow other apps
        // to view the video.
        values.clear();
        values.put(MediaStore.Video.Media.IS_PENDING, 0);
        resolver.update(item, values, null, null);
        //打印写入时间
        Log.i(TAG, "duration : " + (System.currentTimeMillis() - startTime));
    }

}

