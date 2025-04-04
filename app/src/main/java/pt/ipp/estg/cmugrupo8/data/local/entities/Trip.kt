package pt.ipp.estg.cmugrupo8.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import com.google.type.DateTime
import java.time.LocalDateTime

@Entity
data class Trip(
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val driverId: Int,
    val startPointId: Int,
    val finishPointId: Int,
    val dateTime_Start: Long,
    val time_expected : String,
    val cost: Float
)