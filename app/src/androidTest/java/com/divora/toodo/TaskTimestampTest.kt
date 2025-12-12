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
class TaskTimestampTest {

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
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "First task"
        device.findObject(By.text("Hard (5 points)")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
        Thread.sleep(1000) // Wait for recycler view to settle

        // Create second task
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Second task"
        device.findObject(By.text("Hard (5 points)")).click()
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

        // Verify that the second task (completed last) is displayed first
        val taskList = device.wait(Until.findObject(By.res(packageName, "task_list")), LAUNCH_TIMEOUT)
        val tasks = taskList.children
        assert(tasks.size >= 2)
        assert(tasks[0].findObject(By.res(packageName, "task_title")).text == "Second task")
        assert(tasks[1].findObject(By.res(packageName, "task_title")).text == "First task")
    }
}
