package com.trainwatch.models.mbta

import androidx.annotation.Keep
import com.trainwatch.Constants
import com.trainwatch.Constants.ROUTE
import com.trainwatch.enums.Route
import com.trainwatch.enums.TransitStatus
import kotlinx.serialization.Serializable

@Keep
@Serializable
class MBTAVehicle (val label: String, val longitude: Double, val latitude: Double, val speed: Double?, val direction_id: Int,
                   val bearing: Int, val id: String, val routeId: String, val current_status: String, val stopId: String) {

    val route: Route?
        get() {
            val formattedRouteId = routeId.uppercase().replace("-", "_")
            return try {
                Route.valueOf(formattedRouteId)
            }catch (e: IllegalArgumentException){
                null
            }
        }

    val stop: MBTAStop?
        get(){
            return Constants.readStopData("/stops.csv")[stopId]
        }

    val position: Int
        get(){
            return 0
//            if(direction_id == 0){
//                val offset =
//            }
            //if (it.direction_id == 0) stopSequence else 13 - stopSequence
        }

//    //TODO: Add inbetween values for STOPPED_AT vs IN_TRANSIT_TO
//    val stopLocation: Int
//        get(){
//            if(route == Route.BLUE){
//
//            }
//            return 0
//        }

    private val transitStatus: TransitStatus?
        get(){
            return try{
                TransitStatus.valueOf(current_status)
            }catch (e: IllegalArgumentException){
                null
            }
        }

    override fun toString(): String {
        return "Route: $route, Id: $id, Status: $transitStatus, Stop: $stopId"
    }
}