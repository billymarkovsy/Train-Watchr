package com.trainwatch.enums

import android.util.Log
import androidx.annotation.ColorRes
import com.trainwatch.R

enum class Line(@ColorRes val color: Int) {
    NYC_1(R.color.one_train),
    NYC_2(R.color.two_train),
    NYC_3(R.color.three_train),
    NYC_4(R.color.four_train),
    NYC_5(R.color.five_train),
    NYC_6(R.color.six_train),
    //NYC_6X(R.color.six_train),
    NYC_7(R.color.seven_train),
    //NYC_7X(R.color.seven_train),
    NYC_A(R.color.a_train),
    NYC_B(R.color.b_train),
    NYC_C(R.color.c_train),
    NYC_D(R.color.d_train),
    NYC_E(R.color.e_train),
    NYC_F(R.color.f_train),
    NYC_G(R.color.g_train),
    //NYC_H(R.color.a_train),
    NYC_J(R.color.j_train),
    NYC_L(R.color.l_train),
    NYC_M(R.color.m_train),
    NYC_N(R.color.n_train),
    NYC_Q(R.color.q_train),
    NYC_R(R.color.r_train),
    NYC_S(R.color.shuttle),
    NYC_W(R.color.w_train),
    NYC_Z(R.color.z_train),
    BOSTON_BLUE(R.color.blue_line),
    BOSTON_RED(R.color.red_line),
    BOSTON_MATTAPAN(R.color.red_line),
    BOSTON_GREEN_B(R.color.green_line),
    BOSTON_GREEN_C(R.color.green_line),
    BOSTON_GREEN_D(R.color.green_line),
    BOSTON_GREEN_E(R.color.green_line),
    BOSTON_ORANGE(R.color.orange_line);
    companion object {
        private val allLineNames: Set<String> = Line.values().map { it.name }.toSet()
        val cityLineNames: Map<String, Set<String>> = City.values().fold(mutableMapOf()) { acc, city ->
            acc[city.name] = Line.values().map { it.name }.filter { it.startsWith(city.name) }.toSet()
            acc
        }

        fun getLineColor(city: City, line: String): Int {
            val fullName = "${city}_${line.replace("-", "_").uppercase()}"
            if(fullName in allLineNames)
                return Line.valueOf(fullName).color
            Log.i("TRAIN_TAG", fullName)
            return R.color.white
        }
    }
}