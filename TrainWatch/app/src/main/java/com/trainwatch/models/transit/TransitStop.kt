package com.trainwatch.models.transit

data class TransitStop(val id: String, val name: String, val routeId: String, val stopSequence: String?,
    val longitude: Double?, val latitude: Double?)
