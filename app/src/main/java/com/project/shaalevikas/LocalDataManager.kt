package com.project.shaalevikas

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

class LocalDataManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ShaaleVikasLocalDB", Context.MODE_PRIVATE)
    private val gson = Gson()

    // --- Image Storage Helper ---
    fun saveImageToInternalStorage(uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // --- User Data ---
    fun saveUserRole(email: String, isAdmin: Boolean) {
        prefs.edit().putBoolean("role_$email", isAdmin).apply()
    }

    fun isUserAdmin(email: String): Boolean {
        return prefs.getBoolean("role_$email", false)
    }

    // --- Needs Data ---
    fun saveNeeds(needs: List<Need>) {
        val json = gson.toJson(needs)
        prefs.edit().putString("saved_needs", json).apply()
    }

    fun getNeeds(): MutableList<Need> {
        val json = prefs.getString("saved_needs", null)
        return if (json == null) {
            mutableListOf() // Return empty list initially
        } else {
            val type = object : TypeToken<MutableList<Need>>() {}.type
            gson.fromJson<MutableList<Need>>(json, type) ?: mutableListOf()
        }
    }

    // --- Impact Data ---
    fun saveImpacts(impacts: List<Need>) {
        val json = gson.toJson(impacts)
        prefs.edit().putString("saved_impacts", json).apply()
    }

    fun getImpacts(): MutableList<Need> {
        val json = prefs.getString("saved_impacts", null)
        return if (json == null) {
            mutableListOf() // Return empty list initially
        } else {
            val type = object : TypeToken<MutableList<Need>>() {}.type
            gson.fromJson<MutableList<Need>>(json, type) ?: mutableListOf()
        }
    }

    // --- Contributor Data ---
    fun saveContributors(contributors: List<Contributor>) {
        val json = gson.toJson(contributors)
        prefs.edit().putString("saved_contributors", json).apply()
    }

    fun getContributors(): MutableList<Contributor> {
        val json = prefs.getString("saved_contributors", null)
        return if (json == null) {
            mutableListOf()
        } else {
            val type = object : TypeToken<MutableList<Contributor>>() {}.type
            gson.fromJson<MutableList<Contributor>>(json, type) ?: mutableListOf()
        }
    }

    fun recordPledge(userName: String, userBatch: String, amount: Double) {
        val current = getContributors()
        val index = current.indexOfFirst { it.name == userName && it.batch == userBatch }
        
        if (index != -1) {
            current[index] = current[index].copy(totalPledged = current[index].totalPledged + amount)
        } else {
            current.add(Contributor(userName, userBatch, amount))
        }
        
        saveContributors(current)
    }

    fun getCurrentUserName(): String {
        return prefs.getString("current_user_name", "Anonymous Alumni") ?: "Anonymous Alumni"
    }

    fun getCurrentUserBatch(): String {
        return prefs.getString("current_user_batch", "Batch of 2024") ?: "Batch of 2024"
    }

    fun saveLoginProfile(name: String, batch: String) {
        prefs.edit().putString("current_user_name", name)
            .putString("current_user_batch", batch).apply()
    }
}