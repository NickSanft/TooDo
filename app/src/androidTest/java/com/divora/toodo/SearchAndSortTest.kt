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
class SearchAndSortTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val packageName = ApplicationProvider.getApplicationContext<Context>().packageName
    private val LAUNCH_TIMEOUT = 5000L

    @Before
    fun setUp() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().apply()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            val taskViewModel = ViewModelProvider(it).get(TaskViewModel::class.java)
            taskViewModel.deleteAll()
        }
        // Give time for delete to propagate
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testSearchFunctionality() {
        createTask("Apple", "Easy", 1)
        createTask("Banana", "Easy", 1)
        createTask("Cherry", "Easy", 1)

        // Verify initial visibility
        assert(device.wait(Until.hasObject(By.text("Apple")), LAUNCH_TIMEOUT)) { "Apple not found initially" }
        assert(device.wait(Until.hasObject(By.text("Banana")), LAUNCH_TIMEOUT)) { "Banana not found initially" }
        assert(device.wait(Until.hasObject(By.text("Cherry")), LAUNCH_TIMEOUT)) { "Cherry not found initially" }

        // Find the search box in the fragment layout. 
        // It's always visible because iconifiedByDefault="false"
        val searchView = device.findObject(By.res(packageName, "search_view"))
        val searchBox = searchView.findObject(By.res(packageName, "search_src_text")) ?: 
                        device.findObject(By.res("com.divora.toodo:id/search_src_text"))

        assert(searchBox != null) { "Search box not found in fragment layout" }
        
        // Search for "Ban"
        searchBox.text = "Ban"
        
        // Verify results
        assert(device.wait(Until.hasObject(By.text("Banana")), LAUNCH_TIMEOUT)) { "Banana should be visible for query 'Ban'" }
        assert(device.wait(Until.gone(By.text("Apple")), LAUNCH_TIMEOUT)) { "Apple should be filtered out for query 'Ban'" }
        assert(device.wait(Until.gone(By.text("Cherry")), LAUNCH_TIMEOUT)) { "Cherry should be filtered out for query 'Ban'" }

        // Search for "Ch"
        searchBox.text = "Ch"
        
        assert(device.wait(Until.hasObject(By.text("Cherry")), LAUNCH_TIMEOUT)) { "Cherry should be visible for query 'Ch'" }
        assert(device.wait(Until.gone(By.text("Apple")), LAUNCH_TIMEOUT)) { "Apple should be filtered out for query 'Ch'" }
        assert(device.wait(Until.gone(By.text("Banana")), LAUNCH_TIMEOUT)) { "Banana should be filtered out for query 'Ch'" }
    }
    
    // Helper to create task
    private fun createTask(title: String, difficulty: String, points: Int) {
        val fab = device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT)
        fab.click()
        
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = title
        
        if(difficulty == "Easy") {
            device.findObject(By.res(packageName, "easy_button")).click()
        } else if (difficulty == "Hard") {
             device.findObject(By.res(packageName, "hard_button")).click()
        }

        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
        
        // Small delay to ensure DB write is visible to LiveData
        Thread.sleep(500)
    }
}
