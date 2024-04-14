package com.grimsteel.locationshortcuts.ui.edit

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.grimsteel.locationshortcuts.data.Shortcut
import com.grimsteel.locationshortcuts.data.ShortcutDao
import java.util.Date

data class ShortcutInputState(
    val id: Int = 0,
    val label: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lastUsed: Date? = null,
    val region: String = "",
    val type: String = ""
)

data class UiState(
    val regions: List<String> = listOf(),
    val types: List<String> = listOf()
)

private fun ShortcutInputState.toShortcut() = Shortcut(
    id, label, address,
    latitude ?: 0.0,
    longitude ?: 0.0,
    lastUsed ?: Date(),
    region, type
)

private fun Shortcut.toInputState() = ShortcutInputState(
    id, label, address, latitude, longitude, lastUsed, region, type
)

class EditViewModel(savedStateHandle: SavedStateHandle, private val shortcutDao: ShortcutDao) : ViewModel() {
    var currentShortcutState by mutableStateOf(ShortcutInputState())
        private set

    val uiState = combine(shortcutDao.getAllRegions(), shortcutDao.getAllTypes()) { regions, types ->
        UiState(regions, types)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    val shortcutId: Int? = checkNotNull(savedStateHandle.get<Int>("id")).let {
        if (it == 0) null else it
    }

    fun updateShortcut(newShortcut: ShortcutInputState) {
        currentShortcutState = newShortcut
    }

    fun geocodeLocation(context: Context) {
        if (currentShortcutState.address.isNotBlank() && Geocoder.isPresent()) {
            val geocoder = Geocoder(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(currentShortcutState.address, 1) {
                    val address = it[0]
                    if (address != null) {
                        updateShortcut(currentShortcutState.copy(latitude = address.latitude, longitude = address.longitude))
                    }
                }
            } else {
                // use synchronous version
                @Suppress("DEPRECATION") val address = geocoder.getFromLocationName(currentShortcutState.address, 1)?.get(0)
                if (address != null) {
                    updateShortcut(currentShortcutState.copy(latitude = address.latitude, longitude = address.longitude))
                }
            }
        }
    }

    suspend fun saveShortcut(): Boolean {
        if (validateInputState()) {
            if (shortcutId == null) shortcutDao.insert(currentShortcutState.toShortcut())
            else shortcutDao.update(currentShortcutState.toShortcut())
            return true
        }
        return false
    }

    suspend fun updateLastUsed() {
        // can only do this if inputs are valid and this is an existing shortcut
        if (validateInputState() && shortcutId != null) {
            updateShortcut(currentShortcutState.copy(lastUsed = Date()))
            shortcutDao.update(currentShortcutState.toShortcut())
        }
    }

    suspend fun deleteShortcut() {
        if (shortcutId != null) shortcutDao.delete(currentShortcutState.toShortcut())
    }

    private fun validateInputState(): Boolean {
        return !with(currentShortcutState) {
            label.isBlank() || address.isBlank() || latitude == null || longitude == null || region.isBlank() || address.isBlank()
        }
    }

    init {
        if (shortcutId != null) {
            viewModelScope.launch {
                currentShortcutState = shortcutDao.getItem(shortcutId)
                    .filterNotNull()
                    .map { it.toInputState() }
                    .first()
            }
        }
    }
}