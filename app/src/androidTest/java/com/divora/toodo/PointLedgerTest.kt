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
class PointLedgerTest {

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
    fun testPointLedger() {
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

        // Switch to the "Ledger" tab
        device.wait(Until.findObject(By.text("Ledger")), LAUNCH_TIMEOUT).click()

        // Verify the ledger entries are correct
        device.wait(Until.hasObject(By.text("Redeemed: Ice Cream")), LAUNCH_TIMEOUT)
        device.wait(Until.hasObject(By.text("-10")), LAUNCH_TIMEOUT)
        device.wait(Until.hasObject(By.text("Completed: Get points 2")), LAUNCH_TIMEOUT)
        device.wait(Until.hasObject(By.text("+5")), LAUNCH_TIMEOUT)
    }
}
