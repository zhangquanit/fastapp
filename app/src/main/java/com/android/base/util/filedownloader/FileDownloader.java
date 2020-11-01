package com.android.base.util.filedownloader;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.android.util.encode.MD5;
import com.android.util.log.LogUtil;
import com.android.base.data.RestClient;
import com.android.base.util.CommonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 文件下载器
 *
 * @author 张全
 */
public class FileDownloader {
    private static final String TAG = "FileDownloader";
    private static final String DIR="files";
    private DownloadHandler downloadHandler = new DownloadHandler();
    private DownloadCallback callback;
    private Context ctx;
    private boolean exit;
    private boolean saveToGallery; //自动保存到相册

    public FileDownloader(Context ctx) {
        this(ctx, true);
    }

    public FileDownloader(Context ctx, boolean saveToGallery) {
        this.ctx = ctx;
        this.saveToGallery = saveToGallery;
    }

    private File getImgDir() {
        File fileDir = new File(Environment.getExternalStorageDirectory(), DIR);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir;
    }

    /**
     * 下载文件
     *
     * @param url
     * @param callback
     */
    public void downloadFile(String url, DownloadCallback callback) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        List<String> urls = new ArrayList<>();
        urls.add(url);
        downloadFile(urls, callback);
    }

    /**
     * 下载文件
     *
     * @param urls
     * @param callback
     */
    public void downloadFile(List<String> urls, DownloadCallback callback) {
        if (null == urls || urls.isEmpty()) return;
        this.callback = callback;
        for (String url : urls) {
            File imgDir = getImgDir();
            String fileName = MD5.MD5Encode(url);
            if (url.contains(".mp4")) {
                fileName += ".mp4";
            } else {
                fileName += ".png";
            }
            File file = new File(imgDir, fileName);
            if (file.exists() && file.length() > 0) { //已下载完成的
                success(url, file);
            } else {
                if (url.startsWith("http")) {
                    downLoadFile(url, file);
                } else {
                    fail(url);
                }
            }
        }
    }

    /**
     * 结束
     */
    public void stop() {
        this.exit = true;
        this.ctx = null;
        callback = null;
        downloadHandler.removeAllMsg();
    }

    private boolean isExit() {
        return exit || null == callback;
    }

    private void success(String url, File file) {
        if (saveToGallery && null != file && null != ctx) {
            CommonUtil.saveFileToGallery(ctx, file);
        }
        if (isExit()) return;
        callback.success(file, url);
    }

    private void fail(String url) {
        if (isExit()) return;
        callback.fail(url);
    }

    private void downLoadFile(String url, File file) {
        final Request request = new Request.Builder().url(url).build();
        final Call call = RestClient.getHttpClient().newCall(request);


        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (isExit()) return;
                downloadHandler.sendFail(url, file);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (isExit()) return;
                if (response.isSuccessful()) {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len;
                    FileOutputStream fos = null;
                    try {
                        is = response.body().byteStream();
                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                        fos.flush();
                        downloadHandler.sendSuccess(url, file);
                    } catch (Exception e) {
                        LogUtil.e(TAG, e.toString());
                        downloadHandler.sendFail(url, file);
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (Exception e) {
                            LogUtil.e(TAG, e.toString());
                        }
                    }
                } else {
                    downloadHandler.sendFail(url, file);
                }
            }
        });
    }

    private class DownloadHandler extends Handler {
        private int msg_success = 1;
        private int msg_fail = 2;

        public DownloadHandler() {
            super(Looper.getMainLooper());
        }

        public void sendSuccess(String url, File file) {
            Message message = obtainMessage(msg_success);
            message.obj = new FileDownloader.FileEntity(url, file);
            sendMessage(message);
        }

        public void sendFail(String url, File file) {
            if (null != file && file.length() > 0) file.delete(); //删除下载失败的文件
            Message message = obtainMessage(msg_fail);
            message.obj = new FileDownloader.FileEntity(url, null);
            sendMessage(message);
        }

        @Override
        public void handleMessage(Message msg) {
            FileDownloader.FileEntity fileEntity = (FileDownloader.FileEntity) msg.obj;
            if (msg.what == msg_success) {
                success(fileEntity.url, fileEntity.file);
            } else {
                fail(fileEntity.url);
            }
        }

        public void removeAllMsg() {
            removeMessages(msg_fail);
            removeMessages(msg_success);
        }
    }

    private static class FileEntity implements Serializable {
        public String url;
        public File file;

        public FileEntity(String url, File file) {
            this.url = url;
            this.file = file;
        }
    }

    public static interface DownloadCallback {
        void success(File file, String url);

        void fail(String url);
    }
}
