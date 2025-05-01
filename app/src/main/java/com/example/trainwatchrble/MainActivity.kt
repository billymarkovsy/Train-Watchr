package com.example.trainwatchrble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.trainwatchrble.models.Train
import com.example.trainwatchrble.models.TrainWatchrCharacteristic
import com.example.trainwatchrble.util.Constants
import com.example.trainwatchrble.viewModels.TrainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var connectToServerButton: Button
    private lateinit var sendDataToServerButton: Button
    private lateinit var connectToServerLoader: ProgressBar
    private lateinit var sendDataToServerLoader: ProgressBar
    private lateinit var connectToServerStatus: ImageView
    private lateinit var sendDataToServerStatus: ImageView

    private val STATE_CONNECTED = 0
    private val STATE_DISCONNECTED = 2

    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var bluetoothGatt: BluetoothGatt

    private val trainViewModel: TrainViewModel by viewModels()

    private var scanning = false
    //private var connecting = false
    private var discovered = false
    private var connectionState = STATE_DISCONNECTED

    private var dataJob: Job? = null
    private val queue: Queue<TrainWatchrCharacteristic> = LinkedList()

    @SuppressLint("MissingPermission")
    @RequiresPermission(value = "android. permission. BLUETOOTH_SCAN")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        connectToServerButton = findViewById(R.id.serverConnectButton)
        sendDataToServerButton = findViewById(R.id.serverDataButton)
        connectToServerLoader = findViewById(R.id.serverConnectProgressBar)
        sendDataToServerLoader = findViewById(R.id.serverDataProgressBar)
        connectToServerStatus = findViewById(R.id.serverConnectStatus)
        sendDataToServerStatus = findViewById(R.id.serverDataStatus)

        sendDataToServerLoader.visibility = View.GONE
        connectToServerLoader.visibility = View.GONE
        sendDataToServerStatus.visibility = View.GONE
        connectToServerStatus.visibility = View.GONE

        connectToServerButton.setOnClickListener { _ ->
            connectToServerLoader.visibility = View.VISIBLE
            connectToServerStatus.visibility = View.GONE
            connectToServer()
        }

        sendDataToServerButton.setOnClickListener @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT) { _ ->
            sendDataToServer()
        }
    }

    private fun connectToServer() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner!!

        val handler = Handler(Looper.getMainLooper())

        if (!scanning &&
            this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) { // Stops scanning after a pre-defined scan period.
            Log.i("BLE", "Beginning scan...")
            handler.postDelayed( {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)

                connectToServerLoader.visibility = View.GONE
                connectToServerStatus.visibility = View.VISIBLE

            }, Constants.BLE_SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun sendDataToServer() {
        if (::bluetoothDevice.isInitialized)
            bluetoothGatt = bluetoothDevice.connectGatt(this, true, leGattCallback)
        else
            Log.i("BLE", "Unable to connect Bluetooth device")
    }
    private val leScanCallback: ScanCallback = object : ScanCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (discovered)
                return
            Log.d("BLE", result.device.name ?: "")
            if (result.device.name == Constants.TRAIN_WATCHR_DEVICE_NAME) {
                Log.i("BLE", "Found TrainWatchr Server!")
                scanning = false
                discovered = true
                if (!this@MainActivity::bluetoothDevice.isInitialized)
                    bluetoothDevice = result.device
            }
        }
    }

    private val leGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i("BLE", "Connected")
            scanning = false
            if(newState == BluetoothProfile.STATE_CONNECTED){
                connectionState = STATE_CONNECTED
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                connectionState = STATE_DISCONNECTED
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            dataLoop()
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            val nextChar: TrainWatchrCharacteristic? = queue.poll()
            Log.i("BLE", "Characteristic write complete")
            if(nextChar != null){
                Log.i("BLE", "Queueing next characteristic write")
                writeData(nextChar)
            } else {
                Log.i("BLE", "No more characteristics to write, waiting for refresh")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun dataLoop(){
        stopUpdates()
        dataJob = lifecycleScope.launch @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT) {
            while(true) {
                sendDataToServerLoader.visibility = View.VISIBLE
                sendDataToServerStatus.visibility = View.GONE

                val trains: List<Train> = trainViewModel.fetchTrains(Constants.MBTA_URL).await()
                val ledMap: Map<String, Set<Byte>> = Train.mapLinesToBytes(trains)

                queue.addAll(listOf(
                    TrainWatchrCharacteristic(Constants.RED_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.RED_LINE, emptySet())),
                    TrainWatchrCharacteristic(Constants.BLUE_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.BLUE_LINE, emptySet())),
//                    TrainWatchrCharacteristic("Green", Constants.GREEN_LINE_CHARACTERISTIC),
                    TrainWatchrCharacteristic(Constants.ORANGE_LINE_CHARACTERISTIC, ledMap.getOrDefault(Constants.ORANGE_LINE, emptySet()))
                ))

                val characteristic: TrainWatchrCharacteristic = queue.remove()
                writeData(characteristic)

                sendDataToServerLoader.visibility = View.GONE
                sendDataToServerStatus.visibility = View.VISIBLE

                delay(Constants.TRAIN_DATA_REFRESH)
            }
        }
    }

    private fun stopUpdates(){
        dataJob?.cancel()
        dataJob = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun writeData(lineCharacteristic: TrainWatchrCharacteristic) {
        val service = bluetoothGatt.getService(UUID.fromString(Constants.SERVER_ID))
        val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(lineCharacteristic.uuid)
        val data = lineCharacteristic.data
        Log.i("BLE", "Writing following data for ${lineCharacteristic.name}: ${data.map { it.toInt() }}")
        val res = bluetoothGatt.writeCharacteristic(characteristic!!, data.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        Log.i("BLE", "Status Response: $res")
    }
}