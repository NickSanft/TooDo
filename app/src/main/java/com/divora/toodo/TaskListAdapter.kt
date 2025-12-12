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

class TaskListAdapter(
    private val onTaskCheckedChanged: (Task, Boolean) -> Unit,
    private val onTaskDeleteClicked: (Task) -> Unit
) :
    ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TasksComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            TaskViewHolder {
        return TaskViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, onTaskCheckedChanged, onTaskDeleteClicked)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitleView: TextView = itemView.findViewById(R.id.task_title)
        private val taskPointsView: TextView = itemView.findViewById(R.id.task_points)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

        fun bind(task: Task, onTaskCheckedChanged: (Task, Boolean) -> Unit, onTaskDeleteClicked: (Task) -> Unit) {
            taskTitleView.text = task.title
            taskPointsView.text = "${task.points} points"
            checkBox.isChecked = task.isCompleted
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onTaskCheckedChanged(task, isChecked)
            }
            deleteButton.setOnClickListener {
                onTaskDeleteClicked(task)
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
