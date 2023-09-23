package com.trainwatch.viewModels

import android.util.Log
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

class NYCMapViewModel: SubwayMapViewModel() {

    override val apiKey: String = BuildConfig.NYCSecAPIKey
    override val city: City = City.NYC

    private val urls = listOf(
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-ace"),
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-bdfm"),
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-g"),
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-jz"),
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-nqrw"),
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-l"),
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs")
    )
    override suspend fun populateTrainData(apiKey: String, ioDispatcher: CoroutineDispatcher, routeSelector: Set<String>?)
    : Deferred<List<TransitVehicle>> = coroutineScope {
        viewModelScope.async(ioDispatcher){
            var vehicles = urls.flatMap { url -> TransitClient.fetchVehicleData(url, apiKey) }
            if(routeSelector?.isNotEmpty() == true){
                vehicles = vehicles.filter { v -> v.routeId in routeSelector }
                Log.i("TRAIN_TAG", vehicles.toString())
            }
            vehicles

        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory
            get() = viewModelFactory {
                initializer {
                    NYCMapViewModel()
                }
            }
    }
}