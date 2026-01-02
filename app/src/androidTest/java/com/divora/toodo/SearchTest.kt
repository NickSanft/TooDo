package com.divora.toodo

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class SearchTest {

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
            
            taskViewModel.insert(Task(title = "Buy Milk", difficulty = "Easy", points = 5))
            taskViewModel.insert(Task(title = "Walk Dog", difficulty = "Medium", points = 10))
            taskViewModel.insert(Task(title = "Read Book", difficulty = "Hard", points = 15))
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testSearchFunctionality() {
        // Wait for tasks to appear (basic wait, in a real app consider IdlingResource)
        Thread.sleep(1000)

        // Verify all tasks are initially visible
        onView(withText("Buy Milk")).check(matches(isDisplayed()))
        onView(withText("Walk Dog")).check(matches(isDisplayed()))
        onView(withText("Read Book")).check(matches(isDisplayed()))

        // Click on search view to expand it (if it's iconified, but here we set iconifiedByDefault=false, 
        // however clicking it to focus is good practice)
        onView(withId(R.id.search_view)).perform(click())

        // Type "Milk"
        onView(withId(androidx.appcompat.R.id.search_src_text)).perform(typeText("Milk"), closeSoftKeyboard())

        // Wait for filter to apply
        Thread.sleep(500)

        // Verify "Buy Milk" is displayed
        onView(withText("Buy Milk")).check(matches(isDisplayed()))

        // Verify others are NOT displayed
        onView(withText("Walk Dog")).check(doesNotExist())
        onView(withText("Read Book")).check(doesNotExist())

        // Change query to "Walk"
        // Note: We need to clear text first or just append. Let's clear and type.
        onView(withId(androidx.appcompat.R.id.search_src_text)).perform(androidx.test.espresso.action.ViewActions.clearText(), typeText("Walk"), closeSoftKeyboard())

        // Wait for filter to apply
        Thread.sleep(500)

        // Verify "Walk Dog" is displayed
        onView(withText("Walk Dog")).check(matches(isDisplayed()))
        
        // Verify others are NOT displayed
        onView(withText("Buy Milk")).check(doesNotExist())
    }
}
