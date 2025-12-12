package com.divora.toodo

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest {

    private lateinit var device: UiDevice
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L

    @Before
    fun startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage = device.launcherPackageName
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT)

        // Clear shared preferences
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        // Launch the app
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)    // Clear out any previous instances
        context.startActivity(intent)

        // Wait for the app to appear and the overflow menu to be present.
        device.wait(Until.hasObject(By.desc("More options")), LAUNCH_TIMEOUT)
    }

    @Test
    fun testDisableConfirmations() {
        // Open settings
        device.findObject(By.desc("More options")).click()
        device.wait(Until.hasObject(By.text("Settings")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Settings")).click()

        // Disable confirmations
        device.findObject(By.res(packageName, "disable_confirmations_switch")).click()

        // Go back to the main screen
        device.pressBack()

        // Create a task
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "No confirm task"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("No confirm task")), LAUNCH_TIMEOUT)

        // Delete the task and verify no confirmation dialog appears
        device.findObject(By.desc("Delete task: No confirm task")).click()

        // Verify the task is gone
        val taskDisappeared = device.wait(Until.gone(By.text("No confirm task")), LAUNCH_TIMEOUT)
        assert(taskDisappeared)

        // Verify that the confirmation dialog does not appear
        val confirmationDialogGone = device.wait(Until.gone(By.text("Delete Task")), LAUNCH_TIMEOUT)
        assert(confirmationDialogGone)
    }

    @Test
    fun testThemeChange() {
        // Open settings
        device.findObject(By.desc("More options")).click()
        device.wait(Until.hasObject(By.text("Settings")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Settings")).click()

        // Change to dark theme
        device.findObject(By.res(packageName, "dark_theme_button")).click()

        // Go back and reopen settings
        device.pressBack()
        device.wait(Until.hasObject(By.text("Active")), LAUNCH_TIMEOUT)
        device.wait(Until.hasObject(By.desc("More options")), LAUNCH_TIMEOUT)
        device.findObject(By.desc("More options")).click()
        device.wait(Until.hasObject(By.text("Settings")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Settings")).click()

        // Verify dark theme is selected
        val darkThemeButton = device.findObject(By.res(packageName, "dark_theme_button"))
        assert(darkThemeButton.isChecked)

        // Change back to light theme
        device.findObject(By.res(packageName, "light_theme_button")).click()
        device.pressBack()
        device.wait(Until.hasObject(By.text("Active")), LAUNCH_TIMEOUT)
        device.wait(Until.hasObject(By.desc("More options")), LAUNCH_TIMEOUT)
        device.findObject(By.desc("More options")).click()
        device.wait(Until.hasObject(By.text("Settings")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Settings")).click()

        val lightThemeButton = device.findObject(By.res(packageName, "light_theme_button"))
        assert(lightThemeButton.isChecked)
    }
}
