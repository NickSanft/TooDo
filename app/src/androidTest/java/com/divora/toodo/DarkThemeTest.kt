package com.divora.toodo

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DarkThemeTest {

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<SettingsActivity>
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Reset preferences
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        scenario = ActivityScenario.launch(SettingsActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testDarkThemePersists() {
        // Toggle Dark Theme
        val darkThemeButton = device.wait(Until.findObject(By.res(packageName, "dark_theme_button")), LAUNCH_TIMEOUT)
        darkThemeButton.click()

        // Wait a bit for preference to save
        Thread.sleep(500)

        // Close and Reopen to check persistence
        scenario.close()
        scenario = ActivityScenario.launch(SettingsActivity::class.java)

        scenario.onActivity {
            val sharedPrefs = it.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val savedMode = sharedPrefs.getInt("night_mode", -1)
            assertEquals(AppCompatDelegate.MODE_NIGHT_YES, savedMode)
        }
    }
}
