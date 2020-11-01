package com.android.base.task

import android.content.Context
import androidx.work.*
import com.blankj.utilcode.util.Utils

/**
 * 后台任务
 * 1、继承BackgroundTask 重写doTask()
 * 2、调用BackgroundTask.execute()执行任务
 * @author zhangquan
 */
abstract class BackgroundTask(context: Context, wokerParams: WorkerParameters) : Worker(context, wokerParams) {
    private val params = wokerParams
    override fun doWork(): Result {
        doTask(params)
        return Result.success()
    }

    abstract fun doTask(parameters: WorkerParameters)


    companion object {
        @JvmStatic
        fun execute(workerClass: Class<out ListenableWorker>) {
            val request = OneTimeWorkRequest.Builder(workerClass)
                    .build()
            WorkManager.getInstance(Utils.getApp()).enqueue(request)
        }

        @JvmStatic
        fun execute(workerClass: Class<out ListenableWorker>, data: Data) {
            val request = OneTimeWorkRequest.Builder(workerClass)
                    .setInputData(data)
                    .build()
            WorkManager.getInstance(Utils.getApp()).enqueue(request)
        }
    }

}