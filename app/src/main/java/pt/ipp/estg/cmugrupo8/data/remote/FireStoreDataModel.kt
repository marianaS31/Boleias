package pt.ipp.estg.cmugrupo8.data.remote

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

// Modelo para Pagamento
@Keep
@IgnoreExtraProperties
data class Payment(
    var id: String = "", // ID único gerado pelo Firebase
    val tripId: String = "", // Referência para a viagem
    val userId: String = "", // Referência para o utilizador
    val amount: Double = 0.0,
    val paymentMethod: String = "unknown", // e.g., "cash", "credit card"
    val date: Timestamp = Timestamp.now()
)

// Modelo para Viagem
@Keep
@IgnoreExtraProperties
data class Trip(
    val id: String = "",
    val name: String = "",
    val driverId: String = "",
    val startPoint: String = "",
    val finishPoint: String = "",
    val dateStart: String = "",
    val hourStart: String = "",
    val passengerIds: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val maxPassengers: String ="",
    val tripPrice: String = ""
)


@Keep
@IgnoreExtraProperties
data class User(
    val id: String = "", // ID único gerado pelo Firebase
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val image: String = "",
    val drivingLicense: String = "",
    val isActive: Boolean = true,
    val isAdmin: Boolean = false,
)

// Modelo para Veículo
@Keep
@IgnoreExtraProperties
data class Vehicle(
    val id: String = "", // ID único gerado pelo Firebase
    val userId: String = "",
    val plateNumber: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val year: String = "",
    val capacity: String = "4" // Alterado para Int para maior precisão
)

// Modelo para Classificação
@Keep
@IgnoreExtraProperties
data class Classification(
    var id: String = "",
    val tripId: String = "",
    val driverClassification: Int = 0,
    val userClassification: Int = 0,
    val userId: String = "",
    val driverId: String =""

)
