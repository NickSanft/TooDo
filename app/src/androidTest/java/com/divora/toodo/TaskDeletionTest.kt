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
class TaskDeletionTest {

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

        // Launch the app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)    // Clear out any previous instances
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun createTaskAndDeleteIt() {
        // Click on the FAB to open the add task dialog
        device.findObject(By.res(packageName, "fab")).click()

        // Wait for the dialog to appear
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)

        // Type in the task title and select the difficulty
        device.findObject(By.res(packageName, "task_title_input")).text = "Delete this task"
        device.findObject(By.res(packageName, "medium_button")).click()

        // Click on the "Add" button
        device.findObject(By.text("Add")).click()

        // Wait for the task to be displayed on the screen, confirming the dialog is gone.
        val taskAppeared = device.wait(Until.hasObject(By.text("Delete this task")), LAUNCH_TIMEOUT)
        assert(taskAppeared)

        // Find the list item and click the delete button within it.
        val taskListItem = device.findObject(By.hasChild(By.text("Delete this task")))
        taskListItem.findObject(By.res(packageName, "delete_button")).click()

        // Verify the confirmation dialog is shown
        val confirmationDialogAppeared = device.wait(Until.hasObject(By.text("Delete Task")), LAUNCH_TIMEOUT)
        assert(confirmationDialogAppeared)

        // Click the "Delete" button in the confirmation dialog
        device.findObject(By.text("Delete")).click()

        // After the click, the UI re-renders. We now wait for the task to disappear
        val taskDisappeared = device.wait(Until.gone(By.text("Delete this task")), LAUNCH_TIMEOUT)

        assert(taskDisappeared)
    }

    @Test
    fun createTaskAndCancelDeletion() {
        // Click on the FAB to open the add task dialog
        device.findObject(By.res(packageName, "fab")).click()

        // Wait for the dialog to appear
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)

        // Type in the task title and select the difficulty
        device.findObject(By.res(packageName, "task_title_input")).text = "Don't delete this task"
        device.findObject(By.res(packageName, "medium_button")).click()

        // Click on the "Add" button
        device.findObject(By.text("Add")).click()

        // Wait for the task to be displayed on the screen, confirming the dialog is gone.
        val taskAppeared = device.wait(Until.hasObject(By.text("Don't delete this task")), LAUNCH_TIMEOUT)
        assert(taskAppeared)

        // Find the list item and click the delete button within it.
        val taskListItem = device.findObject(By.hasChild(By.text("Don't delete this task")))
        taskListItem.findObject(By.res(packageName, "delete_button")).click()

        // Verify the confirmation dialog is shown
        val confirmationDialogAppeared = device.wait(Until.hasObject(By.text("Delete Task")), LAUNCH_TIMEOUT)
        assert(confirmationDialogAppeared)

        // Click the "Cancel" button in the confirmation dialog
        device.findObject(By.text("Cancel")).click()

        // After the click, the UI re-renders. We now wait for the task to disappear
        val taskStillExists = device.wait(Until.hasObject(By.text("Don't delete this task")), LAUNCH_TIMEOUT)

        assert(taskStillExists)
    }
}
