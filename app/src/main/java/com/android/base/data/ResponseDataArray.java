package com.android.base.data;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

/**
 * data为JsonArray
 *
 * @author 张全
 */
@Keep
public class ResponseDataArray<T> extends ResponseData {
    public List<T> data;

    public int count;
    public int page;
    public int pageSize;
    public int pageCount;


    public List<T> getDataList() {
        return null == data ? new ArrayList<>() : data;
    }

    /**
     * 是否还有更多
     *
     * @return
     */
    public boolean hasMore() {
//        int dataSize = getDataList().size();
//        if (dataSize == 0) {
//            return false;
//        }
//        pageSize = pageSize == 0 ? 10 : pageSize;
//        return dataSize >= pageSize;
        return !getDataList().isEmpty();
    }
}
