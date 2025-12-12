package com.divora.toodo

import android.content.Context
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrizesFragment : Fragment(), FabClickHandler {

    private var _binding: FragmentPrizesBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var adapter: PrizesAdapter
    private val prizes = mutableListOf<Prize>()
    private val gson = Gson()

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

        loadPrizes()

        adapter = PrizesAdapter(prizes) { prize ->
            showRedeemConfirmationDialog(prize)
        }

        binding.prizesList.adapter = adapter
        binding.prizesList.layoutManager = LinearLayoutManager(context)

        taskViewModel.totalPoints.observe(viewLifecycleOwner) {
            points -> binding.totalPointsText.text = "Total Points: ${points ?: 0}"
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
                    val newPrize = Prize(name, cost)
                    prizes.add(newPrize)
                    savePrizes()
                    adapter.notifyItemInserted(prizes.size - 1)
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
                    // Also remove the prize from the list, so it can only be redeemed once
                    prizes.remove(prize)
                    savePrizes()
                    adapter.notifyDataSetChanged()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun savePrizes() {
        val sharedPrefs = requireActivity().getSharedPreferences("prizes", Context.MODE_PRIVATE)
        val json = gson.toJson(prizes.filter { !isDefaultPrize(it) })
        sharedPrefs.edit().putString("custom_prizes", json).apply()
    }

    private fun loadPrizes() {
        val sharedPrefs = requireActivity().getSharedPreferences("prizes", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("custom_prizes", null)
        val type = object : TypeToken<MutableList<Prize>>() {}.type
        val customPrizes: MutableList<Prize> = gson.fromJson(json, type) ?: mutableListOf()

        prizes.clear()
        prizes.addAll(listOf(
            Prize("Movie Night", 25),
            Prize("Ice Cream", 10),
            Prize("New Book", 50)
        ))
        prizes.addAll(customPrizes)
    }

    private fun isDefaultPrize(prize: Prize): Boolean {
        return prize.name == "Movie Night" || prize.name == "Ice Cream" || prize.name == "New Book"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
