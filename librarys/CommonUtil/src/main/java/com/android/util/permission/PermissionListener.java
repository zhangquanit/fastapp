package com.android.util.permission;

import java.util.List;

public interface PermissionListener {
    /**
     * 权限全部已经授权
     */
    void onGranted();

    /**
     * 有权限被拒绝
     *
     * @param deniedPermission 被拒绝的权限
     */
    void onDenied(List<String> deniedPermission);

    /**
     * 权限被拒绝并且勾选了不在询问
     *
     * @param deniedPermission 勾选了不在询问的权限
     */
    void onShouldShowRationale(List<String> deniedPermission);
}