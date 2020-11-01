package com.android.base.util.log;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.util.LContext;
import com.blankj.utilcode.util.DeviceUtils;

import java.lang.reflect.Field;

/**
 * @author 张全
 */
public class AnalysisUtil {
    private static String UA;

    public static String getUA() {
        try {
            if (TextUtils.isEmpty(UA)) {
                UA = "android xlt;" + LContext.versionName + ";" + Build.BRAND + " " + Build.MODEL + ";" + Build.VERSION.RELEASE + ";" + AnalysisUtil.networkType(LContext.getContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UA;
    }

    public static String getUniqueId() {
        return DeviceUtils.getUniqueDeviceId();
    }


    /**
     * 获取网络类型
     *
     * @param context Context
     * @return 网络类型
     */
    public static String networkType(Context context) {
        try {

            // Wifi
            ConnectivityManager manager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Network network = manager.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
                        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return "WIFI";
                        }
                    }
                } else {
                    NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                        return "WIFI";
                    }
                }
            }

            // Mobile network
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                    .TELEPHONY_SERVICE);

            if (telephonyManager == null) {
                return "NULL";
            }

            int networkType = telephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                case 20:  //TelephonyManager.NETWORK_TYPE_NR
                    return "5G";
                default:
                    return "NULL";
            }
        } catch (Exception e) {
            return "NULL";
        }
    }

    public static void testBoard() {
        Class<Build> buildClass = Build.class;
        Field[] declaredFields = buildClass.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                Object value = field.get(null);
                System.out.println(field.getName() + "=" + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }
}
