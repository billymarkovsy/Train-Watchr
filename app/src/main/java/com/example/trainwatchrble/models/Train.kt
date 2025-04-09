package com.example.trainwatchrble.models

import android.util.Log
import com.example.trainwatchrble.models.stations.RedStations.*
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus.*

data class Train(val stopId: String, val stopStatus: VehicleStopStatus, val routeId: String, val directionId: Int) {

    fun getChipLEDId(): Byte{
        return when(routeId){
            "Blue" -> blueLineMapping(stopId, directionId, stopStatus)
            "Red", "Mattapan" -> redLineMapping(stopId, directionId, stopStatus)
            else -> return -1
        }
    }

    companion object {
        private fun blueLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): Byte {
            //West -> Bowdoin (0)
            //East -> Wonderland (1)
            return -1
//            return when(stopId){
//                "70060" -> if (stopStatus == STOPPED_AT) 0 else 3 //Wonderland East
//                "70059" -> if (stopStatus == STOPPED_AT) 1 else 2 //Wonderland West
//                else -> throw IllegalArgumentException("Unknown StopId")
//            }
        }

        private fun redLineMapping(stopId: String, directionId: Int, stopStatus: VehicleStopStatus): Byte {
            //Special Stations
            if(stopId == "70097" && (stopStatus == INCOMING_AT || stopStatus == IN_TRANSIT_TO))
                return 48
            if(stopId == "70096" && (stopStatus == INCOMING_AT || stopStatus == IN_TRANSIT_TO))
                return 106


            val base: Byte = when(stopId){
                // SouthBound
                "70061" -> ALEWIFE_SOUTH.led
                "Alewife-01" -> ALEWIFE_SOUTH.led //Just guessing on this, need to double check
                "70063" -> DAVIS_SOUTH.led
                "70065" -> PORTER_SOUTH.led
                "70067" -> HARVARD_SOUTH.led
                "70069" -> CENTRAL_SOUTH.led
                "70071" -> KENDALL_MIT_SOUTH.led
                "70073" -> CHARLES_MGH_SOUTH.led
                "70075" -> PARK_STREET_SOUTH.led
                "70077" -> DOWNTOWN_CROSSING_SOUTH.led
                "70079" -> SOUTH_STATION_SOUTH.led
                "70081" -> BROADWAY_SOUTH.led
                "70083" -> ANDREW_SOUTH.led
                "70085" -> JFK_UMASS_SAVIN_SOUTH.led
                "70087" -> SAVIN_HILL_SOUTH.led
                "70089" -> FIELDS_CORNER_SOUTH.led
                "70091" -> SHAWMUT_SOUTH.led
                "70093" -> ASHMONT_SOUTH.led
                "70095" -> JFK_UMASS_NORTH_QUINCY_SOUTH.led
                "70097" -> NORTH_QUINCY_SOUTH.led
                "70099" -> WOLLASTON_SOUTH.led
                "70101" -> QUINCY_CENTER_SOUTH.led
                "70103" -> QUINCY_ADAMS_SOUTH.led
                "70105" -> BRAINTREE_SOUTH.led
                "Braintree-01" -> BRAINTREE_SOUTH.led
                "70261" -> ASHMONT_SOUTH.led
                "70263" -> CEDAR_GROVE_SOUTH.led
                "70265" -> BUTLER_SOUTH.led
                "70267" -> MILTON_SOUTH.led
                "70269" -> CENTRAL_AVE_SOUTH.led
                "70271" -> VALLEY_ROAD_SOUTH.led
                "70273" -> CAPEN_STREET_SOUTH.led
                "70275" -> MATTAPAN_SOUTH.led

                //NorthBound
                "Alewife-02" -> ALEWIFE_NORTH.led
                "70064" -> DAVIS_NORTH.led
                "70066" -> PORTER_NORTH.led
                "70068" -> HARVARD_NORTH.led
                "70070" -> CENTRAL_NORTH.led
                "70072" -> KENDALL_MIT_NORTH.led
                "70074" -> CHARLES_MGH_NORTH.led
                "70076" -> PARK_STREET_NORTH.led
                "70078" -> DOWNTOWN_CROSSING_NORTH.led
                "70080" -> SOUTH_STATION_NORTH.led
                "70082" -> BROADWAY_NORTH.led
                "70084" -> ANDREW_NORTH.led
                "70086" -> JFK_UMASS_SAVIN_NORTH.led
                "70088" -> SAVIN_HILL_NORTH.led
                "70090" -> FIELDS_CORNER_NORTH.led
                "70092" -> SHAWMUT_NORTH.led
                "70094" -> ASHMONT_NORTH.led
                "70096" -> JFK_UMASS_NORTH_QUINCY_NORTH.led
                "70098" -> NORTH_QUINCY_NORTH.led
                "70100" -> WOLLASTON_NORTH.led
                "70102" -> QUINCY_CENTER_NORTH.led
                "70104" -> QUINCY_ADAMS_NORTH.led
                "Braintree-02" -> BRAINTREE_NORTH.led
                "70264" -> CEDAR_GROVE_NORTH.led
                "70266" -> BUTLER_NORTH.led
                "70268" -> MILTON_NORTH.led
                "70270" -> CENTRAL_AVE_NORTH.led
                "70272" -> VALLEY_ROAD_NORTH.led
                "70274" -> CAPEN_STREET_NORTH.led
                "70276" -> MATTAPAN_NORTH.led

                else -> {
                    Log.i("BLE", "Unknown stopId $stopId")
                    -1
                }
            }

            return when(stopStatus){
                STOPPED_AT -> base
                IN_TRANSIT_TO, INCOMING_AT -> (base+1).toByte()
            }
        }
    }
}