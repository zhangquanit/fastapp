package com.android.util.db;

/**
 * @author 张全
 */

import androidx.annotation.Keep;

@Keep
public class DBModel {
    /**
     * 获取uid
     *
     * @return
     */
    String uid;

    /**
     * 获取该条数据在数据库中唯一key
     *
     * @return
     */
    String key;

    /**
     * 表名
     *
     * @return
     */
    String name;

    /**
     * 转换为数据库储存值
     *
     * @return
     */
    String value;

}
