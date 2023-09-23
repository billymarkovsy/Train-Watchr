package com.trainwatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorRes
import com.google.transit.realtime.GtfsRealtime
import com.trainwatch.enums.City
import com.trainwatch.enums.Line
import com.trainwatch.models.transit.TransitStop
import com.trainwatch.models.transit.TransitVehicle

class SubwayMapView(context: Context, attrs: AttributeSet): View(context, attrs) {
    private var vehicles: MutableMap<String, TransitVehicle>
    private var stops: List<TransitStop>
    private var mWidth: Int
    private var mHeight: Int
    private val defaultPair: Pair<Int, Int>
    private val trianglePath: Path
    private val emptyStop: Paint
    private var city: City

    init {
        vehicles= mutableMapOf()
        stops = emptyList()
        mWidth = this.layoutParams?.width ?: 916
        defaultPair = Pair(0,0)
        trianglePath = Path().apply {
            fillType = Path.FillType.EVEN_ODD
        }
        mHeight = this.layoutParams?.height ?: 1000
        emptyStop =
            Paint(ANTI_ALIAS_FLAG).apply {
                color = resources.getColor(R.color.empty_stop, context.theme)
                style = Paint.Style.FILL
                textSize = 10f
            }
        city = City.NYC
    }

    fun setCity(c: String){
        this.city = City.valueOf(c)
        invalidate()
        requestLayout()
    }
    fun setStops(s: List<TransitStop>){
        this.stops = s
        invalidate()
        requestLayout()
    }
    fun setVehicles(vehicleList: List<TransitVehicle>){
        this.vehicles = mutableMapOf()
        vehicleList.forEach { v ->
            val c: Int = Line.getLineColor(this.city, v.routeId)
            v.apply {
                paint =
                    Paint(ANTI_ALIAS_FLAG).apply {
                        color = resources.getColor(c, context.theme)
                        style = Paint.Style.FILL
                        textSize = 10f
                }
            }
            this.vehicles[v.stopId] = v
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

    private fun getVehiclesAtStop(stopId: String): List<TransitVehicle>{
        val northStopId = "${stopId}N"
        val southStopId = "${stopId}S"

        return if (vehicles.contains(northStopId) && vehicles.containsKey(southStopId)){
            listOf(vehicles[northStopId]!!)
           //TODO("Handle north & south bound trains at the same stop better")
            //listOf(vehicles[northStopId]!!, vehicles[southStopId]!!)
        } else if (vehicles.contains(northStopId)){
            listOf(vehicles[northStopId]!!)
        } else if (vehicles.containsKey(southStopId)){
            listOf(vehicles[southStopId]!!)
        } else if(vehicles.contains(stopId)){
            listOf(vehicles[stopId]!!)
        } else{
            listOf()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.i("TRAIN_TAG", "Refreshing...")
        Log.i("TRAIN_TAG3", vehicles.filter { record -> record.key.endsWith("S", true) }.toString())
        canvas.apply {
            stops.forEach {s ->
                val vehicles = getVehiclesAtStop(s.stopId)
                if (vehicles.isNotEmpty()){
                    vehicles.forEach{ vehicleAtStop ->
                        val mapLatitude: (Double) -> Float = Constants.getLatitudeMap(city, mHeight)
                        val y = mapLatitude(s.stopLat)
                        val mapLongitude: (Double) -> Float = Constants.getLongitudeMap(city, mWidth)
                        val x = mapLongitude(s.stopLong)

                        when(vehicleAtStop.stopStatus){
                            GtfsRealtime.VehiclePosition.VehicleStopStatus.INCOMING_AT, GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT ->
//                            vehicleAtStop.paint?.let {p ->
//
//                                trianglePath.moveTo(x-7.5f, y-5)
//                                trianglePath.lineTo(x, y+5)
//                                trianglePath.lineTo(x+7.5f, y-5)
//                                trianglePath.close()
//                                this.drawPath(trianglePath, p)
//                                trianglePath.reset()
//                            }
                                vehicleAtStop.paint?.let { p -> this.drawCircle(x, y, 15f, p) }
                            GtfsRealtime.VehiclePosition.VehicleStopStatus.IN_TRANSIT_TO ->
                                vehicleAtStop.paint?.let { p -> this.drawRect(x,y,x+19,y+19, p) }
                        }
                    }
                }
                else { // Empty Stop
                    val mapLatitude: (Double) -> Float = Constants.getLatitudeMap(city, mHeight)
                    val y = mapLatitude(s.stopLat)
                    val mapLongitude: (Double) -> Float = Constants.getLongitudeMap(city, mWidth)
                    val x = mapLongitude(s.stopLong)
                    this.drawCircle(x, y, 8f, emptyStop)
                }
            }
        }
    }
}