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
    void onDenied(List<Permission> deniedPermission);
}