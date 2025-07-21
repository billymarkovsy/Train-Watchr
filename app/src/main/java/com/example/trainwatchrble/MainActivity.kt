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
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.trainwatchrble.intents.BLEIntent
import com.example.trainwatchrble.models.Train
import com.example.trainwatchrble.util.Constants
import com.example.trainwatchrble.viewModels.TrainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var connectToServerButton: Button
    private lateinit var sendDataToServerButton: Button
    private lateinit var connectToServerLoader: ProgressBar
    private lateinit var sendDataToServerLoader: ProgressBar
    private lateinit var connectToServerStatus: ImageView
    private lateinit var sendDataToServerStatus: ImageView

    //@android:drawable/ic_delete
    //@android:drawable/presence_online

    private lateinit var bluetoothDisabledSnackBar: Snackbar
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var bluetoothGatt: BluetoothGatt

    private val trainViewModel: TrainViewModel by viewModels()

    private var scanning = false
    //private var connecting = false
    private var discovered = false

    private var dataJob: Job? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if( grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)
                    Log.i("BLE", "Permission granted")
                else
                    bluetoothDisabledSnackBar.show()
            }
        }
    }

    private fun checkPermission(p: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == checkSelfPermission(p)
    }

    private fun checkAndRequestPermission(permissions: Array<String>){
        val pendingPermissions: MutableList<String> = mutableListOf()
        permissions.forEach { permission ->
            val hasPermission = checkPermission(permission)
            if (hasPermission)
                return
            else
                pendingPermissions.add(permission)
        }


        requestPermissions(pendingPermissions.toTypedArray(), 1)
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(value = "android. permission. BLUETOOTH_SCAN")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val mainLayout = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        bluetoothDisabledSnackBar = Snackbar.make(mainLayout, "Bluetooth permissions not enabled!", LENGTH_SHORT)

        checkAndRequestPermission(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))
        //checkAndRequestPermission(Manifest.permission.BLUETOOTH_SCAN)

        connectToServerButton = findViewById(R.id.serverConnectButton)
        sendDataToServerButton = findViewById(R.id.serverDataButton)
        connectToServerLoader = findViewById(R.id.serverConnectProgressBar)
        sendDataToServerLoader = findViewById(R.id.serverDataProgressBar)
        connectToServerStatus = findViewById(R.id.serverConnectStatus)
        sendDataToServerStatus = findViewById(R.id.serverDataStatus)

        sendDataToServerLoader.visibility = View.GONE
        connectToServerLoader.visibility = View.GONE
        sendDataToServerStatus.visibility = View.GONE
        connectToServerStatus.visibility = View.VISIBLE
        sendDataToServerButton.isEnabled = false

        connectToServerButton.setOnClickListener { _ -> connectToServer() }

        sendDataToServerButton.setOnClickListener { _ ->sendDataToServer() }
    }

    private fun connectToServer() {
        val handler = Handler(Looper.getMainLooper())

        if(checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                Log.i("BLE", "Beginning scan...")

                connectToServerLoader.visibility = View.VISIBLE
                connectToServerStatus.visibility = View.GONE

                handler.postDelayed({
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)

                    connectToServerLoader.visibility = View.GONE
                    connectToServerStatus.visibility = View.VISIBLE

                    if (!discovered) {
                        Log.i("BLE", "Unable to find TrainWatchr Server")

                    }

                }, Constants.BLE_SCAN_PERIOD)
                scanning = true
                bluetoothLeScanner.startScan(leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }
        }else{
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            bluetoothDisabledSnackBar.show()
        }
    }

    private fun sendDataToServer() {
        if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (::bluetoothDevice.isInitialized) {
                bluetoothGatt = bluetoothDevice.connectGatt(this, true, leGattCallback)
            } else
                Log.i("BLE", "Unable to connect Bluetooth device")
        } else
            bluetoothDisabledSnackBar.show()
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
                sendDataToServerButton.isEnabled = true
                discovered = true
                connectToServerStatus.setImageDrawable(AppCompatResources.getDrawable(applicationContext,R.mipmap.green_check))
                if (!this@MainActivity::bluetoothDevice.isInitialized)
                    bluetoothDevice = result.device
            }
            else {
                connectToServerStatus.setImageDrawable(AppCompatResources.getDrawable(applicationContext,R.mipmap.red_x))
            }
        }
    }

    private val leGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.i("BLE", "MTU size $mtu, status: $status")
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val test = gatt?.requestMtu(517)
            Log.i("BLE", "Change MTU size: ${test.toString()}")
            Log.i("BLE", "Connected")
            scanning = false
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.i("BLE", "Connected")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i("BLE", "Disconnected")
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(bluetoothGatt, status)
            dataLoop()
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            BLEIntent.pollData(bluetoothGatt)
        }
    }

    private fun dataLoop(){
        stopUpdates()
        dataJob = lifecycleScope.launch {
            checkPermission(Manifest.permission.BLUETOOTH_CONNECT)
            while(true) {

                val trains: List<Train> = trainViewModel.fetchTrains(Constants.MBTA_URL).await()
                BLEIntent.processTrains(bluetoothGatt, trains)
                delay(Constants.TRAIN_DATA_REFRESH)
            }
        }
    }

    private fun stopUpdates(){
        dataJob?.cancel()
        dataJob = null
    }
}