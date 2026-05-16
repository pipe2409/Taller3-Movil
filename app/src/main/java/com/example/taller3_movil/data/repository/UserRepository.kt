package com.example.taller3_movil.data.repository

import com.example.taller3_movil.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun updateLocation(uid: String, lat: Double, lng: Double, connected: Boolean) {
        usersCollection.document(uid).update(
            mapOf(
                "latitude" to lat,
                "longitude" to lng,
                "connected" to connected
            )
        ).await()
    }

    suspend fun setConnectionStatus(uid: String, connected: Boolean) {
        usersCollection.document(uid).update("connected", connected).await()
    }

    fun getConnectedUsers(): Flow<List<User>> {
        return usersCollection
            .whereEqualTo("connected", true)
            .limit(100)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(User::class.java)
            }
    }
}
