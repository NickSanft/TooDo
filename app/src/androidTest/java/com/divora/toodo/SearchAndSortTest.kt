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

        // Click on the search icon
        val searchIcon = device.wait(Until.findObject(By.res(packageName, "action_search")), LAUNCH_TIMEOUT)
        if (searchIcon == null) {
            throw AssertionError("Search icon not found")
        }
        searchIcon.click()

        // Wait for the search box (EditText) to appear. 
        // Using resource ID is more reliable for SearchView's internal EditText.
        // It is usually "search_src_text".
        var searchBox = device.wait(Until.findObject(By.res("com.divora.toodo:id/search_src_text")), LAUNCH_TIMEOUT)
        
        // Fallback to searching by class if ID lookup fails (sometimes package name might vary for internal resources)
        if (searchBox == null) {
             searchBox = device.wait(Until.findObject(By.clazz("android.widget.EditText")), LAUNCH_TIMEOUT)
        }

        if (searchBox == null) {
            throw AssertionError("Search box not found after clicking search icon")
        }
        
        searchBox.text = "Ban"
        device.waitForIdle()

        assert(device.hasObject(By.text("Banana")))
        assert(!device.hasObject(By.text("Apple")))
        assert(!device.hasObject(By.text("Cherry")))

        searchBox.text = "Ch"
        device.waitForIdle()
        
        assert(device.hasObject(By.text("Cherry")))
        assert(!device.hasObject(By.text("Apple")))
        assert(!device.hasObject(By.text("Banana")))
    }
    
    // Helper to create task
    private fun createTask(title: String, difficulty: String, points: Int) {
        val fab = device.wait(Until.findObject(By.res(packageName, "fab")), LAUNCH_TIMEOUT)
        fab.click()
        
        device.wait(Until.hasObject(By.text("Add New Task")), LAUNCH_TIMEOUT)
        device.wait(Until.findObject(By.res(packageName, "task_title_input")), LAUNCH_TIMEOUT).text = title
        
        // Default is Medium, so click Easy/Hard if needed
        if(difficulty == "Easy") {
            device.findObject(By.res(packageName, "easy_button")).click()
        } else if (difficulty == "Hard") {
             device.findObject(By.res(packageName, "hard_button")).click()
        }

        device.findObject(By.text("Add")).click()
        device.wait(Until.gone(By.text("Add New Task")), LAUNCH_TIMEOUT)
    }
}
