package pt.ipp.estg.cmugrupo8.data.remote.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmugrupo8.data.remote.Trip
import pt.ipp.estg.cmugrupo8.data.remote.User


class TripRepository : FirestoreRepository() {
    private val collectionName = "trips"


    suspend fun createTrip(trip: Trip): Boolean {
        return try {
            val documentRef = db.collection(collectionName).add(trip).await()
            val generatedId = documentRef.id

            db.collection(collectionName)
                .document(generatedId)
                .update("id", generatedId)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Função para obter todas as viagens
    suspend fun getAllTrips(): List<Trip> {
        return try {
            val tripsSnapshot = db.collection("trips").get().await()
            val tripsList = tripsSnapshot.documents.mapNotNull { document ->
                document.toObject(Trip::class.java)
            }
            tripsList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Função para obter viagens de um motorista específico
    suspend fun getTripsByDriver(driverId: String): List<Trip> {
        return try {
            val tripsSnapshot = db.collection("trips")
                .whereEqualTo("driverId", driverId)
                .get().await()

            val tripsList = tripsSnapshot.documents.mapNotNull { document ->
                document.toObject(Trip::class.java)
            }
            tripsList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Função para obter as viagens de um utilizador específico (por userId)
    suspend fun getTripsByUser(userId: String): List<Trip> {
        return try {
            val tripsSnapshot = db.collection("trips")
                .whereArrayContains("passengerIds", userId)
                .get().await()

            val tripsList = tripsSnapshot.documents.mapNotNull { document ->
                document.toObject(Trip::class.java)
            }
            tripsList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Função para obter uma viagem específica por ID
    suspend fun getTripById(tripId: String): Trip? {
        return try {
            val tripDocument = db.collection("trips").document(tripId).get().await()
            tripDocument.toObject(Trip::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Função para atualizar os detalhes de uma viagem
    suspend fun updateTrip(tripId: String, updatedTrip: Trip): Boolean {
        return try {
            db.collection("trips").document(tripId)
                .set(updatedTrip)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Função para remover uma viagem
    suspend fun deleteTrip(tripId: String): Boolean {
        return try {
            db.collection("trips").document(tripId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Função para adicionar um passageiro a uma viagem
    suspend fun addPassengerToTrip(tripId: String): Boolean {
        return try {
            val tripRef = db.collection("trips").document(tripId)
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            firebaseUser?.uid?.let { uid ->
                tripRef.update("passengerIds", FieldValue.arrayUnion(uid)).await()
                true
            } ?: run {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    // Função para remover um passageiro de uma viagem
    suspend fun removePassengerFromTrip(tripId: String): Boolean {
        return try {
            val tripRef = db.collection("trips").document(tripId)
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            firebaseUser?.uid?.let { uid ->
                tripRef.update("passengerIds", FieldValue.arrayRemove(uid)).await()
                true
            } ?: run {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Função para remover todos os passageiros de uma viagem
    suspend fun removeAllPassengersFromTrip(tripId: String): Boolean {
        return try {
            // Obter a referência à viagem
            val tripRef = db.collection("trips").document(tripId)

            // Atualizar o campo "passengerIds" para uma lista vazia
            tripRef.update("passengerIds", FieldValue.delete()).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    // Função para adicionar um local a uma viagem
    suspend fun addLocationToTrip(tripId: String, location: GeoPoint): Boolean {
        return try {
            // Obter a referência à viagem
            val tripRef = db.collection("trips").document(tripId)

            // Adicionar o local à lista de locais usando arrayUnion
            tripRef.update("locations", FieldValue.arrayUnion(location))
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Função para remover um local de uma viagem
    suspend fun removeLocationFromTrip(tripId: String, location: GeoPoint): Boolean {
        return try {
            // Obter a referência à viagem
            val tripRef = db.collection("trips").document(tripId)

            // Remover o local da lista de locais usando arrayRemove
            tripRef.update("locations", FieldValue.arrayRemove(location))
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
