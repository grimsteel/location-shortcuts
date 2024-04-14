package com.grimsteel.locationshortcuts.ui.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.grimsteel.locationshortcuts.data.Shortcut
import com.grimsteel.locationshortcuts.data.ShortcutDao

data class UiState(
    val shortcuts: List<Shortcut> = listOf(),
    val regions: List<String> = listOf(),
    val types: List<String> = listOf(),
    val regionFilter: String? = null,
    val typeFilter: String? = null
)

class HomeViewModel(shortcutDao: ShortcutDao, val preferences: DataStore<Preferences>) : ViewModel() {
    private val _regionFilter: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _typeFilter: MutableStateFlow<String?> = MutableStateFlow(null)

    val uiState = combine(shortcutDao.getAllItems(), shortcutDao.getAllRegions(), shortcutDao.getAllTypes(), _regionFilter, _typeFilter) { shortcuts, regions, types, region, type ->
        UiState(shortcuts.filter {
            (region == null || it.region == region) && (type == null || it.type == type)
        }, regions, types, region, type)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    suspend fun setRegionFilter(region: String?) {
        _regionFilter.value = region

        // save this as the most recently selected
        preferences.edit {
            if (region == null) it.remove(com.grimsteel.locationshortcuts.LocationShortcutsApplication.LAST_SELECTED_REGION_KEY)
            else it[com.grimsteel.locationshortcuts.LocationShortcutsApplication.LAST_SELECTED_REGION_KEY] = region
        }
    }

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    /*@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun sortByDistance(currentList: List<Shortcut>) {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            _uiState.update { state ->
                state.copy(shortcutsList = currentList.sortedBy {
                    val itemLocation = Location("")
                    itemLocation.latitude = it.latitude
                    itemLocation.longitude = it.longitude
                    location.distanceTo(itemLocation)
                })
            }
        }
    }

    private fun sortByLastUsed(currentList: List<Shortcut>) {
        _uiState.update { state ->
            val currentDate = Date().time
            state.copy(shortcutsList = currentList.sortedBy { currentDate - it.lastUsed.time })
        }
    }*/

    init {
        // Load the most recently selected region
        viewModelScope.launch {
            val lastRegion = preferences.data.map {
                it[com.grimsteel.locationshortcuts.LocationShortcutsApplication.LAST_SELECTED_REGION_KEY]
            }.first()

            if (lastRegion != null) {
                setRegionFilter(lastRegion)
            }
        }
    }
}
