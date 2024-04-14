package com.grimsteel.locationshortcuts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.grimsteel.locationshortcuts.ui.LocationShortcutsApp
import com.grimsteel.locationshortcuts.ui.theme.LocationShortcutsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationShortcutsTheme {
                LocationShortcutsApp()
            }
        }
    }
}
