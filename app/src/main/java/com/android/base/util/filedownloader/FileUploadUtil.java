package com.android.base.util.filedownloader;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.util.ext.ToastUtil;
import com.android.base.data.DataConfig;
import com.android.base.data.RestClient;

import org.json.JSONObject;

import java.io.File;

import common.widget.dialog.LoadingDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 上传文件
 *
 * @author 张全
 */
public class FileUploadUtil {
    @SuppressLint("CheckResult")
    public static void uploadFile(Context ctx, File file,FileType fileType) {
        LoadingDialog loadingDialog = LoadingDialog.showCancelableDialog(ctx, "请稍候...");
        Observable.create(new ObservableOnSubscribe<Response>() {
            @Override
            public void subscribe(ObservableEmitter<Response> emitter) throws Exception {
                String url = DataConfig.API_HOST + "";
                String media_type =fileType.type;
                MultipartBody uploadBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("save_type", type)
                        .addFormDataPart("files", file.getName(), RequestBody.create(MediaType.parse(media_type), file))
                        .build();
                Request req = new Request.Builder().url(url).post(uploadBody).build();
                Call call = RestClient.getHttpClient().newCall(req);
                Response response = call.execute();
                emitter.onNext(response);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response>() {
                    @Override
                    public void accept(Response response) throws Exception {
                        loadingDialog.dismiss();
                        String string = response.body().string();
                        JSONObject jsonObject = new JSONObject(string);
                        String data = jsonObject.optString("data");
                        ToastUtil.show(data);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        loadingDialog.dismiss();
                        ToastUtil.show("上传失败，请重试");
                    }
                });
    }
}
