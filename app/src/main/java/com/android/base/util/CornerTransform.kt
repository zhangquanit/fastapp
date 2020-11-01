package com.android.base.util

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.lang.Math.min
import java.security.MessageDigest


/**
 * desc:
 * time: 2019/11/21
 * @author 银进
 */
class CornerTransform constructor(private val radius: Float) : BitmapTransformation() {
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        //得到图片最小边
        val size = min(toTransform.width, toTransform.height)
        //计算图片起点
        val x = (toTransform.width - size) / 2
        val y = (toTransform.height - size) / 2

        val circleBitmap = Bitmap.createBitmap(x,
                y, Bitmap.Config.ARGB_8888)
        val circle = pool.get(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(circle)
        val paint = Paint()
        paint.isAntiAlias = true
        val rectF = RectF(0f, 0f, circleBitmap.width * 1f, circleBitmap.height * 1f)
        paint.shader = BitmapShader(circleBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rectF, radius, radius, paint)
        return circle
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(TAG.toByte())
    }
    companion object{
        private val TAG = CornerTransform::class.java.name
    }

}