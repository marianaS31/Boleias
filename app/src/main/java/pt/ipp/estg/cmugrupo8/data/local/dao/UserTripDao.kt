package pt.ipp.estg.cmugrupo8.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.cmugrupo8.data.local.entities.*

@Dao
interface UserTripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userTrip: UserTrip)

    @Delete
    suspend fun delete(userTrip: UserTrip)

    @Query("SELECT * FROM UserTrip WHERE userId = :userId")
    suspend fun getTripsByUserId(userId: Int): UserTrip?

    @Query("SELECT * FROM UserTrip WHERE tripId = :tripId")
    fun getUsersByTripId(tripId: Int): LiveData <List<UserTrip>>
}
