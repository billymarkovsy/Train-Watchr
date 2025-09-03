package com.example.trainwatchrble.util

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
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.trainwatchrble.intents.BLEWorker

object BluetoothWrapper {

    var bluetoothManager: BluetoothManager? = null
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothLeScanner: BluetoothLeScanner
    var bluetoothDevice: BluetoothDevice? = null
    var bluetoothGatt: BluetoothGatt? = null

    fun initializeBLEManager(manager: BluetoothManager, bluetoothDisabledCallback: () -> Unit) {
        if (bluetoothManager == null){
            bluetoothManager = manager
            bluetoothAdapter = bluetoothManager!!.adapter
        }

        if (bluetoothAdapter.isEnabled && !::bluetoothLeScanner.isInitialized) {
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        }
        if(!::bluetoothLeScanner.isInitialized){
            bluetoothDisabledCallback()
        }
    }

    private var running = true

    fun getRunning(): Boolean { return running }

    fun enableLoop() { running = true }

    fun scanCallback(foundCB: () -> Unit, missedCB: () -> Unit): ScanCallback{
       return object : ScanCallback() {

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                if (bluetoothDevice != null)
                    return
                Log.d("BLE", result.device.name ?: "")
                if (result.device.name == Constants.TRAIN_WATCHR_DEVICE_NAME) {
                    if (bluetoothDevice == null)
                        bluetoothDevice = result.device
                    foundCB()
                }
                else {
                    missedCB()
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun initializeGatt(context: Context) {
        bluetoothGatt = bluetoothDevice!!.connectGatt(context, true, leGattCallback)
    }

    fun disconnect() {
        running = false
        WorkManager.getInstance().cancelAllWork()
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
                disconnect()
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(bluetoothGatt, status)
            val bleWork: WorkRequest = OneTimeWorkRequestBuilder<BLEWorker>()
                .build()
            WorkManager
                .getInstance()
                .enqueue(bleWork)
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            BLEWorker.pollData()
        }
    }

}