package com.project.shaalevikas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etAdminCode = findViewById<TextInputEditText>(R.id.etAdminCode)
        val tilAdminCode = findViewById<TextInputLayout>(R.id.tilAdminCode)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val rbAdmin = findViewById<RadioButton>(R.id.rbAdmin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // Show admin code field if admin is selected
        rgRole.setOnCheckedChangeListener { _, checkedId ->
            tilAdminCode.visibility = if (checkedId == R.id.rbAdmin) View.VISIBLE else View.GONE
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val isAdmin = rbAdmin.isChecked
            val adminCode = etAdminCode.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isAdmin && adminCode != "SHAALE114") {
                Toast.makeText(this, "Invalid admin code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserProfile(auth.currentUser?.uid, name, email, isAdmin)
                    } else {
                        val errorMessage = task.exception?.message ?: ""
                        if (errorMessage.contains("already in use", ignoreCase = true)) {
                            // SELF-HEALING: If account exists in Auth, try to login and fix the Firestore profile
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { signInTask ->
                                    if (signInTask.isSuccessful) {
                                        saveUserProfile(auth.currentUser?.uid, name, email, isAdmin)
                                    } else {
                                        Toast.makeText(this, "Email is already in use. Please use the correct password or try another email.", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Registration failed: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun saveUserProfile(userId: String?, name: String, email: String, isAdmin: Boolean) {
        if (userId == null) return

        // 1. Local Persistence (Works without billing/online)
        val dataManager = LocalDataManager(this)
        dataManager.saveUserRole(email, isAdmin)

        // 2. local sync for current session
        val prefs = getSharedPreferences("ShaaleVikasPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("isAdmin", isAdmin).apply()

        // 3. Online Backup (Try, but ignore if billing/online issues)
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "isAdmin" to isAdmin
        )

        // Save current session name
        dataManager.saveLoginProfile(name, "Batch of 2024") // Default batch for now

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile synced online!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // Proceed even if cloud database fails
                Toast.makeText(this, "Local account created! Cloud backup skipped (Billing required).", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}