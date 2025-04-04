package pt.ipp.estg.cmugrupo8.data.local.models

import androidx.room.Embedded
import androidx.room.Relation
import pt.ipp.estg.cmugrupo8.data.local.entities.*

data class TripPayments(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val payments: List<Payment>
)