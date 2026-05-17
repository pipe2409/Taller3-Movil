package com.example.taller3_movil.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.taller3_movil.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // Intentamos obtener la instancia del bucket que tienes en tu JSON
    private val storage: FirebaseStorage by lazy {
        try {
            FirebaseStorage.getInstance("gs://taller3-movil-7b103.firebasestorage.app")
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "No se pudo inicializar con URL gs://, usando default")
            FirebaseStorage.getInstance()
        }
    }

    suspend fun register(user: User, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val uid = result.user?.uid ?: throw Exception("Error al crear usuario")
            val userWithId = user.copy(uid = uid)
            firestore.collection("users").document(uid).set(userWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() = auth.signOut()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getUserData(uid: String): User? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserData(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            auth.currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String {
        return try {
            Log.d("FirebaseStorage", "Bucket actual: ${storage.reference.bucket}")
            val ref = storage.reference.child("profile_pictures/$uid.jpg")
            
            // Usar un Stream es lo más seguro para evitar problemas de permisos de la Uri
            val inputStream = context.contentResolver.openInputStream(imageUri) 
                ?: throw Exception("No se pudo abrir el archivo local")
            
            Log.d("FirebaseStorage", "Iniciando subida para: $uid")
            
            // Subir y esperar
            ref.putStream(inputStream).await()
            
            // Obtener la URL
            val url = ref.downloadUrl.await().toString()
            Log.d("FirebaseStorage", "URL obtenida con éxito: $url")
            url
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error en el proceso de subida: ${e.message}")
            throw e
        }
    }
}
