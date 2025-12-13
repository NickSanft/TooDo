package com.divora.toodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PrizesAdapter(
    private val onRedeemClicked: (Prize) -> Unit,
    private val onEditClicked: (Prize) -> Unit,
    private val onDeleteClicked: (Prize) -> Unit
) : ListAdapter<Prize, PrizesAdapter.PrizeViewHolder>(PrizeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            PrizeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_prize, parent, false)
        return PrizeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrizeViewHolder, position: Int) {
        val prize = getItem(position)
        holder.bind(prize, onRedeemClicked, onEditClicked, onDeleteClicked)
    }

    class PrizeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val prizeNameView: TextView = itemView.findViewById(R.id.prize_name)
        private val prizeCostView: TextView = itemView.findViewById(R.id.prize_cost)
        private val redeemButton: Button = itemView.findViewById(R.id.redeem_button)
        private val customPrizeActions: LinearLayout = itemView.findViewById(R.id.custom_prize_actions)
        private val editButton: Button = itemView.findViewById(R.id.edit_button)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_button)

        fun bind(
            prize: Prize,
            onRedeemClicked: (Prize) -> Unit,
            onEditClicked: (Prize) -> Unit,
            onDeleteClicked: (Prize) -> Unit
        ) {
            prizeNameView.text = prize.name
            prizeCostView.text = "${prize.cost} points"

            val isDefault = isDefaultPrize(prize)
            redeemButton.isVisible = isDefault
            customPrizeActions.isVisible = !isDefault

            if (isDefault) {
                redeemButton.contentDescription = "Redeem prize: ${prize.name}"
                redeemButton.setOnClickListener {
                    onRedeemClicked(prize)
                }
            } else {
                editButton.setOnClickListener { onEditClicked(prize) }
                deleteButton.setOnClickListener { onDeleteClicked(prize) }
            }
        }

        private fun isDefaultPrize(prize: Prize): Boolean {
            return prize.name == "Movie Night" || prize.name == "Ice Cream" || prize.name == "New Book"
        }
    }
}

class PrizeDiffCallback : DiffUtil.ItemCallback<Prize>() {
    override fun areItemsTheSame(oldItem: Prize, newItem: Prize): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Prize, newItem: Prize): Boolean {
        return oldItem == newItem
    }
}
