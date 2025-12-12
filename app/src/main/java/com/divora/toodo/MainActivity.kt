package com.divora.toodo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.divora.toodo.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val viewPager = binding.root.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.view_pager)
        viewPager.adapter = ViewPagerAdapter(this)

        val tabLayout = binding.root.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Active"
                else -> "Completed"
            }
        }.attach()

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        binding.fab.setOnClickListener { 
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val taskTitleInput = dialogView.findViewById<EditText>(R.id.task_title_input)

        AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = taskTitleInput.text.toString()
                val difficulty = when (dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group).checkedRadioButtonId) {
                    R.id.easy_button -> "Easy"
                    R.id.medium_button -> "Medium"
                    else -> "Hard"
                }
                val points = when (difficulty) {
                    "Easy" -> 1
                    "Medium" -> 2
                    else -> 5
                }
                taskViewModel.insert(Task(title = title, difficulty = difficulty, points = points))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showDeleteConfirmationDialog(task: Task) {
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("disable_confirmations", false)) {
            taskViewModel.delete(task)
            return
        }

        AlertDialog.Builder(this)
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

        AlertDialog.Builder(this)
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
