package com.grimsteel.locationshortcuts.car

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.grimsteel.locationshortcuts.R
import com.grimsteel.locationshortcuts.data.Shortcut
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date

class ShortcutSelectScreen(val region: String?, val type: String?, carContext: CarContext) : Screen(carContext) {
    companion object {
        private val DATE_FORMAT = SimpleDateFormat.getDateInstance()
        private const val METERS_IN_MILE = 1609
    }

    private var shortcuts: List<Shortcut>? = null
    private var currentLocation: Location? = null
    private val locationClient = LocationServices.getFusedLocationProviderClient(carContext.applicationContext)

    private fun sortByLocation() {
        if (currentLocation != null) {
            // we have the location, so sort right away
            currentLocation?.let { loc ->
                shortcuts = shortcuts?.sortedBy {
                    val location = Location("")
                    location.latitude = it.latitude
                    location.longitude = it.longitude
                    loc.distanceTo(location)
                }
                invalidate()
            }
        } else @SuppressLint("MissingPermission") if (hasLocationPermission()) {
            // we need to get the location
            locationClient.lastLocation.addOnSuccessListener {
                currentLocation = it
                if (it != null) sortByLocation()
            }
        }
    }

    private fun sortByLastUsed() {
        shortcuts = shortcuts?.sortedBy {
            it.lastUsed.time
        }?.reversed()
        invalidate()
    }

    private fun hasLocationPermission(): Boolean =
        carContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        carContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onGetTemplate(): Template {
        val shortcutDao = (carContext.applicationContext as com.grimsteel.locationshortcuts.LocationShortcutsApplication).shortcutsRepository
        if (shortcuts == null) {
            // Load the data and show a loading screen
            runBlocking {
                shortcuts = shortcutDao.getAllForRegionAndType(region, type).filterNotNull().first()
            }
        }
        // Build the shortcut list
        val itemList = ItemList.Builder()
        shortcuts!!.forEach { it ->
            val row = Row.Builder()
                // Only include the region if we're showing all regions
                .setTitle(if (region == null) "${it.label} in ${it.region}" else it.label)
                .setImage(getTypeIcon(carContext.applicationContext, it.type))
                .setOnClickListener {
                    // launch in directions app
                    val navigationUri = Uri.parse("geo:0,0").buildUpon()
                    navigationUri.appendQueryParameter("q", it.address)
                    val navigateIntent = Intent(CarContext.ACTION_NAVIGATE, navigationUri.build())
                    carContext.startCarApp(navigateIntent)

                    // set the last used time
                    lifecycleScope.launch(Dispatchers.IO) {
                        val newShortcut = it.copy(lastUsed = Date())
                        shortcutDao.update(newShortcut)
                    }
                }

            // show how far away it is
            currentLocation?.let { loc ->
                val location = Location("")
                location.latitude = it.latitude
                location.longitude = it.longitude
                row.addText("%.2f miles away".format((loc.distanceTo(location) / METERS_IN_MILE)))
            }

            row.addText("Last used: ${DATE_FORMAT.format(it.lastUsed)}")

            itemList.addItem(row.build())
        }

        val actionStrip = ActionStrip.Builder()

        @SuppressLint("MissingPermission") if (hasLocationPermission()) {
            // We have the location permission, so show a sort by location button
            actionStrip.addAction(
                Action.Builder()
                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext.applicationContext, R.drawable.rounded_distance_24)).build())
                    .setOnClickListener { sortByLocation() }
                    .build()
            )
        } else {
            // We don't have the location permission, so show a button to prompt for it
            actionStrip.addAction(
                Action.Builder()
                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext.applicationContext, R.drawable.rounded_distance_24)).build())
                    .setOnClickListener(ParkedOnlyOnClickListener.create {
                        carContext.requestPermissions(listOf(Manifest.permission.ACCESS_FINE_LOCATION)) { granted, _ ->
                            // Refresh to show distances
                            if (granted.contains(Manifest.permission.ACCESS_FINE_LOCATION))
                                sortByLocation()
                        }
                        CarToast.makeText(carContext, "Grant Location Shortcuts the location permission on your phone", CarToast.LENGTH_LONG).show()
                    })
                    .build()
            )
        }

        // sort by last used date
        actionStrip.addAction(
            Action.Builder()
                .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext.applicationContext, R.drawable.round_access_time_24)).build())
                .setOnClickListener { sortByLastUsed() }
                .build()
        )

        return ListTemplate.Builder()
            .setTitle("${type ?: "All Categories"} in ${region ?: "All Regions"}")
            .setHeaderAction(Action.BACK)
            .setLoading(false)
            .setSingleList(itemList.build())
            .setActionStrip(actionStrip.build())
            .build()
    }
}