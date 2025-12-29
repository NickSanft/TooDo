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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskCategoryTest {

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
    fun testCategoryCreationAndFiltering() {
        // Create tasks with different categories
        createTask("Work Task", "Work")
        createTask("Personal Task", "Personal")
        createTask("Health Task", "Health")

        // Verify all tasks are shown initially (assuming 'All' is default)
        var taskObjects = device.wait(Until.findObjects(By.res(packageName, "task_title")), LAUNCH_TIMEOUT)
        var taskTitles = taskObjects.map { it.text }
        assertTrue(taskTitles.contains("Work Task"))
        assertTrue(taskTitles.contains("Personal Task"))
        assertTrue(taskTitles.contains("Health Task"))

        // Filter by "Work"
        selectCategoryFilter("Work")
        taskObjects = device.wait(Until.findObjects(By.res(packageName, "task_title")), LAUNCH_TIMEOUT)
        taskTitles = taskObjects.map { it.text }
        
        // We expect only 1, but we should make sure we're not seeing stale objects.
        // Wait for update.
        device.waitForIdle()

        // Re-query
        taskObjects = device.findObjects(By.res(packageName, "task_title"))
        taskTitles = taskObjects.map { it.text }
        
        assertEquals(1, taskTitles.size)
        assertTrue(taskTitles.contains("Work Task"))

        // Filter by "Personal"
        selectCategoryFilter("Personal")
        device.waitForIdle()
        taskObjects = device.findObjects(By.res(packageName, "task_title"))
        taskTitles = taskObjects.map { it.text }
        
        assertEquals(1, taskTitles.size)
        assertTrue(taskTitles.contains("Personal Task"))

        // Filter by "All"
        selectCategoryFilter("All")
        device.waitForIdle()
        taskObjects = device.findObjects(By.res(packageName, "task_title"))
        taskTitles = taskObjects.map { it.text }
        
        assertEquals(3, taskTitles.size)
    }

    private fun createTask(title: String, category: String) {
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = title
        
        // Select category
        val categorySpinner = device.findObject(By.res(packageName, "category_spinner"))
        categorySpinner.click()
        device.wait(Until.hasObject(By.text(category)), LAUNCH_TIMEOUT)
        device.findObject(By.text(category)).click()

        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
    }

    private fun selectCategoryFilter(category: String) {
        val filterSpinner = device.findObject(By.res(packageName, "filter_spinner"))
        filterSpinner.click()
        device.wait(Until.hasObject(By.text(category)), LAUNCH_TIMEOUT)
        device.findObject(By.text(category)).click()
        // Wait for UI to update (simple wait, in real app might need IdlingResource)
        Thread.sleep(1000) 
    }
}
