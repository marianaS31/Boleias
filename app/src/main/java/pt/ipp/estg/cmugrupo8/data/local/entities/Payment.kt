package pt.ipp.estg.cmugrupo8.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    foreignKeys = [
        ForeignKey(entity = Trip::class, parentColumns = ["id"], childColumns = ["tripId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"])
    ]
)
data class Payment(
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val paymentMethod: String,
    val cost: Float,
    val userId: Int
)