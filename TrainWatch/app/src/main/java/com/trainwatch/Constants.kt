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
    const val TRAIN_TAG = "TRAIN_TAG"

    //Bundle Keys
    const val CITY_KEY = "city"
    const val ROUTES_KEY = "routes"

    //Database Values
    const val ASSET_DATABASE_NAME = "nyc.db"
    const val LOCAL_DATABASE_NAME = "stops.db"

    //REQUEST HEADER VALUES
    const val X_API_KEY = "x-api-key"
    const val ACCEPT = "Accept"
    const val JSON_API_FORMAT = "application/vnd.api+json"
    const val HTTP_GET_REQUEST = "GET"

    //Async intervals
    const val REQUEST_STATUS =50L
    const val GET_MBTA_DATA = 6000L

}