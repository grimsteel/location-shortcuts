package com.grimsteel.locationshortcuts.car

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.grimsteel.locationshortcuts.R

@DrawableRes
private fun getTypeDrawable(type: String): Int {
    return if ("home" in type || "house" in type) R.drawable.ic_home
    else if ("work" in type || "office" in type) R.drawable.ic_work
    else if ("medical" in type || "doctor" in type || type.contains("hospital")) R.drawable.ic_local_hospital
    else if ("gas" in type) R.drawable.ic_local_gas_station
    else if ("store" in type || "grocer" in type) R.drawable.ic_local_grocery_store
    else if ("education" in type || "school" in type) R.drawable.ic_school
    else if ("misc" in type || "other" in type) R.drawable.ic_home_work
    else R.drawable.ic_not_listed_location
}

fun getTypeIcon(context: Context, type: String): CarIcon =
    CarIcon.Builder(IconCompat.createWithResource(context, getTypeDrawable(type.lowercase()))).build()