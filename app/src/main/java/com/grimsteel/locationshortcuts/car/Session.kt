package com.grimsteel.locationshortcuts.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class Session : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return TypeSelectScreen(carContext)
    }
}