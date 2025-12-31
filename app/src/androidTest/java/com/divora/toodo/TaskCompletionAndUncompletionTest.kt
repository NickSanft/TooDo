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
class TaskCompletionAndUncompletionTest {

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
    fun testTaskCompletionAndUncompletion() {
        // Create and complete a task
        val fab = device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT)
        fab?.click() ?: run {
             // Retry finding FAB
             device.waitForIdle()
             device.findObject(By.res(packageName, "fab")).click()
        }
        
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Complete me"
        device.wait(Until.findObject(By.res(packageName, "hard_button")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Add")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)

        val activeCheckbox = device.wait(Until.findObject(By.desc("Complete task: Complete me")), LAUNCH_TIMEOUT)
        if (activeCheckbox == null) {
            // Wait for list update
             device.waitForIdle()
        }
        device.findObject(By.desc("Complete task: Complete me")).click()
        
        device.wait(Until.gone(By.text("Complete me")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.text("Completed")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)

        // Uncheck the task and confirm
        device.wait(Until.findObject(By.desc("Complete task: Complete me")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Uncheck")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("Complete me")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.text("Active")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)

        // Create a task and cancel unchecking
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "No reprompt"
        device.wait(Until.findObject(By.res(packageName, "hard_button")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Add")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)

        device.wait(Until.findObject(By.desc("Complete task: No reprompt")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("No reprompt")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.text("Completed")), LAUNCH_TIMEOUT).click()

        // Attempt to uncheck and then cancel
        device.wait(Until.findObject(By.desc("Complete task: No reprompt")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Cancel")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("No reprompt")), LAUNCH_TIMEOUT)

        // Verify that the confirmation dialog does not appear again (it's gone because we cancelled)
        val confirmationDialog = device.findObject(By.text("Uncheck Task"))
        assert(confirmationDialog == null)
    }
}
