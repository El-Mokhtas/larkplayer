package com.larkplayer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LarkPlayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any global configurations here
    }
}