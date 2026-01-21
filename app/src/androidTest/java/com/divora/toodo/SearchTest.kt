package com.divora.toodo

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
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
        
        // Reset settings
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Populate with test data
        scenario.onActivity {
            val taskViewModel = ViewModelProvider(it).get(TaskViewModel::class.java)
            taskViewModel.deleteAll()
            
            taskViewModel.insert(Task(title = "Buy Milk", difficulty = "Easy", points = 1))
            taskViewModel.insert(Task(title = "Walk Dog", difficulty = "Medium", points = 2))
            taskViewModel.insert(Task(title = "Read Book", difficulty = "Hard", points = 5))
        }
        
        // Give time for database inserts and UI update
        Thread.sleep(2000)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testSearchFunctionality() {
        // 1. Verify initial state (Active tab)
        onView(allOf(withText("Buy Milk"), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf(withText("Walk Dog"), isDisplayed())).check(matches(isDisplayed()))

        // 2. Focus on search view in the Fragment layout
        onView(allOf(withId(R.id.search_view), isDisplayed())).perform(click())

        // 3. Type "Milk" into the SearchAutoComplete text view
        onView(allOf(withId(androidx.appcompat.R.id.search_src_text), isDisplayed()))
            .perform(replaceText("Milk"), closeSoftKeyboard())

        // 4. Wait for filter to apply (DiffUtil takes a moment)
        Thread.sleep(1500)

        // 5. Verify filtering results
        onView(allOf(withText("Buy Milk"), isDisplayed())).check(matches(isDisplayed()))
        
        // If "Walk Dog" is filtered out of the RecyclerView, it is removed from the hierarchy.
        // onView throws NoMatchingViewException if it's gone, so check(doesNotExist()) is required.
        onView(withText("Walk Dog")).check(doesNotExist())
        onView(withText("Read Book")).check(doesNotExist())

        // 6. Change query to "Walk"
        onView(allOf(withId(androidx.appcompat.R.id.search_src_text), isDisplayed()))
            .perform(replaceText("Walk"), closeSoftKeyboard())

        Thread.sleep(1500)

        // 7. Verify updated filter
        onView(allOf(withText("Walk Dog"), isDisplayed())).check(matches(isDisplayed()))
        
        onView(withText("Buy Milk")).check(doesNotExist())
    }
}
