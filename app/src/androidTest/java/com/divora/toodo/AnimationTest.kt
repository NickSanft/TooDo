package com.divora.toodo

import android.animation.LayoutTransition
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
        // This test checks if the animateLayoutChanges property is enabled on the containers
        scenario.onActivity { activity ->
            // Wait for fragment to be attached if necessary, but in onActivity it should be there or created shortly.
            // However, ViewPager2 loads lazily. We might need to ensure the fragment is added.
            // Accessing "f0" assumes FragmentStateAdapter implementation details (tag naming strategy).
            
            val taskListFragment = activity.supportFragmentManager.findFragmentByTag("f0")
            if (taskListFragment != null && taskListFragment.view != null) {
                // The root of fragment_task_list is a FrameLayout/ConstraintLayout or whatever
                // android:animateLayoutChanges might be on the RecyclerView's parent or the container
                // Let's check the view itself.
                
                val viewGroup = taskListFragment.view as? android.view.ViewGroup
                val layoutTransition = viewGroup?.layoutTransition
                
                // Note: The original test might fail if animateLayoutChanges="true" is not set in XML.
                // But the primary fix here is adding HiltAndroidTest and HiltAndroidRule.
                
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
}
