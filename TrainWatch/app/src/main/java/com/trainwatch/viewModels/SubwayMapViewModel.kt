package com.trainwatch.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainwatch.models.transit.TransitClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.trainwatch.models.transit.TransitVehicle
import java.net.URL

class SubwayMapViewModel : ViewModel(){

    private val urls = listOf(
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-g"),
        URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-jz")
    )
    private val apiKey = "QNEbklPk9aaslx6EVPxyE9SuE2mTZ8pN1REHODRp"

    private val client = TransitClient(apiKey)
    suspend fun populateTrainData(ioDispatcher: CoroutineDispatcher): Deferred<List<TransitVehicle>> = coroutineScope{
        viewModelScope.async(ioDispatcher){
            urls.flatMap { client.fetchVehicleData(it) }

        }
    }
}