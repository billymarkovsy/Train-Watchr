package com.trainwatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.navigation.NavigationView
import com.trainwatch.enums.City
import com.trainwatch.enums.Line

class MainActivity : AppCompatActivity() {

    private var city: City? = City.NYC
    private var drawerLayout: DrawerLayout? = null
    private var navView: NavigationView? = null
    private var selectedRoutes: MutableSet<String> = refreshRoutes(city)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = this.findViewById(R.id.drawer_layout)
        navView = this.findViewById(R.id.route_navigation_drawer)

        if(savedInstanceState == null){
            createLegend()
            createSubwayFragment()

            val drawerListener: SimpleDrawerListener = object : SimpleDrawerListener(){
                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                    createSubwayFragment()
                }

                override fun onDrawerOpened(drawerView: View) {
                    val navView = drawerView as NavigationView
                    val cityView = navView.menu.children.first{child -> child.itemId == R.id.city_item}
                    cityView.subMenu?.children?.forEach { it.isChecked = it.title == city?.name }
                    val legendView = navView.menu.children.first{child -> child.itemId == R.id.route_item}
                    legendView.subMenu?.children?.forEach { it.isChecked = it.title in selectedRoutes }
                    super.onDrawerOpened(drawerView)
                }
            }
            drawerLayout?.addDrawerListener(drawerListener)
            navView?.setNavigationItemSelectedListener {i ->
                val newCheckVal = !i.isChecked
                i.isChecked = newCheckVal
                if(newCheckVal){
                    selectedRoutes.add(i.title!!.toString())
                } else{
                    selectedRoutes.remove(i.title!!.toString())
                }
                false// Return false to have display reflect the current list
            }

            val t: Button = findViewById(R.id.test_button)
            t.setOnClickListener { drawerLayout?.openDrawer(GravityCompat.END) }
        }
    }

    fun onCityMenuItemClick(item: MenuItem){
        val cityName: String = item.title.toString()
        if(cityName in City.cityNames){
            this.city = City.valueOf(cityName)
        }
        val cityView = navView?.menu?.children?.first{child -> child.itemId == R.id.city_item}
        cityView?.subMenu?.children?.forEach { it.isChecked = it.title == cityName }
        this.selectedRoutes = this.refreshRoutes(this.city)
        this.createLegend()
    }

    private fun createLegend(){
        city?.let {c ->
            val lines = Line.cityLineNames[c.name]
            val legendView = navView!!.menu.children.first{child -> child.itemId == R.id.route_item}
            val routeMenu: SubMenu? = legendView.subMenu
            routeMenu?.let { menu ->
                menu.clear()
                lines?.forEach { lineName ->
                    val lineNameData: List<String> = lineName.split("_")
                    val reducedLineName = lineNameData.slice(1 until lineNameData.size).joinToString("-")
                    val menuItem = menu.add(reducedLineName)
                    menuItem.isCheckable = true
                    menuItem.isChecked = selectedRoutes.contains(reducedLineName)
                }
            }
        }
    }

    private fun createSubwayFragment(){
        city?.let {c ->
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

    private fun refreshRoutes(city: City?): MutableSet<String>{
        return Line.cityLineNames[city?.name]!!.map {l ->
            val lineNameData: List<String> = l.split("_")
            val res = lineNameData.slice(1 until lineNameData.size).joinToString("-")
            res
        }.toMutableSet()
    }
}