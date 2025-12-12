package com.divora.toodo

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskCompletionAndUncompletionTest {

    private lateinit var device: UiDevice
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L

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

        // Launch the app
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)    // Clear out any previous instances
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun completeTaskAndMoveToCompletedTab() {
        // Click on the FAB to open the add task dialog
        device.findObject(By.res(packageName, "fab")).click()

        // Wait for the dialog to appear
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)

        // Type in the task title and select the difficulty
        device.findObject(By.res(packageName, "task_title_input")).text = "Complete me"
        device.findObject(By.res(packageName, "medium_button")).click()

        // Click on the "Add" button
        device.findObject(By.text("Add")).click()

        // Wait for the task to be displayed on the screen, confirming the dialog is gone.
        val taskAppeared = device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)
        assert(taskAppeared)

        // Find the list item and click the checkbox within it.
        device.findObject(By.desc("Complete task: Complete me")).click()

        // After the click, the task should disappear from the active tab
        val taskDisappeared = device.wait(Until.gone(By.text("Complete me")), LAUNCH_TIMEOUT)
        assert(taskDisappeared)

        // Switch to the "Completed" tab
        device.findObject(By.text("Completed")).click()

        // The task should now be visible in the completed tab
        val completedTaskAppeared = device.wait(Until.hasObject(By.text("Complete me")), LAUNCH_TIMEOUT)
        assert(completedTaskAppeared)
    }

    @Test
    fun uncheckTaskAndMoveToActiveTab() {
        // Create and complete a task
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "Uncheck me"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("Uncheck me")), LAUNCH_TIMEOUT)
        device.findObject(By.desc("Complete task: Uncheck me")).click()
        device.wait(Until.gone(By.text("Uncheck me")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Completed")).click()
        device.wait(Until.hasObject(By.text("Uncheck me")), LAUNCH_TIMEOUT)

        // Click the checkbox to uncheck the task
        device.findObject(By.desc("Complete task: Uncheck me")).click()

        // Verify the confirmation dialog is shown
        val confirmationDialogAppeared = device.wait(Until.hasObject(By.text("Uncheck Task")), LAUNCH_TIMEOUT)
        assert(confirmationDialogAppeared)

        // Click the "Uncheck" button in the confirmation dialog
        device.findObject(By.text("Uncheck")).click()

        // Wait for the dialog to disappear
        device.wait(Until.gone(By.text("Uncheck Task")), LAUNCH_TIMEOUT)

        // After the click, the task should disappear from the completed tab
        val taskDisappeared = device.wait(Until.gone(By.text("Uncheck me")), LAUNCH_TIMEOUT)
        assert(taskDisappeared)

        // Switch to the "Active" tab
        device.findObject(By.text("Active")).click()

        // The task should now be visible in the active tab
        val activeTaskAppeared = device.wait(Until.hasObject(By.text("Uncheck me")), LAUNCH_TIMEOUT)
        assert(activeTaskAppeared)
    }

    @Test
    fun uncheckTaskAndMoveToActiveTab_DoesNotReprompt() {
        // Create and complete a task
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

        // Click the checkbox to uncheck the task
        device.findObject(By.desc("Complete task: No reprompt")).click()

        // Verify the confirmation dialog is shown
        val confirmationDialogAppeared = device.wait(Until.hasObject(By.text("Uncheck Task")), LAUNCH_TIMEOUT)
        assert(confirmationDialogAppeared)

        // Click the "Uncheck" button in the confirmation dialog
        device.findObject(By.text("Uncheck")).click()

        // Wait for the dialog to disappear
        device.wait(Until.gone(By.text("Uncheck Task")), LAUNCH_TIMEOUT)

        // After the click, the task should disappear from the completed tab
        val taskDisappeared = device.wait(Until.gone(By.text("No reprompt")), LAUNCH_TIMEOUT)
        assert(taskDisappeared)

        // Switch to the "Active" tab
        device.findObject(By.text("Active")).click()

        // The task should now be visible in the active tab
        val activeTaskAppeared = device.wait(Until.hasObject(By.text("No reprompt")), LAUNCH_TIMEOUT)
        assert(activeTaskAppeared)

        // Verify that the confirmation dialog does not appear again
        val confirmationDialogGone = device.wait(Until.gone(By.text("Uncheck Task")), LAUNCH_TIMEOUT)
        assert(confirmationDialogGone)
    }
}
