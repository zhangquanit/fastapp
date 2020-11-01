package com.android.base.util.filedownloader;

/**
 * @author 张全
 */
public enum FileType {
    /**
     * 图片
     */
    IMAGE("image/*"),
    /**
     * 文件
     */
    FILE("multipart/form-data"),
    /**
     * 视频
     */
    VIDEO("video*");

    public String type;

    private FileType(String type) {
        this.type = type;
    }
}
