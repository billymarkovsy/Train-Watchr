package com.example.trainwatchrble.models

import android.util.Log
import com.example.trainwatchrble.models.stations.BlueStations.*
import com.example.trainwatchrble.models.stations.RedStations
import com.example.trainwatchrble.models.stations.RedStations.*
import com.example.trainwatchrble.models.stations.OrangeStations
import com.example.trainwatchrble.models.stations.OrangeStations.*
import com.example.trainwatchrble.util.Constants
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus.*

data class Train(val stopId: String, val stopStatus: VehicleStopStatus, val routeId: String, val directionId: Int) {

    fun getChipLEDId(): List<Byte>{
        return when(routeId){
            Constants.BLUE_LINE -> blueLineMapping(stopId, directionId, stopStatus)
            Constants.ORANGE_LINE -> orangeLineMapping(stopId, directionId, stopStatus)
            Constants.RED_LINE, Constants.MATTAPAN_LINE -> redLineMapping(stopId, directionId, stopStatus)
            else -> return emptyList()
        }
    }

    companion object {

        fun mapLinesToBytes(trains: List<Train>): Map<String, List<Byte>>{

            val map: Map<String, MutableList<Byte>> = mapOf(
                Pair(Constants.BLUE_LINE, mutableListOf()),
                Pair(Constants.ORANGE_LINE, mutableListOf()),
                Pair(Constants.RED_LINE, mutableListOf()),
                Pair(Constants.GREEN_LINE, mutableListOf())
            )

            trains.forEach {
                val bytes = it.getChipLEDId()
                map[it.routeId]?.addAll(bytes)
            }

            return map
        }

        private fun blueLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): List<Byte> {
            //West -> Bowdoin (0)
            //East -> Wonderland (1)

            val directionOffset = when(directionId){
                0 -> 1
                1 -> -1
                else -> throw IllegalArgumentException("Unknown directionId $directionId")
            }

            val base: List<Byte> = when(stopId) {

                "70838" -> listOf(BOWDOIN_WEST.led)
                "70039" -> listOf(GOVT_CENTER_WEST.led)
                "70041" -> listOf(STATE_WEST.led)
                "70043" -> listOf(AQUARIUM_WEST.led)
                "70045" -> listOf(MAVERICK_WEST.led)
                "70047" -> listOf(AIRPORT_WEST.led)
                "70049" -> listOf(WOOD_ISLAND_WEST.led)
                "70051" -> listOf(ORIENT_HEIGHTS_WEST.led)
                "70053" -> listOf(SUFFOLK_DOWNS_WEST.led)
                "70055" -> listOf(BEACHMONT_WEST.led)
                "70057" -> listOf(REVERE_BEACH_WEST.led)
                "70059" -> listOf(WONDERLAND_WEST.led)

                "70038" -> listOf(BOWDOIN_EAST.led)
                "70040" -> listOf(GOVT_CENTER_EAST.led)
                "70042" -> listOf(STATE_EAST.led)
                "70044" -> listOf(AQUARIUM_EAST.led)
                "70046" -> listOf(MAVERICK_EAST.led)
                "70048" -> listOf(AIRPORT_EAST.led)
                "70050" -> listOf(WOOD_ISLAND_EAST.led)
                "70052" -> listOf(ORIENT_HEIGHTS_EAST.led)
                "70054" -> listOf(SUFFOLK_DOWNS_EAST.led)
                "70056" -> listOf(BEACHMONT_EAST.led)
                "70058" -> listOf(REVERE_BEACH_EAST.led)
                "70060" -> listOf(WONDERLAND_EAST.led)

                else -> {
                    Log.i("BLE", "Unknown stopId $stopId")
                    emptyList()
                }
            }

            return when(stopStatus){
                STOPPED_AT -> base
                IN_TRANSIT_TO, INCOMING_AT -> base.map { (it +directionOffset).toByte() }
            }
        }

        private fun orangeLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): List<Byte> {

            val directionOffset = when(directionId){
                0 -> -1
                1 -> 1
                else -> throw IllegalArgumentException("Unknown direction id $directionId")
            }

            val base: List<Byte> = when(stopId) {
                "70001" -> listOf(FOREST_HILLS_SOUTH.led, FOREST_HILLS_NORTH.led)
                "70036" -> listOf(OAKGROVE_SOUTH.led, OAKGROVE_NORTH.led)
                "Forest Hills-01" -> listOf(FOREST_HILLS_NORTH.led, FOREST_HILLS_SOUTH.led)
                "Forest Hills-02" -> listOf(FOREST_HILLS_NORTH.led, FOREST_HILLS_SOUTH.led)
                "Oak Grove-01" -> listOf(OAKGROVE_NORTH.led, OAKGROVE_SOUTH.led)
                "Oak Grove-02" -> listOf(OAKGROVE_NORTH.led, OAKGROVE_SOUTH.led)

                // SouthBound
                "70002" -> listOf(GREEN_STREET_SOUTH.led)
                "70004" -> listOf(STONY_BROOK_SOUTH.led)
                "70006" -> listOf(JACKSON_SQUARE_SOUTH.led)
                "70008" -> listOf(ROXBURY_CROSSING_SOUTH.led)
                "70010" -> listOf(RUGGLES_SOUTH.led)
                "70012" -> listOf(MASS_AVE_SOUTH.led)
                "70014" -> listOf(BACK_BAY_SOUTH.led)
                "70016" -> listOf(TUFTS_SOUTH.led)
                "70018" -> listOf(CHINATOWN_SOUTH.led)
                "70020" -> listOf(OrangeStations.DOWNTOWN_CROSSING_SOUTH.led)
                "70022" -> listOf(STATE_SOUTH.led)
                "70024" -> listOf(HAYMARKET_SOUTH.led)
                "70026" -> listOf(NORTH_STATION_SOUTH.led)
                "70028" -> listOf(COMMUNITY_COLLEGE_SOUTH.led)
                "70030" -> listOf(SULLIVAN_SQUARE_SOUTH.led)
                "70278" -> listOf(ASSEMBLY_SOUTH.led)
                "70032" -> listOf(WELLINGTON_SOUTH.led)
                "70034" -> listOf(MALDEN_CENTER_SOUTH.led)

                // NorthBound
                "70003" -> listOf(GREEN_STREET_NORTH.led)
                "70005" -> listOf(STONY_BROOK_NORTH.led)
                "70007" -> listOf(JACKSON_SQUARE_NORTH.led)
                "70009" -> listOf(ROXBURY_CROSSING_NORTH.led)
                "70011" -> listOf(RUGGLES_NORTH.led)
                "70013" -> listOf(MASS_AVE_NORTH.led)
                "70015" -> listOf(BACK_BAY_NORTH.led)
                "70017" -> listOf(TUFTS_NORTH.led)
                "70019" -> listOf(CHINATOWN_NORTH.led)
                "70021" -> listOf(OrangeStations.DOWNTOWN_CROSSING_NORTH.led)
                "70023" -> listOf(STATE_NORTH.led)
                "70025" -> listOf(HAYMARKET_NORTH.led)
                "70027" -> listOf(NORTH_STATION_NORTH.led)
                "70029" -> listOf(COMMUNITY_COLLEGE_NORTH.led)
                "70031" -> listOf(SULLIVAN_SQUARE_NORTH.led)
                "70279" -> listOf(ASSEMBLY_NORTH.led)
                "70033" -> listOf(WELLINGTON_NORTH.led)
                "70035" -> listOf(MALDEN_CENTER_NORTH.led)

                else -> {
                    Log.i("BLE", "Unknown stopId $stopId")
                    emptyList()
                }
            }

            return when(stopStatus){
                STOPPED_AT -> base
                IN_TRANSIT_TO, INCOMING_AT -> base.map { (it +directionOffset).toByte() }
            }
        }

        private fun redLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): List<Byte> {
            //Special Stations
            if(stopId == "70097" && (stopStatus == INCOMING_AT || stopStatus == IN_TRANSIT_TO))
                return listOf(48)
            if(stopId == "70096" && (stopStatus == INCOMING_AT || stopStatus == IN_TRANSIT_TO))
                return listOf(106)

            val directionOffset = when(directionId){
                0 -> -1
                1 -> 1
                else -> throw IllegalArgumentException("Unknown direction id $directionId")
            }


            val base: List<Byte> = when(stopId){
                // SouthBound
                "70061" ->  listOf(ALEWIFE_SOUTH.led, ALEWIFE_NORTH.led)
                "Alewife-01" -> listOf(ALEWIFE_SOUTH.led, ALEWIFE_NORTH.led)
                "Alewife-02" -> listOf(ALEWIFE_SOUTH.led, ALEWIFE_NORTH.led)
                "Braintree-01" -> listOf(BRAINTREE_SOUTH.led, BRAINTREE_NORTH.led)
                "Braintree-02" -> listOf(BRAINTREE_SOUTH.led, BRAINTREE_NORTH.led)

                //SouthBound
                "70063" -> listOf(DAVIS_SOUTH.led)
                "70065" -> listOf(PORTER_SOUTH.led)
                "70067" -> listOf(HARVARD_SOUTH.led)
                "70069" -> listOf(CENTRAL_SOUTH.led)
                "70071" -> listOf(KENDALL_MIT_SOUTH.led)
                "70073" -> listOf(CHARLES_MGH_SOUTH.led)
                "70075" -> listOf(PARK_STREET_SOUTH.led)
                "70077" -> listOf(RedStations.DOWNTOWN_CROSSING_SOUTH.led)
                "70079" -> listOf(SOUTH_STATION_SOUTH.led)
                "70081" -> listOf(BROADWAY_SOUTH.led)
                "70083" -> listOf(ANDREW_SOUTH.led)
                "70085" -> listOf(JFK_UMASS_SAVIN_SOUTH.led)
                "70087" -> listOf(SAVIN_HILL_SOUTH.led)
                "70089" -> listOf(FIELDS_CORNER_SOUTH.led)
                "70091" -> listOf(SHAWMUT_SOUTH.led)
                "70093" -> listOf(ASHMONT_SOUTH.led)
                "70095" -> listOf(JFK_UMASS_NORTH_QUINCY_SOUTH.led)
                "70097" -> listOf(NORTH_QUINCY_SOUTH.led)
                "70099" -> listOf(WOLLASTON_SOUTH.led)
                "70101" -> listOf(QUINCY_CENTER_SOUTH.led)
                "70103" -> listOf(QUINCY_ADAMS_SOUTH.led)
                "70105" -> listOf(BRAINTREE_SOUTH.led)
                "70261" -> listOf(ASHMONT_SOUTH.led)
                "70263" -> listOf(CEDAR_GROVE_SOUTH.led)
                "70265" -> listOf(BUTLER_SOUTH.led)
                "70267" -> listOf(MILTON_SOUTH.led)
                "70269" -> listOf(CENTRAL_AVE_SOUTH.led)
                "70271" -> listOf(VALLEY_ROAD_SOUTH.led)
                "70273" -> listOf(CAPEN_STREET_SOUTH.led)
                "70275" -> listOf(MATTAPAN_SOUTH.led)

                //NorthBound
                "70064" -> listOf(DAVIS_NORTH.led)
                "70066" -> listOf(PORTER_NORTH.led)
                "70068" -> listOf(HARVARD_NORTH.led)
                "70070" -> listOf(CENTRAL_NORTH.led)
                "70072" -> listOf(KENDALL_MIT_NORTH.led)
                "70074" -> listOf(CHARLES_MGH_NORTH.led)
                "70076" -> listOf(PARK_STREET_NORTH.led)
                "70078" -> listOf(RedStations.DOWNTOWN_CROSSING_NORTH.led)
                "70080" -> listOf(SOUTH_STATION_NORTH.led)
                "70082" -> listOf(BROADWAY_NORTH.led)
                "70084" -> listOf(ANDREW_NORTH.led)
                "70086" -> listOf(JFK_UMASS_SAVIN_NORTH.led)
                "70088" -> listOf(SAVIN_HILL_NORTH.led)
                "70090" -> listOf(FIELDS_CORNER_NORTH.led)
                "70092" -> listOf(SHAWMUT_NORTH.led)
                "70094" -> listOf(ASHMONT_NORTH.led)
                "70096" -> listOf(JFK_UMASS_NORTH_QUINCY_NORTH.led)
                "70098" -> listOf(NORTH_QUINCY_NORTH.led)
                "70100" -> listOf(WOLLASTON_NORTH.led)
                "70102" -> listOf(QUINCY_CENTER_NORTH.led)
                "70104" -> listOf(QUINCY_ADAMS_NORTH.led)
                "70264" -> listOf(CEDAR_GROVE_NORTH.led)
                "70266" -> listOf(BUTLER_NORTH.led)
                "70268" -> listOf(MILTON_NORTH.led)
                "70270" -> listOf(CENTRAL_AVE_NORTH.led)
                "70272" -> listOf(VALLEY_ROAD_NORTH.led)
                "70274" -> listOf(CAPEN_STREET_NORTH.led)
                "70276" -> listOf(MATTAPAN_NORTH.led)

                else -> {
                    Log.i("BLE", "Unknown stopId $stopId")
                    emptyList()
                }
            }

            return when(stopStatus){
                STOPPED_AT -> base
                IN_TRANSIT_TO, INCOMING_AT -> {
                    when (stopId) {
                        "70081" -> {
                            return listOf((BROADWAY_SOUTH.led-1).toByte(), (BROADWAY_SOUTH.led-2).toByte())
                        }
                        "70078" -> {
                            return listOf((SOUTH_STATION_NORTH.led+1).toByte(), (SOUTH_STATION_NORTH.led+2).toByte())
                        }
                        else -> {
                            base.map {(it+directionOffset).toByte()}
                        }
                    }
                }
            }
        }
    }
}