package com.trainwatch.models.transit

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Query
import java.lang.IllegalArgumentException
enum class TransitStopOrientation{
    HORIZONTAL,
    VERTICAL
}
@Entity(tableName="CityStopsSimple")
data class TransitStop(
    val city: String,
    @PrimaryKey val stopId: String,
    val stopName: String,
    val stopLat: Double,
    val stopLong: Double,
    val routes: String?,
    val orientation: String
){

    val generalStopId: String
        get(){
            if(this.stopId.endsWith("N", true) || this.stopId.endsWith("S", true))
                return this.stopId.substring(0,this.stopId.length-1)
            return this.stopId
        }
    @get:Ignore
    private val stopOrientation: TransitStopOrientation
        get() {
            return if(orientation.lowercase() == "H")
                TransitStopOrientation.HORIZONTAL
            else if(orientation.lowercase() == "L")
                TransitStopOrientation.VERTICAL
            else
                throw IllegalArgumentException("Invalid stop orientation")
        }
}

@Dao
interface TransitStopDao {
    @Query("SELECT * FROM CityStopsSimple")
    fun getAllStops(): List<TransitStop>

    @Query("SELECT * FROM CityStopsSimple WHERE city = :cityName")
    fun getStopsByCityName(cityName: String): List<TransitStop>

//    @Query("SELECT * FROM CityStopsSimple WHERE city=:cityName AND stopId")

//    @Query("SELECT stopLat, stopLong FROM CityStopsSimple WHERE stopId = :stopId")
//    fun getStopPositionById(stopId: String): TransitStopPosition?
}

//data class TransitStopPosition(val x: Int, val y: Int)