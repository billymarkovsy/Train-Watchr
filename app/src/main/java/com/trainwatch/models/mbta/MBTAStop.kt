package com.trainwatch.models.mbta

import androidx.annotation.Keep
import com.trainwatch.enums.Route

@Keep
@kotlinx.serialization.Serializable
class MBTAStop (val id: String, val stopName: String, val routeId: String, val stopSequence: String, val latitude: Double, val longitude: Double){

    override fun toString(): String {
        return "$stopName ($id)"
    }

    private val route: Route?
        get() {
            val formattedRouteId = routeId.uppercase().replace("-", "_")
            return try {
                Route.valueOf(formattedRouteId)
            }catch (e: IllegalArgumentException){
                null
            }
        }

}