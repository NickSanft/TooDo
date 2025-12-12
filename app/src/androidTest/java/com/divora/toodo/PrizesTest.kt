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
class PrizesTest {

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
        device.wait(Until.findObject(By.pkg(packageName)), LAUNCH_TIMEOUT)
    }

    @Test
    fun testRedeemPrize() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Create a task to get some points
            device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
            device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Get points"
            device.findObject(By.text("Hard (5 points)")).click()
            device.findObject(By.text("Add")).click()

            // Complete the task
            device.wait(Until.findObject(By.desc("Complete task: Get points")), LAUNCH_TIMEOUT).click()
            device.wait(Until.gone(By.text("Get points")), LAUNCH_TIMEOUT)

            // Switch to the "Prizes" tab
            device.findObject(By.text("Prizes")).click()

            // Redeem the "Ice Cream" prize
            device.wait(Until.findObject(By.desc("Redeem prize: Ice Cream")), LAUNCH_TIMEOUT).click()
            device.wait(Until.findObject(By.text("Redeem")), LAUNCH_TIMEOUT).click()

            // Switch to the "Completed" tab and verify the redeemed prize is there
            device.wait(Until.findObject(By.text("Completed")), LAUNCH_TIMEOUT).click()
            device.wait(Until.hasObject(By.text("Redeemed: Ice Cream")), LAUNCH_TIMEOUT)

            // Verify the total points are updated correctly
            val pointsTextView = device.wait(Until.findObject(By.res(packageName, "total_points_text")), LAUNCH_TIMEOUT)
            assert(pointsTextView.text == "Total Points: -5")
        }
    }
}
