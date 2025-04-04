package pt.ipp.estg.cmugrupo8.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.cmugrupo8.data.local.entities.*

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment)

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM Payment WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: Int): Payment?

    @Query("SELECT * FROM Payment WHERE tripId = :tripId")
    fun getPaymentsByTripId(tripId: Int): LiveData <List<Payment>>

    @Query("SELECT * FROM Payment")
    fun getAllPayments(): LiveData <List<Payment>>
}