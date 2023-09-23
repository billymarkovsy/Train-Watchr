package com.trainwatch.models.transit

import com.google.transit.realtime.GtfsRealtime.FeedEntity
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import com.google.transit.realtime.GtfsRealtime.VehiclePosition
import com.trainwatch.Constants
import java.net.HttpURLConnection
import java.net.URL

object TransitClient{

    fun fetchVehicleData(url: URL, apiKey: String): List<TransitVehicle> {
        with(url.openConnection() as HttpURLConnection) {
            this.setRequestProperty("x-api-key", apiKey)
            val message: FeedMessage = FeedMessage.parseFrom(this.inputStream)

            return message.entityList.mapNotNull { buildVehicle(it) }
        }
    }

    private fun buildVehicle(e: FeedEntity): TransitVehicle? {
        return if (e.hasVehicle() && e.vehicle.hasTrip()){
            val v: VehiclePosition = e.vehicle
            val routeId = Constants.ALTERNATE_ROUTES.getOrDefault(v.trip.routeId, v.trip.routeId)
            TransitVehicle(e.id, v.position?.longitude?.toDouble(), v.position?.latitude?.toDouble(),
                v.currentStatus, routeId, v.stopId)
        } else {
            null
        }
    }
}