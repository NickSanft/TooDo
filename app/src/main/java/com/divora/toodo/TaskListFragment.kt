package com.divora.toodo

import android.animation.LayoutTransition
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TaskListFragment : Fragment(), FabClickHandler {

    private val taskViewModel: TaskViewModel by activityViewModels()
    private val categories = listOf("General", "Work", "Personal", "Health", "Study", "Finance")
    private lateinit var adapter: TaskListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fix for crash when using animateLayoutChanges="true" with ViewPager2
        val rootLayout = view.findViewById<ViewGroup>(R.id.task_list_root)
        rootLayout.layoutTransition?.setAnimateParentHierarchy(false)

        val isCompleted = arguments?.getBoolean("isCompleted") ?: false

        adapter = TaskListAdapter(
            onCheckBoxClicked = { task, isChecked, position ->
                if (isChecked) {
                    taskViewModel.update(task.copy(isCompleted = true))
                    playKonfetti()
                } else {
                    (activity as MainActivity).showUncheckConfirmationDialog(task) {
                        // User cancelled, revert the checkbox visually
                        adapter.notifyItemChanged(position)
                    }
                }
            },
            onTaskClicked = { task -> showTaskDetailsDialog(task) },
            onTaskMoved = { fromPosition, toPosition ->
                // This callback is called during the drag.
            }
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.task_list)
        val emptyListTextView = view.findViewById<TextView>(R.id.empty_list_text)
        val filterSpinner = view.findViewById<Spinner>(R.id.filter_spinner)
        val searchView = view.findViewById<SearchView>(R.id.search_view)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        setupSwipeToDeleteAndDrag(recyclerView)

        // Set up filter spinner
        val filterCategories = listOf("All") + categories
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterCategories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        var currentCategoryFilter = "All"

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentCategoryFilter = filterCategories[position]
                taskViewModel.filteredTasks.value?.let { tasks ->
                    updateTaskList(tasks, isCompleted, currentCategoryFilter, recyclerView, emptyListTextView)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        
        // Set up Search View
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                taskViewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        taskViewModel.filteredTasks.observe(viewLifecycleOwner) { tasks ->
            tasks?.let {
                updateTaskList(it, isCompleted, currentCategoryFilter, recyclerView, emptyListTextView)
            }
        }

        taskViewModel.totalPoints.observe(viewLifecycleOwner) {
            points -> view.findViewById<TextView>(R.id.total_points_text).text = "Total Points: ${points ?: 0}"
        }
    }

    private fun playKonfetti() {
        val konfettiView = view?.findViewById<KonfettiView>(R.id.konfettiView)
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        konfettiView?.start(party)
    }

    private fun setupSwipeToDeleteAndDrag(recyclerView: RecyclerView) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                
                adapter.onItemMove(fromPosition, toPosition)
                
                val currentList = adapter.currentList.toMutableList()
                Collections.swap(currentList, fromPosition, toPosition)
                adapter.submitList(currentList)
                
                return true
            }
            
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                val tasks = adapter.currentList
                tasks.forEachIndexed { index, task ->
                    if (task.orderIndex != index) {
                        taskViewModel.update(task.copy(orderIndex = index))
                    }
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
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
        emptyListTextView: TextView
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
            val sortedTasks = if (isCompleted) {
                filteredTasks.sortedByDescending { it.completedAt }
            } else {
                // Sort by orderIndex for active tasks to respect drag-and-drop
                filteredTasks.sortedBy { it.orderIndex }
            }
            adapter.submitList(sortedTasks)
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
        val dueDateButton = dialogView.findViewById<Button>(R.id.due_date_button)

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        // Load defaults from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val defaultCategory = sharedPreferences.getString("default_category", "General")
        val defaultDifficulty = sharedPreferences.getString("default_difficulty", "Medium")

        categorySpinner.setSelection(categories.indexOf(defaultCategory))

        when (defaultDifficulty) {
            "Easy" -> difficultyRadioGroup.check(R.id.easy_button)
            "Medium" -> difficultyRadioGroup.check(R.id.medium_button)
            "Hard" -> difficultyRadioGroup.check(R.id.hard_button)
        }
        
        var selectedDueDate: Long? = null
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        dueDateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDueDate = calendar.timeInMillis
                    dueDateButton.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

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
                    
                    val newOrderIndex = (adapter.currentList.maxOfOrNull { it.orderIndex } ?: 0) + 1
                    
                    taskViewModel.insert(Task(title = title, difficulty = difficulty, points = points, priority = priority, category = category, orderIndex = newOrderIndex, dueDate = selectedDueDate))
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun showTaskDetailsDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_view_task, null)
        
        val titleView = dialogView.findViewById<TextView>(R.id.view_task_title)
        val statusView = dialogView.findViewById<TextView>(R.id.view_task_status)
        val pointsView = dialogView.findViewById<TextView>(R.id.view_task_points)
        val difficultyView = dialogView.findViewById<TextView>(R.id.view_task_difficulty)
        val priorityView = dialogView.findViewById<TextView>(R.id.view_task_priority)
        val categoryView = dialogView.findViewById<TextView>(R.id.view_task_category)
        val dueDateView = dialogView.findViewById<TextView>(R.id.view_task_due_date)
        val completedLabel = dialogView.findViewById<TextView>(R.id.view_task_completed_label)
        val completedDateView = dialogView.findViewById<TextView>(R.id.view_task_completed_date)

        titleView.text = task.title
        statusView.text = if (task.isCompleted) "Completed" else "Active"
        pointsView.text = "${task.points}"
        difficultyView.text = task.difficulty
        priorityView.text = when (task.priority) {
            1 -> "High"
            2 -> "Medium"
            else -> "Low"
        }
        categoryView.text = task.category
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        
        if (task.dueDate != null) {
            dueDateView.text = dateFormat.format(Date(task.dueDate))
        } else {
            dueDateView.text = "None"
        }

        if (task.isCompleted && task.completedAt != null) {
            completedLabel.visibility = View.VISIBLE
            completedDateView.visibility = View.VISIBLE
            completedDateView.text = timeFormat.format(Date(task.completedAt))
        } else {
            completedLabel.visibility = View.GONE
            completedDateView.visibility = View.GONE
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Task Details")
            .setView(dialogView)
            .setPositiveButton("Edit") { _, _ ->
                showEditTaskDialog(task)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val taskTitleInput = dialogView.findViewById<EditText>(R.id.task_title_input)
        val difficultyRadioGroup = dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group)
        val priorityRadioGroup = dialogView.findViewById<RadioGroup>(R.id.priority_radio_group)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.category_spinner)
        val dueDateButton = dialogView.findViewById<Button>(R.id.due_date_button)

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
        
        var selectedDueDate: Long? = task.dueDate
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        if (selectedDueDate != null) {
            calendar.timeInMillis = selectedDueDate!!
            dueDateButton.text = dateFormat.format(calendar.time)
        }
        
        dueDateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDueDate = calendar.timeInMillis
                    dueDateButton.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
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
                    taskViewModel.update(task.copy(title = title, difficulty = difficulty, points = points, priority = priority, category = category, dueDate = selectedDueDate))
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
