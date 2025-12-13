package com.divora.toodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PointLedgerAdapter : ListAdapter<PointLedger, PointLedgerAdapter.PointLedgerViewHolder>(PointLedgerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            PointLedgerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ledger_entry, parent, false)
        return PointLedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointLedgerViewHolder, position: Int) {
        val ledgerEntry = getItem(position)
        holder.bind(ledgerEntry)
    }

    class PointLedgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ledgerDescriptionView: TextView = itemView.findViewById(R.id.ledger_description)
        private val ledgerPointsView: TextView = itemView.findViewById(R.id.ledger_points)

        fun bind(ledgerEntry: PointLedger) {
            ledgerDescriptionView.text = ledgerEntry.description
            val pointsText = if (ledgerEntry.points >= 0) "+${ledgerEntry.points}" else ledgerEntry.points.toString()
            ledgerPointsView.text = pointsText
        }
    }
}

class PointLedgerDiffCallback : DiffUtil.ItemCallback<PointLedger>() {
    override fun areItemsTheSame(oldItem: PointLedger, newItem: PointLedger): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PointLedger, newItem: PointLedger): Boolean {
        return oldItem == newItem
    }
}
