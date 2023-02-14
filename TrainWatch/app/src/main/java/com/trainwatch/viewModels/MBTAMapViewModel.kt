package com.trainwatch.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainwatch.Constants
import com.trainwatch.enums.Route
import com.trainwatch.models.mbta.MBTAVehicle
import com.trainwatch.models.mbta.MBTATransitRequestCallback
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.chromium.net.UrlRequest
import org.chromium.net.CronetEngine
import java.util.concurrent.Executors

private const val TAG: String = "MBTAViewModelTAG"
class MBTAMapViewModel: ViewModel() {

    var allTrains: List<MBTAVehicle> = listOf()

    suspend fun populateTrainData(cronetEngine: CronetEngine, ioDispatcher: CoroutineDispatcher): Deferred<List<Pair<Int,Int>>> = coroutineScope{
        viewModelScope.async(ioDispatcher){
            val mbtaCallback = MBTATransitRequestCallback()
            val requestBuilder = cronetEngine.newUrlRequestBuilder(
                "https://api-v3.mbta.com/vehicles?route_type=0,1&include=route&fields[vehicle]=bearing,latitude,longitude,speed,label,current_status,direction_id",
                mbtaCallback,
                Executors.newSingleThreadExecutor()
            )
            requestBuilder.setHttpMethod(Constants.HTTP_GET_REQUEST)
            requestBuilder.addHeader(Constants.X_API_KEY, "8f9cb5ae161b4cce8a7e9b831dc023ce")
            requestBuilder.addHeader(Constants.ACCEPT, Constants.JSON_API_FORMAT)

            val request: UrlRequest = requestBuilder.build()
            request.start()
            val response: String =
                runBlocking { fetchMBTAData(request, mbtaCallback, ioDispatcher) }
            val responseObject: JsonObject = Json.parseToJsonElement(response) as JsonObject
            val dataArray: JsonArray = responseObject[Constants.DATA] as JsonArray
            allTrains = dataArray.map { ele -> convertJsonToMBTAResponse(ele as JsonObject) } .filterNotNull()

            val blueTrains: List<MBTAVehicle> = allTrains.filter{t -> t.route == Route.BLUE}

            blueTrains.map {
                val stopSequence = it.stop?.stopSequence?.toInt()
                checkNotNull(stopSequence)
                val stopIndex = if (it.direction_id == 0) stopSequence else 13 - stopSequence
                Constants.BLUE_MAP.getOrDefault(stopIndex, Pair(0,0))
            }
        }
    }

    private suspend fun fetchMBTAData(request: UrlRequest, callback: MBTATransitRequestCallback, ioDispatcher: CoroutineDispatcher): String {
        withContext(ioDispatcher) {
            val requestCompleted = pollRequestStatus(request)
            Log.i(TAG, "Request finished")
            //TODO: we have the flexibility to execute other logic here if needed
        }
        return callback.getData()
    }

    private suspend fun pollRequestStatus(request: UrlRequest) = coroutineScope {
        launch{
            while(!request.isDone)
                delay(Constants.REQUEST_STATUS)
        }
    }

    private fun convertJsonToMBTAResponse(element: JsonObject): MBTAVehicle? {
        return try {
            val attributes: JsonElement? = element[Constants.ATTRIBUTES]
            checkNotNull(attributes)
            val mbtaObj: MutableMap<String, JsonElement> = (attributes as JsonObject).toMutableMap()

            val id: JsonElement? = element[Constants.ID]
            checkNotNull(id)
            mbtaObj[Constants.ID] = id

            val routeId: JsonElement =
                (((element[Constants.RELATIONSHIPS] as JsonObject)[Constants.ROUTE] as JsonObject)[Constants.DATA] as JsonObject)[Constants.ID] as JsonElement
            mbtaObj[Constants.ROUTE_ID] = routeId

            mbtaObj[Constants.STOP_ID] =
                (((element[Constants.RELATIONSHIPS] as JsonObject)[Constants.STOP_INFO] as JsonObject)[Constants.DATA] as JsonObject)[Constants.ID] as JsonElement

            return Json.decodeFromJsonElement(JsonObject(mbtaObj))
        } catch (e: java.lang.ClassCastException) { null }
    }
}
