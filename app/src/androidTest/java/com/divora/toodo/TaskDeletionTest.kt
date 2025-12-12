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
class TaskDeletionTest {

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
    fun testTaskDeletion() {
        // Create a task to delete from the active tab
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "Delete this active task"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("Delete this active task")), LAUNCH_TIMEOUT)

        // Delete the task and confirm
        device.findObject(By.desc("Delete task: Delete this active task")).click()
        device.wait(Until.hasObject(By.text("Delete Task")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Delete")).click()
        device.wait(Until.gone(By.text("Delete this active task")), LAUNCH_TIMEOUT)

        // Create a task to cancel deletion from the active tab
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "Don't delete this active task"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("Don't delete this active task")), LAUNCH_TIMEOUT)

        // Attempt to delete and then cancel
        device.findObject(By.desc("Delete task: Don't delete this active task")).click()
        device.wait(Until.hasObject(By.text("Delete Task")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Cancel")).click()
        device.wait(Until.hasObject(By.text("Don't delete this active task")), LAUNCH_TIMEOUT)

        // Create a task to delete from the completed tab
        device.findObject(By.res(packageName, "fab")).click()
        device.wait(Until.hasObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT)
        device.findObject(By.res(packageName, "task_title_input")).text = "Delete this completed task"
        device.findObject(By.res(packageName, "medium_button")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.hasObject(By.text("Delete this completed task")), LAUNCH_TIMEOUT)

        // Complete the task
        device.findObject(By.desc("Complete task: Delete this completed task")).click()
        device.wait(Until.gone(By.text("Delete this completed task")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Completed")).click()
        device.wait(Until.hasObject(By.text("Delete this completed task")), LAUNCH_TIMEOUT)

        // Delete the task from the completed tab
        device.findObject(By.desc("Delete task: Delete this completed task")).click()
        device.wait(Until.hasObject(By.text("Delete Task")), LAUNCH_TIMEOUT)
        device.findObject(By.text("Delete")).click()
        device.wait(Until.gone(By.text("Delete this completed task")), LAUNCH_TIMEOUT)
    }
}
