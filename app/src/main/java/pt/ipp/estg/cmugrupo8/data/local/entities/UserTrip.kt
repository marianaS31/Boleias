package pt.ipp.estg.cmugrupo8.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["userId", "tripId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"]),
        ForeignKey(entity = Trip::class, parentColumns = ["id"], childColumns = ["tripId"])
    ]
)
data class UserTrip(
    val userId: Int,
    val tripId: Int
)
