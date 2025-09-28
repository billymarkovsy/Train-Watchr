package com.example.trainwatchrble

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainwatchrble.service.BLEService
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

    private lateinit var bleServiceIntent: Intent

    //@android:drawable/ic_delete
    //@android:drawable/presence_online

    private lateinit var bluetoothDisabledSnackBar: Snackbar
    private lateinit var bluetoothOffSnackBar: Snackbar

    private var scanning = false

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

    @RequiresPermission(allOf=[Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
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

        bluetoothDisabledSnackBar = Snackbar.make(mainLayout, "Bluetooth permissions not enabled!", LENGTH_SHORT)
        bluetoothOffSnackBar = Snackbar.make(mainLayout, "Turn on Bluetooth in settings!", LENGTH_SHORT)

        checkAndRequestPermission(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))

        bleServiceIntent = Intent(this, BLEService::class.java)

        BluetoothWrapper.initializeBLEManager(getSystemService(BluetoothManager::class.java), bluetoothOffSnackBar::show)
        BluetoothWrapper.disconnect()

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
            BluetoothWrapper.initializeBLEManager(getSystemService(BluetoothManager::class.java), bluetoothOffSnackBar::show)
            if(BluetoothWrapper.bluetoothAdapter != null && BluetoothWrapper.bluetoothAdapter!!.isEnabled)
                connectToServer()
            else
                Toast.makeText(this, "Please enable bluetooth before continuing", LENGTH_SHORT).show()
        }

        sendDataToServerButton.setOnClickListener { _ ->

            if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                if (BluetoothWrapper.bluetoothDevice != null) {
                    BluetoothWrapper.bluetoothGatt = BluetoothWrapper.bluetoothDevice!!.connectGatt(this, true, leGattCallback)
                    //BluetoothWrapper.initializeGatt(this)
                    disconnectFromServerButton.isEnabled = true
                    sendDataToServerLoader.visibility = View.VISIBLE
                } else
                    Log.i("BLE", "Unable to connect Bluetooth device1")
            } else {
                bluetoothDisabledSnackBar.show()
                return@setOnClickListener
            }
        }

        disconnectFromServerButton.setOnClickListener { _ ->
            BluetoothWrapper.disconnect()
            stopService(bleServiceIntent)
            disconnectFromServerButton.isEnabled = false
            sendDataToServerLoader.visibility = View.INVISIBLE
        }

        val channelName = getString(R.string.notification_channel_name)
        val mChannel = NotificationChannel(channelName, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Channel for the BLE Service to start syncing data"
            setShowBadge(true)
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun connectToServer() {
        val handler = Handler(Looper.getMainLooper())

        val leScanCallback: ScanCallback = BluetoothWrapper.scanCallback({
            Log.i("BLE", "Found TrainWatchr Server!")
            scanning = false
            sendDataToServerButton.isEnabled = true
            connectToServerStatus.setImageDrawable(AppCompatResources.getDrawable(applicationContext,R.mipmap.green_check))
        }, {
//            Log.i("BLE", "Unable to find TrainWatchr Server")
//            connectToServerStatus.setImageDrawable(AppCompatResources.getDrawable(applicationContext,R.mipmap.red_x))
        })

        if(checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                Log.i("BLE", "Beginning scan...")

                connectToServerLoader.visibility = View.VISIBLE
                connectToServerStatus.visibility = View.INVISIBLE

                handler.postDelayed({
                    scanning = false
                    BluetoothWrapper.bluetoothLeScanner.stopScan(leScanCallback)

                    connectToServerLoader.visibility = View.INVISIBLE
                    connectToServerStatus.visibility = View.VISIBLE

                    if (BluetoothWrapper.bluetoothDevice == null) {
                        Log.i("BLE", "Unable to find TrainWatchr Server")
                        connectToServerStatus.setImageDrawable(AppCompatResources.getDrawable(applicationContext,R.mipmap.red_x))
                    }

                }, Constants.BLE_SCAN_PERIOD)
                scanning = true
                BluetoothWrapper.bluetoothLeScanner.startScan(leScanCallback)
            } else {
                scanning = false
                BluetoothWrapper.bluetoothLeScanner.stopScan(leScanCallback)
            }
        }else{
            scanning = false
            BluetoothWrapper.bluetoothLeScanner.stopScan(leScanCallback)
            bluetoothDisabledSnackBar.show()
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
            Log.d("BLE", "Change MTU size: ${test.toString()}")
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.i("BLE", "Connected")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i("BLE", "Disconnected")
                BluetoothWrapper.disconnect()
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(BluetoothWrapper.bluetoothGatt, status)

            startService(bleServiceIntent)
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            BLEService.pollData()
        }
    }

}