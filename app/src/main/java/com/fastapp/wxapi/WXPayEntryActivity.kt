package com.fastapp.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.android.util.LContext
import com.android.util.ext.ToastUtil
import com.android.util.log.LogUtil
import com.android.base.Constant
import com.android.base.event.EventMsg
import com.android.base.util.pay.PayWay
import com.fastapp.R
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.greenrobot.eventbus.EventBus

/**
 * 微信支付回调页面
 *
 * @author 张全
 */
class WXPayEntryActivity : Activity(), IWXAPIEventHandler {
    private var api: IWXAPI? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wx_pay_result)
        api = WXAPIFactory.createWXAPI(this, LContext.getString(R.string.wx_appid))
        api?.handleIntent(intent, this)
        LogUtil.d(TAG, "WXPayEntryActivity............onCreate")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api?.handleIntent(intent, this)
        LogUtil.d(TAG, "WXPayEntryActivity............onNewIntent")
    }

    override fun onReq(req: BaseReq) {}
    override fun onResp(resp: BaseResp) {
        if (null == resp) {
            finish()
            return
        }
        printInfo(resp)
        LogUtil.d(TAG, "WXPayEntryActivity,onResp...onPayFinish, errCode = "
                + resp.errCode + ",type=" + resp.type)
        if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            when (resp.errCode) {
                0 -> {
                    EventBus.getDefault().post(
                        EventMsg(
                            Constant.Event.ORDER_BUY_SUCCESS,
                            PayWay.WX
                        )
                    )
                    finish()
                }
                -1 -> {
                    /*
                     * 可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                     */EventBus.getDefault().post(
                        EventMsg(
                            Constant.Event.ORDER_BUY_FAIL,
                            PayWay.WX
                        )
                    )
                    ToastUtil.show("支付失败")
                    finish()
                }
                -2 -> {
                    /*
                     * 无需处理。发生场景：用户不支付了，点击取消，返回APP。
                     */EventBus.getDefault().post(
                        EventMsg(
                            Constant.Event.ORDER_BUY_CANCEL,
                            PayWay.WX
                        )
                    )
                    ToastUtil.show("取消支付")
                    finish()
                }
                else -> finish()
            }
        }
    }

    companion object {
        private const val TAG = "WXPayEntryActivity"
        fun printInfo(baseResp: BaseResp?) {
            if (!LContext.isDebug || null == baseResp) {
                return
            }
            LogUtil.d(TAG, "---------微信支付--------start")
            val fields = baseResp.javaClass.declaredFields
            for (field in fields) {
                field.isAccessible = true
                try {
                    val value = field[baseResp]
                    LogUtil.d(TAG, field.name + "=" + value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            LogUtil.d(TAG, "---------微信支付--------end")
        }
    }
}