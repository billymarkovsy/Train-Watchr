package com.example.trainwatchrble.util

import java.net.URL
import kotlin.math.ceil

object Constants {
    const val SERVER_ID = "000000ff-0000-1000-8000-00805f9b34fb"
    const val RED_LINE_CHARACTERISTIC = "0000ff01-0000-1000-8000-00805f9b34fb"
    const val BLUE_LINE_CHARACTERISTIC = "0000fe02-0000-1000-8000-00805f9b34fb"
    const val GREEN_LINE_CHARACTERISTIC = "0000fd03-0000-1000-8000-00805f9b34fb"
    const val ORANGE_LINE_CHARACTERISTIC = "0000fc04-0000-1000-8000-00805f9b34fb"

    const val ORANGE_LINE_STATION_COUNT = 78
    const val BLUE_LINE_STATION_COUNT = 48
    const val RED_LINE_STATION_COUNT = 116
    const val GREEN_LINE_STATION_COUNT = 204

    const val BLE_SCAN_PERIOD = 3000L

    val MBTA_URL = URL("https://cdn.mbta.com/realtime/VehiclePositions.pb")
    const val TRAIN_DATA_REFRESH = 2000L

    const val TRAIN_WATCHR_DEVICE_NAME = "TrainWatchr SeJeff"

    const val RED_LINE = "Red"
    const val BLUE_LINE = "Blue"
    const val ORANGE_LINE = "Orange"
    const val GREEN_LINE = "Green"
    const val GREEN_B_LINE = "Green-B"
    const val GREEN_C_LINE = "Green-C"
    const val GREEN_D_LINE = "Green-D"
    const val GREEN_E_LINE = "Green-E"
    const val MATTAPAN_LINE = "Mattapan"

    const val WORK_TAG = "BLESync"

    fun toNearestMultipleOf8(input: Int): Int{
        return (8*(ceil((input-0.1)/8))).toInt()
    }
}