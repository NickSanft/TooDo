package com.divora.toodo

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.viewpager2.widget.ViewPager2
import com.divora.toodo.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    fun showUncheckConfirmationDialog(task: Task, onCancel: () -> Unit) {
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
            .setNegativeButton("Cancel") { _, _ ->
                onCancel()
            }
            .setOnCancelListener {
                onCancel()
            }
            .show()
    }

    fun playSoundEffect() {
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("sound_effects", false)) {
            try {
                // Using system notification sound as a placeholder
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(applicationContext, notification)
                r.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                taskViewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedSort = sharedPreferences.getString("sort_order", "PRIORITY")
        taskViewModel.setSortOrder(SortOrder.valueOf(savedSort ?: "PRIORITY"))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.sort_priority -> {
                saveSortOrder(SortOrder.PRIORITY)
                true
            }
            R.id.sort_points -> {
                saveSortOrder(SortOrder.POINTS)
                true
            }
            R.id.sort_az -> {
                saveSortOrder(SortOrder.AZ)
                true
            }
            R.id.sort_newest -> {
                saveSortOrder(SortOrder.NEWEST)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveSortOrder(sortOrder: SortOrder) {
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("sort_order", sortOrder.name).apply()
        taskViewModel.setSortOrder(sortOrder)
    }
}
