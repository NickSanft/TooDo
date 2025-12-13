package com.divora.toodo

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.core.app.ActivityScenario

@RunWith(AndroidJUnit4::class)
class SettingsTest {

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L

    @Before
    fun setUp() {
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
    fun testDisableConfirmations() {
        // Open settings
        device.wait(Until.findObject(By.desc("More options")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()

        // Disable confirmations
        device.wait(Until.findObject(By.res(packageName, "disable_confirmations_switch")), LAUNCH_TIMEOUT).click()

        // Go back to the main screen
        device.pressBack()

        // Create a task
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "No confirm task"
        device.findObject(By.text("Hard (5 points)")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)

        // Delete the task and verify no confirmation dialog appears
        device.wait(Until.findObject(By.desc("Delete task: No confirm task")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("No confirm task")), LAUNCH_TIMEOUT)

        // Verify that the confirmation dialog does not appear
        val confirmationDialog = device.findObject(By.text("Delete Task"))
        assert(confirmationDialog == null)
    }

    @Test
    fun testThemeChange() {
        // Open settings
        device.wait(Until.findObject(By.desc("More options")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()

        // Change to dark theme
        device.wait(Until.findObject(By.res(packageName, "dark_theme_button")), LAUNCH_TIMEOUT).click()

        // Go back and reopen settings
        device.pressBack()
        device.wait(Until.findObject(By.desc("More options")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()

        // Verify dark theme is selected
        val darkThemeButton = device.wait(Until.findObject(By.res(packageName, "dark_theme_button")), LAUNCH_TIMEOUT)
        assert(darkThemeButton.isChecked)

        // Change back to light theme
        device.wait(Until.findObject(By.res(packageName, "light_theme_button")), LAUNCH_TIMEOUT).click()
        device.pressBack()
        device.wait(Until.findObject(By.desc("More options")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()

        val lightThemeButton = device.wait(Until.findObject(By.res(packageName, "light_theme_button")), LAUNCH_TIMEOUT)
        assert(lightThemeButton.isChecked)
    }
}
