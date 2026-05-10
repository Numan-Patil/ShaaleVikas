package com.project.shaalevikas

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val rbAdmin = findViewById<RadioButton>(R.id.rbAdmin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val wantAdmin = rbAdmin.isChecked

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val dataManager = LocalDataManager(this)
                        val actualIsAdmin = dataManager.isUserAdmin(email)

                        if (wantAdmin && !actualIsAdmin) {
                            auth.signOut()
                            Toast.makeText(this, "This account is not an Admin locally. Please register as Admin.", Toast.LENGTH_LONG).show()
                        } else if (!wantAdmin && actualIsAdmin) {
                            auth.signOut()
                            Toast.makeText(this, "This is an Admin account. Select Admin role.", Toast.LENGTH_LONG).show()
                        } else {
                            // Proceed
                            val prefs = getSharedPreferences("ShaaleVikasPrefs", MODE_PRIVATE)
                            prefs.edit().putBoolean("isAdmin", actualIsAdmin).apply()
                            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}