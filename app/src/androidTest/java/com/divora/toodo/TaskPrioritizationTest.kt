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
class TaskPrioritizationTest {

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
    fun testTaskPrioritization() {
        // Create three tasks with different priorities
        createTask("Low Priority Task", 3)
        createTask("High Priority Task", 1)
        createTask("Medium Priority Task", 2)

        // Verify that the tasks are displayed in the correct order
        val taskTitles = device.wait(Until.findObjects(By.res(packageName, "task_title")), LAUNCH_TIMEOUT)
            .map { it.text }

        assert(taskTitles[0] == "High Priority Task")
        assert(taskTitles[1] == "Medium Priority Task")
        assert(taskTitles[2] == "Low Priority Task")
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
