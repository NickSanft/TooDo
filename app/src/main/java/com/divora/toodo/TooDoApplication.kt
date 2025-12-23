package com.divora.toodo

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TooDoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
