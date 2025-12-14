package com.divora.toodo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.divora.toodo.databinding.FragmentPrizesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

class PrizesFragment : Fragment(), FabClickHandler {

    private var _binding: FragmentPrizesBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var prizesViewModel: PrizesViewModel
    private lateinit var pointLedgerViewModel: PointLedgerViewModel
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
        pointLedgerViewModel = ViewModelProvider(this).get(PointLedgerViewModel::class.java)

        adapter = PrizesAdapter(
            onRedeemClicked = { prize -> showRedeemConfirmationDialog(prize) },
            onEditClicked = { prize -> showEditPrizeDialog(prize) },
            onDeleteClicked = { prize -> showDeleteConfirmationDialog(prize) }
        )

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

        MaterialAlertDialogBuilder(requireContext())
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

    private fun showEditPrizeDialog(prize: Prize) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_prize, null)
        val prizeNameInput = dialogView.findViewById<EditText>(R.id.prize_name_input)
        val prizeCostInput = dialogView.findViewById<EditText>(R.id.prize_cost_input)

        prizeNameInput.setText(prize.name)
        prizeCostInput.setText(prize.cost.toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Prize")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = prizeNameInput.text.toString()
                val cost = prizeCostInput.text.toString().toIntOrNull() ?: 0
                if (name.isNotBlank()) {
                    val updatedPrize = prize.copy(name = name, cost = cost)
                    prizesViewModel.updatePrize(updatedPrize)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(prize: Prize) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Prize")
            .setMessage("Are you sure you want to delete \"${prize.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                prizesViewModel.removePrize(prize)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRedeemConfirmationDialog(prize: Prize) {
        val currentPoints = taskViewModel.totalPoints.value ?: 0
        if (currentPoints < prize.cost) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Not Enough Points")
                .setMessage("You do not have enough points to redeem this prize.")
                .setPositiveButton("OK", null)
                .show()
        } else {
            MaterialAlertDialogBuilder(requireContext())
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

                    val ledgerEntry = PointLedger(
                        description = "Redeemed: ${prize.name}",
                        points = -prize.cost,
                        timestamp = System.currentTimeMillis()
                    )
                    pointLedgerViewModel.insert(ledgerEntry)

                    prizesViewModel.removePrize(prize)

                    if (!isTestMode()) {
                        // Trigger the confetti animation
                        binding.konfettiView.start(
                            Party(
                                speed = 0f,
                                maxSpeed = 30f,
                                damping = 0.9f,
                                spread = 360,
                                colors = listOf(Color.YELLOW, Color.GREEN, Color.MAGENTA),
                                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                                position = Position.Relative(0.5, 0.3)
                            )
                        )
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun isTestMode(): Boolean {
        return try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
