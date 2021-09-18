package com.fastapp.wxapi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.android.base.Constant
import com.android.base.event.EventMsg
import com.fastapp.BuildConfig
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.umeng.socialize.weixin.view.WXCallbackActivity
import org.greenrobot.eventbus.EventBus

/**
 * 微信登录回调
 */
class WXEntryActivity : WXCallbackActivity() {
    override fun onResp(authResp: BaseResp) {
        if (authResp is SendAuth.Resp) { // 授权
            when (authResp.errCode) {
                BaseResp.ErrCode.ERR_OK -> { // 用户同意
                    addToClip(authResp.code)
                    EventBus.getDefault().post(
                        EventMsg(
                            Constant.Event.WX_CODE,
                            authResp.code
                        )
                    )
                }
                BaseResp.ErrCode.ERR_AUTH_DENIED -> { // 用户拒绝授权
                    EventBus.getDefault().post(
                        EventMsg(
                            Constant.Event.WX_CODE,
                            "0"
                        )
                    )
                }
                BaseResp.ErrCode.ERR_USER_CANCEL -> { // 取消授权
                    EventBus.getDefault().post(
                        EventMsg(
                            Constant.Event.WX_CODE,
                            "1"
                        )
                    )
                }
                else -> {
                    EventBus.getDefault().post(
                        EventMsg(
                            Constant.Event.WX_CODE,
                            "1"
                        )
                    )
                }
            }
            finish()
        } else { // 分享
            super.onResp(authResp)
        }
    }

    private fun addToClip(code: String) {
        if (BuildConfig.DEBUG) {
            val myClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val myClip = ClipData.newPlainText("levelText", code)
            myClipboard.setPrimaryClip(myClip)
        }
    }
}