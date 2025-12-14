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
class TaskDeletionTest {

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
    fun testTaskDeletion() {
        // Create a task to delete from the active tab
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Delete this active task"
        device.findObject(By.text("Hard (5 points)")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)

        // Delete the task and confirm
        val deleteButton1 = device.wait(Until.findObject(By.desc("Delete task: Delete this active task")), LAUNCH_TIMEOUT)
        deleteButton1.click()
        device.wait(Until.findObject(By.text("Delete")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("Delete this active task")), LAUNCH_TIMEOUT)

        // Create a task to cancel deletion from the active tab
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Don't delete this active task"
        device.findObject(By.text("Hard (5 points)")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)

        // Attempt to delete and then cancel
        val deleteButton2 = device.wait(Until.findObject(By.desc("Delete task: Don't delete this active task")), LAUNCH_TIMEOUT)
        deleteButton2.click()
        device.wait(Until.findObject(By.text("Cancel")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Don't delete this active task")), LAUNCH_TIMEOUT)

        // Create a task to delete from the completed tab
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Delete this completed task"
        device.findObject(By.text("Hard (5 points)")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)

        // Complete the task
        val completeCheckbox = device.wait(Until.findObject(By.desc("Complete task: Delete this completed task")), LAUNCH_TIMEOUT)
        completeCheckbox.click()
        device.wait(Until.gone(By.text("Delete this completed task")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Completed")).click()

        // Delete the task from the completed tab
        val deleteButton3 = device.wait(Until.findObject(By.desc("Delete task: Delete this completed task")), LAUNCH_TIMEOUT)
        deleteButton3.click()
        device.wait(Until.findObject(By.text("Delete")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("Delete this completed task")), LAUNCH_TIMEOUT)
    }
}
