package com.trainwatch.models.transit

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransitStop::class], version=2)
abstract class StopDatabase: RoomDatabase() {
    abstract fun transitStopDao(): TransitStopDao
    companion object: SingletonHolder<StopDatabase, Context>({
        Room.databaseBuilder(it, StopDatabase::class.java, "stops.db")
            .createFromAsset("nyc.db")
            .fallbackToDestructiveMigration()
            .build()
    })
}

open class SingletonHolder<D, C>(builder: (C) -> D){
    private var builder: ((C) -> D)? = builder
    @Volatile private var instance: D? = null

    fun getInstance(arg: C): D{
        val fullInstance = instance // Ensure instance has been fully created via assignment
        if(fullInstance != null) return fullInstance //return built instance w/o locking

        return synchronized(this){
            val fullInstance2 = instance // Ensure instance has been fully created via assignment
            if (fullInstance2 != null) fullInstance2
            else{ //Object has not been created, make new one (should only be called once)
                val newInstance = builder!!(arg)
                instance = newInstance
                builder = null
                newInstance
            }
        }
    }
}