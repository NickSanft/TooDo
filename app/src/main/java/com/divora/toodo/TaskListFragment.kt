package com.divora.toodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskListFragment : Fragment(), FabClickHandler {

    private val taskViewModel: TaskViewModel by activityViewModels()
    private val categories = listOf("General", "Work", "Personal", "Health", "Study", "Finance")

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
            { task -> showEditTaskDialog(task) }
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.task_list)
        val emptyListTextView = view.findViewById<TextView>(R.id.empty_list_text)
        val filterSpinner = view.findViewById<Spinner>(R.id.filter_spinner)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        setupSwipeToDelete(recyclerView)

        // Set up filter spinner
        val filterCategories = listOf("All") + categories
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterCategories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        var currentCategoryFilter = "All"

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentCategoryFilter = filterCategories[position]
                taskViewModel.allTasks.value?.let { tasks ->
                    updateTaskList(tasks, isCompleted, currentCategoryFilter, recyclerView, emptyListTextView, adapter)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            tasks?.let {
                updateTaskList(it, isCompleted, currentCategoryFilter, recyclerView, emptyListTextView, adapter)
            }
        }

        taskViewModel.totalPoints.observe(viewLifecycleOwner) {
            points -> view.findViewById<TextView>(R.id.total_points_text).text = "Total Points: ${points ?: 0}"
        }
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val adapter = recyclerView.adapter as TaskListAdapter
                val task = adapter.currentList[position]

                taskViewModel.delete(task)

                Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        taskViewModel.insert(task.copy(id = 0))
                    }
                    .show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun updateTaskList(
        tasks: List<Task>,
        isCompleted: Boolean,
        categoryFilter: String,
        recyclerView: RecyclerView,
        emptyListTextView: TextView,
        adapter: TaskListAdapter
    ) {
        val filteredTasks = tasks.filter {
            it.isCompleted == isCompleted && (categoryFilter == "All" || it.category == categoryFilter)
        }

        if (filteredTasks.isEmpty()) {
            recyclerView.isVisible = false
            emptyListTextView.isVisible = true
            val emptyText = if (isCompleted) "No completed tasks found!" else "No active tasks found!"
            emptyListTextView.text = emptyText
        } else {
            recyclerView.isVisible = true
            emptyListTextView.isVisible = false
            if (isCompleted) {
                adapter.submitList(filteredTasks.sortedByDescending { it.completedAt })
            } else {
                adapter.submitList(filteredTasks.sortedBy { it.priority })
            }
        }
    }

    override fun onFabClick() {
        showAddTaskDialog()
    }

    private fun sanitizeInput(input: String): String {
        return input.replace("\r", "").replace("\n", "").trim()
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val taskTitleInput = dialogView.findViewById<EditText>(R.id.task_title_input)
        val priorityRadioGroup = dialogView.findViewById<RadioGroup>(R.id.priority_radio_group)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.category_spinner)
        val difficultyRadioGroup = dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group)

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val title = sanitizeInput(taskTitleInput.text.toString())
                if (title.isBlank()) {
                    taskTitleInput.error = "Title cannot be empty"
                } else {
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
                    val priority = when (priorityRadioGroup.checkedRadioButtonId) {
                        R.id.high_priority_button -> 1
                        R.id.medium_priority_button -> 2
                        else -> 3
                    }
                    val category = categorySpinner.selectedItem.toString()
                    taskViewModel.insert(Task(title = title, difficulty = difficulty, points = points, priority = priority, category = category))
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val taskTitleInput = dialogView.findViewById<EditText>(R.id.task_title_input)
        val difficultyRadioGroup = dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group)
        val priorityRadioGroup = dialogView.findViewById<RadioGroup>(R.id.priority_radio_group)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.category_spinner)

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        taskTitleInput.setText(task.title)
        categorySpinner.setSelection(categories.indexOf(task.category))
        when (task.difficulty) {
            "Easy" -> difficultyRadioGroup.check(R.id.easy_button)
            "Medium" -> difficultyRadioGroup.check(R.id.medium_button)
            "Hard" -> difficultyRadioGroup.check(R.id.hard_button)
        }
        when (task.priority) {
            1 -> priorityRadioGroup.check(R.id.high_priority_button)
            2 -> priorityRadioGroup.check(R.id.medium_priority_button)
            3 -> priorityRadioGroup.check(R.id.low_priority_button)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val title = sanitizeInput(taskTitleInput.text.toString())
                if (title.isBlank()) {
                    taskTitleInput.error = "Title cannot be empty"
                } else {
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
                    val priority = when (priorityRadioGroup.checkedRadioButtonId) {
                        R.id.high_priority_button -> 1
                        R.id.medium_priority_button -> 2
                        else -> 3
                    }
                    val category = categorySpinner.selectedItem.toString()
                    taskViewModel.update(task.copy(title = title, difficulty = difficulty, points = points, priority = priority, category = category))
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
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
