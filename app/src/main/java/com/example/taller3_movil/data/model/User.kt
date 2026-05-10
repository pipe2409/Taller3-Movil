package com.example.taller3_movil.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val identification: String = "",
    val email: String = "",
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val connected: Boolean = false,
    val profilePictureUrl: String = ""
)
