package com.android.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.android.base.data.BaseResponseObserver;
import com.android.base.data.ResponseDataObject;
import com.google.gson.Gson;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author 张全
 */
public class BaseAndroidViewModel extends AndroidViewModel {
    public CompositeDisposable compositeDisposable;

    public BaseAndroidViewModel(@NonNull Application application) {
        super(application);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }


    @SuppressLint("AutoDispose")
    protected <T> void executeNoMapHttp(Observable<T> observable, @NonNull BaseResponseObserver<T> observer) {
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(observer);
        if (compositeDisposable != null) {
            compositeDisposable.add(observer);
        }
    }

    @SuppressLint({"AutoDispose", "CheckResult"})
    protected <T> void executeNoMapHttp(Observable<T> observable, Consumer<T> onNext, Consumer<Throwable> onError) {
        Disposable disposable = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(onNext, onError);

        if (compositeDisposable != null) {
            compositeDisposable.add(disposable);
        }
    }

    @SuppressLint({"AutoDispose", "CheckResult"})
    protected <T> void executeNoMapHttp(Class<T> cls, Observable<String> observable, Consumer<ResponseDataObject<T>> onNext, Consumer<Throwable> onError) {
        Disposable disposable = observable
                .map(new Function<String, ResponseDataObject<T>>() {
                    @Override
                    public ResponseDataObject<T> apply(String s) throws Exception {
                        JSONObject jsonObject = new JSONObject(s);

                        ResponseDataObject<T> dataObject = new ResponseDataObject<>();
                        dataObject.code = jsonObject.optInt("code");
                        dataObject.message = jsonObject.optString("msg");
                        String data = jsonObject.optString("data");
                        if (!TextUtils.isEmpty(data) && data.startsWith("{")) { //防止data返回[]
                            dataObject.data = new Gson().fromJson(data, cls);
                        }
                        return dataObject;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(onNext, onError);

        if (compositeDisposable != null) {
            compositeDisposable.add(disposable);
        }
    }


    @SuppressLint({"AutoDispose", "CheckResult"})
    protected <T> void executeNoMapHttp(Class<T> cls, Observable<String> observable, BaseResponseObserver<ResponseDataObject<T>> observer) {
        observable
                .map(new Function<String, ResponseDataObject<T>>() {
                    @Override
                    public ResponseDataObject<T> apply(String s) throws Exception {
                        JSONObject jsonObject = new JSONObject(s);

                        ResponseDataObject<T> dataObject = new ResponseDataObject<>();
                        dataObject.code = jsonObject.optInt("code");
                        dataObject.message = jsonObject.optString("msg");
                        String data = jsonObject.optString("data");
                        if (!TextUtils.isEmpty(data) && data.startsWith("{")) { //防止data返回[]
                            dataObject.data = new Gson().fromJson(data, cls);
                        }
                        return dataObject;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(observer);
        if (compositeDisposable != null) {
            compositeDisposable.add(observer);
        }
    }

    protected void addDisposable(Disposable disposable) {
        if (compositeDisposable != null) {
            compositeDisposable.add(disposable);
        }
    }

}
