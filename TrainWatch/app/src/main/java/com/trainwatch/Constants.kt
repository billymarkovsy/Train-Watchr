package com.trainwatch

import com.trainwatch.enums.City

object Constants {

    fun getLatitudeMap(city: City, height: Int): (Double) -> Float{
        val slope = height.toFloat()/(city.corner2.first - city.corner1.first)
        val intercept = -1*slope*city.corner1.first
        return {x -> ((slope * x) + intercept).toFloat() }
    }

    fun getLongitudeMap(city: City, width: Int): (Double) -> Float{
        val slope = width.toFloat()/(city.corner2.second - city.corner1.second)
        val intercept = -1*slope*city.corner1.second
        return {x -> ((slope * x) + intercept).toFloat() }
    }

    val ALTERNATE_ROUTES: Map<String, String> = mapOf(
        "H" to "A",
        "6X" to "6",
        "7X" to "7",
        "GS" to "S"
    )

    //TAGS
    const val MAIN_ACTIVITY_TAG = "TRAIN_TAG"
    const val MBTA_REQUEST_CALLBACK_TAG  = "MBTA_CALLBACK_TAG"
    const val MBTA_VIEW_MODEL_TAG = "MBTAViewModelTAG"

    //Bundle Keys
    const val CITY_KEY = "city"
    const val ROUTES_KEY = "routes"

    //REQUEST HEADER VALUES
    const val X_API_KEY = "x-api-key"
    const val ACCEPT = "Accept"
    const val JSON_API_FORMAT = "application/vnd.api+json"
    const val HTTP_GET_REQUEST = "GET"

    //MBTA JSON Fields
    const val DATA = "data"
    const val ID = "id"
    const val ATTRIBUTES = "attributes"
    const val RELATIONSHIPS = "relationships"
    const val ROUTE_ID = "routeId"
    const val ROUTE = "route"
    const val STOP_INFO = "stop"
    const val STOP_ID = "stopId"
    const val CURRENT_TRANSIT_STATUS = "current_status"
    const val TRANSIT_STATUS_ID = "transitStatusId"

    //Async intervals
    const val REQUEST_STATUS =50L
    const val GET_MBTA_DATA = 6000L

}