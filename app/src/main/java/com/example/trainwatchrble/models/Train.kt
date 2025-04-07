package com.example.trainwatchrble.models

import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus.*

data class Train(val stopId: String, val stopStatus: VehicleStopStatus, val routeId: String, val directionId: Int) {

    fun getChipLEDId(): Int{
        return when(routeId){
            "Blue" -> blueLineMapping(stopId, directionId, stopStatus)
            else -> throw IllegalArgumentException("Unknown TripId")
        }
    }

    companion object {
        private fun blueLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): Int {
            //West -> Bowdoin (0)
            //East -> Wonderland (1)
            return when(stopId){
                "70060" -> if (stopStatus == STOPPED_AT) 0 else 3 //Wonderland East
                "70059" -> if (stopStatus == STOPPED_AT) 1 else 2 //Wonderland West
                else -> throw IllegalArgumentException("Unknown StopId")
            }
        }
    }
}