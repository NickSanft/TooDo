package com.divora.toodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PrizesAdapter(
    private val prizes: List<Prize>,
    private val onRedeemClicked: (Prize) -> Unit
) : RecyclerView.Adapter<PrizesAdapter.PrizeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            PrizeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_prize, parent, false)
        return PrizeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrizeViewHolder, position: Int) {
        val prize = prizes[position]
        holder.bind(prize, onRedeemClicked)
    }

    override fun getItemCount(): Int = prizes.size

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
