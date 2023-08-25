package com.trainwatch.models.transit

import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus
import com.trainwatch.R

data class TransitVehicle(val name: String, val longitude: Double?, val latitude: Double?,
    val stopStatus: VehicleStopStatus, val routeId: String, val stopId: String, var paint: Paint? = null){

    @get:ColorRes
    val color: Int
        get() {
            return when(routeId){
                "A", "C", "E" -> R.color.a_train
                "B", "D", "F", "M" -> R.color.b_train
                "N", "Q", "R", "W" -> R.color.n_train
                "G" -> R.color.g_train
                "J", "Z" -> R.color.j_train
                "L" -> R.color.l_train
                "S" -> R.color.shuttle
                "1", "2", "3" -> R.color.one_train
                "4", "5", "6" -> R.color.four_train
                "7" -> R.color.seven_train
                else -> R.color.white
            }
        }
}
