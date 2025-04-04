package pt.ipp.estg.cmugrupo8.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.cmugrupo8.data.local.entities.*

@Dao
interface ClassificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classification: Classification)

    @Update
    suspend fun update(classification: Classification)

    @Delete
    suspend fun delete(classification: Classification)

    @Query("SELECT * FROM Classification WHERE id = :classificationId")
    suspend fun getClassificationById(classificationId: Int): Classification?

    @Query("SELECT * FROM Classification WHERE tripId = :tripId")
    fun getClassificationsByTripId(tripId: Int): LiveData <List<Classification>>

    @Query("SELECT * FROM Classification WHERE userId = :userId")
    fun getClassificationsByUserId(userId: Int): LiveData <List<Classification>>
}
