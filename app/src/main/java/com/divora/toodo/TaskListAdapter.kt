package com.divora.toodo

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.divora.toodo.databinding.ListItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Locale

class TaskListAdapter(
    private val onCheckBoxClicked: (Task, Boolean, Int) -> Unit,
    private val onTaskClicked: (Task) -> Unit,
    private val onTaskMoved: ((Int, Int) -> Unit)? = null
) : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ListItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        onTaskMoved?.invoke(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class TaskViewHolder(private val binding: ListItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.taskTitle.text = task.title
            binding.taskPoints.text = "${task.points} pts"
            
            // Remove the listener temporarily to avoid triggering it when setting the state programmatically
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = task.isCompleted
            
            // Set content description for testing
            binding.checkBox.contentDescription = "Complete task: ${task.title}"

            // Strike-through for completed tasks
            if (task.isCompleted) {
                binding.taskTitle.paintFlags = binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.alpha = 0.6f
            } else {
                binding.taskTitle.paintFlags = binding.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.alpha = 1.0f
            }

            binding.taskPriority.text = when (task.priority) {
                1 -> "High"
                2 -> "Medium"
                else -> "Low"
            }
            
            val priorityColor = when (task.priority) {
                1 -> R.color.priority_high
                2 -> R.color.priority_medium
                else -> R.color.priority_low
            }
            binding.taskPriority.setTextColor(ContextCompat.getColor(binding.root.context, priorityColor))
            
            binding.taskCategory.text = task.category
            
            if (task.dueDate != null) {
                binding.taskDueDate.isVisible = true
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.taskDueDate.text = "Due: ${dateFormat.format(task.dueDate)}"
                
                // Highlight overdue tasks (if not completed)
                if (!task.isCompleted && task.dueDate < System.currentTimeMillis()) {
                    binding.taskDueDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.priority_high))
                } else {
                     binding.taskDueDate.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.darker_gray))
                }
            } else {
                binding.taskDueDate.isVisible = false
            }

            // Set the listener with the current adapter position
            binding.checkBox.setOnClickListener {
                onCheckBoxClicked(task, binding.checkBox.isChecked, layoutPosition)
            }

            binding.root.setOnClickListener {
                onTaskClicked(task)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
