package pt.ipp.estg.cmugrupo8.data.remote.repository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmugrupo8.data.remote.Classification

class ClassificationRepository: FirestoreRepository() {

    private val collectionName = "classifications"


    // Cria uma nova classificação
    suspend fun createClassification(classification: Classification): Boolean {
        return try {
            val classificationData = hashMapOf(
                "tripId" to classification.tripId,
                "driverClassification" to classification.driverClassification,
                "userClassification" to classification.userClassification,
                "userId" to classification.userId,
                "driverId" to classification.driverId
            )

            // Salva a classificação no Firestore
            db.collection("classifications")
                .add(classificationData)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Obtém todas as classificações de uma viagem específica
    suspend fun getClassificationsByTripId(tripId: String): List<Classification> {
        return try {
            val snapshot: QuerySnapshot = db.collection("classifications")
                .whereEqualTo("tripId", tripId)
                .get()
                .await()

            snapshot.documents.map { document ->
                document?.toObject(Classification::class.java)?.apply {
                    id = document.id// Atribui o ID do Firestore
                } ?: Classification()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Obtém a classificação de um usuário específico para uma viagem
    suspend fun getUserClassificationForTrip(tripId: String, userId: String): Classification? {
        return try {
            val snapshot = db.collection("classifications")
                .whereEqualTo("tripId", tripId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()
            document?.toObject(Classification::class.java)?.apply {
                id = document.id // Atribui o ID do Firestore
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllClassificationsByUserId(userId: String): List<Classification> {
        return try {
            val classifications = mutableListOf<Classification>()

            // Obtenha as classificações do Firestore
            val snapshot = db.collection("classifications")
                .whereEqualTo("userId", userId) // Filtra pelas classificações do usuário
                .get()
                .await() // Aguarda a resposta de forma assíncrona

            // Processa os documentos retornados
            for (document in snapshot.documents) {
                val classification = document.toObject(Classification::class.java)
                classification?.let {
                    it.id = document.id // Atribui o ID do documento ao campo id
                    classifications.add(it)
                }
            }

            classifications // Retorna a lista de classificações
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Retorna uma lista vazia em caso de erro
        }
    }

    suspend fun getAverageClassificationBydriverId(driverId: String): Double {
        return try {

            val snapshot = db.collection("classifications")
                .whereEqualTo("driverId", driverId)
                .get()
                .await()

            var total = 0.0
            var count = 0

            for (document in snapshot.documents) {
                val classification = document.toObject(Classification::class.java)
                classification?.let {
                    total += it.driverClassification
                    count++
                }
            }

            if (count > 0) {
                total / count
            } else {
                0.0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    // Atualiza uma classificação existente
    suspend fun updateClassification(classificationId: String, updatedData: Map<String, Any>): Boolean {
        return try {
            db.collection("classifications")
                .document(classificationId)
                .update(updatedData)
                .await()  // Aguarda a operação assíncrona
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Remove uma classificação
    suspend fun deleteClassification(classificationId: String): Boolean {
        return try {
            db.collection("classifications")
                .document(classificationId)
                .delete()
                .await()  // Aguarda a operação assíncrona
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}