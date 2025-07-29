package com.example.trainwatchrble.intents

import android.Manifest
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.trainwatchrble.BuildConfig
import com.example.trainwatchrble.TrainAPIClient
import com.example.trainwatchrble.models.Train
import com.example.trainwatchrble.models.TrainWatchrCharacteristic
import com.example.trainwatchrble.util.BluetoothWrapper
import com.example.trainwatchrble.util.Constants
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

class BLEWorker(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun doWork(): Result {
        BluetoothWrapper.enableLoop()
        while (BluetoothWrapper.getRunning()) {
            val trains: List<Train> = TrainWorker.fetchTrains()
            processTrains(trains)
            Thread.sleep(Constants.TRAIN_DATA_REFRESH)
        }
        return Result.success()
    }

    object TrainWorker {

        val url = Constants.MBTA_URL

        fun fetchTrains(): List<Train> {
            val trains = TrainAPIClient.fetchVehicleData(url, BuildConfig.API_KEY)
            return trains
        }
    }

    companion object {
        private val queue: Queue<TrainWatchrCharacteristic> = LinkedList()

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun processTrains(trains: List<Train>){
            val ledMap: Map<String, ByteArray> = Train.mapLinesToBytes(trains)
            queue.addAll(listOf(
                TrainWatchrCharacteristic(Constants.RED_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.RED_LINE, byteArrayOf())),
                TrainWatchrCharacteristic(Constants.BLUE_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.BLUE_LINE, byteArrayOf())),
                TrainWatchrCharacteristic(Constants.ORANGE_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.ORANGE_LINE, byteArrayOf())),
                TrainWatchrCharacteristic(Constants.GREEN_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.GREEN_LINE, byteArrayOf()))
            ))
            pollData()
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun pollData() {
            val nextChar: TrainWatchrCharacteristic? = queue.poll()
            if (nextChar != null) {
                Log.i("BLE", "Queueing next characteristic write")
                writeData(nextChar)
            } else {
                Log.i("BLE", "No more characteristics to write, waiting for refresh")
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private fun writeData(lineCharacteristic: TrainWatchrCharacteristic) {
            Log.i("BLE", BluetoothWrapper.bluetoothGatt.services.toString())
            val service = BluetoothWrapper.bluetoothGatt.getService(UUID.fromString(Constants.SERVER_ID))
            Log.i("BLE", service.toString())
            val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(lineCharacteristic.uuid)
            val data = lineCharacteristic.data
            Log.i("BLE", "Writing following data for ${lineCharacteristic.name}: ${data.map { it.toInt() }}")
            Log.i("BLE", (characteristic == null).toString())
            val res = BluetoothWrapper.bluetoothGatt.writeCharacteristic(characteristic!!, data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            Log.i("BLE", "Status response: $res")
        }
    }
}