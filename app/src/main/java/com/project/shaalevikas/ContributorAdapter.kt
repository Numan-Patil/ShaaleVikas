package com.project.shaalevikas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ContributorAdapter(private val contributors: List<Contributor>) :
    RecyclerView.Adapter<ContributorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val ivMedal: ImageView = itemView.findViewById(R.id.ivMedal)
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvBatch: TextView = itemView.findViewById(R.id.tvBatch)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contributor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contributor = contributors[position]
        
        holder.tvAvatar.text = contributor.name.first().toString().uppercase()
        holder.tvName.text = contributor.name
        holder.tvBatch.text = contributor.batch
        holder.tvAmount.text = "₹${contributor.totalPledged.toInt()}"

        // Gold/silver/bronze icons for top 3
        when (position) {
            0 -> {
                holder.tvRank.visibility = View.GONE
                holder.ivMedal.visibility = View.VISIBLE
                holder.ivMedal.setImageResource(R.drawable.ic_medal)
                holder.ivMedal.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.gold))
            }
            1 -> {
                holder.tvRank.visibility = View.GONE
                holder.ivMedal.visibility = View.VISIBLE
                holder.ivMedal.setImageResource(R.drawable.ic_medal)
                holder.ivMedal.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.silver))
            }
            2 -> {
                holder.tvRank.visibility = View.GONE
                holder.ivMedal.visibility = View.VISIBLE
                holder.ivMedal.setImageResource(R.drawable.ic_medal)
                holder.ivMedal.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.bronze))
            }
            else -> {
                holder.tvRank.visibility = View.VISIBLE
                holder.ivMedal.visibility = View.GONE
                holder.tvRank.text = "${position + 1}"
            }
        }
    }

    override fun getItemCount() = contributors.size
}