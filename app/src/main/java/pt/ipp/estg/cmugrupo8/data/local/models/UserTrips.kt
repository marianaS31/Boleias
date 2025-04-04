package pt.ipp.estg.cmugrupo8.data.local.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import pt.ipp.estg.cmugrupo8.data.local.entities.*

data class UserTrips(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(UserTrip::class)
    )
    val trips: List<Trip>
)
