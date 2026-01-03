package com.divora.toodo

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class DueDateTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        
        // Reset
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Populate with test data
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
    fun testDueDateDisplay() {
        val taskTitle = "Task with Due Date"
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
        val dueDate = calendar.timeInMillis
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val expectedDateString = "Due: ${dateFormat.format(calendar.time)}"

        scenario.onActivity {
            val taskViewModel = ViewModelProvider(it).get(TaskViewModel::class.java)
            taskViewModel.insert(Task(title = taskTitle, difficulty = "Easy", points = 5, dueDate = dueDate))
        }
        
        // Wait for list to update (simple sleep for simplicity)
        Thread.sleep(1000)

        // Verify task title is displayed
        onView(withText(taskTitle)).check(matches(isDisplayed()))
        
        // Verify due date is displayed
        onView(withText(expectedDateString)).check(matches(isDisplayed()))
    }
}
