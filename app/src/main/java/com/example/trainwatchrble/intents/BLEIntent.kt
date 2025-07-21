package com.example.trainwatchrble.intents

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.trainwatchrble.models.Train
import com.example.trainwatchrble.models.TrainWatchrCharacteristic
import com.example.trainwatchrble.util.Constants
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

class BLEIntent {

    companion object {
        private val queue: Queue<TrainWatchrCharacteristic> = LinkedList()

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun processTrains(bluetoothGatt: BluetoothGatt, trains: List<Train>){
            val ledMap: Map<String, ByteArray> = Train.mapLinesToBytes(trains)
            queue.addAll(listOf(
                TrainWatchrCharacteristic(Constants.RED_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.RED_LINE, byteArrayOf())),
                TrainWatchrCharacteristic(Constants.BLUE_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.BLUE_LINE, byteArrayOf())),
                TrainWatchrCharacteristic(Constants.ORANGE_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.ORANGE_LINE, byteArrayOf())),
                TrainWatchrCharacteristic(Constants.GREEN_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.GREEN_LINE, byteArrayOf()))
            ))
            pollData(bluetoothGatt)
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun pollData(bluetoothGatt: BluetoothGatt) {
            val nextChar: TrainWatchrCharacteristic? = queue.poll()
            if (nextChar != null) {
                Log.i("BLE", "Queueing next characteristic write")
                writeData(bluetoothGatt, nextChar)
            } else {
                Log.i("BLE", "No more characteristics to write, waiting for refresh")
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private fun writeData(bluetoothGatt: BluetoothGatt, lineCharacteristic: TrainWatchrCharacteristic) {
            val service = bluetoothGatt.getService(UUID.fromString(Constants.SERVER_ID))
            val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(lineCharacteristic.uuid)
            val data = lineCharacteristic.data
            Log.i("BLE", "Writing following data for ${lineCharacteristic.name}: ${data.map { it.toInt() }}")
            val res = bluetoothGatt.writeCharacteristic(characteristic!!, data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            Log.i("BLE", "Status response: $res")
        }
    }
}