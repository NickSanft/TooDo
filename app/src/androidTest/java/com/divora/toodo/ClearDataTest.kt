package com.divora.toodo

import android.content.Context
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.lifecycle.ViewModelProvider

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ClearDataTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L

    @Before
    fun setUp() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Ensure clean slate
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            val taskViewModel = ViewModelProvider(it).get(TaskViewModel::class.java)
            taskViewModel.deleteAll()
        }
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
        // UiAutomator can find the overflow menu by description "More options" usually
        val menuButton = device.wait(Until.findObject(By.desc("More options")), LAUNCH_TIMEOUT)
        // If not found, try openOptionsMenu manually or use key event
        if (menuButton != null) {
            menuButton.click()
        } else {
            device.pressMenu()
        }
        
        device.wait(Until.findObject(By.text("Settings")), LAUNCH_TIMEOUT).click()
        
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
