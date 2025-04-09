package com.example.trainwatchrble.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainwatchrble.TrainAPIClient
import com.example.trainwatchrble.models.Train
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.URL

class TrainViewModel: ViewModel() {

    suspend fun fetchTrains(url: URL): Deferred<List<Train>> = coroutineScope {
        viewModelScope.async (Dispatchers.IO) {
            var trains = TrainAPIClient.fetchVehicleData(url, "8f9cb5ae161b4cce8a7e9b831dc023ce")
            trains.forEach { Log.i("Trains", it.toString()) }
            trains
        }
    }
}