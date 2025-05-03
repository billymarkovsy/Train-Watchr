package com.example.trainwatchrble.models

import android.util.Log
import com.example.trainwatchrble.models.stations.BlueStations.*
import com.example.trainwatchrble.models.stations.RedStations
import com.example.trainwatchrble.models.stations.RedStations.*
import com.example.trainwatchrble.models.stations.OrangeStations
import com.example.trainwatchrble.models.stations.OrangeStations.*
import com.example.trainwatchrble.models.stations.MainGreenStations.*
import com.example.trainwatchrble.util.Constants
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus.*
import kotlin.math.ceil

data class Train(val stopId: String, val stopStatus: VehicleStopStatus, val routeId: String, val directionId: Int) {

    val truncatedRouteId: String
        get() = routeId.split("-")[0]

    fun getChipLEDId(): Set<Int>{
        return when(truncatedRouteId){
            Constants.BLUE_LINE -> blueLineMapping(stopId, directionId, stopStatus)
            Constants.ORANGE_LINE -> orangeLineMapping(stopId, directionId, stopStatus)
            Constants.RED_LINE, Constants.MATTAPAN_LINE -> redLineMapping(stopId, directionId, stopStatus)
            Constants.GREEN_LINE -> greenLineMapping(stopId, directionId, stopStatus)
            else -> {
                Log.i("BLE", "Unknown route $routeId found")
                return emptySet()
            }
        }
    }

    companion object {

        fun mapLinesToBytes(trains: List<Train>): Map<String,ByteArray>{

            val stationIndexMap: Map<String, MutableSet<Int>> = mapOf(
                Pair(Constants.BLUE_LINE, mutableSetOf()),
                Pair(Constants.ORANGE_LINE, mutableSetOf()),
                Pair(Constants.RED_LINE, mutableSetOf()),
                Pair(Constants.GREEN_LINE, mutableSetOf())
            )

            trains.forEach {
                val stationIndices = it.getChipLEDId()
                stationIndexMap[it.truncatedRouteId]?.addAll(stationIndices)
            }

            val stationByteArrayMap: MutableMap<String, ByteArray> = mutableMapOf()
            stationIndexMap.forEach {
                val count = when(it.key){
                    Constants.ORANGE_LINE -> Constants.ORANGE_LINE_STATION_COUNT
                    Constants.GREEN_LINE -> Constants.GREEN_LINE_STATION_COUNT
                    Constants.BLUE_LINE -> Constants.BLUE_LINE_STATION_COUNT
                    Constants.RED_LINE -> Constants.RED_LINE_STATION_COUNT
                    else -> throw IllegalArgumentException("Unknown Line specified")
                }
                stationByteArrayMap[it.key] = mapToByteArray(count, it.value)
            }
            return stationByteArrayMap
        }

        private fun mapToByteArray(stationCount: Int, stationIndices: Set<Int>): ByteArray{
            val roundedByteCount: Int = Constants.toNearestPowerOf8(stationCount)
            val byteArrayLength: Int = ceil(roundedByteCount/8.0).toInt()
            Log.i("BLE", "ByteCount: $roundedByteCount | ArrayLength: $byteArrayLength")
            //var result = 0b0
            val result: Array<Int> = Array(byteArrayLength) { 0 }
            for (i in 0 until roundedByteCount){
                if(stationIndices.contains(i)){
                    val index: Int = i/8
                    val bit = (0b1 shl i%8)
                    Log.d("BLE", "Index: $index bit: $bit")
                    result[index] = result[index] or bit
                    Log.d("BLE", "Result: ${result[index]}")
                }
            }
            Log.i("BLE", "Bit array: ${result.toList()}")
            return result.map { it.toByte() }.toByteArray()
        }

        //Green line doesn't have many in-between stations, so we can just map indices directly
        private fun greenLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): Set<Int> {

            //West/South -> 0
            //East/North -> 1
            val stationSet: Set<Int> = when(stopId) {
                "70511" -> when(stopStatus) {
                    STOPPED_AT -> setOf(MEDFORD_TUFTS_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(BALL_SQUARE_TO_MEDFORD_TUFTS.led)
                }
                "70509" -> when(stopStatus) {
                    STOPPED_AT -> setOf(BALL_SQUARE_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(MAGOUN_SQUARE_TO_BALL_SQUARE.led)
                }
                "70507" -> when(stopStatus) {
                    STOPPED_AT -> setOf(MAGOUN_SQUARE_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(GILMAN_SQUARE_TO_MAGOUN_SQUARE.led)
                }
                "70505" -> when(stopStatus) {
                    STOPPED_AT -> setOf(GILMAN_SQUARE_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(EAST_SOMERVILLE_TO_GILMAN_SQUARE.led)
                }
                "70513" -> when(stopStatus) {
                    STOPPED_AT -> setOf(EAST_SOMERVILLE_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(LECHMERE_TO_EAST_SOMERVILLE.led)
                }
                "70503" -> when(stopStatus) {
                    STOPPED_AT -> setOf(UNION_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(LECHMERE_TO_UNION.led)
                }
                "70501" -> when(stopStatus) {
                    STOPPED_AT -> setOf(LECHMERE_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(SCIENCEPARK_TO_LECHMERE.led)
                }
                "70207" -> when(stopStatus) {
                    STOPPED_AT -> setOf(SCIENCEPARK_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(NSTATIONGREEN_TO_SCIENCEPARK.led)
                }
                "70205" -> when(stopStatus) {
                    STOPPED_AT -> setOf(NSTATIONGREEN_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(HAYMARKETGREEN_TO_NSTATIONGREEN.led)
                }
                "70203" -> when(stopStatus) {
                    STOPPED_AT -> setOf(HAYMARKETGREEN_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(GOVCENTGREEN_TO_HAYMARKETGREEN.led)
                }
                "70201" -> when(stopStatus) {
                    STOPPED_AT -> setOf(GOVCENTGREEN_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(PARKSTGREEN_TO_GOVCENTGREEN.led)
                }
                "70200" -> when(stopStatus) {
                    STOPPED_AT -> setOf(PARKSTGREEN_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(BOYLSTON_TO_PARKSTGREEN.led)
                }
                "70158" -> when(stopStatus) {
                    STOPPED_AT -> setOf(BOYLSTON_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(ARLINGTON_TO_BOYLSTON.led)
                }
                "70156" -> when(stopStatus) {
                    STOPPED_AT -> setOf(ARLINGTON_NORTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(COPLEY_TO_ARLINGTON.led)
                }
                "70154" -> setOf(COPLEY_NORTH.led)
                "70152" -> setOf(HYNES_NORTH.led)
                "70150" -> setOf(KENMORE_NORTH.led)

                "70512" -> setOf(MEDFORD_TUFTS_SOUTH.led)
                "70510" -> when(stopStatus) {
                    STOPPED_AT -> setOf(BALL_SQUARE_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(MEDFORD_TUFTS_TO_BALL_SQUARE.led)
                }
                "70508" -> when(stopStatus) {
                    STOPPED_AT -> setOf(MAGOUN_SQUARE_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(BALL_SQUARE_TO_MAGOUN_SQUARE.led)
                }
                "70506" -> when(stopStatus) {
                    STOPPED_AT -> setOf(GILMAN_SQUARE_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(MAGOUN_SQUARE_TO_GILMAN_SQUARE.led)
                }
                "70514" -> when(stopStatus) {
                    STOPPED_AT -> setOf(EAST_SOMERVILLE_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(GILMAN_SQUARE_TO_EAST_SOMERVILLE.led)
                }
                "70504" -> when(stopStatus) {
                    STOPPED_AT -> setOf(UNION_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(UNION_TO_LECHMERE.led)
                }
                "70502" -> when(stopStatus) {
                    STOPPED_AT -> setOf(LECHMERE_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(EAST_SOMERVILLE_TO_LECHMERE.led)
                }
                "70208" -> when(stopStatus) {
                    STOPPED_AT -> setOf(SCIENCEPARK_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(LECHMERE_TO_SCIENCEPARK.led)
                }
                "70206" -> when(stopStatus) {
                    STOPPED_AT -> setOf(NSTATIONGREEN_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(SCIENCEPARK_TO_NSTATIONGREEN.led)
                }
                "70204" -> when(stopStatus) {
                    STOPPED_AT -> setOf(HAYMARKETGREEN_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(NSTATIONGREEN_TO_HAYMARKETGREEN.led)
                }
                "70202" -> when(stopStatus) {
                    STOPPED_AT -> setOf(GOVCENTGREEN_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(HAYMARKETGREEN_TO_GOVCENTGREEN.led)
                }
                "70196", "70197", "70198", "70199" -> when(stopStatus) {
                    STOPPED_AT -> setOf(PARKSTGREEN_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(GOVCENTGREEN_TO_PARKST.led) //TODO: review the incoming-at values for park st
                }
                "70159" -> when(stopStatus) {
                    STOPPED_AT -> setOf(BOYLSTON_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(PARKSTGREEN_TO_BOYLSTON.led)
                }
                "70157" -> when(stopStatus) {
                    STOPPED_AT -> setOf(ARLINGTON_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(BOYLSTON_TO_ARLINGTON.led)
                }
                "70155" -> when(stopStatus){
                    STOPPED_AT -> setOf(COPLEY_SOUTH.led)
                    IN_TRANSIT_TO, INCOMING_AT -> setOf(ARLINGTON_TO_COPLEY.led)
                }
                "70153" -> setOf(HYNES_SOUTH.led)
                "70151" -> setOf(KENMORE_SOUTH.led)
                else -> {
                    Log.i("BLE", "Unknown stopId $stopId")
                    emptySet()
                }
            }

            return stationSet
        }

        private fun blueLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): Set<Int> {
            //West -> Bowdoin (0)
            //East -> Wonderland (1)

            val directionOffset = when(directionId){
                0 -> 1
                1 -> -1
                else -> throw IllegalArgumentException("Unknown directionId $directionId")
            }

            val base: Set<Int> = when(stopId) {

                "70838" -> setOf(BOWDOIN_WEST.led)
                "70039" -> setOf(GOVT_CENTER_WEST.led)
                "70041" -> setOf(STATE_WEST.led)
                "70043" -> setOf(AQUARIUM_WEST.led)
                "70045" -> setOf(MAVERICK_WEST.led)
                "70047" -> setOf(AIRPORT_WEST.led)
                "70049" -> setOf(WOOD_ISLAND_WEST.led)
                "70051" -> setOf(ORIENT_HEIGHTS_WEST.led)
                "70053" -> setOf(SUFFOLK_DOWNS_WEST.led)
                "70055" -> setOf(BEACHMONT_WEST.led)
                "70057" -> setOf(REVERE_BEACH_WEST.led)
                "70059" -> setOf(WONDERLAND_WEST.led)

                "70038" -> setOf(BOWDOIN_EAST.led)
                "70040" -> setOf(GOVT_CENTER_EAST.led)
                "70042" -> setOf(STATE_EAST.led)
                "70044" -> setOf(AQUARIUM_EAST.led)
                "70046" -> setOf(MAVERICK_EAST.led)
                "70048" -> setOf(AIRPORT_EAST.led)
                "70050" -> setOf(WOOD_ISLAND_EAST.led)
                "70052" -> setOf(ORIENT_HEIGHTS_EAST.led)
                "70054" -> setOf(SUFFOLK_DOWNS_EAST.led)
                "70056" -> setOf(BEACHMONT_EAST.led)
                "70058" -> setOf(REVERE_BEACH_EAST.led)
                "70060" -> setOf(WONDERLAND_EAST.led)

                else -> {
                    Log.i("BLE", "Unknown stopId $stopId")
                    emptySet()
                }
            }

            return when(stopStatus){
                STOPPED_AT -> base
                IN_TRANSIT_TO, INCOMING_AT -> when(stopId){
                    "70045" -> return setOf(MAVERICK_WEST.led+1, MAVERICK_WEST.led+2)
                    "70044" -> return setOf(AQUARIUM_EAST.led-1, AQUARIUM_EAST.led-2)
                    else -> base.map { it +directionOffset }.toSet()
                }
            }
        }

        private fun orangeLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): Set<Int> {

            val directionOffset = when(directionId){
                0 -> -1
                1 -> 1
                else -> throw IllegalArgumentException("Unknown direction id $directionId")
            }

            val base: Set<Int> = when(stopId) {
                "70001" -> setOf(FOREST_HILLS_SOUTH.led, FOREST_HILLS_NORTH.led)
                "70036" -> setOf(OAKGROVE_SOUTH.led, OAKGROVE_NORTH.led)
                "Forest Hills-01" -> setOf(FOREST_HILLS_NORTH.led, FOREST_HILLS_SOUTH.led)
                "Forest Hills-02" -> setOf(FOREST_HILLS_NORTH.led, FOREST_HILLS_SOUTH.led)
                "Oak Grove-01" -> setOf(OAKGROVE_NORTH.led, OAKGROVE_SOUTH.led)
                "Oak Grove-02" -> setOf(OAKGROVE_NORTH.led, OAKGROVE_SOUTH.led)

                // SouthBound
                "70002" -> setOf(GREEN_STREET_SOUTH.led)
                "70004" -> setOf(STONY_BROOK_SOUTH.led)
                "70006" -> setOf(JACKSON_SQUARE_SOUTH.led)
                "70008" -> setOf(ROXBURY_CROSSING_SOUTH.led)
                "70010" -> setOf(RUGGLES_SOUTH.led)
                "70012" -> setOf(MASS_AVE_SOUTH.led)
                "70014" -> setOf(BACK_BAY_SOUTH.led)
                "70016" -> setOf(TUFTS_SOUTH.led)
                "70018" -> setOf(CHINATOWN_SOUTH.led)
                "70020" -> setOf(OrangeStations.DOWNTOWN_CROSSING_SOUTH.led)
                "70022" -> setOf(STATE_SOUTH.led)
                "70024" -> setOf(HAYMARKET_SOUTH.led)
                "70026" -> setOf(NORTH_STATION_SOUTH.led)
                "70028" -> setOf(COMMUNITY_COLLEGE_SOUTH.led)
                "70030" -> setOf(SULLIVAN_SQUARE_SOUTH.led)
                "70278" -> setOf(ASSEMBLY_SOUTH.led)
                "70032" -> setOf(WELLINGTON_SOUTH.led)
                "70034" -> setOf(MALDEN_CENTER_SOUTH.led)

                // NorthBound
                "70003" -> setOf(GREEN_STREET_NORTH.led)
                "70005" -> setOf(STONY_BROOK_NORTH.led)
                "70007" -> setOf(JACKSON_SQUARE_NORTH.led)
                "70009" -> setOf(ROXBURY_CROSSING_NORTH.led)
                "70011" -> setOf(RUGGLES_NORTH.led)
                "70013" -> setOf(MASS_AVE_NORTH.led)
                "70015" -> setOf(BACK_BAY_NORTH.led)
                "70017" -> setOf(TUFTS_NORTH.led)
                "70019" -> setOf(CHINATOWN_NORTH.led)
                "70021" -> setOf(OrangeStations.DOWNTOWN_CROSSING_NORTH.led)
                "70023" -> setOf(STATE_NORTH.led)
                "70025" -> setOf(HAYMARKET_NORTH.led)
                "70027" -> setOf(NORTH_STATION_NORTH.led)
                "70029" -> setOf(COMMUNITY_COLLEGE_NORTH.led)
                "70031" -> setOf(SULLIVAN_SQUARE_NORTH.led)
                "70279" -> setOf(ASSEMBLY_NORTH.led)
                "70033" -> setOf(WELLINGTON_NORTH.led)
                "70035" -> setOf(MALDEN_CENTER_NORTH.led)

                else -> {
                    Log.i("BLE", "Unknown stopId $stopId")
                    emptySet()
                }
            }

            return  when(stopStatus){
                STOPPED_AT -> base
                IN_TRANSIT_TO, INCOMING_AT -> base.map { it +directionOffset }.toSet()
            }
        }

        private fun redLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): Set<Int> {
            //Special Stations
            if(stopId == "70097" && (stopStatus == INCOMING_AT || stopStatus == IN_TRANSIT_TO))
                return setOf(48)
            if(stopId == "70096" && (stopStatus == INCOMING_AT || stopStatus == IN_TRANSIT_TO))
                return setOf(106)

            val directionOffset = when(directionId){
                0 -> -1
                1 -> 1
                else -> throw IllegalArgumentException("Unknown direction id $directionId")
            }


            val base: Set<Int> = when(stopId){
                // SouthBound
                "70061" ->  setOf(ALEWIFE_SOUTH.led, ALEWIFE_NORTH.led)
                "Alewife-01" -> setOf(ALEWIFE_SOUTH.led, ALEWIFE_NORTH.led)
                "Alewife-02" -> setOf(ALEWIFE_SOUTH.led, ALEWIFE_NORTH.led)
                "Braintree-01" -> setOf(BRAINTREE_SOUTH.led, BRAINTREE_NORTH.led)
                "Braintree-02" -> setOf(BRAINTREE_SOUTH.led, BRAINTREE_NORTH.led)

                //SouthBound
                "70063" -> setOf(DAVIS_SOUTH.led)
                "70065" -> setOf(PORTER_SOUTH.led)
                "70067" -> setOf(HARVARD_SOUTH.led)
                "70069" -> setOf(CENTRAL_SOUTH.led)
                "70071" -> setOf(KENDALL_MIT_SOUTH.led)
                "70073" -> setOf(CHARLES_MGH_SOUTH.led)
                "70075" -> setOf(PARK_STREET_SOUTH.led)
                "70077" -> setOf(RedStations.DOWNTOWN_CROSSING_SOUTH.led)
                "70079" -> setOf(SOUTH_STATION_SOUTH.led)
                "70081" -> setOf(BROADWAY_SOUTH.led)
                "70083" -> setOf(ANDREW_SOUTH.led)
                "70085" -> setOf(JFK_UMASS_SAVIN_SOUTH.led)
                "70087" -> setOf(SAVIN_HILL_SOUTH.led)
                "70089" -> setOf(FIELDS_CORNER_SOUTH.led)
                "70091" -> setOf(SHAWMUT_SOUTH.led)
                "70093" -> setOf(ASHMONT_SOUTH.led)
                "70095" -> setOf(JFK_UMASS_NORTH_QUINCY_SOUTH.led)
                "70097" -> setOf(NORTH_QUINCY_SOUTH.led)
                "70099" -> setOf(WOLLASTON_SOUTH.led)
                "70101" -> setOf(QUINCY_CENTER_SOUTH.led)
                "70103" -> setOf(QUINCY_ADAMS_SOUTH.led)
                "70105" -> setOf(BRAINTREE_SOUTH.led)
                "70261" -> setOf(ASHMONT_SOUTH.led)
                "70263" -> setOf(CEDAR_GROVE_SOUTH.led)
                "70265" -> setOf(BUTLER_SOUTH.led)
                "70267" -> setOf(MILTON_SOUTH.led)
                "70269" -> setOf(CENTRAL_AVE_SOUTH.led)
                "70271" -> setOf(VALLEY_ROAD_SOUTH.led)
                "70273" -> setOf(CAPEN_STREET_SOUTH.led)
                "70275" -> setOf(MATTAPAN_SOUTH.led)

                //NorthBound
                "70064" -> setOf(DAVIS_NORTH.led)
                "70066" -> setOf(PORTER_NORTH.led)
                "70068" -> setOf(HARVARD_NORTH.led)
                "70070" -> setOf(CENTRAL_NORTH.led)
                "70072" -> setOf(KENDALL_MIT_NORTH.led)
                "70074" -> setOf(CHARLES_MGH_NORTH.led)
                "70076" -> setOf(PARK_STREET_NORTH.led)
                "70078" -> setOf(RedStations.DOWNTOWN_CROSSING_NORTH.led)
                "70080" -> setOf(SOUTH_STATION_NORTH.led)
                "70082" -> setOf(BROADWAY_NORTH.led)
                "70084" -> setOf(ANDREW_NORTH.led)
                "70086" -> setOf(JFK_UMASS_SAVIN_NORTH.led)
                "70088" -> setOf(SAVIN_HILL_NORTH.led)
                "70090" -> setOf(FIELDS_CORNER_NORTH.led)
                "70092" -> setOf(SHAWMUT_NORTH.led)
                "70094" -> setOf(ASHMONT_NORTH.led)
                "70096" -> setOf(JFK_UMASS_NORTH_QUINCY_NORTH.led)
                "70098" -> setOf(NORTH_QUINCY_NORTH.led)
                "70100" -> setOf(WOLLASTON_NORTH.led)
                "70102" -> setOf(QUINCY_CENTER_NORTH.led)
                "70104" -> setOf(QUINCY_ADAMS_NORTH.led)
                "70264" -> setOf(CEDAR_GROVE_NORTH.led)
                "70266" -> setOf(BUTLER_NORTH.led)
                "70268" -> setOf(MILTON_NORTH.led)
                "70270" -> setOf(CENTRAL_AVE_NORTH.led)
                "70272" -> setOf(VALLEY_ROAD_NORTH.led)
                "70274" -> setOf(CAPEN_STREET_NORTH.led)
                "70276" -> setOf(MATTAPAN_NORTH.led)

                else -> {
                    Log.i("BLE", "Unknown stopId $stopId")
                    emptySet()
                }
            }

            return when(stopStatus){
                STOPPED_AT -> base
                IN_TRANSIT_TO, INCOMING_AT -> {
                    when (stopId) {
                        "70081" -> return setOf(BROADWAY_SOUTH.led-1, BROADWAY_SOUTH.led-2)
                        "70078" -> return setOf(SOUTH_STATION_NORTH.led+1, SOUTH_STATION_NORTH.led+2)
                        else -> base.map {it+directionOffset}.toSet()
                    }
                }
            }
        }
    }
}