package com.android.base.ui.splash

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.android.base.util.ext.onClick
import com.android.util.common.CommonCallBack
import com.fastapp.R
import kotlinx.android.synthetic.main.privacy_dialog.*
import net.medlinker.android.splash.SplashUtil


/**
 * 隐私政策弹窗
 */
class PrivacyDialog : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.update_dialog_style)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.privacy_dialog, container)

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog?.window
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        tv_cancel.onClick {
            mCallBack?.onCallBack(false)
            dismiss()
        }
        tv_know.onClick {
            SplashUtil.setPrivacyGranted(true)
            mCallBack?.onCallBack(true)
            dismiss()
        }
    }

    private var mCallBack: CommonCallBack<Boolean>? = null
    fun callback(callBack: CommonCallBack<Boolean>) {
        mCallBack = callBack
    }
}

