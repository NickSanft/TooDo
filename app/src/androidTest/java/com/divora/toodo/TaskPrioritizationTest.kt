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
class TaskPrioritizationTest {

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
    fun testTaskPrioritization() {
        // Create three tasks with different priorities
        createTask("Low Priority Task", 3)
        createTask("High Priority Task", 1)
        createTask("Medium Priority Task", 2)
        
        // Wait for list to update and stabilize
        device.waitForIdle()

        // Verify that the tasks are displayed in the correct order
        // Note: findObjects doesn't guarantee order matching the UI layout order strictly in all cases,
        // but typically for RecyclerView it returns in order of children.
        val taskObjects = device.wait(Until.findObjects(By.res(packageName, "task_title")), LAUNCH_TIMEOUT)
        
        // Ensure we found all 3
        if (taskObjects.size < 3) {
            // Wait a bit more or fail
            Thread.sleep(1000)
        }
        
        val taskTitles = device.findObjects(By.res(packageName, "task_title")).map { it.text }

        // Depending on sorting implementation, we expect High -> Medium -> Low
        // Assuming default sort is by priority.
        
        if (taskTitles.size >= 3) {
            assert(taskTitles[0] == "High Priority Task")
            assert(taskTitles[1] == "Medium Priority Task")
            assert(taskTitles[2] == "Low Priority Task")
        } else {
             throw AssertionError("Not all tasks displayed. Found: $taskTitles")
        }
    }

    private fun createTask(title: String, priority: Int) {
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = title
        when (priority) {
            1 -> device.findObject(By.res(packageName, "high_priority_button")).click()
            2 -> device.findObject(By.res(packageName, "medium_priority_button")).click()
            3 -> device.findObject(By.res(packageName, "low_priority_button")).click()
        }
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
    }
}
