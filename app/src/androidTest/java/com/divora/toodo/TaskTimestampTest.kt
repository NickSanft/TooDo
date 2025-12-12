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
class TaskTimestampTest {

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

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun testTimestampAndSorting() {
        // Create first task
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "First task"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("First task")), LAUNCH_TIMEOUT)

        // Create second task
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "Second task"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("Second task")), LAUNCH_TIMEOUT)

        // Complete first task
        device.findObject(By.desc("Complete task: First task")).click()
        device.wait(Until.gone(By.text("First task")), LAUNCH_TIMEOUT)

        // Add a delay to ensure the timestamps are different
        Thread.sleep(1000)

        // Complete second task
        device.findObject(By.desc("Complete task: Second task")).click()
        device.wait(Until.gone(By.text("Second task")), LAUNCH_TIMEOUT)

        // Go to completed tab
        device.findObject(By.text("Completed")).click()

        // Verify timestamp is visible for both tasks
        device.wait(Until.hasObject(By.textContains("Completed at:")), LAUNCH_TIMEOUT)

        // Verify that the second task (completed last) is displayed first
        val taskList = device.findObject(By.res(packageName, "task_list"))
        val tasks = taskList.children
        assert(tasks.size >= 2)
        assert(tasks[0].findObject(By.res(packageName, "task_title")).text == "Second task")
        assert(tasks[1].findObject(By.res(packageName, "task_title")).text == "First task")
    }
}
