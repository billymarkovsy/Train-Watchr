package com.example.trainwatchrble.util

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.trainwatchrble.intents.BLEWorker

object BluetoothWrapper {

    lateinit var bluetoothDevice: BluetoothDevice
    lateinit var bluetoothGatt: BluetoothGatt

    var deviceInitialized = false
    var gattInitialized = false
    private var running = true

    fun getRunning(): Boolean { return running }

    fun enableLoop() { running = true }

    fun initializeDevice(device: BluetoothDevice) {
        bluetoothDevice = device
        deviceInitialized = true
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun initializeGatt(context: Context) {
        bluetoothGatt = bluetoothDevice.connectGatt(context, true, leGattCallback)
        gattInitialized = true
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
            Log.i("BLE", "Change MTU size: ${test.toString()}")
            Log.i("BLE", "Connected")
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