package com.example.trainwatchrble

import com.example.trainwatchrble.models.Train
import com.google.transit.realtime.GtfsRealtime.FeedEntity
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import java.net.HttpURLConnection
import java.net.URL

object TrainAPIClient {

    val validTrainRoutes: Set<String> = setOf("Blue", "Green-B", "Green-C", "Green-D", "Green-E", "Red", "Orange", "Mattapan")

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