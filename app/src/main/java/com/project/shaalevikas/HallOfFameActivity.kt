package com.project.shaalevikas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HallOfFameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hall_of_fame)

        // Adjust for system bars
        val header = findViewById<View>(R.id.header)
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = 24.dpToPx() + systemBars.top)
            insets
        }

        val localDB = LocalDataManager(this)
        val contributors = localDB.getContributors()
        
        // Sort by total pledged amount (Descending)
        contributors.sortByDescending { it.totalPledged }

        val rv = findViewById<RecyclerView>(R.id.rvContributors)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ContributorAdapter(contributors)

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_hall_of_fame
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_impact -> {
                    startActivity(Intent(this, ImpactActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_hall_of_fame -> true
                else -> false
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}