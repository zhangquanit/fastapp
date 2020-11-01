package com.fastapp

import android.content.Context
import androidx.work.WorkerParameters
import com.android.base.task.BackgroundTask
import java.io.InputStreamReader
import java.net.URL

/**
 *
 * @author zhangquan
 */
class MyTask(context: Context, param: WorkerParameters) : BackgroundTask(context, param) {
    override fun doTask(parameters: WorkerParameters) {
        val inputData = parameters.inputData
        val value = inputData?.getString("key")
        val value2 = inputData?.getBoolean("key2", false)
        println("doTask thread=${Thread.currentThread().name},data=$inputData")

        var url = URL("http://www.baidu.com");
        var urlConnection = url.openConnection();
        try {
            val inputStream = urlConnection.getInputStream()
            val inputStreamReader = InputStreamReader(inputStream)
            val readLines = inputStreamReader.readLines()
            println("data=$readLines")
            inputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}