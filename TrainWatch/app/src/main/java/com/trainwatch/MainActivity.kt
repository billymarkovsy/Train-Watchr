package com.trainwatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.trainwatch.enums.City
import com.trainwatch.enums.Line

private const val TAG: String = Constants.MAIN_ACTIVITY_TAG
class MainActivity : AppCompatActivity() {

    private var selectedRoutes: Set<String> = setOf()
    private var city: City? = City.NYC
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null){
            createLegend()
            createSubwayFragment()
        }
    }

    fun onCityRadioButtonClick(v: View){
        if(v is RadioButton){
            city = mapButtonToCity(v)
            createLegend()
        }
    }

    private fun mapButtonToCity(r: RadioButton): City?{
        return when(r.id){
            R.id.nyc_radio_button -> if (r.isChecked) City.NYC else null
            R.id.boston_radio_button -> if(r.isChecked) City.BOSTON else null
            else -> null
        }
    }

    private fun createLegend(){
        city?.let {c ->
            val lines = Line.cityLineNames[c.name]
            val legendView = this.findViewById<LinearLayout>(R.id.map_legend_button_group)
            legendView.removeAllViews()
            lines?.let { l -> l.forEach {lineName ->
                val lineButton = CheckBox(this)
                lineButton.text = lineName
                lineButton.tag = lineName
                lineButton.isChecked = true
                lineButton.setOnClickListener {v ->
                    updateRoutes()
                }
                legendView.addView(lineButton)
            }}
            updateRoutes()
        }
    }

    private fun updateRoutes(){
        val legendView = this.findViewById<LinearLayout>(R.id.map_legend_button_group)
        selectedRoutes = legendView.children.filter { v -> v is CheckBox && v.isChecked }
            .map {v -> v.tag.toString().split("_")[1]}.toSet()
        createSubwayFragment()
    }

    private fun createSubwayFragment(){
        city?.let{c ->
            val bundle = bundleOf(
                Pair(Constants.CITY_KEY, c.name),
                Pair(Constants.ROUTES_KEY, selectedRoutes.toList())
            )
            supportFragmentManager.commit {
                replace<SubwayFragment>(R.id.subway_fragment_container, args=bundle)
                setReorderingAllowed(true)
                addToBackStack(c.name)
            }
        }
    }
}