package com.divora.toodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskListAdapter(
    private val onTaskCheckedChanged: (Task, Boolean) -> Unit,
    private val onTaskEditClicked: (Task) -> Unit
) :
    ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TasksComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            TaskViewHolder {
        return TaskViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, onTaskCheckedChanged, onTaskEditClicked)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitleView: TextView = itemView.findViewById(R.id.task_title)
        private val taskCategoryView: TextView = itemView.findViewById(R.id.task_category)
        private val taskPriorityView: TextView = itemView.findViewById(R.id.task_priority)
        private val taskPointsView: TextView = itemView.findViewById(R.id.task_points)
        private val taskCompletedAtView: TextView = itemView.findViewById(R.id.task_completed_at)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val taskTextLayout: View = itemView.findViewById(R.id.task_text_layout)

        fun bind(
            task: Task,
            onTaskCheckedChanged: (Task, Boolean) -> Unit,
            onTaskEditClicked: (Task) -> Unit
        ) {
            taskTitleView.text = task.title
            taskCategoryView.text = task.category
            taskPointsView.text = "${task.points} points"
            
            val priorityText = when (task.priority) {
                1 -> "High Priority"
                2 -> "Medium Priority"
                else -> "Low Priority"
            }
            taskPriorityView.text = priorityText

            val priorityColor = when (task.priority) {
                1 -> R.color.priority_high
                2 -> R.color.priority_medium
                else -> R.color.priority_low
            }
            taskPriorityView.setTextColor(ContextCompat.getColor(itemView.context, priorityColor))

            checkBox.contentDescription = "Complete task: ${task.title}"

            if (task.isCompleted && task.completedAt != null) {
                taskCompletedAtView.visibility = View.VISIBLE
                taskPriorityView.visibility = View.GONE
                val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                taskCompletedAtView.text = "Completed: ${sdf.format(Date(task.completedAt))}"
                
                // Strike through title if completed
                taskTitleView.paintFlags = taskTitleView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                taskTitleView.alpha = 0.6f
            } else {
                taskCompletedAtView.visibility = View.GONE
                taskPriorityView.visibility = View.VISIBLE
                
                // Remove strike through
                taskTitleView.paintFlags = taskTitleView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskTitleView.alpha = 1.0f
            }

            // Remove the listener before setting the checked state to prevent unwanted triggers.
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = task.isCompleted

            // Re-add the listener for user interactions.
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onTaskCheckedChanged(task, isChecked)
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
