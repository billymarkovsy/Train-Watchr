package com.example.trainwatchrble

import com.example.trainwatchrble.models.Train
import com.example.trainwatchrble.util.Constants.BLUE_LINE
import com.example.trainwatchrble.util.Constants.GREEN_B_LINE
import com.example.trainwatchrble.util.Constants.GREEN_C_LINE
import com.example.trainwatchrble.util.Constants.GREEN_D_LINE
import com.example.trainwatchrble.util.Constants.GREEN_E_LINE
import com.example.trainwatchrble.util.Constants.MATTAPAN_LINE
import com.example.trainwatchrble.util.Constants.ORANGE_LINE
import com.example.trainwatchrble.util.Constants.RED_LINE
import com.google.transit.realtime.GtfsRealtime.FeedEntity
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import java.net.HttpURLConnection
import java.net.URL

object TrainAPIClient {

    val validTrainRoutes: Set<String> = setOf(BLUE_LINE, GREEN_B_LINE, GREEN_C_LINE, GREEN_D_LINE, GREEN_E_LINE, RED_LINE, ORANGE_LINE, MATTAPAN_LINE)

    fun fetchVehicleData(url: URL, apiKey: String): List<Train>{
        with(url.openConnection() as HttpURLConnection){
            this.setRequestProperty("x-api-key", apiKey)
            val message = FeedMessage.parseFrom(this.inputStream)
            return message.entityList.mapNotNull { buildVehicle(it) }.filter { it.routeId in validTrainRoutes }
        }
    }

    private fun buildVehicle(e: FeedEntity): Train? {
        return if (e.hasVehicle() && e.vehicle.hasTrip()){
            val vehicle = e.vehicle
            val routeId = vehicle.trip.routeId
            val directionId = vehicle.trip.directionId
            Train(vehicle.stopId, vehicle.currentStatus, routeId, directionId)
        } else null
    }

}