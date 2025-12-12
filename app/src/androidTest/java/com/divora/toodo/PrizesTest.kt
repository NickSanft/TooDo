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
class PrizesTest {

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
    fun testRedeemPrize() {
        // Create a task to get some points
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Get points"
        device.findObject(By.text("Hard (5 points)")).click()
        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)

        // Complete the task
        device.wait(Until.findObject(By.desc("Complete task: Get points")), LAUNCH_TIMEOUT).click()
        device.wait(Until.gone(By.text("Get points")), LAUNCH_TIMEOUT)
        Thread.sleep(1000)

        // Switch to the "Prizes" tab
        device.findObject(By.text("Prizes")).click()

        // Redeem the "Ice Cream" prize
        device.wait(Until.findObject(By.desc("Redeem prize: Ice Cream")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.text("Redeem")), LAUNCH_TIMEOUT).click()
        Thread.sleep(1000)

        // Switch to the "Completed" tab and verify the redeemed prize is there
        device.findObject(By.text("Completed")).click()
        device.wait(Until.hasObject(By.text("Redeemed: Ice Cream")), LAUNCH_TIMEOUT)

        // Verify the total points are updated correctly
        val pointsTextView = device.wait(Until.findObject(By.res(packageName, "total_points_text")), LAUNCH_TIMEOUT)
        assert(pointsTextView.text == "Total Points: -5")
    }
}
