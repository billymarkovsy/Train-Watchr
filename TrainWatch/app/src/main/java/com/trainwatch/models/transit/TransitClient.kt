package com.trainwatch.models.transit

import com.google.transit.realtime.GtfsRealtime.FeedEntity
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import com.google.transit.realtime.GtfsRealtime.VehiclePosition
import java.net.HttpURLConnection
import java.net.URL

class TransitClient(private val apiKey: String){

    fun fetchVehicleData(url: URL): List<TransitVehicle> {
        with(url.openConnection() as HttpURLConnection) {
            this.setRequestProperty("x-api-key", apiKey)
            val message: FeedMessage = FeedMessage.parseFrom(this.inputStream)

            return message.entityList.mapNotNull { buildVehicle(it) }
        }
    }

    private fun buildVehicle(e: FeedEntity): TransitVehicle? {
        return if (e.hasVehicle() && e.vehicle.hasTrip()){
            val v: VehiclePosition = e.vehicle
            TransitVehicle(e.id, v.position?.longitude?.toDouble(), v.position?.latitude?.toDouble(),
                v.currentStatus, v.trip.routeId, v.stopId)
        } else {
            null
        }
    }
}