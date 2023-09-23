package com.trainwatch.models.transit

import android.graphics.Paint
import androidx.annotation.ColorRes
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus
import com.trainwatch.R

data class TransitVehicle(val name: String, val longitude: Double?, val latitude: Double?,
    val stopStatus: VehicleStopStatus, val routeId: String, val stopId: String){

    var paint: Paint? = null
}
