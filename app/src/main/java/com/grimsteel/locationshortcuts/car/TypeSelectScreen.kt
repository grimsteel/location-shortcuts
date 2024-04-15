package com.grimsteel.locationshortcuts.car

import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarIconSpan
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.grimsteel.locationshortcuts.R
import com.grimsteel.locationshortcuts.data.TypeWithCount

class TypeSelectScreen(carContext: CarContext) : Screen(carContext) {
    private var types: List<TypeWithCount>? = null
    private var currentRegion: String? = null
    private var fetchCurrentRegion = true
    private val loadingScreen = ListTemplate.Builder()
        .setTitle("Select Category")
        .setHeaderAction(Action.APP_ICON)
        .setLoading(true)
        .build()

    private val application = carContext.applicationContext as com.grimsteel.locationshortcuts.LocationShortcutsApplication

    private fun promptRegion() {
        screenManager.pushForResult(
            RegionSelectScreen(
                carContext
            )
        ) { selectedRegion ->
            currentRegion = selectedRegion as String?
            // Remember their choice
            lifecycleScope.launch(Dispatchers.IO) {
                application.preferences.edit {
                    // Remove it if they selected null
                    if (currentRegion == null) it.remove(com.grimsteel.locationshortcuts.LocationShortcutsApplication.LAST_SELECTED_REGION_KEY)
                    else it[com.grimsteel.locationshortcuts.LocationShortcutsApplication.LAST_SELECTED_REGION_KEY] = currentRegion!!
                }
            }
            // we need to refresh counts
            types = null
            invalidate()
        }
    }

    private fun makeRow(text: String, type: String?, icon: CarIcon, count: Int): Row {
        val rowTitle = SpannableString("• $text")
        // Replace the bullet with the correct icon
        rowTitle.setSpan(
            CarIconSpan.create(
                icon,
                CarIconSpan.ALIGN_CENTER
            ),
            0,
            1,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return Row.Builder()
            .setTitle(rowTitle)
            .setNumericDecoration(count)
            .setOnClickListener {
                // Navigate to the list for this type
                screenManager.push(
                    ShortcutSelectScreen(currentRegion, type, carContext)
                )
            }
            .build()
    }

    override fun onGetTemplate(): Template {
        if (types == null) {
            // Fetch the types and current region
            lifecycleScope.launch(Dispatchers.IO) {
                if (fetchCurrentRegion) {
                    // Get the current region from prefs
                    currentRegion =
                        application.preferences.data.map { it[com.grimsteel.locationshortcuts.LocationShortcutsApplication.LAST_SELECTED_REGION_KEY] }
                            .first()

                    // We only need to fetch this once
                    fetchCurrentRegion = false
                }

                // If it's null, default to all regions
                types = (
                    currentRegion?.let {
                        application.shortcutsRepository.getAllTypesWithCountForRegion(it)
                    } ?: application.shortcutsRepository.getAllTypesWithCount()
                ).filterNotNull().first()
                invalidate()
            }
            return loadingScreen
        } else {
            val itemList = ItemList.Builder()
            // Build the list
            types!!.forEach {
                itemList.addItem(makeRow(it.type, it.type, getTypeIcon(carContext.applicationContext, it.type), it.count))
            }
            // All types
            itemList.addItem(makeRow(
                "All Categories",
                null,
                CarIcon.Builder(IconCompat.createWithResource(carContext.applicationContext, R.drawable.ic_asterisk)).build(),
                types!!.sumOf { it.count }
            ))
            return ListTemplate.Builder()
                .setTitle("Select Category (${currentRegion ?: "All Regions"})")
                .setHeaderAction(Action.APP_ICON)
                .setLoading(false)
                .setSingleList(
                    itemList.build()
                )
                // Add change region FAB
                .addAction(
                    Action.Builder()
                        .setIcon(
                            CarIcon.Builder(
                                IconCompat
                                    .createWithResource(carContext.applicationContext, R.drawable.round_edit_location_alt_24)
                            ).build()
                        )
                        .setBackgroundColor(CarColor.PRIMARY)
                        .setOnClickListener { promptRegion() }
                        .build()
                )
                .build()
        }
    }
}