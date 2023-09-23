package com.trainwatch

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.trainwatch.enums.City
import com.trainwatch.models.transit.StopDatabase
import com.trainwatch.models.transit.TransitStop
import com.trainwatch.models.transit.TransitVehicle
import com.trainwatch.viewModels.BostonMapViewModel
import com.trainwatch.viewModels.NYCMapViewModel
import com.trainwatch.viewModels.SubwayMapViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList

class SubwayFragment : Fragment() {
    private var city: String? = null
    private var routes: Set<String>? = null
    private val nycMapViewModel: NYCMapViewModel by viewModels()
    private val bostonMapViewModel: BostonMapViewModel by viewModels()
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("TRAIN_TAG", "New fragment made")
        arguments?.let {
            city = it.getString(Constants.CITY_KEY)
            routes = it.getStringArrayList(Constants.ROUTES_KEY)?.toSet()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subway, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context: Context = requireContext().applicationContext
        val subwayViewModel: SubwayMapViewModel? = fetchCityViewModel()
        subwayViewModel?.let { vm ->
            val subwayView: SubwayMapView? = view.findViewById(R.id.city_subway_view)
            subwayView?.let {v ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        val stopDao = StopDatabase.getInstance(context).transitStopDao()
                        val stopData: List<TransitStop> = vm.populateStopData(stopDao, ioDispatcher).await()
                        v.setCity(city!!)
                        v.setStops(stopData)
                        while (true) {
                            val trainData: List<TransitVehicle> = vm.populateTrainData(vm.apiKey, ioDispatcher, routes).await()
                            v.setVehicles(trainData)
                            delay(10000)
                        }
                    }
                }
            }
        }
    }
    private fun fetchCityViewModel(): SubwayMapViewModel?{
        return when(city){
            City.NYC.name -> nycMapViewModel
            City.BOSTON.name -> bostonMapViewModel
            else -> null
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param city Parameter 1.
         * @param routes Parameter 2
         * @return A new instance of fragment SubwayFragment.
         */
        @JvmStatic
        fun newInstance(city: String, routes: Set<String>) =
            SubwayFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.CITY_KEY, city)
                    putStringArrayList(Constants.ROUTES_KEY, routes.toList() as ArrayList<String>)
                }
            }
    }
}