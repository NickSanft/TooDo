package com.divora.toodo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.divora.toodo.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewPager = binding.root.findViewById<ViewPager2>(R.id.view_pager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        val tabLayout = binding.root.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Active"
                1 -> "Completed"
                2 -> "Prizes"
                else -> "Ledger"
            }
        }.attach()

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.fab.visibility = when (position) {
                    0 -> View.VISIBLE
                    1 -> View.GONE
                    2 -> View.VISIBLE
                    else -> View.GONE
                }
            }
        })

        binding.fab.setOnClickListener { 
            val fragment = supportFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
            if (fragment is FabClickHandler) {
                fragment.onFabClick()
            }
        }
    }

    fun showDeleteConfirmationDialog(task: Task) {
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("disable_confirmations", false)) {
            taskViewModel.delete(task)
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                taskViewModel.delete(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showUncheckConfirmationDialog(task: Task) {
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("disable_confirmations", false)) {
            taskViewModel.update(task.copy(isCompleted = false))
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Uncheck Task")
            .setMessage("Are you sure you want to uncheck this task? This will reduce your total points.")
            .setPositiveButton("Uncheck") { _, _ ->
                taskViewModel.update(task.copy(isCompleted = false))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
