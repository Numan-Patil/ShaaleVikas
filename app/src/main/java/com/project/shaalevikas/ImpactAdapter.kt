package com.project.shaalevikas

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.io.File

class ImpactAdapter(private val completedNeeds: MutableList<Need>) :
    RecyclerView.Adapter<ImpactAdapter.ImpactViewHolder>() {

    class ImpactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSchool: TextView = itemView.findViewById(R.id.tvSchool)
        val imgAfter: ImageView = itemView.findViewById(R.id.imgAfter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImpactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_impact, parent, false)
        return ImpactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImpactViewHolder, position: Int) {
        val need = completedNeeds[position]
        holder.tvTitle.text = need.title
        holder.tvSchool.text = need.school

        // Load image if path exists
        if (need.afterPhoto.isNotEmpty()) {
            try {
                val file = File(need.afterPhoto)
                if (file.exists()) {
                    holder.imgAfter.setImageURI(Uri.fromFile(file))
                } else {
                    holder.imgAfter.setImageResource(R.drawable.ic_camera)
                }
            } catch (e: Exception) {
                holder.imgAfter.setImageResource(R.drawable.ic_camera)
            }
        } else {
            holder.imgAfter.setImageResource(R.drawable.ic_camera)
        }

        // Vary height for pinterest effect (dp to px)
        val density = holder.itemView.context.resources.displayMetrics.density
        val heightDp = if (position % 3 == 0) 200 else if (position % 3 == 1) 150 else 250
        
        val layoutParams = holder.imgAfter.layoutParams
        layoutParams.height = (heightDp * density).toInt()
        holder.imgAfter.layoutParams = layoutParams

        holder.itemView.setOnClickListener {
            showComparisonDialog(holder.itemView.context, need)
        }

        // Admin Long Press for CRUD
        val isAdmin = holder.itemView.context.getSharedPreferences("ShaaleVikasPrefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("isAdmin", false)

        if (isAdmin) {
            holder.itemView.setOnLongClickListener {
                showAdminOptions(holder, position)
                true
            }
        }
    }

    private fun showAdminOptions(holder: ImpactViewHolder, position: Int) {
        val context = holder.itemView.context
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_admin_options, null)
        dialog.setContentView(view)

        view.findViewById<MaterialButton>(R.id.btnEdit).setOnClickListener {
            dialog.dismiss()
            showEditImpactDialog(holder, position)
        }

        view.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            dialog.dismiss()
            completedNeeds.removeAt(position)
            LocalDataManager(context).saveImpacts(completedNeeds)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, completedNeeds.size)
            Toast.makeText(context, "Impact story deleted", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showEditImpactDialog(holder: ImpactViewHolder, position: Int) {
        val context = holder.itemView.context
        val impact = completedNeeds[position]
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_impact, null)
        dialog.setContentView(view)

        val tvHeader = view.findViewById<TextView>(R.id.tvHeader)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etSchool = view.findViewById<EditText>(R.id.etSchool)
        val ivBeforePreview = view.findViewById<ImageView>(R.id.ivBeforePreview)
        val ivAfterPreview = view.findViewById<ImageView>(R.id.ivAfterPreview)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveImpact)

        // Pre-fill
        tvHeader.text = "Edit Success Story"
        etTitle.setText(impact.title)
        etSchool.setText(impact.school)
        btnSave.text = "Update Story"

        if (impact.beforePhoto.isNotEmpty()) {
            val file = File(impact.beforePhoto)
            if (file.exists()) ivBeforePreview.setImageURI(Uri.fromFile(file))
        }
        if (impact.afterPhoto.isNotEmpty()) {
            val file = File(impact.afterPhoto)
            if (file.exists()) ivAfterPreview.setImageURI(Uri.fromFile(file))
        }

        // Note: For full edit of images, we'd need to bridge back to Activity for results
        // or just keep them as they are in this simple implementation.
        
        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val school = etSchool.text.toString()

            if (title.isEmpty() || school.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedImpact = impact.copy(
                title = title,
                school = school
            )

            completedNeeds[position] = updatedImpact
            LocalDataManager(context).saveImpacts(completedNeeds)
            notifyItemChanged(position)
            dialog.dismiss()
            Toast.makeText(context, "Story updated", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showComparisonDialog(context: android.content.Context, need: Need) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_before_after, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvComparisonTitle).text = need.title
        
        val imgBefore = view.findViewById<ImageView>(R.id.imgBeforeFull)
        val imgAfter = view.findViewById<ImageView>(R.id.imgAfterFull)

        if (need.beforePhoto.isNotEmpty()) {
            val file = File(need.beforePhoto)
            if (file.exists()) imgBefore.setImageURI(Uri.fromFile(file))
        }
        if (need.afterPhoto.isNotEmpty()) {
            val file = File(need.afterPhoto)
            if (file.exists()) imgAfter.setImageURI(Uri.fromFile(file))
        }

        view.findViewById<MaterialButton>(R.id.btnCloseDialog).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount() = completedNeeds.size
}