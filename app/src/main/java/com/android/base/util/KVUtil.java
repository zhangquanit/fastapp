package com.android.base.util;

import android.os.Parcelable;

import com.tencent.mmkv.MMKV;

import java.util.Set;

public class KVUtil {


    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    public static void set(String key, Object object) {
        MMKV mv = MMKV.defaultMMKV();
        if (object instanceof String) {
            mv.encode(key, (String) object);
        } else if (object instanceof Integer) {
            mv.encode(key, (Integer) object);
        } else if (object instanceof Boolean) {
            mv.encode(key, (Boolean) object);
        } else if (object instanceof Float) {
            mv.encode(key, (Float) object);
        } else if (object instanceof Long) {
            mv.encode(key, (Long) object);
        } else if (object instanceof Double) {
            mv.encode(key, (Double) object);
        } else if (object instanceof byte[]) {
            mv.encode(key, (byte[]) object);
        } else if (object instanceof Parcelable) {
            mv.encode(key, (Parcelable) object);
        } else if (object instanceof Set) {
            mv.encode(key, (Set) object);
        } else {
            mv.encode(key, object.toString());
        }

    }

    public static void setSet(String key, Set<String> sets) {
        MMKV mv = MMKV.defaultMMKV();
        mv.encode(key, sets);
    }

    public static void setParcelable(String key, Parcelable obj) {
        MMKV mv = MMKV.defaultMMKV();
        mv.encode(key, obj);
    }


    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int def) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeInt(key, def);
    }

    public static double getDouble(String key) {
        return getDouble(key, 0.0d);
    }

    public static double getDouble(String key, double def) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeDouble(key, def);
    }

    public static long getLong(String key) {
        return getLong(key, 0);
    }

    public static long getLong(String key, long def) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeLong(key, def);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean def) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeBool(key, def);
    }

    public static float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public static float getFloat(String key, float def) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeFloat(key, def);
    }

    public static byte[] getBytes(String key) {
        return getBytes(key, (byte[]) null);
    }

    public static byte[] getBytes(String key, byte[] def) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeBytes(key, def);
    }

    public static String getString(String key) {
        return getString(key, (String) null);
    }

    public static String getString(String key, String def) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeString(key, def);
    }

    public static Set<String> getStringSet(String key) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeStringSet(key, (Set) null);
    }

    public static <T extends Parcelable> T getParcelable(String key, Class<T> tClass) {
        MMKV mv = MMKV.defaultMMKV();
        return mv.decodeParcelable(key, tClass, null);
    }

    /**
     * 移除某个key对
     *
     * @param key
     */
    public static void removeKey(String key) {
        MMKV mv = MMKV.defaultMMKV();
        mv.removeValueForKey(key);
    }

    /**
     * 清除所有key
     */
    public static void clearAll() {
        MMKV mv = MMKV.defaultMMKV();
        mv.clearAll();
    }
}
