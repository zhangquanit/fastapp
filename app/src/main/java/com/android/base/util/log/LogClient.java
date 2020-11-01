package com.android.base.util.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.android.util.LContext;
import com.android.base.Constant;
import com.blankj.utilcode.util.FileIOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 本地日志采集和上报
 */
public class LogClient {
    private static final SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static File DIR = new File(Environment.getExternalStorageDirectory(), Constant.SD_DIR + "/log");
    private static final int max = 5; //保留最近5天的日志
    private static List<String> logUrls = new ArrayList<>();

    static {

         //需要采集返回数据的http接口
//        logUrls.add(ApiHost.GOODS_DECODE);
//        logUrls.add(ApiHost.PROMOTION_LINK);
//        logUrls.add(ApiHost.GOODS_DETAIL);
//        logUrls.add(ApiHost.GOODS_DETAIL_DESC);


        if (!DIR.exists()) {
            DIR.mkdirs();
        }

        //只保留最近max天的日志
        Calendar calendar = Calendar.getInstance();
        List<String> filterNames = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            calendar.add(Calendar.DATE, -i);
            filterNames.add(getFileName(calendar.getTime()));
        }

        File[] files = DIR.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !filterNames.contains(name);
            }
        });

        if (null != files) {
            for (File file : files) {
                file.delete();
            }
        }

    }

    public static void log(String tag, String info) {
//        if (App.devEnv) {
//            return;
//        }
        try {
            String logInfo = new LogInfo(tag, info).toString();
            String fileName = getFileName(new Date());
            File logFile = new File(DIR, fileName);

            //文件第一行是设备信息
            if (!logFile.exists()) {
                String header = "v" + LContext.versionName + " " + Build.BRAND + " " + Build.MODEL + " " + Build.VERSION.RELEASE + " " + AnalysisUtil.networkType(LContext.getContext()) + "\n";
                FileIOUtils.writeFileFromString(logFile, header, true);
            }
            FileIOUtils.writeFileFromString(logFile, logInfo, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFileName(Date date) {
        return DateFormat.format(date) + ".txt";
    }

    //-------------------------------------------------------
    public static void appStart() {
        log("App", "---------启动app");
    }

    public static void appEnd() {
        log("App", "---------退出app");
    }

    public static void clipboardLog(String info) {
        log("Clipboard", info);
    }

    public static void logRequest(String url, Map<String, String> paramMap) {
        if (!isLogRequest(url)) {
            return;
        }

        log("RestClient", "请求url=" + url + ",请求参数=" + paramMap);
    }

    public static void logResponse(String url, String responseMessage) {
        if (!isLogRequest(url)) {
            return;
        }
        log("RestClient", "响应url=" + url + "\n" + responseMessage);
    }

    private static boolean isLogRequest(String url) {
        for (String item : logUrls) {
            if (url.contains(item)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 压缩目录下的文件
     */
    public static File getZipFile() {
        try {
            File[] files = DIR.listFiles();
            if (null == files || files.length == 0) {
                return null;
            }

            File zipFile = new File(Environment.getExternalStorageDirectory() + File.separator + Constant.SD_DIR, DateFormat.format(new Date()) + ".zip");
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

            for (File item : files) {
                InputStream inputStream = new FileInputStream(item);
                zipOutputStream.putNextEntry(new ZipEntry(item.getName()));
                byte[] buffer = new byte[1024];
                int lenth = -1;
                while ((lenth = inputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, lenth);
                }
                inputStream.close();
            }
            zipOutputStream.close();
            return zipFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("CheckResult")
    public static void uploadLog(Context ctx) {
        File zipFile = getZipFile();
        if (null == zipFile) {
            return;
        }

//        LoadingDialog loadingDialog = LoadingDialog.showCancelableDialog(ctx, "请稍候...");
//        Observable.create(new ObservableOnSubscribe<Response>() {
//            @Override
//            public void subscribe(ObservableEmitter<Response> emitter) throws Exception {
//                String url = DataConfig.API_HOST + ApiHost.LOG_UPLOAD;
//                MultipartBody uploadBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("save_type","images")
//                        .addFormDataPart("files", "1.png", RequestBody.create(MediaType.parse("image/*"), new File("/storage/emulated/0/DCIM/Screenshots/Screenshot_20200515_195525.jpg")))
//                        .build();
//                Request req = new Request.Builder().url(url).post(uploadBody).build();
//                Call call = RestClient.getHttpClient().newCall(req);
//                Response response = call.execute();
//                emitter.onNext(response);
//                emitter.onComplete();
//            }
//        }).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Response>() {
//                    @Override
//                    public void accept(Response response) throws Exception {
//                        loadingDialog.dismiss();
//                        String string = response.body().string();
//                        JSONObject jsonObject = new JSONObject(string);
//                        String data = jsonObject.optString("data");
//                        ToastUtil.show(data);
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        throwable.printStackTrace();
//                        loadingDialog.dismiss();
//                        ToastUtil.show("上传失败，请重试");
//                    }
//                });
    }
}
