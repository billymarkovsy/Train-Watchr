package com.example.trainwatchrble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.WorkManager
import com.example.trainwatchrble.util.BluetoothWrapper
import com.example.trainwatchrble.util.Constants
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var connectToServerButton: Button
    private lateinit var sendDataToServerButton: Button
    private lateinit var connectToServerLoader: ProgressBar
    private lateinit var sendDataToServerLoader: ProgressBar
    private lateinit var connectToServerStatus: ImageView
    private lateinit var disconnectFromServerButton: Button

    //@android:drawable/ic_delete
    //@android:drawable/presence_online

    private lateinit var bluetoothDisabledSnackBar: Snackbar
    private lateinit var bluetoothOffSnackBar: Snackbar

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner

    private var scanning = false
    private var discovered = false


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

        initializeBLEScanner()

        bluetoothDisabledSnackBar = Snackbar.make(mainLayout, "Bluetooth permissions not enabled!", LENGTH_SHORT)
        bluetoothOffSnackBar = Snackbar.make(mainLayout, "Turn on Bluetooth in settings!", LENGTH_SHORT)

        checkAndRequestPermission(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))

        connectToServerButton = findViewById(R.id.serverConnectButton)
        sendDataToServerButton = findViewById(R.id.serverDataButton)
        connectToServerLoader = findViewById(R.id.serverConnectProgressBar)
        sendDataToServerLoader = findViewById(R.id.serverDataProgressBar)
        connectToServerStatus = findViewById(R.id.serverConnectStatus)
        disconnectFromServerButton = findViewById(R.id.serverDisconnectButton)

        sendDataToServerLoader.visibility = View.INVISIBLE
        connectToServerLoader.visibility = View.INVISIBLE
        connectToServerStatus.visibility = View.VISIBLE
        sendDataToServerButton.isEnabled = false
        disconnectFromServerButton.isEnabled = false

        connectToServerButton.setOnClickListener { _ ->
            initializeBLEScanner()
            connectToServer()
        }

        sendDataToServerButton.setOnClickListener { _ ->

            if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                if (BluetoothWrapper.deviceInitialized) {
                    BluetoothWrapper.initializeGatt(this)
                    disconnectFromServerButton.isEnabled = true
                    sendDataToServerLoader.visibility = View.VISIBLE
                } else
                    Log.i("BLE", "Unable to connect Bluetooth device")
            } else {
                bluetoothDisabledSnackBar.show()
                return@setOnClickListener
            }
                Log.i("BLE", "test")
        }

        disconnectFromServerButton.setOnClickListener { _ ->
            BluetoothWrapper.disconnect()
            disconnectFromServerButton.isEnabled = false
            sendDataToServerLoader.visibility = View.INVISIBLE
        }
    }

    private fun connectToServer() {
        val handler = Handler(Looper.getMainLooper())

        if(checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                Log.i("BLE", "Beginning scan...")

                connectToServerLoader.visibility = View.VISIBLE
                connectToServerStatus.visibility = View.INVISIBLE

                handler.postDelayed({
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)

                    connectToServerLoader.visibility = View.INVISIBLE
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

    private fun initializeBLEScanner() {
        if (!this::bluetoothAdapter.isInitialized){
            bluetoothManager = getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.adapter
        }

        if (bluetoothAdapter.isEnabled) {
            if (!this::bluetoothLeScanner.isInitialized){
                bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            }
        } else {
            bluetoothOffSnackBar.show()
        }
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
                if (!BluetoothWrapper.deviceInitialized)
                    BluetoothWrapper.initializeDevice(result.device)
            }
            else {
                connectToServerStatus.setImageDrawable(AppCompatResources.getDrawable(applicationContext,R.mipmap.red_x))
            }
        }
    }
}