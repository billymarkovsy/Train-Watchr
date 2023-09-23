package com.trainwatch.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainwatch.enums.City
import com.trainwatch.models.transit.TransitStop
import com.trainwatch.models.transit.TransitStopDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import com.trainwatch.models.transit.TransitVehicle
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

abstract class SubwayMapViewModel : ViewModel(){

    abstract val apiKey: String
    abstract val city: City
    abstract suspend fun populateTrainData(apiKey: String, ioDispatcher: CoroutineDispatcher, routeSelector: Set<String>? = null): Deferred<List<TransitVehicle>>

    suspend fun populateStopData(stopDao: TransitStopDao, ioDispatcher: CoroutineDispatcher)
        : Deferred<List<TransitStop>> = coroutineScope {

        viewModelScope.async(ioDispatcher) {
                stopDao.getStopsByCityName(city.name)
        }
    }
}