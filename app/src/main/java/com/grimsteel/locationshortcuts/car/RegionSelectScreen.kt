package com.grimsteel.locationshortcuts.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class RegionSelectScreen(carContext: CarContext) : Screen(carContext) {
    private var regions: List<String>? = null

    override fun onGetTemplate(): Template {
        val shortcutDao = (carContext.applicationContext as com.grimsteel.locationshortcuts.LocationShortcutsApplication).shortcutsRepository
        if (regions == null) {
            runBlocking {
                regions = shortcutDao.getAllRegions().filterNotNull().first()
            }
        }
        val itemList = ItemList.Builder()
        regions!!.forEach {
            itemList.addItem(
                Row.Builder()
                    .setTitle(it)
                    .setOnClickListener {
                        setResult(it)
                        screenManager.pop()
                    }
                    .build()
            )
        }
        // All regions
        itemList.addItem(
            Row.Builder()
                .setTitle("All regions")
                .setOnClickListener {
                    setResult(null)
                    screenManager.pop()
                }
                .build()
        )
        return ListTemplate.Builder()
            .setTitle("Select Region")
            .setHeaderAction(Action.APP_ICON)
            .setLoading(false)
            .setSingleList(
                itemList.build()
            )
            .build()
    }
}