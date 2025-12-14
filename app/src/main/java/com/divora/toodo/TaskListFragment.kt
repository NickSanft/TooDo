package com.divora.toodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TaskListFragment : Fragment(), FabClickHandler {

    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isCompleted = arguments?.getBoolean("isCompleted") ?: false

        val adapter = TaskListAdapter(
            { task, isChecked ->
                if (isChecked) {
                    taskViewModel.update(task.copy(isCompleted = true))
                } else {
                    (activity as MainActivity).showUncheckConfirmationDialog(task)
                }
            },
            { task -> (activity as MainActivity).showDeleteConfirmationDialog(task) },
            { task -> showEditTaskDialog(task) }
        )

        view.findViewById<RecyclerView>(R.id.task_list).adapter = adapter
        view.findViewById<RecyclerView>(R.id.task_list).layoutManager = LinearLayoutManager(context)

        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            tasks?.let {
                val filteredTasks = it.filter { it.isCompleted == isCompleted }
                if (isCompleted) {
                    adapter.submitList(filteredTasks.sortedByDescending { it.completedAt })
                } else {
                    adapter.submitList(filteredTasks)
                }
            }
        }

        taskViewModel.totalPoints.observe(viewLifecycleOwner) {
            points -> view.findViewById<android.widget.TextView>(R.id.total_points_text).text = "Total Points: ${points ?: 0}"
        }
    }

    override fun onFabClick() {
        showAddTaskDialog()
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val taskTitleInput = dialogView.findViewById<EditText>(R.id.task_title_input)

        AlertDialog.Builder(requireContext())
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

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val taskTitleInput = dialogView.findViewById<EditText>(R.id.task_title_input)
        val difficultyRadioGroup = dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group)

        taskTitleInput.setText(task.title)
        when (task.difficulty) {
            "Easy" -> difficultyRadioGroup.check(R.id.easy_button)
            "Medium" -> difficultyRadioGroup.check(R.id.medium_button)
            "Hard" -> difficultyRadioGroup.check(R.id.hard_button)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = taskTitleInput.text.toString()
                val difficulty = when (difficultyRadioGroup.checkedRadioButtonId) {
                    R.id.easy_button -> "Easy"
                    R.id.medium_button -> "Medium"
                    else -> "Hard"
                }
                val points = when (difficulty) {
                    "Easy" -> 1
                    "Medium" -> 2
                    else -> 5
                }
                taskViewModel.update(task.copy(title = title, difficulty = difficulty, points = points))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        fun newInstance(isCompleted: Boolean): TaskListFragment {
            val fragment = TaskListFragment()
            val args = Bundle()
            args.putBoolean("isCompleted", isCompleted)
            fragment.arguments = args
            return fragment
        }
    }
}
