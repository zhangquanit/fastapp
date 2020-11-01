package com.android.base.version;

import androidx.annotation.Keep;

/**
 * 版本升级信息
 *
 * @author 张全
 */
@Keep
public class VersionEntity {
    public String version;
    public int version_code;
    public String update_log;
    public String url;
    public int forced_update;

    @Override
    public String toString() {
        return "VersionEntity{" +
                "version='" + version + '\'' +
                ", version_code=" + version_code +
                ", update_log='" + update_log + '\'' +
                ", url='" + url + '\'' +
                ", forced_update=" + forced_update +
                '}';
    }
}
