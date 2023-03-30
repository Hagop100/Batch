package com.example.batchtest.myGroupsTab.Swipe

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.R
import java.util.*

abstract class MyGroupSwipeHelper(context: Context, private val recyclerView: RecyclerView, internal var buttonWidth: Int)
    : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){

    var buttonList: MutableList<SwipeButtons> ?= null
    lateinit var gestureDetector: GestureDetector
    var swipePosition = 1
    var swipeThreshold = 0.5f
    var buttonBuffer:MutableMap<Int,MutableList<SwipeButtons>>
    lateinit var removerQueue: LinkedList<Int>

    abstract fun instantiateSwipeButtons(viewHolder: RecyclerView.ViewHolder, buffer: MutableList<SwipeButtons>)

    private val gestureListener = object:GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            for(button in buttonList!!)
                if(button.onClick(e!!.x, e.y))
                    break
            return super.onSingleTapUp(e)
        }
    }

    /*
   This code handles the swiping motion
    */
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { _, motionEvent ->
        if(swipePosition < 0) return@OnTouchListener false
        val point = Point(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())
        val swipeViewHolder = recyclerView.findViewHolderForAdapterPosition(swipePosition)
        val swipedItem = swipeViewHolder!!.itemView
        val rect = Rect()
        swipedItem.getGlobalVisibleRect(rect)

        if(motionEvent.action == MotionEvent.ACTION_DOWN ||
            motionEvent.action == MotionEvent.ACTION_MOVE ||
            motionEvent.action == MotionEvent.ACTION_UP) {
            if(rect.top < point.y && rect.bottom > point.y) {
                gestureDetector.onTouchEvent(motionEvent)
            }
            else {
                removerQueue.add(swipePosition)
                swipePosition = -1
                recoverSwipeItem()
            }
        }
        false
    }

    @Synchronized
    private fun recoverSwipeItem() {
        while(!removerQueue.isEmpty()) {
            val pos = removerQueue.poll()!!.toInt()
            if(pos > -1) {
                recyclerView.adapter!!.notifyItemChanged(pos)
            }
        }
    }

    init {
        this.buttonList = ArrayList()
        this.gestureDetector = GestureDetector(context, gestureListener)
        this.recyclerView.setOnTouchListener(onTouchListener)
        this.buttonBuffer = HashMap()
        this.removerQueue = MyGroupSwipeHelper.IntLinkedList()

        attachSwipe()
    }
    private fun attachSwipe() {
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    class IntLinkedList: LinkedList<Int>() {
        override fun contains(element: Int): Boolean {
            return false
        }

        override fun lastIndexOf(element: Int): Int {
            return element
        }

        override fun remove(element: Int): Boolean {
            return false
        }

        override fun indexOf(element: Int): Int {
            return element
        }

        override fun add(element: Int): Boolean {
            return if (contains(element))
                false
            else super.add(element)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false //we wont be using this function
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.absoluteAdapterPosition
        if(swipePosition != pos) {
            removerQueue.add(swipePosition)
        }
        swipePosition = pos
        if(buttonBuffer.containsKey(swipePosition)) {
            buttonList = buttonBuffer[swipePosition]
        }
        else {
            buttonList!!.clear()
        }
        buttonBuffer.clear()
        swipeThreshold = 0.5f*buttonList!!.size.toFloat()*buttonWidth.toFloat()
        recoverSwipeItem()
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f*defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f*defaultValue
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val pos = viewHolder.absoluteAdapterPosition
        var translationX = dX
        var itemView = viewHolder.itemView
        if(pos < 0) {
            swipePosition = pos
            return
        }

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if(dX < 0) {
                var buffer: MutableList<SwipeButtons> = ArrayList()
                if(!buttonBuffer.containsKey(pos)) {
                    instantiateSwipeButtons(viewHolder, buffer)
                    buttonBuffer[pos] = buffer
                }
                else {
                    buffer = buttonBuffer[pos]!!
                }
                translationX = dX*buffer.size.toFloat() * buttonWidth.toFloat() / itemView.width
                drawButton(c, itemView, buffer, pos, translationX)
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
    }


    //Draws the buttons revealed via swiping
    private fun drawButton(c: Canvas, itemView: View, buffer: MutableList<SwipeButtons>, pos: Int, translationX: Float) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1*translationX/buffer.size
        //get cardView item because we will need its margin values to adjust the top and bottom of the rectangle
        val cardView: CardView = itemView.findViewById(R.id.group_card_view)
        //Top must be added with the margin
        //Remember that on a computer the y-axis becomes more positive moving downwards
        //This adjusts the rectangle to match the cardview size
        val top = itemView.top.toFloat() + cardView.marginTop.toFloat()
        val bottom = itemView.bottom.toFloat() - cardView.marginBottom.toFloat()
        for(button in buffer) {
            val left = right - dButtonWidth
            button.onDraw(c, RectF(left, top, right, bottom), pos)
            right = left
        }
    }

}