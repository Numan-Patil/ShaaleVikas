package com.project.shaalevikas

data class Need(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val school: String = "",
    val priority: String = "",
    val estimatedCost: Double = 0.0,
    val currentAmount: Double = 0.0,
    val status: String = "open",
    val beforePhoto: String = "",
    val afterPhoto: String = ""
)