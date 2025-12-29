package com.divora.toodo

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L

    @Before
    fun setUp() {
        hiltRule.inject()
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Clear shared preferences and database before each test
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            val taskViewModel = ViewModelProvider(it).get(TaskViewModel::class.java)
            taskViewModel.deleteAll()
            val prizesViewModel = ViewModelProvider(it).get(PrizesViewModel::class.java)
            prizesViewModel.deleteAll()
            val pointLedgerViewModel = ViewModelProvider(it).get(PointLedgerViewModel::class.java)
            pointLedgerViewModel.deleteAll()
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testThemeChange() {
        // Open settings
        // UiAutomator can find the overflow menu by description "More options" usually
        val menuButton = device.wait(Until.findObject(By.desc("More options")), LAUNCH_TIMEOUT)
        // If not found, try openOptionsMenu manually or use key event
        if (menuButton != null) {
            menuButton.click()
        } else {
            device.pressMenu()
        }
        
        device.waitForIdle()
        device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()
        device.waitForIdle()

        // Change to dark theme
        device.wait(Until.findObject(By.res(packageName, "dark_theme_button")), LAUNCH_TIMEOUT).click()
        device.waitForIdle()

        // Go back and reopen settings
        device.pressBack()
        device.waitForIdle()
        val menuButton2 = device.wait(Until.findObject(By.desc("More options")), LAUNCH_TIMEOUT)
        if (menuButton2 != null) {
            menuButton2.click()
        } else {
            device.pressMenu()
        }
        device.waitForIdle()
        device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()
        device.waitForIdle()

        // Verify dark theme is selected
        val darkThemeButton = device.wait(Until.findObject(By.res(packageName, "dark_theme_button")), LAUNCH_TIMEOUT)
        assert(darkThemeButton.isChecked)

        // Change back to light theme
        device.wait(Until.findObject(By.res(packageName, "light_theme_button")), LAUNCH_TIMEOUT).click()
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
        val menuButton3 = device.wait(Until.findObject(By.desc("More options")), LAUNCH_TIMEOUT)
        if (menuButton3 != null) {
            menuButton3.click()
        } else {
            device.pressMenu()
        }
        device.waitForIdle()
        device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()
        device.waitForIdle()

        val lightThemeButton = device.wait(Until.findObject(By.res(packageName, "light_theme_button")), LAUNCH_TIMEOUT)
        assert(lightThemeButton.isChecked)
    }
}
