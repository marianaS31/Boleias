package pt.ipp.estg.cmugrupo8.data.remote.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmugrupo8.data.remote.User
import pt.ipp.estg.cmugrupo8.data.remote.Vehicle

class VehicleRepository : FirestoreRepository() {

    private val tag = "VehicleRepository"
    private val collectionName = "vehicles"  // Coleção para veículos no Firestore
    private val usersCollectionName = "users"

    // Função para criar um novo veículo
    suspend fun createVehicle(vehicle: Vehicle): Boolean {
        return try {
            // Obtém o userId do utilizador autenticado no Firebase
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Log.e(tag, "User is not authenticated")
                return false
            }

            // Verifica se o utilizador já tem um veículo associado
            val userSnapshot = db.collection(usersCollectionName).document(userId).get().await()
            val user = userSnapshot.toObject(User::class.java)

            // Se o utilizador já tiver um veículo associado, retorna false
            if (user?.id != "" && db.collection(collectionName).whereEqualTo("userId", userId).get().await().documents.isNotEmpty()) {
                Log.e(tag, "User already has a vehicle associated")
                return false
            }

            // Cria um novo veículo associando ao userId
            val vehicleRef = db.collection(collectionName).document()  // Cria um novo documento com ID gerado automaticamente
            val vehicleId = vehicleRef.id  // Obtém o ID gerado automaticamente para o veículo
            val vehicleWithUserId = vehicle.copy(id = vehicleId, userId = userId)  // Adiciona o userId ao veículo

            // Adiciona o veículo à coleção de veículos
            vehicleRef.set(vehicleWithUserId).await()

            // Associa o veículo ao utilizador, neste caso não precisamos atualizar a coleção de users pois o veículo já contém o userId
            Log.d(tag, "Vehicle successfully created for userId: $userId with vehicleId: $vehicleId")
            true
        } catch (e: Exception) {
            Log.e(tag, "Error creating vehicle", e)
            false
        }
    }
    suspend fun getVehicleByUserId(userId: String): Vehicle? {
        return try {
            // Procura o veículo com o userId correspondente na coleção de veículos
            val snapshot = db.collection(collectionName).whereEqualTo("userId", userId).get().await()

            // Verifica se foi encontrado algum veículo para o utilizador
            if (snapshot.isEmpty) {
                Log.d(tag, "No vehicle found for user with ID: $userId")
                return null
            }

            // Assume que há apenas um veículo para cada utilizador
            val vehicle = snapshot.documents.firstOrNull()?.toObject(Vehicle::class.java)

            // Log de sucesso ou falha
            if (vehicle != null) {
                Log.d(tag, "Vehicle found for user with ID: $userId: $vehicle")
            } else {
                Log.d(tag, "Failed to retrieve vehicle for user with ID: $userId")
            }

            vehicle
        } catch (e: Exception) {
            Log.e(tag, "Error retrieving vehicle for user with ID: $userId", e)
            null
        }
    }

    // Função para obter um veículo pelo ID
    suspend fun getVehicleById(vehicleId: String): Vehicle? {
        return try {
            val snapshot = db.collection(collectionName).document(vehicleId).get().await()
            if (snapshot.exists()) {
                val vehicle = snapshot.toObject(Vehicle::class.java)
                Log.d(tag, "Vehicle successfully retrieved: $vehicle")
                vehicle
            } else {
                Log.d(tag, "No vehicle found with ID: $vehicleId")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting vehicle by ID: $vehicleId", e)
            null
        }
    }

    suspend fun updateVehicle(vehicle: Vehicle): Boolean {
        return try {
            // Atualiza os dados do veículo no Firestore usando o ID do veículo
            val vehicleRef = db.collection("vehicles").document(vehicle.id) // Documento com o ID do veículo
            vehicleRef.set(vehicle).await()  // Usa o set() para atualizar os dados do veículo
            true
        } catch (e: Exception) {
            Log.e("VehicleRepository", "Erro ao atualizar veículo", e)
            false
        }
    }

    // Função para excluir um veículo
    suspend fun deleteVehicle(vehicleId: String): Boolean {
        return try {
            db.collection("vehicles").document(vehicleId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Função para obter todos os veículos de um utilizador específico
    suspend fun getVehiclesByUserId(userId: String): List<Vehicle> {
        return try {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("userId", userId)  // Filtra por userId para pegar os veículos daquele utilizador
                .get().await()

            val vehicles = snapshot.documents.mapNotNull { it.toObject(Vehicle::class.java) }
            Log.d(tag, "Vehicles successfully retrieved for userId: $userId")
            vehicles
        } catch (e: Exception) {
            Log.e(tag, "Error getting vehicles for userId: $userId", e)
            emptyList()
        }
    }
}