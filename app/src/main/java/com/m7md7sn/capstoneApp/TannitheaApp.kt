package com.m7md7sn.capstoneApp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import com.m7md7sn.capstoneApp.ui.theme.TannitheaTheme
import com.m7md7sn.capstoneApp.data.model.TimedSensorReading
import com.m7md7sn.capstoneApp.ui.navigation.TopBar
import com.m7md7sn.capstoneApp.ui.screen.home.MainScreen
import com.m7md7sn.capstoneApp.ui.screen.monitoring.MonitoringScreen
import com.m7md7sn.capstoneApp.ui.screen.sensor.SensorScreen
import com.m7md7sn.capstoneApp.ui.screen.control.ControlScreen
import com.m7md7sn.capstoneApp.ui.screen.home.HomeViewModel
import com.m7md7sn.capstoneApp.ui.screen.monitoring.MonitoringViewModel
import com.m7md7sn.capstoneApp.ui.screen.splash.SplashScreen
import com.m7md7sn.capstoneApp.ui.navigation.AnimatedBottomBar

@HiltAndroidApp
class TannitheaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true) // Enable offline capabilities
        } catch (e: Exception) {
            // Persistence can only be enabled once, if this is called twice it will throw an exception
            // Just catch and ignore
        }
    }
}