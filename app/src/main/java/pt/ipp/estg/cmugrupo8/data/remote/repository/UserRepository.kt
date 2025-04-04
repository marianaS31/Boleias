package pt.ipp.estg.cmugrupo8.data.remote.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmugrupo8.data.remote.User
import pt.ipp.estg.cmugrupo8.data.remote.Vehicle

private val b = false

class UserRepository : FirestoreRepository() {

    private val tag = "UserRepository"
    private val collectionName = "users"

    init {

    }

    // Função para criar um novo utilizador no Firestore
    suspend fun createUser(user: User): Boolean {
        return try {
            storeInFirebase(collectionName, user, user.id)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Função para obter um utilizador por ID
    suspend fun getUserById(userId: String): User? {
        return try {
            Log.d(tag, "Get User by Id: $userId")
            val snapshot = db.collection(collectionName).document(userId).get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                Log.d(tag, "User successfully retrieved: $user")
                user
            } else {
                Log.d(tag, "No user found with ID: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting user by ID: $userId", e)
            null
        }
    }

    suspend fun updateUser(userId: String, updatedData: Map<String, Any>): Boolean {
        return try {
            db.collection(collectionName).document(userId).update(updatedData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Função para eliminar um utilizador
    suspend fun deleteUser(userId: String): Boolean {
        return try {
            val updates = mapOf("active" to false) // Cria um mapa com o campo a atualizar
            db.collection(collectionName).document(userId).update(updates).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

        // Função para obter todos os utilizadores
    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = db.collection(collectionName).get().await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
