package com.divora.toodo

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.core.app.ActivityScenario

@RunWith(AndroidJUnit4::class)
class TaskCompletionAndUncompletionTest {

    private lateinit var device: UiDevice
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L
    private lateinit var taskViewModel: TaskViewModel

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

        // Launch the activity and initialize the ViewModel
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            taskViewModel = ViewModelProvider(it).get(TaskViewModel::class.java)
            taskViewModel.deleteAll()
        }

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun testTaskCompletionAndUncompletion() {
        // Create and complete a task
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "Complete me"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)
        device.findObject(By.desc("Complete task: Complete me")).click()
        device.wait(Until.gone(By.text("Complete me")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Completed")).click()
        device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)

        // Uncheck the task and confirm
        device.findObject(By.desc("Complete task: Complete me")).click()
        device.wait(Until.hasObject(By.text("Uncheck Task")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Uncheck")).click()
        device.wait(Until.gone(By.text("Complete me")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Active")).click()
        device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)

        // Create a task and cancel unchecking
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "No reprompt"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("No reprompt")), LAUNCH_TIMEOUT)
        device.findObject(By.desc("Complete task: No reprompt")).click()
        device.wait(Until.gone(By.text("No reprompt")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Completed")).click()
        device.wait(Until.hasObject(By.text("No reprompt")), LAUNCH_TIMEOUT)

        // Attempt to uncheck and then cancel
        device.findObject(By.desc("Complete task: No reprompt")).click()
        device.wait(Until.hasObject(By.text("Uncheck Task")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Cancel")).click()
        device.wait(Until.hasObject(By.text("No reprompt")), LAUNCH_TIMEOUT)

        // Verify that the confirmation dialog does not appear again
        val confirmationDialogGone = device.wait(Until.gone(By.text("Uncheck Task")), LAUNCH_TIMEOUT)
        assert(confirmationDialogGone)
    }
}
