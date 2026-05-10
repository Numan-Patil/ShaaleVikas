package com.project.shaalevikas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var adapter: NeedAdapter
    private val needsList = mutableListOf<Need>()
    private lateinit var localDB: LocalDataManager
    private lateinit var rvNeeds: RecyclerView
    
    private var selectedProjectPhotoUri: Uri? = null
    private var activeDialog: BottomSheetDialog? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedProjectPhotoUri = it
            activeDialog?.findViewById<ImageView>(R.id.ivProjectPreview)?.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        localDB = LocalDataManager(this)
        val prefs = getSharedPreferences("ShaaleVikasPrefs", MODE_PRIVATE)
        val isAdmin = prefs.getBoolean("isAdmin", false)

        // FAB for admin
        val fabAddNeed = findViewById<ImageButton>(R.id.fabAddNeed)
        if (isAdmin) {
            fabAddNeed.visibility = View.VISIBLE
            fabAddNeed.setOnClickListener {
                showAddNeedDialog()
            }
        }

        // Adjust for system bars
        val topBar = findViewById<View>(R.id.topBar)
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = 20.dpToPx() + systemBars.top)
            insets
        }

        // Logout
        findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            getSharedPreferences("ShaaleVikasPrefs", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Sample needs data (load from Local DB instead of hardcoding list here)
        val savedNeeds = localDB.getNeeds()
        // Sort by ID (timestamp) descending to show latest first
        needsList.addAll(savedNeeds.sortedByDescending { it.id })

        // Setup RecyclerView
        rvNeeds = findViewById(R.id.rvNeeds)
        rvNeeds.layoutManager = LinearLayoutManager(this)
        adapter = NeedAdapter(needsList)
        rvNeeds.adapter = adapter

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_impact -> {
                    startActivity(Intent(this, ImpactActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_hall_of_fame -> {
                    startActivity(Intent(this, HallOfFameActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun showAddNeedDialog() {
        val dialog = BottomSheetDialog(this)
        activeDialog = dialog
        val view = layoutInflater.inflate(R.layout.dialog_add_need, null)
        dialog.setContentView(view)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etSchool = view.findViewById<EditText>(R.id.etSchool)
        val etDesc = view.findViewById<EditText>(R.id.etDescription)
        val etCost = view.findViewById<EditText>(R.id.etCost)
        val chipGroupPriority = view.findViewById<ChipGroup>(R.id.chipGroupPriority)
        val btnUpload = view.findViewById<View>(R.id.btnUploadProjectPhoto)
        val btnSave = view.findViewById<Button>(R.id.btnSaveNeed)

        selectedProjectPhotoUri = null

        btnUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

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
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Copy image to permanent local storage
            val permanentImagePath = selectedProjectPhotoUri?.let { 
                localDB.saveImageToInternalStorage(it) 
            } ?: ""

            val newNeed = Need(
                id = System.currentTimeMillis().toString(),
                title = title,
                school = school,
                description = desc,
                estimatedCost = cost,
                priority = priority,
                beforePhoto = permanentImagePath
            )

            needsList.add(0, newNeed)
            localDB.saveNeeds(needsList)
            adapter.notifyItemInserted(0)
            rvNeeds.scrollToPosition(0)
            dialog.dismiss()
            activeDialog = null
            Toast.makeText(this, "Project added successfully", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}