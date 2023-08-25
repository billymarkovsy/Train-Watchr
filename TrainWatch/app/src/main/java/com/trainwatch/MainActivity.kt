package com.trainwatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.trainwatch.viewModels.SubwayMapViewModel
import kotlinx.coroutines.*
import com.trainwatch.models.transit.TransitVehicle

private const val TAG: String = Constants.MAIN_ACTIVITY_TAG
class MainActivity : AppCompatActivity() {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val subwayView: SubwayMapView = findViewById(R.id.nyc_subway)
        val subwayViewModel: SubwayMapViewModel by viewModels()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val trainData: List<TransitVehicle> = subwayViewModel.populateTrainData(ioDispatcher).await()
                    //trainData.forEach {Log.i(TAG, it.toString())}
                    subwayView.setVehicles(trainData)
                    delay(15000)
                }
            }
        }

//        val layout = findViewById<GridLayout>(R.id.layout)
//        layout.columnCount = Constants.BLUE_COLUMNS
//        layout.rowCount = Constants.BLUE_ROWS
//        layout.alignmentMode = GridLayout.ALIGN_BOUNDS
//        for(i in 0 until layout.columnCount){
//            for (j in 0 until layout.rowCount){
//                val block = TextView(applicationContext)
//                block.width = 30
//                block.height = 30
//                block.text = "-"
//                block.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.white))
//                block.id = (i*Constants.BLUE_COLUMNS)+j
//                val params = GridLayout.LayoutParams()
//                params.setMargins(4)
//                params.rowSpec = GridLayout.spec(i,1)
//                params.columnSpec = GridLayout.spec(j, 1)
//                layout.addView(block,params)
//            }
//        }
//
//        val cronetEngineBuilder: CronetEngine.Builder = CronetEngine.Builder(this)
//        val cronetEngine: CronetEngine = cronetEngineBuilder.build()
//        val mbtaViewModel: MBTAMapViewModel by viewModels()
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                while (true) {
//                    layout.forEach {
//                        it.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.white))
//                    }
//                    val locations = mbtaViewModel.populateTrainData(cronetEngine, ioDispatcher).await()
//                    Log.i(TAG, locations.toString())
//                    locations.forEach {
//                        val block = layout.findViewById<TextView>(it.second*24+it.first)
//                        block.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.teal_200))
//                    }
//                    delay(10000)
//                }
//            }
//        }
    }
}