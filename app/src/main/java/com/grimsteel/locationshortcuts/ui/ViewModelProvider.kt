package com.grimsteel.locationshortcuts.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.grimsteel.locationshortcuts.ui.edit.EditViewModel
import com.grimsteel.locationshortcuts.ui.home.HomeViewModel

object ViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(locationShortcutsApplication().shortcutsRepository, locationShortcutsApplication().preferences)
        }
        initializer  {
            EditViewModel(createSavedStateHandle(), locationShortcutsApplication().shortcutsRepository)
        }
    }
}

fun CreationExtras.locationShortcutsApplication() = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as com.grimsteel.locationshortcuts.LocationShortcutsApplication