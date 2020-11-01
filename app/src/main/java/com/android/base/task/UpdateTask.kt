package com.android.base.task

import android.content.Context
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.NetworkUtils
import component.update.AppDownloadClient
import component.update.AppVersion
import component.update.VersionUpdateListener


/**
 * 升级下载任务
 * @author zhangquan
 */
class UpdateTask(context: Context, param: WorkerParameters) : BackgroundTask(context, param) {
    override fun doTask(parameters: WorkerParameters) {
        if (NetworkUtils.isAvailable()) {
            AppDownloadClient.doCheckVersion(object : VersionUpdateListener {
                override fun onNoVersionReturned() {
                }

                override fun fail() {}
                override fun onNewVersionReturned(appVersion: AppVersion) {
                }
            })
        }
    }
}