package net.medlinker.android.splash

import com.android.base.util.KVUtil

/**
 *
 * @author zhangquan
 */
object SplashUtil {
    private const val KEY_PRIVACY_PROTOCOL_STATE = "KEY_PRIVACY_PROTOCOL_STATE"
    private const val KEY_SHOW_GUIDE = "KEY_SHOW_GUIDE"

    @JvmStatic
    fun isPrivacyGranted(): Boolean {
        return KVUtil.getBoolean(KEY_PRIVACY_PROTOCOL_STATE)
    }

    @JvmStatic
    fun setPrivacyGranted(granted: Boolean) {
        KVUtil.set(KEY_PRIVACY_PROTOCOL_STATE, granted)
    }

    @JvmStatic
    fun hasShowGuide(): Boolean {
        return KVUtil.getBoolean(KEY_SHOW_GUIDE)
    }

    @JvmStatic
    fun setShowGuide(shown: Boolean) {
        KVUtil.set(KEY_SHOW_GUIDE, shown)
    }
}