package com.example.trainwatchrble

import android.Manifest
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
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.trainwatchrble.viewModels.TrainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL

import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val STATE_CONNECTED = 0
    private val STATE_DISCONNECTED = 2
    private val SCAN_PERIOD: Long = 3000
    private val url = URL("https://cdn.mbta.com/realtime/VehiclePositions.pb")

    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothLeScanner: BluetoothLeScanner

    private val trainViewModel: TrainViewModel by viewModels()

    private var scanning = false;
    private var discovered = false;
    private var connectionState = STATE_DISCONNECTED

    private var dataJob: Job? = null
    private val DATA_DELAY = 5000L

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

                if (::bluetoothDevice.isInitialized)
                    bluetoothDevice.connectGatt(this, true, leGattCallback)
                else
                    Log.i("BLE", "Unable to connect Bluetooth device")

            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
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
                    //bluetoothGatt = result.device.connectGatt(this@MainActivity, true, leGattCallback)
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

    fun dataLoop(gatt: BluetoothGatt?){
        stopUpdates()
        dataJob = lifecycleScope.launch @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT) {
            while(true) {
                var data = getData()
                trainViewModel.fetchTrains(url)
                val service = gatt?.getService(UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb"))
                Log.d("BLE", "$service@")
                val characteristic = service?.getCharacteristic(UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb"))
                Log.d("BLE", "$characteristic#")
                val res = gatt?.writeCharacteristic(characteristic!!, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                Log.i("BLE", "Status Response: $res")
                delay(DATA_DELAY)
            }
        }
    }

    fun getData(): ByteArray {
        return ByteArray(3) { (Math.random()*255).toInt().toByte(); (Math.random()*255).toInt().toByte(); (Math.random()*255).toInt().toByte(); }
    }

    fun stopUpdates(){
        dataJob?.cancel()
        dataJob = null
    }
}