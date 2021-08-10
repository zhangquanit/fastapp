package com.android.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * 解决有值就会回调
 * 原因：先发送一个通知后，后面同一个LifecycleOwner注册的Observer会立即收到通知。
 * 解决：在发射数据后，发射一个null，则之后注册的observer就会接收到null，然后将null过滤即可。
 *
 * @author zhangquan
 */
public class CommonLiveData<T> extends MutableLiveData<T> {

    @Override
    public void setValue(T value) {
        super.setValue(value); //发射正常数据
        setValue(null);//发射一个null，后面同一LifecycleOwner注册的observer就会收到null
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, final @NonNull Observer<? super T> observer) {
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T data) {
                if (data == null) return; //过滤null
                observer.onChanged(data);
            }
        });
    }
}
