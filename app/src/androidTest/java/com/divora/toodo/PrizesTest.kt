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
    fun testAddCustomPrize() {
        // Switch to the "Prizes" tab
        device.wait(Until.findObject(By.text("Prizes")), LAUNCH_TIMEOUT).click()

        // Click the "Add Prize" button
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()

        // Verify the "Add New Prize" dialog is shown
        device.wait(Until.hasObject(By.text("Add New Prize")), LAUNCH_TIMEOUT)

        // Add a new prize
        device.wait(Until.findObject(By.res(packageName, "prize_name_input")), LAUNCH_TIMEOUT).text = "Custom Prize"
        device.wait(Until.findObject(By.res(packageName, "prize_cost_input")), LAUNCH_TIMEOUT).text = "5"
        device.wait(Until.findObject(By.text("Add")), LAUNCH_TIMEOUT).click()

        // Verify the new prize is displayed in the list
        device.wait(Until.hasObject(By.text("Custom Prize")), LAUNCH_TIMEOUT)

        // Restart the activity to verify the prize is saved
        scenario.recreate()

        // Switch to the "Prizes" tab
        device.wait(Until.findObject(By.text("Prizes")), LAUNCH_TIMEOUT).click()

        // Verify the new prize is still displayed in the list
        device.wait(Until.hasObject(By.text("Custom Prize")), LAUNCH_TIMEOUT)
    }

    @Test
    fun testEditCustomPrize() {
        // Switch to the "Prizes" tab
        device.wait(Until.findObject(By.text("Prizes")), LAUNCH_TIMEOUT).click()

        // Add a custom prize to edit
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.res(packageName, "prize_name_input")), LAUNCH_TIMEOUT).text = "Custom Prize"
        device.wait(Until.findObject(By.res(packageName, "prize_cost_input")), LAUNCH_TIMEOUT).text = "5"
        device.wait(Until.findObject(By.text("Add")), LAUNCH_TIMEOUT).click()

        // Click the "Edit" button
        device.wait(Until.findObject(By.res(packageName, "edit_button")), LAUNCH_TIMEOUT).click()

        // Verify the "Edit Prize" dialog is shown
        device.wait(Until.hasObject(By.text("Edit Prize")), LAUNCH_TIMEOUT)

        // Edit the prize
        device.wait(Until.findObject(By.res(packageName, "prize_name_input")), LAUNCH_TIMEOUT).text = "Updated Prize"
        device.wait(Until.findObject(By.res(packageName, "prize_cost_input")), LAUNCH_TIMEOUT).text = "10"
        device.wait(Until.findObject(By.text("Save")), LAUNCH_TIMEOUT).click()

        // Verify the prize is updated in the list
        device.wait(Until.hasObject(By.text("Updated Prize")), LAUNCH_TIMEOUT)
        device.wait(Until.hasObject(By.text("10 points")), LAUNCH_TIMEOUT)
    }

    @Test
    fun testDeleteCustomPrize() {
        // Switch to the "Prizes" tab
        device.wait(Until.findObject(By.text("Prizes")), LAUNCH_TIMEOUT).click()

        // Add a custom prize to delete
        device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
        device.wait(Until.findObject(By.res(packageName, "prize_name_input")), LAUNCH_TIMEOUT).text = "Custom Prize"
        device.wait(Until.findObject(By.res(packageName, "prize_cost_input")), LAUNCH_TIMEOUT).text = "5"
        device.wait(Until.findObject(By.text("Add")), LAUNCH_TIMEOUT).click()

        // Click the "Delete" button
        device.wait(Until.findObject(By.res(packageName, "delete_button")), LAUNCH_TIMEOUT).click()

        // Confirm the deletion
        device.wait(Until.findObject(By.text("Delete")), LAUNCH_TIMEOUT).click()

        // Verify the prize is removed from the list
        device.wait(Until.gone(By.text("Custom Prize")), LAUNCH_TIMEOUT)
    }

    @Test
    fun testRedeemPrize() {
        // Create two tasks to get 10 points
        for (i in 1..2) {
            device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT).click()
            device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = "Get points $i"
            device.wait(Until.findObject(By.res(packageName, "hard_button")), LAUNCH_TIMEOUT).click()
            device.wait(Until.findObject(By.text("Add")), LAUNCH_TIMEOUT).click()
            device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
            device.wait(Until.findObject(By.desc("Complete task: Get points $i")), LAUNCH_TIMEOUT).click()
            device.wait(Until.gone(By.text("Get points $i")), LAUNCH_TIMEOUT)
        }

        // Switch to the "Prizes" tab
        device.wait(Until.findObject(By.text("Prizes")), LAUNCH_TIMEOUT).click()

        // Redeem the "Ice Cream" prize
        device.wait(Until.findObject(By.desc("Redeem prize: Ice Cream")), LAUNCH_TIMEOUT).click()

        // Confirm the redemption in the dialog
        device.wait(Until.findObject(By.text("Redeem")), LAUNCH_TIMEOUT).click()

        // Switch to the "Completed" tab and verify the redeemed prize is there
        device.wait(Until.findObject(By.text("Completed")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.text("Redeemed: Ice Cream")), LAUNCH_TIMEOUT)

        // Verify the total points are updated correctly
        device.wait(Until.findObject(By.text("Prizes")), LAUNCH_TIMEOUT).click()
        device.wait(Until.hasObject(By.res(packageName, "total_points_text").text("Total Points: 0")), LAUNCH_TIMEOUT)
    }

    @Test
    fun testRedeemPrize_notEnoughPoints() {
        // Switch to the "Prizes" tab
        device.wait(Until.findObject(By.text("Prizes")), LAUNCH_TIMEOUT).click()

        // Attempt to redeem the "Ice Cream" prize (costs 10 points)
        device.wait(Until.findObject(By.desc("Redeem prize: Ice Cream")), LAUNCH_TIMEOUT).click()

        // Verify the "Not Enough Points" dialog is shown
        device.wait(Until.hasObject(By.text("Not Enough Points")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.text("OK")), LAUNCH_TIMEOUT).click()

        // Verify the total points are still 0
        device.wait(Until.hasObject(By.res(packageName, "total_points_text").text("Total Points: 0")), LAUNCH_TIMEOUT)
    }
}
