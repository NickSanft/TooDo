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
class TaskCompletionAndUncompletionTest {

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
    fun testTaskCompletionAndUncompletion() {
        // Create and complete a task
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Complete me"
        device.findObject(By.text("Hard (5 points)")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
        Thread.sleep(1000) // Wait for recycler view to settle

        val completeCheckbox1 = device.wait(Until.findObject(By.desc("Complete task: Complete me")), LAUNCH_TIMEOUT)
        completeCheckbox1.click()
        device.wait(Until.gone(By.text("Complete me")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Completed")).click()
        Thread.sleep(1000) // Wait for recycler view to settle
        device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)

        // Uncheck the task and confirm
        val uncheckCheckbox = device.wait(Until.findObject(By.desc("Complete task: Complete me")), LAUNCH_TIMEOUT)
        uncheckCheckbox.click()
        device.wait(Until.findObject(By.text("Uncheck")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("Complete me")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Active")).click()
        device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)

        // Create a task and cancel unchecking
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "No reprompt"
        device.findObject(By.text("Hard (5 points)")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
        Thread.sleep(1000) // Wait for recycler view to settle

        val completeCheckbox2 = device.wait(Until.findObject(By.desc("Complete task: No reprompt")), LAUNCH_TIMEOUT)
        completeCheckbox2.click()
        device.wait(Until.gone(By.text("No reprompt")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Completed")).click()
        Thread.sleep(1000) // Wait for recycler view to settle

        // Attempt to uncheck and then cancel
        val uncheckCheckbox2 = device.wait(Until.findObject(By.desc("Complete task: No reprompt")), LAUNCH_TIMEOUT)
        uncheckCheckbox2.click()
        device.wait(Until.findObject(By.text("Cancel")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("No reprompt")), LAUNCH_TIMEOUT)

        // Verify that the confirmation dialog does not appear again
        val confirmationDialog = device.findObject(By.text("Uncheck Task"))
        assert(confirmationDialog == null)
    }
}
