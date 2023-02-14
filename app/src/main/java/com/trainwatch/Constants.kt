package com.trainwatch

import android.util.Log
import com.trainwatch.models.mbta.MBTAStop
import java.io.File

object Constants {

    fun readStopData(filepath: String): MutableMap<String, MBTAStop>{
        val map: MutableMap<String, MBTAStop> = mutableMapOf()
        val lines = this::class.java.getResourceAsStream(filepath)?.bufferedReader()?.readLines()
        lines?.forEach {
            try{
                val csvData = it.split(",")
                val stopId = csvData[0]
                val stopName = csvData[2]
                val stopRoute = csvData[3]
                val stopSequence = csvData[4]
                val longitude = csvData[8].toDouble()
                val latitude = csvData[9].toDouble()
                map[stopId] = MBTAStop(stopId,stopName,stopRoute,stopSequence,longitude,latitude)
            } catch (e: java.lang.NumberFormatException){
                Log.w(MAIN_ACTIVITY_TAG, it)
            }
        }
        return map
    }

    //Map Coords (testing)
    const val BLUE_ROWS = 25
    const val BLUE_COLUMNS = 25
    val BLUE_MAP: Map<Int, Pair<Int, Int>> = mapOf(
        1 to Pair(0,20),
        2 to Pair(2,22),
        3 to Pair(4,24),
        4 to Pair(6,22),
        5 to Pair(8,20),
        6 to Pair(10,18),
        7 to Pair(12,16),
        8 to Pair(14,14),
        9 to Pair(16,12),
        10 to Pair(18,10),
        11 to Pair(20,8),
        12 to Pair(22,6)
    )

    //TAGS
    const val MAIN_ACTIVITY_TAG = "TRAIN_TAG"
    const val MBTA_REQUEST_CALLBACK_TAG  = "MBTA_CALLBACK_TAG"
    const val MBTA_VIEW_MODEL_TAG = "MBTAViewModelTAG"

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