package com.trainwatch

import android.graphics.Color
import android.graphics.Paint
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

    val NYC_MAP: Map<String, Pair<Int, Int>> = mapOf(
        "G22N" to Pair(340, 450),
        "G22S" to Pair(345, 450),
        "G24N" to Pair(327, 456),
        "G24S" to Pair(332, 456),
        "G26N" to Pair(327, 492),
        "G26S" to Pair(332, 492),
        "G28N" to Pair(327, 515),
        "G28S" to Pair(332, 515),
        "G29N" to Pair(342, 550),
        "G29S" to Pair(347, 550),
        "G30N" to Pair(372, 578),
        "G30S" to Pair(377, 578),
        "G31N" to Pair(408, 615),
        "G31S" to Pair(413, 615),
        "G32N" to Pair(423, 630),
        "G32S" to Pair(428, 630),
        "G33N" to Pair(428, 647),
        "G33S" to Pair(433, 647),
        "G34N" to Pair(428, 666),
        "G34S" to Pair(433, 666),
        "G35N" to Pair(428, 686),
        "G35S" to Pair(433, 686),
        "G36N" to Pair(428, 705),
        "G36S" to Pair(433, 705),
        "A42N" to Pair(388, 760),
        "A42S" to Pair(393, 760),
        "F20N" to Pair(355, 797),
        "F20S" to Pair(360, 797),
        "F21N" to Pair(355, 807),
        "F21S" to Pair(360, 807),
        "F22N" to Pair(382, 810),
        "F22S" to Pair(382, 815),
        "F23N" to Pair(415, 810),
        "F23S" to Pair(415, 815),
        "F24N" to Pair(420, 820),
        "F24S" to Pair(425, 820),
        "F25N" to Pair(438, 828),
        "F25S" to Pair(443, 828),
        "F26N" to Pair(448, 835),
        "F26S" to Pair(453, 835),
        "F27N" to Pair(458, 847),
        "F27S" to Pair(463, 847),
        "J12N" to Pair(745, 404),
        "J12S" to Pair(750, 404),
        "J13N" to Pair(730, 419),
        "J13S" to Pair(735, 419),
        "J14N" to Pair(715, 432),
        "J14S" to Pair(720, 432),
        "J15N" to Pair(703, 448),
        "J15S" to Pair(707, 448),
        "J16N" to Pair(690, 462),
        "J16S" to Pair(695, 462),
        "J17N" to Pair(675, 476),
        "J17S" to Pair(680, 476),
        "J19N" to Pair(660, 487),
        "J19S" to Pair(665, 487),
        "J20N" to Pair(645, 503),
        "J20S" to Pair(650, 503),
        "J21N" to Pair(633, 516),
        "J21S" to Pair(638, 516),
        "J22N" to Pair(618, 530),
        "J22S" to Pair(623, 530),
        "J23N" to Pair(605, 545),
        "J23S" to Pair(610, 545),
        "J24N" to Pair(590, 559),
        "J24S" to Pair(595, 559),
        "J27N" to Pair(560, 586),
        "J27S" to Pair(565, 586),
        "J28N" to Pair(532, 595),
        "J28S" to Pair(532, 600),
        "J29N" to Pair(507, 595),
        "J29S" to Pair(507, 600),
        "J30N" to Pair(487, 595),
        "J30S" to Pair(487, 600),
        "J31N" to Pair(463, 595),
        "J31S" to Pair(463, 600),
        "M11N" to Pair(440, 595),
        "M11S" to Pair(440, 600),
        "M12N" to Pair(420, 595),
        "M12S" to Pair(420, 600),
        "M13N" to Pair(402, 595),
        "M13S" to Pair(402, 600),
        "M14N" to Pair(384, 595),
        "M14S" to Pair(384, 600),
        "M16N" to Pair(360, 595),
        "M16S" to Pair(360, 600),
        "M18N" to Pair(310, 595),
        "M18S" to Pair(310, 600),
        "M19N" to Pair(270, 615),
        "M19S" to Pair(275, 615),
        "M20N" to Pair(255, 632),
        "M20S" to Pair(260, 632),
        "M21N" to Pair(255, 658),
        "M21S" to Pair(260, 658),
        "M22N" to Pair(260, 675),
        "M22S" to Pair(260, 675),
        "M23N" to Pair(268, 714),
        "M23S" to Pair(273, 714),
    )

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