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
import com.example.trainwatchrble.viewModels.TrainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL

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
    private val SCAN_PERIOD: Long = 3000
    private val url = URL("https://cdn.mbta.com/realtime/VehiclePositions.pb")

    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothLeScanner: BluetoothLeScanner

    private val trainViewModel: TrainViewModel by viewModels()

    private var scanning = false
    private var connecting = false
    private var discovered = false;
    private var connectionState = STATE_DISCONNECTED

    private var dataJob: Job? = null
    private val DATA_DELAY = 5000L

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

            }, SCAN_PERIOD)
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
            bluetoothDevice.connectGatt(this, true, leGattCallback)
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
            if (result.device.name == "ESP_GATTS_DEMO") {
                Log.i("BLE", "Found TrainWatchr Server!");
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
            dataLoop(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    fun dataLoop(gatt: BluetoothGatt?){
        stopUpdates()
        dataJob = lifecycleScope.launch @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT) {
            while(true) {
                sendDataToServerLoader.visibility = View.VISIBLE
                sendDataToServerStatus.visibility = View.GONE

                val trains = trainViewModel.fetchTrains(url).await()
                val leds = trains.map { it.getChipLEDId() }.filter { it > 0 }
                val service = gatt?.getService(UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb"))
                Log.d("BLE", "$service@")
                val characteristic = service?.getCharacteristic(UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb"))
                Log.d("BLE", "$characteristic#")
                val data = leds.toByteArray()
                Log.i("BLE", data.toString())
                val res = gatt?.writeCharacteristic(characteristic!!, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                Log.i("BLE", "Status Response: $res")

                sendDataToServerLoader.visibility = View.GONE
                sendDataToServerStatus.visibility = View.VISIBLE

                delay(DATA_DELAY)
            }
        }
    }

    fun stopUpdates(){
        dataJob?.cancel()
        dataJob = null
    }
}