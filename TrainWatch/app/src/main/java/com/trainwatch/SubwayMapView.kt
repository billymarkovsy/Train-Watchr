package com.trainwatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.transit.realtime.GtfsRealtime
import com.trainwatch.models.transit.TransitVehicle

class SubwayMapView(context: Context, attrs: AttributeSet): View(context, attrs) {
    private var vehicles: List<TransitVehicle>
    private var mWidth: Int
    private var mHeight: Int
    private val defaultPair = Pair(0,0)

    init {
        vehicles= emptyList()
        mWidth = this.layoutParams?.width ?: 916
        mHeight = this.layoutParams?.height ?: 1000
//        context.theme.obtainStyledAttributes(attrs, R.styleable.SubwayMapView, 0,0).apply {
//            try{
//            } finally {
//                recycle()
//            }
//        }

    }
    fun setVehicles(v: List<TransitVehicle>){
        this.vehicles = v
        this.vehicles.forEach {
            it.apply {
                paint =
                    Paint(ANTI_ALIAS_FLAG).apply {
                        color = resources.getColor(it.color, context.theme)
                        style = Paint.Style.FILL
                        textSize = 10f
                }
            }
        }

        invalidate()
        requestLayout()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(w != 0 && height != 0){
            this.mWidth = w
            this.mHeight = h
        }
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val minWidth: Int = this.paddingLeft + this.paddingRight + this.suggestedMinimumWidth
//        val width: Int = View.resolveSizeAndState(minWidth, widthMeasureSpec, 1)
//
//        val minHeight: Int = View.MeasureSpec.getSize(width) - textWidth.toInt() + paddingBottom + paddingTop
//        val height: Int = View.resolveSizeAndState(minHeight, heightMeasureSpec, 0)
//
//        setMeasuredDimension(width, height)
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.i("TRAIN_TAG", "Refreshing...")
        canvas.apply {
            vehicles.forEach {
                context.resources.getIdentifier(it.stopId, "string", context.applicationInfo.packageName)
                //context.resources.getIdentifier("", string, )
                val coords: Pair<Int, Int> = Constants.NYC_MAP.getOrDefault(it.stopId, defaultPair)
                val x: Float = mapCoords(coords.first, 916, mWidth)
                val y: Float = mapCoords(coords.second, 1000, mHeight)
                when(it.stopStatus){
                    GtfsRealtime.VehiclePosition.VehicleStopStatus.INCOMING_AT, GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT ->
                        it.paint?.let { p -> this.drawCircle(x, y, 10f, p) }
                    GtfsRealtime.VehiclePosition.VehicleStopStatus.IN_TRANSIT_TO ->
                        it.paint?.let { p -> this.drawRect(x,y,x+14,y+14, p) }
                }
            }
        }
    }

    private fun mapCoords(position: Int, bound: Int, dimension: Int): Float {
        return position.toFloat()/bound.toFloat() * dimension.toFloat()
    }
}