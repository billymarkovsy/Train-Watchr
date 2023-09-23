package com.trainwatch.viewModels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.trainwatch.BuildConfig
import com.trainwatch.enums.City
import com.trainwatch.models.transit.TransitClient
import com.trainwatch.models.transit.TransitVehicle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.URL

class BostonMapViewModel: SubwayMapViewModel() {

    override val apiKey: String =  BuildConfig.BostonSecAPIKey
    override val city: City = City.BOSTON

    private val url = URL("https://cdn.mbta.com/realtime/VehiclePositions.pb")
    private val validTrainRoutes: Set<String> = setOf("Blue", "Green-B", "Green-C", "Green-D", "Green-E", "Orange", "Mattapan", "Red")

    override suspend fun populateTrainData(apiKey: String, ioDispatcher: CoroutineDispatcher, routeSelector: Set<String>?)
    : Deferred<List<TransitVehicle>> = coroutineScope {
        viewModelScope.async(ioDispatcher){
            val vehicles = TransitClient.fetchVehicleData(url, apiKey).filter { v -> v.routeId in validTrainRoutes }
            routeSelector?.let { s ->
                vehicles.filter { v -> v.routeId in s }
            }
            vehicles

        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory
            get() = viewModelFactory {
                initializer {
                    BostonMapViewModel()
                }
            }
    }
}