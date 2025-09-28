package com.example.trainwatchrble.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.WorkManager

object BluetoothWrapper {

    var bluetoothManager: BluetoothManager? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var bluetoothLeScanner: BluetoothLeScanner
    var bluetoothDevice: BluetoothDevice? = null
    var bluetoothGatt: BluetoothGatt? = null

    fun initializeBLEManager(manager: BluetoothManager, bluetoothDisabledCallback: () -> Unit) {
        if (bluetoothManager == null){
            bluetoothManager = manager
            bluetoothAdapter = bluetoothManager!!.adapter
        }

        if (bluetoothAdapter != null && bluetoothAdapter!!.isEnabled && !::bluetoothLeScanner.isInitialized) {
            bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
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

    fun disconnect() {
        running = false
        WorkManager.getInstance().cancelAllWork()
    }

}