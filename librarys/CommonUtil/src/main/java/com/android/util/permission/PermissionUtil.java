package com.android.util.permission;


import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;


public class PermissionUtil {

    private static final String TAG = "PermissionsUtil";
    private PermissionFragment fragment;

    public PermissionUtil(@NonNull FragmentActivity activity) {
        fragment = getPermissionsFragment(activity);
    }

    private PermissionFragment getPermissionsFragment(FragmentActivity activity) {
        PermissionFragment fragment = (PermissionFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG);
        boolean isNewInstance = fragment == null;
        if (isNewInstance) {
            fragment = new PermissionFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
        }

        return fragment;
    }

    /**
     * 外部调用申请权限
     *
     * @param permissions 申请的权限
     * @param listener    监听权限接口
     */
    public void requestPermissions(String[] permissions, PermissionListener listener) {
        fragment.setListener(listener);
        fragment.requestPermissions(permissions);

    }

//    //创建PermissionUtil对象，参数为继承自V4包的 FragmentActivity
//    PermissionUtil permissionUtil = new PermissionUtil(MainActivity.this);
//    //调用requestPermissions
//        permissionUtil.requestPermissions(new String[]{
//        Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE},
//            new PermissionListener() {
//        @Override
//        public void onGranted() {
//            //所有权限都已经授权
//            Toast.makeText(MainActivity.this, "所有权限都已授权", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onDenied(List<String> deniedPermission) {
//            //Toast第一个被拒绝的权限
//            Toast.makeText(MainActivity.this, "拒绝了权限" + deniedPermission.get(0), Toast.LENGTH_LONG).show();
//
//        }
//
//        @Override
//        public void onShouldShowRationale(List<String> deniedPermission) {
//            //Toast第一个勾选不在提示的权限
//            Toast.makeText(MainActivity.this, "这个权限" + deniedPermission.get(0)+"勾选了不在提示，要像用户解释为什么需要这权限", Toast.LENGTH_LONG).show();
//        }
//    });
}