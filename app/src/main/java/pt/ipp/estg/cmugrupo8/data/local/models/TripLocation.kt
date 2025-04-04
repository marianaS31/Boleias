package pt.ipp.estg.cmugrupo8.data.local.models

import androidx.room.*
import pt.ipp.estg.cmugrupo8.data.local.entities.*

data class TripLocations(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val locations: List<Location>
)