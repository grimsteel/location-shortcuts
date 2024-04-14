package com.grimsteel.locationshortcuts

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.grimsteel.locationshortcuts.data.ShortcutDao
import com.grimsteel.locationshortcuts.data.ShortcutDatabase

class LocationShortcutsApplication : Application() {
    val shortcutsRepository: ShortcutDao by lazy {
        ShortcutDatabase.getDatabase(this).shortcutDao()
    }

    val preferences: DataStore<Preferences> by preferencesDataStore(name = "settings")

    companion object {
        val LAST_SELECTED_REGION_KEY = stringPreferencesKey("last_selected_region")
    }
}