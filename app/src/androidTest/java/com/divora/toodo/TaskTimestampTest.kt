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
class TaskTimestampTest {

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
    fun testTimestampAndSorting() {
        // Create first task
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "First task"
        device.findObject(By.res(packageName, "hard_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
        Thread.sleep(1000) // Wait for recycler view to settle

        // Create second task
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)

        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Second task"
        device.findObject(By.res(packageName, "hard_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
        Thread.sleep(1000) // Wait for recycler view to settle

        // Complete first task
        val firstCheckbox = device.wait(Until.findObject(By.desc("Complete task: First task")), LAUNCH_TIMEOUT)
        firstCheckbox.click()
        device.wait(Until.gone(By.text("First task")), LAUNCH_TIMEOUT)

        // Add a delay to ensure the timestamps are different
        Thread.sleep(1000)

        // Complete second task
        val secondCheckbox = device.wait(Until.findObject(By.desc("Complete task: Second task")), LAUNCH_TIMEOUT)
        secondCheckbox.click()
        device.wait(Until.gone(By.text("Second task")), LAUNCH_TIMEOUT)

        // Go to completed tab and wait for it to settle
        device.findObject(By.text("Completed")).click()
        Thread.sleep(1000)
        
        device.waitForIdle()

        // Verify that the second task (completed last) is displayed first
        val taskList = device.wait(Until.findObject(By.res(packageName, "task_list")), LAUNCH_TIMEOUT)
        val tasks = taskList.children
        
        // This assertion can be flaky if UI Automator hasn't fully loaded the children or if layout is animating.
        // Assuming waitForIdle helped.
        
        if (tasks.size >= 2) {
             // Note: findObject on a child object might need depth/scope consideration or just work.
            val firstTaskTitle = tasks[0].findObject(By.res(packageName, "task_title"))?.text
            val secondTaskTitle = tasks[1].findObject(By.res(packageName, "task_title"))?.text

            assert(firstTaskTitle == "Second task") { "Expected Second task at top but found $firstTaskTitle" }
            assert(secondTaskTitle == "First task") { "Expected First task at second position but found $secondTaskTitle" }
        } else {
             // Fallback if children are not accessible as list items yet
             val taskTitles = device.findObjects(By.res(packageName, "task_title")).map { it.text }
             if (taskTitles.size >= 2) {
                 assert(taskTitles[0] == "Second task")
                 assert(taskTitles[1] == "First task")
             } else {
                 throw AssertionError("Tasks not found in completed list")
             }
        }
    }
}
