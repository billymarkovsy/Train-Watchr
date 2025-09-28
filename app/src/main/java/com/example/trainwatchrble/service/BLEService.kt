package com.example.trainwatchrble.service

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.trainwatchrble.BuildConfig
import com.example.trainwatchrble.R
import com.example.trainwatchrble.TrainAPIClient
import com.example.trainwatchrble.models.Train
import com.example.trainwatchrble.models.TrainWatchrCharacteristic
import com.example.trainwatchrble.util.BluetoothWrapper
import com.example.trainwatchrble.util.Constants
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

class BLEService: Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

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
            val service = BluetoothWrapper.bluetoothGatt!!.getService(UUID.fromString(Constants.SERVER_ID))
            val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(lineCharacteristic.uuid)
            val data = lineCharacteristic.data
            Log.i("BLE", "Writing following data for ${lineCharacteristic.name}: ${data.map { it.toInt() }}")
            val res = BluetoothWrapper.bluetoothGatt!!.writeCharacteristic(characteristic!!, data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            Log.i("BLE", "Status response: $res")
        }
    }

    object TrainWorker {

        val url = Constants.MBTA_URL

        fun fetchTrains(): List<Train> {
            val trains = TrainAPIClient.fetchVehicleData(url, BuildConfig.API_KEY)
            return trains
        }
    }
    private inner class ServiceHandler(looper: Looper): Handler(looper) {
        @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
        override fun handleMessage(msg: Message){
            try {
                BluetoothWrapper.enableLoop()
                while (BluetoothWrapper.getRunning()) {
                    val trains: List<Train> = TrainWorker.fetchTrains()
                    processTrains(trains)
                    Thread.sleep(Constants.TRAIN_DATA_REFRESH)
                }
            } catch (e: InterruptedException){
                Thread.currentThread().interrupt()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        val notification = NotificationCompat.Builder(this, getString(R.string.notification_channel_name))
            .setContentTitle("TrainWatchr")
            .setContentText("Syncing data...")
            .setSmallIcon(R.drawable.metro_map_background)
            .build()
        ServiceCompat.startForeground(this, 5, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        return START_STICKY
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }
}