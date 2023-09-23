package com.trainwatch.enums

// corner 1 -> (0,0)
// corner 2 -> (h,w)
enum class City(val corner1: Pair<Double, Double>, val corner2: Pair<Double, Double>) {
    NYC(Pair(40.91638,-74.00500), Pair(40.59782,-73.75027)),
    BOSTON(Pair(42.40145,-71.15958), Pair(42.2000,-70.98107))
}