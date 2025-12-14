package com.divora.toodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskListAdapter(
    private val onTaskCheckedChanged: (Task, Boolean) -> Unit,
    private val onTaskDeleteClicked: (Task) -> Unit,
    private val onTaskEditClicked: (Task) -> Unit
) :
    ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TasksComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            TaskViewHolder {
        return TaskViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, onTaskCheckedChanged, onTaskDeleteClicked, onTaskEditClicked)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitleView: TextView = itemView.findViewById(R.id.task_title)
        private val taskPriorityView: TextView = itemView.findViewById(R.id.task_priority)
        private val taskPointsView: TextView = itemView.findViewById(R.id.task_points)
        private val taskCompletedAtView: TextView = itemView.findViewById(R.id.task_completed_at)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
        private val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        private val taskTextLayout: View = itemView.findViewById(R.id.task_text_layout)

        fun bind(
            task: Task,
            onTaskCheckedChanged: (Task, Boolean) -> Unit,
            onTaskDeleteClicked: (Task) -> Unit,
            onTaskEditClicked: (Task) -> Unit
        ) {
            taskTitleView.text = task.title
            taskPointsView.text = "${task.points} points"
            taskPriorityView.text = when (task.priority) {
                1 -> "High Priority"
                2 -> "Medium Priority"
                else -> "Low Priority"
            }
            checkBox.contentDescription = "Complete task: ${task.title}"
            deleteButton.contentDescription = "Delete task: ${task.title}"
            editButton.contentDescription = "Edit task: ${task.title}"

            if (task.isCompleted && task.completedAt != null) {
                taskCompletedAtView.visibility = View.VISIBLE
                taskPriorityView.visibility = View.GONE
                val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
                taskCompletedAtView.text = "Completed at: ${sdf.format(Date(task.completedAt))}"
            } else {
                taskCompletedAtView.visibility = View.GONE
                taskPriorityView.visibility = View.VISIBLE
            }

            // Remove the listener before setting the checked state to prevent unwanted triggers.
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = task.isCompleted

            // Re-add the listener for user interactions.
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onTaskCheckedChanged(task, isChecked)
            }

            deleteButton.setOnClickListener {
                onTaskDeleteClicked(task)
            }

            editButton.setOnClickListener {
                onTaskEditClicked(task)
            }

            taskTextLayout.setOnClickListener {
                onTaskEditClicked(task)
            }
        }

        companion object {
            fun create(parent: ViewGroup): TaskViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_task, parent, false)
                return TaskViewHolder(view)
            }
        }
    }

    class TasksComparator : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task):
                Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task):
                Boolean {
            return oldItem == newItem
        }
    }
}
