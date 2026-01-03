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
class TaskUncheckCancelTest {

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
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testCancelUncheckTask() {
        // 1. Create a task
        val fab = device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT)
        fab?.click() ?: run {
            device.waitForIdle()
            device.findObject(By.res(packageName, "fab")).click()
        }

        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Task to Cancel Uncheck"
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)

        // 2. Complete the task
        val activeCheckbox = device.wait(Until.findObject(By.desc("Complete task: Task to Cancel Uncheck")), LAUNCH_TIMEOUT)
        activeCheckbox.click()
        device.wait(Until.gone(By.text("Task to Cancel Uncheck")), LAUNCH_TIMEOUT)

        // 3. Go to Completed tab
        device.findObject(By.text("Completed")).click()
        device.wait(Until.hasObject(By.text("Task to Cancel Uncheck")), LAUNCH_TIMEOUT)

        // 4. Click checkbox to uncheck (should show dialog)
        val completedCheckbox = device.wait(Until.findObject(By.desc("Complete task: Task to Cancel Uncheck")), LAUNCH_TIMEOUT)
        completedCheckbox.click()
        
        device.wait(Until.hasObject(By.text("Uncheck Task")), LAUNCH_TIMEOUT)

        // 5. Click "Cancel" in dialog
        device.findObject(By.text("Cancel")).click()
        device.wait(Until.gone(By.text("Uncheck Task")), LAUNCH_TIMEOUT)

        // 6. Verify task is STILL in completed list (and checkbox is still checked visually)
        // Note: The UI Automator might see the checkbox state as checked if we did our job right.
        val taskTitle = device.wait(Until.findObject(By.text("Task to Cancel Uncheck")), LAUNCH_TIMEOUT)
        assert(taskTitle != null) { "Task disappeared from Completed list after cancelling uncheck" }
        
        // We can also verify that the checkbox is still checked.
        val checkbox = device.findObject(By.desc("Complete task: Task to Cancel Uncheck"))
        assert(checkbox.isChecked) { "Checkbox should remain checked after cancelling" }
        
        // 7. Verify task is NOT in Active list
        device.findObject(By.text("Active")).click()
        device.wait(Until.gone(By.text("Task to Cancel Uncheck")), LAUNCH_TIMEOUT)
        val activeTask = device.findObject(By.text("Task to Cancel Uncheck"))
        assert(activeTask == null) { "Task appeared in Active list after cancelling uncheck" }
    }
}
