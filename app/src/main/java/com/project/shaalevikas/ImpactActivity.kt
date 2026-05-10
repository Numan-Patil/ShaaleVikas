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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

class ImpactActivity : AppCompatActivity() {

    private lateinit var adapter: ImpactAdapter
    private val impactList = mutableListOf<Need>()
    private lateinit var localDB: LocalDataManager
    private lateinit var rvImpact: RecyclerView
    
    private var beforeImageUri: Uri? = null
    private var afterImageUri: Uri? = null
    private var currentSelectingBefore = true

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (currentSelectingBefore) {
                beforeImageUri = it
                activeDialog?.findViewById<ImageView>(R.id.ivBeforePreview)?.setImageURI(it)
            } else {
                afterImageUri = it
                activeDialog?.findViewById<ImageView>(R.id.ivAfterPreview)?.setImageURI(it)
            }
        }
    }

    private var activeDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_impact)

        localDB = LocalDataManager(this)
        val prefs = getSharedPreferences("ShaaleVikasPrefs", MODE_PRIVATE)
        val isAdmin = prefs.getBoolean("isAdmin", false)

        // FAB for admin
        val fabAddImpact = findViewById<ImageButton>(R.id.fabAddImpact)
        if (isAdmin) {
            fabAddImpact.visibility = View.VISIBLE
            fabAddImpact.setOnClickListener {
                showAddImpactDialog()
            }
        }

        // Adjust for system bars
        val topBar = findViewById<View>(R.id.topBar)
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = 20.dpToPx() + systemBars.top)
            insets
        }

        // 1. Load explicit Impact Stories
        val savedImpacts = localDB.getImpacts()
        // Sort by ID (timestamp) descending to show latest first
        impactList.addAll(savedImpacts.sortedByDescending { it.id })

        rvImpact = findViewById(R.id.rvImpact)
        rvImpact.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = ImpactAdapter(impactList)
        rvImpact.adapter = adapter

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_impact
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_impact -> true
                R.id.nav_hall_of_fame -> {
                    startActivity(Intent(this, HallOfFameActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun showAddImpactDialog() {
        val dialog = BottomSheetDialog(this)
        activeDialog = dialog
        val view = layoutInflater.inflate(R.layout.dialog_add_impact, null)
        dialog.setContentView(view)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etSchool = view.findViewById<EditText>(R.id.etSchool)
        val btnBefore = view.findViewById<View>(R.id.btnUploadBefore)
        val btnAfter = view.findViewById<View>(R.id.btnUploadAfter)
        val btnSave = view.findViewById<Button>(R.id.btnSaveImpact)

        beforeImageUri = null
        afterImageUri = null

        btnBefore.setOnClickListener {
            currentSelectingBefore = true
            pickImageLauncher.launch("image/*")
        }

        btnAfter.setOnClickListener {
            currentSelectingBefore = false
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val school = etSchool.text.toString()

            if (title.isEmpty() || school.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Copy images to permanent local storage
            val permanentBeforePath = beforeImageUri?.let { localDB.saveImageToInternalStorage(it) } ?: ""
            val permanentAfterPath = afterImageUri?.let { localDB.saveImageToInternalStorage(it) } ?: ""

            val newImpact = Need(
                id = System.currentTimeMillis().toString(),
                title = title,
                school = school,
                status = "completed",
                beforePhoto = permanentBeforePath,
                afterPhoto = permanentAfterPath
            )

            impactList.add(0, newImpact)
            localDB.saveImpacts(impactList)
            adapter.notifyItemInserted(0)
            rvImpact.scrollToPosition(0)
            dialog.dismiss()
            activeDialog = null
            Toast.makeText(this, "Impact story added!", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}