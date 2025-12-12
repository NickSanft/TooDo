package com.divora.toodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TaskListFragment : Fragment() {

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
            { task -> (activity as MainActivity).showDeleteConfirmationDialog(task) }
        )

        view.findViewById<RecyclerView>(R.id.task_list).adapter = adapter
        view.findViewById<RecyclerView>(R.id.task_list).layoutManager = LinearLayoutManager(context)

        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            tasks?.let { adapter.submitList(it.filter { it.isCompleted == isCompleted }) }
        }

        taskViewModel.totalPoints.observe(viewLifecycleOwner) {
            points -> view.findViewById<android.widget.TextView>(R.id.total_points_text).text = "Total Points: ${points ?: 0}"
        }
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
