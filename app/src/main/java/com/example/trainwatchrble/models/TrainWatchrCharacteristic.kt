package com.example.trainwatchrble.models

import com.example.trainwatchrble.util.Constants
import java.util.UUID

class TrainWatchrCharacteristic(private val id: String, val data: List<Byte>) {
    val uuid: UUID = UUID.fromString(id)

    val name: String
        get() = when(id){
            Constants.RED_LINE_CHARACTERISTIC -> Constants.RED_LINE
            Constants.BLUE_LINE_CHARACTERISTIC -> Constants.BLUE_LINE
            Constants.ORANGE_LINE_CHARACTERISTIC -> Constants.ORANGE_LINE
            Constants.GREEN_LINE_CHARACTERISTIC -> Constants.GREEN_LINE
            else -> "Unknown Train Characteristic"
        }
}