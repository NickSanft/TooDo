package com.divora.toodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.divora.toodo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val adapter = TaskListAdapter { task, isChecked ->
            taskViewModel.update(task.copy(isCompleted = isChecked))
        }
        binding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.task_list).adapter = adapter
        binding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.task_list).layoutManager = LinearLayoutManager(this)

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        taskViewModel.allTasks.observe(this) {
            tasks -> tasks?.let { adapter.submitList(it) }
        }

        taskViewModel.totalPoints.observe(this) {
            points -> binding.root.findViewById<android.widget.TextView>(R.id.total_points_text).text = "Total Points: ${points ?: 0}"
        }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_light_theme -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                true
            }
            R.id.action_dark_theme -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
