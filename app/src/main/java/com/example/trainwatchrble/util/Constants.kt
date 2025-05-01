package com.example.trainwatchrble.util

import java.net.URL
import kotlin.math.floor

object Constants {
    val SERVER_ID = "000000ff-0000-1000-8000-00805f9b34fb"
    val RED_LINE_CHARACTERISTIC = "0000ff01-0000-1000-8000-00805f9b34fb"
    val BLUE_LINE_CHARACTERISTIC = "0000fe02-0000-1000-8000-00805f9b34fb"
    val GREEN_LINE_CHARACTERISTIC = ""
    val ORANGE_LINE_CHARACTERISTIC = "0000fd03-0000-1000-8000-00805f9b34fb"

    val ORANGE_LINE_STATION_COUNT = 78
    val BLUE_LINE_STATION_COUNT = 48
    val RED_LINE_STATION_COUNT = 116

    val BLE_SCAN_PERIOD = 3000L

    val MBTA_URL = URL("https://cdn.mbta.com/realtime/VehiclePositions.pb")
    val TRAIN_DATA_REFRESH = 7000L

    val TRAIN_WATCHR_DEVICE_NAME = "TrainWatchr Server"

    val RED_LINE = "Red"
    val BLUE_LINE = "Blue"
    val ORANGE_LINE = "Orange"
    val GREEN_LINE = "Green"
    val MATTAPAN_LINE = "Mattapan"

    fun toNearestPowerOf8(input: Int): Int{
        return (8*(floor((input-0.1)/8) + 1)).toInt()
    }
}