package pt.ipp.estg.cmugrupo8.data.local.models

import androidx.room.*
import pt.ipp.estg.cmugrupo8.data.local.entities.*

data class UserVehicle(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val vehicle: Vehicle
)