package com.android.base.ui.splash

import android.Manifest
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.android.base.App
import com.android.util.common.CommonCallBack
import com.android.util.permission.Permission
import com.android.util.permission.PermissionListener
import com.android.util.permission.PermissionUtil
import com.fastapp.MainActivity
import net.medlinker.android.splash.SplashUtil

/**
 * @author zhangquan
 */
class SplashActivity : AppCompatActivity() {
    private var mHandler = Handler()
    private val mDelay = 1000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkShowDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mRunnable)
    }

    private fun checkShowDialog() {
        if (!SplashUtil.isPrivacyGranted()) {
            showPrivacyDialog()
        } else {
            grantPermission()
        }
    }

    /**
     * 隐私政策弹框
     */
    private fun showPrivacyDialog() {
        val dialogFragment = PrivacyDialog()
        dialogFragment.callback(CommonCallBack {
            grantPermission()
            App.initAfterPrivacy()
        })
        dialogFragment.show(supportFragmentManager, "PrivacyProtocol")
    }

    /**
     * 权限申请
     */
    private fun grantPermission() {
        PermissionUtil(this).requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            object : PermissionListener {
                override fun onGranted() {
                    doNextStep()
                }

                override fun onDenied(deniedPermission: MutableList<Permission>?) {
                    doNextStep()
                }
            })
    }

    private fun doNextStep() {
        if (App.coldStart) { //冷启动
            doNext()
        } else {
            mHandler.postDelayed(mRunnable, mDelay) //热启动
        }
        App.coldStart = false
    }

    private fun doNext() {
        if (SplashUtil.hasShowGuide()) {
            MainActivity.start(this)
        } else {
            GuideActivity.start(this)
        }
        finish()
    }

    private var mRunnable = Runnable {
        doNext()
    }
}