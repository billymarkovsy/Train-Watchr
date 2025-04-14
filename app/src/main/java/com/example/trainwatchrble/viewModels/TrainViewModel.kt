package com.example.trainwatchrble.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainwatchrble.TrainAPIClient
import com.example.trainwatchrble.models.Train
import com.example.trainwatchrble.BuildConfig
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.URL

class TrainViewModel: ViewModel() {

    suspend fun fetchTrains(url: URL): Deferred<List<Train>> = coroutineScope {
        viewModelScope.async (Dispatchers.IO) {
            val trains = TrainAPIClient.fetchVehicleData(url, BuildConfig.API_KEY)
            trains
        }
    }
}