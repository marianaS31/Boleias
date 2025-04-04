package pt.ipp.estg.cmugrupo8.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmugrupo8.data.remote.Payment

class PaymentRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createPayment(payment: Payment): Boolean {
        return try {
            val paymentRef = db.collection("payments").document() // Cria um novo documento
            paymentRef.set(payment.copy(id = paymentRef.id)) // Atribui o ID do Firestore ao objeto Payment
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Função para obter todos os pagamentos de uma viagem
    suspend fun getPaymentsByTripId(tripId: String): List<Payment> {
        return try {
            val payments = mutableListOf<Payment>()
            val snapshot = db.collection("payments")
                .whereEqualTo("tripId", tripId) // Filtra pagamentos pela viagem
                .get()
                .await() // Aguarda a resposta de forma assíncrona

            for (document in snapshot.documents) {
                val payment = document.toObject(Payment::class.java)
                payment?.let {
                    it.id = document.id // Atribui o ID do documento ao campo id
                    payments.add(it)
                }
            }
            payments
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Retorna uma lista vazia em caso de erro
        }
    }

    // Função para obter todos os pagamentos de um usuário
    suspend fun getPaymentsByUserId(userId: String): List<Payment> {
        return try {
            val payments = mutableListOf<Payment>()
            val snapshot = db.collection("payments")
                .whereEqualTo("userId", userId) // Filtra pagamentos pelo usuário
                .get()
                .await() // Aguarda a resposta de forma assíncrona

            for (document in snapshot.documents) {
                val payment = document.toObject(Payment::class.java)
                payment?.let {
                    it.id = document.id // Atribui o ID do documento ao campo id
                    payments.add(it)
                }
            }
            payments
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Retorna uma lista vazia em caso de erro
        }
    }

    // Função para excluir um pagamento
    suspend fun deletePayment(paymentId: String): Boolean {
        return try {
            db.collection("payments").document(paymentId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
