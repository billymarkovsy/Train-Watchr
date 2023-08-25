package com.trainwatch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.trainwatch.databinding.ActivityNycmapBinding

class NYCMap : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNycmapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNycmapBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val url = URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-g")
//        val apiKey = "QNEbklPk9aaslx6EVPxyE9SuE2mTZ8pN1REHODRp"
//
//        val client = TransitClient(url, apiKey)
//        val vehicleData: List<TransitVehicle> = client.fetchVehicleData()
//
//        val subwayView: SubwayMapView = SubwayMapView(vehicleData, this, _)

    }
}
