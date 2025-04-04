package pt.ipp.estg.cmugrupo8.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

abstract class FirestoreRepository {

    protected val db: FirebaseFirestore = Firebase.firestore

    suspend fun <T : Any> storeInFirebase(collectionName: String, obj: T, id: String): Boolean {
        return try {
            db.collection(collectionName).document(id).set(obj).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
