package com.divora.toodo

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClearDataTest {

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Ensure clean slate
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testClearAllData() {
        // 1. Create a task
        createTask("Task to be deleted")
        
        // Verify task exists
        assertTrue(device.wait(Until.hasObject(By.text("Task to be deleted")), LAUNCH_TIMEOUT))

        // 2. Navigate to Settings
        // Assuming "Settings" is in the overflow menu
        val openMenuDesc = "More options" // Standard content description for overflow
        val menuButton = device.findObject(By.desc(openMenuDesc))
        if (menuButton != null) {
            menuButton.click()
            device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()
        } else {
             // If no overflow menu button found by description, maybe it's ID based or different on this device/emulator config?
             // Let's try finding by ID if we knew it, or assume standard ActionBar behavior.
             // Alternatively, try pressing the menu key if it exists, or look for the text "Settings" if it's exposed.
             // Given MainActivity.kt has onCreateOptionsMenu, it's likely in the overflow.
             // Let's try finding object with className "androidx.appcompat.widget.ActionMenuPresenter$OverflowMenuButton" if description fails,
             // or just look for the text if the menu is already open (unlikely).
             // However, UiAutomator is usually good with "More options".
        }
        
        // Wait for Settings Activity
        device.wait(Until.hasObject(By.text("Data Management")), LAUNCH_TIMEOUT)

        // 3. Click Clear Data
        device.findObject(By.res(packageName, "clear_data_button")).click()

        // 4. Confirm Dialog
        device.wait(Until.hasObject(By.text("Clear All Data")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Clear")).click()
        
        // Wait for toast or action to complete
        Thread.sleep(1000)

        // 5. Return to Main Activity and verify task is gone
        device.pressBack()
        
        // Wait for Main Activity
        device.wait(Until.hasObject(By.res(packageName, "view_pager")), LAUNCH_TIMEOUT)

        // Verify task is gone
        // We expect "No tasks yet!" or empty list
        val taskObject = device.wait(Until.findObject(By.text("Task to be deleted")), 2000)
        assertTrue("Task should be deleted", taskObject == null)
    }

    private fun createTask(title: String) {
        val fab = device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT)
        fab.click()
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = title
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
    }
}
