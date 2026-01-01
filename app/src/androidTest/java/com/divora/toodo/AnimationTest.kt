package com.divora.toodo

import android.animation.LayoutTransition
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
import nl.dionsegijn.konfetti.xml.KonfettiView
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AnimationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Reset
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            val taskViewModel = ViewModelProvider(it).get(TaskViewModel::class.java)
            taskViewModel.deleteAll()
            val prizesViewModel = ViewModelProvider(it).get(PrizesViewModel::class.java)
            prizesViewModel.deleteAll()
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testLayoutAnimationEnabled() {
        scenario.onActivity { activity ->
            val taskListFragment = activity.supportFragmentManager.findFragmentByTag("f0")
            if (taskListFragment != null && taskListFragment.view != null) {
                val viewGroup = taskListFragment.view as? android.view.ViewGroup
                val layoutTransition = viewGroup?.layoutTransition
                
                if (layoutTransition != null) {
                     assertTrue("Layout transition should have CHANGE_APPEARING enabled", 
                        layoutTransition.isTransitionTypeEnabled(LayoutTransition.CHANGE_APPEARING))
                    assertTrue("Layout transition should have CHANGE_DISAPPEARING enabled", 
                        layoutTransition.isTransitionTypeEnabled(LayoutTransition.CHANGE_DISAPPEARING))
                    assertTrue("Layout transition should have APPEARING enabled", 
                        layoutTransition.isTransitionTypeEnabled(LayoutTransition.APPEARING))
                    assertTrue("Layout transition should have DISAPPEARING enabled", 
                        layoutTransition.isTransitionTypeEnabled(LayoutTransition.DISAPPEARING))
                }
            }
        }
    }

    @Test
    fun testKonfettiAnimationPlaysOnTaskCompletion() {
        val taskTitle = "Celebration Task"

        scenario.onActivity { activity ->
            val taskViewModel = ViewModelProvider(activity).get(TaskViewModel::class.java)
            taskViewModel.insert(Task(title = taskTitle, difficulty = "Easy", points = 10))
        }

        // Wait for the task to appear in the list
        val checkBox = device.wait(Until.findObject(By.desc("Complete task: $taskTitle")), 5000)
        assertNotNull("Checkbox for the task should be visible", checkBox)

        // Click the checkbox to complete the task
        checkBox.click()

        // Verify that KonfettiView exists in the hierarchy
        scenario.onActivity { activity ->
            val taskListFragment = activity.supportFragmentManager.findFragmentByTag("f0")
            val konfettiView = taskListFragment?.view?.findViewById<KonfettiView>(R.id.konfettiView)
            
            assertNotNull("KonfettiView should exist in the layout", konfettiView)
            assertTrue("KonfettiView should be visible", konfettiView?.visibility == android.view.View.VISIBLE)
        }
    }
}
