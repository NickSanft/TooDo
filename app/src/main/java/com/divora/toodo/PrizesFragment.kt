package com.divora.toodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.divora.toodo.databinding.FragmentPrizesBinding

class PrizesFragment : Fragment(), FabClickHandler {

    private var _binding: FragmentPrizesBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var prizesViewModel: PrizesViewModel
    private lateinit var adapter: PrizesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrizesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)
        prizesViewModel = ViewModelProvider(this).get(PrizesViewModel::class.java)

        adapter = PrizesAdapter { prize ->
            showRedeemConfirmationDialog(prize)
        }

        binding.prizesList.adapter = adapter
        binding.prizesList.layoutManager = LinearLayoutManager(context)

        taskViewModel.totalPoints.observe(viewLifecycleOwner) {
            points -> binding.totalPointsText.text = "Total Points: ${points ?: 0}"
        }

        prizesViewModel.prizes.observe(viewLifecycleOwner) {
            prizes ->
            adapter.submitList(prizes)
        }
    }

    override fun onFabClick() {
        showAddPrizeDialog()
    }

    private fun showAddPrizeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_prize, null)
        val prizeNameInput = dialogView.findViewById<EditText>(R.id.prize_name_input)
        val prizeCostInput = dialogView.findViewById<EditText>(R.id.prize_cost_input)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Prize")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = prizeNameInput.text.toString()
                val cost = prizeCostInput.text.toString().toIntOrNull() ?: 0
                if (name.isNotBlank()) {
                    val newPrize = Prize(name = name, cost = cost)
                    prizesViewModel.addPrize(newPrize)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRedeemConfirmationDialog(prize: Prize) {
        val currentPoints = taskViewModel.totalPoints.value ?: 0
        if (currentPoints < prize.cost) {
            AlertDialog.Builder(requireContext())
                .setTitle("Not Enough Points")
                .setMessage("You do not have enough points to redeem this prize.")
                .setPositiveButton("OK", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Redeem Prize")
                .setMessage("Are you sure you want to redeem \"${prize.name}\" for ${prize.cost} points?")
                .setPositiveButton("Redeem") { _, _ ->
                    val redeemedTask = Task(
                        title = "Redeemed: ${prize.name}",
                        difficulty = "",
                        points = -prize.cost,
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                    taskViewModel.insert(redeemedTask)
                    prizesViewModel.removePrize(prize)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
