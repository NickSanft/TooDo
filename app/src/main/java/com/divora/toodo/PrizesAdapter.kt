package com.divora.toodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PrizesAdapter(
    private val onRedeemClicked: (Prize) -> Unit
) : ListAdapter<Prize, PrizesAdapter.PrizeViewHolder>(PrizeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            PrizeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_prize, parent, false)
        return PrizeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrizeViewHolder, position: Int) {
        val prize = getItem(position)
        holder.bind(prize, onRedeemClicked)
    }

    class PrizeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val prizeNameView: TextView = itemView.findViewById(R.id.prize_name)
        private val prizeCostView: TextView = itemView.findViewById(R.id.prize_cost)
        private val redeemButton: Button = itemView.findViewById(R.id.redeem_button)

        fun bind(prize: Prize, onRedeemClicked: (Prize) -> Unit) {
            prizeNameView.text = prize.name
            prizeCostView.text = "${prize.cost} points"
            redeemButton.contentDescription = "Redeem prize: ${prize.name}"
            redeemButton.setOnClickListener {
                onRedeemClicked(prize)
            }
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
