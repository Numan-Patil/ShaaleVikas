package com.project.shaalevikas

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.io.File

class NeedAdapter(private val needs: MutableList<Need>) :
    RecyclerView.Adapter<NeedAdapter.NeedViewHolder>() {

    class NeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSchool: TextView = itemView.findViewById(R.id.tvSchool)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val btnPledge: MaterialButton = itemView.findViewById(R.id.btnPledge)
        val ivPriority: ImageView = itemView.findViewById(R.id.ivPriority)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_need, parent, false)
        return NeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: NeedViewHolder, position: Int) {
        val need = needs[position]

        holder.tvTitle.text = need.title
        holder.tvSchool.text = need.school
        holder.tvDescription.text = need.description

        // Priority badge
        when (need.priority) {
            "high" -> {
                holder.tvPriority.text = "High Priority"
                holder.ivPriority.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.colorAccentPink))
            }
            "medium" -> {
                holder.tvPriority.text = "Medium Priority"
                holder.ivPriority.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.colorAccentYellow))
            }
            else -> {
                holder.tvPriority.text = "Low Priority"
                holder.ivPriority.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.colorAccentGreen))
            }
        }

        // Progress
        val percent = if (need.estimatedCost > 0)
            ((need.currentAmount / need.estimatedCost) * 100).toInt()
        else 0

        holder.tvProgress.text = "₹${need.currentAmount.toInt()} of ₹${need.estimatedCost.toInt()} pledged"
        holder.progressBar.progress = percent

        // Initial Button State
        if (need.currentAmount >= need.estimatedCost) {
            holder.btnPledge.text = "Impact Created"
            holder.btnPledge.setBackgroundResource(R.drawable.bg_impact_created_button)
            holder.btnPledge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorTextPrimary))
            holder.btnPledge.setIconResource(R.drawable.ic_check)
            holder.btnPledge.setIconTintResource(R.color.colorTextPrimary)
            holder.btnPledge.isEnabled = false
        } else {
            holder.btnPledge.text = "Pledge Support"
            holder.btnPledge.setBackgroundResource(R.drawable.bg_pledge_button)
            holder.btnPledge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.btnPledge.setIconResource(R.drawable.ic_pledge)
            holder.btnPledge.setIconTintResource(R.color.white)
            holder.btnPledge.isEnabled = true
        }

        // Pledge button
        holder.btnPledge.setOnClickListener {
            showPledgeDialog(holder, need)
        }

        // Admin Long Press for CRUD (Delete/Update)
        val isAdmin = holder.itemView.context.getSharedPreferences("ShaaleVikasPrefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("isAdmin", false)
        
        if (isAdmin) {
            holder.itemView.setOnLongClickListener {
                showAdminOptions(holder, position)
                true
            }
        }

        // Show Detail on Click
        holder.itemView.setOnClickListener {
            showNeedDetailDialog(holder, need)
        }
    }

    private fun showNeedDetailDialog(holder: NeedViewHolder, need: Need) {
        val context = holder.itemView.context
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_need_detail, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvDetailTitle).text = need.title
        view.findViewById<TextView>(R.id.tvDetailSchool).text = need.school
        view.findViewById<TextView>(R.id.tvDetailCost).text = "Estimated: ₹${need.estimatedCost.toInt()}"
        view.findViewById<TextView>(R.id.tvDetailDesc).text = need.description
        
        val img = view.findViewById<ImageView>(R.id.imgDetailPhoto)
        if (need.beforePhoto.isNotEmpty()) {
            try {
                val file = File(need.beforePhoto)
                if (file.exists()) {
                    img.setImageURI(Uri.fromFile(file))
                } else {
                    img.setImageResource(R.drawable.ic_camera)
                }
            } catch (e: Exception) {
                img.setImageResource(R.drawable.ic_camera)
            }
        }

        view.findViewById<MaterialButton>(R.id.btnCloseDetail).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAdminOptions(holder: NeedViewHolder, position: Int) {
        val context = holder.itemView.context
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_admin_options, null)
        dialog.setContentView(view)

        view.findViewById<MaterialButton>(R.id.btnEdit).setOnClickListener {
            dialog.dismiss()
            showEditNeedDialog(holder, position)
        }

        view.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            dialog.dismiss()
            needs.removeAt(position)
            LocalDataManager(context).saveNeeds(needs)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, needs.size)
            Toast.makeText(context, "Project deleted", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showEditNeedDialog(holder: NeedViewHolder, position: Int) {
        val context = holder.itemView.context
        val need = needs[position]
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_need, null)
        dialog.setContentView(view)

        val tvHeader = view.findViewById<TextView>(R.id.tvHeader)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etSchool = view.findViewById<EditText>(R.id.etSchool)
        val etDesc = view.findViewById<EditText>(R.id.etDescription)
        val etCost = view.findViewById<EditText>(R.id.etCost)
        val chipGroupPriority = view.findViewById<ChipGroup>(R.id.chipGroupPriority)
        val ivPreview = view.findViewById<ImageView>(R.id.ivProjectPreview)
        val btnUpload = view.findViewById<View>(R.id.btnUploadProjectPhoto)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveNeed)

        // Pre-fill data
        tvHeader.text = "Edit Project"
        etTitle.setText(need.title)
        etSchool.setText(need.school)
        etDesc.setText(need.description)
        etCost.setText(need.estimatedCost.toString())
        
        if (need.beforePhoto.isNotEmpty()) {
            val file = File(need.beforePhoto)
            if (file.exists()) ivPreview.setImageURI(Uri.fromFile(file))
        }

        when (need.priority) {
            "high" -> chipGroupPriority.check(R.id.chipHigh)
            "medium" -> chipGroupPriority.check(R.id.chipMedium)
            else -> chipGroupPriority.check(R.id.chipLow)
        }
        
        // Note: Full image upload in edit requires more plumbing,
        // for now we'll keep the current photo or let user know.
        btnUpload.setOnClickListener {
            Toast.makeText(context, "Photo update currently only for new projects", Toast.LENGTH_SHORT).show()
        }

        btnSave.text = "Update Project"

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val school = etSchool.text.toString()
            val desc = etDesc.text.toString()
            val cost = etCost.text.toString().toDoubleOrNull() ?: 0.0
            
            val priority = when (chipGroupPriority.checkedChipId) {
                R.id.chipHigh -> "high"
                R.id.chipMedium -> "medium"
                else -> "low"
            }

            if (title.isEmpty() || school.isEmpty() || cost <= 0) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedNeed = need.copy(
                title = title,
                school = school,
                description = desc,
                estimatedCost = cost,
                priority = priority
            )

            needs[position] = updatedNeed
            LocalDataManager(context).saveNeeds(needs)
            notifyItemChanged(position)
            dialog.dismiss()
            Toast.makeText(context, "Project updated", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showPledgeDialog(holder: NeedViewHolder, need: Need) {
        val context = holder.itemView.context
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_pledge, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val tvRemaining = view.findViewById<TextView>(R.id.tvRemainingAmount)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupAmounts)
        val etCustom = view.findViewById<EditText>(R.id.etCustomAmount)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirmPledge)

        val remainingAmount = need.estimatedCost - need.currentAmount
        tvTitle.text = need.title
        tvRemaining.text = "Remaining to be funded: ₹${remainingAmount.toInt()}"

        // Disable chips that are more than remaining
        view.findViewById<Chip>(R.id.chip500).isEnabled = remainingAmount >= 500
        view.findViewById<Chip>(R.id.chip1000).isEnabled = remainingAmount >= 1000
        view.findViewById<Chip>(R.id.chip2000).isEnabled = remainingAmount >= 2000
        view.findViewById<Chip>(R.id.chip5000).isEnabled = remainingAmount >= 5000

        btnConfirm.setOnClickListener {
            val selectedChipId = chipGroup.checkedChipId
            val amountStr = etCustom.text.toString()
            
            val commitAmount = if (amountStr.isNotEmpty()) {
                amountStr.toDoubleOrNull() ?: 0.0
            } else if (selectedChipId != View.NO_ID) {
                val chip = view.findViewById<Chip>(selectedChipId)
                chip.text.toString().replace("₹", "").toDoubleOrNull() ?: 0.0
            } else {
                0.0
            }

            if (commitAmount <= 0) {
                Toast.makeText(context, "Please select or enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (commitAmount > remainingAmount) {
                Toast.makeText(context, "Amount exceeds remaining balance (₹${remainingAmount.toInt()})", Toast.LENGTH_SHORT).show()
                etCustom.setText(remainingAmount.toInt().toString())
                return@setOnClickListener
            }

            // Update Need progress (In a real app, this would be a DB update)
            val newAmount = (need.currentAmount + commitAmount).coerceAtMost(need.estimatedCost)
            val newPercent = ((newAmount / need.estimatedCost) * 100).toInt()

            holder.tvProgress.text = "₹${newAmount.toInt()} of ₹${need.estimatedCost.toInt()} pledged"
            holder.progressBar.progress = newPercent

            // Update in list and save
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                needs[adapterPosition] = needs[adapterPosition].copy(currentAmount = newAmount)
                val dm = LocalDataManager(context)
                dm.saveNeeds(needs)
                
                // Record contributor
                dm.recordPledge(dm.getCurrentUserName(), dm.getCurrentUserBatch(), commitAmount)
            }

            Toast.makeText(context, "₹${commitAmount.toInt()} pledged for: ${need.title}", Toast.LENGTH_SHORT).show()

            if (newAmount >= need.estimatedCost) {
                holder.btnPledge.text = "Impact Created"
                holder.btnPledge.setBackgroundResource(R.drawable.bg_impact_created_button)
                holder.btnPledge.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))
                holder.btnPledge.setIconResource(R.drawable.ic_check)
                holder.btnPledge.setIconTintResource(R.color.colorTextPrimary)
            } else {
                holder.btnPledge.text = context.getString(R.string.pledged)
                holder.btnPledge.setIconResource(R.drawable.ic_check)
            }
            holder.btnPledge.isEnabled = false
            
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount() = needs.size
}