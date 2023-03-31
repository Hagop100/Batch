package com.example.batchtest.myGroupsTab.Swipe

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.batchtest.myGroupsTab.MyGroupAdapter
import io.grpc.internal.SharedResourceHolder.Resource

class SwipeButtons(
    private val context: Context,
    private val text:String,
    private val textSize: Int,
    private val imageResId:Int,
    private val color: Int,
    private val listener: MyGroupAdapter.GroupProfileViewEvent
)    {
    private var pos:Int = 0
    private var clickRegion: RectF?=null
    private val resources:Resources = context.resources

    fun onClick(x: Float, y: Float): Boolean {
        if(clickRegion != null && clickRegion!!.contains(x, y)) {
            listener.onItemClick(pos)
            return true
        }
        return false
    }

    fun onDraw(c: Canvas, rectF: RectF, pos: Int) {
        val p = Paint()
        p.color = color
        c.drawRect(rectF, p)

        //Text
        p.color = Color.WHITE
        p.textSize = textSize.toFloat()

        val r = Rect()
        val cHeight = rectF.height()
        val cWidth = rectF.width()
        p.textAlign = Paint.Align.LEFT
        p.getTextBounds(text, 0, text.length, r)
        var x = 0f
        var y = 0f
        if(imageResId == 0) {
            x = cWidth / 2f - r.width() / 2f - r.left.toFloat()
            y = cHeight / 2f + r.height() / 2f - r.bottom.toFloat()
            c.drawText(text, rectF.left + x, rectF.top + y, p)
        }
        else {
            val d = ContextCompat.getDrawable(context, imageResId)
            val bitmap = drawableToBitMap(d)
            val left = ((rectF.left + rectF.right) / 2) - (bitmap.width / 2).toFloat()
            val top = ((rectF.top + rectF.bottom) / 2) - (bitmap.height / 2).toFloat()
            /*
            This is the moment where we actually draw the icon into the button
            Note that to properly center the buttons we cannot simply take the left and right side of the rectangle
            and divide by 2. While this does get us the center of our rectangle, the image will be placed at this center
            starting from the images top left. The top left of the image is the (0,0) coordinate and that exact point of
            the image is placed where we tell it to. Therefore, if we want the image to SEEM centered, we actually have
            to modify the center of the rectangle to actually be offset by the images width and height. This is exactly what
            I do above.
             */
            c.drawBitmap(bitmap, left, top, p)
        }

        clickRegion = rectF
        this.pos = pos
    }

    /*
    This specifically takes in the icon images and converts them into a bitMap
    That is how android handles drawing images programmatically by creating a bitmap out of them
     */
    private fun drawableToBitMap(d: Drawable?): Bitmap {
        if(d is BitmapDrawable) return d.bitmap
        val bitmap = Bitmap.createBitmap(d!!.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        d.setBounds(0, 0, canvas.width, canvas.height)
        d.draw(canvas)
        return bitmap
    }
}